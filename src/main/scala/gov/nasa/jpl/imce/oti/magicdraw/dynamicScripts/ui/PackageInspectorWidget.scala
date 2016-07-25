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
package gov.nasa.jpl.imce.oti.magicdraw.dynamicScripts.ui

import java.awt.event.ActionEvent

import gov.nasa.jpl.imce.oti.magicdraw.dynamicScripts.utils.OTIHelper
import org.omg.oti.uml.RelationTriple
import com.nomagic.magicdraw.core.Project
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package
import gov.nasa.jpl.dynamicScripts.DynamicScriptsTypes
import gov.nasa.jpl.dynamicScripts.magicdraw.designations.MagicDrawElementKindDesignation
import gov.nasa.jpl.dynamicScripts.magicdraw.utils._
import org.omg.oti.magicdraw.uml.canonicalXMI.MagicDrawIDGenerator
import org.omg.oti.magicdraw.uml.canonicalXMI.helper._
import org.omg.oti.uml.read.api._
import org.omg.oti.magicdraw.uml.read.MagicDrawUML

import scala.collection.immutable._
import scala.util.{Failure, Success, Try}

object PackageInspectorWidget {

  import ComputedDerivedWidgetHelper._
  import RelationTripleWidgetHelper._

  def nonImportedNestedPackage
  ( project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedWidget,
    ek: MagicDrawElementKindDesignation, e: Element )
  : Try[(java.awt.Component, Seq[ValidationAnnotation])]
  = OTIHelper.toTry(
    MagicDrawOTIHelper.getOTIMagicDrawInfoForProfileCharacteristics(project),
    (ordsa: MagicDrawOTIResolvedDocumentSetAdapterForProfileProvider) => {
      implicit val idg = MagicDrawIDGenerator()(ordsa.rds.ds)
      elementOperationWidget[UMLPackage[MagicDrawUML], UMLPackageableElement[MagicDrawUML]](
        derived, e,
        _.nonImportedNestedPackages,
        ordsa.otiAdapter.umlOps)
    })

  def allNestedPackages
  ( project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedWidget,
    ek: MagicDrawElementKindDesignation, e: Element )
  : Try[(java.awt.Component, Seq[ValidationAnnotation])]
  = OTIHelper.toTry(
    MagicDrawOTIHelper.getOTIMagicDrawInfoForProfileCharacteristics(project),
    (ordsa: MagicDrawOTIResolvedDocumentSetAdapterForProfileProvider) => {
      implicit val idg = MagicDrawIDGenerator()(ordsa.rds.ds)
      elementOperationWidget[UMLPackage[MagicDrawUML], UMLPackageableElement[MagicDrawUML]](
        derived, e,
        _.allNestedPackages,
        ordsa.otiAdapter.umlOps)
    })

  def allNestingPackagesTransitively
  ( project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedWidget,
    ek: MagicDrawElementKindDesignation, e: Element )
  : Try[(java.awt.Component, Seq[ValidationAnnotation])]
  = OTIHelper.toTry(
    MagicDrawOTIHelper.getOTIMagicDrawInfoForProfileCharacteristics(project),
    (ordsa: MagicDrawOTIResolvedDocumentSetAdapterForProfileProvider) => {
      implicit val idg = MagicDrawIDGenerator()(ordsa.rds.ds)
      elementOperationWidget[UMLPackage[MagicDrawUML], UMLPackageableElement[MagicDrawUML]](
        derived, e,
        _.allNestingPackagesTransitively,
        ordsa.otiAdapter.umlOps)
    })

  def allDirectlyImportedPackagesIncludingNestingPackagesTransitively
  ( project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedWidget,
    ek: MagicDrawElementKindDesignation, e: Element )
  : Try[(java.awt.Component, Seq[ValidationAnnotation])]
  = OTIHelper.toTry(
    MagicDrawOTIHelper.getOTIMagicDrawInfoForProfileCharacteristics(project),
    (ordsa: MagicDrawOTIResolvedDocumentSetAdapterForProfileProvider) => {
      implicit val idg = MagicDrawIDGenerator()(ordsa.rds.ds)
      elementOperationWidget[UMLPackage[MagicDrawUML], UMLPackageableElement[MagicDrawUML]](
        derived, e,
        _.allDirectlyImportedPackagesIncludingNestingPackagesTransitively,
        ordsa.otiAdapter.umlOps)
    })

