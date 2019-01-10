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

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.*;
import nl.digitalekabeltelevisie.util.BitSource;

public class AudioSyncStream implements TreeNode {
	
	// based on ISO/IEC 14496-3:2005(E)  1.7.2 Synchronization Layer
	// Low Overhead Audio Stream (LOAS)

	private int syncword;
	private int audioMuxLengthBytes;
	private AudioMuxElement audioMuxElement;

	public AudioSyncStream(BitSource bitSource, Audio144963Handler audio144963Handler) {
		
		syncword = bitSource.readBits(11);
		audioMuxLengthBytes = bitSource.readBits(13);
		audioMuxElement = new AudioMuxElement(new BitSource(bitSource,audioMuxLengthBytes), 1,audio144963Handler);
		bitSource.advanceBytes(audioMuxLengthBytes);

	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(int modus) {
		DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("AudioSyncStream"));
		t.add(new DefaultMutableTreeNode(new KVP("syncword",syncword,"should be 0x2B7")));
		t.add(new DefaultMutableTreeNode(new KVP("audioMuxLengthBytes",audioMuxLengthBytes,null)));
		t.add(audioMuxElement.getJTreeNode(modus));
		
		return t;
	}

	public int getAudioMuxLengthBytes() {
		return audioMuxLengthBytes;
	}

	public AudioMuxElement getAudioMuxElement() {
		return audioMuxElement;
	}

}
