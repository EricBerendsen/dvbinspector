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

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;

/**
 *
 * Based on ETSI TS 102 366 V1.2.1 (2008-08) Digital Audio Compression (AC-3, Enhanced AC-3) Standard
 * @author Eric
 *
 */
public class AC3SyncFrame extends AbstractAC3SyncFrame implements TreeNode {

	private final  int crc1;
	private final  int frmsizecod;

	private final  int acmod;
	private int cmixlev;
	private int surmixlev;
	private final  int langcode;
	private int langcod;
	private int langcod2e;
	private int langcod2;
	private int timecod1e;
	private int timecod1;
	private int timecod2e;
	private int timecod2;

	// Alternate bit stream syntax
	private int xbsi1e;
	private int xbsi2e;
	private int dsurexmod;
	private int adconvtyp;
	private int xbsi2;
	private int encinfo;

	private final int addbsie;
	private int addbsil;

	// based on  ProjectX AudioFormatAC3.java
	static int[] ac3_bitrate_index =  {
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

	public AC3SyncFrame(final byte[] data, final int offset) {
		super(data,offset);
		syncword = bs.readBits(16);
		crc1= bs.readBits(16);
		fscod= bs.readBits(2);
		frmsizecod= bs.readBits(6);

		bsid = bs.readBits(5);
		bsmod = bs.readBits(3);
		acmod = bs.readBits(3);

		if(((acmod & 0x1)!=0) && (acmod != 0x1)){ /* if 3 front channels */
			cmixlev = bs.readBits(2);
		}
		if((acmod & 0x4)!=0){ /* if a surround channel exists */
			surmixlev = bs.readBits(2);
		}
		if(acmod == 0x2){ /* if in 2/0 mode */
			dsurmod = bs.readBits(2);
		}
		lfeon = bs.readBits(1);
		dialnorm = bs.readBits(5);
		compre = bs.readBits(1);
		if(compre==1){
			compr = bs.readBits(8);
		}
		langcode  = bs.readBits(1);
		if(langcode==1){
			langcod = bs.readBits(8);
		}
		audprodie = bs.readBits(1);
		if(audprodie==1){
			mixlevel = bs.readBits(5);
			roomtyp = bs.readBits(2);
		}
		if(acmod == 0){ /* if 1+1 mode (dual mono, so some items need a second value) */
			dialnorm2 = bs.readBits(5);
			compr2e = bs.readBits(1);
			if(compr2e==1){
				compr2 = bs.readBits(8);
			}
			langcod2e  = bs.readBits(1);
			if(langcod2e==1){
				langcod2 = bs.readBits(8);
			}
			audprodi2e = bs.readBits(1);
			if(audprodi2e==1){
				mixlevel2 = bs.readBits(5);
				roomtyp2 = bs.readBits(2);
			}

		}

		copyrightb = bs.readBits(1);
		origbs = bs.readBits(1);
		if(bsid!=6){ // normal syntax
			timecod1e = bs.readBits(1);
			if(timecod1e==1) {
				timecod1 = bs.readBits(14);
			}
			timecod2e = bs.readBits(1);
			if(timecod2e==1) {
				timecod2 = bs.readBits(14);
			}
		}else{  // Alternate bit stream syntax
			xbsi1e = bs.readBits(1);
			if(xbsi1e==1){
				dmixmod = bs.readBits(2);
				ltrtcmixlev = bs.readBits(3);
				ltrtsurmixlev = bs.readBits(3);
				lorocmixlev = bs.readBits(3);
				lorosurmixlev = bs.readBits(3);
			}
			xbsi2e = bs.readBits(1);
			if(xbsi2e==1){
				dsurexmod = bs.readBits(2);
				dheadphonmod = bs.readBits(2);
				adconvtyp = bs.readBits(1);
				xbsi2 = bs.readBits(8);
				encinfo = bs.readBits(1);
			}
		}
		addbsie = bs.readBits(1);
		if(addbsie==1)
		{
			addbsil = bs.readBits(6);
			//addbsi = bs.readBits((addbsil+1) * 8);
		}

	}


	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode s = new DefaultMutableTreeNode(new KVP("AC3 SyncFrame"));
		s.add(new DefaultMutableTreeNode(new KVP("syncword",syncword,"must be 0x0B77")));
		s.add(new DefaultMutableTreeNode(new KVP("crc1",crc1,null)));
		s.add(new DefaultMutableTreeNode(new KVP("fscod",fscod,"Sample rate code: "+getSampleRateCodeString(fscod))));
		s.add(new DefaultMutableTreeNode(new KVP("frmsizecod",frmsizecod,"Frame size code: "+getFrmsizecodString(fscod, frmsizecod))));

		s.add(new DefaultMutableTreeNode(new KVP("bsid",bsid,getBsidString(bsid))));
		s.add(new DefaultMutableTreeNode(new KVP("bsmod",bsmod,"Bit stream mode: "+getBsModString(bsmod, acmod))));
		s.add(new DefaultMutableTreeNode(new KVP("acmod",acmod,"Audio coding mode: "+getACModString(acmod))));
		if(((acmod & 0x1)!=0) && (acmod != 0x1)){ /* if 3 front channels */
			s.add(new DefaultMutableTreeNode(new KVP("cmixlev",cmixlev,"Centre mix level: "+getCentreMixLevelString(cmixlev))));
		}
		if((acmod & 0x4)!=0){ /* if a surround channel exists */
			s.add(new DefaultMutableTreeNode(new KVP("surmixlev",surmixlev,"Surround mix level: "+getSurroundMixLevelString(surmixlev))));
		}
		if(acmod == 0x2){ /* if in 2/0 mode */
			s.add(new DefaultMutableTreeNode(new KVP("dsurmod",dsurmod,"Dolby Surround mode")));
		}
		s.add(new DefaultMutableTreeNode(new KVP("lfeon",lfeon,getLfeOnString(lfeon))));

		s.add(new DefaultMutableTreeNode(new KVP("dialnorm",dialnorm,getDialNormString(dialnorm))));
		s.add(new DefaultMutableTreeNode(new KVP("compre",compre,getCompreString(compre))));
		if(compre==1){
			s.add(new DefaultMutableTreeNode(new KVP("compr",compr,"Compression gain word")));
		}
		s.add(new DefaultMutableTreeNode(new KVP("langcode",langcode,langcode==1?"Language code exists":"langcod does not exist in the bit stream")));
		if(langcode==1){
			s.add(new DefaultMutableTreeNode(new KVP("langcod",langcod,null)));
		}
		s.add(new DefaultMutableTreeNode(new KVP("audprodie",audprodie,audprodie==1?"mixlevel and roomtyp fields exist":"No audio production information exists")));
		if(audprodie==1){
			s.add(new DefaultMutableTreeNode(new KVP("mixlevel",mixlevel,"Mixing level: "+(80+mixlevel)+" dB")));
			s.add(new DefaultMutableTreeNode(new KVP("roomtyp","Room type: "+roomtyp,AC3SyncFrame.getRoomTypeString(roomtyp))));
		}
		if(acmod == 0){ /* if 1+1 mode (dual mono, so some items need a second value) */
			s.add(new DefaultMutableTreeNode(new KVP("dialnorm2",dialnorm2,getDialNormString(dialnorm2))));
			s.add(new DefaultMutableTreeNode(new KVP("compre2",compr2e,compr2e==1?"Compression gain word exists":"No compression gain word exists")));
			if(compr2e==1){
				s.add(new DefaultMutableTreeNode(new KVP("compr2",compr2,"Compression gain word")));
			}
			s.add(new DefaultMutableTreeNode(new KVP("langcod2e",langcod2e,"Language code exists")));
			if(langcod2e==1){
				s.add(new DefaultMutableTreeNode(new KVP("langcod2",langcod2,null)));
			}
			s.add(new DefaultMutableTreeNode(new KVP("audprodi2e",audprodi2e,audprodi2e==1?"following two data fields exist indicating information about the audio production for Ch2":"No audio production information exists for Ch2")));
			if(audprodi2e==1){
				s.add(new DefaultMutableTreeNode(new KVP("mixlevel2",mixlevel2,"peak mixing level during the final audio mixing session: "+(80+mixlevel2)+" dB")));
				s.add(new DefaultMutableTreeNode(new KVP("roomtyp2",roomtyp2,AC3SyncFrame.getRoomTypeString(roomtyp))));
			}
		}

		s.add(new DefaultMutableTreeNode(new KVP("copyrightb",copyrightb,copyrightb==1?"protected by copyright":"not indicated as protected")));
		s.add(new DefaultMutableTreeNode(new KVP("origbs",origbs,origbs==1?"original bit stream":"copy of another bit stream")));
		if(bsid!=6){ // normal syntax

			s.add(new DefaultMutableTreeNode(new KVP("timecod1e",timecod1e,null)));
			if(timecod1e==1) {
				s.add(new DefaultMutableTreeNode(new KVP("timecod1",timecod1,null)));
			}
			s.add(new DefaultMutableTreeNode(new KVP("timecod2e",timecod2e,null)));
			if(timecod2e==1) {
				s.add(new DefaultMutableTreeNode(new KVP("timecod2",timecod2,null)));
			}
		}else{  // Alternate bit stream syntax
			s.add(new DefaultMutableTreeNode(new KVP("xbsi1e",xbsi1e,"Extra bitstream information #1 exists")));
			if(xbsi1e==1){
				s.add(new DefaultMutableTreeNode(new KVP("dmixmod",dmixmod,"Preferred stereo downmix mode: "+AC3SyncFrame.getDmixmodString(dmixmod))));
				s.add(new DefaultMutableTreeNode(new KVP("ltrtcmixlev",ltrtcmixlev,"Lt/Rt center mix level: "+getCmixlevString(ltrtcmixlev))));
				s.add(new DefaultMutableTreeNode(new KVP("ltrtsurmixlev",ltrtsurmixlev,"Lt/Rt surround mix level: "+getSurmixlevString(ltrtsurmixlev))));
				s.add(new DefaultMutableTreeNode(new KVP("lorocmixlev",lorocmixlev,"Lo/Ro center mix level: "+getCmixlevString(lorocmixlev))));
				s.add(new DefaultMutableTreeNode(new KVP("lorosurmixlev",lorosurmixlev,"Lo/Ro surround mix level: "+getSurmixlevString(lorosurmixlev))));
			}
			s.add(new DefaultMutableTreeNode(new KVP("xbsi2e",xbsi2e,"Extra bit stream information #2 exists")));
			if(xbsi2e==1){
				s.add(new DefaultMutableTreeNode(new KVP("dsurexmod",dsurexmod,"Dolby Surround EX mode: "+getDsurexmodString(dsurexmod))));
				s.add(new DefaultMutableTreeNode(new KVP("dheadphonmod",dheadphonmod,"Dolby Headphone mode: "+getDheadphonmodString(dheadphonmod))));
				s.add(new DefaultMutableTreeNode(new KVP("adconvtyp",adconvtyp,"A/D converter type: "+(adconvtyp==1?"HDCD":"standard"))));
				s.add(new DefaultMutableTreeNode(new KVP("xbsi2",xbsi2,"Extra bit stream information, reserved for future assignment. shall be set to 0")));
				s.add(new DefaultMutableTreeNode(new KVP("encinfo",encinfo,"Encoder information, reserved for use by the encoder")));
			}
		}
		s.add(new DefaultMutableTreeNode(new KVP("addbsie",addbsie,addbsie==1?"Additional bit stream information exists":"Additional bit stream information does not exist")));
		if(addbsie==1)
		{
			s.add(new DefaultMutableTreeNode(new KVP("addbsil",addbsil,"Additional bit stream information length")));
			// s.add(new DefaultMutableTreeNode(new KVP("addbsi",addbsi,null)));
		}
		return s;
	}


