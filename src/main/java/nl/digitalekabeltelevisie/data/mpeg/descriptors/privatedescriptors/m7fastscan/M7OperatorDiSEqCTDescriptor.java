/**
 * 
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 * 
 *  This code is Copyright 2009-2020 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.m7fastscan;

import static nl.digitalekabeltelevisie.util.Utils.getBCD;

import java.util.ArrayList;
import java.util.List;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.Utils;

public class M7OperatorDiSEqCTDescriptor extends M7Descriptor {

	public class DiSEqC implements TreeNode{

		private String orbital_position;
		private int westEastFlag;
		private int reserved;

		@Override
		public KVP getJTreeNode(int modus) {
			KVP t = new KVP("DiSEqC");
			t.add(new KVP("orbital_position", orbital_position, Descriptor.formatOrbitualPosition(orbital_position)));
			t.add(new KVP("west_east_flag", westEastFlag, getWestEastString()));
			t.add(new KVP("reserved", reserved));

			return t;
		}
		
		public String getTotalPositionString() {
			return Descriptor.formatOrbitualPosition(orbital_position) + " " + getWestEastString();
		}

		public String getWestEastString() {
			return westEastFlag==1?"east":"west";
		}

		public String getOrbital_position() {
			return orbital_position;
		}

		public void setOrbital_position(String orbital_position) {
			this.orbital_position = orbital_position;
		}

		public int getWestEastFlag() {
			return westEastFlag;
		}

		public void setWestEastFlag(int westEastFlag) {
			this.westEastFlag = westEastFlag;
		}

		public int getReserved() {
			return reserved;
		}

		public void setReserved(int reserved) {
			this.reserved = reserved;
		}
		
	}
	
	List<DiSEqC> diSEqCList = new ArrayList<>();
	
	public M7OperatorDiSEqCTDescriptor(byte[] b, TableSection parent) {
		super(b, parent);
		diSEqCList = buildDiSEqCList(b,2,descriptorLength);
	}

	
	private List<DiSEqC> buildDiSEqCList(byte[] data, int offset, int diseqc_loop_length) {
		final ArrayList<DiSEqC> r = new ArrayList<>();
		int t =0;
		while(t<diseqc_loop_length){
			final DiSEqC diseqc = new DiSEqC();
			diseqc.setOrbital_position(getBCD(data, (offset +t) *2, 4));
			diseqc.setWestEastFlag(Utils.getInt(data, offset+t+2, 1, 0X80)>>>7);
			diseqc.setReserved(Utils.getInt(data, offset+t+2, 1, Utils.MASK_7BITS));
			r.add(diseqc);
			t = t+3;
		}
		return r;
	}

	@Override
	public KVP getJTreeNode(final int modus){
		final KVP t = super.getJTreeNode(modus);
		t.addList(diSEqCList,modus,"DiSEqC_loop");
		return t;
	}


	public List<DiSEqC> getDiSEqCList() {
		return diSEqCList;
	}

}
