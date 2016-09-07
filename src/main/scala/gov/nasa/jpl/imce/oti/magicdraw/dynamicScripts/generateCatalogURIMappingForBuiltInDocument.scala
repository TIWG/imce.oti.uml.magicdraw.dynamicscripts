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

import java.awt.event.ActionEvent

import com.nomagic.magicdraw.core.{Application, Project}
import com.nomagic.magicdraw.ui.browser.{Node, Tree}
import com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdmodels.Model
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.{Element, Package}
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Profile
import gov.nasa.jpl.dynamicScripts.DynamicScriptsTypes
import gov.nasa.jpl.dynamicScripts.magicdraw.validation.MagicDrawValidationDataResults
import org.omg.oti.magicdraw.uml.read.MagicDrawUMLUtil

import scala.util.{Success, Try}
import scala.{Option,None}
/**
 * @author Nicolas.F.Rouquette@jpl.nasa.gov
 */
object generateCatalogURIMappingForBuiltInDocument {

  def doit
  ( project: Project,
    ev: ActionEvent,
    script: DynamicScriptsTypes.BrowserContextMenuAction,
    tree: Tree,
    node: Node,
    pkg: Profile,
    selection: java.util.Collection[Element] )
  : Try[Option[MagicDrawValidationDataResults]] =
    doit( project, ev, script, tree, node, pkg.asInstanceOf[Package], selection )

  def doit
  ( project: Project,
    ev: ActionEvent,
    script: DynamicScriptsTypes.BrowserContextMenuAction,
    tree: Tree,
    node: Node,
    pkg: Model,
    selection: java.util.Collection[Element] )
  : Try[Option[MagicDrawValidationDataResults]] =
    doit( project, ev, script, tree, node, pkg.asInstanceOf[Package], selection )

  def doit
  ( p: Project,
    ev: ActionEvent,
    script: DynamicScriptsTypes.BrowserContextMenuAction,
    tree: Tree,
    node: Node,
    top: Package,
    selection: java.util.Collection[Element] )
  : Try[Option[MagicDrawValidationDataResults]] = {

    val a = Application.getInstance()
    val guiLog = a.getGUILog()
    guiLog.clearLog()

    val umlUtil = MagicDrawUMLUtil( p )

    import umlUtil._

    val pkg = umlPackage( top )

//    val (pkgURI, otiURI) = pkg.URI match {
//      case None =>
//        return Failure( new IllegalArgumentException( "Package should have a URI!" ) )
//      case Some( pURI ) =>
//        pURI match {
//          case "http://www.omg.org/spec/PrimitiveTypes/20100901" =>
//            (pURI, MDBuiltInPrimitiveTypes.documentURL.toString)
//          case "http://www.omg.org/spec/UML/20131001" =>
//            (pURI, MDBuiltInUML.documentURL.toString)
//          case "http://www.omg.org/spec/UML/20131001/StandardProfile" =>
//            (pURI, MDBuiltInStandardProfile.documentURL.toString)
//          case x =>
//              return Failure( new IllegalArgumentException( s"Unrecognized package with built-in URI: $x" ) )
//        }
//    }
//
//    val rewrites =
//      pkg.ownedType.flatMap {
//        case pt: UMLPrimitiveType[Uml] =>
//          Iterable( s"""<uri uri="$otiURI#${pt.name.get}" name="$otiURI#${pt.toolSpecific_id}"/>""" )
//        case s: UMLStereotype[Uml] =>
//          Iterable( s"""<uri uri="$otiURI#${s.name.get}" name="$otiURI#${s.toolSpecific_id}"/>""" ) ++
//          (for {
//            baseProperty <- s.baseMetaPropertiesExceptRedefined
//            baseID = s"${s.name.get}_${baseProperty.name.get}"
//          } yield s"""<uri uri="$otiURI#$baseID" name="$otiURI#${baseProperty.toolSpecific_id}"/>""")
//
//        case ex: UMLExtension[Uml] =>
//          val ee = ex.ownedEnd.head
//          val end = ee._type.head.name.get
//          val base = ex.metaclass.head.name.get
//          val oti_extensionID = base+"_"+end
//          val oti_endID = oti_extensionID+"-extension_"+end
//          Iterable(
//              s"""<uri uri="$otiURI#$oti_extensionID" name="$otiURI#${ex.toolSpecific_id}"/>""",
//              s"""<uri uri="$otiURI#$oti_endID" name="$otiURI#${ee.toolSpecific_id}"/>"""
//              )
//
//        case c: UMLClass[Uml] =>
//          Iterable( s"""<uri uri="$otiURI#${c.name.get}" name="$otiURI#${c.toolSpecific_id}"/>""" )
//
//        case _ =>
//          Iterable()
//      }
//
//    val builtInRewrites = Iterable( s"""<uri uri="$otiURI#_0" name="$otiURI#${pkg.toolSpecific_id}"/>""" ) ++ rewrites
//
//    builtInRewrites.foreach { rewrite => System.out.println(rewrite) }
//
    Success( None )
  }
}