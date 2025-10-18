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

import static nl.digitalekabeltelevisie.util.Utils.addListJTree;
import static nl.digitalekabeltelevisie.util.Utils.getBCD;
import static nl.digitalekabeltelevisie.util.Utils.getInt;

import java.util.ArrayList;
import java.util.List;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

public class ZiggoVodDeliveryDescriptor extends Descriptor {


	private final List<VODChannel> channelList = new ArrayList<>();

	public record VODChannel(String frequency,int FEC_outer,int modulation,String symbol_rate, int FEC_inner) implements TreeNode{

		public String getFEC_outerString() {
			return switch (FEC_outer) {
			case 0 -> "not defined";
			case 1 -> "no outer FEC coding";
			case 2 -> "RS(204/188)";
			default -> "reserved for future use";
			};
		}

		public String getModulationString() {
			return switch (modulation) {
			case 0x00 -> "not defined";
			case 0x01 -> "16-QAM";
			case 0x02 -> "32-QAM";
			case 0x03 -> "64-QAM";
			case 0x04 -> "128-QAM";
			case 0x05 -> "256-QAM";
			default -> "reserved for future use";
			};
		}

		public String getFEC_innerString() {
			return switch (FEC_inner) {
			case 0 -> "not defined";
			case 1 -> "1/2 conv. code rate";
			case 2 -> "2/3 conv. code rate";
			case 3 -> "3/4 conv. code rate";
			case 4 -> "5/6 conv. code rate";
			case 5 -> "7/8 conv. code rate";
			case 6 -> "8/9 conv. code rate";
			case 7 -> "3/5 conv. code rate";
			case 8 -> "4/5 conv. code rate";
			case 9 -> "9/10 conv. code rate";
			case 15 -> "no conv. Coding";
			default -> "reserved for future use";
			};
		}

		@Override
		public KVP getJTreeNode(int modus) {
			KVP t = new KVP("VOD_channel");

			t.add(new KVP("frequency", frequency));
			t.add(new KVP("FEC_outer", FEC_outer, getFEC_outerString()));
			t.add(new KVP("modulation", modulation, getModulationString()));
			t.add(new KVP("symbol_rate", symbol_rate));
			t.add(new KVP("FEC_inner", FEC_inner, getFEC_innerString()));

			return t;
		}

	}


	public ZiggoVodDeliveryDescriptor(byte[] b, TableSection parent) {
		super(b, parent);

		int t=0;
		while (t<descriptorLength) {
			String frequency; // use as bits, BCD coded.
			int FEC_outer;
			int modulation;
			String symbol_rate;
			int FEC_inner;

			frequency = getBCD(b, 2*(2+t), 8);
			FEC_outer = getInt(b, 7+t, 1, 0x0f);
			modulation= getInt(b, 8+t, 1, 0xff);
			symbol_rate = getBCD(b, (9+t)*2,7);
			FEC_inner = getInt(b, 12+t, 1, 0x0f);
			VODChannel s = new VODChannel(frequency, FEC_outer, modulation, symbol_rate,FEC_inner);
			channelList.add(s);

			t=t+11;
		}

	}

	@Override
	public String getDescriptorname(){
		return "Video On Demand delivery descriptor";
	}

	@Override
	public KVP getJTreeNode(int modus){
		KVP t = super.getJTreeNode(modus);
		addListJTree(t,channelList,modus,"vod_channels");
		return t;
	}
}
