package nl.digitalekabeltelevisie.data.mpeg.pes.audio.ac4;

/**
*
*  http://www.digitalekabeltelevisie.nl/dvb_inspector
*
*  This code is Copyright 2009-2021 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

import static nl.digitalekabeltelevisie.util.Utils.addListJTree;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.PesPacketData;


// based on TS 103 190(-1,2) and TS 101 154 V2.6.1 vh 6.6 AC-4 channel-based audio

// The number of AC-4 frames per PES packet may
// vary between PES packets, but each PES packet shall contain an integer number of AC-4 frames only. AC-4 frames
// shall not be split over two or more PES packets.


public class AC4PesDataField extends PesPacketData {

	private List<AC4SyncFrame> ac4SyncFrames = new ArrayList<>();
	
	protected AC4PesDataField(PesPacketData pesPacket) {
		super(pesPacket);
		int offset = pesDataStart;
		while(offset < (pesDataLen+pesDataStart)) {
			AC4SyncFrame syncFrame = new AC4SyncFrame(data, offset);
			offset += syncFrame.getSyncFrameSize();
			ac4SyncFrames.add(syncFrame);
		}

	}
	
	
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		DefaultMutableTreeNode jTreeNode = super.getJTreeNode(modus,new KVP("AC-4 PES Packet"));
		addListJTree(jTreeNode, ac4SyncFrames, modus, "AC4 SyncFrames");
		return jTreeNode;
}

}
