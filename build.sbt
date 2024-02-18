ThisBuild / scalaVersion := "3.4.0"
ThisBuild / version := "0.1.0"
ThisBuild / organization := "com.mv"

lazy val root = (project in file("."))
  .settings(
    name := "Kca",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % "2.0.13",
      "dev.zio" %% "zio-test" % "2.0.13" % Test,
      "io.getquill" %% "quill-jdbc-zio" % "4.6.0.1",
      "org.postgresql" % "postgresql" % "42.5.4",
      "dev.zio" %% "zio-http" % "3.0.0-RC4",
      "dev.zio" %% "zio-config" % "3.0.7",
      "dev.zio" % "zio-json_3" % "0.5.0"
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )
