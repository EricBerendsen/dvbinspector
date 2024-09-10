/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2024 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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


import nl.digitalekabeltelevisie.controller.KVP;


public class DisplayDefinitionSegment extends Segment{

	public DisplayDefinitionSegment(byte[] data, int offset) {
		super(data,offset);
	}

	@Override
	public KVP getJTreeNode(int modus) {
		KVP s = super.getJTreeNode(modus);
		s.add(new KVP("dds_version_number",getDDSVersionNumber()));
		s.add(new KVP("display_window_flag",getDisplayWindowFlag()));
		s.add(new KVP("display_width",getDisplayWidth()).setDescription("("+(getDisplayWidth()+1)+")"));
		s.add(new KVP("display_height",getDisplayHeight()).setDescription("("+(getDisplayHeight()+1)+")"));
		if(getDisplayWindowFlag()==1){
			s.add(new KVP("display_window_horizontal_position_minimum",getDisplayWindowHorizontalPositionMinimum()));
			s.add(new KVP("display_window_horizontal_position_maximum",getDisplayWindowHorizontalPositionMaximum()));
			s.add(new KVP("display_window_vertical_position_minimum",getDisplayWindowVerticalPositionMinimum()));
			s.add(new KVP("display_window_vertical_position_maximum",getDisplayWindowVerticalPositionMaximum()));

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