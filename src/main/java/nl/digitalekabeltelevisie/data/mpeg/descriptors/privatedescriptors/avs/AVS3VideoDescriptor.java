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

import static nl.digitalekabeltelevisie.util.Utils.getInt;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.LookUpList;


import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;

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

	// T/AI 109.2 table XX
	private static LookUpList frame_rate_Strings = new LookUpList.
		Builder(new String[]{"forbidden", "24/1.001", "24", "25", "30/1.001", "30", "50", "60/1.001", "60", 
							 "100", "120", "240", "400", "120/1.001", "unknown"}).
		build();

	// T/AI 109.2 table XX
	private static LookUpList sample_precision_Strings = new LookUpList.
		Builder(new String[]{"forbidden", "8-bit", "10-bit"}).
		add(0x3, 0x7, "reserved").
		build();

	// T/AI 109.2 table XX
	private static LookUpList chroma_format_Strings = new LookUpList.
		Builder(new String[]{"forbidden", "4:2:0", "4:2:2", "unknown"}).
		build();

	public AVS3VideoDescriptor(byte[] b, TableSection parent) {
		super(b, parent);
		profile_id = getInt(b, 2, 1, 0xFF);
		level_id = getInt(b, 3, 1, 0xFF);
		multiple_frame_rate_flag = getInt(b, 4, 1, 0b10000000) >>> 7;
		frame_rate_code = getInt(b, 4, 1, 0b01111111) >>> 3;
		sample_precision = getInt(b, 4, 1, 0b00000111);
		chroma_format = getInt(b, 5, 1, 0b11000000) >>> 6;
		temporal_id_flag = getInt(b, 5, 1, 0b00100000) >>> 5;
		td_mode_flag = getInt(b, 5, 1, 0b00010000) >>> 4;
		library_stream_flag = getInt(b, 5, 1, 0b00001000) >>> 3;
		library_picture_enable_flag = getInt(b, 5, 1, 0b00000100) >>> 2;
		colour_primaries = getInt(b, 6, 1, 0xFF);
		transfer_characteristics = getInt(b, 7, 1, 0xFF);
		matrix_coefficients = getInt(b, 8, 1, 0xFF);
	}

	private static final String profile_id_String(int profile_id) {
		return switch (profile_id) {
			case 0x20 -> "Main 8-bit Profile";
			case 0x22 -> "Main 10-bit Profile";
			case 0x30 -> "High 8-bit Profile";
			case 0x32 -> "High 10-bit Profile";
			default -> throw new IllegalArgumentException("Invalid value in profile_id_String:"+profile_id);
		};
	}

	private static final String level_id_String(int level_id) {
		return switch (level_id) {
			case 0x10 -> "2.0.15";
			case 0x12 -> "2.0.30";
			case 0x14 -> "2.0.60";
			case 0x20 -> "4.0.30";
			case 0x22 -> "4.0.60";
			case 0x40 -> "6.0.30";
			case 0x42 -> "6.2.30";
			case 0x41 -> "6.4.30";
			case 0x43 -> "6.6.30";
			case 0x44 -> "6.0.60";
			case 0x46 -> "6.2.60";
			case 0x45 -> "6.4.60";
			case 0x47 -> "6.6.60";
			case 0x48 -> "6.0.120";
			case 0x4A -> "6.2.120";
			case 0x49 -> "6.4.120";
			case 0x4B -> "6.6.120";
			case 0x50 -> "8.0.30";
			case 0x52 -> "8.2.30";
			case 0x51 -> "8.4.30";
			case 0x53 -> "8.6.30";
			case 0x54 -> "8.0.60";
			case 0x56 -> "8.2.60";
			case 0x55 -> "8.4.60";
			case 0x57 -> "8.6.60";
			case 0x58 -> "8.0.120";
			case 0x5A -> "8.2.120";
			case 0x59 -> "8.4.120";
			case 0x5B -> "8.6.120";
			case 0x60 -> "10.0.30";
			case 0x62 -> "10.2.30";
			case 0x61 -> "10.4.30";
			case 0x63 -> "10.6.30";
			case 0x64 -> "10.0.60";
			case 0x66 -> "10.2.60";
			case 0x65 -> "10.4.60";
			case 0x67 -> "10.6.60";
			case 0x68 -> "10.0.120";
			case 0x6A -> "10.2.120";
			case 0x69 -> "10.4.120";
			case 0x6B -> "10.6.120";
			default -> throw new IllegalArgumentException("Invalid value in level_id_String:"+level_id);
		};
	}

	private static final String frame_rate_code_String(int frame_rate_code) {
		String res =frame_rate_Strings.get(frame_rate_code);
		if (res != null)
			return res;
		throw new IllegalArgumentException("Invalid value in frame_rate_code_String:"+frame_rate_code);
	}

	private static final String sample_precision_String(int sample_precision) {
		String res = sample_precision_Strings.get(sample_precision);
		if (res != null)
			return res; 
		throw new IllegalArgumentException("Invalid value in sample_precision_String:"+sample_precision);
	}

	private static final String chroma_format_String(int chroma_format) {
		String res = chroma_format_Strings.get(chroma_format);
		if (res != null)
			return res;
		throw new IllegalArgumentException("Invalid value in chroma_format_String:"+chroma_format);
	}

	@Override
	public KVP getJTreeNode(int modus) {
		KVP t = super.getJTreeNode(modus);
		t.add(new KVP("profile_id", profile_id, profile_id_String(profile_id)));
		t.add(new KVP("level_id", level_id, level_id_String(level_id)));
		t.add(new KVP("multiple_frame_rate_flag", multiple_frame_rate_flag));
		t.add(new KVP("frame_rate_code", frame_rate_code, frame_rate_code_String(frame_rate_code)));
		t.add(new KVP("sample_precision", sample_precision, sample_precision_String(sample_precision)));
		t.add(new KVP("chroma_format", chroma_format, chroma_format_String(chroma_format)));
		t.add(new KVP("temporal_id_flag", temporal_id_flag));
		t.add(new KVP("td_mode_flag", td_mode_flag));
		t.add(new KVP("library_stream_flag", library_stream_flag));
		t.add(new KVP("library_picture_enable_flag", library_picture_enable_flag));
		t.add(new KVP("colour_primaries", colour_primaries));
		t.add(new KVP("transfer_characteristics", transfer_characteristics));
		t.add(new KVP("matrix_coefficients", matrix_coefficients));
		return t;
	}

	@Override
	public String getDescriptorname() {
		return "AVS3 Video Descriptor";
	}
}
