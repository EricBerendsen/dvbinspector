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

package nl.digitalekabeltelevisie.data.mpeg.pes.audio.ac4;

import static nl.digitalekabeltelevisie.util.Utils.addListJTree;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.util.BitSource;

/**
 * @author Eric
 *
 */
public class SubstreamIndexTable implements TreeNode {
	
	
	class SubStreamSize implements TreeNode{
		private int substream_size;

		SubStreamSize(int substream_size){
			this.substream_size = substream_size;
		}
		
		@Override
		public DefaultMutableTreeNode getJTreeNode(int modus) {
			DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("substream_size",substream_size,null));
			return t;
		}
	}

	private int n_substreams;
	private int b_size_present;
	private int b_more_bits;

	private List<SubStreamSize> subStreamSizes= new ArrayList<>();
	
	/**
	 * @param bs
	 */
	public SubstreamIndexTable(BitSource bs) {
		n_substreams = bs.readBits(2);
		if (n_substreams == 0) {
			n_substreams = bs.variable_bits(2) + 4;
		}
		if (n_substreams == 1) {
			b_size_present = bs.readBits(1);
		} else {
			b_size_present = 1;
		}
		if (b_size_present == 1) {
			for (int s = 0; s < n_substreams; s++) {
				int b_more_bits = bs.readBits(1);
				int substream_size = bs.readBits(10);
				if (b_more_bits == 1) {
					substream_size += (bs.variable_bits(2) << 10);
				}
				subStreamSizes.add(new SubStreamSize(substream_size));
			}
		}
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(int modus) {
		DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("substream_index_table"));
		t.add(new DefaultMutableTreeNode(new KVP("n_substreams", n_substreams, null)));
		t.add(new DefaultMutableTreeNode(new KVP("b_size_present", b_size_present, (n_substreams != 1) ? "implied" : null)));
		addListJTree(t, subStreamSizes, modus, "subStreamSizes");

		return t;
	}

}
