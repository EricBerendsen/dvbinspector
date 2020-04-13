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

package nl.digitalekabeltelevisie.data.mpeg.pes.video.common;

import static nl.digitalekabeltelevisie.util.Utils.*;

import java.util.*;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.*;

public class CCData implements TreeNode{

	private final int reserved;
	private final int process_cc_data_flag;
	private final int zero_bit;
	private final int cc_count;
	private final int reserved2;
	private final List<Construct> constructs = new ArrayList<Construct>();
	private final int marker_bits;


	public CCData(final byte[] data, final int offset, final int len){
		reserved = getInt(data,offset,1,0x80)>>7;
			process_cc_data_flag = getInt(data,offset,1,0x40)>>6;
			zero_bit = getInt(data,offset,1,0x20)>>5;
			cc_count = getInt(data,offset,1,0x1F);
			reserved2= getInt(data,offset+1,1,MASK_8BITS);
			int localOffset = offset+2;
			for (int i = 0; i < cc_count; i++) {
				final Construct construct = new Construct(data, localOffset);
				constructs.add(construct);
				localOffset+=3;
			}
			marker_bits= getInt(data,localOffset,1,MASK_8BITS);
	}

	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.controller.TreeNode#getJTreeNode(int)
	 */
	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("cc_data()"));
		t.add(new DefaultMutableTreeNode(new KVP("reserved",reserved,null)));
		t.add(new DefaultMutableTreeNode(new KVP("process_cc_data_flag",process_cc_data_flag,process_cc_data_flag==1?"cc_data shall be parsed and its meaning processed":"cc_data shall be discarded")));
		t.add(new DefaultMutableTreeNode(new KVP("zero_bit",zero_bit,null)));
		t.add(new DefaultMutableTreeNode(new KVP("cc_count",cc_count,null)));
		t.add(new DefaultMutableTreeNode(new KVP("reserved",reserved2,null)));
		addListJTree(t,constructs,modus,"CC Constructs");
		//t.add(new DefaultMutableTreeNode(new KVP("constructs",constructs)));
		t.add(new DefaultMutableTreeNode(new KVP("marker_bits",marker_bits,null)));
		return t;
	}

	public int getProcess_cc_data_flag() {
		return process_cc_data_flag;
	}

	public int getCc_count() {
		return cc_count;
	}

	public List<Construct> getConstructs() {
		return constructs;
	}

	public int getMarker_bits() {
		return marker_bits;
	}
}