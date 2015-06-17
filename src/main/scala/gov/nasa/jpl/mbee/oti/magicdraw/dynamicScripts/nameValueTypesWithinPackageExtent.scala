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
package gov.nasa.jpl.mbee.oti.magicdraw.dynamicScripts

import java.awt.event.ActionEvent
import scala.collection.JavaConversions._
import scala.language.implicitConversions
import scala.language.postfixOps
import scala.util.Success
import scala.util.Try
import com.nomagic.magicdraw.core.Application
import com.nomagic.magicdraw.core.Project
import com.nomagic.magicdraw.ui.browser.Node
import com.nomagic.magicdraw.ui.browser.Tree
import com.nomagic.magicdraw.uml.actions.SelectInContainmentTreeRunnable
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Profile
import org.omg.oti.magicdraw.MagicDrawUMLUtil
import gov.nasa.jpl.dynamicScripts.DynamicScriptsTypes
import gov.nasa.jpl.dynamicScripts.magicdraw.MagicDrawValidationDataResults
import org.omg.oti.changeMigration.Metamodel
import com.nomagic.magicdraw.core.ApplicationEnvironment
import java.io.File
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.xmi.XMLResource
import scala.util.Failure
import com.nomagic.magicdraw.uml.UUIDRegistry
import com.nomagic.magicdraw.core.utils.ChangeElementID
import com.nomagic.task.RunnableWithProgress
import com.nomagic.task.ProgressStatus
import com.nomagic.magicdraw.ui.MagicDrawProgressStatusRunner
import com.nomagic.magicdraw.core.ProjectUtilitiesInternal
import java.util.UUID
import com.nomagic.ci.persistence.local.spi.localproject.LocalPrimaryProject

import org.omg.oti.api._
import org.omg.oti.magicdraw.MagicDrawUML
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement
import com.nomagic.magicdraw.uml.symbols.shapes.PackageView
import com.nomagic.magicdraw.uml.symbols.PresentationElement

/**
 * @author Nicolas.F.Rouquette@jpl.nasa.gov
 */
object nameValueTypesWithinPackageExtent {

  def doit(
    p: Project,
    ev: ActionEvent,
    script: DynamicScriptsTypes.DiagramContextMenuAction,
    dpe: DiagramPresentationElement,
    triggerView: PackageView,
    triggerElement: Package,
    selection: java.util.Collection[PresentationElement] ): Try[Option[MagicDrawValidationDataResults]] = {
    
    val app = Application.getInstance()
    val guiLog = app.getGUILog()
    guiLog.clearLog()

    val umlUtil = MagicDrawUMLUtil( p )
    import umlUtil._
    
    val selectedPackages = selection.toIterator selectByKindOf ( { case pv: PackageView => umlPackage( pv.getPackage ) } ) toList;    
    selectedPackages foreach ( nameValueTypes( umlUtil, _ ) )

    Success( None )
  }
  
  def doit(
    p: Project,
    ev: ActionEvent,
    script: DynamicScriptsTypes.BrowserContextMenuAction,
    tree: Tree,
    node: Node,
    top: Package,
    selection: java.util.Collection[Element] ): Try[Option[MagicDrawValidationDataResults]] = {

    val a = Application.getInstance()
    val guiLog = a.getGUILog()
    guiLog.clearLog()

    val umlUtil = MagicDrawUMLUtil( p )
    import umlUtil._

    val selectedPackages = selection.toIterator selectByKindOf ( { case p: Package => umlPackage( p ) } ) toList;    
    selectedPackages foreach ( nameValueTypes( umlUtil, _ ) )

    Success( None )
  }

  val VT_NAME_PATTERN = "^(.*)\\[(.*)\\]$".r
  
  def nameValueTypes( umlUtil: MagicDrawUMLUtil, p: UMLPackage[MagicDrawUML] ): Unit = {
    import umlUtil._
    val app = Application.getInstance()
    val guiLog = app.getGUILog()

    System.out.println(s"name value types in: ${p.qualifiedName.get}")
    
    val dts = p.allOwnedElements.selectByKindOf{ case dt: UMLDataType[MagicDrawUML] => dt }
    var renames = 0
    var skipped = 0
    dts.foreach { dt =>
      dt.name match {
        case Some( VT_NAME_PATTERN(q,u) ) =>
          renames = renames+1
          umlMagicDrawUMLElement(dt).getMagicDrawElement.asInstanceOf[MagicDrawUML#DataType].setName(q+"("+u+")")
          
        case _ =>
          skipped = skipped+1
          ()
      }
    }
    guiLog.log( s"Done (renamed ${renames} value types, skipped ${skipped}) !" )
    System.out.println( s"Done (renamed ${renames} value types, skipped ${skipped}) !" )
  }
}