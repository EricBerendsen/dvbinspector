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

	// Alternate bit stream syntax
	private int xbsi1e;
	private int xbsi2e;
	private int xbsi2;
	private int encinfo;


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
			s.add(new DefaultMutableTreeNode(new KVP("dsurmod",dsurmod,"Dolby Surround mode: "+getDsurmodString(dsurmod))));
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
			s.add(new DefaultMutableTreeNode(new KVP("roomtyp","Room type: "+roomtyp,getRoomTypeString(roomtyp))));
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
				s.add(new DefaultMutableTreeNode(new KVP("roomtyp2",roomtyp2,getRoomTypeString(roomtyp))));
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
			s.add(new DefaultMutableTreeNode(new KVP("xbsi1e",xbsi1e,"Extra bitstream information #1 "+doesExistString(xbsi1e))));
			if(xbsi1e==1){
				s.add(new DefaultMutableTreeNode(new KVP("dmixmod",dmixmod,"Preferred stereo downmix mode: "+getDmixmodString(dmixmod))));
				s.add(new DefaultMutableTreeNode(new KVP("ltrtcmixlev",ltrtcmixlev,"Lt/Rt center mix level: "+getCmixlevString(ltrtcmixlev))));
				s.add(new DefaultMutableTreeNode(new KVP("ltrtsurmixlev",ltrtsurmixlev,"Lt/Rt surround mix level: "+getSurmixlevString(ltrtsurmixlev))));
				s.add(new DefaultMutableTreeNode(new KVP("lorocmixlev",lorocmixlev,"Lo/Ro center mix level: "+getCmixlevString(lorocmixlev))));
				s.add(new DefaultMutableTreeNode(new KVP("lorosurmixlev",lorosurmixlev,"Lo/Ro surround mix level: "+getSurmixlevString(lorosurmixlev))));
			}
			s.add(new DefaultMutableTreeNode(new KVP("xbsi2e",xbsi2e,"Extra bit stream information #2 "+doesExistString(xbsi2e))));
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


	public int getXbsi1e() {
		return xbsi1e;
	}


	public int getXbsi2e() {
		return xbsi2e;
	}


	public int getXbsi2() {
		return xbsi2;
	}


	public int getEncinfo() {
		return encinfo;
	}


}