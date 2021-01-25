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
import nl.digitalekabeltelevisie.util.LookUpList;

/**
 * @author Eric
 *
 * based on TS 103 190-2 V1.2.1 (2018-02) 6.2.1.11 ac4_substream_info_obj
 */
public class AC4SubstreamInfoObj extends AC4SubstreamInfo implements TreeNode {
	
	
	LookUpList n_objects_code_list = new LookUpList.Builder().
			add(0, "b_lfe").
			add(1, "1+b_lfe").
			add(2, "2+b_lfe").
			add(3, "3+b_lfe").
			add(4, "5+b_lfe").
			add(5,7, "reserved").
			build();
	
	LookUpList bed_chan_assign_code_list = new LookUpList.Builder().
			add(0, "L, R (2.0.0)").
			add(1, "L, R, C (3.0.0)").
			add(2, "L, R, C, LFE, Ls, Rs (5.1.0)").
			add(3, "L, R, C, LFE, Ls, Rs, Tl, Tr (5.1.2)").
			add(4, "L, R, C, LFE, Ls, Rs, Tfl, Tfr, Tbl, Tbr (5.1.4)").
			add(5, "L, R, C, LFE, Ls, Rs, Lb, Rb (7.1.0)").
			add(6, "L, R, C, LFE, Ls, Rs, Lb, Rb, Tl, Tr (7.1.2)").
			add(7, "L, R, C, LFE, Ls, Rs, Lb, Rb, Tfl, Tfr, Tbl,Tbr (7.1.4)").
			build();

	private AC4Toc parentAc4Toc;

	private int n_objects_code;
	private int b_dynamic_objects;
	private int b_lfe;
	private int b_bed_objects;
	private int b_bed_start;
	private int b_ch_assign_code;
	private int bed_chan_assign_code;
	private int b_nonstd_bed_channel_assignment;
	private int nonstd_bed_channel_assignment_mask;
	private int std_bed_channel_assignment_mask;
	private int b_isf;
	private int b_isf_start;
	private int isf_config;
	private int res_bytes;
	private byte[] reserved_data;

	private int b_sf_multiplier;
	private int sf_multiplier;

	private int b_bitrate_info;
	private int bitrate_indicator;

	

	private int substream_index;

	private int sus_ver;

	private int b_substreams_present;

	/**
	 * @param bs 
	 * @param parentAc4Toc 
	 * @param b_substreams_present
	 */
	public AC4SubstreamInfoObj(BitSource bs, AC4Toc parentAc4Toc, int b_substreams_present) {
		
		this.parentAc4Toc = parentAc4Toc;
		this.b_substreams_present = b_substreams_present;
		
		n_objects_code = bs.readBits(3);
		b_dynamic_objects = bs.readBits(1);
		if (b_dynamic_objects==1) {
			b_lfe= bs.readBits(1);
		}else {
			b_bed_objects = bs.readBits(1);
			if (b_bed_objects==1) {
				b_bed_start= bs.readBits(1);
				if (b_bed_start==1) {
					b_ch_assign_code = bs.readBits(1);
					if (b_ch_assign_code==1) {
						bed_chan_assign_code = bs.readBits(3);
					} else {
						b_nonstd_bed_channel_assignment = bs.readBits(1);
						if (b_nonstd_bed_channel_assignment == 1) {
							nonstd_bed_channel_assignment_mask = bs.readBits(17);
						} else {
							std_bed_channel_assignment_mask = bs.readBits(10);
						}
					}
				}
			} else { // (b_bed_objects!=1) {
				b_isf = bs.readBits(1);
				if (b_isf == 1) {
					b_isf_start = bs.readBits(1);
					if (b_isf_start == 1) {
						isf_config = bs.readBits(3);
					}
				}	else {
					res_bytes = bs.readBits(4);
					reserved_data = bs.readUnalignedBytes(res_bytes) ;
				}
			}
		}
		if (parentAc4Toc.getFs_index() == 1) {
			b_sf_multiplier = bs.readBits(1);
			if (b_sf_multiplier == 1) {
				sf_multiplier = bs.readBits(1);
			}
		}
		b_bitrate_info = bs.readBits(1);
		if (b_bitrate_info == 1) {
			bitrate_indicator = readBitrateIndiciator(bs);
		}
		//parentAc4Toc.get
		//for (int i = 0; i <  frame_rate_factor; i++) {
		for (int i = 0; i <  1; i++) { // TODO get frame_rate_factor
			int b_audio_ndot = bs.readBits(1);
			b_audio_ndot_list.add(new BAudioNdot(b_audio_ndot));
		}

		if (b_substreams_present == 1) {
			substream_index = bs.readBits(2);
			if (substream_index == 3) {
				substream_index += bs.variable_bits(2);
			}
		}
		sus_ver = 1;
	}
		
		
	@Override
	public DefaultMutableTreeNode getJTreeNode(int modus) {
		DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("ac4_substream_info_obj"));
		t.add(new DefaultMutableTreeNode(new KVP("n_objects_code",n_objects_code,n_objects_code_list.get(n_objects_code))));
		t.add(new DefaultMutableTreeNode(new KVP("b_dynamic_objects",b_dynamic_objects,null)));
		
