/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2014 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.descriptors.extension;

import static nl.digitalekabeltelevisie.util.Utils.*;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.ExtensionDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.BitSource;
import nl.digitalekabeltelevisie.util.Utils;

public class TargetRegionDescriptor extends ExtensionDescriptor {


	// 0x09 target region descriptor

	private class TargetRegion implements TreeNode{

		private final int reserved;
		private final int country_code_flag;
		private final int region_dept;
		private String country_code;
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
		private TargetRegion(final int reserved, final int country_code_flag, final int region_dept, final byte [] country_codeBytes,
				final int primary_region_code, final int secondary_region_code, final int tertiary_region_code) {
			super();
			this.reserved = reserved;
			this.country_code_flag = country_code_flag;
			this.region_dept = region_dept;
			if((country_code_flag == 1) && (country_codeBytes != null)){
				this.country_code = getISO8859_1String(country_codeBytes,0,3);
			}
			this.primary_region_code = primary_region_code;
			this.secondary_region_code = secondary_region_code;
			this.tertiary_region_code = tertiary_region_code;
		}

		/* (non-Javadoc)
		 * @see nl.digitalekabeltelevisie.controller.TreeNode#getJTreeNode(int)
		 */
		@Override
		public DefaultMutableTreeNode getJTreeNode(final int modus) {
			final DefaultMutableTreeNode t =  new DefaultMutableTreeNode(new KVP("TargetRegion"));
			t.add(new DefaultMutableTreeNode(new KVP("reserved",reserved,null)));
			t.add(new DefaultMutableTreeNode(new KVP("country_code_flag",country_code_flag,null)));
			t.add(new DefaultMutableTreeNode(new KVP("region_dept",region_dept,null)));
			if(country_code_flag==1){
				t.add(new DefaultMutableTreeNode(new KVP("country_code",country_code,null)));
			}
			if(region_dept>=1){
				t.add(new DefaultMutableTreeNode(new KVP("primary_region_code",primary_region_code,null)));
				if(region_dept>=2){
					t.add(new DefaultMutableTreeNode(new KVP("secondary_region_code",secondary_region_code,null)));
					if(region_dept==3){
						t.add(new DefaultMutableTreeNode(new KVP("tertiary_region_code",tertiary_region_code,null)));
					}
				}
			}
			return t;
		}

	}

	private final String country_code;
	private final List<TargetRegion> targetRegions = new ArrayList<TargetRegionDescriptor.TargetRegion>();

	// 0x0a target name descriptor

	public TargetRegionDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset,parent);
		country_code = getISO8859_1String(selector_byte,0,3);

		final BitSource bs =new BitSource(selector_byte, 3);
		while(bs.available()>0){
			final int reserved = bs.readBits(5);
			final int country_code_flag = bs.readBits(1);
			final int region_depth = bs.readBits(2);
			int primary_region_code = 0;
			int secondary_region_code = 0;
			int tertiary_region_code = 0;
			byte[] countryCodeBytes = null;
			if(country_code_flag==1){
				countryCodeBytes = bs.readBytes(3);
			}
			if(region_depth>=1){
				primary_region_code = bs.readBits(8);
				if(region_depth>=2){
					secondary_region_code = bs.readBits(8);
					if(region_depth==3){
						tertiary_region_code = bs.readBits(8);
					}
				}
			}
			targetRegions.add(new TargetRegion(reserved, country_code_flag, region_depth, countryCodeBytes, primary_region_code, secondary_region_code, tertiary_region_code));
		}

	}


	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){

		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("country_code",country_code,null)));
		Utils.addToList(t, targetRegions, modus);

		return t;
	}



}
