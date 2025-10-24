/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2025 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.pes.temi;

import static nl.digitalekabeltelevisie.util.Utils.getInt;

import java.util.ArrayList;
import java.util.List;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.PesPacketData;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.afdescriptors.AFDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.afdescriptors.AFDescriptorFactory;
import nl.digitalekabeltelevisie.util.Utils;

/**
 * 
 */
	public class TEMIPesDataField extends PesPacketData {

	private int crc_flag;

	private List<AFDescriptor> afDescriptors = new ArrayList<>();

	private long crc_32;



	public TEMIPesDataField(PesPacketData pesPacketData) {
		super(pesPacketData);
		int offset = pesPacketData.getPesDataStart();
		if(pesDataLen>0) { // PES packet with more than just header
			crc_flag = getInt(data, offset, 1, 0x80)>>>7; 
			int descriptorsLen = pesPacketData.getPesDataLen() - 1 - 4 * crc_flag;
			afDescriptors = AFDescriptorFactory.buildDescriptorList(data, offset+1, descriptorsLen);
			if(crc_flag == 1) {
				crc_32 = Utils.getLong(data, offset + descriptorsLen + 1 , 4, Utils.MASK_32BITS);
			}
		}
		
	}

	@Override
	public KVP getJTreeNode(int modus) {
		KVP s = getJTreeNode(modus, new KVP("TEMI PES Packet"));
		if (pesDataLen > 0) { // PES packet with more than just header
			s.add(new KVP("CRC_flag", crc_flag));
			s.addList(afDescriptors, modus, "af_descriptors");
			if (crc_flag == 1) {
				s.add(new KVP("CRC_32", crc_32));
			}
		}
		return s;
	}

	public int getCrc_flag() {
		return crc_flag;
	}

	public List<AFDescriptor> getAfDescriptors() {
		return afDescriptors;
	}

	public long getCrc_32() {
		return crc_32;
	}


}
