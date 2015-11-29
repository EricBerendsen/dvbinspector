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
import nl.digitalekabeltelevisie.gui.utils.GuiUtils;

/**
 *
 * Based on ETSI TS 102 366 V1.2.1 (2008-08) Digital Audio Compression (AC-3, Enhanced AC-3) Annex E
 * @author Eric
 *
 */
public class EAC3SyncFrame extends AbstractAC3SyncFrame implements TreeNode {

	private int strmtyp;

	private int lfemixlevcode;
	private int lfemixlevcod;

	private int substreamid;

	private int frmsiz;
	private int fscod2;
	private int numblkscod;
	private int chanmape;
	private int chanmap;

	private int mixmdate;

	private int pgmscle;
	private int pgmscl;
	private int pgmscl2e;
	private int pgmscl2;

	private int extpgmscle;
	private int extpgmscl;

	private int mixdef;
	private int premixcmpsel;
	private int drcsrc;
	private int premixcmpscl;

	private int mixdata;
	private int mixdeflen;

	private int paninfoe;
	private int panmean;
	private int paninfo;
	private int paninfo2e;
	private int panmean2;
	private int paninfo2;

	private int frmmixcfginfoe;
	private int[] blkmixcfginfo;
	private int[] blkmixcfginfoe;

	private int infomdate;

	private int adconvtyp2;
	private int sourcefscod;
	private int blkid;
	private int convsync;


