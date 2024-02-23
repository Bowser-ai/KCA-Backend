package com.mv

import com.mv.api.Api
import com.mv.configuration.Configuration
import com.mv.data.FilialenRepository
import io.getquill.jdbczio.Quill.DataSource
import zio.http.{HttpApp, Server}
import zio.*
import zio.Console.printLine
object Main extends ZIOAppDefault {
  override def run: Task[Unit] =
    val runningServer = for {
      conf <- ZIO.service[Configuration]
      _ <- printLine(s"Server starting on port: ${conf.port}")
      app <- ZIO.service[HttpApp[FilialenRepository]]
      _ <- Server
        .serve(
          app
        )
    } yield ()

    runningServer.provide(
      Configuration.live,
      Api.live,
      FilialenRepository.live,
      DataSource.fromPrefix("databaseConfig"),
      ZLayer
        .service[Configuration]
        .project(c => Server.defaultWithPort(c.port))
        .flatten
    )
}
