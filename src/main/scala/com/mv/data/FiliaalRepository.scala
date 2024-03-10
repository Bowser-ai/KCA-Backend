package com.mv.data

import com.mv.models.Filiaal
import io.getquill.*
import zio.*

import java.sql.SQLException
import javax.sql.DataSource

trait FiliaalRepository {
  def getFilialen(
      pageSize: Int,
      offSet: Int = 0
  ): ZIO[Any, SQLException, List[Filiaal]]
  def getFiliaalByNumber(
      filiaalNumber: Int
  ): ZIO[Any, SQLException, Option[Filiaal]]
  def createFilialen(filialen: List[Filiaal]): ZIO[Any, SQLException, Unit]

  def updateFiliaal(filiaal: Filiaal): ZIO[Any, SQLException, Unit]

  def deleteFiliaal(filiaalNumber: Int): ZIO[Any, SQLException, Unit]

  def getTotalCount: ZIO[Any, SQLException, Long]
}

object FiliaalRepository {
  val live: ZLayer[DataSource, Nothing, FiliaalRepository] =
    ZLayer.fromFunction((dataSource: DataSource) =>
      new FiliaalRepository {
        import ctx.*

        private val dsLayer = ZLayer.succeed(dataSource)

        override def getFilialen(
            pageSize: Int,
            offset: Int = 0
        ): ZIO[Any, SQLException, List[Filiaal]] =
          run(
            query[Filiaal]
              .sortBy(_.filiaalNumber)
              .drop(lift(offset))
              .take(lift(pageSize))
          )
            .provide(dsLayer)

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

        override def getTotalCount: ZIO[Any, SQLException, Long] =
          run(quote { query[Filiaal].size }).provide(dsLayer)
      }
    )

  def getFilialen(
      pageSize: Int,
      offset: Int = 0
  ): ZIO[FiliaalRepository, SQLException, List[Filiaal]] =
    ZIO.serviceWithZIO(_.getFilialen(pageSize, offset))

  def getFiliaalByNumber(
      filiaalNumber: Int
  ): ZIO[FiliaalRepository, SQLException, Option[Filiaal]] =
    ZIO.serviceWithZIO(_.getFiliaalByNumber(filiaalNumber))

  def createFilialen(
      filialen: List[Filiaal]
  ): ZIO[FiliaalRepository, SQLException, Unit] =
    ZIO.serviceWithZIO(_.createFilialen(filialen))

  def updateFiliaal(
      filiaal: Filiaal
  ): ZIO[FiliaalRepository, SQLException, Unit] =
    ZIO.serviceWithZIO(_.updateFiliaal(filiaal))

  def deleteFiliaal(
      filiaalNumber: Int
  ): ZIO[FiliaalRepository, SQLException, Unit] =
    ZIO.serviceWithZIO(_.deleteFiliaal(filiaalNumber))

  def getTotalCount: ZIO[FiliaalRepository, SQLException, Long] =
    ZIO.serviceWithZIO(_.getTotalCount)
}
