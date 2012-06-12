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

import static nl.digitalekabeltelevisie.util.Utils.*;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

public class CADescriptor extends Descriptor {

	private int caSystemID;
	private int caPID;
	private byte[] privateDataByte;

	public CADescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset,parent);
		caSystemID = getInt(b,offset+2,2,0xFFFF);
		caPID = getInt(b,offset+4,2,0x1FFF);
		privateDataByte = copyOfRange(b, offset+6, offset+descriptorLength+2);
	}

	@Override
	public String toString() {
		return super.toString() + "caSystemID="+caSystemID;
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("CA_system_ID",caSystemID ,getCASystemIDString(caSystemID))));
		t.add(new DefaultMutableTreeNode(new KVP("CA_PID",caPID ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("private_data_byte",privateDataByte ,null)));
		return t;
	}

	public int getCaPID() {
		return caPID;
	}


	//	public static String getcaSystemIDString(int id) {
	//		if (id == 0x0000) {
	//			return "Reserved";
	//		} else if ((0x0001 <= id) && (id <= 0x00FF)) {
	//			return "Standardized systems";
	//		} else if ((0x0100 <= id) && (id <= 0x01FF)) {
	//			return "Canal Plus";
	//		} else if ((0x0200 <= id) && (id <= 0x02FF)) {
	//			return "CCETT";
	//		} else if ((0x0300 <= id) && (id <= 0x03FF)) {
	//			return "Deutsche Telecom";
	//		} else if ((0x0400 <= id) && (id <= 0x04FF)) {
	//			return "Eurodec";
	//		} else if ((0x0500 <= id) && (id <= 0x05FF)) {
	//			return "France Telecom";
	//		} else if ((0x0600 <= id) && (id <= 0x06FF)) {
	//			return "Irdeto";
	//		} else if ((0x0700 <= id) && (id <= 0x07FF)) {
	//			return "Jerrold/GI";
	//		} else if ((0x0800 <= id) && (id <= 0x08FF)) {
	//			return "Matra Communication";
	//		} else if ((0x0900 <= id) && (id <= 0x09FF)) {
	//			return "News Datacom";
	//		} else if ((0x0A00 <= id) && (id <= 0x0AFF)) {
	//			return "Nokia";
	//		} else if ((0x0B00 <= id) && (id <= 0x0BFF)) {
	//			return "Norwegian Telekom";
	//		} else if ((0x0C00 <= id) && (id <= 0x0CFF)) {
	//			return "NTL";
	//		} else if ((0x0D00 <= id) && (id <= 0x0DFF)) {
	//			return "Philips";
	//		} else if ((0x0E00 <= id) && (id <= 0x0EFF)) {
	//			return "Scientific Atlanta";
	//		} else if ((0x0F00 <= id) && (id <= 0x0FFF)) {
	//			return "Sony";
	//		} else if ((0x1000 <= id) && (id <= 0x10FF)) {
	//			return "Tandberg Television";
	//		} else if ((0x1100 <= id) && (id <= 0x11FF)) {
	//			return "Thomson";
	//		} else if ((0x1200 <= id) && (id <= 0x12FF)) {
	//			return "TV/Com";
	//		} else if ((0x1300 <= id) && (id <= 0x13FF)) {
	//			return "HPT - Croatian Post and Telecommunications";
	//		} else if ((0x1400 <= id) && (id <= 0x14FF)) {
	//			return "HRT - Croatian Radio and Television";
	//		} else if ((0x1500 <= id) && (id <= 0x15FF)) {
	//			return "IBM";
	//		} else if ((0x1600 <= id) && (id <= 0x16FF)) {
	//			return "Nera";
	//		} else if ((0x1700 <= id) && (id <= 0x17FF)) {
	//			return "BetaTechnik";
	//
	//		// source DVBsnoop 1.4.5 dvb_str.c
	//
	//		} else if ((0x1800 <= id) && (id <= 0x18FF)) {
	//			return "Kudelski SA";
	//		} else if ((0x1900 <= id) && (id <= 0x19FF)) {
	//			return "Titan Information Systems";
	//		} else if ((0x2000 <= id) && (id <= 0x20FF)) {
	//			return "Telefónica Servicios Audiovisuales";
	//		} else if ((0x2100 <= id) && (id <= 0x21FF)) {
	//			return "STENTOR (France Telecom, CNES and DGA)";
	//		} else if ((0x2200 <= id) && (id <= 0x22FF)) {
	//			return "Scopus Network Technologies";
	//		} else if ((0x2300 <= id) && (id <= 0x23FF)) {
	//			return "BARCO AS";
	//		} else if ((0x2400 <= id) && (id <= 0x24FF)) {
	//			return "StarGuide Digital Networks  ";
	//		} else if ((0x2500 <= id) && (id <= 0x25FF)) {
	//			return "Mentor Data System, Inc.";
	//		} else if ((0x2600 <= id) && (id <= 0x26FF)) {
	//			return "European Broadcasting Union";
	//		} else if ((0x4700 <= id) && (id <= 0x47FF)) {
	//			return "General Instrument";
	//		} else if ((0x4800 <= id) && (id <= 0x48FF)) {
	//			return "Telemann";
	//		} else if ((0x4900 <= id) && (id <= 0x49FF)) {
	//			return "Digital TV Industry Alliance of China";
	//		} else if ((0x4A00 <= id) && (id <= 0x4A0F)) {
	//			return "Tsinghua TongFang";
	//		} else if ((0x4A10 <= id) && (id <= 0x4A1F)) {
	//			return "Easycas";
	//		} else if ((0x4A20 <= id) && (id <= 0x4A2F)) {
	//			return "AlphaCrypt";
	//		} else if ((0x4A30 <= id) && (id <= 0x4A3F)) {
	//			return "DVN Holdings";
	//		} else if ((0x4A40 <= id) && (id <= 0x4A4F)) {
	//			return "Shanghai Advanced Digital Technology Co. Ltd. (ADT)";
	//		} else if ((0x4A50 <= id) && (id <= 0x4A5F)) {
	//			return "Shenzhen Kingsky Company (China) Ltd";
	//		} else if ((0x4A60 <= id) && (id <= 0x4A6F)) {
	//			return "@SKY";
	//		} else if ((0x4A70 <= id) && (id <= 0x4A7F)) {
	//			return "DreamCrypt";
	//		} else if ((0x4A80 <= id) && (id <= 0x4A8F)) {
	//			return "THALESCrypt";
	//		} else if ((0x4A90 <= id) && (id <= 0x4A9F)) {
	//			return "Runcom Technologies";
	//		} else if ((0x4AA0 <= id) && (id <= 0x4AAF)) {
	//			return "SIDSA";
	//		} else if ((0x4AB0 <= id) && (id <= 0x4ABF)) {
	//			return "Beijing Compunicate Technology Inc.";
	//		} else if ((0x4AC0 <= id) && (id <= 0x4ACF)) {
	//			return "Latens Systems Ltd";
	//
	//		} else {
	//			return "Illegal Value";
	//		}
	//	}

	public void setCaPID(final int caPID) {
		this.caPID = caPID;
	}

	public int getCaSystemID() {
		return caSystemID;
	}

	public void setCaSystemID(final int caSystemID) {
		this.caSystemID = caSystemID;
	}

	public byte[] getPrivateDataByte() {
		return privateDataByte;
	}

	public void setPrivateDataByte(final byte[] privateDataByte) {
		this.privateDataByte = privateDataByte;
	}

}
