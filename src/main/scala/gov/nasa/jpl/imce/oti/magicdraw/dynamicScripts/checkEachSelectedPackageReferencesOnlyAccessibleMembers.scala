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
package gov.nasa.jpl.imce.oti.magicdraw.dynamicScripts

//import java.awt.event.ActionEvent

//import com.nomagic.magicdraw.core.{Application, Project}
//import com.nomagic.magicdraw.uml.symbols.{DiagramPresentationElement, PresentationElement}
//import com.nomagic.magicdraw.uml.symbols.shapes.PackageView
//import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package
//import com.nomagic.uml2.ext.magicdraw.mdprofiles.Profile
//import gov.nasa.jpl.dynamicScripts.DynamicScriptsTypes
//import gov.nasa.jpl.dynamicScripts.magicdraw.validation.MagicDrawValidationDataResults
//import gov.nasa.jpl.imce.oti.magicdraw.dynamicScripts.validation.OTIMagicDrawValidation
//import org.omg.oti.uml.read.api._
//import org.omg.oti.magicdraw.uml.read.{MagicDrawUML, MagicDrawUMLUtil}
//import org.omg.oti.uml.validation._

//import scala.collection.JavaConversions._
//import scala.language.{implicitConversions, postfixOps}
//import scala.util.{Success, Try}

/**
 * @author Nicolas.F.Rouquette@jpl.nasa.gov
 */
