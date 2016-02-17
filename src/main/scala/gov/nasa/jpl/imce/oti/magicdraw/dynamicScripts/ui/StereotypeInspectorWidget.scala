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
import java.awt.event.InputEvent
import javax.swing.JOptionPane
import gov.nasa.jpl.imce.oti.magicdraw.dynamicScripts.utils.OTIHelper
import gov.nasa.jpl.imce.oti.magicdraw.dynamicScripts.validation.OTIMagicDrawValidation
import org.omg.oti.magicdraw.uml.canonicalXMI.MagicDrawDocumentOps
import org.omg.oti.magicdraw.uml.characteristics.MagicDrawOTICharacteristicsProfileProvider
import org.omg.oti.uml.characteristics.OTICharacteristicsProvider
import org.omg.oti.uml.xmi.IDGenerator

import scala.collection.immutable._
import scala.language.{implicitConversions, postfixOps}
import scala.util.{Failure, Success, Try}
import scala.{Option,None,StringContext,Unit}
import scala.Predef.ArrowAssoc

import com.nomagic.magicdraw.core.Project
import com.nomagic.magicdraw.ui.dialogs.specifications.SpecificationDialogManager
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element
import gov.nasa.jpl.dynamicScripts.DynamicScriptsTypes
import gov.nasa.jpl.dynamicScripts.magicdraw.DynamicScriptsPlugin
import gov.nasa.jpl.dynamicScripts.magicdraw.designations.MagicDrawElementKindDesignation
import gov.nasa.jpl.dynamicScripts.magicdraw.specificationDialog.SpecificationComputedComponent
import gov.nasa.jpl.dynamicScripts.magicdraw.utils._
import org.omg.oti.uml.read.api._
import org.omg.oti.magicdraw.uml.read.MagicDrawUML
import org.omg.oti.magicdraw.uml.read.MagicDrawUMLUtil
import com.nomagic.magicdraw.core.Application

object StereotypeInspectorWidget {

  import ComputedDerivedWidgetHelper._

  def baseMetaProperties
  ( project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedWidget,
    ek: MagicDrawElementKindDesignation, e: Element )
  : Try[( java.awt.Component, Seq[ValidationAnnotation] )] = {
    implicit val umlUtil = MagicDrawUMLUtil( project )
    OTIHelper.getOTIMDInfo().fold[Try[( java.awt.Component, Seq[ValidationAnnotation] )]](
    l = (nels) => Failure(nels.head),
    r = (info) => {
      implicit val idg = info._1
      elementOperationWidget[UMLStereotype[MagicDrawUML], UMLProperty[MagicDrawUML]](
        derived, e,
        (s: UMLStereotype[MagicDrawUML]) => s.baseMetaProperties.toList.sortBy(_.qualifiedName.get),
        umlUtil )
    })
  }

  def baseMetaPropertiesExceptRedefined
  ( project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedWidget,
    ek: MagicDrawElementKindDesignation, e: Element )
  : Try[( java.awt.Component, Seq[ValidationAnnotation] )] = {
    implicit val umlUtil = MagicDrawUMLUtil( project )
    OTIHelper.getOTIMDInfo().fold[Try[( java.awt.Component, Seq[ValidationAnnotation] )]](
      l = (nels) => Failure(nels.head),
      r = (info) => {
        implicit val idg = info._1
        elementOperationWidget[UMLStereotype[MagicDrawUML], UMLProperty[MagicDrawUML]](
          derived, e,
          (s: UMLStereotype[MagicDrawUML]) => s.baseMetaPropertiesExceptRedefined.toList.sortBy(_.qualifiedName.get),
          umlUtil)
      })
  }

  def profile
  ( project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedWidget,
    ek: MagicDrawElementKindDesignation, e: Element )
  : Try[(java.awt.Component, Seq[ValidationAnnotation])] = {
    implicit val umlUtil = MagicDrawUMLUtil(project)
    OTIHelper.getOTIMDInfo().fold[Try[(java.awt.Component, Seq[ValidationAnnotation])]](
      l = (nels) => Failure(nels.head),
      r = (info) => {
        implicit val idg: IDGenerator[MagicDrawUML] = info._1
        elementOperationWidget[UMLStereotype[MagicDrawUML], UMLProfile[MagicDrawUML]](
          derived, e,
          _.profile.toSet,
          MagicDrawUMLUtil(project))
      })
  }

