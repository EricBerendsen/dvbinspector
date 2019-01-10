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
import static nl.digitalekabeltelevisie.util.Utils.addListJTree;

import java.util.*;
import java.util.logging.Logger;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.*;
import nl.digitalekabeltelevisie.util.*;

public class StreamMuxConfig implements TreeNode {
	
	 // based on SO/IEC 14496-3:2005(E) 1.7.3.1 Table 1.29 – Syntax of StreamMuxConfig() 
	
	private static final Logger logger = Logger.getLogger(StreamMuxConfig.class.getName());
	
	static LookUpList frameLengthTypeList = new LookUpList.Builder().

		add(0 ,"Payload with variable frame length. The payload length in bytes is directly specified with 8-bit codes in PayloadLengthInfo()").
		add(1 ,"Payload with fixed frame length. The payload length in bits is specified with frameLength in StreamMuxConfig()").
		add(2 ,"Reserved").
		add(3 ,"Payload for a CELP object with one of 2 kinds of frame length. The payload length is specified by two tableindices, namely CELPframeLengthTableIndex and MuxSlotLengthCoded").
		add(4 ,"Payload for a CELP or ER_CELP object with fixed frame length. CELPframeLengthTableIndex specifies the payload length").
		add(5 ,"Payload for an ER_CELP object with one of 4 kinds of frame length. The payload length is specified by two tableindices, namely CELPframeLengthTableIndex and MuxSlotLengthCoded").
		add(6 ,"Payload for a HVXC or ER_HVXC object with fixed frame length. HVXCframeLengthTableIndex specifies the payload length").
		add(7 ,"Payload for an HVXC or ER_HVXC object with one of 4 kinds of frame length. The payload length is specified by two table-indices, namely HVXCframeLengthTableIndex and MuxSlotLengthCoded").
		build();
 

	
// 	helper variables
	int[] progSIndx = new int[64];
	int[] laySIndx= new int[8];
	int [] [] streamID; 
	int [] frameLengthType = new int[16]; // TODO assumption no of streams,  think we are safe
	int [] latmBufferFullness = new int[64]; 
	
	

	
	class ProgramConfig implements TreeNode {
		
		private List<LayerConfig> layerConfigList = new ArrayList<>();
		
	
		public List<LayerConfig> getLayerConfigList() {
			return layerConfigList;
		}

		class LayerConfig implements TreeNode {
			
			AudioSpecificConfig audioSpecificConfig;
			
			public AudioSpecificConfig getAudioSpecificConfig() {
				return audioSpecificConfig;
			}

			public int getProg() {
				return prog;
			}

			public int getLay() {
				return lay;
			}

			public int getUseSameConfig() {
				return useSameConfig;
			}

			public BitSource getLocalBitSource3() {
				return localBitSource3;
			}

			private int prog;
			private int lay;

			private int useSameConfig;
			private BitSource localBitSource3;
			
			public LayerConfig(BitSource bitSource, int prog, int lay) {
				localBitSource3 = new BitSource(bitSource);
				this.prog = prog;
				this.lay = lay;
			
				progSIndx[streamCnt] = prog;
				laySIndx[streamCnt] = lay; 
				streamID [ prog][ lay] = streamCnt++; 
				
				if (prog == 0 & lay == 0) {
					useSameConfig = 0;
				} else {
					useSameConfig = bitSource.readBits(1);
				}
				if (useSameConfig == 0) {
					if (audioMuxVersion == 0) {
						audioSpecificConfig = new AudioSpecificConfig(bitSource);
					}else{
						logger.info("LayerConfig: }else{ of if (audioMuxVersion == 0)  not implemented");

//						ascLen = LatmGetValue();
//						ascLen -= AudioSpecificConfig();  Note 1       
//						fillBits; ascLen bslbf      
					}
				}
				frameLengthType[streamID [prog][ lay]] = bitSource.readBits(3);

				if (frameLengthType[streamID[prog][lay]] == 0) {
					latmBufferFullness[streamID[prog][lay]] = bitSource.readBits(8);
					
					// if ( allStreamsSameTimeFraming ==0) {
					// if ((AudioObjectType[lay] == 6 ||
					// 		AudioObjectType[lay] == 20) &&
					// 		(AudioObjectType[lay-1] == 8 ||
					// 		AudioObjectType[lay-1] == 24)) {
					// coreFrameOffset; 6 uimsbf }}}
				   //    } else 
					//if (frameLengthType[streamID[prog][ lay]] == 1) {
					//frameLength[streamID[prog][lay]]; 9 uimsbf    
					//} else if ( frameLengthType[streamID[prog][ lay]] == 4 ||
					//  frameLengthType[streamID[prog][ lay]] == 5 || 
					//  frameLengthType[streamID[prog][ lay]] == 3 ) {
					//		CELPframeLengthTableIndex[streamID[prog][lay]]; 6 uimsbf     
					//} else if ( frameLengthType[streamID[prog][ lay]] == 6 ||
					//			frameLengthType[streamID[prog][ lay]] == 7 ) {
					//		HVXCframeLengthTableIndex[streamID[prog][ lay]]; 1 uimsbf     
					// }
				}
			}

