package gov.nasa.jpl.imce.oti.magicdraw.dynamicScripts.tiwg

import java.awt.event.ActionEvent

import com.nomagic.magicdraw.core.{Application, Project}
import com.nomagic.magicdraw.ui.browser.{Node, Tree}
import com.nomagic.magicdraw.uml.symbols.shapes.PackageView
import com.nomagic.magicdraw.uml.symbols.{DiagramPresentationElement, PresentationElement}
import com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdmodels.Model
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.{Element, Package}
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Profile
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper
import gov.nasa.jpl.dynamicScripts.DynamicScriptsTypes
import gov.nasa.jpl.dynamicScripts.magicdraw.ui.symbols.internal.SymbolHelper._
import gov.nasa.jpl.dynamicScripts.magicdraw.validation.MagicDrawValidationDataResults
import gov.nasa.jpl.imce.oti.magicdraw.dynamicScripts.utils.MDAPI
import gov.nasa.jpl.imce.oti.magicdraw.dynamicScripts.validation.OTIMagicDrawValidation
import org.omg.oti.magicdraw.uml.canonicalXMI.helper._
import org.omg.oti.magicdraw.uml.characteristics.MagicDrawOTICharacteristicsProfileProvider
import org.omg.oti.magicdraw.uml.read._
import org.omg.oti.uml.UMLError
import org.omg.oti.uml.read.api._
import org.omg.oti.uml.xmi.Document

import scala.Predef.ArrowAssoc
import scala.collection.JavaConversions._
import scala.collection.immutable._
import scala.language.{implicitConversions, postfixOps}
import scalaz._
import Scalaz._
import scala.Predef.String
import scala.{None, Option, Some, StringContext, Unit}

object CreateOTICharacterizationsForSelectedPackages {

  def doit
  (p: Project, ev: ActionEvent,
   script: DynamicScriptsTypes.DiagramContextMenuAction,
   dpe: DiagramPresentationElement,
   triggerView: PackageView,
   triggerElement: Package,
   selection: java.util.Collection[PresentationElement])
  : scala.util.Try[Option[MagicDrawValidationDataResults]]
  = createOTICharacterizations(
    p,
    triggerElement,
    selection
      .flatMap { case pv: PackageView => getPackageOfView(pv).map { pkg => (pv, pkg) } }
      .to[Seq])

  def createOTICharacterizations
  (p: Project,
   trigger: Package,
   selection: Seq[(PackageView, Package)])
  : scala.util.Try[Option[MagicDrawValidationDataResults]] = {

    val a = Application.getInstance()
    val guiLog = a.getGUILog
    guiLog.clearLog()

    val otiCharacterizations: Option[Map[UMLPackage[MagicDrawUML], UMLComment[MagicDrawUML]]] = None

    MagicDrawOTIAdapters.initializeWithProfileCharacterizations(p, otiCharacterizations)()
      .fold[scala.util.Try[Option[MagicDrawValidationDataResults]]](
      (nels: Set[java.lang.Throwable]) =>
        scala.util.Failure(nels.head),
      (oa: MagicDrawOTIProfileAdapter) => {

        val a = Application.getInstance()
        val guiLog = a.getGUILog
        guiLog.clearLog()

        implicit val umlOps = oa.umlOps
        import umlOps._

        val context: UMLPackage[MagicDrawUML] = trigger
        val toBeCharacterized
        : Seq[(PackageView, UMLPackage[MagicDrawUML])]
        = selection
          .map { case (pv, pkg) => (pv, umlPackage(pkg)) }
          .sortBy(_._2.qualifiedName.get)

        val message: String = s"Context: ${context.qualifiedName.get}"
        guiLog.log(message)

        val chProv = oa.otiCharacteristicsProvider match {
          case pchProv: MagicDrawOTICharacteristicsProfileProvider =>
            pchProv
          case _ =>
            return scala.util.Failure(UMLError.umlAdaptationError("Need an MD OTICharacteristics Profile Provider"))
        }

        val r0
        : Set[java.lang.Throwable] \/ Unit
        = \/-(())

        val rN
        : Set[java.lang.Throwable] \/ Unit
        = (r0 /: toBeCharacterized) { case (ri, (pv, pkg)) =>

          val inc
          : Set[java.lang.Throwable] \/ Unit
          = for {
            chS <- chProv.OTI_SPECIFICATION_ROOT_CHARACTERIZATION_S

            c <- oa.umlF.createUMLComment
            _ <- oa.umlU.Element_owningElement_ownedComment_Comment.link(context, c)
            _ <- oa.umlU.Comment_comment_annotatedElement_Element.link(c, pkg)

            _ = StereotypesHelper.addStereotype(
              umlMagicDrawUMLComment(c).getMagicDrawComment,
              umlMagicDrawUMLStereotype(chS).getMagicDrawStereotype)

          } yield ()

          ri +++ inc

        }

        val otiV = OTIMagicDrawValidation(p)
        otiV.errorSet2TryOptionMDValidationDataResults(p, s"*** OTI Package Inspector ***", rN.swap.toOption)

      })


  }

}