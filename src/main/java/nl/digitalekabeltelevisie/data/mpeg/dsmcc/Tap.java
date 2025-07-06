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

public class Tap implements TreeNode{

	private final int id;
	private final int use;
	private final int association_tag;
	private final int selector_length;
	private int selector_type;
	private long transactionId;
	private long timeout;


	public Tap(byte[] data, int offset) {
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


	public KVP getJTreeNode(int modus) {
		KVP t = new KVP("BIOP::Tap");
		t.add(new KVP("id",id));
		t.add(new KVP("use",use ,getUseString(use)));
		t.add(new KVP("association_tag",association_tag));
		t.add(new KVP("selector_length",selector_length));
		if(use==0x0016){ // BIOP_DELIVERY_PARA_USE
			t.add(new KVP("selector_type",selector_type));
			t.add(new KVP("transactionId",transactionId ,DSMCC_UNMessageSection.getTransactionIDString(transactionId)));
			t.add(new KVP("timeout",timeout));
		}
		return t;
	}

	// based on char *dsmccStrBIOP_TAP_Use (u_int id)  in dsmcc_str.c of DVBSnoop
	public static String getUseString(int use){
        return switch (use) {
            case 0x0000 -> "UNKNOWN";
            case 0x0001 -> "MPEG_TS_UP_USE";
            case 0x0002 -> "MPEG_TS_DOWN_USE";
            case 0x0003 -> "MPEG_ES_UP_USE";
            case 0x0004 -> "MPEG_ES_DOWN_USE";
            case 0x0005 -> "DOWNLOAD_CTRL_USE";
            case 0x0006 -> "DOWNLOAD_CTRL_UP_USE";
            case 0x0007 -> "DOWNLOAD_CTRL_DOWN_USE";
            case 0x0008 -> "DOWNLOAD_DATA_USE";
            case 0x0009 -> "DOWNLOAD_DATA_UP_USE";
            case 0x000A -> "DOWNLOAD_DATA_DOWN_USE";
            case 0x000B -> "STR_NPT_USE";
            case 0x000C -> "STR_STATUS_AND_EVENT_USE";
            case 0x000D -> "STR_EVENT_USE";
            case 0x000E -> "STR_STATUS_USE";
            case 0x000F -> "RPC_USE";
            case 0x0010 -> "IP_USE";
            case 0x0011 -> "SDB_CTRL_USE";
            case 0x0015 -> "T120_TAP reserved";
            case 0x0016 -> "BIOP_DELIVERY_PARA_USE";
            case 0x0017 -> "BIOP_OBJECT_USE";
            case 0x0018 -> "BIOP_ES_USE";
            case 0x0019 -> "BIOP_PROGRAM_USE";
            case 0x001A -> "BIOP_DNL_CTRL_USE";
            default -> "unknown";
        };
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