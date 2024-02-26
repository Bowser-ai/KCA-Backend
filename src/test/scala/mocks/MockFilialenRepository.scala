package mocks

import com.mv.data.FilialenRepository
import com.mv.models.Filiaal
import zio.{URLayer, ZIO, ZLayer}
import zio.mock.*

import java.sql.SQLException

object MockFilialenRepository extends Mock[FilialenRepository] {
  object GetFilialen extends Effect[Unit, SQLException, List[Filiaal]]

  object GetFiliaalByNumber extends Effect[Int, SQLException, Option[Filiaal]]

  object CreateFilialen extends Effect[List[Filiaal], SQLException, Unit]

  object UpdateFiliaal extends Effect[Filiaal, SQLException, Unit]

  object DeleteFiliaal extends Effect[Int, SQLException, Unit]

  val compose: URLayer[Proxy, FilialenRepository] =
    ZLayer {
      for {
        proxy <- ZIO.service[Proxy]
      } yield new FilialenRepository {
        override def getFilialen: ZIO[Any, SQLException, List[Filiaal]] =
          proxy(GetFilialen)

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
      }
    }
}
