package com.mv.api

import com.mv.api.middleware.Cors
import com.mv.api.routes.{FilialenRoutes, RemarkRoutes}
import com.mv.configuration.Configuration
import com.mv.data.FilialenRepository
import zio.ZLayer
import zio.http.Middleware.cors
import zio.http.{HttpApp, Response, Status}
import com.mv.api.errors.InputDecodeError
object Api {
  val live: ZLayer[Configuration, Nothing, HttpApp[FilialenRepository]] =
    ZLayer {
      for {
        corsConfig <- Cors.corsConfig
        filialen <- FilialenRoutes.make
        remarks <- RemarkRoutes.make
      } yield (filialen ++ remarks).handleError {
        case InputDecodeError(msg) =>
          Response.text(msg).status(Status.BadRequest)
        case e: Throwable =>
          Response.internalServerError(e.getMessage)
      }.toHttpApp @@ cors(corsConfig)
    }
}
