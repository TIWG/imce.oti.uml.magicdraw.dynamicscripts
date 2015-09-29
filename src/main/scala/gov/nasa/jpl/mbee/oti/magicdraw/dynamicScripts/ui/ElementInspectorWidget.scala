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

import com.nomagic.magicdraw.core.Project
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element
import gov.nasa.jpl.dynamicScripts.DynamicScriptsTypes
import gov.nasa.jpl.dynamicScripts.magicdraw.designations.MagicDrawElementKindDesignation
import gov.nasa.jpl.dynamicScripts.magicdraw.utils._
import org.omg.oti.uml._
import org.omg.oti.uml.read.api._
import org.omg.oti.magicdraw.uml.read.{MagicDrawUML, MagicDrawUMLUtil}
import org.omg.oti.uml.xmi.IDGenerator

import scala.language.postfixOps
import scala.util.Try

object ElementInspectorWidget {

  import AppliedStereotypeWidgetHelper._
  import ComputedDerivedWidgetHelper._

  def appliedStereotypes
  (project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedWidget,
   ek: MagicDrawElementKindDesignation, e: Element)
  ( implicit idg: IDGenerator[MagicDrawUML])
  : Try[(java.awt.Component, Seq[ValidationAnnotation])] =
    appliedStereotypeInstanceWidget[UMLElement[MagicDrawUML]](
      derived, e,
      MagicDrawUMLUtil(project))

  def appliedStereotypesWithoutMetaclassProperties
  (project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedWidget,
   ek: MagicDrawElementKindDesignation, e: Element)
  ( implicit idg: IDGenerator[MagicDrawUML])
  : Try[(java.awt.Component, Seq[ValidationAnnotation])] =
    elementOperationWidget[UMLElement[MagicDrawUML], UMLStereotype[MagicDrawUML]](
      derived, e,
      _.getAppliedStereotypesWithoutMetaclassProperties,
      MagicDrawUMLUtil(project))

  def allForwardReferencesFromStereotypeTagProperties
  (project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedWidget,
   ek: MagicDrawElementKindDesignation, e: Element)
  ( implicit idg: IDGenerator[MagicDrawUML])
  : Try[(java.awt.Component, Seq[ValidationAnnotation])] =
    elementOperationWidget[UMLElement[MagicDrawUML], UMLElement[MagicDrawUML]](
      derived, e,
      _.allForwardReferencesFromStereotypeTagProperties,
      MagicDrawUMLUtil(project))

  def allForwardReferencesToElements
  (project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedWidget,
   ek: MagicDrawElementKindDesignation, e: Element)
  ( implicit idg: IDGenerator[MagicDrawUML])
  : Try[(java.awt.Component, Seq[ValidationAnnotation])] =
    elementOperationWidget[UMLElement[MagicDrawUML], UMLElement[MagicDrawUML]](
      derived, e,
      _.allForwardReferencesToElements,
      MagicDrawUMLUtil(project))

  def allForwardReferencesToImportablePackageableElements
  (project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedWidget,
   ek: MagicDrawElementKindDesignation, e: Element)
  ( implicit idg: IDGenerator[MagicDrawUML])
  : Try[(java.awt.Component, Seq[ValidationAnnotation])] =
    elementOperationWidget[UMLElement[MagicDrawUML], UMLElement[MagicDrawUML]](
      derived, e,
      _.allForwardReferencesToImportablePackageableElements,
      MagicDrawUMLUtil(project))

  def getPackageOwnerWithURI
  (project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedWidget,
   ek: MagicDrawElementKindDesignation, e: Element)
  ( implicit
    idg: IDGenerator[MagicDrawUML],
    otiCharacterizations: Option[Map[UMLPackage[MagicDrawUML], UMLComment[MagicDrawUML]]] = None)
  : Try[(java.awt.Component, Seq[ValidationAnnotation])] =
    elementOperationWidget[UMLElement[MagicDrawUML], UMLElement[MagicDrawUML]](
      derived, e,
      _.getPackageOwnerWithEffectiveURI,
      MagicDrawUMLUtil(project))

  def packageOrProfileOwner
  (project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedWidget,
   ek: MagicDrawElementKindDesignation, e: Element)
  ( implicit idg: IDGenerator[MagicDrawUML])
  : Try[(java.awt.Component, Seq[ValidationAnnotation])] =
    elementOperationWidget[UMLElement[MagicDrawUML], UMLPackage[MagicDrawUML]](
      derived, e,
      getPackageOrProfileOwner(_),
      MagicDrawUMLUtil(project))

  def ownedElements
  (project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedWidget,
   ek: MagicDrawElementKindDesignation, e: Element)
  ( implicit idg: IDGenerator[MagicDrawUML])
  : Try[(java.awt.Component, Seq[ValidationAnnotation])] =
    elementOperationWidget[UMLElement[MagicDrawUML], UMLElement[MagicDrawUML]](
      derived, e,
      _.ownedElement,
      MagicDrawUMLUtil(project))

  def allOwnedElementsWithinPackageScope
  (project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedWidget,
   ek: MagicDrawElementKindDesignation, e: Element)
  ( implicit idg: IDGenerator[MagicDrawUML])
  : Try[(java.awt.Component, Seq[ValidationAnnotation])] =
    elementOperationWidget[UMLElement[MagicDrawUML], UMLElement[MagicDrawUML]](
      derived, e,
      _.allOwnedElementsWithinPackageScope,
      MagicDrawUMLUtil(project))
}