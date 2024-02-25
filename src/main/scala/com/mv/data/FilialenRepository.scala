package com.mv.data

import com.mv.models.{Filiaal, Remark}
import io.getquill.*
import zio.*

import java.sql.SQLException
import java.time.LocalDateTime
import javax.sql.DataSource

trait FilialenRepository {
  def getFilialen: ZIO[Any, SQLException, List[Filiaal]]
  def getFiliaalByNumber(
      filiaalNumber: Int
  ): ZIO[Any, SQLException, Option[Filiaal]]
  def createFilialen(filialen: List[Filiaal]): ZIO[Any, SQLException, Unit]

  def updateFiliaal(filiaal: Filiaal): ZIO[Any, SQLException, Unit]

  def deleteFiliaal(filiaalNumber: Int): ZIO[Any, SQLException, Long]

  def getRemarks: ZIO[Any, SQLException, List[Remark]]
  def getRemarksByFiliaal(
      filiaalNumber: Int
  ): ZIO[Any, SQLException, List[Remark]]
  def getRemarkById(
      id: Int
  ): ZIO[Any, SQLException, Option[Remark]]
  def createRemark(filiaalId: Int, body: String): ZIO[Any, SQLException, Int]
  def updateRemark(id: Int, body: String): ZIO[Any, SQLException, Long]
}

object FilialenRepository {
  val live: ZLayer[DataSource, Nothing, FilialenRepository] =
    ZLayer.fromFunction((dataSource: DataSource) =>
      new FilialenRepository {
        import ctx.*

        private val dsLayer = ZLayer.succeed(dataSource)
        private inline def remarkTable = "mededeling"

        override def getFilialen: ZIO[Any, SQLException, List[Filiaal]] =
          run(query[Filiaal]).provide(dsLayer)

        override def getFiliaalByNumber(
            number: Int
        ): ZIO[Any, SQLException, Option[Filiaal]] =
          run(query[Filiaal].filter(_.filiaalNumber == lift(number)))
            .map(_.headOption)
            .provide(dsLayer)

        override def createFilialen(
            filialen: List[Filiaal]
        ): ZIO[Any, SQLException, Unit] =
          run(liftQuery(filialen).foreach(query[Filiaal].insertValue(_))).unit
            .provide(dsLayer)

        override def updateFiliaal(
            filiaal: Filiaal
        ): ZIO[Any, SQLException, Unit] =
          run(
            query[Filiaal]
              .filter(_.filiaalNumber == lift(filiaal.filiaalNumber))
              .update(
                _.info -> lift(filiaal.info),
                _.tel -> lift(filiaal.tel),
                _.address -> lift(filiaal.address),
                _.zipcode -> lift(filiaal.zipcode)
              )
          ).unit.provide(dsLayer)

        override def deleteFiliaal(
            filiaalNumber: Int
        ): ZIO[Any, SQLException, Long] =
          run(
            query[Filiaal].filter(_.filiaalNumber == lift(filiaalNumber)).delete
          ).provide(dsLayer)

        override def getRemarks: ZIO[Any, SQLException, List[Remark]] =
          run(quote {
            querySchema[Remark](
              remarkTable
            )
          })
            .provide(dsLayer)

        override def getRemarksByFiliaal(
            filiaalNumber: Int
        ): ZIO[Any, SQLException, List[Remark]] =
          run(quote {
            querySchema[Remark](remarkTable).filter(
              _.filiaalId == lift(filiaalNumber)
            )
          })
            .provide(dsLayer)

        override def createRemark(
            filiaalId: Int,
            body: String
        ): ZIO[Any, SQLException, Int] =
          run(quote {
            querySchema[Remark](remarkTable)
              .insert(
                _.filiaalId -> lift(filiaalId),
                _.body -> lift(body)
              )
              .returning(_.id)
          }).provide(dsLayer)

        override def updateRemark(
            id: Int,
            body: String
        ): ZIO[Any, SQLException, Long] =
          run(quote {
            querySchema[Remark](remarkTable)
              .filter(_.id == lift(id))
              .update(
                _.body -> lift(body),
                _.dateModified -> lift(LocalDateTime.now)
              )
          }).provide(dsLayer)

        override def getRemarkById(
            id: Int
        ): ZIO[Any, SQLException, Option[Remark]] =
          run(quote {
            querySchema[Remark](remarkTable)
              .filter(_.id == lift(id))
          }).map(_.headOption).provide(dsLayer)
      }
    )
  private val ctx = PostgresZioJdbcContext(SnakeCase)

  def getFilialen: ZIO[FilialenRepository, SQLException, List[Filiaal]] =
    ZIO.serviceWithZIO(_.getFilialen)

  def getFiliaalByNumber(
      filiaalNumber: Int
  ): ZIO[FilialenRepository, SQLException, Option[Filiaal]] =
    ZIO.serviceWithZIO(_.getFiliaalByNumber(filiaalNumber))

  def createFilialen(
      filialen: List[Filiaal]
  ): ZIO[FilialenRepository, SQLException, Unit] =
    ZIO.serviceWithZIO(_.createFilialen(filialen))

  def updateFiliaal(
      filiaal: Filiaal
  ): ZIO[FilialenRepository, SQLException, Unit] =
    ZIO.serviceWithZIO(_.updateFiliaal(filiaal))

  def deleteFiliaal(
      filiaalNumber: Int
  ): ZIO[FilialenRepository, SQLException, Long] =
    ZIO.serviceWithZIO(_.deleteFiliaal(filiaalNumber))

  def getRemarks: ZIO[FilialenRepository, SQLException, List[Remark]] =
    ZIO.serviceWithZIO(_.getRemarks)

  def getRemarksByFiliaal(
      filiaalNumber: Int
  ): ZIO[FilialenRepository, SQLException, List[Remark]] =
    ZIO.serviceWithZIO(_.getRemarksByFiliaal(filiaalNumber))

  def getRemarkById(
      id: Int
  ): ZIO[FilialenRepository, SQLException, Option[Remark]] =
    ZIO.serviceWithZIO(_.getRemarkById(id))
  def createRemark(
      filiaalId: Int,
      body: String
  ): ZIO[FilialenRepository, SQLException, Int] =
    ZIO.serviceWithZIO(_.createRemark(filiaalId, body))

  def updateRemark(
      id: Int,
      body: String
  ): ZIO[FilialenRepository, SQLException, Long] =
    ZIO.serviceWithZIO(_.updateRemark(id, body))
}
