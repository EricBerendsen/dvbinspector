/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2018 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.pid.t2mi;

import static nl.digitalekabeltelevisie.data.mpeg.MPEGConstants.PAYLOAD_PACKET_LENGTH;

import java.io.*;
import java.util.*;
import java.util.logging.*;

import nl.digitalekabeltelevisie.data.mpeg.TSPacket;
import nl.digitalekabeltelevisie.gui.*;

public class PlpHandler implements SaveAble{

	private static final Logger logger = Logger.getLogger(PlpHandler.class.getName());

	private static final int T2_BBHEADER_SIZE = 10;
	private static final int PKT_SIZE = 188;
	private byte[] buffer = new byte[0];
	private int bufferStart = 0;
	private int bufferEnd = 0;
	private int packetsProduced = 0;

	private int pid;
	private int plpId;
	private List<T2miPacket> plpPackets;
	private Iterator<T2miPacket> plpIter;
	
	
	private boolean first_packet = true;
	
	public PlpHandler(int pid, int plpId, List<T2miPacket> plpPackets) {
		super();
		this.pid = pid;
		this.plpId = plpId;
		this.plpPackets = plpPackets;
		plpIter = plpPackets.iterator();
	}
	
	public void reset() {
		buffer = new byte[0];
		bufferStart = 0;
		bufferEnd = 0;
		packetsProduced = 0;
		plpIter = plpPackets.iterator();
	}
	
	public TSPacket getTsPacket() {
		if (bufferEnd - bufferStart >= PAYLOAD_PACKET_LENGTH) {
			TSPacket packet = new TSPacket(Arrays.copyOfRange(buffer, bufferStart, bufferStart + PAYLOAD_PACKET_LENGTH), packetsProduced++, null);
			bufferStart += PAYLOAD_PACKET_LENGTH;
			return packet;
		}
		processT2miPackets(PAYLOAD_PACKET_LENGTH);
		if (bufferEnd - bufferStart >= PAYLOAD_PACKET_LENGTH) {
			TSPacket packet = new TSPacket(Arrays.copyOfRange(buffer, bufferStart, bufferStart + PAYLOAD_PACKET_LENGTH), packetsProduced++, null);
			bufferStart += PAYLOAD_PACKET_LENGTH;
			return packet;
		}
		return null;
	}

	
	// largely based on tsT2MIDemux.cpp in TsDuck https://tsduck.io/ 
	// errors are mine!
	
	// T2MI_HEADER_SIZE = 6;
	// T2_BBHEADER_SIZE = 10; header inside BBFRAME
	// PKT_SIZE = 188;
	
	private void processT2miPackets(int requestedBytes) {
		while (plpIter.hasNext() && ((bufferEnd - bufferStart) < requestedBytes)) {
			T2miPacket newT2miPacket = plpIter.next();
			BasebandFramePayload payLoad = (BasebandFramePayload) newT2miPacket.getPayload();
			if (payLoad.getTSGS() != 3) { // only TS type supported
				return;
			}
			int size = payLoad.data.length - 9; // skip 6 for T2mi header, and also 3 for header of baseband frame (so
												// starting at BBFrame)

			int npd = payLoad.getNPD();
			int dfl = payLoad.getDFLinBytes();
			int syncd = payLoad.getSYNCD();

			int dataStart = 9 + T2_BBHEADER_SIZE;
			size -= T2_BBHEADER_SIZE;

			// Adjust invalid DFL (should not happen).
			if (dfl > size) {
				dfl = size;
			}

			if (syncd == 0xFFFF) {
				// No user packet in data field
				append(payLoad.data, dataStart, size);
			} else {
				// Synchronization distance in bytes, bounded by data field size.
				syncd = Math.min(syncd / 8, dfl);

				// Process end of previous packet.
				if (!first_packet && syncd > 0) {
					if (((bufferEnd - bufferStart) % 188) == 0) {
						append(new byte[] { 0x47 }, 0, 1);
					}
					append(payLoad.data, dataStart, (syncd - npd));
				}
				first_packet = false;
				dataStart += syncd;
				dfl -= syncd;

				// Process subsequent complete packets.
				while (dfl >= PKT_SIZE - 1) {
					append(new byte[] { 0x47 }, 0, 1);
					append(payLoad.data, dataStart, PKT_SIZE - 1);
					dataStart += (PKT_SIZE - 1);
					dfl -= (PKT_SIZE - 1);
				}

				// Process optional trailing truncated packet.
				if (dfl > 0) {
					append(new byte[] { 0x47 }, 0, 1);
					append(payLoad.data, dataStart, dfl);
				}
			}
		}

	}

	private void append(byte[] data, int dataStart, int size) {
		int currentLen = bufferEnd - bufferStart;
		int newLen = currentLen + size;
		byte[] newBuffer = new byte[newLen];
		System.arraycopy(buffer, bufferStart, newBuffer, 0, currentLen);
		System.arraycopy(data, dataStart, newBuffer, currentLen, size);
		buffer = newBuffer;
		bufferStart = 0;
		bufferEnd = newLen;
	}

	public boolean hasMoreTsPackets() {
		if (bufferEnd - bufferStart >= PAYLOAD_PACKET_LENGTH) {
			return true;
		}
		processT2miPackets(PAYLOAD_PACKET_LENGTH);
		return (bufferEnd - bufferStart >= PAYLOAD_PACKET_LENGTH);
	}

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public int getPlpId() {
		return plpId;
	}

	public void setPlpId(int plpId) {
		this.plpId = plpId;
	}

	public List<T2miPacket> getPlpPackets() {
		return plpPackets;
	}

	public void setPlpPackets(List<T2miPacket> plpPackets) {
		this.plpPackets = plpPackets;
	}

	@Override
	public void save(File file) {
		try (FileOutputStream out = new FileOutputStream(file)) {
			reset();
			while (hasMoreTsPackets()) {
				TSPacket packet = getTsPacket();
				out.write(packet.getBuffer());
			}
		} catch (IOException e) {
			logger.log(Level.WARNING, "could not write file", e);
		}
		
	}


}
