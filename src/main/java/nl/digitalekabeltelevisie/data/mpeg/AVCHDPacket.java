/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2024 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

import static nl.digitalekabeltelevisie.util.Utils.*;

import java.awt.Color;
import java.util.Arrays;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.util.RangeHashMap;
import nl.digitalekabeltelevisie.util.Utils;

/**
 * 
 */
public class AVCHDPacket extends TSPacket {
	
	static final int bit30 = 0x4000_0000;
	byte[] tp_extra_header; 
	long roll_over;

	public AVCHDPacket(byte[] buf, int no, TransportStream ts, long roll_over) {
		super(Arrays.copyOfRange(buf,4,192), no, ts);
		tp_extra_header = Arrays.copyOf(buf,4);
		this.roll_over = roll_over;
	}

	
	public byte[] getTP_extra_header() {
		return tp_extra_header;
	}
	
	public int getCopyPermissionIndicator() {
		return (tp_extra_header[0] & 0b1100_0000)>>6;
	}
	
	public int getArrivalTimestamp() {
		return getInt(tp_extra_header,0,4,MASK_30BITS);
	}
	
	
	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {

		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP(buildNodeLabel(),this));
		final DefaultMutableTreeNode tpHeaderNode = new DefaultMutableTreeNode(new KVP("tp_extra_header",tp_extra_header,null));
		tpHeaderNode.add(new DefaultMutableTreeNode(new KVP("Copy_permission_indicator",getCopyPermissionIndicator(),null)));
		tpHeaderNode.add(new DefaultMutableTreeNode(new KVP("Roll-over",roll_over,null)));
		final int arrivalTimestamp = getArrivalTimestamp();
		long cats = (roll_over * bit30) + arrivalTimestamp;
		tpHeaderNode.add(new DefaultMutableTreeNode(new KVP("Arrival_time_stamp",arrivalTimestamp,printPCRTime(arrivalTimestamp))));
		tpHeaderNode.add(new DefaultMutableTreeNode(new KVP("Continuos ATS ",cats,printPCRTime(cats))));
		t.add(tpHeaderNode);
		addMainPacketDetails(modus, t);
		return t;
	}
	
	public String getHTML() {
		final StringBuilder s = new StringBuilder();

		s.append("Packet: ").append(packetNo);
		if(packetOffset!=-1){
			s.append("<br>File Offset: ").append(packetOffset);
		}
		if(transportStream!=null){
			//s.append("<br>Time: ").append(transportStream.getPacketTime(packetNo));
			final short pid = transportStream.getPacket_pid(packetNo);
			s.append("<br>").append(escapeHtmlBreakLines(transportStream.getShortLabel(pid))).append("<br>");
		}
		

		
		final RangeHashMap<Integer, Color> coloring = new RangeHashMap<>();
		
		//tp_extra_header
		Utils.appendHeader(s, "TP_extra_header: 0x" + toHexString(tp_extra_header, 0, 4), FEC_COLOR);
		coloring.put(0, 4, FEC_COLOR);
		
		
		s.append("<br>Copy_permission_indicator: ").append(getCopyPermissionIndicator());
		s.append("<br>roll-over: ").append(roll_over);
		s.append("<br>Arrival_time_stamp: ").append(getArrivalTimestamp()).append(" (").append(printPCRTime(getArrivalTimestamp())).append(")").append("</span><br>");

		addBasicPacketDetails(s, 4, coloring);
		
		byte[] buf = new byte[192];
		
		System.arraycopy(tp_extra_header, 0, buf, 0, 4);
		System.arraycopy(buffer, 0, buf, 4, 188);

		s.append("<br><b>Data:</b><br>").append(getHTMLHexviewColored(buf,0,192,coloring));
		return s.toString();
	}


	public long getRoll_over() {
		return roll_over;
	}


	public void setRoll_over(long roll_over) {
		this.roll_over = roll_over;
	}


}
