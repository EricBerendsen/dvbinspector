/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2023 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.descriptors.dsmcc;

import static nl.digitalekabeltelevisie.util.Utils.MASK_16BITS;
import static nl.digitalekabeltelevisie.util.Utils.getInt;

import java.util.Arrays;
import java.util.Base64;
import java.util.Base64.Decoder;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.DVBString;
import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.PsiSectionData;
import nl.digitalekabeltelevisie.data.mpeg.psi.SCTE35;
import nl.digitalekabeltelevisie.data.mpeg.psi.SpliceInfoSection;

import static nl.digitalekabeltelevisie.util.Utils.*;


/**
 * 
 */
public class DSMCCStreamEventPayloadBinary implements TreeNode {
	
	private byte[] binary;
	private int dvb_data_length;
	private int reserved_zero_future_use;
	private int event_type;
	private int timeline_type;
	private int temi_component_tag;
	private int temi_timeline_id;
	private byte[] reserved_zero_future_use2;
	private int private_data_length;
	private long private_data_specifier;
	private byte[] private_data_byte;
	private int carousel_object_name_length;
	private DVBString carousel_object_name;
	
	SpliceInfoSection scte35_section;

	public DSMCCStreamEventPayloadBinary(byte[] privateDataByte) {

		Decoder base64Decoder = Base64.getDecoder();
		binary = base64Decoder.decode(privateDataByte);

		if (binary.length > 0) {
			dvb_data_length = getInt(binary, 0, 1, MASK_8BITS);
			int byte1 = getInt(binary, 1, 1, MASK_8BITS);
			reserved_zero_future_use = (byte1 & 0b1110_0000) >> 5;
			event_type = (byte1 & 0b0001_0000) >> 4;
			timeline_type = (byte1 & 0b0000_1111);
			int offset = 2;

			if (timeline_type == 0x2) {
				temi_component_tag = getInt(binary, offset++, 1, MASK_8BITS);
				temi_timeline_id = getInt(binary, offset++, 1, MASK_8BITS);
			}
			if(dvb_data_length + 1 > offset) {
				reserved_zero_future_use2 = Arrays.copyOfRange(binary, offset, offset -1 - dvb_data_length);
				offset = dvb_data_length + 1;
			}
			
			private_data_length  = getInt(binary, offset++, 1, MASK_8BITS);
			
			if (private_data_length > 0) {
				private_data_specifier = getLong(binary, offset, 4, MASK_32BITS);
				offset +=4;
				private_data_byte= getBytes(binary, offset, private_data_length - 4);
			}
			
			if (event_type == 1) {
				carousel_object_name_length  = getInt(binary, offset++, 1, MASK_8BITS);
				carousel_object_name = new DVBString(binary, offset, carousel_object_name_length);
				offset += carousel_object_name_length;
				
			}
	
			
			if (event_type == 0) {
				PsiSectionData psiSectionData = new PsiSectionData(Arrays.copyOfRange(binary, offset, binary.length));
				scte35_section = new SpliceInfoSection(psiSectionData, null);
			}

		}

	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(int modus) {
		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("DSM-CC_stream_event_payload_binary"));
		t.add(new DefaultMutableTreeNode(new KVP("binary", binary, null)));
		if (binary.length > 0) {
			t.add(new DefaultMutableTreeNode(new KVP("DVB_data_length", dvb_data_length, null)));
			t.add(new DefaultMutableTreeNode(new KVP("reserved_zero_future_use", reserved_zero_future_use, null)));
			t.add(new DefaultMutableTreeNode(new KVP("event_type", event_type, null)));
			t.add(new DefaultMutableTreeNode(new KVP("timeline_type", timeline_type, null)));

			if (timeline_type == 0x2) {
				t.add(new DefaultMutableTreeNode(new KVP("temi_component_tag", temi_component_tag, null)));
				t.add(new DefaultMutableTreeNode(new KVP("temi_timeline_id", temi_timeline_id, null)));
			}
			if(reserved_zero_future_use2 != null) {
				t.add(new DefaultMutableTreeNode(new KVP("reserved_zero_future_use", reserved_zero_future_use2, null)));
			}
			t.add(new DefaultMutableTreeNode(new KVP("private_data_length", private_data_length, null)));
			
			if (private_data_length > 0) {
				t.add(new DefaultMutableTreeNode(new KVP("private_data_specifier", private_data_specifier, getPrivateDataSpecString(private_data_specifier))));
				t.add(new DefaultMutableTreeNode(new KVP("private_data_byte", private_data_byte, null)));
			}
			if (event_type == 1) {
				t.add(new DefaultMutableTreeNode(new KVP("carousel_object_name_length", carousel_object_name_length, null)));
				t.add(new DefaultMutableTreeNode(new KVP("carousel_object_name", carousel_object_name, null)));
			}
			if (event_type == 0) {
				t.add(scte35_section.getJTreeNode(2));
			}

		}
		return t;
	}

}
