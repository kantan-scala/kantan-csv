ThisBuild / kantanProject := "csv"
ThisBuild / startYear := Some(2015)

ThisBuild / scalafixDependencies += "com.github.xuwei-k" %% "scalafix-rules" % "0.6.12"

lazy val jsModules: Seq[ProjectReference] = Seq(
  cats.js,
  codecsCats.js,
  codecsCatsLaws.js,
  codecsCore.js,
  codecsEnumeratum.js,
  codecsEnumeratumLaws.js,
  codecsLaws.js,
  codecsRefined.js,
  codecsRefinedLaws.js,
  codecsScalaz.js,
  codecsScalazLaws.js,
  codecsShapeless.js,
  codecsShapelessLaws.js,
  core.js,
  enumeratum.js,
  generic.js,
  laws.js,
  refined.js,
  scalaz.js
)

enablePlugins(UnpublishedPlugin)

lazy val docs = project
  .enablePlugins(DocumentationPlugin)
  .settings(name := "docs")
  .settings(
    ScalaUnidoc / unidoc / unidocProjectFilter :=
      inAnyProject -- inProjects(benchmark) -- inProjects(jsModules: _*)
  )
  .settings(libraryDependencies += "joda-time" % "joda-time" % "2.14.0")
  .dependsOn(
    coreJVM,
    java8,
    lawsJVM,
    catsJVM,
    scalazJVM,
    genericJVM,
    jackson,
    commons,
    refinedJVM,
    enumeratumJVM
  )

lazy val benchmark = project
  .enablePlugins(UnpublishedPlugin, JmhPlugin)
  .dependsOn(coreJVM, jackson, commons, lawsJVM % Test)
  .settings(
    libraryDependencies ++= Seq(
      "com.opencsv" % "opencsv" % "5.11.2",
      "com.univocity" % "univocity-parsers" % "2.9.1",
      "com.github.tototoshi" %% "scala-csv" % "2.0.0"
    )
  )

// - core projects -----------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
lazy val core = kantanCrossProject("core", "core")
  .settings(moduleName := "kantan.csv")
  .enablePlugins(PublishedPlugin, BoilerplatePlugin)
  .laws("laws")
  .dependsOn(codecsCore)

lazy val coreJVM = core.jvm

lazy val laws = kantanCrossProject("laws", "laws")
  .settings(moduleName := "kantan.csv-laws")
  .enablePlugins(PublishedPlugin, BoilerplatePlugin)
  .dependsOn(core, codecsLaws)

lazy val lawsJVM = laws.jvm

// - external engines projects -----------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
lazy val jackson = project
  .settings(moduleName := "kantan.csv-jackson")
  .enablePlugins(PublishedPlugin)
  .dependsOn(coreJVM, lawsJVM % Test)
  .settings(
    libraryDependencies ++= Seq(
      "com.fasterxml.jackson.dataformat" % "jackson-dataformat-csv" % "2.19.1"
    )
  )

lazy val commons = project
  .settings(moduleName := "kantan.csv-commons")
  .enablePlugins(PublishedPlugin)
  .dependsOn(coreJVM, lawsJVM % Test)
  .settings(
    libraryDependencies ++= Seq(
      "org.apache.commons" % "commons-csv" % "1.14.0"
    )
  )

// - shapeless projects ------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
lazy val generic = kantanCrossProject("generic", "generic")
  .settings(moduleName := "kantan.csv-generic")
  .enablePlugins(PublishedPlugin)
  .dependsOn(core, laws % Test, codecsShapeless, codecsShapelessLaws % Test)

lazy val genericJVM = generic.jvm
lazy val genericJS = generic.js

// - scalaz projects ---------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
lazy val scalaz = kantanCrossProject("scalaz", "scalaz")
  .settings(moduleName := "kantan.csv-scalaz")
  .enablePlugins(PublishedPlugin)
  .dependsOn(core, laws % Test, codecsScalaz, codecsScalazLaws % Test)

lazy val scalazJVM = scalaz.jvm
lazy val scalazJS = scalaz.js

// - cats projects -----------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
lazy val cats = kantanCrossProject("cats", "cats")
  .settings(moduleName := "kantan.csv-cats")
  .enablePlugins(PublishedPlugin)
  .dependsOn(core, laws % Test, codecsCats, codecsCatsLaws % Test)

lazy val catsJVM = cats.jvm
lazy val catsJS = cats.js

// - java8 projects ----------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
lazy val java8 = project
  .settings(
    moduleName := "kantan.csv-java8",
    name := "java8"
  )
  .enablePlugins(PublishedPlugin)
  .dependsOn(coreJVM, lawsJVM % "test", codecsJava8, codecsJava8Laws % Test)

