package com.mv.api.routes

import com.mv.api.errors.InputDecodeError
import com.mv.configuration.Configuration
import com.mv.domain.FiliaalManager
import com.mv.models.*
import com.mv.models.Filiaal.given
import com.mv.api.routes.given
import zio.ZIO
import zio.http.*
import zio.json.*
import com.mv.domain.errors.NotFoundError

import scala.collection.SortedMap

@jsonNoExtraFields
case class FilialenWithPageToken(
    filialen: SortedMap[Int, Filiaal],
    pageToken: Option[String]
)

given filialenWithPageTokenEncoder: JsonEncoder[FilialenWithPageToken] =
  DeriveJsonEncoder.gen[FilialenWithPageToken]

private[api] object FiliaalRoutes {
  def make: ZIO[Configuration, Nothing, Routes[FiliaalManager, Throwable]] =
    ZIO.serviceWith[Configuration] { conf =>
      Routes(
        Method.GET / conf.baseApiUrl / "filialen" -> handler((req: Request) =>
          req.url.queryParams
            .get("page_token")
            .fold(
              FiliaalManager.list(
                req.url.queryParams.get("page_size").flatMap(_.toIntOption)
              )
            )(FiliaalManager.list)
            .map { case (filialen, nextPageToken) =>
              Response.json(
                FilialenWithPageToken(
                  SortedMap(
                    filialen
                      .map(f => f.filiaalNumber -> f)*
                  ),
                  nextPageToken
                ).toJson
              )
            }
        ),
        Method.GET / conf.baseApiUrl / "filialen" / int("id") -> handler(
          (id: Int, req: Request) =>
            FiliaalManager
              .get(id)
              .map(
                _.fold(Response.notFound)(filiaal =>
                  Response.json(filiaal.toJson)
                )
              )
        ),
        Method.PUT / conf.baseApiUrl / "filialen" -> handler(
          (request: Request) =>
            for {
              body <- request.body.asString
              filialen <- ZIO
                .fromEither(body.fromJson[Array[Filiaal]])
                .mapError(InputDecodeError.apply)
              createdFilialen <- FiliaalManager.create(filialen.toList)
            } yield Response.json(createdFilialen.toJson)
        ),
        Method.PATCH / conf.baseApiUrl / "filialen" / int("id") -> handler(
          (id: Int, req: Request) =>
            req.body.asString.flatMap(body =>
              ZIO
                .fromEither(body.fromJson[PartialFiliaal])
                .mapError(InputDecodeError.apply)
                .flatMap(partialFiliaal =>
                  FiliaalManager
                    .update(id, partialFiliaal)
                    .map(updatedFiliaal => Response.json(updatedFiliaal.toJson))
                )
            )
        ),
        Method.DELETE / conf.baseApiUrl / "filialen" / int("id") -> handler(
          (id: Int, req: Request) =>
            FiliaalManager
              .delete(id)
              .map(deletedFiliaal => Response.json(deletedFiliaal.toJson))
              .catchSome { case NotFoundError(msg) =>
                ZIO.succeed(Response.notFound(msg))
              }
        )
      )
    }
}
