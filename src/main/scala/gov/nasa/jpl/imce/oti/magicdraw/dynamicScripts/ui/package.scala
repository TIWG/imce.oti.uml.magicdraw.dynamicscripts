package gov.nasa.jpl.imce.oti.magicdraw.dynamicScripts

import gov.nasa.jpl.dynamicScripts.magicdraw.ui.nodes.ReferenceNodeInfo
import org.omg.oti.magicdraw.uml.read.{MagicDrawUML, MagicDrawUMLUtil}

import org.omg.oti.json.common.OTIPrimitiveTypes.TOOL_SPECIFIC_ID
import org.omg.oti.uml.read.api.UMLElement

package object ui {

  def toToolSpecificIDReferenceNodeInfo
  (e: UMLElement[MagicDrawUML])
  (implicit umlUtil: MagicDrawUMLUtil)
  : ReferenceNodeInfo
  = ReferenceNodeInfo(
    TOOL_SPECIFIC_ID.unwrap(e.toolSpecific_id),
    umlUtil.umlMagicDrawUMLElement(e).getMagicDrawElement )
}
