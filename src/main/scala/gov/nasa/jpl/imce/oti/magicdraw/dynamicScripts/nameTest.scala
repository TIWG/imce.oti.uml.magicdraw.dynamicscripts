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

import java.awt.event.ActionEvent
import java.lang.System

import com.nomagic.magicdraw.actions.ActionsProvider
import com.nomagic.magicdraw.core.Application
import com.nomagic.magicdraw.core.Project
import com.nomagic.magicdraw.ui.browser.Node
import com.nomagic.magicdraw.uml.ClassTypes
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype

import org.omg.oti.uml._
import org.omg.oti.uml.characteristics._
import org.omg.oti.uml.read.api._

import org.omg.oti.magicdraw.uml.characteristics._
import org.omg.oti.magicdraw.uml.read._

import gov.nasa.jpl.dynamicScripts.DynamicScriptsTypes._
import gov.nasa.jpl.dynamicScripts.magicdraw.utils.MDUML._
import gov.nasa.jpl.dynamicScripts.magicdraw.{ClassLoaderHelper, DynamicScriptsPlugin}
import gov.nasa.jpl.dynamicScripts.magicdraw.validation.MagicDrawValidationDataResults

import scala.collection.JavaConversions._
import scala.collection.immutable._
import scala.util.{Success, Try}
import scala.{Boolean,Option,None,Some,StringContext}
import scala.Predef.genericArrayOps

/**
 * @author Nicolas.F.Rouquette@jpl.nasa.gov
 */
object nameTest {

