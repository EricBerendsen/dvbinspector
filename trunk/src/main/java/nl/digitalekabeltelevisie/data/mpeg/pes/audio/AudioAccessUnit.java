/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2014 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.pes.audio;

import static nl.digitalekabeltelevisie.util.Utils.*;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.util.Utils;

/**
 * @author Eric Berendsen
 *
 * this also can have ancillary data, including RDS via EUCP
 * see ts_101154v010901p.pdf Annex C.4.2.18 RDS data via UECP protocol
 *
 *  partially based (framesize calculation) on http://www.javazoom.net/javalayer/
 */

public class AudioAccessUnit implements TreeNode {



	private final long pts;
	private final int syncWord;
	private final int id;

	/**
	 * This is the values as it is represented in the header of the access unit. It does NOT match the logical layer value!
	 *
	 * <pre>
	 * 2 bits to indicate which layer is used, according to the following table.
	 * "11" Layer I
	 * "10" Layer II
	 * "01" Layer III
	 * "00" reserved
	 * </pre>
	 */
	private final int layer;
	private final int protection_bit;
	private final int bit_rate_index;
	private final int sampling_frequency_index;
	private final int padding_bit;
	private final int private_bit;
	private final int mode;
	private final int mode_extension;
	private final int copyright;
	private final int original_home;
	private final int emphasis;
	//private int crc_check;

	private final byte [] data;
	private final int start;

	public static final int		MPEG2_LSF = 0;
	public static final int		MPEG25_LSF = 2;	// SZD

	/**
	 * Constant for MPEG-1 version
	 */
	public static final int		MPEG1 = 1;
	public static final int		STEREO = 0;
	public static final int		JOINT_STEREO = 1;
	public static final int		DUAL_CHANNEL = 2;
	public static final int		SINGLE_CHANNEL = 3;
	public static final int		FOURTYFOUR_POINT_ONE = 0;
	public static final int		FOURTYEIGHT=1;
	public static final int		THIRTYTWO=2;

	public static final int bitrates[][][] = {
		{{0 /*free format*/, 32000, 48000, 56000, 64000, 80000, 96000,
			112000, 128000, 144000, 160000, 176000, 192000 ,224000, 256000, 0},
			{0 /*free format*/, 8000, 16000, 24000, 32000, 40000, 48000,
				56000, 64000, 80000, 96000, 112000, 128000, 144000, 160000, 0},
				{0 /*free format*/, 8000, 16000, 24000, 32000, 40000, 48000,
					56000, 64000, 80000, 96000, 112000, 128000, 144000, 160000, 0}},

					{{0 /*free format*/, 32000, 64000, 96000, 128000, 160000, 192000,
						224000, 256000, 288000, 320000, 352000, 384000, 416000, 448000, 0},
						{0 /*free format*/, 32000, 48000, 56000, 64000, 80000, 96000,
							112000, 128000, 160000, 192000, 224000, 256000, 320000, 384000, 0},
							{0 /*free format*/, 32000, 40000, 48000, 56000, 64000, 80000,
								96000, 112000, 128000, 160000, 192000, 224000, 256000, 320000, 0}},
								// SZD: MPEG2.5
								{{0 /*free format*/, 32000, 48000, 56000, 64000, 80000, 96000,
									112000, 128000, 144000, 160000, 176000, 192000 ,224000, 256000, 0},
									{0 /*free format*/, 8000, 16000, 24000, 32000, 40000, 48000,
										56000, 64000, 80000, 96000, 112000, 128000, 144000, 160000, 0},
										{0 /*free format*/, 8000, 16000, 24000, 32000, 40000, 48000,
											56000, 64000, 80000, 96000, 112000, 128000, 144000, 160000, 0}},

	};

	public  static final int[][]	frequencies =
		{{22050, 24000, 16000, 1},
		{44100, 48000, 32000, 1},
		{11025, 12000, 8000, 1}};	// SZD: MPEG25

	public AudioAccessUnit(final byte[] data, final int offset, final long pts) {
		super();
		this.data=data;
		this.start=offset;

		this.pts = pts;

		final int i = offset;

		syncWord = getInt(data, i, 2, 0xFFF0)>>4;
		// TODO
		//		if(syncWord!=0xFFF){
		//		}
		id = getInt(data, i+1, 1, 0x08)>>3;
		layer = getInt(data, i+1, 1, 0x06)>>1;
		protection_bit = getInt(data, i+1, 1, 0x01);
		bit_rate_index = getInt(data, i+2, 1, 0Xf0)>>4;
		sampling_frequency_index = getInt(data, i+2, 1, 0X0C)>>2;
		padding_bit = getInt(data, i+2, 1, 0X02)>>1;
		private_bit = getInt(data, i+2, 1, 0X01);

		mode = getInt(data, i+3, 1, 0XC0)>>6;
		mode_extension = getInt(data, i+3, 1, 0X30)>>4;
		copyright = getInt(data, i+3, 1, 0X08)>>3;
		original_home = getInt(data, i+3, 1, 0X04)>>2;
		emphasis = getInt(data, i+3, 1, 0X03);
		if (protection_bit==0){
			//crc_check = getInt(data, i+4, 2, Utils.MASK_16BITS);
		}
	}


