import ReleaseTransformations._
import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

lazy val scalaVersions: Map[String, String] =
  Map("2.11" -> "2.11.12", "2.12" -> "2.12.8", "2.13" -> "2.13.0-RC3")

lazy val scala211 = scalaVersions("2.11")

// Projects

lazy val spire = project.in(file("."))
    .settings(moduleName := "minispire-root")
    .settings(spireSettings)
    .settings(noPublishSettings)
    .aggregate(macrosJVM, coreJVM, macrosJS, coreJS)

lazy val macros =
  crossProject(JVMPlatform, JSPlatform, NativePlatform)
    .crossType(CrossType.Pure)
    .in(file("macros"))
    .settings(moduleName := "minispire-macros")
    .settings(spireSettings:_*)
    .jvmSettings(commonJvmSettings:_*)
    .jsSettings(commonJsSettings:_*)
    .nativeSettings(commonNativeSettings:_*)

lazy val macrosJVM = macros.jvm
lazy val macrosJS = macros.js
lazy val macrosNative = macros.native

lazy val core =
  crossProject(JVMPlatform, JSPlatform, NativePlatform)
    .crossType(CrossType.Pure)
    .in(file("core"))
    .settings(moduleName := "minispire")
    .settings(spireSettings:_*)
    .jvmSettings(commonJvmSettings:_*)
    .jsSettings(commonJsSettings:_*)
    .nativeSettings(commonNativeSettings:_*)
    .dependsOn(macros)

lazy val coreJVM = core.jvm
lazy val coreJS = core.js
lazy val coreNative = core.native

// General settings

lazy val buildSettings = Seq(
  organization := "org.typelevel"
)

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
  parallelExecution in Test := false,
  scalaVersion := scalaVersions("2.13"),
  crossScalaVersions := Seq(scalaVersions("2.11"), scalaVersions("2.12"), scalaVersions("2.13"))
)

lazy val commonJvmSettings = Seq(
  // -optimize has no effect in scala-js other than slowing down the build
  //  scalacOptions += "-optimize", // disabling for now
  scalacOptions ++= (CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, scalaMajor)) if scalaMajor <= 11 => Seq("-optimize")
    case _ => Seq.empty
  }),
  scalaVersion := scalaVersions("2.13"),
  crossScalaVersions := Seq(scalaVersions("2.11"), scalaVersions("2.12"), scalaVersions("2.13"))
)

lazy val commonNativeSettings = Seq(
  scalaVersion := scalaVersions("2.11"),
  crossScalaVersions := Seq(scalaVersions("2.11"))
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
    releaseStepCommand(s"++${scala211}"),
    releaseStepCommand("coreNative/publish"),
    setNextVersion,
    commitNextVersion,
    releaseStepCommand("bintrayRelease"),
    pushChanges
  )
)

lazy val spireSettings = buildSettings ++ commonSettings ++ publishSettings

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
