import java.io.File

import gov.nasa.jpl.mbee.sbt._
import sbt.Keys._
import sbt._

import com.typesafe.sbt.SbtAspectj._
import com.typesafe.sbt.SbtAspectj.AspectjKeys._

lazy val copyPublishedArtifactLibraries = TaskKey[Unit]("copy-published-artifact-librariess", "Copies published artifact libraries")

lazy val jpl_omg_oti_magicdraw_dynamicscripts = Project("jpl-omg-oti-magicdraw-dynamicscripts", file(".")).
  settings(GitVersioning.buildSettings). // in principle, unnecessary; in practice: doesn't work without this
  enablePlugins(MBEEGitPlugin, MBEEMagicDrawEclipseClasspathPlugin).
  settings(aspectjSettings: _*).
  settings(
    MBEEKeys.mbeeLicenseYearOrRange := "2014-2015",
    MBEEKeys.mbeeOrganizationInfo := MBEEPlugin.MBEEOrganizations.imce,
    scalacOptions += "-g:vars",

    javacOptions += "-g:vars",

    extraAspectjOptions in Aspectj := Seq("-g"),

    // only compile the aspects (no weaving)
    compileOnly in Aspectj := true,

    // add the compiled aspects as products
    products in Compile <++= products in Aspectj,

    // only compile the aspects (no weaving)
    compileOnly in Aspectj := true,

    // add the compiled aspects as products
    products in Compile <++= products in Aspectj,

    classDirectory in Compile := baseDirectory.value / "bin",
    unmanagedClasspath in Compile <++= unmanagedJars in Compile,

    copyPublishedArtifactLibraries <<= publish,

    copyPublishedArtifactLibraries <<= (baseDirectory, packagedArtifacts, streams) map {
      (base, pas, s) =>
        for {
          ( a, f ) <- pas
          if f.ext == "jar"
          dir = a.`type` match {
            case "jar" => base / "lib"
            case "src" => base / "lib.srcs"
            case "doc" => base / "lib.javadoc"
          }
        } {
          s.log.info(s"artifact: ${a.`type`} ${a.name} \nfile: $f")
          IO.copyFile(f, dir / f.name )
        }
    }
  )

