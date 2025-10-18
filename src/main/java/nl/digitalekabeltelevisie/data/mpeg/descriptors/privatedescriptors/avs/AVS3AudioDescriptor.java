/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2024 by Paul Higgs (paul_higgs@hotmail.com)
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

import static java.util.Arrays.copyOfRange;
import static nl.digitalekabeltelevisie.util.Utils.*;


import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.LookUpList;

import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;

public class AVS3AudioDescriptor extends Descriptor {

	private int audio_codec_id = 0;                   // 4 bits
	private int sampling_frequency_index = 0;         // 4 bits
	private int anc_data_index = 0;                   // 1 bit
	private int coding_profile = 0;                   // 3 bits
	private int bitrate_index = 0;                    // 4 bits
	private int bitstream_type = 0;                   // 1 bit
	private int channel_number_index = 0;             // 7 bits
	private int raw_frame_length = 0;                 // 16 bits
	private int sampling_frequency = 0;               // 24 bits
	private int channel_number = 0;                   // 8 bits
	private int nn_type = 0;                          // 3 bits
	private int content_type = 0;                     // 4 bits
	private int object_channel_number = 0;            // 7 bits
	private int hoa_order = 0;                    	  // 4 bits
	private int total_bitrate = 0;                    // 16 bits
	private int resolution = 0;                       // 2 bits

	private byte[] addition_info;

	private static final int GENERAL_HIGH_RATE_CODING = 0;
	private static final int LOSSLESS_CODING          = 1;
	private static final int GENERAL_FULL_RATE_CODING = 2;

	private static final int CHANNEL_SIGNAL = 0;
	private static final int OBJECT_SIGNAL  = 1;
	private static final int HYBRID_SIGNAL  = 2;
	private static final int HOA_SIGNAL     = 3;

	// T/AI 109.3 table A.8
	private static final LookUpList channel_number_index_Strings = new LookUpList.Builder().
		add(0x00, "Mono").
		add(0x01, "Dual channel stereo").
		add(0x02, "5.1").
		add(0x03, "7.1").
		add(0x04, "10.2").
		add(0x05, "22.2").
		add(0x06, "4.0/FOA").
		add(0x07, "5.1.2").
		add(0x08, "5.1.4").
		add(0x09, "7.1.2").
		add(0x0A, "7.1.4").
		add(0x0B, "3-order HOA").
		add(0x0C, "2-order HOA").
		add(0x0D, 0x7F, "reserved").
		build();

	// T/AI 109.3 table A.9
	private static LookUpList sampling_frequency_index_Strings = new LookUpList.
		Builder(new String[]{"192000", "96000", "48000", "44100", "32000", "24000", "22050", "16000", "8000"}).
		add(0x9, 0xF, "reserved").
		build();

	// T/AI 109.3 table A.10
	private static LookUpList bitrate_index_Mono_Strings = new LookUpList.
		Builder(new String[]{"16", "32", "44", "56", "64", "72", "80", "96", "128", "144", "164", "192"}).
		add(0xC, 0xF, "reserved").
		build();

	// T/AI 109.3 table A.11
	private static LookUpList bitrate_index_Stereo_Strings = new LookUpList.
		Builder(new String[]{"24", "32", "48", "64", "80", "96", "128", "144", "192", "256", "320"}).
		add(0xB, 0xF, "reserved").
		build();

	// T/AI 109.3 table A.12
	private static LookUpList bitrate_index_5_1_Strings = new LookUpList.
		Builder(new String[]{"192", "256", "320", "384", "448", "512", "640", "720", "144", "96", "128", "160"}).
		add(0xC, 0xF, "reserved").
		build();

	// T/AI 109.3 table A.13
	private static LookUpList bitrate_index_7_1_Strings = new LookUpList.
		Builder(new String[]{"192", "480", "256", "384", "576", "640", "128", "160"}).
		add(0x8, 0xF, "reserved").
		build();

	// T/AI 109.3 table A.14
	private static LookUpList bitrate_index_FOA_Strings = new LookUpList.
		Builder(new String[]{"48", "96", "128", "192", "256"}).
		add(0x5, 0xF, "reserved").
		build();

	// T/AI 109.3 table A.15
	private static LookUpList bitrate_index_5_1_2_Strings = new LookUpList.
		Builder(new String[]{"152", "320", "480", "576"}).
		add(0x4, 0xF, "reserved").
		build();

	// T/AI 109.3 table A.16
	private static LookUpList bitrate_index_5_1_4_Strings = new LookUpList.
		Builder(new String[]{"176", "384", "576", "704", "356", "448"}).
		add(0x6, 0xF, "reserved").
		build();

	// T/AI 109.3 table A.17
	private static LookUpList bitrate_index_7_1_2_Strings = new LookUpList.
		Builder(new String[]{"216", "480", "576", "384", "768"}).
		add(0x5, 0xF, "reserved").
		build();

	// T/AI 109.3 table A.18
	private static LookUpList bitrate_index_7_1_4_Strings = new LookUpList.
		Builder(new String[]{"240", "608", "384", "512", "832"}).
		add(0x5, 0xF, "reserved").
		build();

