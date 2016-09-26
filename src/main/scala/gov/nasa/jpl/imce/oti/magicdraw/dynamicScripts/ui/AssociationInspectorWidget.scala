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

import com.nomagic.magicdraw.core.Project
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element
import gov.nasa.jpl.dynamicScripts.DynamicScriptsTypes
import gov.nasa.jpl.dynamicScripts.magicdraw.designations.MagicDrawElementKindDesignation
import gov.nasa.jpl.dynamicScripts.magicdraw.utils._
import gov.nasa.jpl.imce.oti.magicdraw.dynamicScripts.utils.OTIHelper
import org.omg.oti.magicdraw.uml.canonicalXMI.MagicDrawIDGenerator
import org.omg.oti.magicdraw.uml.canonicalXMI.helper._
import org.omg.oti.magicdraw.uml.read.MagicDrawUML
import org.omg.oti.uml.read.api._

import scala.collection.immutable._
import scala.util.Try
import scala.{None, Some, Tuple2}

object AssociationInspectorWidget {

  import ComputedDerivedWidgetHelper._

  def memberEnds
  ( project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedWidget,
    ek: MagicDrawElementKindDesignation, e: Element )
  : Try[(java.awt.Component, Seq[ValidationAnnotation])]
  = OTIHelper.toTry(
    MagicDrawOTIHelper.getOTIMagicDrawInfoForDataCharacteristics(project),
    (ordsa: MagicDrawOTIResolvedDocumentSetAdapterForDataProvider) => {
      implicit val idg = MagicDrawIDGenerator()(ordsa.rds.ds)
      propertyOperationWidget[UMLAssociation[MagicDrawUML], UMLProperty[MagicDrawUML]](
        derived, e,
        (a: UMLAssociation[MagicDrawUML]) => {
          a.memberEnd
        },
        ordsa.otiAdapter.umlOps)
    })
  def orderedMemberEnds
  ( project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedWidget,
    ek: MagicDrawElementKindDesignation, e: Element )
  : Try[(java.awt.Component, Seq[ValidationAnnotation])]
  = OTIHelper.toTry(
    MagicDrawOTIHelper.getOTIMagicDrawInfoForDataCharacteristics(project),
    (ordsa: MagicDrawOTIResolvedDocumentSetAdapterForDataProvider) => {
      implicit val idg = MagicDrawIDGenerator()(ordsa.rds.ds)
      propertyOperationWidget[UMLAssociation[MagicDrawUML], UMLProperty[MagicDrawUML]](
        derived, e,
        (a: UMLAssociation[MagicDrawUML]) => {
          a.getDirectedAssociationEnd match {
            case None =>
              Seq.empty[UMLProperty[MagicDrawUML]]
            case Some(Tuple2(source, target)) =>
              Seq(source, target)
          }
        },
        ordsa.otiAdapter.umlOps)
    })
}
