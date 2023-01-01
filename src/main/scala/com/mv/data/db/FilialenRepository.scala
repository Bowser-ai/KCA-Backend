package com.mv.data.db

import com.mv.configuration.Configuration
import com.mv.models.Remark.*
import com.mv.models.{Filiaal, Remark}
import io.getquill.*
import zio.*

import java.sql.SQLException
import javax.sql.DataSource

trait FilialenRepository {
  def getFilialen: ZIO[Any, SQLException, List[Filiaal]]
  def getFiliaalByNumber(
      filiaalNumber: Int
  ): ZIO[Any, SQLException, Option[Filiaal]]
  def getRemarks: ZIO[Any, SQLException, List[Remark]]
  def getRemarksByFiliaal(
      filiaalNumber: Int
  ): ZIO[Any, SQLException, List[Remark]]
  def createRemark(filiaalId: Int, body: String): ZIO[Any, SQLException, Long]
  def updateRemark(id: Int, body: String): ZIO[Any, SQLException, Long]
}

object FilialenRepository {
  private val ctx = PostgresZioJdbcContext(SnakeCase)

  def getFilialen: ZIO[FilialenRepository, SQLException, List[Filiaal]] =
    ZIO.serviceWithZIO[FilialenRepository](_.getFilialen)

  def getFiliaalByNumber(
      filiaalNumber: Int
  ): ZIO[FilialenRepository, SQLException, Option[Filiaal]] =
    ZIO.serviceWithZIO[FilialenRepository](_.getFiliaalByNumber(filiaalNumber))

  def getRemarks: ZIO[FilialenRepository, SQLException, List[Remark]] =
    ZIO.serviceWithZIO[FilialenRepository](_.getRemarks)

  def getRemarksByFiliaal(
      filiaalNumber: Int
  ): ZIO[FilialenRepository, SQLException, List[Remark]] =
    ZIO.serviceWithZIO[FilialenRepository](_.getRemarksByFiliaal(filiaalNumber))

  def createRemark(
      filiaalId: Int,
      body: String
  ): ZIO[FilialenRepository, SQLException, Long] =
    ZIO.serviceWithZIO[FilialenRepository](_.createRemark(filiaalId, body))

  def updateRemark(
      id: Int,
      body: String
  ): ZIO[FilialenRepository, SQLException, Long] =
    ZIO.serviceWithZIO[FilialenRepository](_.updateRemark(id, body))

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
        ): ZIO[Any, SQLException, Long] =
          run(quote {
            querySchema[Remark](remarkTable).insert(
              _.filiaalId -> lift(filiaalId),
              _.body -> lift(body)
            )
          }).provide(dsLayer)

        override def updateRemark(
            id: Int,
            body: String
        ): ZIO[Any, SQLException, Long] =
          run(quote {
            querySchema[Remark](remarkTable)
              .filter(_.id == lift(id))
              .update(
                _.body -> lift(body)
              )
          }).provide(dsLayer)
      }
    )
}
