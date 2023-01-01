ThisBuild / scalaVersion := "3.2.1"
ThisBuild / version := "0.1.0"
ThisBuild / organization := "com.mv"

lazy val root = (project in file("."))
  .settings(
    name := "Kca",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % "2.0.5",
      "dev.zio" %% "zio-test" % "2.0.5" % Test,
      "io.getquill" %% "quill-jdbc-zio" % "4.6.0",
      "org.postgresql" % "postgresql" % "42.5.1",
      "io.d11" %% "zhttp" % "2.0.0-RC11",
      "io.d11" %% "zhttp-test" % "2.0.0-RC7" % Test,
      "dev.zio" %% "zio-config" % "3.0.6",
      "dev.zio" % "zio-json_3" % "0.4.2"
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )
