/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2016 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.descriptors.extension.dvb;

import static nl.digitalekabeltelevisie.util.Utils.*;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.BitSource;
import nl.digitalekabeltelevisie.util.Utils;

public class TargetRegionNameDescriptor extends DVBExtensionDescriptor {



	private class TargetRegionName implements TreeNode{

		private final int region_dept;
		private final int region_name_length;
		private final String region_name;
		private final int primary_region_code;
		private final int secondary_region_code;
		private final int tertiary_region_code;

		/**
		 * @param reserved
		 * @param country_code_flag
		 * @param region_dept
		 * @param country_code
		 * @param primary_region_code
		 * @param secondary_region_code
		 * @param tertiary_region_code
		 */
		private TargetRegionName(final int region_dept, final int region_name_length, final byte [] region_name,
				final int primary_region_code, final int secondary_region_code, final int tertiary_region_code) {
			super();
			this.region_dept = region_dept;
			this.region_name_length = region_name_length;
			this.region_name = Utils.getString(region_name, 0, region_name_length);

			this.primary_region_code = primary_region_code;
			this.secondary_region_code = secondary_region_code;
			this.tertiary_region_code = tertiary_region_code;
		}


		/* (non-Javadoc)
		 * @see nl.digitalekabeltelevisie.controller.TreeNode#getJTreeNode(int)
		 */
		@Override
		public DefaultMutableTreeNode getJTreeNode(final int modus) {
			final DefaultMutableTreeNode t =  new DefaultMutableTreeNode(new KVP("TargetRegionName"));
			t.add(new DefaultMutableTreeNode(new KVP("region_dept",region_dept,null)));
			t.add(new DefaultMutableTreeNode(new KVP("region_name_length",region_name_length,null)));
			t.add(new DefaultMutableTreeNode(new KVP("region_name",region_name,null)));
			t.add(new DefaultMutableTreeNode(new KVP("primary_region_code",primary_region_code,null)));
			if(region_dept>=2){
				t.add(new DefaultMutableTreeNode(new KVP("secondary_region_code",secondary_region_code,null)));
				if(region_dept==3){
					t.add(new DefaultMutableTreeNode(new KVP("tertiary_region_code",tertiary_region_code,null)));
				}
			}
			return t;
		}

	}

	private final String country_code;
	private final String iso_639_language_code;
	private final List<TargetRegionName> targetRegions = new ArrayList<TargetRegionNameDescriptor.TargetRegionName>();

	// 0x0a target name descriptor

	public TargetRegionNameDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset,parent);
		country_code = getISO8859_1String(selector_byte,0,3);
		iso_639_language_code = getISO8859_1String(selector_byte,3,3);

		final BitSource bs =new BitSource(selector_byte, 6);
		while(bs.available()>0){
			final int region_depth = bs.readBits(2);
			final int region_name_length = bs.readBits(6);
			final byte [] region_name = bs.readBytes(region_name_length);

			final int primary_region_code = bs.readBits(8);

			int secondary_region_code = 0;
			int tertiary_region_code = 0;
			if(region_depth>=2){
				secondary_region_code = bs.readBits(8);
				if(region_depth==3){
					tertiary_region_code = bs.readBits(16);
				}
			}
			targetRegions.add(new TargetRegionName(region_depth, region_name_length, region_name, primary_region_code, secondary_region_code, tertiary_region_code));
		}

	}


	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){

		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("country_code",country_code,null)));
		t.add(new DefaultMutableTreeNode(new KVP("ISO_639_language_code",iso_639_language_code,null)));
		Utils.addToList(t, targetRegions, modus);

		return t;
	}



}
