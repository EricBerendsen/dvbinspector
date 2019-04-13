/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2017 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.descriptors.extension.dvb;

import static nl.digitalekabeltelevisie.util.Utils.*;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.*;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.gui.utils.GuiUtils;

/* based on DVB Document A038 January 2017 */

public class S2XSatelliteDeliverySystemDescriptor extends DVBExtensionDescriptor {


	// S2X satellite S2Xdelivery system descriptor 0x17

	private final int receiver_profiles; 
	private final int reserved_zero_future_use1; 
	private final int S2X_mode; 
	private final int scrambling_sequence_selector; 
	private final int reserved_zero_future_use2; 
	private final int TS_GS_S2X_mode;
	
	private int reserved_zero_future_use3;
	private int scrambling_sequence_index;

	private String frequency; // use as bits, BCD coded.
	
	private final String orbitalPosition;
	private final int westEastFlag;
	private final int polarization;
	private final int multiple_input_stream_flag;
	private final int reserved_zero_future_use4;
	private final int rollOff;
	private final int reserved_zero_future_use5;

	
	private String symbol_rate;
	
	private int input_stream_identifier;
	private int timeslice_number;



	public S2XSatelliteDeliverySystemDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset,parent);
		receiver_profiles = getInt(b, offset+3, 1, 0xF8)>>3;
		reserved_zero_future_use1 = getInt(b, offset+3, 1, MASK_3BITS);
		
		S2X_mode = getInt(b, offset+4, 1, 0xC0)>>6;
		scrambling_sequence_selector = getInt(b, offset+4, 1, 0x20)>>5;
		reserved_zero_future_use2 = getInt(b, offset+4, 1, 0x1C)>>2;
		TS_GS_S2X_mode = getInt(b, offset+4, 1, MASK_2BITS);
		int localOffset = offset+5;
		if (scrambling_sequence_selector == 1) {
			reserved_zero_future_use3 = getInt(b, localOffset, 1, 0xFC)>>2;
			scrambling_sequence_index = getInt(b, localOffset, 3, MASK_18BITS);
			localOffset+=3;
		}
		frequency = getBCD(b, localOffset*2, 8);
		localOffset+=4;
		orbitalPosition = getBCD(b, localOffset*2, 4);
		localOffset+=2;
		westEastFlag= getInt(b, localOffset , 1, 0x80)>>7;
		polarization= getInt(b, localOffset , 1, 0x60)>>5;
		multiple_input_stream_flag = getInt(b, localOffset , 1, 0x10)>>4;
		reserved_zero_future_use4 = getInt(b, localOffset , 1, 0x08)>>3;
		rollOff= getInt(b, localOffset , 1, MASK_3BITS);
		localOffset++;
		reserved_zero_future_use5 = getInt(b, localOffset , 1, 0xF0)>>4;
		symbol_rate = getBCD(b, (localOffset*2) + 1,7);
		localOffset+=4;
		if(multiple_input_stream_flag == 1){
			input_stream_identifier =  getInt(b, localOffset , 1, MASK_8BITS);
			localOffset++;
		}
		
		if (S2X_mode==2) {
			timeslice_number = getInt(b, localOffset , 1, MASK_8BITS);
			localOffset++;
		}


	}


	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){

		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("receiver_profiles",receiver_profiles,null)));
		t.add(new DefaultMutableTreeNode(new KVP("reserved_zero_future_use",reserved_zero_future_use1,null)));
		t.add(new DefaultMutableTreeNode(new KVP("S2X_mode",S2X_mode,null)));
		t.add(new DefaultMutableTreeNode(new KVP("scrambling_sequence_selector",scrambling_sequence_selector,null)));
		t.add(new DefaultMutableTreeNode(new KVP("reserved_zero_future_use",reserved_zero_future_use2,null)));
		t.add(new DefaultMutableTreeNode(new KVP("TS_GS_S2X_mode",TS_GS_S2X_mode,getTS_GS_S2X_modeString(TS_GS_S2X_mode))));
		if (scrambling_sequence_selector == 1) {
			t.add(new DefaultMutableTreeNode(new KVP("reserved_zero_future_use",reserved_zero_future_use3,null)));
			t.add(new DefaultMutableTreeNode(new KVP("scrambling_sequence_index",scrambling_sequence_index,null)));
		}
		t.add(new DefaultMutableTreeNode(new KVP("frequency",frequency ,Descriptor.formatSatelliteFrequency(frequency))));
		t.add(new DefaultMutableTreeNode(new KVP("orbital_position",orbitalPosition,Descriptor.formatOrbitualPosition(orbitalPosition))));
		t.add(new DefaultMutableTreeNode(new KVP("west_east_flag",westEastFlag,westEastFlag==1?"east":"west")));
		t.add(new DefaultMutableTreeNode(new KVP("polarization",polarization,SatelliteDeliverySystemDescriptor.getPolarizationString(polarization))));
		
		t.add(new DefaultMutableTreeNode(new KVP("multiple_input_stream_flag",multiple_input_stream_flag,multiple_input_stream_flag==1?"multiple transport streams are conveyed":"single transport stream is carried")));
		t.add(new DefaultMutableTreeNode(new KVP("reserved_zero_future_use",reserved_zero_future_use4,null)));
		t.add(new DefaultMutableTreeNode(new KVP("roll_off",rollOff,getS2XRollOffString(rollOff))));

		t.add(new DefaultMutableTreeNode(new KVP("reserved_zero_future_use",reserved_zero_future_use5,null)));
		t.add(new DefaultMutableTreeNode(new KVP("symbol_rate",symbol_rate,Descriptor.formatSymbolRate(symbol_rate))));
		
		if(multiple_input_stream_flag == 1){
			t.add(new DefaultMutableTreeNode(new KVP("input_stream_identifier",input_stream_identifier,null)));
		}
		if (S2X_mode==2) {
			t.add(new DefaultMutableTreeNode(new KVP("timeslice_number",timeslice_number,null)));
		}
		if (S2X_mode==3) {
			t.add(new DefaultMutableTreeNode(GuiUtils.getNotImplementedKVP("S2X_mode==3")));
		}

		return t;
	}


	private static String getTS_GS_S2X_modeString(int tS_GS_S2X_mode2) {
		switch (tS_GS_S2X_mode2) {
		case 0:
			return "generic packetized";
		case 1:
			return "GSE";
		case 2:
			return "GSE high efficiency mode";
		case 3:
			return "Transport Stream";

		default:
			return "illegal value";
		}

	}

	public static String getS2XRollOffString(final int pol) {
		switch (pol) {
		case 0x00: return "\u03b1 = 0,35"; // alpha
		case 0x01: return "\u03b1 = 0,25";
		case 0x02: return "\u03b1 = 0,20";
		case 0x03: return "reserved";
		case 0x04: return "\u03b1 = 0,15"; // alpha
		case 0x05: return "\u03b1 = 0,10";
		case 0x06: return "\u03b1 = 0,05";
		case 0x07: return "reserved";
		default: return "illegal value";		}
	}


}
