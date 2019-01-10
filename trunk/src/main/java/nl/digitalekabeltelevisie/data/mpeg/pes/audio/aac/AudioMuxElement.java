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

import java.util.*;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.*;
import nl.digitalekabeltelevisie.util.BitSource;
import static nl.digitalekabeltelevisie.util.Utils.addListJTree;

// based on ISO/IEC 14496-3:2005(E) 1.7.3 Multiplex Layer 
//  (Low-overhead MPEG-4 Audio Transport Multiplex: LATM) 

public class AudioMuxElement implements TreeNode {

	private int muxConfigPresent;
	private int useSameStreamMux;
	private StreamMuxConfig streamMuxConfig;
	
	List<PayloadLengthInfo> payloadLengthInfoList = new ArrayList<>();
	BitSource localBitSource;
	List<PayloadMux> payloadMuxList = new ArrayList<>();

	public AudioMuxElement(BitSource bitSource, int muxConfigPresent, Audio144963Handler audio144963Handler) {
		localBitSource = new BitSource(bitSource);
		streamMuxConfig = audio144963Handler.getStreamMuxConfig();
		this.muxConfigPresent = muxConfigPresent;
		if( muxConfigPresent == 1 ) {
			useSameStreamMux = bitSource.readBits(1);
			if(useSameStreamMux != 1) {
				streamMuxConfig = new StreamMuxConfig(bitSource);
				audio144963Handler.setStreamMuxConfig(streamMuxConfig);
				
			}
		}
		if ((streamMuxConfig != null)&&(streamMuxConfig.getAudioMuxVersionA() == 0)) {
			for (int i = 0; i <= streamMuxConfig.getNumSubFrames(); i++) {
				PayloadLengthInfo payloadLengthInfo = new PayloadLengthInfo(bitSource,streamMuxConfig);   
				payloadLengthInfoList.add(payloadLengthInfo);
				PayloadMux payloadMux = new PayloadMux(bitSource,streamMuxConfig,payloadLengthInfo);
				payloadMuxList.add(payloadMux);
			}     
			if (streamMuxConfig.getOtherDataPresent()==1) {
//				for(int i = 0; i < otherDataLenBits; I++) {
//					otherDataBit; 1 bslbf    
//				}     
			}
		} // else{     /* tbd */    }    
		bitSource.skiptoByteBoundary();//  ByteAlign(); 
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(int modus) {
		DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("AudioMuxElement("+muxConfigPresent+")"));
		if( muxConfigPresent == 1 ) {
			t.add(new DefaultMutableTreeNode(new KVP("useSameStreamMux",useSameStreamMux,null)));
			if(useSameStreamMux != 1) {
				t.add(streamMuxConfig.getJTreeNode(modus));
			}
		}
		if ((streamMuxConfig != null)&&(streamMuxConfig.getAudioMuxVersionA() == 0)) {
			addListJTree(t, payloadLengthInfoList, modus, "PayloadLengthInfoList");
			addListJTree(t, payloadMuxList, modus, "PayloadMuxList");
		}
		return t;
	}

	public int getMuxConfigPresent() {
		return muxConfigPresent;
	}

	public int getUseSameStreamMux() {
		return useSameStreamMux;
	}

	public StreamMuxConfig getStreamMuxConfig() {
		return streamMuxConfig;
	}

	public List<PayloadLengthInfo> getPayloadLengthInfoList() {
		return payloadLengthInfoList;
	}

	public List<PayloadMux> getPayloadMuxList() {
		return payloadMuxList;
	}

}
