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

import static nl.digitalekabeltelevisie.util.Utils.addListJTree;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.gui.utils.GuiUtils;
import nl.digitalekabeltelevisie.util.BitSource;
import nl.digitalekabeltelevisie.util.LookUpList;

/**
 * Based on ETSI TS 103 190-2 V1.2.1 (2018-02) 6.2.1.1 ac4_toc
 * @author Eric
 *
 */
public class AC4Toc implements TreeNode {
	
	private static final Logger	logger	= Logger.getLogger(AC4Toc.class.getName());
	
	LookUpList frame_rate_index_list = new LookUpList.Builder().
			add(0,"23,976 fps").
			add(1,"24 fps").
			add(2 ,"25 fps").
			add(3 ,"29,97 fps").
			add(4 ,"30 fps").
			add(5 ,"47,95 fps").
			add(6 ,"48 fps").
			add(7 ,"50 fps").
			add(8 ,"59,94 fps").
			add(9 ,"60 768 fps").
			add(10 ,"100 fps").
			add(11 ,"119,88 fps").
			add(12 ,"120 fps").
			add(13 ,"(23,44) fps").
			add(14 ,"reserved").
			add(15 ,"reserved").
			build();
	
	
	private int bitstream_version;
	private int sequence_counter;
	private int b_wait_frames;
	private int wait_frames;
	private int br_code;
	private int fs_index;
	private int frame_rate_index ;
	private int b_iframe_global;
	private int b_single_presentation;
	private int n_presentations;
	private int b_more_presentations;
	private int payload_base;
	private int b_payload_base;
	private int payload_base_minus1;
	private int b_program_id;
	private int short_program_id;
	private int b_program_uuid_present;
	private byte[] program_uuid = new byte[0];

	private List<AC4PresentationV1Info> ac4_presentation_v1_infoList = new ArrayList<>();
	private List<AC4SubstreamGroupInfo> ac4_substream_group_info_list = new ArrayList<>();


	private SubstreamIndexTable substream_index_table;


	/**
	 * @param bs
	 */
	public AC4Toc(BitSource bs) {
		bitstream_version = bs.readBits(2);
		
		if (bitstream_version == 3) {
			bitstream_version += bs.variable_bits(2);
		}
		sequence_counter = bs.readBits(10);
		b_wait_frames = bs.readBits(1);
		
		if (b_wait_frames==1) {
			wait_frames= bs.readBits(3);
			if (wait_frames > 0) {
				br_code =  bs.readBits(2);
			}
		}
		
		fs_index = bs.readBits(1);
		frame_rate_index =  bs.readBits(4);
		
		b_iframe_global = bs.readBits(1);
		b_single_presentation = bs.readBits(1);
		if (b_single_presentation==1) {
			n_presentations = 1;
		} else {
			b_more_presentations = bs.readBits(1);
			if (b_more_presentations==1) {
				n_presentations = bs.variable_bits(2) + 2;
			} else {
				n_presentations = 0;
			}
		}
		
		payload_base = 0;
		b_payload_base = bs.readBits( 1);
		if (b_payload_base==1) {
			payload_base_minus1 = bs.readBits(5);
			payload_base = payload_base_minus1 + 1;
			if (payload_base == 0x20) {
				payload_base += bs.variable_bits(3);
			}
		}
		
		if (bitstream_version <= 1) {
			logger.warning("if (bitstream_version <= 1) { not implemented");
//			for (int i = 0; i < n_presentations; i++) {
//				// ac4_presentation_info(bs);
//			}
		} else {
			b_program_id = bs.readBits(1);
			if (b_program_id == 1) {
				short_program_id = bs.readBits(16);
				b_program_uuid_present = bs.readBits(1);
				if (b_program_uuid_present == 1) {
					program_uuid = bs.readUnalignedBytes(16);
				}
			}
			for (int i = 0; i < n_presentations; i++) {
				AC4PresentationV1Info ac4_presentation_v1_info = new AC4PresentationV1Info(bs, this);
				ac4_presentation_v1_infoList.add(ac4_presentation_v1_info);
			}
			
			// TS 103 190-2 V1.2.1 (2018-02) 6.3.2.1.8 total_n_substream_groups
			int total_n_substream_groups = 1+  getMaxGroupIndex(ac4_presentation_v1_infoList);
			
			
			for (int j = 0; j < total_n_substream_groups; j++) {
				AC4SubstreamGroupInfo ac4_substream_group_info = new AC4SubstreamGroupInfo(bs, this);
				ac4_substream_group_info_list.add(ac4_substream_group_info);
			}
		}
		
		substream_index_table = new SubstreamIndexTable(bs);

	}

