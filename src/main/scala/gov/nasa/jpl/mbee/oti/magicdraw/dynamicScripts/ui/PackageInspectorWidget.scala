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
package gov.nasa.jpl.mbee.oti.magicdraw.dynamicScripts.ui

import java.awt.event.ActionEvent
import java.awt.event.InputEvent
import javax.swing.JOptionPane
import scala.collection.JavaConversions._
import scala.language.postfixOps
import scala.util.Failure
import scala.util.Success
import scala.util.Try
import com.nomagic.magicdraw.core.Project
import com.nomagic.magicdraw.ui.dialogs.specifications.SpecificationDialogManager
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package
import gov.nasa.jpl.dynamicScripts.DynamicScriptsTypes
import gov.nasa.jpl.dynamicScripts.magicdraw.DynamicScriptsPlugin
import gov.nasa.jpl.dynamicScripts.magicdraw.designations.MagicDrawElementKindDesignation
import gov.nasa.jpl.dynamicScripts.magicdraw.specificationDialog.SpecificationComputedComponent
import gov.nasa.jpl.dynamicScripts.magicdraw.ui.nodes._
import gov.nasa.jpl.dynamicScripts.magicdraw.utils._

import org.omg.oti.uml.read.api._
import org.omg.oti.magicdraw.uml.read.MagicDrawUML
import org.omg.oti.magicdraw.uml.read.MagicDrawUMLUtil

object PackageInspectorWidget {

  import ComputedDerivedWidgetHelper._
  import RelationTripleWidgetHelper._

  def nonImportedNestedPackages(
    project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedWidget,
    ek: MagicDrawElementKindDesignation, e: Element ): Try[( java.awt.Component, Seq[ValidationAnnotation] )] =
    elementOperationWidget[UMLPackage[MagicDrawUML], UMLPackageableElement[MagicDrawUML]](
      derived, e,
      _.nonImportedNestedPackages,
      MagicDrawUMLUtil( project ) )

  def allNestedPackages(
    project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedWidget,
    ek: MagicDrawElementKindDesignation, e: Element ): Try[( java.awt.Component, Seq[ValidationAnnotation] )] =
    elementOperationWidget[UMLPackage[MagicDrawUML], UMLPackageableElement[MagicDrawUML]](
      derived, e,
      _.allNestedPackages,
      MagicDrawUMLUtil( project ) )

  def allNestingPackagesTransitively(
    project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedWidget,
    ek: MagicDrawElementKindDesignation, e: Element ): Try[( java.awt.Component, Seq[ValidationAnnotation] )] =
    elementOperationWidget[UMLPackage[MagicDrawUML], UMLPackageableElement[MagicDrawUML]](
      derived, e,
      _.allNestingPackagesTransitively,
      MagicDrawUMLUtil( project ) )

  def allDirectlyImportedPackagesIncludingNestingPackagesTransitively(
    project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedWidget,
    ek: MagicDrawElementKindDesignation, e: Element ): Try[( java.awt.Component, Seq[ValidationAnnotation] )] =
    elementOperationWidget[UMLPackage[MagicDrawUML], UMLPackageableElement[MagicDrawUML]](
      derived, e,
      _.allDirectlyImportedPackagesIncludingNestingPackagesTransitively,
      MagicDrawUMLUtil( project ) )

  def allPackagesWithinScope(
    project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedWidget,
    ek: MagicDrawElementKindDesignation, e: Element ): Try[( java.awt.Component, Seq[ValidationAnnotation] )] =
    elementOperationWidget[UMLPackage[MagicDrawUML], UMLPackage[MagicDrawUML]](
      derived, e,
      _.allPackagesWithinScope,
      MagicDrawUMLUtil( project ) )
      
  def allApplicableStereotypes(
    project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedWidget,
    ek: MagicDrawElementKindDesignation, e: Element ): Try[( java.awt.Component, Seq[ValidationAnnotation] )] =
    elementOperationWidget[UMLPackage[MagicDrawUML], UMLPackageableElement[MagicDrawUML]](
      derived, e,
      _.allApplicableStereotypes,
      MagicDrawUMLUtil( project ) )

  def containingProfile(
    project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedWidget,
    ek: MagicDrawElementKindDesignation, e: Element ): Try[( java.awt.Component, Seq[ValidationAnnotation] )] =
    elementOperationWidget[UMLPackage[MagicDrawUML], UMLPackageableElement[MagicDrawUML]](
      derived, e,
      _.containingProfile,
      MagicDrawUMLUtil( project ) )

