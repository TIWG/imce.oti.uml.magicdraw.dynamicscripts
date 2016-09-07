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

package gov.nasa.jpl.imce.oti.magicdraw.dynamicScripts

import java.lang.System
import java.awt.event.ActionEvent

import com.nomagic.magicdraw.core.Application
import com.nomagic.magicdraw.core.Project
import com.nomagic.magicdraw.ui.browser.Node
import com.nomagic.magicdraw.ui.browser.Tree
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package
import org.omg.oti.magicdraw.uml.read.MagicDrawUMLUtil
import gov.nasa.jpl.dynamicScripts.DynamicScriptsTypes
import gov.nasa.jpl.dynamicScripts.magicdraw.ui.symbols.internal.SymbolHelper._
import gov.nasa.jpl.dynamicScripts.magicdraw.validation.MagicDrawValidationDataResults

import org.omg.oti.uml.read.api._
import org.omg.oti.magicdraw.uml.read.MagicDrawUML
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement
import com.nomagic.magicdraw.uml.symbols.shapes.PackageView
import com.nomagic.magicdraw.uml.symbols.PresentationElement

import scala.collection.JavaConversions._
import scala.collection.immutable._
import scala.util.{Success, Try}
import scala.{Option,None,Some,StringContext,Unit}
import scala.Predef.augmentString

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
    val guiLog = app.getGUILog
    guiLog.clearLog()

    val umlUtil = MagicDrawUMLUtil( p )
    import umlUtil._
    
    val selectedPackages =
      selection
      .toIterable
      .selectByKindOf { case pv: PackageView => umlPackage( getPackageOfView(pv).get ) }
      .to[List]

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

    val selectedPackages =
      selection
      .toIterable
      .selectByKindOf { case p: Package => umlPackage( p ) }
      .to[List]

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