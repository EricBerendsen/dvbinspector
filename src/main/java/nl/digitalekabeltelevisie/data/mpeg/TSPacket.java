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

package nl.digitalekabeltelevisie.data.mpeg;

import static java.lang.Byte.toUnsignedInt;
import static nl.digitalekabeltelevisie.data.mpeg.MPEGConstants.PAYLOAD_PACKET_LENGTH;
import static nl.digitalekabeltelevisie.gui.utils.GuiUtils.getErrorKVP;
import static nl.digitalekabeltelevisie.util.Utils.MASK_13BITS;
import static nl.digitalekabeltelevisie.util.Utils.MASK_8BITS;
import static nl.digitalekabeltelevisie.util.Utils.escapeHtmlBreakLines;
import static nl.digitalekabeltelevisie.util.Utils.getHTMLHexviewColored;
import static nl.digitalekabeltelevisie.util.Utils.getHexAndDecimalFormattedString;
import static nl.digitalekabeltelevisie.util.Utils.getInt;
import static nl.digitalekabeltelevisie.util.Utils.toHexString;

import java.awt.Color;
import java.util.Arrays;
import java.util.logging.Logger;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.pes.PesHeader;
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
	
	private static final Logger	logger	= Logger.getLogger(TSPacket.class.getName());
	private static final String ERROR_PARSING_ADAPTATION_FIELD = "Error parsing AdaptationField";
	final private byte[] buffer ;

	private int packetNo=-1;
	final private static Color HEADER_COLOR = new Color(0x0000ff);
	final private static Color ADAPTATION_FIELD_COLOR = new Color(0x008000);
	final private static Color FEC_COLOR = new Color(0x800080);
	final private static Color PES_HEADER_COLOR = new Color(0x800000);
	private static final Color ERROR_COLOR = new Color(0xFF0000);
	final private TransportStream transportStream;
	private long packetOffset = -1;

	private PesHeader pesHeader = null;

	synchronized public PesHeader getPesHeader(){
		if(pesHeader==null){
			if(hasPayload()&&isPayloadUnitStartIndicator()){
				int payloadStart = 4;
				if(hasAdaptationField()){
					AdaptationField adaptationField = getAdaptationField();
					if(adaptationField==null) { // something wrong with adaptationField, don't even bother with PesHeader
						return null;
					}
					payloadStart = 5+adaptationField.getAdaptation_field_length();
				}
				try {
					pesHeader = new PesHeader(buffer, payloadStart);
				} catch (Exception e) {
					logger.info("Exception getting PesHeader");
				}
			}
		}
		return pesHeader;
	}

	/**
	 * @param buf bytes forming tspacket, should be 188 long
	 * @param no position number of this packet in the stream
	 * @param ts TransportStream this packet belongs to, needed to find stuff like service name.
	 */
	public TSPacket(final byte[] buf, final int no, final TransportStream ts) {
		buffer=Arrays.copyOf(buf, buf.length); //buf should be copied, or else PID.getLast_packet() will always point to last packet parsed, regardless of the actual pid.
		packetNo = no;
		transportStream = ts;
		
//		if((getAdaptationFieldControl()==2)||(getAdaptationFieldControl()==3)) { //Adaptation field present
//			getAdaptationField();
//		}
//
//		if(getPayloadUnitStartIndicator()==1){
//			if((getAdaptationFieldControl()!=1)&&(getAdaptationFieldControl()!=3)){
//				System.out.println("Error: TSPacket:"+packetNo+", PID:"+getPID()+", If a PID is carrying PES packets or PSI sections, and payload_unit_start_indicator is set to 1, then test that adaptation_field_control is ‘01’ or ‘11’”");
//			}
//		}
//			
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
		final int adaptationFieldControl = getAdaptationFieldControl();
		return (adaptationFieldControl==2)||(adaptationFieldControl==3);

	}

	public byte[] getAdaptationFieldBytes(){
		final int adaptationFieldControl = getAdaptationFieldControl();
		if((adaptationFieldControl==2)||(adaptationFieldControl==3)) { //Adaptation field present
			return Arrays.copyOfRange(buffer,4, 4+toUnsignedInt(buffer[4])+1);
		}
		return new byte[0];
	}

	public AdaptationField getAdaptationField(){
		final int adaptationFieldControl = getAdaptationFieldControl();
		if((adaptationFieldControl==2)||(adaptationFieldControl==3)) { //Adaptation field present
			AdaptationField adaptationField;
			try {
				adaptationField =  new AdaptationField(Arrays.copyOfRange(buffer,4, 4+toUnsignedInt(buffer[4])+1));
				return adaptationField;
			} catch (Exception e) {
				logger.info("Exception creating AdaptationField");
			}
		}
		return null;
	}

	/**
	 * @return the payload of this TSPacket, direct after the adaptationField
	 */
	public byte[] getData(){
		final int adaptationFieldControl = getAdaptationFieldControl();
		if((adaptationFieldControl==1)) { //payload only
			return Arrays.copyOfRange(buffer,4, PAYLOAD_PACKET_LENGTH);
		}else if((adaptationFieldControl==3)) { //Adaptation followed by payload
			final int start = Math.min(4+toUnsignedInt(buffer[4])+1, PAYLOAD_PACKET_LENGTH);
			return Arrays.copyOfRange(buffer, start,PAYLOAD_PACKET_LENGTH);
		}
		return new byte[0];
	}

	public short getPID()
	{
		return (short)getInt(buffer,1,2,MASK_13BITS) ;
	}
	@Override
	public String toString() {
		return "bytes="+ toHexString(buffer, 4) +" , PID="+getPID()+" (0x"+Integer.toHexString(getPID())+"), packetNo="+packetNo+" , continuity_counter="+getContinuityCounter()+", Adaptation_field_control="+getAdaptationFieldControlString()+", Transport Scrambling Control="+getTransportScramblingControlString() ;
	}

	public int  getContinuityCounter() {
		return (buffer[3] & 0x0F);

	}

	/**
	 * @return the position of this packet in the TransportStream
	 */
	public int getPacketNo() {
		return packetNo;
	}

	/**
	 * @param packet_no
	 */
	public void setPacketNo(final int packet_no) {
		this.packetNo = packet_no;
	}

	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.gui.HTMLSource#getHTML()
	 */
	@Override
	public String getHTML() {
		final StringBuilder s = new StringBuilder();

		s.append("Packet: ").append(packetNo);
		if(packetOffset!=-1){
			s.append("<br>File Offset: ").append(packetOffset);
		}
		if(transportStream!=null){
			s.append("<br>Time: ").append(transportStream.getPacketTime(packetNo));
			final short pid = transportStream.getPacket_pid(packetNo);
			s.append("<br>").append(escapeHtmlBreakLines(transportStream.getShortLabel(pid))).append("<br>");
		}

		appendHeader(s, "Header:", HEADER_COLOR);
		final RangeHashMap<Integer, Color> coloring = new RangeHashMap<>();
		coloring.put(0, 3, HEADER_COLOR);

		s.append("<br>sync_byte: ").append(getHexAndDecimalFormattedString(getSyncByte()));
		s.append("<br>transport_error_indicator: ").append(getTransportErrorIndicator());
		s.append("<br>payload_unit_start_indicator: ").append(getPayloadUnitStartIndicator());
		s.append("<br>transport_priority: ").append(getTransportPriority());

		s.append("<br>PID: ").append(getHexAndDecimalFormattedString(getPID()));
		s.append("<br>transport_scrambling_control: ").append(getTransportScramblingControl()).append(" (").append(getTransportScramblingControlString()).append(")");
		s.append("<br>adaptation_field_control: ").append(getAdaptationFieldControl()).append(" (").append(getAdaptationFieldControlString()).append(")");
		s.append("<br>continuity_counter: ").append(getHexAndDecimalFormattedString(getContinuityCounter())).append("</span><br>");

		AdaptationField adaptationField = null;
		try{
			adaptationField = getAdaptationField();
		}catch(RuntimeException re){ // might be some error in adaptation field, it is not well protected
			adaptationField = null;
			appendHeader(s,ERROR_PARSING_ADAPTATION_FIELD,ERROR_COLOR);
			s.append("<br></span>");
		}
		if(adaptationField!=null){
			appendHeader(s, "adaptation_field:", ADAPTATION_FIELD_COLOR);
			s.append(adaptationField.getHTML()).append("<br></span>");
			coloring.put(4, 4+adaptationField.getAdaptation_field_length(), ADAPTATION_FIELD_COLOR);
		}

		// PES header
		if(hasPayload()) {
			int payloadStart = 4;
			if(adaptationField!=null){
				payloadStart = 5+adaptationField.getAdaptation_field_length();
			}
			if((getPayloadUnitStartIndicator()==1)&&(getTransportScramblingControl()==0)){
				final PesHeader pesHeaderView = getPesHeader();
				if((pesHeaderView!=null)&&(pesHeaderView.isValidPesHeader())){
					final DefaultMutableTreeNode treeNode = pesHeaderView.getJTreeNode(0);
					appendHeader(s, "Pes Header:", PES_HEADER_COLOR);
					s.append("<br>").append(Utils.getChildrenAsHTML(treeNode));
					s.append("</span>");
					if(pesHeaderView.hasExtendedHeader()){
						coloring.put(payloadStart, payloadStart+8+pesHeaderView.getPes_header_data_length(), PES_HEADER_COLOR);
					}else{
						coloring.put(payloadStart, payloadStart+5, PES_HEADER_COLOR);
					}
				}
			}
		}

		// FEC / timestamp
		if(buffer.length>PAYLOAD_PACKET_LENGTH){
			final RangeHashMap<Integer, Color> localColoring = new RangeHashMap<>();
			//for some reason using getHTMLHexview resets color, so we use getHTMLHexviewColored with only one color.
			localColoring.put(0, buffer.length-PAYLOAD_PACKET_LENGTH, FEC_COLOR);
			appendHeader(s, "FEC/timestamp:", FEC_COLOR);
			s.append(getHTMLHexviewColored(buffer,PAYLOAD_PACKET_LENGTH,buffer.length-PAYLOAD_PACKET_LENGTH,localColoring)).append("</span>");
			coloring.put(PAYLOAD_PACKET_LENGTH, buffer.length, FEC_COLOR);
		}
		s.append("<br><b>Data:</b><br>").append(getHTMLHexviewColored(buffer,0,buffer.length,coloring));
		return s.toString();
	}

	/**
	 * @param s
	 * @param headerString
	 * @param color
	 */
	private static void appendHeader(final StringBuilder s, final String headerString, final Color color) {
		s.append("<br><span style=\"color:").append(Utils.toHexString(color)).append("\"><b>");
		s.append(headerString);
		s.append("</b><br>");
	}

	/**
	 * @return
	 */
	public boolean hasPayload() {
		return (getAdaptationFieldControl() == 1) || (getAdaptationFieldControl() == 3);
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
	public DefaultMutableTreeNode getJTreeNode(final int modus) {

		final StringBuilder l = new StringBuilder("transport_packet [").append(packetNo).append("]");
		if((getAdaptationFieldControl()==2)||(getAdaptationFieldControl()==3)) { //Adaptation field present
			l.append(" (adaptation field)");
		}
		if(hasPayload()) { //payload present
			l.append(" (payload)");
		}
		if(isPayloadUnitStartIndicator()){
			l.append(" (payload start)");
		}

		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP(l.toString(), this));
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
			if(adaptationField!=null){
				t.add(adaptationField.getJTreeNode(modus));
			}
		}catch(RuntimeException re){ // might be some error in adaptation field, it is not well protected
			adaptationField = null;
			t.add(new DefaultMutableTreeNode(getErrorKVP(ERROR_PARSING_ADAPTATION_FIELD)));
		}

		if(hasPayload()) {
			int payloadStart = 4;
			if(adaptationField!=null){
				payloadStart = 5+adaptationField.getAdaptation_field_length();
			}
			t.add(new DefaultMutableTreeNode(new KVP("data_byte",buffer,payloadStart ,PAYLOAD_PACKET_LENGTH - payloadStart, null)));
			if((getPayloadUnitStartIndicator()==1)&&(getTransportScramblingControl()==0)){
				final PesHeader pesHeaderView = getPesHeader();
				if((pesHeaderView !=null)&&(pesHeaderView.isValidPesHeader())){
					t.add(pesHeaderView.getJTreeNode(modus));
				}
			}
			if(buffer.length>PAYLOAD_PACKET_LENGTH){
				t.add(new DefaultMutableTreeNode(new KVP("FEC/timestamp",buffer,PAYLOAD_PACKET_LENGTH ,buffer.length - PAYLOAD_PACKET_LENGTH, null)));
			}
		}
		return t;
	}

	public long getPacketOffset() {
		return packetOffset;
	}

	public void setPacketOffset(long packetOffset) {
		this.packetOffset = packetOffset;
	}

	/**
	 * @return all data in TSPacket, including Header, Adaptation field and any bytes > 188
	 */
	public byte[] getBuffer() {
		return buffer;
	}

}
