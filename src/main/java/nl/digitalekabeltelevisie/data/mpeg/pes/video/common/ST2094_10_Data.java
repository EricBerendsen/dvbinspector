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

package nl.digitalekabeltelevisie.data.mpeg.pes.video.common;

import static nl.digitalekabeltelevisie.util.Utils.addListJTree;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.util.BitSource;
import nl.digitalekabeltelevisie.util.LookUpList;

/**
 * 
 * based on ch. 4.2 TS 103 572 V1.1.1 (2018-03) 
 * @author Eric
 *
 */
public class ST2094_10_Data implements TreeNode {
	
	LookUpList ext_block_level_list = new LookUpList.Builder().
			add(0, "Reserved").
			add(1, "Level 1 Metadata - Content Range").
			add(2, "Level 2 Metadata - Trim Pass ").
			add(3, "Reserved").
			add(4, "Reserved").
			add(5, "Level 5 Metadata - Active Area").
			add(6,255, "Reserved ").
			build();
	
	private class ExtDmDataBlock implements TreeNode{

		private int ext_block_length;
		private int ext_block_level;
		private int min_PQ;
		private int max_PQ;
		private int avg_PQ;
		/**
		 * @param bitSource
		 */
		private int target_max_PQ;
		private int trim_slope;
		private int trim_offset;
		private int trim_power;
		private int trim_chroma_weight;
		private int trim_saturation_gain;
		private int ms_weight;
		private int active_area_left_offset;
		private int active_area_right_offset;
		private int active_area_top_offset;
		private int active_area_bottom_offset;
		public ExtDmDataBlock(BitSource bitSource) {
			ext_block_length = bitSource.ue();
			ext_block_level = bitSource.u(8);
			
			int ext_block_len_bits = 8 * ext_block_length;
			int ext_block_use_bits = 0;
			
			if( ext_block_level == 1 ) {
				min_PQ = bitSource.u(12);
				max_PQ = bitSource.u(12);
				avg_PQ = bitSource.u(12);
				ext_block_use_bits += 36;
			} 
			
			if( ext_block_level == 2 ) {
				target_max_PQ = bitSource.u(12);
				trim_slope = bitSource.u(12);
				trim_offset = bitSource.u(12);
				trim_power = bitSource.u(12);
				trim_chroma_weight = bitSource.u(12);
				trim_saturation_gain = bitSource.u(12);
				ms_weight = bitSource.i(13);
				ext_block_use_bits += 85;
			}
			
			if( ext_block_level == 5 ) {
				active_area_left_offset = bitSource.u(13);
				active_area_right_offset = bitSource.u(13);
				active_area_top_offset = bitSource.u(13);
				active_area_bottom_offset = bitSource.u(13);
				ext_block_use_bits += 52;
			} 
			
			
			while( ext_block_use_bits++ < ext_block_len_bits ) {
				@SuppressWarnings("unused")
				int ext_dm_alignment_zero_bit = bitSource.f(1);
			}
			// bitSource.skiptoByteBoundary();
			
			
			
		}

		@Override
		public DefaultMutableTreeNode getJTreeNode(int modus) {
			final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("ext_dm_data_block"));
			t.add(new DefaultMutableTreeNode(new KVP("ext_block_length",ext_block_length,null)));
			t.add(new DefaultMutableTreeNode(new KVP("ext_block_level",ext_block_level,ext_block_level_list.get(ext_block_level))));

			if( ext_block_level == 1 ) {
				t.add(new DefaultMutableTreeNode(new KVP("min_PQ",min_PQ,"minimum luminance value of current picture in 12-bit PQ encoding")));
				t.add(new DefaultMutableTreeNode(new KVP("max_PQ",max_PQ,"maximum luminance value of current picture in 12-bit PQ encoding")));
				t.add(new DefaultMutableTreeNode(new KVP("avg_PQ",avg_PQ,"midpoint luminance value of current picture in 12-bit PQ encoding")));
			} 

			if( ext_block_level == 2 ) {
				t.add(new DefaultMutableTreeNode(new KVP("target_max_PQ",target_max_PQ,"maximum luminance value of a target display in 12-bit PQ encoding")));
				t.add(new DefaultMutableTreeNode(new KVP("trim_slope",trim_slope,"slope metadata")));
				t.add(new DefaultMutableTreeNode(new KVP("trim_offset",trim_offset,"offset metadata")));
				t.add(new DefaultMutableTreeNode(new KVP("trim_power",trim_power,"power metadata")));
				t.add(new DefaultMutableTreeNode(new KVP("trim_chroma_weight",trim_chroma_weight," chroma weight metadata")));
				t.add(new DefaultMutableTreeNode(new KVP("trim_saturation_gain",trim_saturation_gain,"saturation gain metadata")));
				t.add(new DefaultMutableTreeNode(new KVP("ms_weight",ms_weight,"reserved for future specification")));
			}
			
			if( ext_block_level == 5 ) {
				t.add(new DefaultMutableTreeNode(new KVP("active_area_left_offset",active_area_left_offset,null)));
				t.add(new DefaultMutableTreeNode(new KVP("active_area_right_offset",active_area_right_offset,null)));
				t.add(new DefaultMutableTreeNode(new KVP("active_area_top_offset",active_area_top_offset,null)));
				t.add(new DefaultMutableTreeNode(new KVP("active_area_bottom_offset",active_area_bottom_offset,null)));
			} 
			
			return t;
		}
		
	}

	private int app_identifier;
	private int app_version;
	private int metadata_refresh_flag;
	
	private int num_ext_blocks;
	
	private List<ExtDmDataBlock> ext_dm_data_blockList = new ArrayList<>();
	
	public ST2094_10_Data(byte[] data, final int offset, final int len){
		var bitSource = new BitSource(data, offset);
		
		app_identifier = bitSource.ue();
		app_version = bitSource.ue();
		metadata_refresh_flag = bitSource.u(1);
		
		if(metadata_refresh_flag == 1) {
			num_ext_blocks = bitSource.ue();
			bitSource.skiptoByteBoundary();
			
			for (int i = 0; i < num_ext_blocks; i++) {
				ExtDmDataBlock ext_dm_data_block = new ExtDmDataBlock(bitSource);
				ext_dm_data_blockList.add(ext_dm_data_block);
			}
		}
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(int modus) {
		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("ST2094-10_data()"));
		t.add(new DefaultMutableTreeNode(new KVP("app_identifier",app_identifier,null)));
		t.add(new DefaultMutableTreeNode(new KVP("app_version",app_version,null)));
		t.add(new DefaultMutableTreeNode(new KVP("metadata_refresh_flag",metadata_refresh_flag,null)));
		if(metadata_refresh_flag == 1) {
			t.add(new DefaultMutableTreeNode(new KVP("num_ext_blocks",num_ext_blocks,null)));
			addListJTree(t,ext_dm_data_blockList,modus,"CC ext_dm_data_block_list");
		}
		return t;
	}

}
