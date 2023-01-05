package com.mv.api

import com.mv.configuration.Configuration
import com.mv.data.db.FilialenRepository
import com.mv.models.*
import com.mv.models.Filiaal.*
import com.mv.models.Remark.*
import zhttp.http.*
import zhttp.http.middleware.Cors.CorsConfig
import zio.ZIO
import zio.json.*

import java.sql.SQLException

private object Cors {
  val corsMiddleWare: Middleware[
    Configuration,
    Nothing,
    Request,
    Response,
    Request,
    Response
  ] =
    Middleware.collectZIO[Request](_ =>
      ZIO
        .service[Configuration]
        .map(config =>
          Middleware.cors(
            CorsConfig(
              anyOrigin = false,
              anyMethod = true,
              allowedOrigins = _ == config.origin,
              allowCredentials = true,
              allowedHeaders = Some(
                Set("content-type")
              )
            )
          )
        )
    )
}

object FilialenApi {
  val filialen
      : Http[Configuration & FilialenRepository, Throwable, Request, Response] =
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
                      sanitize(remark.body)
                    )
                    .as(Response.ok)
          })
      case req @ Method.PUT -> !! / "mededelingen" =>
        req.body.asString.flatMap(body =>
          body.fromJson[RemarkWithId] match
            case Left(error) =>
              ZIO.succeed(Response.text(error).setStatus(Status.BadRequest))
            case Right(filiaal) =>
              FilialenRepository
                .updateRemark(filiaal.id, sanitize(filiaal.body))
                .as(Response.ok)
        )
    } @@ Cors.corsMiddleWare

  private def sanitize(input: String): String =
    input.trim.replaceAll("[<>\"']", "")
}
