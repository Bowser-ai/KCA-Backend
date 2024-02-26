package com.mv.api.routes

import com.mv.api.errors.InputDecodeError
import com.mv.configuration.Configuration
import com.mv.data.RemarkRepository
import com.mv.models.Remark.given
import com.mv.models.{PartialRemark, Remark}
import zio.ZIO
import zio.http.*
import zio.json.{DecoderOps, EncoderOps}

private[api] object RemarkRoutes {
  def make: ZIO[Configuration, Nothing, Routes[
    RemarkRepository,
    Throwable
  ]] =
    ZIO.serviceWith[Configuration](conf =>
      Routes(
        Method.GET / conf.baseApiUrl / "mededelingen" -> handler(
          RemarkRepository.getRemarks
            .map(remarks => Response.json(remarks.toJson))
        ),
        Method.PUT / conf.baseApiUrl / "mededelingen" -> handler(
          (req: Request) =>
            for {
              body <- req.body.asString
              remark <- ZIO
                .fromEither(body.fromJson[PartialRemark])
                .mapError(InputDecodeError.apply)
              _ <- RemarkRepository
                .createRemark(remark)
            } yield Response.json(remark.toJson)
        ),
        Method.PATCH / conf.baseApiUrl / "mededelingen" / int("id") -> handler(
          (id: Int, req: Request) =>
            for {
              body <- req.body.asString
              remarkToUpdate <- ZIO
                .fromEither(body.fromJson[Remark])
                .mapError(InputDecodeError.apply)
              existingRemark <- RemarkRepository.getRemarkById(id)
            } yield existingRemark.fold(Response.status(Status.NotFound))(_ =>
              Response.json(remarkToUpdate.toJson)
            )
        )
      )
    )
}
