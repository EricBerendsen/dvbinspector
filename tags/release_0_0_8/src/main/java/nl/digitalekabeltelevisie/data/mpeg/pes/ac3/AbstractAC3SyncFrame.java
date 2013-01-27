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

package nl.digitalekabeltelevisie.data.mpeg.pes.ac3;

import nl.digitalekabeltelevisie.util.BitSource;


public class AbstractAC3SyncFrame {

	static int[] ac3_bitrate_index = {
				32000, 40000, 48000, 56000, 64000, 80000, 96000,
				112000, 128000, 160000, 192000, 224000, 256000,
				320000, 384000, 448000, 512000, 576000, 640000,
				0,0,0,0,0,0,0,0,0,0,0,0,0
			};

	// this is different from the version in ProjectX in 2 ways;
	// 1) it has size in words (2 bytes) instead of bytes (more confirming to the spec)
	// 2) double number of entries, for 44,1 kHz this makes difference

	private static int[][] ac3_size_table = {
			{64,64,80,80,96,96,112,112,128,128,160,160,192,192,224,224,256,256,320,320,384,384,448,448,512,512,640,640,768,768,896,896,1024,1024,1152,1152,1280,1280}, //48 kHz
			{69,70,87,88,104,105,121,122,139,140,174,175,208,209,243,244,278,279,348,349,417,418,487,488,557,558,696,697,836,835,975,976,1114,1115,1253,1254,1393,1394}, // 44,1 kHz
			{96,96,120,120,144,144,168,168,192,192,240,240,288,288,336,336,384,384,480,480,576,576,672,672,768,768,960,960,1152,1152,1344,1344,1560,1560,1728,1728,1920,1920} //32 kHz
		};

	protected int syncword;
	protected int fscod;
	protected int bsid;
	protected int lfeon;
	protected int dialnorm;
	protected int compre;
	protected int compr;
	protected int audprodie;
	protected int mixlevel;
	protected int roomtyp;
	protected int dialnorm2;
	protected int compr2e;
	protected int audprodi2e;
	protected int mixlevel2;
	protected int roomtyp2;
	protected BitSource bs = null;
	protected int offset;
	protected int dmixmod;
	protected int compr2;
	protected int ltrtcmixlev;
	protected int ltrtsurmixlev;
	protected int lorocmixlev;
	protected int lorosurmixlev;
	protected int bsmod;
	protected int dsurmod;
	protected int copyrightb;
	protected int origbs;
	protected int dheadphonmod;
	protected int addbsil;
	protected int frmsizecod;
	protected int addbsie;
	protected int crc1;
	protected int acmod;
	protected int cmixlev;
	protected int surmixlev;
	protected int langcode;
	protected int langcod;
	protected int langcod2e;
	protected int langcod2;
	protected int timecod1e;
	protected int timecod1;
	protected int timecod2e;
	protected int timecod2;
	protected int dsurexmod;
	protected int adconvtyp;

	public AbstractAC3SyncFrame(final byte[] data, final int offset) {
		super();
		this.offset = offset;
		bs = new BitSource(data, offset);
	}

	public static String getBsidString(final int bsid) {
		String t="";
		if(bsid==16){
			t = "E-AC-3";
		}else if(bsid==6){
			t = "AC3 Alternate bit stream syntax";
		}else if(bsid==8){
			t = "AC3 normal syntax";
		}else if(bsid<8){
			t = "AC3 backward compatible";
		}else if((bsid==9)||(bsid==10)){
			t = "AC3 newer version";
		}else if((bsid>10)||(bsid<16)){
			t = "E-AC-3 backward compatible";
		}else{
			t = "E-AC-3 newer version";
		}
		return "Bit stream identification "+t;
	}

	/**
	 * @return
	 */
	protected static String getLfeOnString(int lfeon) {
		return (lfeon==1)?"Low frequency effects channel is on":"Low frequency effects channel is off";
	}

	/**
	 * @return
	 */
	protected static String getDialNormString(int dialnorm) {
		return "Dialogue normalization: "+((dialnorm==0)?"Reserved":"-"+dialnorm+" dB");
	}