// - refined project ---------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
lazy val refined = kantanCrossProject("refined", "refined")
  .settings(moduleName := "kantan.csv-refined")
  .enablePlugins(PublishedPlugin)
  .dependsOn(core, laws % Test, codecsRefined, codecsRefinedLaws % Test)

lazy val refinedJVM = refined.jvm

// - enumeratum project ---------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
lazy val enumeratum = kantanCrossProject("enumeratum", "enumeratum")
  .settings(moduleName := "kantan.csv-enumeratum")
  .enablePlugins(PublishedPlugin)
  .dependsOn(core, laws % Test, codecsEnumeratum, codecsEnumeratumLaws % Test)

lazy val enumeratumJVM = enumeratum.jvm
lazy val enumeratumJS = enumeratum.js

// - Command alisases --------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
addCommandAlias("runBench", "benchmark/jmh:run -i 10 -wi 10 -f 2 -t 1 -rf csv -rff benchmarks.csv")
addCommandAlias(
  "runProfiler",
  "benchmark/jmh:run -i 10 -wi 5 -f 1 -t 1 -o profiler.txt -prof \"stack:detailLine=true;lines=5;period=1\" kantan.csv.benchmark.*kantan.*"
)

// - core projects -----------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
lazy val codecsCore = kantanCrossProject("codecs-core", "codecs/core")
  .settings(moduleName := "kantan.codecs")
  .settings(
    libraryDependencies ++= Seq(
      "org.scalatest" %%% "scalatest" % "3.2.19" % Test
    )
  )
  .jvmSettings(
    libraryDependencies += "commons-io" % "commons-io" % "2.19.0" % Test
  )
  .enablePlugins(PublishedPlugin)
  .laws("codecs-laws")

lazy val codecsCoreJVM = codecsCore.jvm

lazy val codecsLaws = kantanCrossProject("codecs-laws", "codecs/laws")
  .settings(moduleName := "kantan.codecs-laws")
  .enablePlugins(BoilerplatePlugin, PublishedPlugin)
  .dependsOn(codecsCore)
  .settings(
    libraryDependencies ++= Seq(
      "org.scalacheck" %%% "scalacheck" % "1.18.1",
      "org.scalatest" %%% "scalatest" % "3.2.19",
      "org.typelevel" %%% "discipline-scalatest" % "2.3.0"
    )
  )

lazy val codecsLawsJVM = codecsLaws.jvm

// - cats projects -----------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
lazy val codecsCats = kantanCrossProject("codecs-cats", "codecs/cats")
  .in(file("codecs/cats/core"))
  .settings(moduleName := "kantan.codecs-cats")
  .enablePlugins(PublishedPlugin)
  .dependsOn(codecsCore)
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-core" % "2.13.0",
      "org.scalatest" %%% "scalatest" % "3.2.19" % Test
    )
  )
  .laws("codecs-cats-laws")

lazy val codecsCatsLaws = kantanCrossProject("codecs-cats-laws", "codecs/cats-laws")
  .in(file("codecs/cats/laws"))
  .settings(moduleName := "kantan.codecs-cats-laws")
  .enablePlugins(PublishedPlugin)
  .dependsOn(codecsCore, codecsLaws, codecsCats)
  .settings(
    libraryDependencies += "org.typelevel" %%% "cats-laws" % "2.13.0"
  )

// - java8 projects ----------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
lazy val codecsJava8 = Project(id = "codecs-java8", base = file("codecs/java8/core"))
  .settings(
    moduleName := "kantan.codecs-java8",
    name := "java8"
  )
  .enablePlugins(PublishedPlugin)
  .dependsOn(codecsCoreJVM)
  .settings(
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-reflect" % scalaVersion.value % "provided",
      "org.scalatest" %% "scalatest" % "3.2.19" % "test"
    )
  )
  .laws("codecs-java8-laws")

lazy val codecsJava8Laws = Project(id = "codecs-java8-laws", base = file("codecs/java8/laws"))
  .settings(
    moduleName := "kantan.codecs-java8-laws",
    name := "java8-laws"
  )
  .enablePlugins(PublishedPlugin)
  .dependsOn(codecsCoreJVM, codecsLawsJVM, codecsJava8)