	/**
	 * based on TS 103 190-2 V1.2.1 (2018-02) 6.3.2.1.8
	 * @param ac4_presentation_v1_infoList2
	 * @return
	 */
	private static int getMaxGroupIndex(List<AC4PresentationV1Info> ac4_presentation_v1_infoList2) {
		
		int max = 0;
		for (AC4PresentationV1Info ac4PresentationV1Info : ac4_presentation_v1_infoList2) {
			for( AC4SgiSpecifier ac4_sgi_specifier :ac4PresentationV1Info.getAc4_sgi_specifier_list()) {
				max = Integer.max(max, ac4_sgi_specifier.getGroup_index());
			}
		}
		return max;
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(int modus) {
		DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("ac4_toc"));
		t.add(new DefaultMutableTreeNode(new KVP("bitstream_version",bitstream_version,null)));
		t.add(new DefaultMutableTreeNode(new KVP("sequence_counter",sequence_counter,null)));
		t.add(new DefaultMutableTreeNode(new KVP("b_wait_frames",b_wait_frames,null)));
		
		if (b_wait_frames==1) {
			t.add(new DefaultMutableTreeNode(new KVP("wait_frames",wait_frames,null)));
			if (wait_frames > 0) {
				t.add(new DefaultMutableTreeNode(new KVP("br_code",br_code,null)));
			}
		}
		t.add(new DefaultMutableTreeNode(new KVP("fs_index",fs_index,fs_index==0?"44,1 kHz":"48 kHz")));
		t.add(new DefaultMutableTreeNode(new KVP("frame_rate_index",frame_rate_index,frame_rate_index_list.get(frame_rate_index))));

		t.add(new DefaultMutableTreeNode(new KVP("b_iframe_global",b_iframe_global,null)));

		t.add(new DefaultMutableTreeNode(new KVP("b_single_presentation",b_single_presentation,b_single_presentation==1?"n_presentations = 1":null)));
		
		if (b_single_presentation!=1) {
			t.add(new DefaultMutableTreeNode(new KVP("b_more_presentations",b_more_presentations,b_more_presentations==0?"n_presentations = 0":null)));
			
			if (b_more_presentations==1) {
				t.add(new DefaultMutableTreeNode(new KVP("n_presentations",n_presentations,null)));
			}

		}
		
		t.add(new DefaultMutableTreeNode(new KVP("b_payload_base",b_payload_base,null)));
		if (b_payload_base==1) {
			t.add(new DefaultMutableTreeNode(new KVP("payload_base_minus1",payload_base_minus1,"payload_base="+payload_base)));
		}
		
		if (bitstream_version <= 1) {
			t.add(new DefaultMutableTreeNode(GuiUtils.getNotImplementedKVP("bitstream_version <= 1")));
//			for (int i = 0; i < n_presentations; i++) {
//				// ac4_presentation_info(bs);
//			}
		} else {
			t.add(new DefaultMutableTreeNode(new KVP("b_program_id",b_program_id,null)));
			if (b_program_id == 1) {
				t.add(new DefaultMutableTreeNode(new KVP("short_program_id",short_program_id,null)));
				t.add(new DefaultMutableTreeNode(new KVP("b_program_uuid_present",b_program_uuid_present,null)));
				if (b_program_uuid_present == 1) {
					t.add(new DefaultMutableTreeNode(new KVP("program_uuid",program_uuid,null)));
				}
			}
			addListJTree(t, ac4_presentation_v1_infoList, modus, "ac4_presentation_v1_info(s)");
			addListJTree(t, ac4_substream_group_info_list, modus, "ac4_substream_group_info(s)");
			
			t.add(substream_index_table.getJTreeNode(modus));
		}
		return t;
	}

	public int getBitstream_version() {
		return bitstream_version;
	}

	public int getFrame_rate_index() {
		return frame_rate_index;
	}

	public int getSequence_counter() {
		return sequence_counter;
	}

	public int getB_wait_frames() {
		return b_wait_frames;
	}

	public int getWait_frames() {
		return wait_frames;
	}

	public int getBr_code() {
		return br_code;
	}

	public int getFs_index() {
		return fs_index;
	}

	public int getB_iframe_global() {
		return b_iframe_global;
	}

	public int getB_single_presentation() {
		return b_single_presentation;
	}

	public int getN_presentations() {
		return n_presentations;
	}

	public int getB_more_presentations() {
		return b_more_presentations;
	}

	public int getPayload_base() {
		return payload_base;
	}

	public int getB_payload_base() {
		return b_payload_base;
	}

	public int getPayload_base_minus1() {
		return payload_base_minus1;
	}

	public int getB_program_id() {
		return b_program_id;
	}

	public int getShort_program_id() {
		return short_program_id;
	}

	public int getB_program_uuid_present() {
		return b_program_uuid_present;
	}

	public List<AC4PresentationV1Info> getAc4_presentation_v1_infoList() {
		return ac4_presentation_v1_infoList;
	}

}
