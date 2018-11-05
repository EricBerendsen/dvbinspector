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

import static nl.digitalekabeltelevisie.data.mpeg.pid.t2mi.T2miPacket.getLenInBytes;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.util.*;

public class BasebandFramePayload extends Payload {
	
	//based on ETSI EN 302 755 V1.4.1 (2015-07) and ETSI EN 302 307 V1.1.2 (2006-06)

	
	private static LookUpList stream_type_list = new LookUpList.Builder().
			add(0x00 ,"Generic Packetized Stream (GFPS)").
			add(0x01 ,"Generic Continuous Stream (GCS)").
			add(0x02 ,"Generic Encapsulated Stream (GSE)").
			add(0x03 ,"Transport Stream (TS)").
			build();


	public BasebandFramePayload(byte[] data) {
		super(data);

	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(int modus) {
		DefaultMutableTreeNode payloadNode = new DefaultMutableTreeNode(new KVP("payload"));
		payloadNode.add(new DefaultMutableTreeNode(new KVP("frame_idx", getFrameIdx(), null)));
		payloadNode.add(new DefaultMutableTreeNode(new KVP("plp_id", getPlpId(), null)));
		payloadNode.add(new DefaultMutableTreeNode(new KVP("intl_frame_start", getIntlFrameStart(), null)));
		
		DefaultMutableTreeNode bbframeNode = new DefaultMutableTreeNode(new KVP("BBFRAME"));
		
		DefaultMutableTreeNode matypeNode = new DefaultMutableTreeNode(new KVP("MATYPE", getMATYPE(), null));
		matypeNode.add(new DefaultMutableTreeNode(new KVP("StreamType TS/GS", getTSGS(), stream_type_list.get(getTSGS()))));
		matypeNode.add(new DefaultMutableTreeNode(new KVP("SingleInputStream SIS/MIS", getSISMIS(), getSISMIS()==1?"single":"multiple")));
		matypeNode.add(new DefaultMutableTreeNode(new KVP("ConstantCodingAndModulation CCM/ACM ", getCCMACM(), getCCMACM()==1?"CCM":"ACM")));
		matypeNode.add(new DefaultMutableTreeNode(new KVP("InputStreamSyncIndicator ISSYI", getISSYI(), getISSYI()==1?"active":"not-active")));
		matypeNode.add(new DefaultMutableTreeNode(new KVP("NullPacketDeletion NPD", getNPD(), getNPD()==1?"active":"not-active")));
		matypeNode.add(new DefaultMutableTreeNode(new KVP("RO", getRO(), null)));
		matypeNode.add(new DefaultMutableTreeNode(new KVP("MAT2 (ISI/PLP_ID)", getMATYPE2(), null)));
		bbframeNode.add(matypeNode);
		
		bbframeNode.add(new DefaultMutableTreeNode(new KVP("UPL", getUPL(), "User Packet Length "+getLenInBytes(getUPL()))));
		bbframeNode.add(new DefaultMutableTreeNode(new KVP("DFL", getDFL(), "Data Field Length "+getLenInBytes(getDFL()))));
		bbframeNode.add(new DefaultMutableTreeNode(new KVP("SYNC", getSYNC(), "copy of the User Packet Sync-byte")));
		bbframeNode.add(new DefaultMutableTreeNode(new KVP("SYNCD", getSYNCD(), "distance in bits from the beginning of the DATA FIELD and the first UP from this frame"+T2miPacket.getLenInBytes(getSYNCD()))));
		bbframeNode.add(new DefaultMutableTreeNode(new KVP("CRC-8 MODE", getCRC8(), null)));

		payloadNode.add(bbframeNode);

		return payloadNode;
	}

	public int getIntlFrameStart() {
		return (Byte.toUnsignedInt(data[8]) & 0b1000_0000) >> 7;
	}
	

	public int getMATYPE() {
		return Utils.getInt(data, 9, 2, Utils.MASK_16BITS);
	}

	
	public int getMATYPE1() {
		return Utils.getInt(data, 9, 1, Utils.MASK_8BITS);
	}
	
	public int getMATYPE2() {
		return Utils.getInt(data, 10, 1, Utils.MASK_8BITS);
	}

	
	public int getTSGS() {
		return (getMATYPE1() & 0b1100_0000)>>6;
	}

	public int getSISMIS() {
		return (getMATYPE1() & 0b0010_0000)>>5;
	}


	public int getCCMACM() {
		return (getMATYPE1() & 0b0001_0000)>>4;
	}

	
	public int getISSYI() {
		return (getMATYPE1() & 0b0000_1000)>>3;
	}

	
	public int getNPD() {
		return (getMATYPE1() & 0b0000_0100)>>2;
	}

	public int getRO() {
		return (getMATYPE1() & 0b0000_0011);
	}
	
	
	
	public int getUPL() {
		return Utils.getInt(data, 11, 2, Utils.MASK_16BITS);
	}

	public int getDFL() {
		return Utils.getInt(data, 13, 2, Utils.MASK_16BITS);
	}

	public int getSYNC() {
		return Utils.getInt(data, 15, 1, Utils.MASK_8BITS);
	}

	public int getSYNCD() {
		return Utils.getInt(data, 16, 2, Utils.MASK_16BITS);
	}

	public int getCRC8() {
		return Utils.getInt(data, 18, 1, Utils.MASK_8BITS);
	}

	public int getDFLinBytes() {
		return ((getDFL() + 7) / 8);
	}


}