// - scalaz projects ---------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
lazy val codecsScalaz = kantanCrossProject("codecs-scalaz", "codecs/scalaz")
  .in(file("codecs/scalaz/core"))
  .settings(moduleName := "kantan.codecs-scalaz")
  .enablePlugins(PublishedPlugin)
  .dependsOn(codecsCore)
  .settings(
    libraryDependencies ++= Seq(
      "org.scalaz" %%% "scalaz-core" % "7.3.8",
      "org.scalatest" %%% "scalatest" % "3.2.19" % Test
    )
  )
  .laws("codecs-scalaz-laws")

lazy val codecsScalazLaws = kantanCrossProject("codecs-scalaz-laws", "codecs/scalaz-laws")
  .in(file("codecs/scalaz/laws"))
  .settings(moduleName := "kantan.codecs-scalaz-laws")
  .enablePlugins(PublishedPlugin)
  .dependsOn(codecsCore, codecsLaws, codecsScalaz)
  .settings(
    libraryDependencies ++= Seq(
      "org.scalaz" %%% "scalaz-core" % "7.3.8",
      "org.scalaz" %%% "scalaz-scalacheck-binding" % "7.3.8",
      "org.scalatest" %%% "scalatest" % "3.2.19" % "optional"
    )
  )

// - refined project ---------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
lazy val codecsRefined = kantanCrossProject("codecs-refined", "codecs/refined")
  .in(file("codecs/refined/core"))
  .settings(moduleName := "kantan.codecs-refined")
  .enablePlugins(PublishedPlugin)
  .dependsOn(codecsCore)
  .settings(
    libraryDependencies ++= Seq(
      "eu.timepit" %%% "refined" % "0.11.3",
      "org.scalatest" %%% "scalatest" % "3.2.19" % Test
    )
  )
  .laws("codecs-refined-laws")

lazy val codecsRefinedLaws = kantanCrossProject("codecs-refined-laws", "codecs/refined-laws")
  .in(file("codecs/refined/laws"))
  .settings(moduleName := "kantan.codecs-refined-laws")
  .settings(libraryDependencies += "eu.timepit" %%% "refined-scalacheck" % "0.11.3")
  .enablePlugins(PublishedPlugin)
  .dependsOn(codecsCore, codecsLaws, codecsRefined)

// - enumeratum project ------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
lazy val codecsEnumeratum = kantanCrossProject("codecs-enumeratum", "codecs/enumeratum")
  .in(file("codecs/enumeratum/core"))
  .settings(moduleName := "kantan.codecs-enumeratum")
  .enablePlugins(PublishedPlugin)
  .dependsOn(codecsCore)
  .settings(
    Compile / compile / scalacOptions ++= {
      scalaBinaryVersion.value match {
        case "3" =>
          // https://github.com/lloydmeta/enumeratum/blob/c76f9487bc86b5fc7/README.md?plain=1#L28
          Seq("-Yretain-trees")
        case _ =>
          Nil
      }
    },
    libraryDependencies ++= Seq(
      "com.beachape" %%% "enumeratum" % "1.9.0",
      "org.scalatest" %%% "scalatest" % "3.2.19" % Test
    )
  )
  .laws("codecs-enumeratum-laws")

lazy val codecsEnumeratumLaws = kantanCrossProject("codecs-enumeratum-laws", "codecs/enumeratum-laws")
  .in(file("codecs/enumeratum/laws"))
  .settings(moduleName := "kantan.codecs-enumeratum-laws")
  .settings(libraryDependencies += "com.beachape" %%% "enumeratum-scalacheck" % "1.9.0")
  .enablePlugins(PublishedPlugin)
  .dependsOn(codecsCore, codecsLaws, codecsEnumeratum)

// - shapeless projects ------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
lazy val codecsShapeless = kantanCrossProject("codecs-shapeless", "codecs/shapeless")
  .in(file("codecs/shapeless/core"))
  .settings(moduleName := "kantan.codecs-shapeless")
  .enablePlugins(PublishedPlugin)
  .dependsOn(codecsCore)
  .settings(
    libraryDependencies ++= Seq(
      "com.chuusai" %%% "shapeless" % "2.3.13",
      "org.scalatest" %%% "scalatest" % "3.2.19" % Test
    )
  )
  .laws("codecs-shapeless-laws")

lazy val codecsShapelessLaws = kantanCrossProject("codecs-shapeless-laws", "codecs/shapeless-laws")
  .in(file("codecs/shapeless/laws"))
  .settings(moduleName := "kantan.codecs-shapeless-laws")
  .settings(
    libraryDependencies +=
      "com.github.alexarchambault" %%% "scalacheck-shapeless_1.18" % "1.3.2"
  )
  .enablePlugins(PublishedPlugin)
  .dependsOn(codecsCore, codecsLaws, codecsShapeless)
