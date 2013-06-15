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


	public BIOPMessage(final byte[] data, final int off) {
		this.data=data;
		this.offset= off;

		magic = Utils.copyOfRange(data,offset,offset+4);

		biop_version_major = Utils.getInt(data, offset+4, 1, Utils.MASK_8BITS);
		biop_version_minor = Utils.getInt(data, offset+5, 1, Utils.MASK_8BITS);
		byte_order = Utils.getInt(data, offset+6, 1, Utils.MASK_8BITS);
		message_type = Utils.getInt(data, offset+7, 1, Utils.MASK_8BITS);
		message_size = Utils.getLong(data, offset+8, 4, Utils.MASK_32BITS);
		objectKey_length = Utils.getInt(data, offset+12, 1, Utils.MASK_8BITS);
		objectKey_data_byte = Utils.copyOfRange(data,offset+13,offset+13+objectKey_length);
		byte_counter=offset+13+objectKey_length;
		objectKind_length = 4;// Utils.getLong(data,byte_counter, 4, Utils.MASK_32BITS); // should be 4
		byte_counter +=4;
		objectKind_data = Utils.copyOfRange(data,byte_counter,byte_counter+(int)objectKind_length);
		byte_counter += objectKind_length;
		objectInfo_length =  Utils.getInt(data, byte_counter, 2, Utils.MASK_16BITS);
		byte_counter +=2;

	}

	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.controller.TreeNode#getJTreeNode(int)
	 */
	public DefaultMutableTreeNode getJTreeNode(final int modus, final String label) {
		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP(IOR.getTypeIdString(objectKind_data),label,null));
		t.add(new DefaultMutableTreeNode(new KVP("magic",magic ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("biop_version_major",biop_version_major ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("biop_version_minor",biop_version_minor ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("byte_order",byte_order ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("message_type",message_type ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("message_size",message_size ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("objectKey_length",objectKey_length ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("objectKey_data_byte",objectKey_data_byte ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("objectKind_length",objectKind_length ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("objectKind_data",objectKind_data ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("objectInfo_length",objectInfo_length ,null)));
		return t;
	}


	public DefaultMutableTreeNode getJTreeNode(final int modus) {
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
