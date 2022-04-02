/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2022 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.canal_international;

import static nl.digitalekabeltelevisie.util.Utils.*;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.DVBString;
import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

public class CosBatSelectionDescriptor extends Descriptor {
	
	int bouquet_id;
	int selector_type;
	int usage_id;
	DVBString region_name;
	byte[] databyte;

	/**
	 * @param b
	 * @param offset
	 * @param parent
	 */
	public CosBatSelectionDescriptor(byte[] b, int offset, TableSection parent) {
		super(b, offset, parent);
		bouquet_id = getInt(b, offset+2,2,MASK_16BITS);
		selector_type = getInt(b, offset+4,1,MASK_8BITS);
		if (selector_type == 0x01) { 
			usage_id = getInt(b, offset+5,1,MASK_8BITS);
		}else if (selector_type == 0x02) { 
			region_name = new DVBString (b,offset+5);
		}else if (selector_type != 0x03) { 
			databyte = getBytes(b, offset+5, descriptorLength -3);
		}
	}

	@Override
	public String getDescriptorname(){
		return "cos_bat_selection_descriptor";
	}
	
	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){

		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("bouquet_id",bouquet_id,null)));
		t.add(new DefaultMutableTreeNode(new KVP("selector_type",selector_type,getSelectorTypeString(selector_type))));
		if (selector_type == 0x01) { 
			t.add(new DefaultMutableTreeNode(new KVP("usage_id",usage_id,null)));
		}else if (selector_type == 0x02) { 
			t.add(new DefaultMutableTreeNode(new KVP("region_name_length",region_name.getLength(),null)));
			t.add(new DefaultMutableTreeNode(new KVP("region_name",region_name,null)));
		}else if (selector_type != 0x03) { 
			t.add(new DefaultMutableTreeNode(new KVP("databyte",databyte,null)));

		}


		return t;
	}

	/**
	 * @param selector_type2
	 * @return
	 */
	private static String getSelectorTypeString(int selector_type) {
		return switch (selector_type) {
		case 1 -> "usage_id";
		case 2 -> "Region name";
		case 3 -> "Wildcard";
		default -> "reserved for future use";
		};

	}

	public int getBouquet_id() {
		return bouquet_id;
	}

	public int getSelector_type() {
		return selector_type;
	}

	public int getUsage_id() {
		return usage_id;
	}

	public DVBString getRegion_name() {
		return region_name;
	}

	public byte[] getDatabyte() {
		return databyte;
	}

}