		if (b_dynamic_objects==1) {
			t.add(new DefaultMutableTreeNode(new KVP("b_lfe",b_lfe,null)));
		}else {
			t.add(new DefaultMutableTreeNode(new KVP("b_bed_objects",b_bed_objects,null)));
			if (b_bed_objects==1) {
				t.add(new DefaultMutableTreeNode(new KVP("b_bed_start",b_bed_start,null)));
				if (b_bed_start==1) {
					t.add(new DefaultMutableTreeNode(new KVP("b_ch_assign_code",b_ch_assign_code,null)));
					if (b_ch_assign_code==1) {
						t.add(new DefaultMutableTreeNode(new KVP("bed_chan_assign_code",bed_chan_assign_code,bed_chan_assign_code_list.get(bed_chan_assign_code))));
					} else {
						t.add(new DefaultMutableTreeNode(new KVP("b_nonstd_bed_channel_assignment",b_nonstd_bed_channel_assignment,null)));
						if (b_nonstd_bed_channel_assignment == 1) {
							t.add(new DefaultMutableTreeNode(new KVP("nonstd_bed_channel_assignment_mask",nonstd_bed_channel_assignment_mask,null)));
						} else {
							t.add(new DefaultMutableTreeNode(new KVP("std_bed_channel_assignment_mask",std_bed_channel_assignment_mask,null)));
						}
					}
				}
			} else { // (b_bed_objects!=1) {
				t.add(new DefaultMutableTreeNode(new KVP("b_isf",b_isf,null)));
				if (b_isf == 1) {
					t.add(new DefaultMutableTreeNode(new KVP("b_isf_start",b_isf_start,null)));
					if (b_isf_start == 1) {
						t.add(new DefaultMutableTreeNode(new KVP("isf_config",isf_config,null)));
					}
				}	else {
					t.add(new DefaultMutableTreeNode(new KVP("res_bytes",res_bytes,null)));
					t.add(new DefaultMutableTreeNode(new KVP("reserved_data",reserved_data,null)));
				}
			} // if (b_bed_objects==1)
		}
		
		if (parentAc4Toc.getFs_index() == 1) {
			t.add(new DefaultMutableTreeNode(new KVP("b_sf_multiplier",b_sf_multiplier,null)));
			if (b_sf_multiplier == 1) {
				t.add(new DefaultMutableTreeNode(new KVP("sf_multiplier",sf_multiplier,null)));
			}
		}
		t.add(new DefaultMutableTreeNode(new KVP("b_bitrate_info",b_bitrate_info,null)));
		if (b_bitrate_info == 1) {
			t.add(new DefaultMutableTreeNode(new KVP("bitrate_indicator",bitrate_indicator,null)));
		}

		addListJTree(t, b_audio_ndot_list, modus, "b_audio_ndots");
		
		if (b_substreams_present == 1) {
			t.add(new DefaultMutableTreeNode(new KVP("substream_index",substream_index,null)));
		}
		return t;
	}

}
