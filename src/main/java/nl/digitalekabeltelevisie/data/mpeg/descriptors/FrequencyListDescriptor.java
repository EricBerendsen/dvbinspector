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

package nl.digitalekabeltelevisie.data.mpeg.descriptors;

import static nl.digitalekabeltelevisie.util.Utils.*;

import java.util.ArrayList;
import java.util.List;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

public class FrequencyListDescriptor extends Descriptor {


	private List<CentreFrequency> frequencyList = new ArrayList<>();
	private int codingType = 0;


	public class CentreFrequency implements TreeNode{
		private final int freqOffset; // use depends on codingtype


		public CentreFrequency(final int freqOffset){
			this.freqOffset = freqOffset;
		}


		@Override
		public KVP getJTreeNode(int modus) {
			long frequencyAsLong = getLong(privateData, freqOffset, 4, MASK_32BITS);
			return switch (codingType) {
			case 1: // satellite
				yield new KVP("centre_frequency", frequencyAsLong, formatSatelliteFrequency(getBCD(privateData, freqOffset * 2, 8)));
			case 2: // cable
				yield new KVP("centre_frequency", frequencyAsLong, formatCableFrequency(getBCD(privateData, freqOffset * 2, 8)));
			case 3: // terrestrial
				yield new KVP("centre_frequency", frequencyAsLong, Descriptor.formatTerrestrialFrequency(frequencyAsLong));
			default:
				yield new KVP("centre_frequency", frequencyAsLong);
			};
		}

	}

	public FrequencyListDescriptor(byte[] b, TableSection parent) {
		super(b, parent);
		codingType = getInt(b, 2, 1, 0x03);
		int t = 1;
		while (t < descriptorLength) {

			final CentreFrequency s = new CentreFrequency((2 + t));
			frequencyList.add(s);
			t += 4;
		}
	}

	@Override
	public KVP getJTreeNode(int modus){

		KVP t = super.getJTreeNode(modus);
		t.add(new KVP("coding_type",codingType,getCodingTypeString(codingType)));
		addListJTree(t,frequencyList,modus,"frequencies");
		return t;
	}



	private static String getCodingTypeString( int codingType) {
		return switch (codingType) {
		case 0 -> "not defined";
		case 1 -> "satellit";
		case 2 -> "cable";
		case 3 -> "terrestrial";
		default -> "illegal value codng_type:"+codingType;
		};

	}
}
