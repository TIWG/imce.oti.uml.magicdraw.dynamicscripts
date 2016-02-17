/*
 *
 * License Terms
 *
 * Copyright (c) 2014-2016, California Institute of Technology ("Caltech").
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
package gov.nasa.jpl.imce.oti.magicdraw.dynamicScripts.validation

import java.awt.event.ActionEvent

import com.nomagic.magicdraw.core.Project
import com.nomagic.magicdraw.ui.browser.{Node, Tree}
import com.nomagic.magicdraw.uml.symbols.shapes.PackageView
import com.nomagic.magicdraw.uml.symbols.{DiagramPresentationElement, PresentationElement}
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.{Element, Package}
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Profile
import gov.nasa.jpl.dynamicScripts.DynamicScriptsTypes
import gov.nasa.jpl.dynamicScripts.magicdraw.ui.symbols.internal.SymbolHelper._
import gov.nasa.jpl.dynamicScripts.magicdraw.validation.MagicDrawValidationDataResults
import gov.nasa.jpl.dynamicScripts.magicdraw.validation.internal.MDValidationAPIHelper._
import org.omg.oti.magicdraw.uml.read._
import org.omg.oti.uml.read.api._

import scala.collection.JavaConversions._
import scala.collection.immutable._
import scala.language.{implicitConversions, postfixOps}
import scala.util.{Success, Try}
import scala.{Option,None}

object MOFAllValidation {

  def doit
  ( p: Project, ev: ActionEvent,
    script: DynamicScriptsTypes.BrowserContextMenuAction,
    tree: Tree, node: Node,
    pkg: Profile, selection: java.util.Collection[Element] )
  : Try[Option[MagicDrawValidationDataResults]] = {

    implicit val umlUtil = MagicDrawUMLUtil( p )
    import umlUtil._

    val selectedPackages: Set[UMLPackage[Uml]] =
      selection
      .toIterable
      .selectByKindOf { case p: Package => umlPackage( p ) }
      .to[Set]

    doit(p, selectedPackages)
  }

  def doit
  ( p: Project, ev: ActionEvent,
    script: DynamicScriptsTypes.BrowserContextMenuAction,
    tree: Tree, node: Node,
    top: Package, selection: java.util.Collection[Element] )
  : Try[Option[MagicDrawValidationDataResults]] = {

    implicit val umlUtil = MagicDrawUMLUtil( p )
    import umlUtil._

    val selectedPackages: Set[UMLPackage[Uml]] =
      selection
      .toIterable
      .selectByKindOf { case p: Package => umlPackage( p ) }
      .to[Set]

    doit(p, selectedPackages)
  }

  def doit
  ( p: Project,
    ev: ActionEvent,
    script: DynamicScriptsTypes.DiagramContextMenuAction,
    dpe: DiagramPresentationElement,
    triggerView: PackageView,
    triggerElement: Profile,
    selection: java.util.Collection[PresentationElement] )
  : Try[Option[MagicDrawValidationDataResults]] = {

    implicit val umlUtil = MagicDrawUMLUtil( p )
    import umlUtil._

    doit(
      p,
      selection.toSet selectByKindOf { case pv: PackageView => umlPackage( getPackageOfView(pv).get ) } )
  }

  def doit(
    p: Project,
    ev: ActionEvent,
    script: DynamicScriptsTypes.DiagramContextMenuAction,
    dpe: DiagramPresentationElement,
    triggerView: PackageView,
    triggerElement: Package,
    selection: java.util.Collection[PresentationElement] ): Try[Option[MagicDrawValidationDataResults]] = {

    implicit val umlUtil = MagicDrawUMLUtil( p )
    import umlUtil._

    doit(
      p,
      selection.toSet selectByKindOf { case pv: PackageView => umlPackage( getPackageOfView(pv).get ) } )
  }

  def doit
  ( p: Project,
    pkgs: Iterable[UMLPackage[MagicDrawUML]] )
  ( implicit _umlUtil: MagicDrawUMLUtil )
  : Try[Option[MagicDrawValidationDataResults]] = {

    for {
      vo <- MOFDefaultValueValidation.doit(p, pkgs)
      v <- vo
    } p.showMDValidationDataResults(v)

    for {
      vo <- MOFMultiplicityValidation.doit(p, pkgs)
      v <- vo
    } p.showMDValidationDataResults(v)

    for {
      vo <- MOFNamedElementValidation.doit(p, pkgs)
      v <- vo
    } p.showMDValidationDataResults(v)

    for {
      vo <- MOFTypedElementValidation.doit(p, pkgs)
      v <- vo
    } p.showMDValidationDataResults(v)

    for {
      vo <- MOFVisibilityValidation.doit(p, pkgs)
      v <- vo
    } p.showMDValidationDataResults(v)

    Success(None)

  }
}