	/*
	 * (non-Javadoc)
	 *
	 * @see nl.digitalekabeltelevisie.controller.TreeNode#getJTreeNode(int)
	 */
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode s = new DefaultMutableTreeNode(new KVP("Frame"));
		s.add(new DefaultMutableTreeNode(new KVP("data",data, start,getFrameSize(),null)));
		s.add(new DefaultMutableTreeNode(new KVP("len",getFrameSize(), null)));
		s.add(new DefaultMutableTreeNode(new KVP("pts",pts, printTimebase90kHz(pts))));
		s.add(new DefaultMutableTreeNode(new KVP("syncWord",syncWord, null)));
		s.add(new DefaultMutableTreeNode(new KVP("id",id, id==1?"MPEG audio":"extension to lower sampling frequencies")));
		s.add(new DefaultMutableTreeNode(new KVP("layer",layer, getLayerString(layer))));
		s.add(new DefaultMutableTreeNode(new KVP("protection_bit",protection_bit, protection_bit==1?"no redundancy":"redundancy has been added")));
		s.add(new DefaultMutableTreeNode(new KVP("bit_rate_index",bit_rate_index, getBitrateString(id,layer, bit_rate_index))));
		s.add(new DefaultMutableTreeNode(new KVP("sampling_frequency",sampling_frequency_index, getSamplingFrequencyString(id,sampling_frequency_index))));
		s.add(new DefaultMutableTreeNode(new KVP("padding_bit",padding_bit, padding_bit==1?"frame contains an additional slot to adjust the mean bitrate to the sampling frequency":"No padding")));
		s.add(new DefaultMutableTreeNode(new KVP("private_bit",private_bit, null)));
		s.add(new DefaultMutableTreeNode(new KVP("mode",mode, getModeString(mode))));
		s.add(new DefaultMutableTreeNode(new KVP("mode_extension",mode_extension, getModeExtensionString(layer, mode, mode_extension))));
		s.add(new DefaultMutableTreeNode(new KVP("copyright",copyright, copyright==1?"copyright protected":"no copyright")));
		s.add(new DefaultMutableTreeNode(new KVP("original/home",original_home, original_home==1?"original":"copy")));
		s.add(new DefaultMutableTreeNode(new KVP("emphasis",emphasis, getEmphasisString(emphasis))));
		if (protection_bit==0){
			s.add(new DefaultMutableTreeNode(new KVP("crc_check",getInt(data, start+4, 2, Utils.MASK_16BITS), null)));
		}