  def getSpecializedStereotypes
  ( project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedWidget,
    ek: MagicDrawElementKindDesignation, e: Element )
  : Try[(java.awt.Component, Seq[ValidationAnnotation])] = {
    implicit val umlUtil = MagicDrawUMLUtil(project)
    OTIHelper.getOTIMDInfo().fold[Try[(java.awt.Component, Seq[ValidationAnnotation])]](
      l = (nels) => Failure(nels.head),
      r = (info) => {
        implicit val idg: IDGenerator[MagicDrawUML] = info._1
        elementOperationWidget[UMLStereotype[MagicDrawUML], UMLStereotype[MagicDrawUML]](
          derived, e,
          org.omg.oti.uml.getSpecializedStereotypes(_),
          umlUtil)
      })
  }

  def getSpecializedStereotypesOutsideProfile
  ( project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedWidget,
    ek: MagicDrawElementKindDesignation, e: Element )
  : Try[(java.awt.Component, Seq[ValidationAnnotation])] = {
    implicit val umlUtil = MagicDrawUMLUtil(project)
    OTIHelper.getOTIMDInfo().fold[Try[(java.awt.Component, Seq[ValidationAnnotation])]](
      l = (nels) => Failure(nels.head),
      r = (info) => {
        implicit val idg: IDGenerator[MagicDrawUML] = info._1
        elementOperationWidget[UMLStereotype[MagicDrawUML], UMLStereotype[MagicDrawUML]](
          derived, e,
          org.omg.oti.uml.getSpecializedStereotypesOutsideProfile(_),
          umlUtil)
      })
  }

  def getSpecializedStereotypesWithinProfile
  ( project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedWidget,
    ek: MagicDrawElementKindDesignation, e: Element )
  : Try[(java.awt.Component, Seq[ValidationAnnotation])] = {
    implicit val umlUtil = MagicDrawUMLUtil(project)
    OTIHelper.getOTIMDInfo().fold[Try[(java.awt.Component, Seq[ValidationAnnotation])]](
      l = (nels) => Failure(nels.head),
      r = (info) => {
        implicit val idg: IDGenerator[MagicDrawUML] = info._1
        elementOperationWidget[UMLStereotype[MagicDrawUML], UMLStereotype[MagicDrawUML]](
          derived, e,
          org.omg.oti.uml.getSpecializedStereotypesWithinProfile(_),
          umlUtil)
      })
  }

  def getAllSpecializedStereotypes
  ( project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedWidget,
    ek: MagicDrawElementKindDesignation, e: Element )
  : Try[(java.awt.Component, Seq[ValidationAnnotation])] = {
    implicit val umlUtil = MagicDrawUMLUtil(project)
    OTIHelper.getOTIMDInfo().fold[Try[(java.awt.Component, Seq[ValidationAnnotation])]](
      l = (nels) => Failure(nels.head),
      r = (info) => {
        implicit val idg: IDGenerator[MagicDrawUML] = info._1
        elementOperationWidget[UMLStereotype[MagicDrawUML], UMLStereotype[MagicDrawUML]](
          derived, e,
          org.omg.oti.uml.getAllSpecializedStereotypes(_),
          umlUtil)
      })
  }

  def getAllSpecializedStereotypesWithinProfile
  ( project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedWidget,
    ek: MagicDrawElementKindDesignation, e: Element )
  : Try[(java.awt.Component, Seq[ValidationAnnotation])] = {
    implicit val umlUtil = MagicDrawUMLUtil(project)
    OTIHelper.getOTIMDInfo().fold[Try[(java.awt.Component, Seq[ValidationAnnotation])]](
      l = (nels) => Failure(nels.head),
      r = (info) => {
        implicit val idg: IDGenerator[MagicDrawUML] = info._1
        elementOperationWidget[UMLStereotype[MagicDrawUML], UMLStereotype[MagicDrawUML]](
          derived, e,
          org.omg.oti.uml.getAllSpecializedStereotypesWithinProfile(_),
          umlUtil)
      })
  }

  def getSpecializedStereotypesFromOtherProfiles
  ( project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedWidget,
    ek: MagicDrawElementKindDesignation, e: Element )
  : Try[(java.awt.Component, Seq[ValidationAnnotation])] = {
    implicit val umlUtil = MagicDrawUMLUtil(project)
    OTIHelper.getOTIMDInfo().fold[Try[(java.awt.Component, Seq[ValidationAnnotation])]](
      l = (nels) => Failure(nels.head),
      r = (info) => {
        implicit val idg: IDGenerator[MagicDrawUML] = info._1
        elementOperationWidget[UMLStereotype[MagicDrawUML], UMLStereotype[MagicDrawUML]](
          derived, e,
          org.omg.oti.uml.getSpecializedStereotypesFromOtherProfiles(_),
          umlUtil)
      })
  }
}