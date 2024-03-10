ThisBuild / scalaVersion := "3.4.0"
ThisBuild / version := "0.1.0"
ThisBuild / organization := "com.mv"

Compile / PB.targets := Seq(
  scalapb.gen() -> (Compile / sourceManaged).value / "scalapb"
)

lazy val root = (project in file("."))
  .settings(
    name := "Kca",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % "2.0.21",
      "dev.zio" %% "zio-test" % "2.0.21" % Test,
      "dev.zio" %% "zio-mock" % "1.0.0-RC12",
      "io.getquill" %% "quill-jdbc-zio" % "4.8.1",
      "org.postgresql" % "postgresql" % "42.7.1",
      "dev.zio" %% "zio-http" % "3.0.0-RC4",
      "dev.zio" %% "zio-config" % "4.0.1",
      "dev.zio" %% "zio-config-typesafe" % "4.0.1",
      "dev.zio" % "zio-json_3" % "0.6.2"
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )
