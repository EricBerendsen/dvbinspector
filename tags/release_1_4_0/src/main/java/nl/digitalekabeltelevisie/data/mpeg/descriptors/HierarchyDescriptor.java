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

package nl.digitalekabeltelevisie.data.mpeg.descriptors;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.Utils;

public class HierarchyDescriptor extends Descriptor {
	
	// based on ITU-T Rec. H.222.0 (2000 E)/ ISO/IEC 13818-1 : 2000 (E) p 65 2.6.6 Hierarchy descriptor

	private int reserved1;
	private int hierarchy_type;
	private int reserved2;
	private int hierarchy_layer_index;
	private int reserved3;
	private int hierarchy_embedded_layer_index;
	private int reserved4;
	private int hierarchy_channel;


	public HierarchyDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset,parent);
		reserved1 = Utils.getInt(b, offset+2, 1, 0xf0)>>4;
		hierarchy_type = Utils.getInt(b, offset+2, 1, 0x0f);
		reserved2 = Utils.getInt(b, offset+3, 1, 0xb0)>>6;
		hierarchy_layer_index = Utils.getInt(b, offset+3, 1, 0x3F);
		reserved3 = Utils.getInt(b, offset+4, 1, 0xb0)>>6;
		hierarchy_embedded_layer_index = Utils.getInt(b, offset+4, 1, 0x3F);
		reserved4 = Utils.getInt(b, offset+5, 1, 0xb0)>>6;
		hierarchy_channel = Utils.getInt(b, offset+5, 1, 0x3F);


	}


	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("hierarchy_type",hierarchy_type ,getHierarchyTypeString(hierarchy_type))));
		t.add(new DefaultMutableTreeNode(new KVP("hierarchy_layer_index",hierarchy_layer_index ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("hierarchy_embedded_layer_index",hierarchy_embedded_layer_index ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("hierarchy_channel",hierarchy_channel ,null)));


		return t;
	}

	public static String getHierarchyTypeString(final int hierarchyType) {
		switch (hierarchyType) {
		case 0x00 : return "Reserved";
		case 0x01 : return "ITU-T Rec. H.262 | ISO/IEC 13818-2 Spatial Scalability";
		case 0x02 : return "ITU-T Rec. H.262 | ISO/IEC 13818-2 SNR Scalability";
		case 0x03 : return "ITU-T Rec. H.262 | ISO/IEC 13818-2 Temporal Scalability";
		case 0x04 : return "ITU-T Rec. H.262 | ISO/IEC 13818-2 Data partitioning";
		case 0x05 : return "ISO/IEC 13818-3 Extension bitstream";
		case 0x06 : return "ITU-T Rec.H222.0 | ISO/IEC 13818-1 Private Stream";
		case 0x07 : return "ITU-T Rec. H.262 | ISO/IEC 13818-2 Multi-view Profile";
		case 0x0f : return "Base layer";

		default:

			return "Reserved";

		}
	}


	public int getReserved1() {
		return reserved1;
	}


	public int getHierarchy_type() {
		return hierarchy_type;
	}


	public int getReserved2() {
		return reserved2;
	}


	public int getHierarchy_layer_index() {
		return hierarchy_layer_index;
	}


	public int getReserved3() {
		return reserved3;
	}


	public int getHierarchy_embedded_layer_index() {
		return hierarchy_embedded_layer_index;
	}


	public int getReserved4() {
		return reserved4;
	}


	public int getHierarchy_channel() {
		return hierarchy_channel;
	}

}
