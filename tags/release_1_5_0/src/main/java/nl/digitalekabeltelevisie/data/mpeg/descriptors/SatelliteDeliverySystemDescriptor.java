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

public class SatelliteDeliverySystemDescriptor extends Descriptor {

	private String frequency; // use as bits, BCD coded.
	private final String orbitalPosition;
	private final int westEastFlag;
	private final int polarization;
	private final int rollOff;
	private final int modulationSystem;
	private final int modulationType;
	private String symbol_rate;
	private int FEC_inner;


	public SatelliteDeliverySystemDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset, parent);


		frequency = getBCD(b, (offset*2) + 4, 8);
		orbitalPosition = getBCD(b, (offset*2) + 12, 4);
		westEastFlag= getInt(b, offset+ 8 , 1, 0x80)>>7;
		polarization= getInt(b, offset+ 8 , 1, 0x60)>>5;
		rollOff= getInt(b, offset+ 8 , 1, 0x18)>>3;
		modulationSystem= getInt(b, offset+ 8 , 1, 0x04)>>2;
		modulationType= getInt(b, offset+ 8 , 1, MASK_2BITS);
		symbol_rate = getBCD(b, (offset*2)+ 18,7);
		FEC_inner = getInt(b, offset+ 12, 1, MASK_4BITS);


	}


	public int getFEC_inner() {
		return FEC_inner;
	}


	public void setFEC_inner(final int fec_inner) {
		FEC_inner = fec_inner;
	}



	public static String getModulationString(final int mod) {
		switch (mod) {
		case 0x00: return "Auto";
		case 0x01: return "QPSK";
		case 0x02: return "8PSK";
		case 0x03: return "16-QAM";
		default: return "illegal value";		}
	}

	public static String getPolarizationString(final int pol) {
		switch (pol) {
		case 0x00: return "linear - horizontal";
		case 0x01: return "linear - vertical";
		case 0x02: return "Circular - left";
		case 0x03: return "Circular - right";
		default: return "illegal value";		}
	}

	public static String getRollOffString(final int pol) {
		switch (pol) {
		case 0x00: return "\u03b1 = 0,35"; // alpha
		case 0x01: return "\u03b1 = 0,25";
		case 0x02: return "\u03b1 = 0,20";
		case 0x03: return "reserved";
		default: return "illegal value";		}
	}
	public String getFrequency() {
		return frequency;
	}


	public void setFrequency(final String frequency) {
		this.frequency = frequency;
	}


	public String getSymbol_rate() {
		return symbol_rate;
	}


	public void setSymbol_rate(final String symbol_rate) {
		this.symbol_rate = symbol_rate;
	}


	@Override
	public String toString() {
		return super.toString() + "Frequency="+getFrequency()+", FEC_inner="+Descriptor.getFEC_innerString(FEC_inner);
	}


	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);

		t.add(new DefaultMutableTreeNode(new KVP("frequency",frequency ,Descriptor.formatSatelliteFrequency(frequency))));
		t.add(new DefaultMutableTreeNode(new KVP("orbital_position",orbitalPosition,Descriptor.formatOrbitualPosition(orbitalPosition))));
		t.add(new DefaultMutableTreeNode(new KVP("west_east_flag",westEastFlag,westEastFlag==1?"east":"west")));
		t.add(new DefaultMutableTreeNode(new KVP("polarization",polarization,getPolarizationString(polarization))));
		t.add(new DefaultMutableTreeNode(new KVP("modulation_system",modulationSystem,modulationSystem==1?"DVB-S2":"DVB-S")));
		if(modulationSystem==1){
			t.add(new DefaultMutableTreeNode(new KVP("roll_off",rollOff,getRollOffString(rollOff))));
		}
		t.add(new DefaultMutableTreeNode(new KVP("modulation_type",modulationType,getModulationString(modulationType))));
		t.add(new DefaultMutableTreeNode(new KVP("symbol_rate",symbol_rate ,Descriptor.formatSymbolRate(symbol_rate))));
		t.add(new DefaultMutableTreeNode(new KVP("FEC_inner",FEC_inner ,Descriptor.getFEC_innerString(FEC_inner))));

		return t;
	}


	public String getOrbitalPosition() {
		return orbitalPosition;
	}


	public int getWestEastFlag() {
		return westEastFlag;
	}


	public int getPolarization() {
		return polarization;
	}


	public int getRollOff() {
		return rollOff;
	}


	public int getModulationSystem() {
		return modulationSystem;
	}


	public int getModulationType() {
		return modulationType;
	}
}