		return s;
	}


	public static String getModeExtensionString(final int layer, final int mode, final int mode_extension){
		if(mode!=1){ // no joint_stereo
			return null;
		}else if(layer==01){  // layer 3 is coded as 01
			switch (mode_extension) {
			case 0:
				return "intensity_stereo: off, ms_stereo: off";
			case 1:
				return "intensity_stereo: on, ms_stereo: off";
			case 2:
				return "intensity_stereo: off, ms_stereo: on";
			case 3:
				return "intensity_stereo: on, ms_stereo: on";
			default:
				return "Illegal value";
			}
		}else{ // layer 1 or 2
			switch (mode_extension) {
			case 0:
				return "subbands 4-31 in intensity_stereo, bound==4";
			case 1:
				return "subbands 8-31 in intensity_stereo, bound==8";
			case 2:
				return "subbands 12-31 in intensity_stereo, bound==12";
			case 3:
				return "subbands 16-31 in intensity_stereo, bound==16";
			default:
				return "Illegal value";
			}

		}
	}

	public static String getLayerString(final int layer) {
		switch (layer) {

		case 0x1:
			return "Layer III";
		case 0x2:
			return "Layer II";
		case 0x3:
			return "Layer I";
		default:
			return "reserved";
		}
	}


	public static String getSamplingFrequencyString(final int id, final int sampling_frequency_index) {

		if(id==1) {
			switch (sampling_frequency_index) {
			case 0x0:
				return "44.1 kHz";
			case 0x1:
				return "48 kHz";
			case 0x2:
				return "32 kHz";
			default:
				return "reserved";
			}
		}else{ // id==0,low bitrate
			switch (sampling_frequency_index) {
			case 0x0:
				return "22.05 kHz";
			case 0x1:
				return "24 kHz";
			case 0x2:
				return "16 kHz";
			default:
				return "reserved";
			}
		}
	}

	public int getSamplingFrequency(){
		return getSamplingFrequency(id, sampling_frequency_index);
	}

	public static int getSamplingFrequency(final int id, final int sampling_frequency_index) {

		if(id==1) {
			switch (sampling_frequency_index) {
			case 0x0:
				return 44100;
			case 0x1:
				return 48000;
			case 0x2:
				return 32000;
			default:
				throw new IllegalArgumentException("id:"+id+",sampling_frequency_index:"+sampling_frequency_index);
			}
		}else{ // id==0,low bitrate
			switch (sampling_frequency_index) {
			case 0x0:
				return 22050;
			case 0x1:
				return 24000;
			case 0x2:
				return 16000;
			default:
				throw new IllegalArgumentException("id:"+id+",sampling_frequency_index:"+sampling_frequency_index);
			}
		}
	}

	public static String getModeString(final int mode) {
		switch (mode) {

		case 0x0:
			return "stereo";
		case 0x1:
			return "joint_stereo (intensity_stereo and/or ms_stereo)";
		case 0x2:
			return "dual_channel";
		case 0x3:
			return "single_channel";
		default:
			return "illegal value";
		}
	}

	public static String getEmphasisString(final int emphasis) {
		switch (emphasis) {

		case 0x0:
			return "no emphasis";
		case 0x1:
			return "50/15 microsec. emphasis";
		case 0x2:
			return "reserved";
		case 0x3:
			return "CCITT J.17";
		default:
			return "illegal value";
		}
	}

	public static String getBitrateString(final int id,final int layer, final int bit_rate_index) {

		if(id==1){
			switch (layer) {
			case 0x1:
				// "Layer III";
				switch (bit_rate_index) {

				case 0x0:
					return "free format";
				case 0x1:
					return "32 kbit/s";
				case 0x2:
					return "40 kbit/s";
				case 0x3:
					return "48 kbit/s";
				case 0x4:
					return "56 kbit/s";
				case 0x5:
					return "64 kbit/s";
				case 0x6:
					return "80 kbit/s";
				case 0x7:
					return "96 kbit/s";
				case 0x8:
					return "112 kbit/s";
				case 0x9:
					return "128 kbit/s";
				case 0xa:
					return "160 kbit/s";
				case 0xb:
					return "192 kbit/s";
				case 0xc:
					return "224 kbit/s";
				case 0xd:
					return "256 kbit/s";
				case 0xe:
					return "320 kbit/s";
				default:
					return "Illegal combination layer/ bitrate";
				}
			case 0x2:
				// "Layer II";
				switch (bit_rate_index) {
				case 0x0:
					return "free format";
				case 0x1:
					return "32 kbit/s";
				case 0x2:
					return "48 kbit/s";
				case 0x3:
					return "56 kbit/s";
				case 0x4:
					return "64 kbit/s";
				case 0x5:
					return "80 kbit/s";
				case 0x6:
					return "96 kbit/s";
				case 0x7:
					return "112 kbit/s";
				case 0x8:
					return "128 kbit/s";
				case 0x9:
					return "160 kbit/s";
				case 0xa:
					return "192 kbit/s";
				case 0xb:
					return "224 kbit/s";
				case 0xc:
					return "256 kbit/s";
				case 0xd:
					return "320 kbit/s";
				case 0xe:
					return "384 kbit/s";
				default:
					return "Illegal combination layer/ bitrate";
				}

			case 0x3:
				// "Layer I";
				switch (bit_rate_index) {
				case 0x0:
					return "free format";
				case 0x1:
					return "32 kbit/s";
				case 0x2:
					return "64 kbit/s";
				case 0x3:
					return "96 kbit/s";
				case 0x4:
					return "128 kbit/s";
				case 0x5:
					return "160 kbit/s";
				case 0x6:
					return "192 kbit/s";
				case 0x7:
					return "224 kbit/s";
				case 0x8:
					return "256 kbit/s";
				case 0x9:
					return "288 kbit/s";
				case 0xa:
					return "320 kbit/s";
				case 0xb:
					return "352 kbit/s";
				case 0xc:
					return "384 kbit/s";
				case 0xd:
					return "416 kbit/s";
				case 0xe:
					return "448 kbit/s";
				default:
					return "Illegal combination layer/ bitrate";
				}
			default:
				return "Illegal combination layer/ bitrate";
			}
		}else{ // id==0, low bitrate iso13818-3
			if(layer==0x3){ // layer 1
				switch (bit_rate_index) {
				case 0x0:
					return "free format";
				case 0x1:
					return "32 kbit/s";
				case 0x2:
					return "48 kbit/s";
				case 0x3:
					return "56 kbit/s";
				case 0x4:
					return "64 kbit/s";
				case 0x5:
					return "80 kbit/s";
				case 0x6:
					return "96 kbit/s";
				case 0x7:
					return "112 kbit/s";
				case 0x8:
					return "128 kbit/s";
				case 0x9:
					return "144 kbit/s";
				case 0xa:
					return "160 kbit/s";
				case 0xb:
					return "176 kbit/s";
				case 0xc:
					return "192 kbit/s";
				case 0xd:
					return "224 kbit/s";
				case 0xe:
					return "256 kbit/s";
				default:
					return "forbidden bit_rate_index";
				}

			}else{ //layer 2 or 3
				switch (bit_rate_index) {
				case 0x0:
					return "free format";
				case 0x1:
					return "8 kbit/s";
				case 0x2:
					return "16 kbit/s";
				case 0x3:
					return "24 kbit/s";
				case 0x4:
					return "32 kbit/s";
				case 0x5:
					return "40 kbit/s";
				case 0x6:
					return "48 kbit/s";
				case 0x7:
					return "56 kbit/s";
				case 0x8:
					return "64 kbit/s";
				case 0x9:
					return "80 kbit/s";
				case 0xa:
					return "96 kbit/s";
				case 0xb:
					return "112 kbit/s";
				case 0xc:
					return "128 kbit/s";
				case 0xd:
					return "144 kbit/s";
				case 0xe:
					return "160 kbit/s";
				default:
					return "forbidden bit_rate_index";
				}
			}
		}
	}

	/**
	 * @return the bit_rate_index
	 */
	public int getBit_rate_index() {
		return bit_rate_index;
	}



	/**
	 * @return the copyright
	 */
	public int getCopyright() {
		return copyright;
	}



	/**
	 * @return the emphasis
	 */
	public int getEmphasis() {
		return emphasis;
	}



	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}



	/**
	 * @return the layer
	 */
	public int getLayer() {
		return layer;
	}



	/**
	 * @return the mode_extension
	 */
	public int getMode_extension() {
		return mode_extension;
	}



	/**
	 * @return the original_home
	 */
	public int getOriginal_home() {
		return original_home;
	}



	/**
	 * @return the padding_bit
	 */
	public int getPadding_bit() {
		return padding_bit;
	}



	/**
	 * @return the private_bit
	 */
	public int getPrivate_bit() {
		return private_bit;
	}



	/**
	 * @return the protection_bit
	 */
	public int getProtection_bit() {
		return protection_bit;
	}



	/**
	 * @return the pts
	 */
	public long getPts() {
		return pts;
	}



	/**
	 * @return the sampling_frequency
	 */
	public int getSampling_frequency_index() {
		return sampling_frequency_index;
	}



	/**
	 * @return the syncWord
	 */
	public int getSyncWord() {
		return syncWord;
	}



	/**
	 * @return the mode
	 */
	public int getMode() {
		return mode;
	}

	public int getFrameSize() {
		// based on javazoom jlayer Header.java
		// For MPEG2.5 this could be 2, but MPEG2.5 is not supported in DVB
		// MPEG2 id ==0,  for extension to lower sampling frequencies.
		final int h_version=id;
		// The layer field in jlayer is corrected for the 'inverse' values from 11172-3
		int javazoomLayer = 4 - layer;

		int framesize =-1;
		if (javazoomLayer == 1)
		{
			framesize = (12 * bitrates[h_version][0][bit_rate_index]) /
					frequencies[h_version][sampling_frequency_index];
			if (padding_bit != 0 ){
				framesize++;
			}
			framesize <<= 2;		// one slot is 4 bytes long
		}
		else if (javazoomLayer > 1)
		{
			framesize = (144 * bitrates[h_version][javazoomLayer - 1][bit_rate_index]) /
					frequencies[h_version][sampling_frequency_index];
			if ((h_version == MPEG2_LSF) || (h_version == MPEG25_LSF)){
				framesize >>= 1;	// SZD
			}
			if (padding_bit != 0){
				framesize++;
			}
		}
		// EB total length
		// framesize -= 4;             // subtract header size
		return framesize;
	}


	public byte[] getData() {
		return data;
	}


	public int getStart() {
		return start;
	}


	/**
	 * Return a AncillaryData from the end oft his unit. Assumes it is already checked (from the PMT) whether there is really AncillaryData in this PES.
	 * Assumes only one unit of AncillaryData is present.
	 * @return
	 */
	public AncillaryData getAncillaryData(){
		return new AncillaryData(data, start, getFrameSize());

	}
}