	/**
	 * @return
	 */
	protected static String getCompreString(int compre) {
		return compre==1?"Compression gain word exists":"No compression gain word exists";
	}

	public static String getSampleRateCodeString(final int fscod) {
		switch (fscod) {
		case 0:
			return "48 kHz";
		case 1:
			return "44,1 kHz";
		case 2:
			return "32 kHz";
		case 3:
			return "Reserved";

		default:
			return "illegal value";
		}
	}

	public static String getDmixmodString(final int dmixmod2) {
		switch (dmixmod2) {

		case 0:
			return "Not indicated";
		case 1:
			return "Lt/Rt downmix preferred";
		case 2:
			return "Lo/Ro downmix preferred";
		case 3:
			return "Reserved";

		default:
			return "Illegal value";
		}
	}

	public static String getDheadphonmodString(final int dheadphonmod) {
		final String [] desc = {"Not indicated",
				"Not Dolby Headphone encoded",
				"Dolby Headphone encoded",
				"Reserved"
				};
		return desc[dheadphonmod];
	}

	public static String getDsurmodString(int dsurmod) {
		final String [] desc = {"Not indicated",
				"NOT Dolby surround encoded",
				"Dolby surround encoded",
				"Reserved"
				};
		return desc[dsurmod];
	}

	public static String getDsurexmodString(final int dsurexmod) {
		final String [] desc = {"Not indicated",
				"Not Dolby Surround EX encoded",
				"Dolby Surround EX encoded",
				"Reserved"
				};
		return desc[dsurexmod];
	}

	public static String getRoomTypeString(final int roomtyp) {

		switch (roomtyp) {

		case 0:
			return "Not indicated";
		case 1:
			return "Large room, X curve monitor";
		case 2:
			return "Small room, flat monitor";
		case 3:
			return "reserved";

		default:
			return "Illegal value";
		}
	}

	public static String getFrmsizecodString(final int fscod, final int frmsizecod) {
		if((fscod>=0) && (fscod<3) && (frmsizecod>=0) && (frmsizecod <=37)){
			return "Nominal bit rate: "+ac3_bitrate_index[frmsizecod>>>1]+", words/syncframe: "+ac3_size_table[fscod][frmsizecod];
		}else{
			return "illegal values for fscod: "+fscod+" or frmsizecod: "+frmsizecod;
		}

	}

	public static String getBsModString(final int bsmod, final int acmod) {
		switch (bsmod) {
		case 0:
			return "Main audio service: complete main (CM)";
		case 1:
			return "Main audio service: music and effects (ME)";
		case 2:
			return "Associated service: visually impaired (VI)";
		case 3:
			return "Associated service: hearing impaired (HI)";
		case 4:
			return "Associated service: dialogue (D)";
		case 5:
			return "Associated service: commentary (C)";
		case 6:
			return "Associated service: emergency (E)";
		case 7:
			if(acmod==1){
				return "Associated service: dialogue (D)";
			}else if((acmod>=2) && (acmod<=7)){
				return "Main audio service: karaoke";
			}else{
				return "Illegal value for acmod: "+acmod;
			}

		default:
			return "Illegal Value for bsmod: "+bsmod;
		}
	}

	public static String getCentreMixLevelString(final int cmixlev) {
		switch (cmixlev) {
		case 0:
			return "0,707 (-3,0 dB)";
		case 1:
			return "0,595 (-4,5 dB)";
		case 2:
			return "0,500 (-6,0 dB)";
		case 3:
			return "reserved";
		default:
			return "Illegal value";
		}
	}

	public static String getSurroundMixLevelString(final int surmixlev) {
		switch (surmixlev) {
		case 0:
			return "0,707 (-3,0 dB)";
		case 1:
			return "0,500 (-6 dB)";
		case 2:
			return "0";
		case 3:
			return "reserved";
		default:
			return "Illegal value";
		}
	}