  def allDirectlyAppliedProfilesExceptNestingPackages(
    project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedWidget,
    ek: MagicDrawElementKindDesignation, e: Element ): Try[( java.awt.Component, Seq[ValidationAnnotation] )] =
    elementOperationWidget[UMLPackage[MagicDrawUML], UMLPackageableElement[MagicDrawUML]](
      derived, e,
      _.allDirectlyAppliedProfilesExceptNestingPackages,
      MagicDrawUMLUtil( project ) )

  def allDirectlyAppliedProfilesIncludingNestingPackagesTransitively(
    project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedWidget,
    ek: MagicDrawElementKindDesignation, e: Element ): Try[( java.awt.Component, Seq[ValidationAnnotation] )] =
    elementOperationWidget[UMLPackage[MagicDrawUML], UMLPackageableElement[MagicDrawUML]](
      derived, e,
      _.allDirectlyAppliedProfilesIncludingNestingPackagesTransitively,
      MagicDrawUMLUtil( project ) )

  def allDirectlyVisibleMembersTransitivelyAccessibleExceptNestingPackagesAndAppliedProfiles(
    project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedWidget,
    ek: MagicDrawElementKindDesignation, e: Element ): Try[( java.awt.Component, Seq[ValidationAnnotation] )] =
    elementOperationWidget[UMLPackage[MagicDrawUML], UMLPackageableElement[MagicDrawUML]](
      derived, e,
      _.allDirectlyVisibleMembersTransitivelyAccessibleExceptNestingPackagesAndAppliedProfiles,
      MagicDrawUMLUtil( project ) )

  def allIndirectlyAppliedProfilesIncludingNestingPackagesTransitively(
    project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedWidget,
    ek: MagicDrawElementKindDesignation, e: Element ): Try[( java.awt.Component, Seq[ValidationAnnotation] )] =
    elementOperationWidget[UMLPackage[MagicDrawUML], UMLPackageableElement[MagicDrawUML]](
      derived, e,
      _.allIndirectlyAppliedProfilesIncludingNestingPackagesTransitively,
      MagicDrawUMLUtil( project ) )

  def allForwardReferencesToImportablePackageableElementsFromAllOwnedElementsTransitively(
    project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedWidget,
    ek: MagicDrawElementKindDesignation, e: Element ): Try[( java.awt.Component, Seq[ValidationAnnotation] )] =
    elementOperationWidget[UMLPackage[MagicDrawUML], UMLPackageableElement[MagicDrawUML]](
      derived, e,
      _.allForwardReferencesToImportablePackageableElementsFromAllOwnedElementsTransitively,
      MagicDrawUMLUtil( project ) )

  def allIndirectlyVisibleMembersTransitivelyAccessibleFromNestingPackagesAndAppliedProfiles(
    project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedWidget,
    ek: MagicDrawElementKindDesignation, e: Element ): Try[( java.awt.Component, Seq[ValidationAnnotation] )] =
    elementOperationWidget[UMLPackage[MagicDrawUML], UMLPackageableElement[MagicDrawUML]](
      derived, e,
      _.allIndirectlyVisibleMembersTransitivelyAccessibleFromNestingPackagesAndAppliedProfiles,
      MagicDrawUMLUtil( project ) )

  def forwardReferencesToPackagesOrProfiles(
    project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedWidget,
    ek: MagicDrawElementKindDesignation, e: Element ): Try[( java.awt.Component, Seq[ValidationAnnotation] )] =
    elementOperationWidget[UMLPackage[MagicDrawUML], UMLPackageableElement[MagicDrawUML]](
      derived, e,
      _.forwardReferencesToPackagesOrProfiles.get,
      MagicDrawUMLUtil( project ) )

  def forwardReferencesBeyondPackageScope(
    project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedWidget,
    ek: MagicDrawElementKindDesignation, e: Element ): Try[( java.awt.Component, Seq[ValidationAnnotation] )] =
    e match {
      case p: Package =>
        packageRelationTripleWidget(
          derived, p,
          _.forwardReferencesBeyondPackageScope,
          MagicDrawUMLUtil( project ) )
      case _ =>
        Failure( new IllegalArgumentException( "Not a package!" ) )
    }
}