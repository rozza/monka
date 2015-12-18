/*
  * Copyright 2015 MongoDB, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */

import com.typesafe.sbt.SbtScalariform._
import org.scalastyle.sbt.ScalastylePlugin._
import sbt.Keys._
import sbt._
import scoverage.ScoverageSbtPlugin._

import scalariform.formatter.preferences.FormattingPreferences

object MonkaBuild extends Build {

  import Dependencies._
  import Resolvers._

  val buildSettings = Seq(
    organization := "org.mongodb.monka",
    organizationHomepage := Some(url("http://www.mongodb.org")),
    version := "0.1-SNAPSHOT",
    scalaVersion := scalaCoreVersion,
    libraryDependencies ++= coreDependencies,
    resolvers := monkaResolvers,
    scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-Xlint", "-Xlint:-missing-interpolator"
                          /*, "-Xlog-implicits", "-Yinfer-debug", "-Xprint:typer"*/)
  )

  /*
   * Test Settings
   */
  val testSettings = Seq(
    testFrameworks += TestFrameworks.ScalaTest,
    ScoverageKeys.coverageMinimum := 5, // TODO
    ScoverageKeys.coverageFailOnMinimum := true,
    libraryDependencies ++= testDependencies
  )

  lazy val UnitTest = config("unit") extend Test

  val scoverageSettings = Seq()

  /*
   * Style and formatting
   */
  def scalariFormFormattingPreferences: FormattingPreferences = {
    import scalariform.formatter.preferences._
    FormattingPreferences()
      .setPreference(AlignParameters, true)
      .setPreference(AlignSingleLineCaseStatements, true)
      .setPreference(DoubleIndentClassDeclaration, true)
  }

  val customScalariformSettings = scalariformSettings ++ Seq(
    ScalariformKeys.preferences in Compile := scalariFormFormattingPreferences,
    ScalariformKeys.preferences in Test := scalariFormFormattingPreferences
  )

  val scalaStyleSettings = Seq(
    (scalastyleConfig in Compile) := file("project/scalastyle-config.xml"),
    (scalastyleConfig in Test) := file("project/scalastyle-config.xml")
  )

  lazy val noPublishing = Seq(
      publish :=(),
      publishLocal :=()
    )

  // Check style
  val checkAlias = addCommandAlias("check", ";clean;scalastyle;coverage;test;it:test;coverageAggregate;coverageReport")

  lazy val monka = Project(
    id = "monka",
    base = file("monka")
  ).configs(UnitTest)
    .settings(buildSettings)
    .settings(testSettings)
    .settings(customScalariformSettings)
    .settings(scalaStyleSettings)
    .settings(scoverageSettings)

  lazy val root = Project(
    id = "mongo-monka",
    base = file(".")
  ).aggregate(monka)
    .settings(buildSettings)
    .settings(scalaStyleSettings)
    .settings(scoverageSettings)
    .settings(noPublishing: _*)
    .settings(checkAlias)
    .dependsOn(monka)

  override def rootProject: Some[Project] = Some(root)

}
