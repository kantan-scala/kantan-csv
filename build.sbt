ThisBuild / kantanProject := "csv"
ThisBuild / startYear := Some(2015)

ThisBuild / scalafixDependencies += "com.github.xuwei-k" %% "scalafix-rules" % "0.6.12"

scalaVersion := Scala213
enablePlugins(UnpublishedPlugin)

lazy val docs = projectMatrix
  .jvmPlatform(
    scalaVersions = Seq(Scala213)
  )
  .enablePlugins(DocumentationPlugin)
  .settings(name := "docs")
  .settings(libraryDependencies += "joda-time" % "joda-time" % "2.14.0")
  .dependsOn(
    core,
    java8,
    laws,
    cats,
    scalaz,
    generic,
    jackson,
    commons,
    refined,
    enumeratum
  )

lazy val benchmark = projectMatrix
  .jvmPlatform(
    scalaVersions = Seq(Scala213, Scala3)
  )
  .enablePlugins(UnpublishedPlugin, JmhPlugin)
  .dependsOn(core, jackson, commons, laws % Test)
  .settings(
    libraryDependencies ++= Seq(
      "com.opencsv" % "opencsv" % "5.11.2",
      "com.univocity" % "univocity-parsers" % "2.9.1",
      "com.github.tototoshi" %% "scala-csv" % "2.0.0"
    )
  )

// - core projects -----------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
lazy val core = kantanCrossProject("core", "core", "laws", true)
  .settings(
    moduleName := "kantan.csv",
    libraryDependencies += "com.github.xuwei-k" %%% "unapply" % "0.1.0" % Test
  )
  .enablePlugins(PublishedPlugin, BoilerplatePlugin)
  .dependsOn(codecsCore)

lazy val laws = kantanCrossProject("laws", "laws")
  .settings(moduleName := "kantan.csv-laws")
  .enablePlugins(PublishedPlugin, BoilerplatePlugin)
  .dependsOn(core, codecsLaws)

// - external engines projects -----------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
lazy val jackson = projectMatrix
  .jvmPlatform(
    scalaVersions = Seq(Scala213, Scala3)
  )
  .settings(moduleName := "kantan.csv-jackson")
  .enablePlugins(PublishedPlugin)
  .dependsOn(core, laws % Test)
  .settings(
    libraryDependencies ++= Seq(
      "com.fasterxml.jackson.dataformat" % "jackson-dataformat-csv" % "2.19.1"
    )
  )

lazy val commons = projectMatrix
  .jvmPlatform(
    scalaVersions = Seq(Scala213, Scala3)
  )
  .settings(moduleName := "kantan.csv-commons")
  .enablePlugins(PublishedPlugin)
  .dependsOn(core, laws % Test)
  .settings(
    libraryDependencies ++= Seq(
      "org.apache.commons" % "commons-csv" % "1.14.0"
    )
  )

// - shapeless projects ------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
lazy val generic = kantanCrossProject("generic", "generic", false)
  .settings(moduleName := "kantan.csv-generic")
  .enablePlugins(PublishedPlugin)
  .dependsOn(core, laws % Test, codecsShapeless, codecsShapelessLaws % Test)

// - scalaz projects ---------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
lazy val scalaz = kantanCrossProject("scalaz", "scalaz")
  .settings(moduleName := "kantan.csv-scalaz")
  .enablePlugins(PublishedPlugin)
  .dependsOn(core, laws % Test, codecsScalaz, codecsScalazLaws % Test)

// - cats projects -----------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
lazy val cats = kantanCrossProject("cats", "cats")
  .settings(moduleName := "kantan.csv-cats")
  .enablePlugins(PublishedPlugin)
  .dependsOn(core, laws % Test, codecsCats, codecsCatsLaws % Test)

// - java8 projects ----------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
lazy val java8 = kantanCrossProject("java8", "java8", false)
  .settings(
    moduleName := "kantan.csv-java8"
  )
  .enablePlugins(PublishedPlugin)
  .dependsOn(core, laws % "test", codecsJava8, codecsJava8Laws % Test)

// - refined project ---------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
lazy val refined = kantanCrossProject("refined", "refined")
  .settings(moduleName := "kantan.csv-refined")
  .enablePlugins(PublishedPlugin)
  .dependsOn(core, laws % Test, codecsRefined, codecsRefinedLaws % Test)

