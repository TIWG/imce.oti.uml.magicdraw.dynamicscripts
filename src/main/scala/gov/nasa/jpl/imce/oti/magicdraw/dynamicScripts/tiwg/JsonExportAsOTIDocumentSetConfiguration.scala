package gov.nasa.jpl.imce.oti.magicdraw.dynamicScripts.tiwg

import java.awt.event.ActionEvent
import java.lang.System
import java.util.concurrent.TimeUnit

import com.nomagic.magicdraw.core.{Application, Project}
import com.nomagic.magicdraw.uml.symbols.{DiagramPresentationElement, PresentationElement}
import com.nomagic.magicdraw.uml.symbols.shapes.PackageView
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Profile
import gov.nasa.jpl.dynamicScripts.DynamicScriptsTypes
import gov.nasa.jpl.dynamicScripts.magicdraw.ui.symbols.internal.SymbolHelper._
import gov.nasa.jpl.dynamicScripts.magicdraw.utils.MDUML
import gov.nasa.jpl.dynamicScripts.magicdraw.utils.MDUML._
import gov.nasa.jpl.dynamicScripts.magicdraw.validation.MagicDrawValidationDataResults
import gov.nasa.jpl.imce.oti.magicdraw.dynamicScripts.utils.OTIHelper
import gov.nasa.jpl.imce.oti.magicdraw.dynamicScripts.validation.OTIMagicDrawValidation
import play.api.libs.json._
import org.omg.oti.magicdraw.uml.canonicalXMI.helper.{MagicDrawOTIHelper, MagicDrawOTIProfileAdapter}
import org.omg.oti.magicdraw.uml.read.{MagicDrawUML, MagicDrawUMLUtil}
import org.omg.oti.json.common._
import org.omg.oti.json.extent.OTIDocumentExtent
import org.omg.oti.json.uml.serialization.OTIJsonSerializationHelper
import org.omg.oti.uml._
import org.omg.oti.uml.read.api.UMLPackage

import scala.collection.JavaConversions._
import scala.collection.immutable._
import scala.{Boolean, Int, Long, None, Option, Some, StringContext, Unit}
import scala.Predef.{ArrowAssoc, String, augmentString, refArrayOps}
import scala.util.{Failure, Success, Try}
import scalaz._
import Scalaz._
/**
  * Created by rouquett on 4/13/16.
  */
object JsonExportAsOTIDocumentSetConfiguration {

  def doit
  (p: Project,
   ev: ActionEvent,
   script: DynamicScriptsTypes.DiagramContextMenuAction,
   dpe: DiagramPresentationElement,
   triggerView: PackageView,
   triggerElement: Profile,
   selection: java.util.Collection[PresentationElement])
  : Try[Option[MagicDrawValidationDataResults]]
  = doit(p, ev, script, dpe, triggerView, triggerElement, selection)

