package com.mv.api.routes

import com.mv.api.Utils.sanitize
import com.mv.configuration.Configuration
import com.mv.data.FilialenRepository
import com.mv.models.Remark
import com.mv.models.Remark.given
import zio.ZIO
import zio.http.*
import zio.json.{DecoderOps, EncoderOps}

private[api] object RemarkRoutes {
  def make: ZIO[Configuration, Nothing, Routes[FilialenRepository, Throwable]] =
    ZIO.serviceWith[Configuration](conf =>
      Routes(
        Method.GET / conf.baseApiUrl / "mededelingen" -> handler(
          FilialenRepository.getRemarks
            .map(remarks => Response.json(remarks.toJson))
        ),
        Method.PUT / conf.baseApiUrl / "mededelingen" -> handler(
          (req: Request) =>
            req.body.asString
              .flatMap(body => {
                body
                  .fromJson[Remark] match
                  case Left(error) =>
                    ZIO.succeed(
                      Response
                        .text(error)
                        .status(Status.BadRequest)
                    )
                  case Right(remark) =>
                    FilialenRepository
                      .createRemark(remark.filiaalId, sanitize(remark.body))
                      .map(id => Response.json(id.toJson))
              })
        ),
        Method.PATCH / conf.baseApiUrl / "mededelingen" -> handler(
          (req: Request) =>
            req.body.asString.flatMap(body =>
              body.fromJson[Remark] match
                case Left(error) =>
                  ZIO.succeed(
                    Response.text(error).status(Status.BadRequest)
                  )
                case Right(filiaal) =>
                  FilialenRepository
                    .updateRemark(filiaal.id, sanitize(filiaal.body))
                    .as(Response.status(Status.NoContent))
            )
        )
      )
    )
}