	public EAC3SyncFrame(final byte[] data, final int offset) {
		super(data,offset);
		syncword = bs.readBits(16);

		strmtyp = bs.readBits(2);
		substreamid = bs.readBits(3);
		frmsiz = bs.readBits(11);
		fscod = bs.readBits(2);
		if(fscod == 0x3)
		{
			fscod2= bs.readBits(2);
			numblkscod = 0x3 ; /* six blocks per frame */
		}else{
			numblkscod= bs.readBits(2);
		}
		acmod= bs.readBits(3);
		lfeon= bs.readBits(1);

		bsid = bs.readBits(5);
		dialnorm= bs.readBits(5);
		compre= bs.readBits(1);
		if(compre==1){
			compr= bs.readBits(8);
		}

		if(acmod == 0){ /* if 1+1 mode (dual mono, so some items need a second value) */
			dialnorm2 = bs.readBits(5);
			compr2e = bs.readBits(1);
			if(compr2e==1){
				compr2 = bs.readBits(8);
			}
		}
		if(strmtyp == 0x1){ /* if dependent stream */

			chanmape = bs.readBits(1);
			if(chanmape==1) {
				chanmap = bs.readBits(16);
			}
		}
		mixmdate = bs.readBits(1);
		if(mixmdate==1) /* mixing metadata */
		{
			if(acmod > 0x2) /* if more than 2 channels */ {
				dmixmod = bs.readBits(2);
			}
			if(((acmod & 0x1)!=0) && (acmod > 0x2)) /* if three front channels exist */
			{
				ltrtcmixlev = bs.readBits(3);
				lorocmixlev = bs.readBits(3);
			}
			if((acmod & 0x4)!=0) /* if a surround channel exists */
			{
				ltrtsurmixlev = bs.readBits(3);
				lorosurmixlev = bs.readBits(3);
			}
			if(lfeon!=0) /* if the LFE channel exists */
			{
				lfemixlevcode = bs.readBits(1);
				if(lfemixlevcode!=0) {
					lfemixlevcod = bs.readBits(5);
				}
			}
			if(strmtyp == 0x0) /* if independent stream */
			{
				pgmscle = bs.readBits(1);
				if(pgmscle!=0) {
					pgmscl = bs.readBits(6);
				}
				if(acmod == 0x0) /* if 1+1 mode (dual mono, so some items need a second value) */
				{
					pgmscl2e = bs.readBits(1);
					if(pgmscl2e!=0) {
						pgmscl2 = bs.readBits(6);
					}
				}
				extpgmscle = bs.readBits(1);
				if(extpgmscle!=0) {
					extpgmscl = bs.readBits(6);
				}
				mixdef = bs.readBits(2);
				if(mixdef == 0x1) /* mixing option 2 */
				{
					premixcmpsel = bs.readBits(1);
					drcsrc = bs.readBits(1);
					premixcmpscl = bs.readBits(3);
				}
				else if(mixdef == 0x2) /* mixing option 3 */ {
					mixdata = bs.readBits(12);
				}else if(mixdef == 0x3) /* mixing option 4 */
				{
					mixdeflen = bs.readBits(5);
					// TODO implement mixing type 3, for now just read all bytes and ignore
					for (int i = 0; i < (mixdeflen+2); i++) {
						bs.readBits(8);
					}
//					mixdata2e = bs.readBits(1);
//					if (mixdata2e!=0)
//					{
//						premixcmpsel = bs.readBits(1);
//						drcsrc = bs.readBits(1);
//						premixcmpscl = bs.readBits(3);
//						extpgmlscle = bs.readBits(1);
//						if (extpgmlscle!=0){ extpgmlscl = bs.readBits(4);}
//						extpgmcscle = bs.readBits(1);
//						if (extpgmcscle!=0){ extpgmcscl = bs.readBits(4);}
//						extpgmrscle = bs.readBits(1);
//						if (extpgmrscle!=0){ extpgmrscl = bs.readBits(4);}
//						extpgmlsscle = bs.readBits(1);
//						if (extpgmlsscle!=0){ extpgmlsscl = bs.readBits(4);}
//						extpgmrsscle = bs.readBits(1);
//						if (extpgmrsscle!=0){ extpgmrsscl = bs.readBits(4);}
//						extpgmlfescle = bs.readBits(1);
//						if (extpgmlfescle!=0){ extpgmlfescl = bs.readBits(4);}
//						dmixscle = bs.readBits(1);
//						if (dmixscle!=0){ dmixscl = bs.readBits(4);}
//						addche = bs.readBits(1);
//						if (addche!=0)
//						{
//							extpgmaux1scle = bs.readBits(1);
//							if (extpgmaux1scle!=0){ extpgmaux1scl = bs.readBits(4);}
//							extpgmaux2scle = bs.readBits(1);
//							if (extpgmaux2scle!=0){ extpgmaux2scl = bs.readBits(4);}
//						}
//						}
//						mixdata3e = bs.readBits(1);
//						if (mixdata3e!=0)
//						{
//							spchdat = bs.readBits(5);
//							addspchdate = bs.readBits(1);
//						if (addspchdate!=0)
//						{
//							spchdat1 = bs.readBits(5);
//							spchan1att = bs.readBits(2);
//							addspchdat1e = bs.readBits(1);
//							if (addspdat1e!=0)
//							{
//								spchdat2 = bs.readBits(5);
//								spchan2att = bs.readBits(3);
//							}
//						}
//					}
//					mixdata  = bs.readBits(8*(mixdeflen+2));
//					//mixdatafill  = bs.readBits(0 - 7);
				}
				if(acmod < 0x2) /* if mono or dual mono source */
				{
					paninfoe = bs.readBits(1);
					if(paninfoe!=0)
					{
						panmean = bs.readBits(8);
						paninfo = bs.readBits(6);
					}
					if(acmod == 0x0) /* if 1+1 mode (dual mono, so some items need a second value) */
					{
						paninfo2e = bs.readBits(1);
						if(paninfo2e!=0)
						{
							panmean2 = bs.readBits(8);
							paninfo2 = bs.readBits(6);
						}
					}
				}
				frmmixcfginfoe = bs.readBits(1);
				if(frmmixcfginfoe!=0){ /* mixing configuration information */
					if(numblkscod == 0x0) {
						blkmixcfginfo = new int[1];
						blkmixcfginfo[0] = bs.readBits(5);
					}else{
						int number_of_blocks_per_sync_frame = getNumBlocks(numblkscod);
						blkmixcfginfo = new int[number_of_blocks_per_sync_frame];
						blkmixcfginfoe = new int[number_of_blocks_per_sync_frame];
						for(int blk = 0; blk < number_of_blocks_per_sync_frame; blk++){
							blkmixcfginfoe[blk] = bs.readBits(1);
							if(blkmixcfginfoe[blk]!=0){
								blkmixcfginfo[blk] = bs.readBits(5);
							}
						}
					}
				}
			}// END if(strmtyp == 0x0) /* if independent stream */
			infomdate = bs.readBits(1);
			if(infomdate!=0){ /* informational metadata */
				bsmod = bs.readBits(3);
				copyrightb = bs.readBits(1);
				origbs = bs.readBits(1);
				if(acmod == 0x2){ /* if in 2/0 mode */
					dsurmod = bs.readBits(2);
					dheadphonmod = bs.readBits(2);
				}
				if(acmod >= 0x6){ /* if both surround channels exist */
					dsurexmod = bs.readBits(2);
				}
				audprodie = bs.readBits(1);
				if(audprodie!=0){
					mixlevel = bs.readBits(5);
					roomtyp = bs.readBits(2);
					adconvtyp = bs.readBits(1);
				}
				if(acmod == 0x0){ /* if 1+1 mode (dual mono, so some items need a second value) */
					audprodi2e = bs.readBits(1);
					if(audprodi2e!=0){
						mixlevel2 = bs.readBits(5);
						roomtyp2 = bs.readBits(2);
						adconvtyp2 = bs.readBits(1);
					}
				}
				if(fscod < 0x3){ /* if not half sample rate */
					sourcefscod = bs.readBits(1);
				}
			}
			if( (strmtyp == 0x0) && (numblkscod != 0x3) ) {
				convsync = bs.readBits(1);
			}
			if(strmtyp == 0x2){ /* if bit stream converted from AC-3 */
				if(numblkscod == 0x3) /* 6 blocks per frame */ {
					blkid = 1;
				}else {
					blkid = bs.readBits(1);
				}
				if(blkid!=0) {
					frmsizecod = bs.readBits(6);
				}
			}
			addbsie = bs.readBits(1);
			if(addbsie!=0){
				addbsil = bs.readBits(6);
				//addbsi = bs.readBits((addbsil+1)*8);
			}
		}
	}



	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode s = new DefaultMutableTreeNode(new KVP("EAC3 SyncFrame"));
		s.add(new DefaultMutableTreeNode(new KVP("syncword",syncword,"must be 0x0B77")));
		s.add(new DefaultMutableTreeNode(new KVP("strmtyp",strmtyp,"Stream type: "+getStreamTypeString(strmtyp))));
		s.add(new DefaultMutableTreeNode(new KVP("substreamid",substreamid,"Substream identification")));
		s.add(new DefaultMutableTreeNode(new KVP("frmsiz",frmsiz,"Frame size in 16 bit words: "+(frmsiz+1))));
		s.add(new DefaultMutableTreeNode(new KVP("fscod",fscod,"Sample rate code: "+getSampleRateCodeString(fscod))));
		if(fscod == 0x3)
		{
			s.add(new DefaultMutableTreeNode(new KVP("fscod2",fscod2,"Reduced sampling rate: "+getReducedSampleRateCodeString(fscod2))));
		}else{
			s.add(new DefaultMutableTreeNode(new KVP("numblkscod",numblkscod,getNumblkscodString(numblkscod))));
		}
		s.add(new DefaultMutableTreeNode(new KVP("acmod",acmod,"Audio coding mode: "+ getACModString(acmod))));
		s.add(new DefaultMutableTreeNode(new KVP("lfeon",lfeon,getLfeOnString(lfeon))));

