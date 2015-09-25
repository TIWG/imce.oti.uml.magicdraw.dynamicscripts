/*
 *
 * License Terms
 *
 * Copyright (c) 2014-2015, California Institute of Technology ("Caltech").
 * U.S. Government sponsorship acknowledged.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * *   Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * *   Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the
 *    distribution.
 *
 * *   Neither the name of Caltech nor its operating division, the Jet
 *    Propulsion Laboratory, nor the names of its contributors may be
 *    used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package gov.nasa.jpl.mbee.oti.magicdraw.dynamicScripts.modeling

import java.awt.event.ActionEvent
import java.io.File

import com.nomagic.magicdraw.core.{ApplicationEnvironment, Application, Project}
import com.nomagic.magicdraw.uml.symbols.{DiagramPresentationElement, PresentationElement}
import com.nomagic.uml2.ext.jmi.helpers.{ModelHelper, StereotypesHelper}
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element
import gov.nasa.jpl.dynamicScripts.DynamicScriptsTypes
import gov.nasa.jpl.dynamicScripts.magicdraw.{DynamicScriptsPlugin, MagicDrawValidationDataResults}
import gov.nasa.jpl.dynamicScripts.magicdraw.utils.MDUML
import gov.nasa.jpl.mbee.oti.magicdraw.dynamicScripts.utils.MDAPI

import org.omg.oti.uml.xmi._
import org.omg.oti.magicdraw.uml.read._
import org.omg.oti.magicdraw.uml.write.MagicDrawUMLUpdate
import org.omg.oti.uml.canonicalXMI.{UnresolvedElementCrossReference, ResolvedDocumentSet, CatalogURIMapper}
import org.omg.oti.magicdraw.uml.canonicalXMI._
import org.omg.oti.uml.read.api._

import scala.collection.Iterable
import scala.collection.JavaConversions._
import scala.collection.immutable._
import scala.language.{implicitConversions, postfixOps}
import scala.util.{Failure, Success, Try}

/**
* Diagram selection: the elements whose ownership will be changed to the browser-selected element
* Browser selection: the single element that will be the new owner of the diagram-selected elements
*/
object ChangeOwner {

  def doit
  (p: Project,
   ev: ActionEvent,
   script: DynamicScriptsTypes.DiagramContextMenuAction,
   dpe: DiagramPresentationElement,
   triggerView: PresentationElement,
   triggerElement: Element,
   selection: java.util.Collection[PresentationElement] )
  : Try[Option[MagicDrawValidationDataResults]] = {
    
    val app = Application.getInstance()
    val guiLog = app.getGUILog
    guiLog.clearLog()

    implicit val umlUtil = MagicDrawUMLUtil( p )
    import umlUtil._

    val upd = MagicDrawUMLUpdate(umlUtil)

    val newOwner = MDUML.getBrowserTreeSelection() match {
      case None =>
        return Failure(new IllegalArgumentException("Select a single element (new owner) in the browser tree"))
      case Some(bInfo) if 1 == bInfo.selection.size =>
        umlElement( bInfo.selection.head.e )
      case _ =>
        return Failure(new IllegalArgumentException("Select a single element (new owner) in the browser tree"))
    }

    val selectedElements =
      selection
      .toIterable
      .selectByKindOf { case pe: PresentationElement => umlElement( pe.getElement ) }
      .to[List]


    MDAPI
      .getMDCatalogs()
      .flatMap { case (documentURIMapper, builtInURIMapper) =>

        MagicDrawDocumentSet
        .createMagicDrawProjectDocumentSet(
            documentURIMapper, builtInURIMapper,
            ignoreCrossReferencedElementFilter = MDAPI.ignoreCrossReferencedElementFilter,
            unresolvedElementMapper = MDAPI.unresolvedElementMapper(umlUtil))
        .transform[Option[MagicDrawValidationDataResults]](
        {
          info: MagicDrawDocumentSet.MagicDrawDocumentSetInfo
          //(MagicDrawOTISymbols, ResolvedDocumentSet[MagicDrawUML], MagicDrawDocumentSet, Iterable[UnresolvedElementCrossReference[MagicDrawUML]])
          =>

          implicit val idg: MagicDrawIDGenerator = MagicDrawIDGenerator(info._2)

          selectedElements.foreach { e =>
            changeOwner(newOwner, e)
              .recover{ case t: Throwable =>
              return Failure(t)
            }
          }

            Success(None)

        },
        {
          f => Failure(f)
        })
    }

  }

  def changeOwner[Uml <: UML]
  (newParent: UMLElement[Uml],
   relocatedChild: UMLElement[Uml])
  (implicit idg: IDGenerator[Uml])
  : Try[Unit] =
    relocatedChild.getContainingMetaPropertyEvaluator match {
      case Failure(f) =>
        Failure(f)
      case Success(None) =>
        Failure(new IllegalArgumentException("cannot change the ownership of the root element"))
      case Success(Some(m)) =>
        if (!newParent.compositeMetaProperties.contains(m))
          Failure(new IllegalArgumentException("Different ownership properties!"))
        else {
          Success(Unit)
        }
    }
}