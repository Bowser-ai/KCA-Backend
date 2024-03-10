package com.mv

import com.mv.api.Api
import com.mv.configuration.Configuration
import com.mv.data.{FiliaalRepository, RemarkRepository}
import com.mv.domain.FiliaalManager
import io.getquill.jdbczio.Quill.DataSource
import zio.*
import zio.Console.printLine
import zio.http.{HttpApp, Server}
object Main extends ZIOAppDefault {
  override def run: Task[Unit] =
    val runningServer = for {
      conf <- ZIO.service[Configuration]
      _ <- printLine(s"Server starting on port: ${conf.port}")
      app <- ZIO.service[HttpApp[FiliaalManager & RemarkRepository]]
      _ <- Server
        .serve(
          app
        )
    } yield ()

    runningServer.provide(
      Configuration.live,
      Api.live,
      FiliaalManager.live,
      FiliaalRepository.live,
      RemarkRepository.live,
      DataSource.fromPrefix("databaseConfig"),
      ZLayer
        .service[Configuration]
        .project(c => Server.defaultWithPort(c.port))
        .flatten
    )
}
