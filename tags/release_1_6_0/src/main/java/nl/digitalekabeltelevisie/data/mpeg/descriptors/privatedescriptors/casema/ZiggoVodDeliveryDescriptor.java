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

package nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.casema;

import static nl.digitalekabeltelevisie.util.Utils.*;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

public class ZiggoVodDeliveryDescriptor extends Descriptor {


	private final List<VODChannel> channelList = new ArrayList<VODChannel>();

	public static class VODChannel implements TreeNode{
		private String frequency; // use as bits, BCD coded.
		private int FEC_outer;
		private int modulation;
		private String symbol_rate;
		private int FEC_inner;

		public int getFEC_inner() {
			return FEC_inner;
		}


		public void setFEC_inner(final int fec_inner) {
			FEC_inner = fec_inner;
		}


		public int getFEC_outer() {
			return FEC_outer;
		}

		public String getFEC_outerString() {
			switch (getFEC_outer()) {
			case 0: return "not defined";
			case 1: return "no outer FEC coding";
			case 2: return "RS(204/188)";
			default: return "reserved for future use";
			}
		}

		public String getModulationString() {
			switch (getModulation()) {
			case 0x00: return "not defined";
			case 0x01: return "16-QAM";
			case 0x02: return "32-QAM";
			case 0x03: return "64-QAM";
			case 0x04: return "128-QAM";
			case 0x05: return "256-QAM";
			default: return "reserved for future use";		}
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

		public void setFEC_outer(final int fec_outer) {
			FEC_outer = fec_outer;
		}


		public int getModulation() {
			return modulation;
		}


		public void setModulation(final int modulation) {
			this.modulation = modulation;
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
			return super.toString() + "Frequency="+getFrequency()+", FEC_outer="+getFEC_outerString()+", modulation="+getModulationString()+", Symbol Rate="+getSymbol_rate()+", FEC_inner="+getFEC_innerString();
		}


		public DefaultMutableTreeNode getJTreeNode(final int modus){
			final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("VOD_channel"));

			t.add(new DefaultMutableTreeNode(new KVP("frequency",frequency ,null)));
			t.add(new DefaultMutableTreeNode(new KVP("FEC_outer",FEC_outer ,getFEC_outerString())));
			t.add(new DefaultMutableTreeNode(new KVP("modulation",modulation ,getModulationString())));
			t.add(new DefaultMutableTreeNode(new KVP("symbol_rate",symbol_rate ,null)));
			t.add(new DefaultMutableTreeNode(new KVP("FEC_inner",FEC_inner ,getFEC_innerString())));

			return t;
		}



		public VODChannel(final String frequency, final int fec_outer, final int modulation, final String symbol_rate, final int fec_inner) {
			super();
			this.frequency = frequency;
			FEC_outer = fec_outer;
			this.modulation = modulation;
			this.symbol_rate = symbol_rate;
			FEC_inner = fec_inner;
		}

	}


	public ZiggoVodDeliveryDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset, parent);

		int t=0;
		while (t<descriptorLength) {
			String frequency; // use as bits, BCD coded.
			int FEC_outer;
			int modulation;
			String symbol_rate;
			int FEC_inner;

			frequency = getBCD(b, 2*(offset+2+t), 8);
			FEC_outer = getInt(b, offset+7+t, 1, 0x0f);
			modulation= getInt(b, offset+8+t, 1, 0xff);
			symbol_rate = getBCD(b, (offset+9+t)*2,7);
			FEC_inner = getInt(b, offset+12+t, 1, 0x0f);
			final VODChannel s = new VODChannel(frequency, FEC_outer, modulation, symbol_rate,FEC_inner);
			channelList.add(s);

			t=t+11;
		}

	}

	@Override
	public String getDescriptorname(){
		return "Video On Demand delivery descriptor";
	}



	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		addListJTree(t,channelList,modus,"vod_channels");
		return t;
	}
}
