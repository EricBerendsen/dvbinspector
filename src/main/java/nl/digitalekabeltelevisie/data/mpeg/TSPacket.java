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
import static nl.digitalekabeltelevisie.util.Utils.*;

import java.awt.Color;
import java.util.Arrays;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.gui.HTMLSource;
import nl.digitalekabeltelevisie.util.RangeHashMap;
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
public class TSPacket implements HTMLSource, TreeNode{
	private final byte[] buffer ;
	private long packetNo=-1;
	private static Color HEADER_COLOR = new Color(0x0000ff);
	private static Color ADAPTATION_FIELD_COLOR = new Color(0x008000);
	private TransportStream transportStream = null;

	/**
	 * @param buf bytes forming tspacket, should be 188 long
	 * @param no position number of this packet in the stream
	 * @param ts TransportStream this packet belongs to, needed to find stuff like service name.
	 */
	public TSPacket(final byte[] buf, final long no, TransportStream ts) {
		buffer=Arrays.copyOf(buf, buf.length); //buf should be copied, or else PID.getLast_packet() will always point to last packet parsed, regardless of the actual pid.
		packetNo = no;
		transportStream = ts;
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

	public int getTransportErrorIndicator() {
		return (buffer[1] & 0x80)>>7;
	}

	public boolean isPayloadUnitStartIndicator() {
		return (buffer[1] & 0x40) != 0;
	}

	public int getPayloadUnitStartIndicator() {
		return (buffer[1] & 0x40)>>6 ;
	}

	public boolean isTransportPriority() {
		return (buffer[1] & 0x20) != 0;
	}

	public int getTransportPriority() {
		return (buffer[1] & 0x20)>>5;
	}

	public int getAdaptationFieldControl(){
		return (buffer[3] & 0x30) >>4;
	}

	public String getAdaptationFieldControlString(){
		switch (getAdaptationFieldControl()) {
		case 0:
			return "reserved for future use by ISO/IEC";
		case 1:
			return "no adaptation_field, payload only";
		case 2:
			return "adaptation_field only, no payload";
		case 3:
			return "adaptation_field followed by payload";
		default:
			throw new IllegalArgumentException("Invalid value in getAdaptationFieldControl:"+getAdaptationFieldControl());
		}
	}

	public boolean hasAdaptationField(){
		return (getAdaptationFieldControl()==2)||(getAdaptationFieldControl()==3);

	}

	public byte[] getAdaptationFieldBytes(){
		if((getAdaptationFieldControl()==2)||(getAdaptationFieldControl()==3)) { //Adaptation field present
			return copyOfRange(buffer,4, 4+getUnsignedByte(buffer[4])+1);
		}
		return null;
	}

	public AdaptationField getAdaptationField(){
		if((getAdaptationFieldControl()==2)||(getAdaptationFieldControl()==3)) { //Adaptation field present
			return new AdaptationField(copyOfRange(buffer,4, 4+getUnsignedByte(buffer[4])+1));
		}
		return null;
	}

	/**
	 * @return the payload of this TSPacket, direct after the adaptationField
	 */
	public byte[] getData(){
		if((getAdaptationFieldControl()==1)) { //payload only
			return copyOfRange(buffer,4, packet_length);
		}else if((getAdaptationFieldControl()==3)) { //Adaptation followed by payload
			int start = Math.min(4+getUnsignedByte(buffer[4])+1, packet_length);
			return copyOfRange(buffer, start,packet_length);
		}
		return null;
	}

	public short getPID()
	{
		return (short)getInt(buffer,1,2,MASK_13BITS) ;
	}
	@Override
	public String toString() {
		return "bytes="+ toHexString(buffer, 4) +" , PID="+Integer.toHexString(getPID())+", packetNo="+packetNo+" , continuity_counter="+getContinuityCounter()+", Adaptation_field_control="+getAdaptationFieldControlString()+", Transport Scrambling Control="+getTransportScramblingControlString() ;
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

	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.gui.HTMLSource#getHTML()
	 */
	@Override
	public String getHTML() {
		StringBuilder s = new StringBuilder();

		s.append("Packet: ").append(packetNo);
		if(transportStream!=null){
			s.append("<br>Time: ").append(transportStream.getPacketTime(packetNo));
			final short pid = transportStream.getPacket_pid((int)packetNo);
			s.append("<br>").append(escapeHtmlBreakLines(transportStream.getShortLabel(pid))).append("<br>");
		}


		s.append("<br><span style=\"color:").append(Utils.toHexString(HEADER_COLOR)).append("\"><b>Header: </b><br>");
		RangeHashMap<Integer, Color> coloring = new RangeHashMap<Integer, Color>();
		coloring.put(0, 3, HEADER_COLOR);

		s.append("<br>sync_byte: ").append(getHexAndDecimalFormattedString(getSyncByte()));
		s.append("<br>transport_error_indicator: ").append(getTransportErrorIndicator());
		s.append("<br>payload_unit_start_indicator: ").append(getPayloadUnitStartIndicator());
		s.append("<br>transport_priority: ").append(getTransportPriority());

		s.append("<br>PID: ").append(getHexAndDecimalFormattedString(getPID()));
		s.append("<br>transport_scrambling_control: ").append(getTransportScramblingControl()).append(" (").append(getTransportScramblingControlString()).append(")");
		s.append("<br>adaptation_field_control: ").append(getAdaptationFieldControl()).append(" (").append(getAdaptationFieldControlString()).append(")");
		s.append("<br>continuity_counter: ").append(getHexAndDecimalFormattedString(getContinuityCounter())).append("</span>");

		AdaptationField adaptationField = null;
		try{
			adaptationField = getAdaptationField();
		}catch(RuntimeException re){ // might be some error in adaptation field, it is not well protected
			adaptationField = null;
		}
		if(adaptationField!=null){
			s.append("<br><br><span style=\"color:").append(Utils.toHexString(ADAPTATION_FIELD_COLOR)).append("\"><b>AdaptationField:</b><br>").append(adaptationField.getHTML()).append("</span>");
			coloring.put(4, 4+adaptationField.getAdaptation_field_length(), ADAPTATION_FIELD_COLOR);
		}

		s.append("<br><br><b>Data:</b><br>").append(getHTMLHexviewColored(buffer,0,buffer.length,coloring));

		return s.toString();
	}

	/**
	 * @return should always be 0x47
	 */
	private int getSyncByte() {
		return getInt(buffer, 0,1,MASK_8BITS);
	}

	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.controller.TreeNode#getJTreeNode(int)
	 */
	@Override
	public DefaultMutableTreeNode getJTreeNode(int modus) {

		StringBuilder l = new StringBuilder("TSPacket [").append(packetNo).append("]");
		if((getAdaptationFieldControl()==2)||(getAdaptationFieldControl()==3)) { //Adaptation field present

			l.append(" (adaptation field)");
		}
		if((getAdaptationFieldControl()==1)||(getAdaptationFieldControl()==3)) { //payload present

			l.append(" (payload)");
		}
		if(isPayloadUnitStartIndicator()){
			l.append(" (payload start)");
		}

		DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP(l.toString(), this));
		t.add(new DefaultMutableTreeNode(new KVP("sync_byte",getSyncByte() ,"Should be 0x47")));
		t.add(new DefaultMutableTreeNode(new KVP("transport_error_indicator",getTransportErrorIndicator() ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("payload_unit_start_indicator",getPayloadUnitStartIndicator() ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("transport_priority",getTransportPriority() ,null)));

		t.add(new DefaultMutableTreeNode(new KVP("PID",getPID() ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("transport_scrambling_control",getTransportScramblingControl() ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("adaptation_field_control",getAdaptationFieldControl() ,getAdaptationFieldControlString())));

		t.add(new DefaultMutableTreeNode(new KVP("continuity_counter",getContinuityCounter() ,null)));

		AdaptationField adaptationField = null;
		try{
			adaptationField = getAdaptationField();
		}catch(RuntimeException re){ // might be some error in adaptation field, it is not well protected
			adaptationField = null;
		}
		if(adaptationField!=null){
			t.add(adaptationField.getJTreeNode(modus));
		}

		if((getAdaptationFieldControl() == 1) || (getAdaptationFieldControl() == 3)) {
			int payloadStart = 4;
			if(adaptationField!=null){
				payloadStart = 5+adaptationField.getAdaptation_field_length();
			}
			t.add(new DefaultMutableTreeNode(new KVP("data_byte",buffer,payloadStart ,buffer.length-payloadStart, null)));
		}



		return t;
	}
}
