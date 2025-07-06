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

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.util.Utils;

public class LiteComponent implements TreeNode {

	protected long component_tag;
	protected int component_data_length;

	public LiteComponent(byte[] data, int offset) {
		component_tag = Utils.getLong(data, offset, 4, Utils.MASK_32BITS);
		component_data_length = Utils.getInt(data, offset+4, 1, Utils.MASK_8BITS);

	}

	public KVP getJTreeNode(int modus) {
		KVP t = new KVP("LiteComponent");
		t.add(new KVP("component_tag",component_tag ,getComponentTagString((int)component_tag)));
		t.add(new KVP("component_data_length",component_data_length));
		return t;
	}

	public static String getComponentTagString(int tag){
		// uses int instead of long. taf = 32 bits, should fit but could result in negative values
		// based on char *dsmccStrIOP_ProfileID (u_int id) in dsmcc_str.c of DVBSnoop
        return switch (tag) {
            case 0x49534f00 -> "TAG_MIN";
            case 0x49534f01 -> "TAG_CHILD";
            case 0x49534f02 -> "TAG_OPTIONS";
            case 0x49534f03 -> "TAG_LITE_MIN";
            case 0x49534f04 -> "TAG_LITE_CHILD";
            case 0x49534f05 -> "TAG_LITE_OPTIONS";
            case 0x49534f06 -> "TAG_BIOP";
            case 0x49534f07 -> "TAG_ONC";
            case 0x49534f40 -> "TAG_ConnBinder";
            case 0x49534f41 -> "TAG_IIOPAddr";
            case 0x49534f42 -> "TAG_Addr";
            case 0x49534f43 -> "TAG_NameId";
            case 0x49534f44 -> "TAG_IntfCode";
            case 0x49534f45 -> "TAG_ObjectKey";
            case 0x49534f46 -> "TAG_ServiceLocation";
            case 0x49534f50 -> "TAG_ObjectLocation";
            case 0x49534f58 -> "TAG_Intf";
            default -> "unknown";
        };

	}

	public long getComponentTag() {
		return component_tag;
	}

	public int getComponentDataLength() {
		return component_data_length;
	}

}
