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

import static java.util.Arrays.copyOfRange;

/**
 * @author Eric
 *
 */
public class BIOPMessage implements TreeNode {

	protected byte[] data;
	protected int offset;

	protected byte[] magic;
	protected int biop_version_major;
	protected int biop_version_minor;
	protected int byte_order;
	protected int message_type;
	protected long message_size;
	protected int objectKey_length;
	protected byte[] objectKey_data_byte;
	protected long objectKind_length;
	protected byte[] objectKind_data;
	protected int objectInfo_length;



	protected int byte_counter; // count bytes


	public BIOPMessage(byte[] data, int off) {
		this.data=data;
		this.offset= off;

		magic = copyOfRange(data, offset, offset + 4);

		biop_version_major = Utils.getInt(data, offset+4, 1, Utils.MASK_8BITS);
		biop_version_minor = Utils.getInt(data, offset+5, 1, Utils.MASK_8BITS);
		byte_order = Utils.getInt(data, offset+6, 1, Utils.MASK_8BITS);
		message_type = Utils.getInt(data, offset+7, 1, Utils.MASK_8BITS);
		message_size = Utils.getLong(data, offset+8, 4, Utils.MASK_32BITS);
		objectKey_length = Utils.getInt(data, offset+12, 1, Utils.MASK_8BITS);
		objectKey_data_byte = copyOfRange(data, offset + 13, offset + 13 + objectKey_length);
		byte_counter=offset+13+objectKey_length;
		objectKind_length = 4;// Utils.getLong(data,byte_counter, 4, Utils.MASK_32BITS); // should be 4
		byte_counter +=4;
		objectKind_data = copyOfRange(data, byte_counter, byte_counter + (int) objectKind_length);
		byte_counter += objectKind_length;
		objectInfo_length =  Utils.getInt(data, byte_counter, 2, Utils.MASK_16BITS);
		byte_counter +=2;

	}


	public KVP getJTreeNode(int modus, String label) {
		KVP t = new KVP(IOR.getTypeIdString(objectKind_data),label);
		t.add(new KVP("magic",magic ));
		t.add(new KVP("biop_version_major",biop_version_major ));
		t.add(new KVP("biop_version_minor",biop_version_minor ));
		t.add(new KVP("byte_order",byte_order ));
		t.add(new KVP("message_type",message_type ));
		t.add(new KVP("message_size",message_size ));
		t.add(new KVP("objectKey_length",objectKey_length ));
		t.add(new KVP("objectKey_data_byte",objectKey_data_byte ));
		t.add(new KVP("objectKind_length",objectKind_length ));
		t.add(new KVP("objectKind_data",objectKind_data ));
		t.add(new KVP("objectInfo_length",objectInfo_length ));
		return t;
	}


	public KVP getJTreeNode(int modus) {
		return getJTreeNode(modus,"");
	}



	public byte[] getData() {
		return data;
	}

	public int getOffset() {
		return offset;
	}

	public byte[] getMagic() {
		return magic;
	}

	public int getBiop_version_major() {
		return biop_version_major;
	}

	public int getBiop_version_minor() {
		return biop_version_minor;
	}

	public int getByte_order() {
		return byte_order;
	}

	public int getMessage_type() {
		return message_type;
	}

	public long getMessage_size() {
		return message_size;
	}

	public int getObjectKey_length() {
		return objectKey_length;
	}

	public byte[] getObjectKey_data_byte() {
		return objectKey_data_byte;
	}

	public long getObjectKind_length() {
		return objectKind_length;
	}

	public byte[] getObjectKind_data() {
		return objectKind_data;
	}


	public int getObjectInfo_length() {
		return objectInfo_length;
	}


}