  def doit
  (p: Project,
   ev: ActionEvent,
   script: DynamicScriptsTypes.DiagramContextMenuAction,
   dpe: DiagramPresentationElement,
   triggerView: PackageView,
   triggerElement: Package,
   selection: java.util.Collection[PresentationElement])
  : Try[Option[MagicDrawValidationDataResults]]
  = OTIHelper.toTry(
    MagicDrawOTIHelper.getOTIMagicDrawAdapterForProfileCharacteristics(p),
    (oa: MagicDrawOTIProfileAdapter) => {

      val app = Application.getInstance()
      val guiLog = app.getGUILog
      guiLog.clearLog()

      implicit val umlUtil = MagicDrawUMLUtil(p)
      import umlUtil._

      val selectedSpecificationRootPackages
      : Set[UMLPackage[Uml]]
      = selection
        .toSet
        .selectByKindOf {
          case pv: PackageView =>
            umlPackage(getPackageOfView(pv).get)
        }

      val otiV = OTIMagicDrawValidation(p)

      val emptyConfig
      : Set[java.lang.Throwable] \&/ OTIDocumentSetConfiguration
      = OTIDocumentSetConfiguration.empty.that

      val t0: Long = java.lang.System.currentTimeMillis()

      val result = for {
        odsa <- MagicDrawOTIHelper.getOTIMagicDrawProfileDocumentSetAdapter(oa, selectedSpecificationRootPackages)
        t1 = java.lang.System.currentTimeMillis()
        _ = {
          System.out.println(
            s"JsonExportAsOTIDocumentSetConfiguration.getOTIMagicDrawProfileDocumentSetAdapter "+
              s"in ${prettyFiniteDuration(t1 - t0, TimeUnit.MILLISECONDS)}")
        }

        config <- ( emptyConfig /: selectedSpecificationRootPackages ) { addSpecificationRootPackage(oa) }
        t2 = java.lang.System.currentTimeMillis()
        _ = {
          System.out.println(
            s"JsonExportAsOTIDocumentSetConfiguration.addSpecificationRootPackages "+
              s"in ${prettyFiniteDuration(t2 - t1, TimeUnit.MILLISECONDS)}")
        }
      } yield {
        val jconfig = Json.toJson(config)
        System.out.println(Json.prettyPrint(jconfig))

        val mdInstallDir = MDUML.getApplicationInstallDir.toPath

        val jsonExportZipURI = mdInstallDir.resolve(s"dynamicScripts/MagicDraw-${p.getPrimaryProjectID}.zip").toUri

        // @see http://www.oracle.com/technetwork/articles/java/compress-1565076.html
        val fos = new java.io.FileOutputStream(new java.io.File(jsonExportZipURI))
        val bos = new java.io.BufferedOutputStream(fos, 100000)
        val cos = new java.util.zip.CheckedOutputStream(bos, new java.util.zip.Adler32())
        val zos = new java.util.zip.ZipOutputStream(new java.io.BufferedOutputStream(cos))

        zos.setMethod(java.util.zip.ZipOutputStream.DEFLATED)

        val jHelper = OTIJsonSerializationHelper(odsa)
        var size: Int = 0
        var nb: Int = 0
        odsa.ds.allDocuments.foreach { d =>
          val p = d.scope
          if (selectedSpecificationRootPackages.contains(p)) {
            nb = nb + 1
            size = size + d.extent
              .size
            val e0: Long = java.lang.System.currentTimeMillis()

            val d0 = OTIDocumentExtent(d.info, p.toolSpecific_id, p.toolSpecific_url)
            val dN = (d0 /: d.extent) { (di, e) => jHelper.addToOTIDocumentExtent( di, e ) }

            val e1: Long = java.lang.System.currentTimeMillis()
            System.out.println(
              s"JsonExportAsOTIDocumentSetConfiguration.extent(${p.qualifiedName.get} with ${d.extent.size} elements) "+
                s"in ${prettyFiniteDuration(e1 - e0, TimeUnit.MILLISECONDS)}")


            val dj = Json.toJson(dN)

            val e2: Long = java.lang.System.currentTimeMillis()
            System.out.println(
              s"JsonExportAsOTIDocumentSetConfiguration.toJson(${p.qualifiedName.get} with ${d.extent.size} elements) "+
                s"in ${prettyFiniteDuration(e2 - e1, TimeUnit.MILLISECONDS)}")

            val dRelativePath: String = Tag.unwrap(d.info.documentURL).stripPrefix("http://")
            val entry = new java.util.zip.ZipEntry(dRelativePath)
            zos.putNextEntry(entry)

            val s = Json.prettyPrint(dj)
            zos.write(s.getBytes(java.nio.charset.Charset.forName("UTF-8")))

            zos.closeEntry()

            val e3: Long = java.lang.System.currentTimeMillis()
            System.out.println(
              s"JsonExportAsOTIDocumentSetConfiguration.pretty(${p.qualifiedName.get} with ${d.extent.size} elements) "+
                s"in ${prettyFiniteDuration(e3 - e2, TimeUnit.MILLISECONDS)}")

          }
        }

        zos.close()
        val tN = java.lang.System.currentTimeMillis()
        System.out.println(
            s"JsonExportAsOTIDocumentSetConfiguration.overall ($nb OTI document packages totalling $size elements) "+
              s"in ${prettyFiniteDuration(tN - t0, TimeUnit.MILLISECONDS)}")

        System.out.println(s"zip: $jsonExportZipURI")
        ()
      }

      otiV.errorSet2TryOptionMDValidationDataResults(p, "*** Json Export as OTIDocumentSetConfiguration ***", result.a)

    })

  def addSpecificationRootPackage
  (oa: MagicDrawOTIProfileAdapter)
  (ri: Set[java.lang.Throwable] \&/ OTIDocumentSetConfiguration,
   p: UMLPackage[MagicDrawUML])
  : Set[java.lang.Throwable] \&/ OTIDocumentSetConfiguration
  = for {
    current <- ri
    pInfo <- oa.otiCharacteristicsProvider.getSpecificationRootCharacteristics(p).toThese

  } yield pInfo.fold[OTIDocumentSetConfiguration](current) { info =>
    current.copy(
      documents =
        current.documents + OTIDocumentConfiguration(info, p.toolSpecific_id, p.toolSpecific_url) )
  }


  def showJson
  (pkg: UMLPackage[MagicDrawUML])
  (implicit oa: MagicDrawOTIProfileAdapter)
  : Unit
  = {
    val elements = pkg.allOwnedElements

  }
}