// - enumeratum project ---------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
lazy val enumeratum = kantanCrossProject("enumeratum", "enumeratum")
  .settings(moduleName := "kantan.csv-enumeratum")
  .enablePlugins(PublishedPlugin)
  .dependsOn(core, laws % Test, codecsEnumeratum, codecsEnumeratumLaws % Test)

// - Command alisases --------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
addCommandAlias("runBench", "benchmark/jmh:run -i 10 -wi 10 -f 2 -t 1 -rf csv -rff benchmarks.csv")
addCommandAlias(
  "runProfiler",
  "benchmark/jmh:run -i 10 -wi 5 -f 1 -t 1 -o profiler.txt -prof \"stack:detailLine=true;lines=5;period=1\" kantan.csv.benchmark.*kantan.*"
)

// - core projects -----------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
lazy val codecsCore = kantanCrossProject("codecs-core", "codecs/core", "codecs-laws", true)
  .settings(moduleName := "kantan.codecs")
  .settings(
    libraryDependencies ++= {
      if(virtualAxes.value.toSet.contains(VirtualAxis.jvm))
        Seq("commons-io" % "commons-io" % "2.19.0" % Test)
      else
        Nil
    }
  )
  .enablePlugins(PublishedPlugin)

lazy val codecsLaws = kantanCrossProject("codecs-laws", "codecs/laws")
  .settings(moduleName := "kantan.codecs-laws")
  .enablePlugins(BoilerplatePlugin, PublishedPlugin)
  .dependsOn(codecsCore)
  .settings(
    libraryDependencies ++= Seq(
      "org.scalacheck" %%% "scalacheck" % "1.18.1",
      "org.scalatest" %%% "scalatest-shouldmatchers" % "3.2.19",
      "org.scalatest" %%% "scalatest-funspec" % "3.2.19",
      "org.scalatest" %%% "scalatest-flatspec" % "3.2.19",
      "org.scalatest" %%% "scalatest-funsuite" % "3.2.19",
      "org.scalatest" %%% "scalatest-wordspec" % "3.2.19",
      ("org.typelevel" %%% "discipline-scalatest" % "2.3.0").excludeAll(
        // https://github.com/typelevel/discipline-scalatest/pull/442
        ExclusionRule(organization = "org.scalatest")
      )
    )
  )

// - cats projects -----------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
lazy val codecsCats = kantanCrossProject("codecs-cats", "codecs/cats/core", "codecs-cats-laws", true)
  .settings(moduleName := "kantan.codecs-cats")
  .enablePlugins(PublishedPlugin)
  .dependsOn(codecsCore)
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-core" % "2.13.0"
    )
  )

lazy val codecsCatsLaws = kantanCrossProject("codecs-cats-laws", "codecs/cats/laws")
  .settings(moduleName := "kantan.codecs-cats-laws")
  .enablePlugins(PublishedPlugin)
  .dependsOn(codecsCore, codecsLaws, codecsCats)
  .settings(
    libraryDependencies += "org.typelevel" %%% "cats-laws" % "2.13.0"
  )

// - java8 projects ----------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
lazy val codecsJava8 = kantanCrossProject("codecs-java8", "codecs/java8/core", "codecs-java8-laws", false)
  .settings(
    moduleName := "kantan.codecs-java8",
    name := "java8"
  )
  .enablePlugins(PublishedPlugin)
  .dependsOn(codecsCore)
  .settings(
    libraryDependencies ++= {
      if(virtualAxes.value.toSet.contains(VirtualAxis.js)) {
        Seq("io.github.cquiroz" %%% "scala-java-time" % "2.6.0")
      } else {
        Nil
      }
    },
    libraryDependencies ++= {
      scalaBinaryVersion.value match {
        case "3" =>
          Nil
        case _ =>
          Seq("org.scala-lang" % "scala-reflect" % scalaVersion.value % "provided")
      }
    }
  )

lazy val codecsJava8Laws = kantanCrossProject("codecs-java8-laws", "codecs/java8/laws", false)
  .settings(
    moduleName := "kantan.codecs-java8-laws",
    name := "java8-laws"
  )
  .enablePlugins(PublishedPlugin)
  .dependsOn(codecsCore, codecsLaws, codecsJava8)

