package gov.nasa.jpl.imce.oti.magicdraw.dynamicScripts

import java.awt.event.ActionEvent

import com.nomagic.magicdraw.core.Project
import com.nomagic.magicdraw.uml.symbols.{DiagramPresentationElement, PresentationElement}
import com.nomagic.magicdraw.uml.symbols.shapes.InstanceSpecificationView
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.{Diagram, Element, InstanceSpecification}
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype
import gov.nasa.jpl.dynamicScripts.DynamicScriptsTypes
import gov.nasa.jpl.dynamicScripts.magicdraw.validation.MagicDrawValidationDataResults

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.collection.immutable.Set
import scala.{None, Option, Some, StringContext}
import scala.util.{Failure, Success, Try}

object AddAsQUDVDiagramPrimaryElements {

  val QUDVDIAGRAM_STEREOTYPE_ID = "_17_0_5_1_ff3038a_1409678720985_699141_10522"

  def doit
  (p: Project, ev: ActionEvent,
   script: DynamicScriptsTypes.DiagramContextMenuAction,
   dpe: DiagramPresentationElement,
   triggerView: PresentationElement,
   triggerElement: Element,
   selection: java.util.Collection[PresentationElement])
  : Try[Option[MagicDrawValidationDataResults]]
  = Option.apply(p.getElementByID(QUDVDIAGRAM_STEREOTYPE_ID)).fold[Try[Option[MagicDrawValidationDataResults]]]{
    Failure(new java.lang.IllegalArgumentException(
      s"No 'QUDVDiagram' stereotype found (ID=${QUDVDIAGRAM_STEREOTYPE_ID})"))
  } { case qudvDiagramS: Stereotype =>

    QUDVHelper.addAsPrimaryQUDVElements(
      p,
      dpe.getDiagram,
      qudvDiagramS,
      selection
        .flatMap {
          case iv: InstanceSpecificationView =>
            iv.getElement match {
              case is: InstanceSpecification
                if is.getClassifier.exists(cls => "IMCE.QUDV" == cls.getOwningPackage.getName) =>
                Some(is)
              case _ =>
                None
            }
          case _ =>
            None
        }
        .to[Set])
  }
}

@scala.deprecated("", "")
trait QUDVHelper

object QUDVHelper {

  def addAsPrimaryQUDVElements
  (p: Project,
   d: Diagram,
   qudvDiagramS: Stereotype,
   qudvElements: Set[InstanceSpecification])
  : Try[Option[MagicDrawValidationDataResults]]
  = {
    val currentValue = StereotypesHelper.getStereotypePropertyValue(d, qudvDiagramS, "primaryQUDVElements", true)
    val newValue = (currentValue.to[Set] ++ qudvElements).toList.asJava
    StereotypesHelper.setStereotypePropertyValue(d, qudvDiagramS, "primaryQUDVElements", newValue)

    Success(None)
  }
}

