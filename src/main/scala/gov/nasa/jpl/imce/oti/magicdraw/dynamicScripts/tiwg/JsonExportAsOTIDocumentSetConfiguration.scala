package gov.nasa.jpl.imce.oti.magicdraw.dynamicScripts.tiwg

import java.awt.event.ActionEvent
import java.lang.System

import com.nomagic.magicdraw.core.{Application, Project}
import com.nomagic.magicdraw.uml.symbols.{DiagramPresentationElement, PresentationElement}
import com.nomagic.magicdraw.uml.symbols.shapes.PackageView
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package

import gov.nasa.jpl.dynamicScripts.DynamicScriptsTypes
import gov.nasa.jpl.dynamicScripts.magicdraw.ui.symbols.internal.SymbolHelper._
import gov.nasa.jpl.dynamicScripts.magicdraw.validation.MagicDrawValidationDataResults
import gov.nasa.jpl.imce.oti.magicdraw.dynamicScripts.utils.OTIHelper
import gov.nasa.jpl.imce.oti.magicdraw.dynamicScripts.validation.OTIMagicDrawValidation

import play.api.libs.json._

import org.omg.oti.magicdraw.uml.canonicalXMI.helper.{MagicDrawOTIHelper, MagicDrawOTIProfileAdapter}
import org.omg.oti.magicdraw.uml.read.{MagicDrawUML, MagicDrawUMLUtil}
import org.omg.oti.json.common._
import org.omg.oti.uml.read.api.UMLPackage

import scala.collection.JavaConversions._
import scala.collection.immutable._
import scala.{Boolean, None, Option, Some, StringContext, Unit}
import scala.Predef.{refArrayOps,ArrowAssoc}
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

      val r0
      : Set[java.lang.Throwable] \/ OTIDocumentSetConfiguration
      = OTIDocumentSetConfiguration.empty.right

      val rN
      : Set[java.lang.Throwable] \/ OTIDocumentSetConfiguration
      = (r0 /: selectedSpecificationRootPackages) {
        addSpecificationRootPackage(oa)
      }

      val otiV = OTIMagicDrawValidation(p)

      rN.fold(
        (errors: Set[java.lang.Throwable]) =>
          otiV.errorSet2TryOptionMDValidationDataResults(p, "*** Json Export as OTIDocumentSetConfiguration ***", Some(errors)),
        (config: OTIDocumentSetConfiguration) => {

          val jconfig = Json.toJson(config)
          guiLog.log(Json.stringify(jconfig))

          System.out.println(Json.prettyPrint(jconfig))

          Success(None)
        })
    })



  def addSpecificationRootPackage
  (oa: MagicDrawOTIProfileAdapter)
  (ri: Set[java.lang.Throwable] \/ OTIDocumentSetConfiguration,
   p: UMLPackage[MagicDrawUML])
  : Set[java.lang.Throwable] \/ OTIDocumentSetConfiguration
  = for {
    current <- ri
    pInfo <- oa.otiCharacteristicsProvider.getSpecificationRootCharacteristics(p)

  } yield pInfo.fold[OTIDocumentSetConfiguration](current) { info =>
    current.copy( documents = current.documents + new OTIDocumentConfiguration(p.toolSpecific_id, info) )
  }

}
