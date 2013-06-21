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

public class Tap implements TreeNode{

	private final int id;
	private final int use;
	private final int association_tag;
	private final int selector_length;
	private int selector_type;
	private long transactionId;
	private long timeout;


	public Tap(final byte[] data, final int offset) {
		id=Utils.getInt(data, offset, 2, Utils.MASK_16BITS);
		use=Utils.getInt(data, offset+2, 2, Utils.MASK_16BITS);
		association_tag=Utils.getInt(data, offset+4, 2, Utils.MASK_16BITS);
		selector_length=Utils.getInt(data, offset+6, 1, Utils.MASK_8BITS);
		// these fields are only used for the first tap in DSMConnBinder. P.32 ETSI TR 101 202 V1.2.1
		if(use==0x0016){ // BIOP_DELIVERY_PARA_USE
			selector_type=Utils.getInt(data, offset+7, 2, Utils.MASK_16BITS);
			transactionId=Utils.getLong(data, offset+9, 4, Utils.MASK_32BITS);
			timeout=Utils.getLong(data, offset+13, 4, Utils.MASK_32BITS);
		}

	}


	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP(
				"BIOP::Tap"));
		t.add(new DefaultMutableTreeNode(new KVP("id",id ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("use",use ,getUseString(use))));
		t.add(new DefaultMutableTreeNode(new KVP("association_tag",association_tag ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("selector_length",selector_length ,null)));
		if(use==0x0016){ // BIOP_DELIVERY_PARA_USE
			t.add(new DefaultMutableTreeNode(new KVP("selector_type",selector_type ,null)));
			t.add(new DefaultMutableTreeNode(new KVP("transactionId",transactionId ,null)));
			t.add(new DefaultMutableTreeNode(new KVP("timeout",timeout ,null)));
		}
		return t;
	}

	// based on char *dsmccStrBIOP_TAP_Use (u_int id)  in dsmcc_str.c of DVBSnoop
	public static String getUseString(final int use){
		switch (use) {
		case 0x0000: return   "UNKNOWN";
		case 0x0001: return   "MPEG_TS_UP_USE";
		case 0x0002: return   "MPEG_TS_DOWN_USE";
		case 0x0003: return   "MPEG_ES_UP_USE";
		case 0x0004: return   "MPEG_ES_DOWN_USE";
		case 0x0005: return   "DOWNLOAD_CTRL_USE";
		case 0x0006: return   "DOWNLOAD_CTRL_UP_USE";
		case 0x0007: return   "DOWNLOAD_CTRL_DOWN_USE";
		case 0x0008: return   "DOWNLOAD_DATA_USE";
		case 0x0009: return   "DOWNLOAD_DATA_UP_USE";
		case 0x000A: return   "DOWNLOAD_DATA_DOWN_USE";
		case 0x000B: return   "STR_NPT_USE";
		case 0x000C: return   "STR_STATUS_AND_EVENT_USE";
		case 0x000D: return   "STR_EVENT_USE";
		case 0x000E: return   "STR_STATUS_USE";
		case 0x000F: return   "RPC_USE";
		case 0x0010: return   "IP_USE";
		case 0x0011: return   "SDB_CTRL_USE";
		case 0x0015: return   "T120_TAP reserved";
		case 0x0016: return   "BIOP_DELIVERY_PARA_USE";
		case 0x0017: return   "BIOP_OBJECT_USE";
		case 0x0018: return   "BIOP_ES_USE";
		case 0x0019: return   "BIOP_PROGRAM_USE";
		case 0x001A: return   "BIOP_DNL_CTRL_USE";

		default:
			return "unknown";
		}
	}


	public int getId() {
		return id;
	}


	public int getUse() {
		return use;
	}


	public int getAssociation_tag() {
		return association_tag;
	}


	public int getSelector_length() {
		return selector_length;
	}


	public int getSelector_type() {
		return selector_type;
	}


	public long getTransactionId() {
		return transactionId;
	}


	public long getTimeout() {
		return timeout;
	}



}