	// T/AI 109.3 table A.16
	private static LookUpList bitrate_index_HOA2_Strings = new LookUpList.
		Builder(new String[]{"192", "256", "320", "384", "480", "512", "640"}).
		add(0x7, 0xF, "reserved").
		build();

	// T/AI 109.3 table A.17
	private static LookUpList bitrate_index_HOA3_Strings = new LookUpList.
		Builder(new String[]{"256", "320", "384", "512", "640", "896"}).
		add(0x6, 0xF, "reserved").
		build();

	private static LookUpList[] channel_bitrates = {
		bitrate_index_Mono_Strings,
		bitrate_index_Stereo_Strings,
		bitrate_index_5_1_Strings,
		bitrate_index_7_1_Strings,
		bitrate_index_FOA_Strings,
		bitrate_index_5_1_2_Strings,
		bitrate_index_5_1_4_Strings,
		bitrate_index_7_1_2_Strings,
		bitrate_index_7_1_4_Strings,
		bitrate_index_HOA2_Strings,
		bitrate_index_HOA3_Strings,
	};

	// T/AI 109.3 annex A.2
	private static LookUpList audio_codec_id_Strings = new LookUpList.Builder().
		add(GENERAL_HIGH_RATE_CODING, "High Rate Coding").
		add(LOSSLESS_CODING, "Lossless Coding").
		add(GENERAL_FULL_RATE_CODING, "Full Rate Coding").
		build();

	// T/AI 109.3 annex A.2
	private static LookUpList coding_profile_Strings = new LookUpList.
		Builder(new String[]{"Basic", "Object Based", "HOA"}).
		add(3, 7, "reserved").
		build();

	// T/AI 109.3 annex A.2
	private static LookUpList nn_type_Strings = new LookUpList.
		Builder(new String[]{"basic", "low complexity"}).
		add(2, 7, "reserved").
		build();

	private static LookUpList content_type_Strings = new LookUpList.Builder().
		add(CHANNEL_SIGNAL, "channels only").	
		add(OBJECT_SIGNAL, "objects only").
		add(HYBRID_SIGNAL, "channels and objects").
		add(HOA_SIGNAL, "HOA").
		build();

	public AVS3AudioDescriptor(byte[] b, TableSection parent) {
		super(b, parent);

		audio_codec_id = getInt(b, 2, 1, 0b11110000) >>> 4;
		sampling_frequency_index = getInt(b, 2, 1, MASK_4BITS);
		int ofs = 3;
		switch (audio_codec_id) {
		case GENERAL_HIGH_RATE_CODING:
			anc_data_index = getInt(b, ofs, 1, 0b10000000) >>> 7;
			coding_profile = getInt(b, ofs, 1, 0b01110000) >>> 4;
			bitrate_index = getInt(b, ofs++, 1, MASK_4BITS);
			bitstream_type = getInt(b, ofs, 1, 0b10000000) >>> 7;
			channel_number_index = getInt(b, ofs++, 1, MASK_7BITS);
			raw_frame_length = getInt(b, ofs, 2, MASK_16BITS);
			ofs+=2;
			break;
		case LOSSLESS_CODING:
			if (sampling_frequency_index == 0xF) {
				sampling_frequency = getInt(b, ofs, 3, MASK_24BITS);
				ofs+=3;
			}
			anc_data_index = getInt(b, ofs, 1, 0b10000000) >>> 7;
			coding_profile = getInt(b, ofs++, 1, 0b01110000) >>> 4;
			channel_number = getInt(b, ofs++, 1, MASK_8BITS);
			break;
		case GENERAL_FULL_RATE_CODING:
			nn_type = getInt(b, ofs, 1, 0b11100000) >>> 5;
			content_type = getInt(b, ofs++, 1, MASK_4BITS);
			if (content_type == CHANNEL_SIGNAL)
				channel_number_index = getInt(b, ofs++, 1, 0b11111110) >>> 1;
			else if (content_type == OBJECT_SIGNAL)
				object_channel_number = getInt(b, ofs++, 1, 0b11111110) >>> 1;
			else if (content_type == HYBRID_SIGNAL) {
				channel_number_index = getInt(b, ofs++, 1, 0b11111110) >>> 1;
				object_channel_number = getInt(b, ofs++, 1, 0b11111110) >>> 1;
			}
			else if (content_type == HOA_SIGNAL)
				hoa_order = getInt(b, ofs++, 1, 0b11110000) >>> 4;
			total_bitrate = getInt(b, ofs, 2, MASK_16BITS);
			ofs+=2;
			break;
		}
		resolution = getInt(b, ofs++, 1, 0b11000000) >> 6;
		addition_info = copyOfRange(b, ofs, descriptorLength+2);
	}


	private static final String audio_codec_id_String(int audio_codec_id) {
		String rc = audio_codec_id_Strings.get(audio_codec_id);
		if (rc != null)
			return rc;
		throw new IllegalArgumentException("Invalid value for audio_codec_id:"+audio_codec_id);
	}

