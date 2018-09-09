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

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

public class FrequencyListDescriptor extends Descriptor {


	private List<CentreFrequency> frequencyList = new ArrayList<CentreFrequency>();
	private int codingType = 0;


	public class CentreFrequency implements TreeNode{
		private final int freqOffset; // use depends on codingtype


		public CentreFrequency(final int freqOffset){
			this.freqOffset = freqOffset;
		}


		public DefaultMutableTreeNode getJTreeNode(final int modus){
			DefaultMutableTreeNode s=null;
			switch (codingType) {
			case 1: // satellite
				final String frequency = getBCD(privateData, freqOffset*2, 8);
				s=new DefaultMutableTreeNode(new KVP("centre_frequency",frequency,formatSatelliteFrequency(frequency)));

				break;

			case 2: // cable
				final String frequency2 = getBCD(privateData, freqOffset*2, 8);
				s=new DefaultMutableTreeNode(new KVP("centre_frequency",frequency2,formatCableFrequency(frequency2)));

				break;

			case 3: // terrestrial
				final long frequencyT = getLong(privateData, freqOffset, 4, MASK_32BITS);
				s=new DefaultMutableTreeNode(new KVP("centre_frequency",frequencyT,Descriptor.formatTerrestrialFrequency(frequencyT)));

				break;
			default:
				break;
			}
			return s;
		}

	}

	public FrequencyListDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset,parent);
		codingType=getInt(b, offset + 2,1,0x03);
		int t=1;
		while (t<descriptorLength) {

			final CentreFrequency s = new CentreFrequency((offset +2+t));
			frequencyList.add(s);
			t+=4;
		}
	}



	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){

		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("coding_type",codingType,getCodingTypeString(codingType))));
		addListJTree(t,frequencyList,modus,"frequencies");
		return t;
	}



	private static String getCodingTypeString(final int codingType) {
		switch (codingType) {
		case 0: return "not defined";
		case 1: return "satellit";
		case 2: return "cable";
		case 3: return "terrestrial";

		default:
			return "illegal value codng_type:"+codingType;

		}

	}
}
