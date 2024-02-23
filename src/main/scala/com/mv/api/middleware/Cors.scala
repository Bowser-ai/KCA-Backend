package com.mv.api.middleware

import com.mv.configuration.Configuration
import zio.ZIO
import zio.http.Header.{
  AccessControlAllowMethods,
  AccessControlAllowOrigin,
  Origin
}
import zio.http.Method
import zio.http.Middleware.CorsConfig

private[api] object Cors {
  def corsConfig: ZIO[Configuration, Nothing, CorsConfig] =
    ZIO.serviceWith(conf =>
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
    )
}