object checkEachSelectedPackageReferencesOnlyAccessibleMembers {

//  def doitExceptNestingPackagesAndAppliedProfiles
//  ( p: Project,
//    ev: ActionEvent,
//    script: DynamicScriptsTypes.DiagramContextMenuAction,
//    dpe: DiagramPresentationElement,
//    triggerView: PackageView,
//    triggerElement: Profile,
//    selection: java.util.Collection[PresentationElement] )
//  : Try[Option[MagicDrawValidationDataResults]] = {
//
//    implicit val umlUtil = MagicDrawUMLUtil( p )
//    import umlUtil._
//
//    checkEachSelectedPackageReferencesOnlyAccessibleMembersExceptNestingPackagesAndAppliedProfiles(
//      p,
//      selection.toSet selectByKindOf { case pv: PackageView => umlPackage( pv.getPackage ) } )
//  }
  
//  def doitIncludingNestingPackagesAndAppliedProfiles
//  ( p: Project,
//    ev: ActionEvent,
//    script: DynamicScriptsTypes.DiagramContextMenuAction,
//    dpe: DiagramPresentationElement,
//    triggerView: PackageView,
//    triggerElement: Profile,
//    selection: java.util.Collection[PresentationElement] )
//  : Try[Option[MagicDrawValidationDataResults]] = {
//
//    implicit val umlUtil = MagicDrawUMLUtil( p )
//    import umlUtil._
//
//    checkEachSelectedPackageReferencesOnlyAccessibleMembersIncludingNestingPackagesAndAppliedProfiles(
//      p,
//      selection.toSet selectByKindOf { case pv: PackageView => umlPackage( pv.getPackage ) } )
//  }

//  def doitExceptNestingPackagesAndAppliedProfiles
//  ( p: Project,
//    ev: ActionEvent,
//    script: DynamicScriptsTypes.DiagramContextMenuAction,
//    dpe: DiagramPresentationElement,
//    triggerView: PackageView,
//    triggerElement: Package,
//    selection: java.util.Collection[PresentationElement] )
//  : Try[Option[MagicDrawValidationDataResults]] = {
//
//    implicit val umlUtil = MagicDrawUMLUtil( p )
//    import umlUtil._
//
//    checkEachSelectedPackageReferencesOnlyAccessibleMembersExceptNestingPackagesAndAppliedProfiles(
//      p,
//      selection.toSet selectByKindOf { case pv: PackageView => umlPackage( pv.getPackage ) } )
//  }

//  def doitIncludingNestingPackagesAndAppliedProfiles
//  ( p: Project,
//    ev: ActionEvent,
//    script: DynamicScriptsTypes.DiagramContextMenuAction,
//    dpe: DiagramPresentationElement,
//    triggerView: PackageView,
//    triggerElement: Package,
//    selection: java.util.Collection[PresentationElement] )
//  : Try[Option[MagicDrawValidationDataResults]] = {
//
//    implicit val umlUtil = MagicDrawUMLUtil( p )
//    import umlUtil._
//
//    checkEachSelectedPackageReferencesOnlyAccessibleMembersIncludingNestingPackagesAndAppliedProfiles(
//      p,
//      selection.toSet selectByKindOf { case pv: PackageView => umlPackage( pv.getPackage ) } )
//  }

//  def checkEachSelectedPackageReferencesOnlyAccessibleMembersExceptNestingPackagesAndAppliedProfiles
//  ( p: Project,
//    pkgs: Iterable[UMLPackage[MagicDrawUML]] )
//  ( implicit _umlUtil: MagicDrawUMLUtil )
//  : Try[Option[MagicDrawValidationDataResults]] = {
//
//    import _umlUtil._
//    val app = Application.getInstance
//    val guiLog = app.getGUILog
//    guiLog.clearLog()
//
//    val otiV = OTIMagicDrawValidation(p)
//
//    val rules = new UMLPackageableElementRules[Uml, MagicDrawUMLUtil] {
//      implicit val umlOps = _umlUtil
//    }
//
//    implicit val referencedButNotAccessibleValidationConstructor =
//      rules.defaultReferencedButNotAccessibleConstructor _
//
//    val elementMessages = for {
//      pkg <- pkgs
//      _ = guiLog.log( s"Analyzing ${pkg.qualifiedName.get}" )
//      mdPkg = umlMagicDrawUMLPackage(pkg).getMagicDrawPackage
//      as = List( actions.SelectInContainmentTreeAction( mdPkg ) )
//      violation <- rules.
//        findNonAccessibleButReferencedImportablePackabeableElementsExceptNestingPackagesAndAppliedProfiles( pkg )
//      vInfo = otiV.constructValidationInfo(
//        otiV.MD_OTI_ValidationConstraint_UnresolvedCrossReference,
//        Some(s"unaccessible cross-reference from ${pkg.qualifiedName.get}"),
//        Nil).get
//    } yield
//      umlMagicDrawUMLPackageableElement(violation.referencedButNotAccessible).getMagicDrawElement -> List(vInfo)
//
//    otiV.makeMDIllegalArgumentExceptionValidation(
//      "Validate each package references only accessible members (excluding nesting packages & applied profiles)",
//      elementMessages.toMap)
//  }
  
//  def checkEachSelectedPackageReferencesOnlyAccessibleMembersIncludingNestingPackagesAndAppliedProfiles
//  ( p: Project,
//    pkgs: Iterable[UMLPackage[MagicDrawUML]] )
//  ( implicit _umlUtil: MagicDrawUMLUtil )
//  : Try[Option[MagicDrawValidationDataResults]] = {
//
//    import _umlUtil._
//    val app = Application.getInstance
//    val guiLog = app.getGUILog
//    guiLog.clearLog()
//
//    val otiV = OTIMagicDrawValidation(p)
//
//    val rules = new UMLPackageableElementRules[Uml, MagicDrawUMLUtil] {
//      implicit val umlOps = _umlUtil
//    }
//
//    implicit val referencedButNotAccessibleValidationConstructor =
//      rules.defaultReferencedButNotAccessibleConstructor _
//
//    val elementMessages = for {
//      pkg <- pkgs
//      _ = guiLog.log( s"Analyzing ${pkg.qualifiedName.get}" )
//      as = List( actions.SelectInContainmentTreeAction( umlMagicDrawUMLPackage(pkg).getMagicDrawPackage ) )
//      violation <- rules.
//        findNonAccessibleButReferencedImportablePackabeableElementsIncludingNestingPackagesAndAppliedProfiles( pkg )
//      vInfo = otiV.constructValidationInfo(
//        otiV.MD_OTI_ValidationConstraint_UnresolvedCrossReference,
//        Some(s"unaccessible cross-reference from ${pkg.qualifiedName.get}"),
//        Nil).get
//    } yield
//      umlMagicDrawUMLPackageableElement(violation.referencedButNotAccessible).getMagicDrawElement -> List(vInfo)
//
//    otiV.makeMDIllegalArgumentExceptionValidation(
//        "Validate each package references only accessible members (including nesting packages & applied profiles)",
//        elementMessages.toMap)
//  }
}