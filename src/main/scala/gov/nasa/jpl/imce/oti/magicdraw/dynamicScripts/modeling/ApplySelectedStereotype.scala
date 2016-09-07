/*
 * Copyright 2016 California Institute of Technology ("Caltech").
 * U.S. Government sponsorship acknowledged.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * License Terms
 */

package gov.nasa.jpl.imce.oti.magicdraw.dynamicScripts.modeling

import java.awt.event.ActionEvent
import java.lang.IllegalArgumentException

import com.nomagic.magicdraw.core.{Application, Project}
import com.nomagic.magicdraw.uml.symbols.{DiagramPresentationElement, PresentationElement}
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element
import gov.nasa.jpl.dynamicScripts.DynamicScriptsTypes
import gov.nasa.jpl.dynamicScripts.magicdraw.utils.MDUML._
import gov.nasa.jpl.dynamicScripts.magicdraw.validation.MagicDrawValidationDataResults
import org.omg.oti.magicdraw.uml.read.MagicDrawUMLUtil
import org.omg.oti.uml.read.api._

import scala.collection.JavaConversions._
import scala.collection.immutable._
import scala.util.{Failure, Success, Try}
import scala.{Option,None,Some}
/**
* Diagram selection: the elements to apply the browser-selected stereotype(s) to
* Browser selection: the stereotypes to be applied to the diagram-selected elements
*/
object ApplySelectedStereotype {

  def doit(
    p: Project,
    ev: ActionEvent,
    script: DynamicScriptsTypes.DiagramContextMenuAction,
    dpe: DiagramPresentationElement,
    triggerView: PresentationElement,
    triggerElement: Element,
    selection: java.util.Collection[PresentationElement] ): Try[Option[MagicDrawValidationDataResults]] = {
    
    val app = Application.getInstance()
    val guiLog = app.getGUILog
    guiLog.clearLog()

    val umlUtil = MagicDrawUMLUtil( p )
    import umlUtil._

    val stereotypes = p.getBrowserTreeSelection match {
      case None =>
        return Failure(new IllegalArgumentException("Select a stereotype in the browser tree"))
      case Some(bInfo) =>
        bInfo.selection flatMap { bneInfo =>
          umlElement( bneInfo.e ) match {
              case s: UMLStereotype[Uml] =>
                Some(s)
              case _ =>
                None
          }
        }
    }

    val mdStereotypes = stereotypes.map ( umlMagicDrawUMLStereotype(_).getMagicDrawStereotype )

    if (mdStereotypes.isEmpty) {
      guiLog.log("Select at least 1 stereotype to apply in the MD browser")
      return Success(None)
    }

    val selectedElements =
      selection
      .toIterable
      .selectByKindOf { case pe: PresentationElement => umlElement( pe.getElement ) }
      .to[List]

    selectedElements foreach { e =>
      val mdE = umlMagicDrawUMLElement(e).getMagicDrawElement
      mdStereotypes foreach { mdS =>
        StereotypesHelper.addStereotype(mdE, mdS)
      }
    }

    guiLog.log("- Done")
    Success( None )
  }

}