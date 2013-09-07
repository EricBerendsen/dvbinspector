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

package nl.digitalekabeltelevisie.data.mpeg.descriptors;

import static nl.digitalekabeltelevisie.util.Utils.*;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

public class TerrestrialDeliverySystemDescriptor extends Descriptor {

	private long frequency; // 32-bit uimsbf field giving the binary coded frequency value in multiples of 10 Hz.
	private final int bandwidth;
	private final int priority;
	private final int time_Slicing_indicator;
	private int FEC_inner;


	public TerrestrialDeliverySystemDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset, parent);


		frequency = getLong(b, offset+2, 4, MASK_32BITS);
		bandwidth = getInt(b, offset + 6, 1, 0xE0)>>>5;
		priority= getInt(b, offset+ 6 , 1, 0x10)>>>4;
		time_Slicing_indicator = getInt(b, offset+ 6 , 1, 0x08)>>>3;
		FEC_inner = getInt(b, offset+ 12, 1, MASK_4BITS);


	}


	public int getFEC_inner() {
		return FEC_inner;
	}


	public void setFEC_inner(final int fec_inner) {
		FEC_inner = fec_inner;
	}



	public static String getBandwidtString(final int b) {
		switch (b) {
		case 0: return "8 MHz";
		case 1: return "7 MHz";
		case 2: return "6 MHz)";
		case 3: return "5 MHz)";
		default: return "reserved for future use";
		}
	}

	public static String getPriorityString(final int p) {
		switch (p) {
		case 0x00: return "LP (low priority)";
		case 0x01: return "HP (high priority)";

		default: return "error";		}
	}

	public String getFEC_innerString() {
		switch (getFEC_inner()) {
		case 0: return"not defined";
		case 1: return"1/2 conv. code rate";
		case 2: return"2/3 conv. code rate";
		case 3: return"3/4 conv. code rate";
		case 4: return"5/6 conv. code rate";
		case 5: return"7/8 conv. code rate";
		case 6: return"8/9 conv. code rate";
		case 7: return"3/5 conv. code rate";
		case 8: return"4/5 conv. code rate";
		case 9: return"9/10 conv. code rate";
		case 15: return"no conv. Coding";
		default: return"reserved for future use";
		}
	}



	public long getFrequency() {
		return frequency;
	}


	public void setFrequency(final long frequency) {
		this.frequency = frequency;
	}



	@Override
	public String toString() {
		return super.toString() + "Frequency="+getFrequency()+", priority="+getPriorityString(priority);
	}


	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);

		t.add(new DefaultMutableTreeNode(new KVP("frequency",frequency , Descriptor.formatTerrestrialFrequency(frequency))));
		t.add(new DefaultMutableTreeNode(new KVP("bandwidth",bandwidth ,getBandwidtString(bandwidth))));
		t.add(new DefaultMutableTreeNode(new KVP("priority",priority ,getPriorityString(priority))));
		t.add(new DefaultMutableTreeNode(new KVP("Time_Slicing_indicator",time_Slicing_indicator ,time_Slicing_indicator==1?"not used":"at least one elementary stream uses Time Slicing.")));
		t.add(new DefaultMutableTreeNode(new KVP("FEC_inner",FEC_inner ,getFEC_innerString())));

		return t;
	}


	public int getBandwidth() {
		return bandwidth;
	}


	public int getPriority() {
		return priority;
	}


	public int getTime_Slicing_indicator() {
		return time_Slicing_indicator;
	}
}
