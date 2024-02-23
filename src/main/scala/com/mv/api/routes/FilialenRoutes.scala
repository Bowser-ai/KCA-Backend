package com.mv.api.routes

import com.mv.configuration.Configuration
import com.mv.data.FilialenRepository
import com.mv.models.*
import com.mv.models.Filiaal.given
import zio.ZIO
import zio.http.*
import zio.json.*

import scala.collection.SortedMap

private[api] object FilialenRoutes {
  def make: ZIO[Configuration, Nothing, Routes[FilialenRepository, Throwable]] =
    ZIO.serviceWith[Configuration] { conf =>
      Routes(
        Method.GET / conf.baseApiUrl / "filialen" -> handler(
          FilialenRepository.getFilialen
            .map(filialen =>
              Response.json(
                Map(
                  "filialen" -> SortedMap(
                    filialen
                      .map(f => f.filiaalNumber -> f)*
                  )
                ).toJson
              )
            )
        ),
        Method.GET / conf.baseApiUrl / "filialen" / int("id") -> handler {
          (id: Int, req: Request) =>
            FilialenRepository
              .getFiliaalByNumber(id)
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
        Method.PATCH / conf.baseApiUrl / "filialen" / int("id") -> handler(
          (id: Int, req: Request) =>
            req.body.asString.flatMap(body =>
              body.fromJson[PartialFiliaal] match
                case Left(error) =>
                  ZIO.succeed(Response.text(error).status(Status.BadRequest))
                case Right(partialFiliaal) =>
                  FilialenRepository
                    .getFiliaalByNumber(id)
                    .flatMap(
                      _.fold(ZIO.succeed(Response.status(Status.NotFound)))(
                        _ => {
                          val updatedFiliaal = partialFiliaal.asFiliaal(id)
                          FilialenRepository
                            .updateFiliaal(updatedFiliaal)
                            .as(Response.json(updatedFiliaal.toJson))
                        }
                      )
                    )
            )
        )
      )
    }
}
