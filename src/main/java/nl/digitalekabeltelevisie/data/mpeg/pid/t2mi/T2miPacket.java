/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2018 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.pid.t2mi;


import java.util.logging.Logger;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.*;
import nl.digitalekabeltelevisie.data.mpeg.CRCcheck;
import nl.digitalekabeltelevisie.util.*;


public class T2miPacket implements TreeNode {
	
	private static final Logger LOGGER = Logger.getLogger(T2miPacket.class.getName());

	private static LookUpList packet_type_list = new LookUpList.Builder().
			add(0x00 ,"Baseband Frame").
			add(0x01 ,"Auxiliary stream I/Q data").
			add(0x02 ,"Arbitrary cell insertion").
			add(0x10 ,"L1-current").
			add(0x11 ,"L1-future").
			add(0x12 ,"P2 bias balancing cells").
			add(0x20 ,"DVB-T2 timestamp").
			add(0x21 ,"Individual addressing").
			add(0x30 ,"FEF part: Null").
			add(0x31 ,"FEF part: I/Q data").
			add(0x32 ,"FEF part: composite").
			add(0x33 ,"FEF sub-part"). 
			build();
	
	
	// based on ETSI TS 102 773 V1.4.1 (2016-03)

	private byte[] data;

	private int packetNo;

	private Payload payload;

	public T2miPacket(byte[] result, int packetNo) {
		data = result;
		this.packetNo = packetNo;

		switch (getPacketType()) {
		case 0x00: // Baseband Frame

			payload = new BasebandFramePayload(data);
			break;
		case 0x10: // L1-current T2-MI packets
			payload = new L1CurrentT2MIPacketsPayload(data);
			break;
		case 0x20: // DVB-T2 timestamp
			payload = new DVBT2TimestampPayload(data);
			break;
		case 0x21: // Individual addressing
			payload = new IndividualAddressingPayload(data);
			break;
		default:
			payload = new Payload(data, getPacketTypeString(getPacketType()));
		}

	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(int modus) {
		DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("T2MI Packet ("+getPacketTypeString(getPacketType())+")"));
		t.add(new DefaultMutableTreeNode(new KVP("Start TS Packet No", packetNo, null)));
		t.add(new DefaultMutableTreeNode(new KVP("data", data, null)));
		t.add(new DefaultMutableTreeNode(
				new KVP("packet_type", getPacketType(), getPacketTypeString(getPacketType()))));
		t.add(new DefaultMutableTreeNode(new KVP("packet_count", getPacketCount(), null)));
		t.add(new DefaultMutableTreeNode(new KVP("superframe_idx", getSuperframeIdx(), null)));
		t.add(new DefaultMutableTreeNode(new KVP("t2mi_stream_id", getT2miStreamId(), null)));
		int payloadLenBits = getPayloadLen();
		t.add(new DefaultMutableTreeNode(new KVP("payload_len", payloadLenBits, getLenInBytes(payloadLenBits))));
		t.add(payload.getJTreeNode(modus));
		long crcResult = getCRCresult();
		String crcMsg = null;
		if (crcResult != 0) {
			LOGGER.warning("CRC check failed for t2mi, result:" + crcResult + ", packetNo:" + packetNo + ", packetType:"
					+ getPacketType());
			crcMsg = "CRC check failed!";
		}
		t.add(new DefaultMutableTreeNode(new KVP("crc32", data, data.length - 4, 4, crcMsg)));
		return t;
	}

	/**
	 * @return
	 */
	public long getCRCresult() {
		return CRCcheck.crc32(data, data.length);
	}

	/**
	 * @param payloadLenBits
	 * @return
	 */
	public static String getLenInBytes(int payloadLenBits) {
		return "bits = " + ((payloadLenBits + 7) / 8) + " Bytes";
	}

	public int getPacketType() {
		return Byte.toUnsignedInt(data[0]);
	}

	public static String getPacketTypeString(int type) {
		return packet_type_list.get(type);
	}

	public int getPacketCount() {
		return Byte.toUnsignedInt(data[1]);
	}

	public int getSuperframeIdx() {
		return Byte.toUnsignedInt(data[2]) >> 4;
	}

	public int getT2miStreamId() {
		return Byte.toUnsignedInt(data[4]) & Utils.MASK_3BITS;
	}

	public int getPayloadLen() {
		return 256 * Byte.toUnsignedInt(data[4]) + Byte.toUnsignedInt(data[5]);

	}

}
