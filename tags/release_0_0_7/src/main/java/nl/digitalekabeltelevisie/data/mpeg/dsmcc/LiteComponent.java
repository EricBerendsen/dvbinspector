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

package nl.digitalekabeltelevisie.data.mpeg.dsmcc;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.util.Utils;

public class LiteComponent implements TreeNode {

	protected long component_tag;
	protected int component_data_length;

	public LiteComponent(final byte[] data, final int offset) {
		component_tag = Utils.getLong(data, offset, 4, Utils.MASK_32BITS);
		component_data_length = Utils.getInt(data, offset+4, 1, Utils.MASK_8BITS);

	}

	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP(
				"LiteComponent"));
		t.add(new DefaultMutableTreeNode(new KVP("component_tag",component_tag ,getComponentTagString((int)component_tag))));
		t.add(new DefaultMutableTreeNode(new KVP("component_data_length",component_data_length ,null)));
		return t;
	}

	public static String getComponentTagString(final int tag){
		// uses int instead of long. taf = 32 bits, should fit but could result in negative values
		// based on char *dsmccStrIOP_ProfileID (u_int id) in dsmcc_str.c of DVBSnoop
		switch (tag) {
		case 0x49534f00: return   "TAG_MIN";
		case 0x49534f01: return   "TAG_CHILD";
		case 0x49534f02: return   "TAG_OPTIONS";
		case 0x49534f03: return   "TAG_LITE_MIN";
		case 0x49534f04: return   "TAG_LITE_CHILD";
		case 0x49534f05: return   "TAG_LITE_OPTIONS";
		case 0x49534f06: return   "TAG_BIOP";
		case 0x49534f07: return   "TAG_ONC";
		case 0x49534f40: return   "TAG_ConnBinder";
		case 0x49534f41: return   "TAG_IIOPAddr";
		case 0x49534f42: return   "TAG_Addr";
		case 0x49534f43: return   "TAG_NameId";
		case 0x49534f44: return   "TAG_IntfCode";
		case 0x49534f45: return   "TAG_ObjectKey";
		case 0x49534f46: return   "TAG_ServiceLocation";
		case 0x49534f50: return   "TAG_ObjectLocation";
		case 0x49534f58: return   "TAG_Intf";

		default:
			return "unknown";
		}

	}

	public long getComponentTag() {
		return component_tag;
	}

	public int getComponentDataLength() {
		return component_data_length;
	}

}