		s.add(new DefaultMutableTreeNode(new KVP("bsid",bsid,getBsidString(bsid))));
		s.add(new DefaultMutableTreeNode(new KVP("dialnorm",dialnorm,getDialNormString(dialnorm))));
		s.add(new DefaultMutableTreeNode(new KVP("compre",compre,getCompreString(compre))));
		if(compre==1){
			s.add(new DefaultMutableTreeNode(new KVP("compr",compr,"Compression gain word")));
		}
		if(acmod == 0){ /* if 1+1 mode (dual mono, so some items need a second value) */
			s.add(new DefaultMutableTreeNode(new KVP("dialnorm2",dialnorm2,"Dialogue normalization: "+((dialnorm==0)?"Reserved":"-"+dialnorm+" dB"))));
			s.add(new DefaultMutableTreeNode(new KVP("compre2",compr2e,compr2e==1?"Compression gain word exists":"No compression gain word exists")));
			if(compr2e==1){
				s.add(new DefaultMutableTreeNode(new KVP("compr2",compr2,"Compression gain word")));
			}
		}
		if(strmtyp == 0x1){ /* if dependent stream */
			s.add(new DefaultMutableTreeNode(new KVP("chanmape",chanmape,"Custom channel map "+doesExistString(chanmape)+" (Only dependent substreams can have a custom channel map)")));
			if(chanmape==1) {
				s.add(new DefaultMutableTreeNode(new KVP("chanmap",chanmap,null)));
				s.add(new DefaultMutableTreeNode(GuiUtils.getNotImplementedKVP("chanmap")));
			}
		}
		s.add(new DefaultMutableTreeNode(new KVP("mixmdate",mixmdate,mixmdate==1?"mixing metadata exists":"mixing metadata does not exist")));

