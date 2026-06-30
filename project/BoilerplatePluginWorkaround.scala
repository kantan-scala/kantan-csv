import sbt.*
import sbt.Keys.*
import spray.boilerplate.BoilerplatePlugin
import spray.boilerplate.BoilerplatePlugin.autoImport.*

/** workaround for https://github.com/sbt/sbt-boilerplate/pull/104
  *
  * TODO remove if new version released
  */
object BoilerplatePluginWorkaround extends AutoPlugin {
  override def requires: Plugins = BoilerplatePlugin

  override def trigger = allRequirements

  override def projectSettings: Seq[Def.Setting[?]] =
    Seq(Compile, Test).flatMap(x =>
      inConfig(x)(
        boilerplateGenerate := Def.uncached(
          BoilerplatePlugin.generateFromTemplates(
            streams.value,
            boilerplateSignature.value,
            boilerplateSource.value,
            sourceManaged.value,
            boilerplateGeneratedExtension.value
          )
        )
      )
    )
}