			@Override
			public DefaultMutableTreeNode getJTreeNode(int modus) {
				DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("LayerConfig"));
				t.add(new DefaultMutableTreeNode(new KVP("useSameConfig",useSameConfig,null)));
				if (useSameConfig == 0) {
					if (audioMuxVersion == 0) {
						t.add(audioSpecificConfig.getJTreeNode(modus));
					}
				}				
				t.add(new DefaultMutableTreeNode(new KVP("frameLengthType[streamID ["+prog+"]["+ lay+"]]",frameLengthType[streamID [prog][ lay]],frameLengthTypeList.get(frameLengthType[streamID [prog][ lay]]))));
				if (frameLengthType[streamID[prog][lay]] == 0) {
					t.add(new DefaultMutableTreeNode(new KVP("latmBufferFullness[streamID["+prog+"]["+ lay+"]]",latmBufferFullness[streamID[prog][lay]],null)));
				}
				
				return t;
			}
		}

		
		private int numLayer;
		
		public ProgramConfig(BitSource bitSource, int prog) {
			numLayer = bitSource.readBits(3);
			
			streamID  [prog] = new int[numLayer+1];
			for (int lay = 0; lay <= numLayer; lay++ ) {
				layerConfigList.add(new LayerConfig(bitSource,  prog, lay));
				
			}
		}

