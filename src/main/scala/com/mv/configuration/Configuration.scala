package com.mv.configuration
import zio.Config.{int, string}
import zio.{Config, ConfigProvider, ZLayer}
import Config.*
import zio.config.*
import zio.config.typesafe.*

case class Configuration(port: Int, origin: String, baseApiUrl: String)

object Configuration {
  private val config: Config[Configuration] =
    int("port")
      .zip(string("origin").map(_.replaceAll("\"", "")))
      .zip(string("baseApiUrl"))
      .to[Configuration]
  val live: ZLayer[Any, Error, Configuration] = ZLayer.fromZIO(
    ConfigProvider
      .fromResourcePath()
      .nested("server")
      .load(config)
  )
}
