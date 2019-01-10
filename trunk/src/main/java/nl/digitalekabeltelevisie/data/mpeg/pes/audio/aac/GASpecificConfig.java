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
 */package nl.digitalekabeltelevisie.data.mpeg.pes.audio.aac;


import java.util.logging.Logger;

import javax.swing.tree.DefaultMutableTreeNode;

// based on ftp://ftp.tnt.uni-hannover.de/pub/MPEG/audio/mpeg4/documents/w2803/w2803_n.pdf
// chapter 8.1
// ISO/IEC 14496-3:/Amd.1:1999(E)  Coding of audio-visual objects – Part 3: Audio
import nl.digitalekabeltelevisie.controller.*;
import nl.digitalekabeltelevisie.gui.utils.GuiUtils;
import nl.digitalekabeltelevisie.util.BitSource;

public class GASpecificConfig implements TreeNode {

	private int frameLength;
	private int dependsOnCoreCoder;
	
	int samplingFrequencyIndex;
	int channelConfiguration;
	int audioObjectType;
	private int coreCoderDelay;
	private int extensionFlag;
	private int layerNr;
	
	private static final Logger logger = Logger.getLogger(GASpecificConfig.class.getName());


	public GASpecificConfig(BitSource bitSource,  int samplingFrequencyIndex, int channelConfiguration, int audioObjectType) {

		this.samplingFrequencyIndex = samplingFrequencyIndex;
		this.channelConfiguration = channelConfiguration;
		this.audioObjectType = audioObjectType;
		
		frameLength = bitSource.readBits(1);
		dependsOnCoreCoder = bitSource.readBits(1);
		if(dependsOnCoreCoder == 1) {
			coreCoderDelay = bitSource.readBits(14);
		}
		extensionFlag = bitSource.readBits(1);
		if ( channelConfiguration == 0) {
			logger.warning("program_config_element not implemented");
		}
		if ((audioObjectType == 6) || (audioObjectType == 20)) { 
			layerNr= bitSource.readBits(3); 
		} 
		
		if (extensionFlag == 1) {
			logger.warning("extensionFlag ==1 not implemented");
//			if (audioObjectType == 22) {
//				numOfSubFrame; 5 bslbf    
//				layer_length; 11 bslbf   
//			}     
//			if (audioObjectType == 17 || audioObjectType == 19 ||    audioObjectType == 20 || audioObjectType == 23){
//				aacSectionDataResilienceFlag; 1 bslbf    
//				aacScalefactorDataResilienceFlag; 1 bslbf    
//				aacSpectralDataResilienceFlag; 1 bslbf   
//			}     
//			extensionFlag3; 1 bslbf   
//			if (extensionFlag3) {
//				/* tbd in version 3 */     
//			}
		} 
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(int modus) {
		DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("GASpecificConfig"));
		t.add(new DefaultMutableTreeNode(new KVP("FrameLength",frameLength,null)));
		t.add(new DefaultMutableTreeNode(new KVP("DependsOnCoreCoder",dependsOnCoreCoder,null)));
		if(dependsOnCoreCoder == 1) {
			t.add(new DefaultMutableTreeNode(new KVP("coreCoderDelay",coreCoderDelay,null)));
		}
		t.add(new DefaultMutableTreeNode(new KVP("ExtensionFlag",extensionFlag,null)));
		if ( channelConfiguration == 0) {
			t.add(new DefaultMutableTreeNode(GuiUtils.getNotImplementedKVP("program_config_element")));
		}

		if ((audioObjectType == 6) || (audioObjectType == 20)) { 
			t.add(new DefaultMutableTreeNode(new KVP("layerNr",layerNr,null)));
		}
		if (extensionFlag ==1) {
			t.add(new DefaultMutableTreeNode(GuiUtils.getNotImplementedKVP("extensionFlag ==1")));
		}
		return t;
	}

	public int getFrameLength() {
		return frameLength;
	}

	public int getDependsOnCoreCoder() {
		return dependsOnCoreCoder;
	}

	public int getAudioObjectType() {
		return audioObjectType;
	}

	public int getCoreCoderDelay() {
		return coreCoderDelay;
	}

	public int getExtensionFlag() {
		return extensionFlag;
	}

	public int getLayerNr() {
		return layerNr;
	}

}
