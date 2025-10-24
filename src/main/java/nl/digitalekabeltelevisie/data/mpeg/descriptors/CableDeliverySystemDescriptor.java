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

import static nl.digitalekabeltelevisie.util.Utils.MASK_4BITS;
import static nl.digitalekabeltelevisie.util.Utils.MASK_8BITS;
import static nl.digitalekabeltelevisie.util.Utils.getBCD;
import static nl.digitalekabeltelevisie.util.Utils.getInt;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

public class CableDeliverySystemDescriptor extends Descriptor{

	private String frequency; // use as bits, BCD coded.
	private int FEC_outer;
	private int modulation;
	private String symbol_rate;
	private int FEC_inner;

	public CableDeliverySystemDescriptor(byte[] b, TableSection parent) {
		super(b, parent);
		frequency = getBCD(b, 4, 8);
		FEC_outer = getInt(b, 7, 1, MASK_4BITS);
		modulation = getInt(b, 8, 1, MASK_8BITS);
		symbol_rate = getBCD(b, 18, 7);
		FEC_inner = getInt(b, 12, 1, MASK_4BITS);
	}

	public int getFEC_inner() {
		return FEC_inner;
	}

	public int getFEC_outer() {
		return FEC_outer;
	}

	public String getFEC_outerString() {
		return switch (FEC_outer) {
		case 0 -> "not defined";
		case 1 -> "no outer FEC coding";
		case 2 -> "RS(204/188)";
		default -> "reserved for future use";
		};
	}

	public static String getModulationString(int mod) {
		return switch (mod) {
		case 0x00 -> "not defined";
		case 0x01 -> "16-QAM";
		case 0x02 -> "32-QAM";
		case 0x03 -> "64-QAM";
		case 0x04 -> "128-QAM";
		case 0x05 -> "256-QAM";
		default -> "reserved for future use";
		};
	}

	public int getModulation() {
		return modulation;
	}


	public String getFrequency() {
		return frequency;
	}

	public String getSymbol_rate() {
		return symbol_rate;
	}


	@Override
	public String toString() {
		return super.toString() + "Frequency="+ frequency +", FEC_outer="+getFEC_outerString()+", modulation="+getModulationString(modulation)+", Symbol Rate="+ symbol_rate +", FEC_inner="+Descriptor.getFEC_innerString(FEC_inner);
	}


	@Override
	public KVP getJTreeNode(int modus){
		KVP t = super.getJTreeNode(modus);

		t.add(new KVP("frequency",frequency ,Descriptor.formatCableFrequency(frequency)));
		t.add(new KVP("FEC_outer",FEC_outer ,getFEC_outerString()));
		t.add(new KVP("modulation",modulation ,getModulationString(modulation)));
		t.add(new KVP("symbol_rate",symbol_rate ,Descriptor.formatSymbolRate(symbol_rate)));
		t.add(new KVP("FEC_inner",FEC_inner ,Descriptor.getFEC_innerString(FEC_inner)));

		return t;
	}

}
