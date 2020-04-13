/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2012 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
 *
 *  This file is part of DVB Inspector.
 *
 *  DVB Inspector is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  DVB Inspector is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with DVB Inspector.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  The author requests that he be notified of any application, applet, or
 *  other binary that makes use of this code, but that's more out of curiosity
 *  than anything and is not required.
 *
 */

package nl.digitalekabeltelevisie.data.mpeg.pes.dvbsubtitling;

import static nl.digitalekabeltelevisie.util.Utils.*;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;

public class DisplayDefinitionSegment extends Segment implements TreeNode{

	public DisplayDefinitionSegment(final byte[] data,final int offset) {
		super(data,offset);
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode s = super.getJTreeNode(modus);
		s.add(new DefaultMutableTreeNode(new KVP("dds_version_number",getDDSVersionNumber(),null)));
		s.add(new DefaultMutableTreeNode(new KVP("display_window_flag",getDisplayWindowFlag(),null)));
		s.add(new DefaultMutableTreeNode(new KVP("display_width",getDisplayWidth(),"("+(getDisplayWidth()+1)+")")));
		s.add(new DefaultMutableTreeNode(new KVP("display_height",getDisplayHeight(),"("+(getDisplayHeight()+1)+")")));
		if(getDisplayWindowFlag()==1){
			s.add(new DefaultMutableTreeNode(new KVP("display_window_horizontal_position_minimum",getDisplayWindowHorizontalPositionMinimum(),null)));
			s.add(new DefaultMutableTreeNode(new KVP("display_window_horizontal_position_maximum",getDisplayWindowHorizontalPositionMaximum(),null)));
			s.add(new DefaultMutableTreeNode(new KVP("display_window_vertical_position_minimum",getDisplayWindowVerticalPositionMinimum(),null)));
			s.add(new DefaultMutableTreeNode(new KVP("display_window_vertical_position_maximum",getDisplayWindowVerticalPositionMaximum(),null)));

		}



		return s;
	}

	/**
	 * @return
	 */
	public int getDisplayWidth() {
		return getInt(data_block, offset+7, 2,MASK_16BITS);
	}

	public int getDisplayHeight() {
		return getInt(data_block, offset+9, 2,MASK_16BITS);
	}
	/**
	 * @return
	 */
	public int getDisplayWindowHorizontalPositionMinimum() {
		return getInt(data_block, offset+11, 2,MASK_16BITS);
	}

	public int getDisplayWindowHorizontalPositionMaximum() {
		return getInt(data_block, offset+13, 2,MASK_16BITS);
	}
	public int getDisplayWindowVerticalPositionMinimum() {
		return getInt(data_block, offset+15, 2,MASK_16BITS);
	}

	public int getDisplayWindowVerticalPositionMaximum() {
		return getInt(data_block, offset+17, 2,MASK_16BITS);
	}
	/**
	 * @return
	 */
	public int getDDSVersionNumber() {
		return getInt(data_block, offset+6, 1,0xf0)>>4;
	}

	public int getDisplayWindowFlag() {
		return getInt(data_block, offset+6, 1,0x08)>>3;
	}


}