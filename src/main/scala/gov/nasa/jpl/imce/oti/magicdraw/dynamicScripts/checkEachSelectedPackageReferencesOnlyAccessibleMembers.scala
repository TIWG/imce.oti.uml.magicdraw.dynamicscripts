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