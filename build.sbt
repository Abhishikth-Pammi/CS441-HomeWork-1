ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.1"

mainClass in Compile := Some("AbhiJob")


lazy val root = (project in file("."))
  .settings(
    name := "MyCode"
  )

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core" % "0.14.1",
  "io.circe" %% "circe-generic" % "0.14.1",
  "io.circe" %% "circe-parser" % "0.14.1",
  "org.apache.hadoop" % "hadoop-common" % "3.2.4",
  "org.apache.hadoop" % "hadoop-hdfs" % "3.2.4",
  "org.apache.hadoop" % "hadoop-mapreduce-client-core" % "3.2.4"
)

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", _*) => MergeStrategy.discard
  case _                        => MergeStrategy.first
}

