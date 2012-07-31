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

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.Utils;

public class IBPDescriptor extends Descriptor {
	
	// based on ITU-T Rec. H.222.0 (2000 E)/ ISO/IEC 13818-1 : 2000 (E) p 74 2.6.34 IBP descriptor

	private int closed_gop_flag;
	private int identical_gop_flag;
	private int max_gop_length;


	public IBPDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset,parent);
		closed_gop_flag = Utils.getInt(b, offset+2, 1, 0x80)>>7;
		identical_gop_flag = Utils.getInt(b, offset+2, 1, 0x40)>>6;
		max_gop_length = Utils.getInt(b, offset+2, 2, Utils.MASK_14BITS);


	}


	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("closed_gop_flag",closed_gop_flag ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("identical_gop_flag",identical_gop_flag ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("max_gop_length",max_gop_length ,null)));


		return t;
	}


	public int getClosed_gop_flag() {
		return closed_gop_flag;
	}


	public int getIdentical_gop_flag() {
		return identical_gop_flag;
	}


	public int getMax_gop_length() {
		return max_gop_length;
	}


}
