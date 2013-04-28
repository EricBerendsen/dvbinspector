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

package nl.digitalekabeltelevisie.data.mpeg;

import static nl.digitalekabeltelevisie.data.mpeg.MPEGConstants.packet_length;

import java.util.Arrays;

import nl.digitalekabeltelevisie.util.Utils;

/**
 * 
 * Represents a single transport stream packet (188 bytes) as defined in 2.4.3.2 Transport Stream packet layer of ISO
 * 13813 , and contains all data in it.
 *
 *
 * <img src="doc-files/tspacket.png">
 *
 * @author Eric Berendsen
 *
 */
public class TSPacket {
	private final byte[] buffer ;
	private long packetNo=-1;

	public TSPacket(final byte[] buf, final long no) {
		buffer=Arrays.copyOf(buf, buf.length); //buf should be copied, or else PID.getLast_packet() will always point to last packet parsed, regardless of the actual pid.
		packetNo = no;
	}

	public int getTransportScramblingControl(){
		return (buffer[3] & 0xC0) >>6;
	}

	public String getTransportScramblingControlString(){
		switch (getTransportScramblingControl()) {
		case 0:
			return "not scrambled";
		case 1:
			return "Reserved for future DVB use";
		case 2:
			return "TS packet scrambled with Even Key";
		case 3:
			return "TS packet scrambled with Odd Key";
		default:
			throw new IllegalArgumentException("Invalid value in getTransportScramblingControl:"+getTransportScramblingControl());
		}
	}

	public boolean isTransportErrorIndicator() {
		return (buffer[1] & 0x80) != 0;
	}

	public boolean isPayloadUnitStartIndicator() {
		return (buffer[1] & 0x40) != 0;
	}

	public boolean isTransportPriority() {
		return (buffer[1] & 0x20) != 0;
	}

	public int isAdaptationFieldControl(){
		return (buffer[3] & 0x30) >>4;
	}

	public String getAdaptationFieldControlString(){
		switch (isAdaptationFieldControl()) {
		case 0:
			return "reserved for future use by ISO/IEC";
		case 1:
			return "no adaptation_field, payload only";
		case 2:
			return "adaptation_field only, no payload";
		case 3:
			return "adaptation_field followed by payload";
		default:
			throw new IllegalArgumentException("Invalid value in getAdaptationFieldControl:"+isAdaptationFieldControl());
		}
	}

	public byte[] getAdaptationField(){
		if((isAdaptationFieldControl()==2)||(isAdaptationFieldControl()==3)) { //Adaptation field presen
			return Utils.copyOfRange(buffer,4, 4+Utils.getUnsignedByte(buffer[4])+1);
		}
		return null;
	}

	/**
	 * @return the payload of this TSPacket, direct after the adaptationField
	 */
	public byte[] getData(){
		if((isAdaptationFieldControl()==1)) { //payload only
			return Utils.copyOfRange(buffer,4, packet_length);
		}else if((isAdaptationFieldControl()==3)) { //Adaptation followed by payload
			return Utils.copyOfRange(buffer, 4+Utils.getUnsignedByte(buffer[4])+1,packet_length);
		}
		return null;
	}

	public short getPID()
	{
		return (short)Utils.getInt(buffer,1,2,Utils.MASK_13BITS) ;
	}
	@Override
	public String toString() {
		return "bytes="+ Utils.toHexString(buffer, 4) +" , PID="+Integer.toHexString(getPID())+" , continuity_counter="+getContinuityCounter()+", Adaptation_field_control="+getAdaptationFieldControlString()+", Transport Scrambling Control="+getTransportScramblingControlString() ;
	}

	public int  getContinuityCounter() {
		return (buffer[3] & 0x0F);

	}

	/**
	 * @return the position of this packet in the TransportStream
	 */
	public long getPacketNo() {
		return packetNo;
	}

	/**
	 * @param packet_no
	 */
	public void setPacketNo(final long packet_no) {
		this.packetNo = packet_no;
	}
}
