/**
 * 
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 * 
 *  This code is Copyright 2009-2020 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.m7fastscan;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.LookUpList;

public class M7Descriptor extends Descriptor {

	private static final LookUpList m7_descriptor_name = new LookUpList.Builder().
			add(0x83, "M7 logical_channel_descriptor").
			add(0x84, "M7 operator_name_descriptor").
			add(0x85, "M7 operator_sublist_name_descriptor").
			add(0x86, "M7 operator_preferences_descriptor").
			add(0x87, "M7 operator_DiSEqC_descriptor").
			add(0x88, "M7 operator_options_descriptor").
			add(0x89, "M7 Nagra_brandID_descriptor").
			add(0x8A, "M7 OTT_brandID_descriptor").
			build();
			
	public M7Descriptor(byte[] b, TableSection parent) {
		super(b, parent);
	}
	
	@Override
	public KVP getJTreeNode(int modus){
        return (KVP)super.getJTreeNode(modus);
	}

	@Override
	public String getDescriptorname(){
		return m7_descriptor_name.get(descriptorTag, "unknown M7 descriptor");
	}

	public static String getDescriptorname(int tag){
		return m7_descriptor_name.get(tag, "unknown M7 descriptor");
	}

}
