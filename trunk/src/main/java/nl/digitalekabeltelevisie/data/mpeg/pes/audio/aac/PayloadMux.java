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
import java.util.logging.Logger;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.*;
import nl.digitalekabeltelevisie.data.mpeg.pes.audio.aac.StreamMuxConfig.ProgramConfig;
import nl.digitalekabeltelevisie.gui.utils.GuiUtils;
import nl.digitalekabeltelevisie.util.BitSource;

public class PayloadMux implements TreeNode {
	
	byte[][] payLoadList = new byte[64][];
	private StreamMuxConfig streamMuxConfig;
	
	private static final Logger logger = Logger.getLogger(PayloadMux.class.getName());


	public PayloadMux(BitSource bitSource, StreamMuxConfig streamMuxConfig, PayloadLengthInfo payloadLengthInfo) {
		this.streamMuxConfig = streamMuxConfig;
		int[] muxSlotLengthBytes = payloadLengthInfo.getMuxSlotLengthBytes();
		if (streamMuxConfig.getAllStreamsSameTimeFraming() == 1) {
			int[][] streamID = streamMuxConfig.getStreamID();
			List<ProgramConfig> programConfigList = streamMuxConfig.getProgramConfigList();
			for (int prog = 0; prog <= streamMuxConfig.getNumProgram(); prog++) {
				ProgramConfig programConfig = programConfigList.get(prog);
				for (int lay = 0; lay <= programConfig.getNumLayer(); lay++) {       

					int lengthPayload = muxSlotLengthBytes[streamID[prog][lay]];
					byte[] payload = new byte[lengthPayload];
					for (int i = 0; i < lengthPayload; i++) {
						payload[i] = (byte) bitSource.readBits(8);
					}
					payLoadList[streamID[prog][lay]] = payload;
				}
			}
		} else {
			logger.warning("! allStreamsSameTimeFraming not implemented");
		}
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(int modus) {
		DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("PayloadMux"));
		
		if (streamMuxConfig.getAllStreamsSameTimeFraming() == 1) { 
			int[][] streamID = streamMuxConfig.getStreamID();
			List<ProgramConfig> programConfigList = streamMuxConfig.getProgramConfigList();
			for (int prog = 0; prog <= streamMuxConfig.getNumProgram(); prog++) {
				ProgramConfig programConfig = programConfigList.get(prog);
				for (int lay = 0; lay <= programConfig.getNumLayer(); lay++) {    
					byte[] payload = payLoadList[streamID[prog][lay]];
					t.add(new DefaultMutableTreeNode(new KVP("payload [streamID["+prog+"]["+lay+"]]",payload,"streamID["+prog+"]["+lay+"]]="+streamID[prog][ lay])));
				}
			}
		} else {
			t.add(new DefaultMutableTreeNode(GuiUtils.getNotImplementedKVP("! allStreamsSameTimeFraming")));
		}
				
		return t;
	}

	public byte[][] getPayLoadList() {
		return payLoadList;
	}

}
