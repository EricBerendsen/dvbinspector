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

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.util.BitSource;
import nl.digitalekabeltelevisie.util.LookUpList;

/**
 * @author Eric
 *
 * based on TS 103 190-1 V1.3.1 (2018-02) 4.2.3.5 emdf_info - EMDF information
 */
public class EmdfInfo implements TreeNode {

	LookUpList protection_length_list = new LookUpList.Builder().
			add(0,"0 / Reserved").
			add(1,"8 bits").
			add(2,"32 bits").
			add(3,"128 bits").
			build();

	
	private int emdf_version;
	private int key_id;
	private int b_emdf_payloads_substream_info;
	private int substream_index;
	private int protection_length_primary;
	private int protection_length_secondary;
	private byte[] protection_bits_primary;
	private byte[] protection_bits_secondary;

	/**
	 * @param bs
	 */
	public EmdfInfo(BitSource bs) {
		emdf_version= bs.readBits(2);
		if ( emdf_version == 3) {
			emdf_version += bs.variable_bits(2);
		}
		key_id= bs.readBits(3);
		if (key_id == 7) {
			key_id += bs.variable_bits(3);
		}
		if ((b_emdf_payloads_substream_info= bs.readBits(1)) ==1) { 
			// 4.2.3.10 emdf_payloads_substream_info;
			if ((substream_index= bs.readBits(2)) ==3) {
				substream_index += bs.variable_bits(2);
				
			}
		}
		// 4.2.14.15 emdf_protection - EMDF protection data
		emdf_protection(bs);

	}
	

	/**
	 * @param bs
	 */
	private void emdf_protection(BitSource bs) {
		protection_length_primary= bs.readBits(2);
		protection_length_secondary= bs.readBits(2);
		protection_bits_primary = bs.readUnalignedBytes(noPretectionBytes(protection_length_primary));
		protection_bits_secondary = bs.readUnalignedBytes(noPretectionBytes(protection_length_secondary));
		
	}
	
	//4.3.15.3 emdf_protection - EMDF protection data TS 103 190-1 V1.3.1 (2018-02)
	private static int noPretectionBytes(int protection_length) {
		switch (protection_length) {
		case 0:
			return 0;
		case 1:
			return 1;
		case 2:
			return 4;
		case 3:
			return 16;
		default:
			return 0;
		}
	}


	@Override
	public DefaultMutableTreeNode getJTreeNode(int modus) {
		DefaultMutableTreeNode emdf_info_node = new DefaultMutableTreeNode(new KVP("emdf_info"));
		emdf_info_node.add(new DefaultMutableTreeNode(new KVP("emdf_version",emdf_version,null)));
		emdf_info_node.add(new DefaultMutableTreeNode(new KVP("key_id",key_id,null)));
		emdf_info_node.add(new DefaultMutableTreeNode(new KVP("b_emdf_payloads_substream_info",b_emdf_payloads_substream_info,null)));

		if (b_emdf_payloads_substream_info ==1) { 
			emdf_info_node.add(new DefaultMutableTreeNode(new KVP("substream_index",substream_index,null)));
		}

		
		DefaultMutableTreeNode emdf_protection_node = new DefaultMutableTreeNode(new KVP("emdf_protection"));
		emdf_protection_node.add(new DefaultMutableTreeNode(new KVP("protection_length_primary",protection_length_primary,protection_length_list.get(protection_length_primary))));
		emdf_protection_node.add(new DefaultMutableTreeNode(new KVP("protection_length_secondary",protection_length_secondary,protection_length_list.get(protection_length_secondary))));
		
		emdf_protection_node.add(new DefaultMutableTreeNode(new KVP("protection_bits_primary",protection_bits_primary,null)));
		emdf_protection_node.add(new DefaultMutableTreeNode(new KVP("protection_bits_secondary",protection_bits_secondary,null)));
		
		
		
		emdf_info_node.add(emdf_protection_node);
		return emdf_info_node;
	}

}