		if(mixmdate==1) /* mixing metadata */
		{
			if(acmod > 0x2) /* if more than 2 channels */ {
				s.add(new DefaultMutableTreeNode(new KVP("dmixmod",dmixmod,"Preferred stereo downmix mode: "+getDmixmodString(dmixmod))));
			}
			if(((acmod & 0x1)!=0) && (acmod > 0x2)) /* if three front channels exist */
			{
				s.add(new DefaultMutableTreeNode(new KVP("ltrtcmixlev",ltrtcmixlev,"Lt/Rt center mix level: "+getCmixlevString(ltrtcmixlev))));
				s.add(new DefaultMutableTreeNode(new KVP("lorocmixlev",lorocmixlev,"Lo/Ro center mix level: "+getCmixlevString(lorocmixlev))));
			}
			if((acmod & 0x4)!=0) /* if a surround channel exists */
			{
				s.add(new DefaultMutableTreeNode(new KVP("ltrtsurmixlev",ltrtsurmixlev,"Lt/Rt surround mix level: "+getSurmixlevString(ltrtsurmixlev))));
				s.add(new DefaultMutableTreeNode(new KVP("lorosurmixlev",lorosurmixlev,"Lo/Ro surround mix level: "+getSurmixlevString(lorosurmixlev))));
			}
			if(lfeon!=0) /* if the LFE channel exists */
			{
				s.add(new DefaultMutableTreeNode(new KVP("lfemixlevcode",lfemixlevcode,"LFE mix level code "+doesExistString(lfemixlevcode))));
				if(lfemixlevcode!=0) {
					s.add(new DefaultMutableTreeNode(new KVP("lfemixlevcod",lfemixlevcod,"LFE mix level (dB): "+(10-lfemixlevcod))));
				}
			}
			if(strmtyp == 0x0) /* if independent stream */
			{
				s.add(new DefaultMutableTreeNode(new KVP("pgmscle",pgmscle,"Programme scale factor "+doesExistString(pgmscle))));
				if(pgmscle!=0) {
					s.add(new DefaultMutableTreeNode(new KVP("pgmscl",pgmscl,"Programme scale factor: "+getProgrammeScaleFactorString(pgmscl))));
				}
				if(acmod == 0x0) /* if 1+1 mode (dual mono, so some items need a second value) */
				{
					s.add(new DefaultMutableTreeNode(new KVP("pgmscl2e",pgmscl2e,"Programme scale factor #2 "+doesExistString(pgmscl2e))));
					if(pgmscl2e!=0) {
						s.add(new DefaultMutableTreeNode(new KVP("pgmscl2",pgmscl2,"Programme scale factor #2: "+getProgrammeScaleFactorString(pgmscl2))));
					}
				}
				s.add(new DefaultMutableTreeNode(new KVP("extpgmscle",extpgmscle,"External programme scale factor "+doesExistString(extpgmscle))));
				if(extpgmscle!=0) {
					extpgmscl = bs.readBits(6);
					s.add(new DefaultMutableTreeNode(new KVP("extpgmscl",extpgmscl,"External programme scale factor: "+getProgrammeScaleFactorString(extpgmscl))));
				}
				s.add(new DefaultMutableTreeNode(new KVP("mixdef",mixdef,"Mix control type: "+getMixControlTypeString(mixdef))));
				if(mixdef == 0x1) /* mixing option 2 */
				{
					s.add(new DefaultMutableTreeNode(new KVP("premixcmpsel",premixcmpsel,null)));
					s.add(new DefaultMutableTreeNode(new KVP("drcsrc",drcsrc,null)));
					s.add(new DefaultMutableTreeNode(new KVP("premixcmpscl",premixcmpscl,null)));
				}
				else if(mixdef == 0x2) /* mixing option 3 */ {
					s.add(new DefaultMutableTreeNode(new KVP("mixdata",mixdata,null)));
				}else if(mixdef == 0x3) /* mixing option 4 */
				{
					s.add(new DefaultMutableTreeNode(new KVP("mixdeflen",mixdeflen,"Length of mixing parameter data field: "+(mixdeflen+2)+" bytes")));
					s.add(new DefaultMutableTreeNode(GuiUtils.getNotImplementedKVP("mixing option 4")));
				}
				if(acmod < 0x2) /* if mono or dual mono source */
				{
					s.add(new DefaultMutableTreeNode(new KVP("paninfoe",paninfoe,"Pan information "+doesExistString(paninfoe))));
					if(paninfoe!=0)
					{
						s.add(new DefaultMutableTreeNode(new KVP("panmean",panmean,"Pan mean direction index: "+(panmean*1.5)+" degrees")));
						s.add(new DefaultMutableTreeNode(new KVP("paninfo",paninfo,"paninfo - reserved")));
					}
					if(acmod == 0x0) /* if 1+1 mode (dual mono, so some items need a second value) */
					{
						s.add(new DefaultMutableTreeNode(new KVP("paninfo2e",paninfo2e,"Pan information #2 "+doesExistString(paninfo2e))));
						if(paninfo2e!=0)
						{
							s.add(new DefaultMutableTreeNode(new KVP("panmean2",panmean2,"Pan mean direction index: "+(panmean2*1.5)+" degrees")));
							s.add(new DefaultMutableTreeNode(new KVP("paninfo2",paninfo2,"paninfo2 - reserved")));
						}
					}
				}

				s.add(new DefaultMutableTreeNode(new KVP("frmmixcfginfoe",frmmixcfginfoe,"Frame mixing configuration information "+doesExistString(frmmixcfginfoe))));
				if(frmmixcfginfoe!=0){ /* mixing configuration information */
					if(numblkscod == 0x0) {
						s.add(new DefaultMutableTreeNode(new KVP("blkmixcfginfo",blkmixcfginfo[0],"Block mixing configuration information")));
					}else{
						int number_of_blocks_per_sync_frame = getNumBlocks(numblkscod);
						for(int blk = 0; blk < number_of_blocks_per_sync_frame; blk++){
							s.add(new DefaultMutableTreeNode(new KVP("blkmixcfginfoe["+blk+"]",blkmixcfginfoe[blk],"Block mixing configuration information["+blk+"] "+doesExistString(blkmixcfginfoe[blk]))));
							if(blkmixcfginfoe[blk]!=0){
								s.add(new DefaultMutableTreeNode(new KVP("blkmixcfginfo["+blk+"]",blkmixcfginfo[blk],"Block mixing configuration information")));
							}
						}
					}
				}


			}// END if(strmtyp == 0x0) /* if independent stream */
			s.add(new DefaultMutableTreeNode(new KVP("infomdate",infomdate,"Informational metadata "+doesExistString(infomdate))));
			if(infomdate!=0){ /* informational metadata */
				s.add(new DefaultMutableTreeNode(new KVP("bsmod",bsmod,"Bit stream mode: "+getBsModString(bsmod, acmod))));
				s.add(new DefaultMutableTreeNode(new KVP("copyrightb",copyrightb,"Copyright bit: "+(copyrightb==1?"information in the bit stream is indicated as protected by copyright":"information is not indicated as protected"))));
				s.add(new DefaultMutableTreeNode(new KVP("origbs",origbs,origbs==1?"original bit stream":"copy of another bit stream")));
				if(acmod == 0x2){ /* if in 2/0 mode */

					s.add(new DefaultMutableTreeNode(new KVP("dsurmod",dsurmod,"Dolby Surround mode: "+getDsurmodString(dsurmod))));
					s.add(new DefaultMutableTreeNode(new KVP("dheadphonmod",dheadphonmod,"Dolby Headphone mode: "+getDheadphonmodString(dheadphonmod))));
				}
				if(acmod >= 0x6){ /* if both surround channels exist */
					s.add(new DefaultMutableTreeNode(new KVP("dsurexmod",dsurexmod,"Dolby Surround EX mode: "+getDsurexmodString(dsurexmod))));
				}
				s.add(new DefaultMutableTreeNode(new KVP("audprodie",audprodie,"Audio production information "+doesExistString(audprodie))));

				if(audprodie!=0){
					s.add(new DefaultMutableTreeNode(new KVP("mixlevel",mixlevel,"Mixing level: "+(80+mixlevel)+" dB")));
					s.add(new DefaultMutableTreeNode(new KVP("roomtyp","Room type: "+roomtyp,getRoomTypeString(roomtyp))));
					s.add(new DefaultMutableTreeNode(new KVP("adconvtyp",adconvtyp,"A/D converter type: "+(adconvtyp==1?"HDCD":"standard"))));
				}
				if(acmod == 0x0){ /* if 1+1 mode (dual mono, so some items need a second value) */
					s.add(new DefaultMutableTreeNode(new KVP("mixdata",mixdata,null)));
					audprodi2e = bs.readBits(1);
					if(audprodi2e!=0){
						s.add(new DefaultMutableTreeNode(new KVP("mixlevel2",mixlevel2,"peak mixing level during the final audio mixing session: "+(80+mixlevel2)+" dB")));
						s.add(new DefaultMutableTreeNode(new KVP("roomtyp2",roomtyp2,getRoomTypeString(roomtyp))));
						s.add(new DefaultMutableTreeNode(new KVP("adconvtyp2",adconvtyp2,"A/D converter type: "+(adconvtyp2==1?"HDCD":"standard"))));
					}
				}
				if(fscod < 0x3){ /* if not half sample rate */
					s.add(new DefaultMutableTreeNode(new KVP("sourcefscod",sourcefscod,"Source sample rate code"+(sourcefscod==1?" the source material was sampled at twice the rate indicated by fscod":" the source material was NOT sampled at twice the rate indicated by fscod"))));
				}
				if( (strmtyp == 0x0) && (numblkscod != 0x3) ) {
					s.add(new DefaultMutableTreeNode(new KVP("convsync",convsync,"Converter synchronization flag. This bit is used for synchronization by a device that converts an Enhanced AC-3 bit stream to a bit stream compliant with a legacy AC-3 decoder")));
				}
				if(strmtyp == 0x2){ /* if bit stream converted from AC-3 */
					if(numblkscod == 0x3) /* 6 blocks per frame */ {
						s.add(new DefaultMutableTreeNode(new KVP("blkid",blkid,"Block identification: the first block in this Enhanced AC-3 frame was the first block in the original standard AC-3 frame: implicit because numblkscod == 0x3 (6 blocks per frame)")));
					}else {
						s.add(new DefaultMutableTreeNode(new KVP("blkid",blkid,"Block identification: "+(blkid==1?"the first block in this Enhanced AC-3 frame was the first block in the original standard AC-3 frame":"the first block in this Enhanced AC-3 frame was NOT the first block in the original standard AC-3 frame"))));
					}
					if(blkid!=0) {
						s.add(new DefaultMutableTreeNode(new KVP("frmsizecod",frmsizecod,"Frame size code: "+getFrmsizecodString(fscod, frmsizecod))));
					}
				}
				addbsie = bs.readBits(1);
				s.add(new DefaultMutableTreeNode(new KVP("addbsie",addbsie,addbsie==1?"Additional bit stream information exists":"Additional bit stream information does not exist")));
				if(addbsie!=0){
					s.add(new DefaultMutableTreeNode(new KVP("addbsil",addbsil,"Additional bit stream information length")));
				}

			}
		}

