lazy val main = (project in file("."))
  .settings(
    name := "akka-http-webapi",
    version := "0.0.1-SNAPSHOT",
    scalaVersion in ThisBuild := "2.11.8",
    scalacOptions ++= Seq("-deprecation", "-feature"),
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-core" % "1.1.7",
      "ch.qos.logback" % "logback-classic" % "1.1.7",
      "com.typesafe.akka" %% "akka-slf4j" % "2.4.11",
      "com.typesafe.akka" %% "akka-stream" % "2.4.11",
      "com.typesafe.akka" %% "akka-http-experimental" % "2.4.11",
      "com.typesafe.akka" %% "akka-http-spray-json-experimental" % "2.4.11",
      "com.typesafe.slick" %% "slick" % "3.1.1",
      "com.typesafe.slick" %% "slick-hikaricp" % "3.1.1",
      "com.h2database" % "h2" % "1.4.192",
      "com.jsuereth" %% "scala-arm" % "1.4",
      "com.typesafe.slick" %% "slick-testkit" % "3.1.1" % "test",
      "com.typesafe.akka" %% "akka-http-testkit" % "2.4.11"  % "test",
      "org.specs2" %% "specs2-core" % "3.8.5" % "test",
      "org.scalatest" %% "scalatest" % "3.0.0" % "test"
    )
  )
