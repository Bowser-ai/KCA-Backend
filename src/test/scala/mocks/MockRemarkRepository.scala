package mocks

import com.mv.data.RemarkRepository
import com.mv.models.{PartialRemark, Remark}
import zio.{URLayer, ZIO, ZLayer}
import zio.mock.{Mock, Proxy}

import java.sql.SQLException

object MockRemarkRepository extends Mock[RemarkRepository] {
  object GetRemarks extends Effect[Unit, SQLException, List[Remark]]

  object GetRemarksByFiliaal extends Effect[Int, SQLException, List[Remark]]

  object GetRemarkById extends Effect[Int, SQLException, Option[Remark]]

  object CreateRemark extends Effect[PartialRemark, SQLException, Unit]

  object UpdateRemark extends Effect[(Int, PartialRemark), SQLException, Unit]

  val compose: URLayer[Proxy, RemarkRepository] =
    ZLayer(for {
      proxy <- ZIO.service[Proxy]
    } yield new RemarkRepository {

      override def getRemarks: ZIO[Any, SQLException, List[Remark]] =
        proxy(GetRemarks)

      override def getRemarksByFiliaal(
          filiaalNumber: Int
      ): ZIO[Any, SQLException, List[Remark]] =
        proxy(GetRemarksByFiliaal, filiaalNumber)

      override def getRemarkById(
          id: Int
      ): ZIO[Any, SQLException, Option[Remark]] =
        proxy(GetRemarkById, id)

      override def createRemark(
          remark: PartialRemark
      ): ZIO[Any, SQLException, Unit] = proxy(CreateRemark, remark)

      override def updateRemark(
          id: Int,
          remark: PartialRemark
      ): ZIO[Any, SQLException, Unit] = proxy(UpdateRemark, id, remark)
    })
}
