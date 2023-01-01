package com.mv

import com.mv.configuration.Configuration
import com.mv.api.FilialenApi
import com.mv.data.db.FilialenRepository
import io.getquill.jdbczio.Quill.DataSource
import zhttp.service.*
import zio.*
import zio.Console.printLine

object Main extends ZIOAppDefault {
  override def run: Task[Unit] = {
    val ds = DataSource.fromPrefix("databaseConfig")
    (for {
      conf <- ZIO.service[Configuration]
      _ <- printLine(s"Server starting on port: ${conf.port}")
      _ <- Server
        .start(conf.port, FilialenApi.filialen)
    } yield ())
      .provide(Configuration.live, ds, FilialenRepository.live)
  }
}
