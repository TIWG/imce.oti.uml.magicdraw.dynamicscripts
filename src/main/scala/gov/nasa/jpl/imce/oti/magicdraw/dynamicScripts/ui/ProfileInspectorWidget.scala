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
import java.lang.IllegalArgumentException

import com.nomagic.magicdraw.core.Project
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element
import gov.nasa.jpl.dynamicScripts.DynamicScriptsTypes
import gov.nasa.jpl.dynamicScripts.magicdraw.designations.MagicDrawElementKindDesignation
import gov.nasa.jpl.dynamicScripts.magicdraw.ui.nodes._

import org.omg.oti.uml.read.api._
import org.omg.oti.magicdraw.uml.read.MagicDrawUMLUtil

import scala.collection.immutable._
import scala.util.{Failure, Success, Try}
import scala.{Any,StringContext}

object ProfileInspectorWidget {

  
  def allImportedProfiles(
    project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedProperty,
    ek: MagicDrawElementKindDesignation, e: Element ): Try[Seq[Any]] = {
    
    val umlUtil = MagicDrawUMLUtil( project )
    import umlUtil._
          
    umlElement( e ) match { 
      case pf: UMLProfile[Uml] =>
        val apf = pf.allImportedProfilesTransitively.toSeq.sortBy { pf => pf.qualifiedName.get }
        Success(
          apf.map { pf =>
            ReferenceNodeInfo( pf.name.get, umlMagicDrawUMLElement(pf).getMagicDrawElement )
          }.to[Seq] )
        
      case x =>
        Failure( new IllegalArgumentException(s"Not a package; instead got a ${x.xmiType}"))
    }
  }
  
  
  def allNestedProfiles(
    project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedProperty,
    ek: MagicDrawElementKindDesignation, e: Element ): Try[Seq[Any]] = {
    
    val umlUtil = MagicDrawUMLUtil( project )
    import umlUtil._
          
    umlElement( e ) match { 
      case pf: UMLProfile[Uml] =>
        val apf = pf.allNestedProfilesTransitively.toSeq.sortBy { pf => pf.qualifiedName.get }
        Success(
          apf.map { pf =>
            ReferenceNodeInfo( pf.name.get, umlMagicDrawUMLElement(pf).getMagicDrawElement )
          }.to[Seq] )
        
      case x =>
        Failure( new IllegalArgumentException(s"Not a package; instead got a ${x.xmiType}"))
    }
  }
  
  def allVisibleProfiles(
    project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedProperty,
    ek: MagicDrawElementKindDesignation, e: Element ): Try[Seq[Any]] = {
    
    val umlUtil = MagicDrawUMLUtil( project )
    import umlUtil._
          
    umlElement( e ) match { 
      case pf: UMLProfile[Uml] =>
        val apf = pf.allVisibleProfilesTransitively.toSeq.sortBy { pf => pf.qualifiedName.get }
        Success(
          apf.map { pf =>
            ReferenceNodeInfo( pf.name.get, umlMagicDrawUMLElement(pf).getMagicDrawElement )
          }.to[Seq] )
        
      case x =>
        Failure( new IllegalArgumentException(s"Not a package; instead got a ${x.xmiType}"))
    }
  }
}