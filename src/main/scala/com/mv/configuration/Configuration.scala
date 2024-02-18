package com.mv.configuration
import zio.config.*
import ConfigDescriptor.*
import ConfigSource.*
import zio.Layer

case class Configuration(port: Int, origin: String, baseApiUrl: String)

object Configuration {
  private val config: ConfigDescriptor[Configuration] =
    int("port")
      .zip(string("origin").map(_.replaceAll("\"", "")))
      .zip(string("baseApiUrl"))
      .to[Configuration]
  val live: Layer[ReadError[String], Configuration] =
    ZConfig.fromPropertiesFile("src/main/resources/application.conf", config)
}
