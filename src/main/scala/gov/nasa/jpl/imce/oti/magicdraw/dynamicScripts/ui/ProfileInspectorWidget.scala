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