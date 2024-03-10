package com.mv.api

import com.mv.api.errors.InputDecodeError
import com.mv.api.middleware.Cors
import com.mv.api.routes.{FiliaalRoutes, RemarkRoutes}
import com.mv.configuration.Configuration
import com.mv.data.{FiliaalRepository, RemarkRepository}
import com.mv.domain.FiliaalManager
import com.mv.domain.errors.{ClientError, NotFoundError}
import zio.ZLayer
import zio.http.*
import zio.http.Middleware.cors
object Api {
  val live: ZLayer[Configuration, Nothing, HttpApp[
    FiliaalManager & RemarkRepository
  ]] =
    ZLayer {
      for {
        corsConfig <- Cors.corsConfig
        filialen <- FiliaalRoutes.make
        remarks <- RemarkRoutes.make
      } yield (filialen ++ remarks).handleError {
        case e: NotFoundError =>
          Response.text(e.getMessage).status(Status.NotFound)
        case e: (InputDecodeError | ClientError) =>
          Response.text(e.getMessage).status(Status.BadRequest)
        case e: Throwable =>
          Response.internalServerError(e.getMessage)
      }.toHttpApp @@ cors(corsConfig) @@ Middleware.addHeader(
        "X-Content-Type-Options",
        "nosniff"
      )
    }
}
