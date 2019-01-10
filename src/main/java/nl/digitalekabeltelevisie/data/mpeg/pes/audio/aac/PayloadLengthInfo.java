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

import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.*;
import nl.digitalekabeltelevisie.data.mpeg.pes.audio.aac.StreamMuxConfig.ProgramConfig;
import nl.digitalekabeltelevisie.util.BitSource;

public class PayloadLengthInfo implements TreeNode {

	private int[] muxSlotLengthBytes = new int[64];
	private int[] muxSlotLengthCoded = new int[64];
	private StreamMuxConfig streamMuxConfig;

	public PayloadLengthInfo(BitSource bitSource, StreamMuxConfig streamMuxConfig) {
		this.streamMuxConfig = streamMuxConfig;
		if (streamMuxConfig.getAllStreamsSameTimeFraming() == 1) {
			int[][] streamID = streamMuxConfig.getStreamID();
			List<ProgramConfig> programConfigList = streamMuxConfig.getProgramConfigList();
			int[] frameLengthType = streamMuxConfig.getFrameLengthType();
			for (int prog = 0; prog <= streamMuxConfig.getNumProgram(); prog++) {
				ProgramConfig programConfig = programConfigList.get(prog);
				for (int lay = 0; lay <= programConfig.getNumLayer(); lay++) {       
					if (frameLengthType[streamID[prog][lay]] == 0) {
						muxSlotLengthBytes[streamID[prog][lay]] = 0;
						int tmp;
						do { /* always one complete access unit */
							tmp = bitSource.readBits(8); // uimsbf
							muxSlotLengthBytes[streamID[prog][lay]] += tmp;
						} while (tmp == 255);
					} else {
						if ( frameLengthType[streamID[prog][ lay]] == 5 ||
							frameLengthType[streamID[prog][ lay]] == 7 ||
							frameLengthType[streamID[prog][ lay]] == 3 ) {
							muxSlotLengthCoded[streamID[prog][ lay]]= bitSource.readBits(2); //uimsbf      
						}
					}
				}
			}
		}
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(int modus) {
		
		DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("PayloadLengthInfo"));
		
		if (streamMuxConfig.getAllStreamsSameTimeFraming() == 1) { 
			int[][] streamID = streamMuxConfig.getStreamID();
			List<ProgramConfig> programConfigList = streamMuxConfig.getProgramConfigList();
			int[] frameLengthType = streamMuxConfig.getFrameLengthType();
			for (int prog = 0; prog <= streamMuxConfig.getNumProgram(); prog++) {
				ProgramConfig programConfig = programConfigList.get(prog);
				for (int lay = 0; lay <= programConfig.getNumLayer(); lay++) {       
					if ( frameLengthType[streamID[prog][ lay]] == 0) { 
						t.add(new DefaultMutableTreeNode(new KVP("MuxSlotLengthBytes[streamID["+prog+"]["+lay+"]]",
								muxSlotLengthBytes[streamID[prog][ lay]],
								"streamID["+prog+"]["+lay+"]]="+streamID[prog][ lay])));
					} else {
//						if ( frameLengthType[streamID[prog][ lay]] == 5 ||
//							frameLengthType[streamID[prog][ lay]] == 7 ||
//							frameLengthType[streamID[prog][ lay]] == 3 ) {
//							muxSlotLengthCoded[streamID[prog][ lay]]= bitSource.readBits(2); //uimsbf      
//						}
					}
				}
			}
		}

		return t;
	}

	public int[] getMuxSlotLengthBytes() {
		return muxSlotLengthBytes;
	}

	public int[] getMuxSlotLengthCoded() {
		return muxSlotLengthCoded;
	}

}
