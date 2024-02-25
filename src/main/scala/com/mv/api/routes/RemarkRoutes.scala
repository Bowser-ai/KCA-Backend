package com.mv.api.routes

import com.mv.api.Utils.sanitize
import com.mv.api.errors.InputDecodeError
import com.mv.configuration.Configuration
import com.mv.data.FilialenRepository
import com.mv.models.Remark
import com.mv.models.Remark.given
import zio.ZIO
import zio.http.*
import zio.json.{DecoderOps, EncoderOps}

private[api] object RemarkRoutes {
  def make: ZIO[Configuration, Nothing, Routes[
    FilialenRepository,
    Throwable
  ]] =
    ZIO.serviceWith[Configuration](conf =>
      Routes(
        Method.GET / conf.baseApiUrl / "mededelingen" -> handler(
          FilialenRepository.getRemarks
            .map(remarks => Response.json(remarks.toJson))
        ),
        Method.PUT / conf.baseApiUrl / "mededelingen" -> handler(
          (req: Request) =>
            for {
              body <- req.body.asString
              remark <- ZIO
                .fromEither(body.fromJson[Remark])
                .mapError(InputDecodeError.apply)
              id <- FilialenRepository
                .createRemark(remark.filiaalId, sanitize(remark.body))
            } yield Response.json(id.toJson)
        ),
        Method.PATCH / conf.baseApiUrl / "mededelingen" -> handler(
          (req: Request) =>
            for {
              body <- req.body.asString
              remark <- ZIO
                .fromEither(body.fromJson[Remark])
                .mapError(InputDecodeError.apply)
              _ <- FilialenRepository.updateRemark(
                remark.id,
                sanitize(remark.body)
              )
            } yield Response.status(Status.NoContent)
        )
      )
    )
}
