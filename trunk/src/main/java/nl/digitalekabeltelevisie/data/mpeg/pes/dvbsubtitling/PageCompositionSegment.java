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

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;

public class PageCompositionSegment extends Segment implements TreeNode{

	public static class Region implements TreeNode{
		private int region_id;
		private int reserved;
		private int region_horizontal_address;
		private int region_vertical_address;

		public Region(final int region_id, final int reserved, final int region_horizontal_address, final int region_vertical_address) {
			super();
			this.region_id = region_id;
			this.reserved = reserved;
			this.region_horizontal_address = region_horizontal_address;
			this.region_vertical_address = region_vertical_address;
		}

		public DefaultMutableTreeNode getJTreeNode(final int modus) {
			final DefaultMutableTreeNode s = new DefaultMutableTreeNode(new KVP("Region"));
			s.add(new DefaultMutableTreeNode(new KVP("region_id",region_id,null)));
			s.add(new DefaultMutableTreeNode(new KVP("reserved",reserved,null)));
			s.add(new DefaultMutableTreeNode(new KVP("region_horizontal_address",region_horizontal_address,null)));
			s.add(new DefaultMutableTreeNode(new KVP("region_vertical_address",region_vertical_address,null)));
			return s;
		}


		/**
		 * @return the region_horizontal_address
		 */
		public int getRegion_horizontal_address() {
			return region_horizontal_address;
		}


		/**
		 * @return the region_id
		 */
		public int getRegion_id() {
			return region_id;
		}


		/**
		 * @return the region_vertical_address
		 */
		public int getRegion_vertical_address() {
			return region_vertical_address;
		}


		/**
		 * @return the reserved
		 */
		public int getReserved() {
			return reserved;
		}

	}

	public PageCompositionSegment(final byte[] data,final int offset) {
		super(data,offset);
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode s = super.getJTreeNode(modus);
		s.add(new DefaultMutableTreeNode(new KVP("page_time_out",getPageTimeOut(),null)));
		s.add(new DefaultMutableTreeNode(new KVP("page_version_number",getPageVersionNumber(),null)));
		s.add(new DefaultMutableTreeNode(new KVP("page_state",getPageState(),DVBSubtitlingPESDataField.getPageStateString(getPageState()))));

		addListJTree(s, getRegions(),modus,"regions");

		return s;
	}

	/**
	 * @return
	 */
	public int getPageState() {
		return getInt(data_block, offset+7, 1,0x0c)>>2;
	}

	/**
	 * @return
	 */
	public int getPageVersionNumber() {
		return getInt(data_block, offset+7, 1,0xF0)>>4;
	}

	/**
	 * @return
	 */
	public int getPageTimeOut() {
		return getInt(data_block, offset+6, 1,MASK_8BITS);
	}

	/**
	 * @return
	 */
	public List<Region> getRegions() {
		final ArrayList<Region> regions = new ArrayList<Region>();
		int t = 0;
		while((t+2)<getSegmentLength()){
			final int region = getInt(data_block, offset+8+t, 1, MASK_8BITS);
			final int res = getInt(data_block, offset+9+t, 1, MASK_8BITS);
			final int hor = getInt(data_block, offset+10+t, 2, MASK_16BITS);
			final int ver = getInt(data_block, offset+12+t, 2, MASK_16BITS);
			regions.add(new Region(region,res,hor,ver));
			t+=6;
		}
		return regions;
	}


}