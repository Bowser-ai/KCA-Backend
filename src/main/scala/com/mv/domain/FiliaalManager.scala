package com.mv.domain

import com.mv.data.FiliaalRepository
import com.mv.domain.errors.{ApplicationError, ClientError, NotFoundError}
import com.mv.domain.page_token.PageToken
import com.mv.models.{Filiaal, PartialFiliaal}
import zio.{IO, Task, ZIO, ZLayer}

import java.sql.SQLException
import java.util.Base64

sealed trait FiliaalManager {
  def list(
      pageSize: Option[Int]
  ): IO[ApplicationError, (List[Filiaal], Option[String])]

  def list(
      pageToken: String
  ): IO[ApplicationError, (List[Filiaal], Option[String])]

  def get(filiaalNumber: Int): IO[ApplicationError, Option[Filiaal]]

  def create(filialen: List[Filiaal]): IO[ApplicationError, List[Filiaal]]

  def update(
      filiaalNumber: Int,
      filiaal: PartialFiliaal
  ): IO[ApplicationError, Filiaal]

  def delete(filiaalNumber: Int): IO[ApplicationError, Filiaal]

}

object FiliaalManager {
  val live: ZLayer[FiliaalRepository, Nothing, FiliaalManager] = ZLayer(for {
    filialenRepository <- ZIO.service[FiliaalRepository]
  } yield new FiliaalManager {

    override def list(
        pageSize: Option[Int]
    ): IO[ApplicationError, (List[Filiaal], Option[String])] = (for {
      _ <- ZIO.unit
      realPageSize = pageSize.getOrElse(100)
      _ <- ZIO
        .fail(
          ClientError("Page size must be between 0 and 999(inclusive)")
        )
        .when(realPageSize < 0 || realPageSize > 999)
      (filialen, totalCount) <- getFilialenAndTotalCount(
        filialenRepository,
        realPageSize,
        offset = 0
      )
    } yield filialen -> getEncodedPageToken(
      realPageSize,
      offset = realPageSize,
      totalCount
    )).mapError {
      case e: ClientError => e
      case e: Throwable   => ApplicationError(e.getMessage)
    }

    override def list(
        pageToken: String
    ): IO[ApplicationError, (List[Filiaal], Option[String])] = (for {
      parsedPageToken <- parsePageToken(pageToken)
      (filialen, totalCount) <- getFilialenAndTotalCount(
        filialenRepository,
        parsedPageToken.pageSize,
        parsedPageToken.offset
      )
    } yield {
      import parsedPageToken.*
      filialen -> getEncodedPageToken(
        pageSize,
        offset + pageSize,
        totalCount
      )
    }).mapError(e => ApplicationError(e.getMessage))

    override def get(
        filiaalNumber: Int
    ): IO[ApplicationError, Option[Filiaal]] =
      filialenRepository
        .getFiliaalByNumber(filiaalNumber)
        .mapError(e => ApplicationError(e.getMessage))

    override def create(
        filialen: List[Filiaal]
    ): IO[ApplicationError, List[Filiaal]] =
      filialenRepository
        .createFilialen(filialen)
        .mapBoth(e => ApplicationError(e.getMessage), _ => filialen)

    override def update(
        filiaalNumber: Int,
        filiaal: PartialFiliaal
    ): IO[ApplicationError, Filiaal] = (for {
      _ <- getExistingFiliaal(filialenRepository, filiaalNumber)
      _ <- filialenRepository.updateFiliaal(filiaal.asFiliaal(filiaalNumber))
    } yield filiaal.asFiliaal(filiaalNumber)).mapError(mapNotFoundError)

    override def delete(filiaalNumber: Int): IO[ApplicationError, Filiaal] =
      (for {
        filiaal <- getExistingFiliaal(filialenRepository, filiaalNumber)
        _ <- filialenRepository.deleteFiliaal(filiaalNumber)
      } yield filiaal).mapError(mapNotFoundError)
  })

  def list(
      pageSize: Option[Int]
  ): ZIO[FiliaalManager, ApplicationError, (List[Filiaal], Option[String])] =
    ZIO.serviceWithZIO(_.list(pageSize))

  def list(
      pageToken: String
  ): ZIO[FiliaalManager, ApplicationError, (List[Filiaal], Option[String])] =
    ZIO.serviceWithZIO(_.list(pageToken))

  def get(
      filiaalNumber: Int
  ): ZIO[FiliaalManager, ApplicationError, Option[Filiaal]] =
    ZIO.serviceWithZIO(_.get(filiaalNumber))

  def create(
      filialen: List[Filiaal]
  ): ZIO[FiliaalManager, ApplicationError, List[Filiaal]] =
    ZIO.serviceWithZIO(_.create(filialen))

  def update(
      filiaalNumber: Int,
      filiaal: PartialFiliaal
  ): ZIO[FiliaalManager, ApplicationError, Filiaal] =
    ZIO.serviceWithZIO(_.update(filiaalNumber, filiaal))

  def delete(
      filiaalNumber: Int
  ): ZIO[FiliaalManager, ApplicationError, Filiaal] =
    ZIO.serviceWithZIO(_.delete(filiaalNumber))

  private def getExistingFiliaal(
      filialenRepository: FiliaalRepository,
      filiaalNumber: Int
  ): Task[Filiaal] =
    filialenRepository
      .getFiliaalByNumber(filiaalNumber)
      .flatMap(
        _.fold(
          ZIO.fail(
            NotFoundError(s"Filiaal: $filiaalNumber could not be found.")
          )
        )(ZIO.succeed(_))
      )

  private def getFilialenAndTotalCount(
      filiaalRepository: FiliaalRepository,
      pageSize: Int,
      offset: Int
  ): IO[SQLException, (List[Filiaal], Long)] =
    filiaalRepository.getFilialen(
      pageSize,
      offset
    ) <&> filiaalRepository.getTotalCount

  private def mapNotFoundError(
      t: Throwable
  ): ApplicationError =
    t match {
      case e: NotFoundError => NotFoundError(e.getMessage)
      case e: Throwable     => ApplicationError(e.getMessage)
    }
  private def getEncodedPageToken(
      pageSize: Int,
      offset: Int,
      totalCount: Long
  ): Option[String] = {
    val b64Encoder = Base64.getEncoder
    Option.when(offset < totalCount)(
      b64Encoder.encodeToString(
        PageToken(
          pageSize,
          Math.min(offset, totalCount.toInt - offset)
        ).toByteArray
      )
    )
  }

  private def parsePageToken(
      pageToken: String
  ): IO[ApplicationError, PageToken] =
    ZIO
      .attempt {
        val b64Decoder = Base64.getDecoder
        val decodedPageToken = b64Decoder.decode(pageToken)
        PageToken.parseFrom(decodedPageToken)
      }
      .mapError(e =>
        ApplicationError(
          s"Failed to parse page token, details: ${e.getMessage}"
        )
      )
}
