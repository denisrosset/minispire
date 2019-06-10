import ReleaseTransformations._

import sbtcrossproject.{CrossType, crossProject}

lazy val scalaVersions: Map[String, String] =
  Map("2.11" -> "2.11.12", "2.12" -> "2.12.8", "2.13" -> "2.13.0")

// Projects

lazy val spire = project.in(file("."))
  .settings(moduleName := "minispire-root")
  .settings(spireSettings)
  .settings(noPublishSettings)
  .aggregate(spireJVM, spireJS)
  .dependsOn(spireJVM, spireJS)

lazy val spireJVM = project.in(file(".spireJVM"))
  .settings(moduleName := "minispire-aggregate")
  .settings(spireSettings)
  .settings(noPublishSettings)
  .aggregate(macrosJVM, coreJVM)
  .dependsOn(macrosJVM, coreJVM)

lazy val spireJS = project.in(file(".spireJS"))
  .settings(moduleName := "minispire-aggregate")
  .settings(spireSettings)
  .settings(noPublishSettings)
  .aggregate(macrosJS, coreJS)
  .dependsOn(macrosJS, coreJS)
  .enablePlugins(ScalaJSPlugin)

lazy val macros = crossProject(JSPlatform, JVMPlatform).crossType(CrossType.Pure)
  .settings(moduleName := "minispire-macros")
  .settings(spireSettings:_*)
  .settings(crossVersionSharedSources:_*)
  .jvmSettings(commonJvmSettings:_*)
  .jsSettings(commonJsSettings:_*)

lazy val macrosJVM = macros.jvm
lazy val macrosJS = macros.js

lazy val core = crossProject(JSPlatform, JVMPlatform).crossType(CrossType.Pure)
  .settings(moduleName := "minispire")
  .settings(spireSettings:_*)
  .settings(coreSettings:_*)
  .settings(crossVersionSharedSources:_*)
  .jvmSettings(commonJvmSettings:_*)
  .jsSettings(commonJsSettings:_*)
  .dependsOn(macros)

lazy val coreJVM = core.jvm
lazy val coreJS = core.js

// General settings

lazy val buildSettings = Seq(
  organization := "org.typelevel",
  scalaVersion := scalaVersions("2.12"),
  crossScalaVersions := Seq(scalaVersions("2.11"), scalaVersions("2.12"), scalaVersions("2.13")),
)

lazy val commonDeps = Seq()

lazy val commonSettings = Seq(
  scalacOptions ++= commonScalacOptions.value.diff(Seq(
    "-Xfatal-warnings",
    "-language:existentials",
    "-Ywarn-dead-code",
    "-Ywarn-numeric-widen",
    "-Ywarn-value-discard"
  )),
  resolvers += Resolver.sonatypeRepo("snapshots")
) ++ scalaMacroDependencies

lazy val commonJsSettings = Seq(
  scalaJSStage in Global := FastOptStage,
  parallelExecution in Test := false
)

lazy val commonJvmSettings = Seq(
  // -optimize has no effect in scala-js other than slowing down the build
  //  scalacOptions += "-optimize", // disabling for now
  scalacOptions ++= (CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, scalaMajor)) if scalaMajor <= 11 => Seq("-optimize")
    case _ => Seq.empty
  })
)

lazy val publishSettings = Seq(
  homepage := Some(url("https://github.com/denisrosset/minispire")),
  licenses += ("MIT", url("https://opensource.org/licenses/MIT")),
  bintrayRepository := "maven",
  publishArtifact in Test := false,
  bintrayReleaseOnPublish in ThisBuild := false,
  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runClean,
    runTest,
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    publishArtifacts,
    releaseStepCommand("bintrayRelease"),
    setNextVersion,
    commitNextVersion,
    pushChanges
  )
)

lazy val coreSettings = Seq(
)

lazy val spireSettings = buildSettings ++ commonSettings ++ commonDeps ++ publishSettings

////////////////////////////////////////////////////////////////////////////////////////////////////
// Base Build Settings - Should not need to edit below this line.
// These settings could also come from another file or a plugin.
// The only issue if coming from a plugin is that the Macro lib versions
// are hard coded, so an overided facility would be required.

lazy val noPublishSettings = Seq(
  publish := (()),
  publishLocal := (()),
  publishArtifact := false
)

lazy val crossVersionSharedSources: Seq[Setting[_]] =
  Seq(Compile, Test).map { sc =>
    (unmanagedSourceDirectories in sc) ++= {
      (unmanagedSourceDirectories in sc ).value.map {
        dir:File =>
          CrossVersion.partialVersion(scalaBinaryVersion.value) match {
            case Some((major, minor)) =>
              new File(s"${dir.getPath}_$major.$minor")
            case None =>
              sys.error("couldn't parse scalaBinaryVersion ${scalaBinaryVersion.value}")
          }
      }
    }
  }

lazy val scalaMacroDependencies: Seq[Setting[_]] = Seq(
  libraryDependencies += scalaOrganization.value % "scala-reflect" % scalaVersion.value % "provided"
)

lazy val commonScalacOptions = Def.setting(
  (CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, v)) if v >= 13 =>
      Seq()
    case _ =>
      Seq("-Yno-adapted-args")
  }) ++ Seq(
    "-deprecation",
    "-encoding", "UTF-8",
    "-feature",
    "-language:existentials",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-language:experimental.macros",
    "-unchecked",
    "-Xfuture"
  )
)
