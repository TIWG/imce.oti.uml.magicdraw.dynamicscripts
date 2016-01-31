
import gov.nasa.jpl.mbee.sbt._
import sbt.Keys._
import sbt._

lazy val copyPublishedArtifactLibraries = TaskKey[Unit]("copy-published-artifact-libraries", "Copies published artifact libraries")

lazy val jpl_omg_oti_magicdraw_dynamicscripts = Project("jpl-omg-oti-magicdraw-dynamicscripts", file("."))
  .settings(GitVersioning.buildSettings) // in principle, unnecessary; in practice: doesn't work without this
  .enablePlugins(MBEEGitPlugin, MBEEMagicDrawEclipseClasspathPlugin)
  //.settings(MBEEPlugin.mbeeDynamicScriptsProjectResourceSettings(Some("jpl.omg.oti.magicdraw.dynamicscripts")))
  .settings(MBEEPlugin.mbeeAspectJSettings)
  .settings(
    MBEEKeys.mbeeLicenseYearOrRange := "2014-2015",
    MBEEKeys.mbeeOrganizationInfo := MBEEPlugin.MBEEOrganizations.imce,
    MBEEKeys.targetJDK := MBEEKeys.jdk17.value,

    classDirectory in Compile := baseDirectory.value / "bin",

    libraryDependencies ++= Seq(
      MBEEPlugin.MBEEOrganizations.imce.mbeeZipArtifactVersion(
        "jpl-mbee-common-owlapi-libraries",
        MBEEKeys.mbeeReleaseVersionPrefix.value, Versions.jpl_mbee_common_scala_libraries_revision
      ),
      MBEEPlugin.MBEEOrganizations.imce.mbeeZipArtifactVersion(
        "jpl-mbee-common-jena-libraries",
        MBEEKeys.mbeeReleaseVersionPrefix.value, Versions.jpl_mbee_common_scala_libraries_revision
      ),
      MBEEPlugin.MBEEOrganizations.oti.mbeeArtifactVersion(
        "oti-core",
        Versions.oti_core_prefix, Versions.oti_core_suffix
      ),
      MBEEPlugin.MBEEOrganizations.oti.mbeeArtifactVersion(
        "oti-change-migration",
        Versions.oti_changeMigration_prefix, Versions.oti_changeMigration_suffix
      ),
      MBEEPlugin.MBEEOrganizations.oti.mbeeArtifactVersion(
        "oti-trees",
        Versions.oti_trees_prefix, Versions.oti_trees_suffix
      ),
      MBEEPlugin.MBEEOrganizations.oti.mbeeArtifactVersion(
        "oti-canonical-xmi",
        Versions.oti_canonical_xmi_prefix, Versions.oti_canonical_xmi_suffix
      ),
      MBEEPlugin.MBEEOrganizations.oti.mbeeArtifactVersion(
        "oti-loader",
        Versions.oti_loader_prefix, Versions.oti_loader_suffix
      ),
      MBEEPlugin.MBEEOrganizations.oti.mbeeArtifactVersion(
        "oti-magicdraw",
        Versions.oti_magicdraw_prefix, Versions.oti_magicdraw_suffix
      )
    ),

    copyPublishedArtifactLibraries <<= publish,

    copyPublishedArtifactLibraries <<= (baseDirectory, packagedArtifacts, streams) map {
      (base, pas, s) =>
        for {
          ( a, f ) <- pas
          if f.ext == "jar"
          dir = a.`type` match {
            case "jar" => base / "lib"
            case "src" => base / "lib.sources"
            case "doc" => base / "lib.javadoc"
          }
        } {
          s.log.info(s"artifact: ${a.`type`} ${a.name} \nfile: $f")
          IO.copyFile(f, dir / f.name )
        }
    }
  )

