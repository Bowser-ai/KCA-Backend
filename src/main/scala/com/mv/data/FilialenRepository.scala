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

  def deleteFiliaal(filiaalNumber: Int): ZIO[Any, SQLException, Unit]
}

object FilialenRepository {
  val live: ZLayer[DataSource, Nothing, FilialenRepository] =
    ZLayer.fromFunction((dataSource: DataSource) =>
      new FilialenRepository {
        import ctx.*

        private val dsLayer = ZLayer.succeed(dataSource)

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
        ): ZIO[Any, SQLException, Unit] =
          run(
            query[Filiaal].filter(_.filiaalNumber == lift(filiaalNumber)).delete
          ).unit.provide(dsLayer)

        private val ctx = PostgresZioJdbcContext(SnakeCase)
      }
    )

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
  ): ZIO[FilialenRepository, SQLException, Unit] =
    ZIO.serviceWithZIO(_.deleteFiliaal(filiaalNumber))
}
