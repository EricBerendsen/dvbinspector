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

import static java.util.Arrays.copyOfRange;
import static nl.digitalekabeltelevisie.util.Utils.*;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;


public class AssociationTagDescriptor extends Descriptor {

	//ETSI TR 101 202 V1.2.1
	//http://www.mhp-interactive.org/tutorials/dtv_intro/dsmcc/service_information

	private final int associationTag;
	private final int use;
	private long transactionId;
	private long timeout;
	private int selectorLength;
	private byte[] selectorByte;
	private final byte[] privateDataByte;

	public AssociationTagDescriptor(byte[] b, TableSection parent) {
		super(b, parent);
		associationTag = getInt(b, 2, 2, MASK_16BITS);
		use = getInt(b, 4, 2, MASK_16BITS);
		if (use == 0x0000) {
			selectorLength = getInt(b, 6, 1, MASK_8BITS); // 0x08;
			transactionId = getLong(b, 7, 4, MASK_32BITS);
			timeout = getLong(b, 11, 4, MASK_32BITS);
		} else if (use == 0x0001) { // General Object Carousel Data 13818-6 11.4.2
			selectorLength = getInt(b, 6, 1, MASK_8BITS); // 0x00;
		} else {
			selectorLength = getInt(b, 6, 1, MASK_8BITS);
			selectorByte = copyOfRange(b, 7, 7 + selectorLength);
		}
		privateDataByte = copyOfRange(b, 7 + selectorLength, 2 + descriptorLength);
	}


	@Override
	public String toString() {

		return super.toString() + "associationTag="+associationTag;
	}

	@Override
	public KVP getJTreeNode(int modus) {
		KVP t = super.getJTreeNode(modus);

		t.add(new KVP("association_tag", associationTag));
		t.add(new KVP("use", use, getUseString(use)));
		if (use == 0x0000) {
			t.add(new KVP("selector_length", selectorLength));
			t.add(new KVP("transaction_id", transactionId));
			t.add(new KVP("timeout", timeout));
		} else if (use == 0x0001) {
			t.add(new KVP("selector_length", selectorLength));
		} else {
			t.add(new KVP("selector_length", selectorLength));
			t.add(new KVP("id_selector_bytes", selectorByte));

		}
		t.add(new KVP("private_data_byte", privateDataByte));

		return t;
	}

	public int getAssociationTag() {
		return associationTag;
	}

	public int getUse() {
		return use;
	}

	// ETSI TR 101 202 V1.2.1 (2003-01) P.47 Table 4.18
	public static String getUseString(int use) {
		if (use == 0) {
			return "DSI with IOR of SGW";
		} else if (use == 1) {
			return "General Object Carousel Data";
		} else if ((use >= 0x100) && (use <= 0x1fff)) {
			return "DVB reserved";
		} else if ((use >= 0x2000) && (use <= 0xffff)) {
			return "user private";
		}
		return "unknown";
	}

	public long getTransactionId() {
		return transactionId;
	}

	public long getTimeout() {
		return timeout;
	}

	public int getSelectorLength() {
		return selectorLength;
	}

	public byte[] getSelectorByte() {
		return selectorByte;
	}

	public byte[] getPrivateDataByte() {
		return privateDataByte;
	}
}
