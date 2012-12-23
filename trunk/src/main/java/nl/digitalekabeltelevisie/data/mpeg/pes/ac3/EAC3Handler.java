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



import static nl.digitalekabeltelevisie.util.Utils.*;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.data.mpeg.PesPacketData;
import nl.digitalekabeltelevisie.data.mpeg.pes.GeneralPesHandler;
/**
*
* Based on ETSI TS 102 366 V1.2.1 (2008-08) Digital Audio Compression (AC-3, Enhanced AC-3) Standard
*
* TS 101 154 V1.9.1 (2009-09) 6.2.1.3 Byte-alignment The AC-3 and Enhanced AC-3 elementary stream shall be byte-aligned within the MPEG-2 data stream.
* So AC-3 frame can start anywhere (and span PES packets). So frames are collected on Handler level, and not on PES packet level.
* @author Eric
*
*/
public class EAC3Handler extends GeneralPesHandler{

	private final List<EAC3SyncFrame> ac3Frames = new ArrayList<EAC3SyncFrame>();

	private byte[] pesDataBuffer = new byte[10000];
	private int bufStart = 0;
	private int bufEnd = 0;

	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.data.mpeg.pes.GeneralPesHandler#processPesDataBytes(int, byte[], int, int)
	 */
	@Override
	public void processPesDataBytes(final PesPacketData pesData){
		final AC3PESDataField ac3PesDataField = new AC3PESDataField(pesData);
		pesPackets.add(ac3PesDataField);
		// add data from packet to buffer

		final int len = ac3PesDataField.getPesDataLen();
		// clean if needed, remove used bytes from start en append new space at end
		if ((len + bufEnd) > pesDataBuffer.length) {
			final byte[] newBuf = new byte[20000];
			System.arraycopy(pesDataBuffer, bufStart, newBuf, 0, bufEnd - bufStart);
			pesDataBuffer = newBuf;
			bufEnd = bufEnd - bufStart;
			bufStart = 0;
		}

		// now copy new data into buf
		System.arraycopy(ac3PesDataField.getData(),
				ac3PesDataField.getPesDataStart(),
				pesDataBuffer,
				bufEnd,
				ac3PesDataField.getPesDataLen());
		bufEnd+= ac3PesDataField.getPesDataLen();

		int i = bufStart;
		int end = bufStart;
		while ((i < (pesDataBuffer.length)) && (i >= 0) && (end >= 0)) {
			i = indexOf(pesDataBuffer, new byte[] { 0x0b, 0x77 }, i);
			if (i >= 0) { // found start,
				end = indexOf(pesDataBuffer, new byte[] { 0x0b, 0x77 }, i + 2);
				if (end > 0) { // also found start of next Frame, so this one is complete
					final EAC3SyncFrame frame = new EAC3SyncFrame(pesDataBuffer, i);
					ac3Frames.add(frame);
					bufStart = end;
					i = end;

				}
			}
		}

	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode s = super.getJTreeNode(modus);
		addListJTree(s, ac3Frames, modus, "EAC3 SyncFrames");
		return s;
	}

}