	private static final String sampling_frequency_index_String(int sampling_frequency_index) {
		String rc = sampling_frequency_index_Strings.get(sampling_frequency_index);
		if (rc != null)
			return rc;
		throw new IllegalArgumentException("Invalid value for sampling_frequency_index:"+sampling_frequency_index);
	}

	private static final String bitrate_index_String(int channel_number_index, int bitrate_index) {
		if (channel_number_index < 0 || channel_number_index > channel_bitrates.length)
			throw new IllegalArgumentException("Invalid value for channel_number_index:"+channel_number_index);
		String rc = channel_bitrates[channel_number_index].get(bitrate_index);
		if (rc != null)
			return rc;
		throw new IllegalArgumentException("Invalid value for bitrate_index:"+bitrate_index);
		}

	private static final String coding_profile_String(int coding_profile) {
		String rc = coding_profile_Strings.get(coding_profile);
		if (rc != null)
			return rc;
		throw new IllegalArgumentException("Invalid value for coding_profile:"+coding_profile);
	}

	private static final String bitstream_type_String(int bitstream_type) {
		return switch (bitstream_type) {
			case 0 -> "uniform";
			case 1 -> "non-uniform";
			default -> throw new IllegalArgumentException("Invalid value for bitstream_type:"+bitstream_type);
		};
	}

	private static final String channel_number_index_String(int channel_number_index) {
		String rc = channel_number_index_Strings.get(channel_number_index);
		if (rc != null)
			return rc;
		throw new IllegalArgumentException("Invalid value for channel_number_index:"+channel_number_index);
	}

	private static final String object_channel_number_String(int object_channel_number) {
		return new String( Integer.toString(object_channel_number+1) + " object" + (object_channel_number+1 == 1 ? "" : "s") );
	}

	private static final String nn_type_String(int nn_type) {
		String rc = nn_type_Strings.get(nn_type);
		if (rc != null)
			return rc;
		throw new IllegalArgumentException("Invalid value for nn_type:"+nn_type);
	}

	private static final String content_type_String(int content_type) {
		String rc = content_type_Strings.get(content_type);
		if (rc != null)
			return rc;
		throw new IllegalArgumentException("Invalid value for content_type:"+content_type);
	}

	private static final String resolution_String(int resolution) {
		return switch (resolution) {
			case 0 -> "8 bits";
			case 1 -> "16 bits";
			case 2 -> "24 bits";
			case 3 -> "reserved";
			default -> throw new IllegalArgumentException("Invalid value for resolution:"+resolution);
		};
	}

	@Override
	public KVP getJTreeNode(int modus) {
		KVP t = super.getJTreeNode(modus);
		t.add(new KVP("audio_codec_id", audio_codec_id, audio_codec_id_String(audio_codec_id)));

		if (audio_codec_id == LOSSLESS_CODING && sampling_frequency_index == 0xF) {
			t.add(new KVP("sampling_frequency", sampling_frequency));
		} else {
			t.add(new KVP("sampling_frequency_index", sampling_frequency_index, sampling_frequency_index_String(sampling_frequency_index)));
		}

		switch (audio_codec_id) {
		case GENERAL_HIGH_RATE_CODING:
			t.add(new KVP("anc_data_index", anc_data_index));
			t.add(new KVP("coding_profile", coding_profile, coding_profile_String(coding_profile)));
			t.add(new KVP("bitrate_index", bitrate_index, bitrate_index_String(channel_number_index, bitrate_index)));
			t.add(new KVP("bitstream_type", bitstream_type, bitstream_type_String(bitstream_type)));
			t.add(new KVP("channel_number_index", channel_number_index, channel_number_index_String(channel_number_index)));
			t.add(new KVP("raw_frame_length", raw_frame_length));
			break;
		case LOSSLESS_CODING:
			t.add(new KVP("anc_data_index", anc_data_index));
			t.add(new KVP("coding_profile", coding_profile, coding_profile_String(coding_profile)));
			t.add(new KVP("channel_number", channel_number));
			break;
		case GENERAL_FULL_RATE_CODING:
			t.add(new KVP("nn_type", nn_type, nn_type_String(nn_type)));
			t.add(new KVP("content_type", content_type, content_type_String(content_type)));
			if (content_type == CHANNEL_SIGNAL || content_type == HYBRID_SIGNAL) 
				t.add(new KVP("channel_number_index", channel_number_index, channel_number_index_String(channel_number_index)));
			if (content_type == OBJECT_SIGNAL|| content_type == HYBRID_SIGNAL) 
				t.add(new KVP("object_channel_number", object_channel_number, object_channel_number_String(object_channel_number)));
			if (content_type == HOA_SIGNAL)
				t.add(new KVP("hoa_order", hoa_order));
			t.add(new KVP("total_bitrate", total_bitrate));
			break;
		}
		t.add(new KVP("resolution", resolution, resolution_String(resolution)));
		if (addition_info.length > 0)
			t.add(new KVP("addition_info",addition_info));
		return t;
	}

	@Override
	public String getDescriptorname() {
		return "AVS3 Audio Descriptor";
	}
}
