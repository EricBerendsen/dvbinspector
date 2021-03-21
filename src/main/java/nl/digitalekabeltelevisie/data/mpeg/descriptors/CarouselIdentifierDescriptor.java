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

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.Utils;

public class CarouselIdentifierDescriptor extends Descriptor {

	// TS 102 809 V1.1.1 Ch.  B.2.8.1 carousel_identifier_descriptor

	private final long carouselId;
	private final int formatId;

	private byte[] privateDataByte;

	private int moduleVersion;
	private int moduleId;
	private int blockSize;
	private long moduleSize;
	private int compressionMethod;
	private long originalSize;
	private int timeOut;
	private int objectKeyLength;

	private byte[] objectKeyData;

	public CarouselIdentifierDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset,parent);
		carouselId = Utils.getLong(b, offset+2, 4, Utils.MASK_32BITS);
		formatId = Utils.getInt(b, offset+6, 1, Utils.MASK_8BITS);
		if(formatId==0x00){
			privateDataByte = copyOfRange(b, offset+7, offset+descriptorLength+2);
		}
		if(formatId==0x01){
			moduleVersion=Utils.getInt(b, offset+7, 1, Utils.MASK_8BITS);
			moduleId=Utils.getInt(b, offset+8, 2, Utils.MASK_16BITS);
			blockSize = Utils.getInt(b, offset+10, 2, Utils.MASK_16BITS);
			moduleSize  = Utils.getLong(b, offset+12, 4, Utils.MASK_32BITS);
			compressionMethod = Utils.getInt(b, offset+16, 1, Utils.MASK_8BITS);
			originalSize = Utils.getLong(b, offset+17, 4, Utils.MASK_32BITS);
			timeOut = Utils.getInt(b, offset+21, 1, Utils.MASK_8BITS);
			objectKeyLength = Utils.getInt(b, offset+22, 1, Utils.MASK_8BITS);
			objectKeyData = copyOfRange(b, offset+23, offset+23+objectKeyLength);
			privateDataByte = copyOfRange(b, offset+23+objectKeyLength, offset+descriptorLength+2);
		}

	}



	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){

		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("carousel_id",carouselId,null)));
		t.add(new DefaultMutableTreeNode(new KVP("format_id",formatId,getFormatIDString(formatId))));
		if(formatId==0x00){
			t.add(new DefaultMutableTreeNode(new KVP("private_data_byte",privateDataByte,null)));
		}
		if(formatId==0x01){
			t.add(new DefaultMutableTreeNode(new KVP("module_version",moduleVersion,null)));
			t.add(new DefaultMutableTreeNode(new KVP("module_id",moduleId,null)));
			t.add(new DefaultMutableTreeNode(new KVP("block_size",blockSize,null)));
			t.add(new DefaultMutableTreeNode(new KVP("module_size",moduleSize,null)));
			t.add(new DefaultMutableTreeNode(new KVP("compression_method",compressionMethod,null)));
			t.add(new DefaultMutableTreeNode(new KVP("Original_size",originalSize,null)));
			t.add(new DefaultMutableTreeNode(new KVP("time_out",timeOut,null)));
			t.add(new DefaultMutableTreeNode(new KVP("object_key_length",objectKeyLength,null)));
			t.add(new DefaultMutableTreeNode(new KVP("object_key_data",objectKeyData,null)));
			t.add(new DefaultMutableTreeNode(new KVP("private_data_byte",privateDataByte,null)));
		}
		return t;
	}

	public static String getFormatIDString(final int formatID) {
		switch (formatID) {
		case 0x00 : return "standard boot";
		case 0x01 : return "enhanced boot";

		default:

			return "unknown value";

		}
	}



	public long getCarouselId() {
		return carouselId;
	}



	public int getFormatId() {
		return formatId;
	}



	public byte[] getPrivateDataByte() {
		return privateDataByte;
	}



	public int getModuleVersion() {
		return moduleVersion;
	}



	public int getModuleId() {
		return moduleId;
	}



	public int getBlockSize() {
		return blockSize;
	}



	public long getModuleSize() {
		return moduleSize;
	}



	public int getCompressionMethod() {
		return compressionMethod;
	}



	public long getOriginalSize() {
		return originalSize;
	}



	public int getTimeOut() {
		return timeOut;
	}



	public int getObjectKeyLength() {
		return objectKeyLength;
	}



	public byte[] getObjectKeyData() {
		return objectKeyData;
	}

}
