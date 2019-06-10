scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject"      % "0.6.0")
addSbtPlugin("org.portable-scala" % "sbt-scala-native-crossproject" % "0.6.0")
addSbtPlugin("org.scala-js"       % "sbt-scalajs"                   % "1.0.0-M8")
addSbtPlugin("org.scala-native"   % "sbt-scala-native"              % "0.4.0-M2")

addSbtPlugin("org.foundweekends" % "sbt-bintray" % "0.5.4")
addSbtPlugin("com.github.gseitz"   % "sbt-release"           % "1.0.11")