  def allPackagesWithinScope
  ( project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedWidget,
    ek: MagicDrawElementKindDesignation, e: Element )
  : Try[(java.awt.Component, Seq[ValidationAnnotation])]
  = OTIHelper.toTry(
    MagicDrawOTIHelper.getOTIMagicDrawInfoForProfileCharacteristics(project),
    (ordsa: MagicDrawOTIResolvedDocumentSetAdapterForProfileProvider) => {
      implicit val idg = MagicDrawIDGenerator()(ordsa.rds.ds)
      elementOperationWidget[UMLPackage[MagicDrawUML], UMLPackage[MagicDrawUML]](
        derived, e,
        _.allPackagesWithinScope,
        ordsa.otiAdapter.umlOps)
    })

  def allApplicableStereotypes
  ( project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedWidget,
    ek: MagicDrawElementKindDesignation, e: Element )
  : Try[(java.awt.Component, Seq[ValidationAnnotation])]
  = OTIHelper.toTry(
    MagicDrawOTIHelper.getOTIMagicDrawInfoForProfileCharacteristics(project),
    (ordsa: MagicDrawOTIResolvedDocumentSetAdapterForProfileProvider) => {
      implicit val idg = MagicDrawIDGenerator()(ordsa.rds.ds)
      elementOperationWidget[UMLPackage[MagicDrawUML], UMLPackageableElement[MagicDrawUML]](
        derived, e,
        _.allApplicableStereotypes,
        ordsa.otiAdapter.umlOps)
    })

  def containingProfile
  ( project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedWidget,
    ek: MagicDrawElementKindDesignation, e: Element )
  : Try[(java.awt.Component, Seq[ValidationAnnotation])]
  = OTIHelper.toTry(
    MagicDrawOTIHelper.getOTIMagicDrawInfoForProfileCharacteristics(project),
    (ordsa: MagicDrawOTIResolvedDocumentSetAdapterForProfileProvider) => {
      implicit val idg = MagicDrawIDGenerator()(ordsa.rds.ds)
      elementOperationWidget[UMLPackage[MagicDrawUML], UMLPackageableElement[MagicDrawUML]](
        derived, e,
        _.containingProfile.to[Iterable],
        ordsa.otiAdapter.umlOps)
    })

  def allDirectlyAppliedProfilesExceptNestingPackages
  ( project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedWidget,
    ek: MagicDrawElementKindDesignation, e: Element )
  : Try[(java.awt.Component, Seq[ValidationAnnotation])]
  = OTIHelper.toTry(
    MagicDrawOTIHelper.getOTIMagicDrawInfoForProfileCharacteristics(project),
    (ordsa: MagicDrawOTIResolvedDocumentSetAdapterForProfileProvider) => {
      implicit val idg = MagicDrawIDGenerator()(ordsa.rds.ds)
      elementOperationWidget[UMLPackage[MagicDrawUML], UMLPackageableElement[MagicDrawUML]](
        derived, e,
        _.allDirectlyAppliedProfilesExceptNestingPackages,
        ordsa.otiAdapter.umlOps)
    })

  def allDirectlyAppliedProfilesIncludingNestingPackagesTransitively
  ( project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedWidget,
    ek: MagicDrawElementKindDesignation, e: Element )
  : Try[(java.awt.Component, Seq[ValidationAnnotation])]
  = OTIHelper.toTry(
    MagicDrawOTIHelper.getOTIMagicDrawInfoForProfileCharacteristics(project),
    (ordsa: MagicDrawOTIResolvedDocumentSetAdapterForProfileProvider) => {
      implicit val idg = MagicDrawIDGenerator()(ordsa.rds.ds)
      elementOperationWidget[UMLPackage[MagicDrawUML], UMLPackageableElement[MagicDrawUML]](
        derived, e,
        _.allDirectlyAppliedProfilesIncludingNestingPackagesTransitively,
        ordsa.otiAdapter.umlOps)
    })

  def allDirectlyVisibleMembersTransitivelyAccessibleExceptNestingPackagesAndAppliedProfiles
  ( project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedWidget,
    ek: MagicDrawElementKindDesignation, e: Element )
  : Try[(java.awt.Component, Seq[ValidationAnnotation])]
  = OTIHelper.toTry(
    MagicDrawOTIHelper.getOTIMagicDrawInfoForProfileCharacteristics(project),
    (ordsa: MagicDrawOTIResolvedDocumentSetAdapterForProfileProvider) => {
      implicit val idg = MagicDrawIDGenerator()(ordsa.rds.ds)
      elementOperationWidget[UMLPackage[MagicDrawUML], UMLPackageableElement[MagicDrawUML]](
        derived, e,
        _.allDirectlyVisibleMembersTransitivelyAccessibleExceptNestingPackagesAndAppliedProfiles,
        ordsa.otiAdapter.umlOps)
    })

