package com.mv.data

import com.mv.models.{PartialRemark, Remark}
import io.getquill.*
import zio.{ZIO, ZLayer}

import java.sql.SQLException
import java.time.LocalDateTime
import javax.sql.DataSource

trait RemarkRepository {
  def getRemarks: ZIO[Any, SQLException, List[Remark]]

  def getRemarksByFiliaal(
      filiaalNumber: Int
  ): ZIO[Any, SQLException, List[Remark]]

  def getRemarkById(
      id: Int
  ): ZIO[Any, SQLException, Option[Remark]]

  def createRemark(
      remark: PartialRemark
  ): ZIO[Any, SQLException, Unit]

  def updateRemark(id: Int, remark: PartialRemark): ZIO[Any, SQLException, Unit]
}

object RemarkRepository {

  val live: ZLayer[DataSource, Nothing, RemarkRepository] = ZLayer(for {
    dataSource <- ZIO.service[DataSource]
  } yield new RemarkRepository {
    private val dsLayer = ZLayer.succeed(dataSource)
    private val ctx = PostgresZioJdbcContext(SnakeCase)
    import ctx.*

    override def getRemarks: ZIO[Any, SQLException, List[Remark]] =
      run(query[Remark]).provide(dsLayer)

    override def getRemarksByFiliaal(
        filiaalNumber: Int
    ): ZIO[Any, SQLException, List[Remark]] =
      run(quote {
        query[Remark].filter(
          _.filiaalId == lift(filiaalNumber)
        )
      })
        .provide(dsLayer)

    override def createRemark(
        remark: PartialRemark
    ): ZIO[Any, SQLException, Unit] =
      run(quote {
        query[Remark]
          .insert(
            _.filiaalId -> lift(remark.filiaalId),
            _.body -> lift(remark.body)
          )
          .returning(_.id)
      }).unit.provide(dsLayer)

    override def updateRemark(
        id: Int,
        remark: PartialRemark
    ): ZIO[Any, SQLException, Unit] =
      run(quote {
        query[Remark]
          .filter(_.id == lift(id))
          .update(
            _.body -> lift(remark.body),
            _.dateModified -> lift(LocalDateTime.now)
          )
      }).unit.provide(dsLayer)

    override def getRemarkById(
        id: Int
    ): ZIO[Any, SQLException, Option[Remark]] =
      run(quote {
        query[Remark]
          .filter(_.id == lift(id))
      }).map(_.headOption).provide(dsLayer)
  })
  def getRemarks: ZIO[RemarkRepository, SQLException, List[Remark]] =
    ZIO.serviceWithZIO(_.getRemarks)

  def getRemarksByFiliaal(
      filiaalNumber: Int
  ): ZIO[RemarkRepository, SQLException, List[Remark]] =
    ZIO.serviceWithZIO(_.getRemarksByFiliaal(filiaalNumber))

  def getRemarkById(
      id: Int
  ): ZIO[RemarkRepository, SQLException, Option[Remark]] =
    ZIO.serviceWithZIO(_.getRemarkById(id))

  def createRemark(
      remark: PartialRemark
  ): ZIO[RemarkRepository, SQLException, Unit] =
    ZIO.serviceWithZIO(_.createRemark(remark))

  def updateRemark(
      id: Int,
      remark: PartialRemark
  ): ZIO[RemarkRepository, SQLException, Unit] =
    ZIO.serviceWithZIO(_.updateRemark(id, remark))
}