	public static String getACModString(final int acmod) {
		switch (acmod) {
		case 0:
			return "1 + 1";
		case 1:
			return "1/0";
		case 2:
			return "2/0";
		case 3:
			return "3/0";
		case 4:
			return "2/1";
		case 5:
			return "3/1";
		case 6:
			return "2/2";
		case 7:
			return "3/2";
		default:
			return "Illegal Value for acmod: "+acmod;
		}
	}



	public static String getCmixlevString(final int cmixlev) {
		final String [] desc = {"1,414 (+3,0 dB)",
				"1,189 (+1,5 dB)",
				"1,000 (0,0 dB)",
				"0,841 (-1,5 dB)",
				"0,707 (-3,0 dB)",
				"0,595 (-4,5 dB)",
				"0,500 (-6,0 dB)",
				"0,000 (-inf dB)"};
		return desc[cmixlev];
	}

	public static String getSurmixlevString(final int surmixlev) {
		final String [] desc = {"Reserved",
				"Reserved",
				"Reserved",
				"0,841 (-1,5 dB)",
				"0,707 (-3,0 dB)",
				"0,595 (-4,5 dB)",
				"0,500 (-6,0 dB)",
				"0,000 (-inf dB)"};
		return desc[surmixlev];
	}

	public static int[] getAc3_bitrate_index() {
		return ac3_bitrate_index;
	}

	public static int[][] getAc3_size_table() {
		return ac3_size_table;
	}

	public static String doesExistString(int p) {
		if(p==1){
			return "does exist";
		}
		return "does not exist";
	}

	public int getSyncword() {
		return syncword;
	}

	public int getFscod() {
		return fscod;
	}

	public int getBsid() {
		return bsid;
	}

	public int getLfeon() {
		return lfeon;
	}

	public int getDialnorm() {
		return dialnorm;
	}

	public int getCompre() {
		return compre;
	}

	public int getCompr() {
		return compr;
	}

	public int getAudprodie() {
		return audprodie;
	}

	public int getMixlevel() {
		return mixlevel;
	}

	public int getRoomtyp() {
		return roomtyp;
	}

	public int getDialnorm2() {
		return dialnorm2;
	}

	public int getCompr2e() {
		return compr2e;
	}

	public int getAudprodi2e() {
		return audprodi2e;
	}

	public int getMixlevel2() {
		return mixlevel2;
	}

	public int getRoomtyp2() {
		return roomtyp2;
	}

	public BitSource getBs() {
		return bs;
	}

	public int getOffset() {
		return offset;
	}

	public int getDmixmod() {
		return dmixmod;
	}

	public int getCompr2() {
		return compr2;
	}

	public int getLtrtcmixlev() {
		return ltrtcmixlev;
	}

	public int getLtrtsurmixlev() {
		return ltrtsurmixlev;
	}

	public int getLorocmixlev() {
		return lorocmixlev;
	}

	public int getLorosurmixlev() {
		return lorosurmixlev;
	}

	public int getBsmod() {
		return bsmod;
	}

	public int getDsurmod() {
		return dsurmod;
	}

	public int getCopyrightb() {
		return copyrightb;
	}

	public int getOrigbs() {
		return origbs;
	}

	public int getDheadphonmod() {
		return dheadphonmod;
	}

	public int getAddbsil() {
		return addbsil;
	}

	public int getFrmsizecod() {
		return frmsizecod;
	}

	public int getAddbsie() {
		return addbsie;
	}

	public int getCrc1() {
		return crc1;
	}

	public int getAcmod() {
		return acmod;
	}

	public int getCmixlev() {
		return cmixlev;
	}

	public int getSurmixlev() {
		return surmixlev;
	}

	public int getLangcode() {
		return langcode;
	}

	public int getLangcod() {
		return langcod;
	}

	public int getLangcod2e() {
		return langcod2e;
	}

	public int getLangcod2() {
		return langcod2;
	}

	public int getTimecod1e() {
		return timecod1e;
	}

	public int getTimecod1() {
		return timecod1;
	}

	public int getTimecod2e() {
		return timecod2e;
	}

	public int getTimecod2() {
		return timecod2;
	}

	public int getDsurexmod() {
		return dsurexmod;
	}

	public int getAdconvtyp() {
		return adconvtyp;
	}

}