  def allIndirectlyAppliedProfilesIncludingNestingPackagesTransitively
  ( project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedWidget,
    ek: MagicDrawElementKindDesignation, e: Element )
  : Try[(java.awt.Component, Seq[ValidationAnnotation])]
  = OTIHelper.toTry(
    MagicDrawOTIHelper.getOTIMagicDrawInfoForProfileCharacteristics(project),
    (ordsa: MagicDrawOTIResolvedDocumentSetAdapterForProfileProvider) => {
      implicit val idg = MagicDrawIDGenerator()(ordsa.rds.ds)
      elementOperationWidget[UMLPackage[MagicDrawUML], UMLPackageableElement[MagicDrawUML]](
        derived, e,
        _.allIndirectlyAppliedProfilesIncludingNestingPackagesTransitively,
        ordsa.otiAdapter.umlOps)
    })

  def allForwardReferencesToImportablePackageableElementsFromAllOwnedElementsTransitively
  ( project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedWidget,
    ek: MagicDrawElementKindDesignation, e: Element )
  : Try[(java.awt.Component, Seq[ValidationAnnotation])]
  = OTIHelper.toTry(
    MagicDrawOTIHelper.getOTIMagicDrawInfoForProfileCharacteristics(project),
    (ordsa: MagicDrawOTIResolvedDocumentSetAdapterForProfileProvider) => {
      implicit val idg = MagicDrawIDGenerator()(ordsa.rds.ds)
      elementOperationWidget[UMLPackage[MagicDrawUML], UMLPackageableElement[MagicDrawUML]](
        derived, e,
        _.allForwardReferencesToImportablePackageableElementsFromAllOwnedElementsTransitively.getOrElse(Set[UMLPackageableElement[MagicDrawUML]]()),
        ordsa.otiAdapter.umlOps)
    })

  def allIndirectlyVisibleMembersTransitivelyAccessibleFromNestingPackagesAndAppliedProfiles
  ( project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedWidget,
    ek: MagicDrawElementKindDesignation, e: Element )
  : Try[(java.awt.Component, Seq[ValidationAnnotation])]
  = OTIHelper.toTry(
    MagicDrawOTIHelper.getOTIMagicDrawInfoForProfileCharacteristics(project),
    (ordsa: MagicDrawOTIResolvedDocumentSetAdapterForProfileProvider) => {
      implicit val idg = MagicDrawIDGenerator()(ordsa.rds.ds)
      elementOperationWidget[UMLPackage[MagicDrawUML], UMLPackageableElement[MagicDrawUML]](
        derived, e,
        _.allIndirectlyVisibleMembersTransitivelyAccessibleFromNestingPackagesAndAppliedProfiles,
        ordsa.otiAdapter.umlOps)
    })

  def forwardReferencesToPackagesOrProfiles
  ( project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedWidget,
    ek: MagicDrawElementKindDesignation, e: Element )
  : Try[(java.awt.Component, Seq[ValidationAnnotation])]
  = OTIHelper.toTry(
    MagicDrawOTIHelper.getOTIMagicDrawInfoForProfileCharacteristics(project),
    (ordsa: MagicDrawOTIResolvedDocumentSetAdapterForProfileProvider) => {
      implicit val idg = MagicDrawIDGenerator()(ordsa.rds.ds)
      elementOperationWidget[UMLPackage[MagicDrawUML], UMLPackageableElement[MagicDrawUML]](
        derived, e,
        _.forwardReferencesToPackagesOrProfiles.getOrElse(Set[UMLPackage[MagicDrawUML]]()),
        ordsa.otiAdapter.umlOps)
    })

  def forwardReferencesBeyondPackageScope
  ( project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedWidget,
    ek: MagicDrawElementKindDesignation, e: Element )
  : Try[( java.awt.Component, Seq[ValidationAnnotation] )]
  = OTIHelper.toTry(
    MagicDrawOTIHelper.getOTIMagicDrawInfoForProfileCharacteristics(project),
    (ordsa: MagicDrawOTIResolvedDocumentSetAdapterForProfileProvider) => {
      implicit val idg = MagicDrawIDGenerator()(ordsa.rds.ds)
      e match {
        case p: Package =>
          packageRelationTripleWidget(
            derived, p,
            (x: UMLPackage[MagicDrawUML]) => {
              Success(x.forwardReferencesBeyondPackageScope.getOrElse(Set[RelationTriple[MagicDrawUML]]()))
            },
            ordsa.otiAdapter.umlOps)
        case _ =>
          Failure(new java.lang.IllegalArgumentException("Not a package!"))
      }
    })

}