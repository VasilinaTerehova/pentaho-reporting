/*!
* This program is free software; you can redistribute it and/or modify it under the
* terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
* Foundation.
*
* You should have received a copy of the GNU Lesser General Public License along with this
* program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
* or from the Free Software Foundation, Inc.,
* 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*
* This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
* without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU Lesser General Public License for more details.
*
* Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
*/

package org.pentaho.reporting.engine.classic.core.layout.model.table;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.reporting.engine.classic.core.ReportAttributeMap;
import org.pentaho.reporting.engine.classic.core.filter.types.AutoLayoutBoxType;
import org.pentaho.reporting.engine.classic.core.layout.model.AutoRenderBox;
import org.pentaho.reporting.engine.classic.core.layout.model.LayoutNodeTypes;
import org.pentaho.reporting.engine.classic.core.layout.model.RenderBox;
import org.pentaho.reporting.engine.classic.core.layout.model.RenderNode;
import org.pentaho.reporting.engine.classic.core.layout.model.context.BoxDefinition;
import org.pentaho.reporting.engine.classic.core.layout.style.SimpleStyleSheet;
import org.pentaho.reporting.engine.classic.core.metadata.ElementType;
import org.pentaho.reporting.engine.classic.core.states.ReportStateKey;
import org.pentaho.reporting.engine.classic.core.style.StyleSheet;
import org.pentaho.reporting.engine.classic.core.util.InstanceID;

/**
 * A table section box does not much rendering or layouting at all. It
 * represents one of the three possible sections and behaves like any other
 * block box. But (here it comes!) it refuses to be added to anything else than
 * a TableRenderBox (a small check to save me a lot of insanity ..).
 * <p/>
 * For a valid layout, the major and minor axes need to be flipped.
 *
 * @author Thomas Morgner
 */
public class TableRowRenderBox extends RenderBox
{
  private static final Log logger = LogFactory.getLog(TableRowRenderBox.class);
  private int rowIndex;
  private boolean bodySection;

  public TableRowRenderBox()
  {
    this(SimpleStyleSheet.EMPTY_STYLE, new InstanceID(), BoxDefinition.EMPTY,
        AutoLayoutBoxType.INSTANCE, ReportAttributeMap.EMPTY_MAP, null);
  }

  public TableRowRenderBox(final StyleSheet styleSheet,
                           final InstanceID instanceID,
                           final BoxDefinition boxDefinition,
                           final ElementType elementType,
                           final ReportAttributeMap attributes,
                           final ReportStateKey stateKey)
  {
    super(HORIZONTAL_AXIS, VERTICAL_AXIS,
        styleSheet, instanceID, boxDefinition, elementType, attributes, stateKey);
    this.rowIndex = -1;
  }

  public boolean useMinimumChunkWidth()
  {
    return true;
  }

  public boolean isAutoGenerated()
  {
    return AutoLayoutBoxType.INSTANCE == getElementType();
  }


  public int getNodeType()
  {
    return LayoutNodeTypes.TYPE_BOX_TABLE_ROW;
  }

  /**
   * If that method returns true, the element will not be used for rendering.
   * For the purpose of computing sizes or performing the layouting (in the
   * validate() step), this element will treated as if it is not there.
   * <p/>
   * If the element reports itself as non-empty, however, it will affect the
   * margin computation.
   *
   * @return
   */
  public boolean isIgnorableForRendering()
  {
    return false;
  }

  protected void reinit(final StyleSheet styleSheet,
                        final ElementType elementType,
                        final ReportAttributeMap attributes,
                        final InstanceID instanceId)
  {
    super.reinit(styleSheet, elementType, attributes, instanceId);
    rowIndex = -1;
    bodySection = false;
  }

  public boolean isBodySection()
  {
    return bodySection;
  }

  public void setBodySection(final boolean bodySection)
  {
    this.bodySection = bodySection;
  }

  public int getRowIndex()
  {
    return rowIndex;
  }

  public void setRowIndex(final int rowIndex)
  {
    this.rowIndex = rowIndex;
  }

  public void setCachedY(final long cachedY)
  {
    super.setCachedY(cachedY);
  }

  public void shiftCached(final long amount)
  {
    super.shiftCached(amount);
  }

  public long extendHeight(final RenderNode child, final long heightOffset)
  {
    return extendHeightInRowMode(child, heightOffset);
  }

  public void addChild(final RenderNode child)
  {
    if (isValid(child) == false)
    {
      TableCellRenderBox tsrb = new TableCellRenderBox();
      tsrb.addChild(child);
      addChild(tsrb);
      tsrb.close();
      return;
    }

    super.addChild(child);
  }

  private boolean isValid(final RenderNode child)
  {
    if ((child.getNodeType() & LayoutNodeTypes.MASK_BOX) != LayoutNodeTypes.MASK_BOX)
    {
      return true;
    }

    if (child.getNodeType() == LayoutNodeTypes.TYPE_BOX_AUTOLAYOUT)
    {
      return true;
    }

    if (child.getNodeType() == LayoutNodeTypes.TYPE_BOX_TABLE_CELL)
    {
      return true;
    }

    if (child.getNodeType() == LayoutNodeTypes.TYPE_BOX_BREAKMARK)
    {
      if (logger.isDebugEnabled())
      {
        logger.debug("WARN: BREAK MARKER INSIDE TABLE ROW");
      }
      return false;
    }

    if (child.getNodeType() == LayoutNodeTypes.TYPE_BOX_PROGRESS_MARKER)
    {
      if (logger.isDebugEnabled())
      {
        logger.debug("WARN: PROGRESS MARKER INSIDE TABLE ROW");
      }
      return true;
    }

    return false;
  }

  public RenderBox create(final StyleSheet styleSheet)
  {
    return new AutoRenderBox(styleSheet);
  }
}
