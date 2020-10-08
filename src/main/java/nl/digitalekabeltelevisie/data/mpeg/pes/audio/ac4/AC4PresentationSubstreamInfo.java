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

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.util.BitSource;

/**
 * @author Eric
 *
 */
public class AC4PresentationSubstreamInfo implements TreeNode {

	private int b_alternative;
	private int b_pres_ndot;
	private int substream_index;

	/**
	 * @param bs
	 */
	public AC4PresentationSubstreamInfo(BitSource bs) {
		b_alternative = bs.readBits(1);
		b_pres_ndot = bs.readBits(1);
		substream_index = bs.readBits(2);
		if (substream_index == 3) {
			substream_index += bs.variable_bits(2);
		}
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(int modus) {
		DefaultMutableTreeNode ac4_presentation_substream_info_node = new DefaultMutableTreeNode(new KVP("ac4_presentation_substream_info"));
		ac4_presentation_substream_info_node.add(new DefaultMutableTreeNode(new KVP("b_alternative",b_alternative,null)));
		ac4_presentation_substream_info_node.add(new DefaultMutableTreeNode(new KVP("b_pres_ndot",b_pres_ndot,null)));
		ac4_presentation_substream_info_node.add(new DefaultMutableTreeNode(new KVP("substream_index",substream_index,null)));
		return ac4_presentation_substream_info_node;
	}

}