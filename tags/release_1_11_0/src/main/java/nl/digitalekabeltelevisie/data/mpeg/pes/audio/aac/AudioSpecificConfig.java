/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2019 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.pes.audio.aac;

import static nl.digitalekabeltelevisie.gui.utils.GuiUtils.getNotImplementedKVP;

import java.util.logging.Logger;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.*;
import nl.digitalekabeltelevisie.util.*;

public class AudioSpecificConfig implements TreeNode {
	
	private static final Logger logger = Logger.getLogger(AudioSpecificConfig.class.getName());
	
	private static LookUpList samplingFrequencyList = new LookUpList.Builder().
	
			add(0x0,"96000").
			add(0x1 ,"88200").
			add(0x2 ,"64000").
			add(0x3 ,"48000"). 
			add(0x4 ,"44100"). 
			add(0x5 ,"32000").
			add(0x6 ,"24000"). 
			add(0x7 ,"22050"). 
			add(0x8 ,"16000"). 
			add(0x9 ,"12000"). 
			add(0xa ,"11025"). 
			add(0xb ,"8000"). 
			add(0xc ,"7350"). 
			add(0xd ,"reserved"). 
			add(0xe ,"reserved"). 
			add(0xf ,"escape value").
			build();

	private static LookUpList audio_object_type_list = new LookUpList.Builder().
			add(0,"Null").
			add(1,"AAC main").
			add(2,"AAC LC").
			add(3,"AAC SSR").
			add(4,"AAC LTP").
			add(5,"(Reserved)").
			add(6,"AAC Scalable").
			add(7,"TwinVQ").
			add(8,"CELP").
			add(9,"HVXC").
			add(10,"(Reserved)").
			add(11,"(Reserved)").
			add(12,"TTSI").
			add(13,"Main synthetic").
			add(14,"Wavetable synthesis").
			add(15,"General MIDI").
			add(16,"Algorithmic Synthesis and Audio FX").
			add(17,"ER AAC LC").
			add(18,"(Reserved)").
			add(19,"AAC LTP").
			add(20,"ER AAC scalable").
			add(21,"ER TwinVQ").
			add(22,"ER BSAC").
			add(23,"ER AAC LD").
			add(24,"ER CELP").
			add(25,"ER HVXC").
			add(26,"ER HILN").
			add(27,"ER Parametric").
			add(28,31,"(Reserved)").
			build();
	

	private int audioObjectType;
	private int samplingFrequencyIndex;
	private int samplingFrequency;
	private int channelConfiguration;
	private int extensionAudioObjectType;
	private int sbrPresentFlag;
	
	private GASpecificConfig gASpecificConfig;

	private int extensionSamplingFrequencyIndex;

	private int extensionSamplingFrequency;

	public AudioSpecificConfig(BitSource bitSource) {
		audioObjectType = getAudioObjectType(bitSource);
		samplingFrequencyIndex = bitSource.readBits(4);
		if (samplingFrequencyIndex == 0xf) {
			samplingFrequency = bitSource.readBits(24);
		}
		channelConfiguration = bitSource.readBits(4);
		
		sbrPresentFlag = -1; 
		
		if ( audioObjectType == 5 ) {
			extensionAudioObjectType = audioObjectType;
			sbrPresentFlag = 1;
			extensionSamplingFrequencyIndex= bitSource.readBits( 4); 
			if ( extensionSamplingFrequencyIndex == 0xf ) {
				extensionSamplingFrequency= bitSource.readBits(24);
			}
			audioObjectType = getAudioObjectType(bitSource);
		} else {
			extensionAudioObjectType = 0;    
		} 
		
		switch (audioObjectType) {
		case 1:
		case 2:
		case 3:
		case 4:
		case 6:
		case 7:
		case 17:
		case 19:
		case 20:
		case 21:
		case 22:
		case 23:

			gASpecificConfig = new GASpecificConfig(bitSource, samplingFrequencyIndex, channelConfiguration,
					audioObjectType);
			break;

		default:
			logger.info("not implemented audioObjectType:"+audioObjectType);
			break;
		}
		// TODO
		//switch (audioObjectType) { 
		//case 17:  case 19:  case 20:  case 21:  case 22:  case 23:  case 24:  case 25:  case 26:  case 27: 
		  
		 // epConfig; 2 bslbf   if ( epConfig == 2 || epConfig == 3 ) {      ErrorProtectionSpecificConfig();     }     if ( epConfig == 3 ) {      directMapping; 1 bslbf    if ( ! directMapping ) {       /* tbd */      }     }    }
//		System.err.println("AudioSpecificConfig");
//		if ( extensionAudioObjectType != 5 && bitSource.available() >= 16 ) { 
//			int syncExtensionType = bitSource.nextBits(11);
//			System.err.println("audioObjectType:"+audioObjectType+", extensionAudioObjectType:"+extensionAudioObjectType+", syncExtensionType = :"+ syncExtensionType +", hex:"+ Utils.toHexString(syncExtensionType, 4)+", bitSource.available():"+bitSource.available());
//		}
		
	}

	private int getAudioObjectType(BitSource bitSource) {
		audioObjectType = bitSource.readBits(5);
		if (audioObjectType == 31) {
			int audioObjectTypeExt = bitSource.readBits(6);
			audioObjectType = 32 + audioObjectTypeExt;
		}
		return audioObjectType;
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(int modus) {
		DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("AudioSpecificConfig"));
		t.add(new DefaultMutableTreeNode(new KVP("audioObjectType",audioObjectType,audio_object_type_list.get(audioObjectType))));
		t.add(new DefaultMutableTreeNode(new KVP("samplingFrequencyIndex",samplingFrequencyIndex,samplingFrequencyList.get(samplingFrequencyIndex))));
		if ( samplingFrequencyIndex==0xf ) {
			t.add(new DefaultMutableTreeNode(new KVP("samplingFrequency",samplingFrequency,null)));
		}
		t.add(new DefaultMutableTreeNode(new KVP("channelConfiguration",channelConfiguration,null)));
		switch (audioObjectType) {
		case 1:
		case 2:
		case 3:
		case 4:
		case 6:
		case 7:
		case 17:
		case 19:
		case 20:
		case 21:
		case 22:
		case 23:
			t.add(gASpecificConfig.getJTreeNode(modus));
			break;

		default:
			t.add(new DefaultMutableTreeNode(getNotImplementedKVP("not implemented audioObjectType:"+audioObjectType)));
			break;
		}

		return t;
	}

	public int getAudioObjectType() {
		return audioObjectType;
	}

	public int getSamplingFrequencyIndex() {
		return samplingFrequencyIndex;
	}

	public int getSamplingFrequency() {
		return samplingFrequency;
	}

	public int getChannelConfiguration() {
		return channelConfiguration;
	}

	public int getExtensionAudioObjectType() {
		return extensionAudioObjectType;
	}

	public int getSbrPresentFlag() {
		return sbrPresentFlag;
	}

	public GASpecificConfig getgASpecificConfig() {
		return gASpecificConfig;
	}

	public int getExtensionSamplingFrequencyIndex() {
		return extensionSamplingFrequencyIndex;
	}

	public int getExtensionSamplingFrequency() {
		return extensionSamplingFrequency;
	}

}
