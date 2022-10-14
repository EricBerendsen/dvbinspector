/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2022 by Paul Higgs (paul_higgs@hotmail.com)
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

package nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.avs;

import static nl.digitalekabeltelevisie.util.Utils.MASK_10BITS;
import static nl.digitalekabeltelevisie.util.Utils.MASK_16BITS;
import static nl.digitalekabeltelevisie.util.Utils.MASK_8BITS;
import static nl.digitalekabeltelevisie.util.Utils.addListJTree;
import static nl.digitalekabeltelevisie.util.Utils.getISO8859_1String;
import static nl.digitalekabeltelevisie.util.Utils.getInt;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.DVBString;
import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.DescriptorContext;
//import nl.digitalekabeltelevisie.data.mpeg.descriptors.logicalchannel.AbstractLogicalChannelDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;

//public class AVS3VideoDescriptor extends AbstractLogicalChannelDescriptor {
public class AVS3VideoDescriptor extends Descriptor {

	private final int profile_id;						// 8 bits
	private final int level_id;							// 8 bits
	private final int multiple_frame_rate_flag;			// 1 bit
	private final int frame_rate_code;					// 4 bits
	private final int sample_precision;					// 3 bits
	private final int chroma_format;					// 2 bits
	private final int temporal_id_flag;					// 1 bit
	private final int td_mode_flag;						// 1 bit
	private final int library_stream_flag;				// 1 bit
	private final int library_picture_enable_flag;		// 1 bit
	private final int colour_primaries;					// 8 bits
	private final int transfer_characteristics;			// 8 bits
	private final int matrix_coefficients;				// 8 bits

	private static final String[] frame_rate_Strings = {"forbidden", "24/1.001", "24", "25", "30/1.001", "30", "50",
														"60/1.001", "60", "100", "120", "240", "400", "120/1.001", "unknown"};
	private static final String[] sample_precision_Strings = {"forbidden", "8-bit", "10-bit", "reserved", "reserved", 
																"reserved", "reserved", "reserved" };
	private static final String[] chroma_format_Strings = {"forbidden", "4:2:0", "4:2:2", "unknown"};

	public AVS3VideoDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset, parent);
		profile_id = getInt(b, offset+2, 1, 0xFF);
		level_id = getInt(b, offset+3, 1, 0xFF);
		multiple_frame_rate_flag = getInt(b, offset + 4, 1, 0x80) >>> 7;
		frame_rate_code = getInt(b, offset+4, 1, 0x78) >> 3;
		sample_precision = getInt(b, offset+4, 1, 0x07);
		chroma_format = getInt(b, offset+5, 1, 0xC0) >>> 6;
		temporal_id_flag = getInt(b, offset+5, 1, 0x20) >>> 5;
		td_mode_flag = getInt(b, offset+5, 1, 0x10) >>> 4;
		library_stream_flag = getInt(b, offset+5, 1, 0x08) >>> 3;
		library_picture_enable_flag = getInt(b, offset+5, 1, 0x04) >>> 2;
		colour_primaries = getInt(b, offset+6, 1, 0xFF);
		transfer_characteristics = getInt(b, offset+7, 1, 0xFF);
		matrix_coefficients = getInt(b, offset+8, 1, 0xFF);
	}

	public static final String profile_id_String(int value) {
		switch (value) {
			case 0x20: return "Main 8-bit Profile";
			case 0x22: return "Main 10-bit Profile";
			case 0x30: return "High 8-bit Profile";
			case 0x32: return "High 10-bit Profile";
		}
		throw new IllegalArgumentException("Invalid value in profile_id_String:"+value);
	}

	public static final String level_id_String(int value) {
		switch (value) {
			case 0x10: return "2.0.15";
			case 0x12: return "2.0.30";
			case 0x14: return "2.0.60";
			case 0x20: return "4.0.30";
			case 0x22: return "4.0.60";
			case 0x40: return "6.0.30";
			case 0x42: return "6.2.30";
			case 0x41: return "6.4.30";
			case 0x43: return "6.6.30";
			case 0x44: return "6.0.60";
			case 0x46: return "6.2.60";
			case 0x45: return "6.4.60";
			case 0x47: return "6.6.60";
			case 0x48: return "6.0.120";
			case 0x4A: return "6.2.120";
			case 0x49: return "6.4.120";
			case 0x4B: return "6.6.120";
			case 0x50: return "8.0.30";
			case 0x52: return "8.2.30";
			case 0x51: return "8.4.30";
			case 0x53: return "8.6.30";
			case 0x54: return "8.0.60";
			case 0x56: return "8.2.60";
			case 0x55: return "8.4.60";
			case 0x57: return "8.6.60";
			case 0x58: return "8.0.120";
			case 0x5A: return "8.2.120";
			case 0x59: return "8.4.120";
			case 0x5B: return "8.6.120";
			case 0x60: return "10.0.30";
			case 0x62: return "10.2.30";
			case 0x61: return "10.4.30";
			case 0x63: return "10.6.30";
			case 0x64: return "10.0.60";
			case 0x66: return "10.2.60";
			case 0x65: return "10.4.60";
			case 0x67: return "10.6.60";
			case 0x68: return "10.0.120";
			case 0x6A: return "10.2.120";
			case 0x69: return "10.4.120";
			case 0x6B: return "10.6.120";
		}
		throw new IllegalArgumentException("Invalid value in level_id_String:"+value);
	}

	public static final String frame_rate_code_String(int value){
		if(value < 0 || value > 15)
			throw new IllegalArgumentException("Invalid value in frame_rate_code_String:"+value);
		return frame_rate_Strings[value];
	}

	public static final String sample_precision_String(int value){
		if(value < 0 || value > 7)
			throw new IllegalArgumentException("Invalid value in sample_precision_String:"+value);
		return sample_precision_Strings[value];
	}

	public static final String chroma_format_String(int value){
		if(value < 0 || value > 3)
			throw new IllegalArgumentException("Invalid value in chroma_format_String:"+value);
		return chroma_format_Strings[value];
	}

	public DefaultMutableTreeNode getJTreeNode(final int modus){
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("profile_id",profile_id,profile_id_String(profile_id))));
		t.add(new DefaultMutableTreeNode(new KVP("level_id",level_id,level_id_String(level_id))));
		t.add(new DefaultMutableTreeNode(new KVP("multiple_frame_rate_flag",multiple_frame_rate_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("frame_rate_code",frame_rate_code,frame_rate_code_String(frame_rate_code))));
		t.add(new DefaultMutableTreeNode(new KVP("sample_precision",sample_precision,sample_precision_String(sample_precision))));
		t.add(new DefaultMutableTreeNode(new KVP("chroma_format",chroma_format,chroma_format_String(chroma_format))));
		t.add(new DefaultMutableTreeNode(new KVP("temporal_id_flag",temporal_id_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("td_mode_flag",td_mode_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("library_stream_flag",library_stream_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("library_picture_enable_flag",library_picture_enable_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("colour_primaries",colour_primaries,null)));
		t.add(new DefaultMutableTreeNode(new KVP("transfer_characteristics",transfer_characteristics,null)));
		t.add(new DefaultMutableTreeNode(new KVP("matrix_coefficients",matrix_coefficients,null)));
		return t;
	}

	@Override
	public String getDescriptorname(){
		return "AVS3 Video Descriptor";
	}
}