		@Override
		public DefaultMutableTreeNode getJTreeNode(int modus) {
			DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("ProgramConfig"));
			t.add(new DefaultMutableTreeNode(new KVP("numLayer",numLayer,"number of Layers:"+(numLayer+1))));
			addListJTree(t, layerConfigList, modus, "layerConfigs");
			return t;
		}

		public int getNumLayer() {
			return numLayer;
		}
	}
	
	int audioMuxVersion;
	int audioMuxVersionA = 0;

	int allStreamsSameTimeFraming;
	int numSubFrames;

	int numProgram;
	
	
	int streamCnt;
	
	private List<ProgramConfig> programConfigList = new ArrayList<>();

	private int otherDataPresent;
	
	BitSource localBitSource;

	private int crcCheckPresent ;

	private int crcCheckSum;

	public StreamMuxConfig(BitSource bitSource) {
		localBitSource = new BitSource(bitSource);
		
		audioMuxVersion = bitSource.readBits(1);
		if(audioMuxVersion == 1) {
			audioMuxVersionA = bitSource.readBits(1);
		}else {
			audioMuxVersionA = 0;
		}
		if(audioMuxVersionA == 0) {
			
			if (audioMuxVersion == 1 ) {
				logger.warning("taraBufferFullness = LatmGetValue() after if(audioMuxVersionA == 0) not implemented");
//				taraBufferFullness = LatmGetValue();     
			}
			
			streamCnt = 0; 
			allStreamsSameTimeFraming = bitSource.readBits(1);
			numSubFrames= bitSource.readBits(6);
			numProgram =  bitSource.readBits(4);
			
			streamID = new int [numProgram+1] [];
			for (int prog = 0; prog <= numProgram; prog++ ) { 
				programConfigList.add(new ProgramConfig(bitSource, prog));
			
			}
			
			otherDataPresent = bitSource.readBits(1);
			// TODO 
			//if ( audioMuxVersion == 1 ) { ....
			
			
			crcCheckPresent = bitSource.readBits(1);
			if (crcCheckPresent ==1) {
				crcCheckSum = bitSource.readBits(8); 
			}
		}

	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(int modus) {
		DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("StreamMuxConfig"));
		t.add(new DefaultMutableTreeNode(new KVP("audioMuxVersion",audioMuxVersion,(audioMuxVersion==0)?"audioMuxVersionA = 0":null)));
		if(audioMuxVersion == 1) {
			t.add(new DefaultMutableTreeNode(new KVP("audioMuxVersionA",audioMuxVersionA,null)));
		}
		if(audioMuxVersionA == 0) {
			if (audioMuxVersion == 1 ) {
				t.add(new DefaultMutableTreeNode(getNotImplementedKVP("taraBufferFullness = LatmGetValue()")));
			}
			t.add(new DefaultMutableTreeNode(new KVP("allStreamsSameTimeFraming",allStreamsSameTimeFraming,null)));
			t.add(new DefaultMutableTreeNode(new KVP("numSubFrames",numSubFrames,"number of SubFrames:"+(numSubFrames+1))));
			t.add(new DefaultMutableTreeNode(new KVP("numProgram",numProgram,"number of Programs:"+(numProgram+1))));
			addListJTree(t, programConfigList, modus, "ProgramConfigs");

			t.add(new DefaultMutableTreeNode(new KVP("otherDataPresent",otherDataPresent,null)));
			t.add(new DefaultMutableTreeNode(new KVP("crcCheckPresent",crcCheckPresent,null)));
			
			if (crcCheckPresent ==1) {
				t.add(new DefaultMutableTreeNode(new KVP("crcCheckSum",crcCheckSum,null)));
			}

		}
		return t;
	}

	public int getAudioMuxVersionA() {
		return audioMuxVersionA;
	}

	public int getNumSubFrames() {
		return numSubFrames;
	}

	public int getAllStreamsSameTimeFraming() {
		return allStreamsSameTimeFraming;
	}

	public static LookUpList getFrameLengthTypeList() {
		return frameLengthTypeList;
	}

	public static void setFrameLengthTypeList(LookUpList frameLengthTypeList) {
		StreamMuxConfig.frameLengthTypeList = frameLengthTypeList;
	}

	public int[] getProgSIndx() {
		return progSIndx;
	}

	public void setProgSIndx(int[] progSIndx) {
		this.progSIndx = progSIndx;
	}

	public int[] getLaySIndx() {
		return laySIndx;
	}

	public void setLaySIndx(int[] laySIndx) {
		this.laySIndx = laySIndx;
	}

	public int[][] getStreamID() {
		return streamID;
	}

	public void setStreamID(int[][] streamID) {
		this.streamID = streamID;
	}

	public int[] getFrameLengthType() {
		return frameLengthType;
	}

	public void setFrameLengthType(int[] frameLengthType) {
		this.frameLengthType = frameLengthType;
	}

	public int[] getLatmBufferFullness() {
		return latmBufferFullness;
	}

	public void setLatmBufferFullness(int[] latmBufferFullness) {
		this.latmBufferFullness = latmBufferFullness;
	}

	public int getAudioMuxVersion() {
		return audioMuxVersion;
	}

	public void setAudioMuxVersion(int audioMuxVersion) {
		this.audioMuxVersion = audioMuxVersion;
	}

	public int getNumProgram() {
		return numProgram;
	}

	public void setNumProgram(int numProgram) {
		this.numProgram = numProgram;
	}

	public int getStreamCnt() {
		return streamCnt;
	}

	public void setStreamCnt(int streamCnt) {
		this.streamCnt = streamCnt;
	}

	public List<ProgramConfig> getProgramConfigList() {
		return programConfigList;
	}

	public void setProgramConfigList(List<ProgramConfig> programConfigList) {
		this.programConfigList = programConfigList;
	}

	public static Logger getLogger() {
		return logger;
	}

	public void setAudioMuxVersionA(int audioMuxVersionA) {
		this.audioMuxVersionA = audioMuxVersionA;
	}

	public void setAllStreamsSameTimeFraming(int allStreamsSameTimeFraming) {
		this.allStreamsSameTimeFraming = allStreamsSameTimeFraming;
	}

	public void setNumSubFrames(int numSubFrames) {
		this.numSubFrames = numSubFrames;
	}

	public void setOtherDataPresent(int otherDataPresent) {
		this.otherDataPresent = otherDataPresent;
	}

	public int getOtherDataPresent() {
		return otherDataPresent;
	}


}
