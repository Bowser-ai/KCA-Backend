package mocks

import com.mv.data.FiliaalRepository
import com.mv.models.Filiaal
import zio.{URLayer, ZIO, ZLayer}
import zio.mock.*

import java.sql.SQLException

object MockFilialenRepository extends Mock[FiliaalRepository] {
  object GetFilialen extends Effect[(Int, Int), SQLException, List[Filiaal]]

  object GetFiliaalByNumber extends Effect[Int, SQLException, Option[Filiaal]]

  object CreateFilialen extends Effect[List[Filiaal], SQLException, Unit]

  object UpdateFiliaal extends Effect[Filiaal, SQLException, Unit]

  object DeleteFiliaal extends Effect[Int, SQLException, Unit]

  object GetTotalCount extends Effect[Unit, SQLException, Long]

  val compose: URLayer[Proxy, FiliaalRepository] =
    ZLayer {
      for {
        proxy <- ZIO.service[Proxy]
      } yield new FiliaalRepository {
        override def getFilialen(
            pageSize: Int,
            offset: Int = 0
        ): ZIO[Any, SQLException, List[Filiaal]] =
          proxy(GetFilialen, pageSize, offset)

        override def getFiliaalByNumber(
            filiaalNumber: Int
        ): ZIO[Any, SQLException, Option[Filiaal]] =
          proxy(GetFiliaalByNumber, filiaalNumber)

        override def createFilialen(
            filialen: List[Filiaal]
        ): ZIO[Any, SQLException, Unit] =
          proxy(CreateFilialen, filialen)

        override def updateFiliaal(
            filiaal: Filiaal
        ): ZIO[Any, SQLException, Unit] =
          proxy(UpdateFiliaal, filiaal)

        override def deleteFiliaal(
            filiaalNumber: Int
        ): ZIO[Any, SQLException, Unit] =
          proxy(DeleteFiliaal, filiaalNumber)

        override def getTotalCount: ZIO[Any, SQLException, Long] =
          proxy(GetTotalCount)
      }
    }
}
