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

import scala.Iterable
import scala.language.implicitConversions
import scala.language.postfixOps
import scala.util.Failure
import scala.util.Success
import scala.util.Try

import com.nomagic.magicdraw.core.Application
import com.nomagic.magicdraw.core.Project
import com.nomagic.magicdraw.ui.browser.Node
import com.nomagic.magicdraw.ui.browser.Tree
import com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdmodels.Model
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Profile

import org.omg.oti.api._
import org.omg.oti.magicdraw.MagicDrawUMLUtil

import gov.nasa.jpl.dynamicScripts.DynamicScriptsTypes
import gov.nasa.jpl.dynamicScripts.magicdraw.MagicDrawValidationDataResults

/**
 * @author Nicolas.F.Rouquette@jpl.nasa.gov
 */
object generateCatalogURIMappingForBuiltInDocument {

  def doit(
    project: Project,
    ev: ActionEvent,
    script: DynamicScriptsTypes.BrowserContextMenuAction,
    tree: Tree,
    node: Node,
    pkg: Profile,
    selection: java.util.Collection[Element] ): Try[Option[MagicDrawValidationDataResults]] =
    doit( project, ev, script, tree, node, pkg.asInstanceOf[Package], selection )

  def doit(
    project: Project,
    ev: ActionEvent,
    script: DynamicScriptsTypes.BrowserContextMenuAction,
    tree: Tree,
    node: Node,
    pkg: Model,
    selection: java.util.Collection[Element] ): Try[Option[MagicDrawValidationDataResults]] =
    doit( project, ev, script, tree, node, pkg.asInstanceOf[Package], selection )

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

    val pkg = umlPackage( top )

    val (pkgURI, otiURI) = pkg.URI match {
      case None => 
        return Failure( new IllegalArgumentException( "Package should have a URI!" ) )
      case Some( pURI ) =>
        pURI match {
          case "http://www.omg.org/spec/PrimitiveTypes/20100901" => 
            (pURI, MDBuiltInPrimitiveTypes.documentURL.toString)
          case "http://www.omg.org/spec/UML/20131001" => 
            (pURI, MDBuiltInUML.documentURL.toString)
          case "http://www.omg.org/spec/UML/20131001/StandardProfile" => 
            (pURI, MDBuiltInStandardProfile.documentURL.toString)
          case x => 
              return Failure( new IllegalArgumentException( s"Unrecognized package with built-in URI: ${x}" ) )
        }
    }

    val rewrites =
      pkg.ownedType flatMap { t =>
      t match {
        case pt: UMLPrimitiveType[Uml] => 
          Iterable( s"""<uri uri="${otiURI}#${pt.name.get}" name="${otiURI}#${pt.id}"/>""" )
        case s: UMLStereotype[Uml] => 
          Iterable( s"""<uri uri="${otiURI}#${s.name.get}" name="${otiURI}#${s.id}"/>""" ) ++          
          (for { 
            baseProperty <- s.baseMetaPropertiesExceptRedefined
            baseID = s"${s.name.get}_${baseProperty.name.get}"
          } yield s"""<uri uri="${otiURI}#${baseID}" name="${otiURI}#${baseProperty.id}"/>""")
          
        case ex: UMLExtension[Uml] => 
          val ee = ex.ownedEnd.head
          val end = ee._type.head.name.get
          val base = ex.metaclass.head.name.get
          val oti_extensionID = base+"_"+end
          val oti_endID = oti_extensionID+"-extension_"+end
          Iterable( 
              s"""<uri uri="${otiURI}#${oti_extensionID}" name="${otiURI}#${ex.id}"/>""",
              s"""<uri uri="${otiURI}#${oti_endID}" name="${otiURI}#${ee.id}"/>""" 
              )
          
        case c: UMLClass[Uml] => 
          Iterable( s"""<uri uri="${otiURI}#${c.name.get}" name="${otiURI}#${c.id}"/>""" )
          
        case _ =>
          Iterable()
      }

    }

    val builtInRewrites = Iterable( s"""<uri uri="${otiURI}#_0" name="${otiURI}#${pkg.id}"/>""" ) ++ rewrites
          
    builtInRewrites.foreach { rewrite => System.out.println(rewrite) }
    
    Success( None )
  }
}