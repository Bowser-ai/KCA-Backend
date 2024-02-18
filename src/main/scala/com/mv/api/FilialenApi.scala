package com.mv.api

import com.mv.configuration.Configuration
import com.mv.data.db.FilialenRepository
import com.mv.models.*
import com.mv.models.Filiaal.*
import com.mv.models.Remark.*
import zio.http.*
import zio.http.Header.*
import zio.http.Middleware.{CorsConfig, cors}
import zio.json.*
import zio.{ZIO, ZLayer}

private object Cors {
  def corsConfig(conf: Configuration): CorsConfig =
    CorsConfig(
      allowedOrigin = {
        case origin @ Origin.Value(scheme, host, Some(port))
            if s"$scheme://$host:$port" == conf.origin =>
          Some(AccessControlAllowOrigin.Specific(origin))
        case _ => None
      },
      allowedMethods = AccessControlAllowMethods(
        Method.PUT,
        Method.PATCH,
        Method.DELETE,
        Method.GET
      )
    )
}

object FilialenApi {
  val live: ZLayer[Configuration, Nothing, HttpApp[FilialenRepository]] =
    ZLayer.fromFunction((conf: Configuration) =>
      Routes(
        Method.GET / conf.baseApiUrl / "filialen" -> handler(
          FilialenRepository.getFilialen
            .map(filialen =>
              Response.json(
                Map(
                  "filialen" -> filialen.map(f => f.filiaalNumber -> f).toMap
                ).toJson
              )
            )
        ),
        Method.GET / conf.baseApiUrl / "filialen" / int("number") -> handler {
          (number: Int, req: Request) =>
            FilialenRepository
              .getFiliaalByNumber(number)
              .map {
                case None          => Response.status(Status.NotFound)
                case Some(filiaal) => Response.json(filiaal.toJson)
              }
        },
        Method.PUT / conf.baseApiUrl / "filialen" -> handler(
          (request: Request) =>
            request.body.asString
              .flatMap(body => {
                body.fromJson[Array[Filiaal]] match
                  case Left(error) =>
                    ZIO.succeed(
                      Response.text(error).status(Status.BadRequest)
                    )
                  case Right(filialen) =>
                    FilialenRepository
                      .createFilialen(filialen.toList)
                      .as(Response.json(filialen.toJson))
              })
        ),
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
      ).handleError(e => Response.internalServerError(s"Internal error: $e"))
        .toHttpApp @@ cors(Cors.corsConfig(conf))
    )

  private def sanitize(input: String): String =
    input.trim.replaceAll("[<>\"']", "")
}
