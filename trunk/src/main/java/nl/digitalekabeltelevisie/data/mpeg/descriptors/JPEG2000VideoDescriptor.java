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

import javax.swing.tree.DefaultMutableTreeNode;

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

	public JPEG2000VideoDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset,parent);
		int byteOffset = offset+2;
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

		final byte aByte = (byte)Utils.getInt(b, byteOffset, 1, Utils.MASK_16BITS);
		byteOffset += 1;

		still_mode = Utils.getBit(aByte, 1);
		setInterlaced_video(still_mode = Utils.getBit(aByte, 2));
		setReserved(Utils.getBits(aByte, 3, 6));

		if(byteOffset < descriptorLength)
		{
			setPrivate_data_byte(getBytes(b, byteOffset, descriptorLength-byteOffset));
		}
	}

	@Override
	public String toString() {
		return super.toString() + " profile_and_level"+profile_and_level ;
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("profile_and_level",profile_and_level ,getProfile_and_levelString(profile_and_level))));
		t.add(new DefaultMutableTreeNode(new KVP("horizontal_size",horizontal_size ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("vertical_size",vertical_size ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("max_bit_rate",max_bit_rate ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("max_buffer_size",max_buffer_size ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("DEN_frame_rate",DEN_frame_rate ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("NUM_frame_rate",NUM_frame_rate ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("color_specification",color_specification ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("still_mode",still_mode ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("interlaced_video",interlaced_video ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("reserved",reserved ,null)));

		t.add(new DefaultMutableTreeNode(new KVP("private_data_byte",private_data_byte ,null)));
		return t;
	}

	public int getProfile_and_level() {
		return profile_and_level;
	}

	public static String getProfile_and_levelString(final int profile_and_level) {
		return ""; ///TODO
	}


	public void setProfile_and_level(final int serviceType) {
		this.profile_and_level = serviceType;
	}

	public int getVertical_size() {
		return vertical_size;
	}

	public void setVertical_size(final int serviceNameLength) {
		this.vertical_size = serviceNameLength;
	}

	public int getHorizontal_size() {
		return horizontal_size;
	}

	public void setHorizontal_size(final int serviceProviderNameLength) {
		this.horizontal_size = serviceProviderNameLength;
	}

	public int getNUM_frame_rate() {
		return NUM_frame_rate;
	}

	public void setNUM_frame_rate(final int NUM_frame_rate) {
		this.NUM_frame_rate = NUM_frame_rate;
	}

	public int getMax_bit_rate() {
		return max_bit_rate;
	}

	public void setMax_bit_rate(final int max_bit_rate) {
		this.max_bit_rate = max_bit_rate;
	}

	public int getColor_specification() {
		return color_specification;
	}

	public void setColor_specification(final int color_specification) {
		this.color_specification = color_specification;
	}

	public int getDEN_frame_rate() {
		return DEN_frame_rate;
	}

	public void setDEN_frame_rate(final int DEN_frame_rate) {
		this.DEN_frame_rate = DEN_frame_rate;
	}

	public int getStill_mode() {
		return still_mode;
	}

	public void setStill_mode(final int still_mode) {
		this.still_mode = still_mode;
	}

	public int getMax_buffer_size() {
		return max_buffer_size;
	}

	public void setMax_buffer_size(final int max_buffer_size) {
		this.max_buffer_size = max_buffer_size;
	}

	public int getInterlaced_video() {
		return interlaced_video;
	}

	public void setInterlaced_video(int interlaced_video) {
		this.interlaced_video = interlaced_video;
	}

	public int getReserved() {
		return reserved;
	}

	public void setReserved(int reserved) {
		this.reserved = reserved;
	}

	public byte[] getPrivate_data_byte() {
		return private_data_byte;
	}

	public void setPrivate_data_byte(byte[] private_data_byte) {
		this.private_data_byte = private_data_byte;
	}

}
