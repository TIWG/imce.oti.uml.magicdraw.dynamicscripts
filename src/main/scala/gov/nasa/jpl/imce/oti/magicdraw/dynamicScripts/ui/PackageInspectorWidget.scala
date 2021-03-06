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
    MagicDrawOTIHelper.getOTIMagicDrawInfoForDataCharacteristics(project),
    (ordsa: MagicDrawOTIResolvedDocumentSetAdapterForDataProvider) => {
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
    MagicDrawOTIHelper.getOTIMagicDrawInfoForDataCharacteristics(project),
    (ordsa: MagicDrawOTIResolvedDocumentSetAdapterForDataProvider) => {
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
    MagicDrawOTIHelper.getOTIMagicDrawInfoForDataCharacteristics(project),
    (ordsa: MagicDrawOTIResolvedDocumentSetAdapterForDataProvider) => {
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
    MagicDrawOTIHelper.getOTIMagicDrawInfoForDataCharacteristics(project),
    (ordsa: MagicDrawOTIResolvedDocumentSetAdapterForDataProvider) => {
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
    MagicDrawOTIHelper.getOTIMagicDrawInfoForDataCharacteristics(project),
    (ordsa: MagicDrawOTIResolvedDocumentSetAdapterForDataProvider) => {
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
    MagicDrawOTIHelper.getOTIMagicDrawInfoForDataCharacteristics(project),
    (ordsa: MagicDrawOTIResolvedDocumentSetAdapterForDataProvider) => {
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
    MagicDrawOTIHelper.getOTIMagicDrawInfoForDataCharacteristics(project),
    (ordsa: MagicDrawOTIResolvedDocumentSetAdapterForDataProvider) => {
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
    MagicDrawOTIHelper.getOTIMagicDrawInfoForDataCharacteristics(project),
    (ordsa: MagicDrawOTIResolvedDocumentSetAdapterForDataProvider) => {
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
    MagicDrawOTIHelper.getOTIMagicDrawInfoForDataCharacteristics(project),
    (ordsa: MagicDrawOTIResolvedDocumentSetAdapterForDataProvider) => {
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
    MagicDrawOTIHelper.getOTIMagicDrawInfoForDataCharacteristics(project),
    (ordsa: MagicDrawOTIResolvedDocumentSetAdapterForDataProvider) => {
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
    MagicDrawOTIHelper.getOTIMagicDrawInfoForDataCharacteristics(project),
    (ordsa: MagicDrawOTIResolvedDocumentSetAdapterForDataProvider) => {
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
    MagicDrawOTIHelper.getOTIMagicDrawInfoForDataCharacteristics(project),
    (ordsa: MagicDrawOTIResolvedDocumentSetAdapterForDataProvider) => {
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
    MagicDrawOTIHelper.getOTIMagicDrawInfoForDataCharacteristics(project),
    (ordsa: MagicDrawOTIResolvedDocumentSetAdapterForDataProvider) => {
      implicit val idg = MagicDrawIDGenerator()(ordsa.rds.ds)
      elementOperationWidget[UMLPackage[MagicDrawUML], UMLPackageableElement[MagicDrawUML]](
        derived, e,
        _.allIndirectlyVisibleMembersTransitivelyAccessibleFromNestingPackagesAndAppliedProfiles,
        ordsa.otiAdapter.umlOps)
    })

  def allStereotypedElements
  ( project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedWidget,
    ek: MagicDrawElementKindDesignation, e: Element )
  : Try[(java.awt.Component, Seq[ValidationAnnotation])]
  = OTIHelper.toTry(
    MagicDrawOTIHelper.getOTIMagicDrawInfoForDataCharacteristics(project),
    (ordsa: MagicDrawOTIResolvedDocumentSetAdapterForDataProvider) => {
      implicit val idg = MagicDrawIDGenerator()(ordsa.rds.ds)
      implicit val util = ordsa.otiAdapter.umlOps
      val otiE = util.umlElement(e)
      otiE.allOwnedElements
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
    MagicDrawOTIHelper.getOTIMagicDrawInfoForDataCharacteristics(project),
    (ordsa: MagicDrawOTIResolvedDocumentSetAdapterForDataProvider) => {
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
    MagicDrawOTIHelper.getOTIMagicDrawInfoForDataCharacteristics(project),
    (ordsa: MagicDrawOTIResolvedDocumentSetAdapterForDataProvider) => {
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