// - scalaz projects ---------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
lazy val codecsScalaz = kantanCrossProject("codecs-scalaz", "codecs/scalaz/core", "codecs-scalaz-laws", true)
  .settings(moduleName := "kantan.codecs-scalaz")
  .enablePlugins(PublishedPlugin)
  .dependsOn(codecsCore)
  .settings(
    libraryDependencies ++= Seq(
      "org.scalaz" %%% "scalaz-core" % "7.3.8"
    )
  )

lazy val codecsScalazLaws = kantanCrossProject("codecs-scalaz-laws", "codecs/scalaz/laws")
  .settings(moduleName := "kantan.codecs-scalaz-laws")
  .enablePlugins(PublishedPlugin)
  .dependsOn(codecsCore, codecsLaws, codecsScalaz)
  .settings(
    libraryDependencies ++= Seq(
      "org.scalaz" %%% "scalaz-scalacheck-binding" % "7.3.8"
    )
  )

// - refined project ---------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
lazy val codecsRefined = kantanCrossProject("codecs-refined", "codecs/refined/core", "codecs-refined-laws", true)
  .settings(moduleName := "kantan.codecs-refined")
  .enablePlugins(PublishedPlugin)
  .dependsOn(codecsCore)
  .settings(
    libraryDependencies ++= Seq(
      "eu.timepit" %%% "refined" % "0.11.3"
    )
  )

lazy val codecsRefinedLaws = kantanCrossProject("codecs-refined-laws", "codecs/refined/laws")
  .settings(moduleName := "kantan.codecs-refined-laws")
  .settings(libraryDependencies += "eu.timepit" %%% "refined-scalacheck" % "0.11.3")
  .enablePlugins(PublishedPlugin)
  .dependsOn(codecsCore, codecsLaws, codecsRefined)

// - enumeratum project ------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
lazy val codecsEnumeratum =
  kantanCrossProject("codecs-enumeratum", "codecs/enumeratum/core", "codecs-enumeratum-laws", true)
    .settings(moduleName := "kantan.codecs-enumeratum")
    .enablePlugins(PublishedPlugin)
    .dependsOn(codecsCore)
    .settings(
      scalacOptions ++= {
        scalaBinaryVersion.value match {
          case "3" =>
            // https://github.com/lloydmeta/enumeratum/blob/c76f9487bc86b5fc7/README.md?plain=1#L28
            Seq("-Yretain-trees")
          case _ =>
            Nil
        }
      },
      libraryDependencies ++= Seq(
        "com.beachape" %%% "enumeratum" % "1.9.0"
      )
    )

lazy val codecsEnumeratumLaws = kantanCrossProject("codecs-enumeratum-laws", "codecs/enumeratum/laws")
  .settings(moduleName := "kantan.codecs-enumeratum-laws")
  .settings(libraryDependencies += "com.beachape" %%% "enumeratum-scalacheck" % "1.9.0")
  .settings(
    scalacOptions ++= {
      scalaBinaryVersion.value match {
        case "3" =>
          // https://github.com/lloydmeta/enumeratum/blob/c76f9487bc86b5fc7/README.md?plain=1#L28
          Seq("-Yretain-trees")
        case _ =>
          Nil
      }
    }
  )
  .enablePlugins(PublishedPlugin)
  .dependsOn(codecsCore, codecsLaws, codecsEnumeratum)

// - shapeless projects ------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
lazy val codecsShapeless =
  kantanCrossProject("codecs-shapeless", "codecs/shapeless/core", "codecs-shapeless-laws", false)
    .settings(moduleName := "kantan.codecs-shapeless")
    .enablePlugins(PublishedPlugin)
    .dependsOn(codecsCore)
    .settings(
      libraryDependencies ++= Seq(
        "com.chuusai" %%% "shapeless" % "2.3.13"
      )
    )

lazy val codecsShapelessLaws = kantanCrossProject("codecs-shapeless-laws", "codecs/shapeless/laws", false)
  .settings(moduleName := "kantan.codecs-shapeless-laws")
  .enablePlugins(PublishedPlugin)
  .dependsOn(codecsCore, codecsLaws, codecsShapeless)
