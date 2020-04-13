/**
 * 
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 * 
 *  This code is Copyright 2009-2019 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

import java.util.*;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.*;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.*;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.Utils;

public class M7OperatorDiSEqCTDescriptor extends M7Descriptor {

	public class DiSEqC implements TreeNode{

		private String orbital_position;
		private int westEastFlag;
		private int reserved;

		@Override
		public DefaultMutableTreeNode getJTreeNode(int modus) {
			DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("DiSEqC"));
			t.add(new DefaultMutableTreeNode(new KVP("orbital_position",orbital_position,Descriptor.formatOrbitualPosition(orbital_position))));
			t.add(new DefaultMutableTreeNode(new KVP("west_east_flag",westEastFlag,westEastFlag==1?"east":"west")));
			t.add(new DefaultMutableTreeNode(new KVP("reserved",reserved,null)));

			return t;
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
	
	public M7OperatorDiSEqCTDescriptor(byte[] b, int offset, TableSection parent) {
		super(b, offset, parent);
		diSEqCList = buildDiSEqCList(b,offset+2,descriptorLength);
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
	public DefaultMutableTreeNode getJTreeNode(final int modus){
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		Utils.addListJTree(t,diSEqCList,modus,"DiSEqC_loop");
		return t;
	}

}