		return s;
	}


	public static String getMixControlTypeString(int mixdef) {
		final String [] desc = {"mixing option 1, no additional bits",
				"mixing option 2, 5 bits",
				"mixing option 3, 12 bits reserved",
				"mixing option 4, 16-264 bits reserved by mixdeflen"
				};
		return desc[mixdef];

	}


	public static String getProgrammeScaleFactorString(int pgmscl) {
		if(pgmscl==0){
			return "mute";
		}else{
			return (pgmscl-51)+" dB";
		}
	}


	public static String getStreamTypeString(final int strmtyp) {
		switch (strmtyp) {
		case 0:
			return "Type 0: These frames comprise an independent stream or substream. The programme may be decoded independently of any other substreams that might exist in the bit stream.";
		case 1:
			return "Type 1: These frames comprise a dependent substream. The programme must be decoded in conjunction with the independent substream with which it is associated.";
		case 2:
			return "Type 2: These frames comprise an independent stream or substream that was previously coded in AC-3. Type 2 streams must be independently decodable, and may not have any dependent streams associated with them.";
		case 3:
			return "Type 3: Reserved";

		default:
			return "illegal value";
		}
	}

	public static String getNumblkscodString(final int numblkscod) {
		switch (numblkscod) {
		case 0:
			return "1 block per syncframe";
		case 1:
			return "2 blocks per syncframe";
		case 2:
			return "3 blocks per syncframe";
		case 3:
			return "6 blocks per syncframe";

		default:
			return "illegal value";
		}
	}

	public static String getReducedSampleRateCodeString(final int fscod2) {
		switch (fscod2) {
		case 0:
			return "24 kHz";
		case 1:
			return "22,05 kHz";
		case 2:
			return "16 kHz";
		case 3:
			return "Reserved";

		default:
			return "illegal value";
		}
	}

	public static  int getNumBlocks(int numblkscod) {
		switch (numblkscod) {
		case 0:
			return 1;
		case 1:
			return 2;
		case 2:
			return 3;
		case 3:
			return 6;

		default:
			return 0;
		}
	}



	public int getStrmtyp() {
		return strmtyp;
	}



	public int getLfemixlevcode() {
		return lfemixlevcode;
	}



	public int getLfemixlevcod() {
		return lfemixlevcod;
	}



	public int getSubstreamid() {
		return substreamid;
	}



	public int getFrmsiz() {
		return frmsiz;
	}



	public int getFscod2() {
		return fscod2;
	}



	public int getNumblkscod() {
		return numblkscod;
	}



	public int getChanmape() {
		return chanmape;
	}



	public int getChanmap() {
		return chanmap;
	}



	public int getMixmdate() {
		return mixmdate;
	}



	public int getPgmscle() {
		return pgmscle;
	}



	public int getPgmscl() {
		return pgmscl;
	}



	public int getPgmscl2e() {
		return pgmscl2e;
	}



	public int getPgmscl2() {
		return pgmscl2;
	}



	public int getExtpgmscle() {
		return extpgmscle;
	}



	public int getExtpgmscl() {
		return extpgmscl;
	}



	public int getMixdef() {
		return mixdef;
	}



	public int getPremixcmpsel() {
		return premixcmpsel;
	}



	public int getDrcsrc() {
		return drcsrc;
	}



	public int getPremixcmpscl() {
		return premixcmpscl;
	}



	public int getMixdata() {
		return mixdata;
	}



	public int getMixdeflen() {
		return mixdeflen;
	}



	public int getPaninfoe() {
		return paninfoe;
	}



	public int getPanmean() {
		return panmean;
	}



	public int getPaninfo() {
		return paninfo;
	}



	public int getPaninfo2e() {
		return paninfo2e;
	}



	public int getPanmean2() {
		return panmean2;
	}



	public int getPaninfo2() {
		return paninfo2;
	}



	public int getFrmmixcfginfoe() {
		return frmmixcfginfoe;
	}



	public int[] getBlkmixcfginfo() {
		return blkmixcfginfo;
	}



	public int[] getBlkmixcfginfoe() {
		return blkmixcfginfoe;
	}



	public int getInfomdate() {
		return infomdate;
	}



	public int getAdconvtyp2() {
		return adconvtyp2;
	}



	public int getSourcefscod() {
		return sourcefscod;
	}



	public int getBlkid() {
		return blkid;
	}



	public int getConvsync() {
		return convsync;
	}

}