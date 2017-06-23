/*
 * Copyright 2017 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import sbt._
import play.sbt.PlayImport._
import play.core.PlayVersion
import uk.gov.hmrc.SbtAutoBuildPlugin
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin
import uk.gov.hmrc.versioning.SbtGitVersioning

object MicroServiceBuild extends Build with MicroService {

  val appName = "api-subscription-fields"

  override lazy val appDependencies: Seq[ModuleID] = compile ++ test()

  val compile = Seq(
    "uk.gov.hmrc" %% "play-reactivemongo" % "5.2.0",
    ws,
    "uk.gov.hmrc" %% "microservice-bootstrap" % "5.16.0",
    "uk.gov.hmrc" %% "play-authorisation" % "4.3.0",
    "uk.gov.hmrc" %% "play-health" % "2.1.0",
    "uk.gov.hmrc" %% "play-url-binders" % "2.1.0",
    "uk.gov.hmrc" %% "play-config" % "4.3.0",
    "uk.gov.hmrc" %% "logback-json-logger" % "3.1.0",
    "uk.gov.hmrc" %% "domain" % "4.1.0"
  )

  def test(scope: String = "test,it") = Seq(
    "uk.gov.hmrc" %% "hmrctest" % "2.3.0" % scope,
    "org.scalatest" %% "scalatest" % "2.2.6" % scope,
    "org.pegdown" % "pegdown" % "1.6.0" % scope,
    "com.typesafe.play" %% "play-test" % PlayVersion.current % scope
  )

}
