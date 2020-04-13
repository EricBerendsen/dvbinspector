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

import static nl.digitalekabeltelevisie.util.Utils.addListJTree;

import java.util.*;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.PesPacketData;
import nl.digitalekabeltelevisie.util.BitSource;

/**
 * @author Eric Berendsen
 *
 */

public class Audio144963PESDataField extends PesPacketData{


	List<AudioSyncStream> audioSyncStreamList = new ArrayList<>();
	
	
	
	public Audio144963PESDataField(final PesPacketData pesPacket, Audio144963Handler audio144963Handler) {
		super(pesPacket);

		BitSource bitSource = new BitSource(pesPacket.getData(), pesDataStart);
		while ((bitSource.available() >= 11) && (bitSource.nextBits(11) == 0x2B7)) {
			AudioSyncStream audioSyncStream = new AudioSyncStream(bitSource, audio144963Handler);
			audioSyncStreamList.add(audioSyncStream);
		}

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see nl.digitalekabeltelevisie.controller.TreeNode#getJTreeNode(int)
	 */
	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {

		final DefaultMutableTreeNode s = super.getJTreeNode(modus,new KVP("Audio 14496-3 PES Packet"));
		
		addListJTree(s, audioSyncStreamList, modus, "AudioSyncStreams");

		return s;
	}

	public List<AudioSyncStream> getAudioSyncStreamList() {
		return audioSyncStreamList;
	}


}