	private String getDsurexmodString(final int dsurexmod) {
		final String [] desc = {"Not indicated",
				"Not Dolby Surround EX encoded",
				"Dolby Surround EX encoded",
				"Reserved"
				};
		return desc[dsurexmod];
	}

	private String getDheadphonmodString(final int dheadphonmod) {
		final String [] desc = {"Not indicated",
				"Not Dolby Headphone encoded",
				"Dolby Headphone encoded",
				"Reserved"
				};
		return desc[dheadphonmod];
	}


	private String getSurmixlevString(final int surmixlev) {
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


	private String getCmixlevString(final int cmixlev) {
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


	public int getOffset() {
		return offset;
	}


	public int getSyncword() {
		return syncword;
	}


	public int getCrc1() {
		return crc1;
	}


	public int getFscod() {
		return fscod;
	}


	public int getFrmsizecod() {
		return frmsizecod;
	}


	public int getBsid() {
		return bsid;
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


	public int getLfeon() {
		return lfeon;
	}


	public int getDialnorm() {
		return dialnorm;
	}


	public int getLangcode() {
		return langcode;
	}


	public int getLangcod() {
		return langcod;
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


	public int getLangcod2e() {
		return langcod2e;
	}


	public int getLangcod2() {
		return langcod2;
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


	public int getCopyrightb() {
		return copyrightb;
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


	public int getXbsi1e() {
		return xbsi1e;
	}


	public int getDmixmod() {
		return dmixmod;
	}


	public int getXbsi2e() {
		return xbsi2e;
	}


	public int getDsurexmod() {
		return dsurexmod;
	}


	public int getDheadphonmod() {
		return dheadphonmod;
	}


	public int getAdconvtyp() {
		return adconvtyp;
	}


	public int getXbsi2() {
		return xbsi2;
	}


	public int getEncinfo() {
		return encinfo;
	}


	public int getAddbsie() {
		return addbsie;
	}


	public int getAddbsil() {
		return addbsil;
	}


	public static String getFrmsizecodString(final int fscod, final int frmsizecod){
		if((fscod>=0) && (fscod<3) && (frmsizecod>=0) && (frmsizecod <=37)){
			return "Nominal bit rate: "+ac3_bitrate_index[frmsizecod>>>1]+", words/syncframe: "+ac3_size_table[fscod][frmsizecod];
		}else{
			return "illegal values for fscod: "+fscod+" or frmsizecod: "+frmsizecod;
		}

	}


	public static String getBsModString(final int bsmod, final int acmod){
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


	public static String getCentreMixLevelString(final int cmixlev){
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


	public static String getSurroundMixLevelString(final int surmixlev){
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


	public static String getRoomTypeString(final int roomtyp){

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


}