  def doit( p: Project, ev: ActionEvent, script: MainToolbarMenuAction )
  : Try[Option[MagicDrawValidationDataResults]] = {

    val a = Application.getInstance()
    val ap = ActionsProvider.getInstance
    val guiLog = a.getGUILog
    guiLog.clearLog()

    implicit val umlUtil = MagicDrawUMLUtil( p )
    import umlUtil._

    implicit val otiCharacterizations: Option[Map[UMLPackage[Uml], UMLComment[Uml]]] = None

    implicit val otiCharacterizationProfileProvider: OTICharacteristicsProvider[MagicDrawUML] =
      MagicDrawOTICharacteristicsProfileProvider()

    val dsp = DynamicScriptsPlugin.getInstance()
    val selectedElements = getMDBrowserSelectedElements map { e => umlElement( e ) }
    selectedElements foreach { e =>
      val mdE = umlMagicDrawUMLElement(e).getMagicDrawElement
      
      guiLog.log( s"==> ID=${e.toolSpecific_id}" )
  
      val mdIS = Option.apply( mdE.getAppliedStereotypeInstance ) 
      guiLog.log( s" mdID=${mdE.getID}: mdIS=${mdIS.isDefined} =$mdIS" )

      mdIS match {
        case None =>
          guiLog.log( s" no AppliedStereotypeInstance!" )

        case Some( is ) =>
          guiLog.log( s" AppliedStereotypeInstance: ${is.getSlot.size} slots" )

          for {
            s <- is.getSlot
            f = s.getDefiningFeature match { case fp: Uml#Property => umlProperty( fp ) }
            v = umlValueSpecification( s.getValue ).toSeq
          } {
            guiLog.log( s" => ${f.qualifiedName.get}: ${s.getValue}" )
            guiLog.log( s" => ${f.qualifiedName.get}: $v" )
          }
      }
      
      
      val eMetaclass = mdE.getClassType
      val appliedS = StereotypesHelper.getStereotypes( mdE ).toSet[Uml#Stereotype].toList.sortBy(_.qualifiedName.get)
      System.out.println(s"Applied stereotypes: ${appliedS.size}")
      appliedS.foreach{ s => 
        val metaProperties = StereotypesHelper.getExtensionMetaProperty( s, true ) filter { sp =>
          val pMetaclass = StereotypesHelper.getClassOfMetaClass( sp.getType.asInstanceOf[Uml#Class] )
          eMetaclass == pMetaclass || StereotypesHelper.isSubtypeOf( pMetaclass, eMetaclass )
        }
        val sGeneral = getAllGeneralStereotypes( s ).toList.sortBy(_.qualifiedName.get)   
        System.out.println(
          s"Applied: ${s.qualifiedName.get} with "+
          s"${metaProperties.size} meta-properties, ${sGeneral.size} general stereotypes")
        metaProperties.foreach{mp => System.out.println(s"meta-property: ${mp.getQualifiedName}")}
             
        sGeneral.foreach{ sg =>
          val mdSG = umlMagicDrawUMLElement(sg).getMagicDrawElement.asInstanceOf[Stereotype]
          val gmetaProperties = StereotypesHelper.getExtensionMetaProperty( mdSG, true ) filter { sp =>
            val pMetaclass = StereotypesHelper.getClassOfMetaClass( sp.getType.asInstanceOf[Uml#Class] )
            eMetaclass == pMetaclass || StereotypesHelper.isSubtypeOf( pMetaclass, eMetaclass )
          }
          System.out.println(s"General: ${sg.qualifiedName.get} with ${gmetaProperties.size} meta-properties")
          gmetaProperties.foreach{mp => System.out.println(s"general meta-property: ${mp.getQualifiedName}")}
        }
          
      }
      
      e match { 
        case s: UMLStereotype[Uml] =>
          val mdS = umlMagicDrawUMLElement(s).getMagicDrawElement.asInstanceOf[Stereotype]
          val baseClasses1 = StereotypesHelper.getBaseClasses( mdS, false )          
          System.out.println(s" baseClasses1: ${baseClasses1.size}")
          baseClasses1.toList.sortBy(_.getQualifiedName)
            .foreach{sp => System.out.println(s"baseClass1: ${sp.getQualifiedName}")}
          
          val baseClasses2 = StereotypesHelper.getBaseClasses( mdS, true ) 
          System.out.println(s" baseClasses2: ${baseClasses2.size}")
          baseClasses2.toList.sortBy(_.getQualifiedName)
            .foreach{sp => System.out.println(s"baseClass2: ${sp.getQualifiedName}")}
          
          val metaProperties = StereotypesHelper.getExtensionMetaProperty( mdS, false ) 
          System.out.println(s" metaProperties: ${metaProperties.size}")
          metaProperties.toList.sortBy(_.getQualifiedName)
            .foreach{sp => System.out.println(s"meta property: ${sp.getQualifiedName}")}

        case ep: UMLPackage[Uml] =>
          System.out.println(s"package: ${ep.qualifiedName}; effective URI=${ep.getEffectiveURI}, URI=${ep.URI}")
        case _ => ()
      }

      val mName = ClassTypes.getShortName( mdE.getClassType )

      def dynamicScriptMenuFilter( das: DynamicActionScript ): Boolean =
        das match {
          case c: BrowserContextMenuAction =>
            val available = ClassLoaderHelper.isDynamicActionScriptAvailable( c )
            c.context match {
              case cp: ProjectContext =>
                if (cp.project.jname == "gov.nasa.jpl.magicdraw.omf.exporter") {
                  System.out.println(c)
                  System.out.println(s"avail? $available")
                }
              case _ =>
                ()
            }
            available
          case _ =>
            false
        }

      System.out.println(s"\n\n")
      val mActions = dsp.getRelevantMetaclassActions( mName, dynamicScriptMenuFilter )
      System.out.println(s"\n\nmActions: ${mActions.size}")
      for {
        mAction <- mActions
      } {
        System.out.println(mAction)
      }
    }

    Success( None )
  }

  def getMDBrowserSelectedElements
  : Set[Element]
  = {
    val project = Application.getInstance().getProjectsManager.getActiveProject
    if ( null == project )
      return Set()

    val tab = project.getProjectActiveBrowserTabTree
    val elementFilter: ( Node => Option[Element] ) = { n =>
      n.getUserObject match {
        case u: Element =>
          Some(u)
        case _ =>
          None
      }
    }
    val elements =
      tab.fold[Set[Element]](Set()){
        btab =>
          btab.getSelectedNodes
            .map( elementFilter( _ ) )
            .filter ( _.isDefined )
            .map ( _.get )
            .to[Set]
      }
    elements
  }
}