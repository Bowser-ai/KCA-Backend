package com.mv.api

import com.mv.data.db.FilialenRepository
import com.mv.models.Filiaal.*
import com.mv.models.Remark.*
import com.mv.models.{Remark, RemarkWithFiliaal}
import zhttp.http.*
import zio.ZIO
import zio.json.*

import java.sql.SQLException

object FilialenApi {
  val filialen: Http[FilialenRepository, Throwable, Request, Response] =
    Http.collectZIO[Request] {
      case Method.GET -> !! / "filialen" =>
        FilialenRepository.getFilialen.map(filialen =>
          Response.json(filialen.toJson)
        )
      case Method.GET -> !! / "filialen" / number =>
        FilialenRepository
          .getFiliaalByNumber(number.toInt)
          .map {
            case None          => Response.status(Status.NotFound)
            case Some(filiaal) => Response.json(filiaal.toJson)
          }
      case Method.GET -> !! / "mededelingen" =>
        FilialenRepository.getRemarks
          .map(remarks => Response.json(remarks.toJson))

      case req @ Method.POST -> !! / "mededelingen" =>
        req.body.asString
          .flatMap(body => {
            body
              .fromJson[RemarkWithFiliaal]
              .match
                case Left(error) =>
                  ZIO.succeed(
                    Response
                      .text(error)
                      .setStatus(Status.BadRequest)
                  )
                case Right(remark) =>
                  FilialenRepository
                    .createRemark(
                      remark.filiaalId,
                      remark.body
                    )
                    .as(Response.ok)
          })

    }
}
