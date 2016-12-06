package gov.nasa.jpl.imce.oti.magicdraw.dynamicScripts

import java.awt.event.ActionEvent
import java.lang.System

import com.nomagic.magicdraw.core.Project
import com.nomagic.magicdraw.uml.symbols.{DiagramPresentationElement, PresentationElement}
import com.nomagic.magicdraw.uml.symbols.shapes.{InstanceSlotsCompartmentView, InstanceSpecificationHeaderView, InstanceSpecificationView}
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.{Element, InstanceSpecification, Slot}
import gov.nasa.jpl.dynamicScripts.DynamicScriptsTypes
import gov.nasa.jpl.dynamicScripts.magicdraw.validation.MagicDrawValidationDataResults

import scala.collection.JavaConversions._
import scala.{None, Option, StringContext, Unit}
import scala.util.{Success, Try}

object ToggleInstanceSpecificationSlotVisibility {

  def doit
  (p: Project, ev: ActionEvent,
   script: DynamicScriptsTypes.DiagramContextMenuAction,
   dpe: DiagramPresentationElement,
   triggerView: PresentationElement,
   triggerElement: Element,
   selection: java.util.Collection[PresentationElement] )
  : Try[Option[MagicDrawValidationDataResults]]
  = {
    selection.foreach {
      case iv: InstanceSpecificationView =>
        Toggler.toggle(p, iv)

      case _ =>
        ()
    }
    Success(None)
  }
}

@scala.deprecated("", "")
trait Toggler

object Toggler {

  def toggle
  (p: Project,
   iv: InstanceSpecificationView)
  : Unit
  = {
    val i: InstanceSpecification = iv.getInstanceSpecification
    val slotCompartment: InstanceSlotsCompartmentView = iv.getHeaderView match {
      case ishv: InstanceSpecificationHeaderView =>
        ishv.getInstanceSlotsCompartmentView
    }
    val views = slotCompartment.getViews

    System.out.println(s"\nToggler (${views.size} views): '${i.getQualifiedName}')")
    views.foreach { view =>
      view.getElement match {
        case s: Slot =>
          val f = s.getDefiningFeature
          f.getName match {
            case "symbol" =>
              view.setVisible(false)
              System.out.println(s"hide: ${s.getDefiningFeature.getQualifiedName}")
            case _ =>
              System.out.println(s"keep: ${s.getDefiningFeature.getQualifiedName}")
          }
      }
    }
  }
}