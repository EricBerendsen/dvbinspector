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

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.Utils;

public class CarouselIdentifierDescriptor extends Descriptor {

	// TS 102 809 V1.1.1 Ch.  B.2.8.1 carousel_identifier_descriptor

	private final long carouselId;
	private int formatId;

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

	public CarouselIdentifierDescriptor(final byte[] b, final TableSection parent) {
		super(b, parent);
		carouselId = Utils.getLong(b, 2, 4, Utils.MASK_32BITS);
		if (descriptorLength > 4) {
			formatId = Utils.getInt(b, 6, 1, Utils.MASK_8BITS);
			if (formatId == 0x00) {
				privateDataByte = copyOfRange(b, 7, descriptorLength + 2);
			}
			if (formatId == 0x01) {
				moduleVersion = Utils.getInt(b, 7, 1, Utils.MASK_8BITS);
				moduleId = Utils.getInt(b, 8, 2, Utils.MASK_16BITS);
				blockSize = Utils.getInt(b, 10, 2, Utils.MASK_16BITS);
				moduleSize = Utils.getLong(b, 12, 4, Utils.MASK_32BITS);
				compressionMethod = Utils.getInt(b, 16, 1, Utils.MASK_8BITS);
				originalSize = Utils.getLong(b, 17, 4, Utils.MASK_32BITS);
				timeOut = Utils.getInt(b, 21, 1, Utils.MASK_8BITS);
				objectKeyLength = Utils.getInt(b, 22, 1, Utils.MASK_8BITS);
				objectKeyData = copyOfRange(b, 23, 23 + objectKeyLength);
				privateDataByte = copyOfRange(b, 23 + objectKeyLength, descriptorLength + 2);
			}
		}

	}

	@Override
	public KVP getJTreeNode(final int modus) {

		final KVP t = (KVP) super.getJTreeNode(modus);
		t.add(new KVP("carousel_id", carouselId));
		if (descriptorLength > 4) {
			t.add(new KVP("format_id", formatId, getFormatIDString(formatId)));
			if (formatId == 0x00) {
				t.add(new KVP("private_data_byte", privateDataByte));
			}
			if (formatId == 0x01) {
				t.add(new KVP("module_version", moduleVersion));
				t.add(new KVP("module_id", moduleId));
				t.add(new KVP("block_size", blockSize));
				t.add(new KVP("module_size", moduleSize));
				t.add(new KVP("compression_method", compressionMethod));
				t.add(new KVP("Original_size", originalSize));
				t.add(new KVP("time_out", timeOut));
				t.add(new KVP("object_key_length", objectKeyLength));
				t.add(new KVP("object_key_data", objectKeyData));
				t.add(new KVP("private_data_byte", privateDataByte));
			}
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
