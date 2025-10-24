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
 * @author Asif Raza
 */


package nl.digitalekabeltelevisie.data.mpeg.descriptors;

import static nl.digitalekabeltelevisie.util.Utils.getBytes;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.Utils;

/*
 * Descriptor for the JPEG 2000 video as defined in
 * ISO/IEC 13818-1:2007/FPDAM5 - Transport of JPEG 2000 part 1 video
 */
public class JPEG2000VideoDescriptor extends Descriptor {

	private int profile_and_level;
	private int horizontal_size;
	private int vertical_size;
	private int max_bit_rate;
	private int max_buffer_size;

	private int DEN_frame_rate;
	private int NUM_frame_rate;
	private int color_specification;
	private int still_mode;
	private int interlaced_video;
	private int reserved;
	private byte[] private_data_byte = new byte[0];

	public JPEG2000VideoDescriptor(byte[] b, TableSection parent) {
		super(b, parent);
		int byteOffset = 2;
		profile_and_level = Utils.getInt(b, byteOffset, 2, Utils.MASK_16BITS);
		byteOffset += 2;

		horizontal_size = Utils.getInt(b, byteOffset, 4, Utils.MASK_32BITS);
		byteOffset += 4;

		vertical_size = Utils.getInt(b, byteOffset, 4, Utils.MASK_32BITS);
		byteOffset += 4;

		max_bit_rate = Utils.getInt(b, byteOffset, 4, Utils.MASK_32BITS);
		byteOffset += 4;

		max_buffer_size = Utils.getInt(b, byteOffset, 4, Utils.MASK_32BITS);
		byteOffset += 4;

		DEN_frame_rate = Utils.getInt(b, byteOffset, 2, Utils.MASK_16BITS);
		byteOffset += 2;

		NUM_frame_rate = Utils.getInt(b, byteOffset, 2, Utils.MASK_16BITS);
		byteOffset += 2;

		color_specification = Utils.getInt(b, byteOffset, 1, Utils.MASK_16BITS);
		byteOffset += 1;

		byte aByte = (byte)Utils.getInt(b, byteOffset, 1, Utils.MASK_16BITS);
		byteOffset += 1;

		still_mode = Utils.getBit(aByte, 1);
		interlaced_video = Utils.getBit(aByte, 2);
		reserved = Utils.getBits(aByte, 3, 6);

		if(byteOffset < descriptorLength)
		{
			private_data_byte = getBytes(b, byteOffset, descriptorLength-byteOffset);
		}
	}

	@Override
	public String toString() {
		return super.toString() + " profile_and_level"+profile_and_level ;
	}

	@Override
	public KVP getJTreeNode(int modus) {
		KVP t = super.getJTreeNode(modus);
		t.add(new KVP("profile_and_level", profile_and_level, getProfile_and_levelString(profile_and_level)));
		t.add(new KVP("horizontal_size", horizontal_size));
		t.add(new KVP("vertical_size", vertical_size));
		t.add(new KVP("max_bit_rate", max_bit_rate));
		t.add(new KVP("max_buffer_size", max_buffer_size));
		t.add(new KVP("DEN_frame_rate", DEN_frame_rate));
		t.add(new KVP("NUM_frame_rate", NUM_frame_rate));
		t.add(new KVP("color_specification", color_specification));
		t.add(new KVP("still_mode", still_mode));
		t.add(new KVP("interlaced_video", interlaced_video));
		t.add(new KVP("reserved", reserved));

		t.add(new KVP("private_data_byte", private_data_byte));
		return t;
	}

	public int getProfile_and_level() {
		return profile_and_level;
	}

	public static String getProfile_and_levelString(int profile_and_level) {
		return ""; ///TODO
	}


	public int getVertical_size() {
		return vertical_size;
	}

	public int getHorizontal_size() {
		return horizontal_size;
	}

	public int getNUM_frame_rate() {
		return NUM_frame_rate;
	}

	public int getMax_bit_rate() {
		return max_bit_rate;
	}

	public int getColor_specification() {
		return color_specification;
	}

	public int getDEN_frame_rate() {
		return DEN_frame_rate;
	}

	public int getStill_mode() {
		return still_mode;
	}

	public int getMax_buffer_size() {
		return max_buffer_size;
	}

	public int getInterlaced_video() {
		return interlaced_video;
	}

	public int getReserved() {
		return reserved;
	}

	public byte[] getPrivate_data_byte() {
		return private_data_byte;
	}

}
