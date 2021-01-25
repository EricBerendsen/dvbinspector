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

package nl.digitalekabeltelevisie.data.mpeg.pes.audio.ac4;

import java.util.logging.Logger;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.gui.utils.GuiUtils;
import nl.digitalekabeltelevisie.util.BitSource;

/**
 * @author Eric
 * 
 * Based on ETSI TS 103 190-2 V1.2.1 (2018-02) 6.2.1.7 ac4_sgi_specifier
 *
 */
public class AC4SgiSpecifier implements TreeNode {
	
	private static final Logger	logger	= Logger.getLogger(AC4SgiSpecifier.class.getName());


	private int group_index;
	private int bitstream_version;

	/**
	 * @param bs
	 * @param bitstream_version
	 */
	public AC4SgiSpecifier(BitSource bs, int bitstream_version) {
		this.bitstream_version = bitstream_version;
		if (bitstream_version == 1) {
			//ac4_substream_group_info();
			logger.warning("ac4_substream_group_info not implemented");
		}else {
			group_index= bs.readBits(3);
			if (group_index == 7) {
				group_index += bs.variable_bits(2);
			}
		}
	}



	@Override
	public DefaultMutableTreeNode getJTreeNode(int modus) {
		DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("ac4_sgi_specifier"));
		if (bitstream_version == 1) {
			t.add(new DefaultMutableTreeNode(GuiUtils.getNotImplementedKVP("ac4_substream_group_info")));
		}else {
			t.add(new DefaultMutableTreeNode(new KVP("group_index",group_index,null)));
		}
		return t;
	}



	public int getGroup_index() {
		return group_index;
	}

}
