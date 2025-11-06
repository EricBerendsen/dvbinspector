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
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.gui.utils.GuiUtils;
import nl.digitalekabeltelevisie.util.BitSource;
import nl.digitalekabeltelevisie.util.LookUpList;

/**
 * Based on ETSI TS 103 190-2 V1.2.1 (2018-02) ch. 6.2.1.3 ac4_presentation_v1_info
 * @author Eric
 *
 */
public class AC4PresentationV1Info implements TreeNode {
	
	private static final Logger	logger	= Logger.getLogger(AC4PresentationV1Info.class.getName());
	
	LookUpList presentation_config_list = new LookUpList.Builder().
		add(0 ,"music and effects + dialogue").
		add(1 ,"Main + dialogue enhancement").
		add(2 ,"Main + associate").
		add(3 ,"music and effects + dialogue + associate").
		add(4 ,"Main + dialogue enhancement + associate").
		add(5 ,"Arbitrary substream groups").
		add(6 ,"EMDF only").
		build();

	
	
	private AC4Toc parentAc4Toc;
	private int b_single_substream_group;
	private int presentation_config;
	private int presentation_version;
	private int b_add_emdf_substreams;
	private int mdcompat;
	private int b_presentation_id;
	private int presentation_id;
	private int multiplier_bit;
	private int b_multiplier ;
	private int frame_rate_fraction;
	private int frame_rate_factor;
	private int b_frame_rate_fraction;
	private int b_frame_rate_fraction_is_4;
	private int b_presentation_filter ;
	private int b_enable_presentation;
	
	private List<AC4SgiSpecifier> ac4_sgi_specifier_list = new ArrayList<>();
	private int n_substream_groups;

	private int b_multi_pid;
	private int b_pre_virtualized;

	private EmdfInfo emdf_info;
	private AC4PresentationSubstreamInfo ac4_presentation_substream_info;
	private int n_add_emdf_substreams;
	private List<EmdfInfo> substreamsEmdfInfo = new ArrayList<>();
	private int n_substream_groups_minus2;

	/**
	 * @param bs
	 */
	public AC4PresentationV1Info(BitSource bs, AC4Toc parent) {
		parentAc4Toc = parent;
		b_single_substream_group = bs.readBits(1);
		if (b_single_substream_group != 1) {
			presentation_config= bs.readBits(3);
			if (presentation_config == 7) {
				presentation_config += bs.variable_bits(2);
			}
		}
		if (parentAc4Toc.getBitstream_version() != 1) {
			presentation_version = getPresentationVersion(bs);
		}
		if (b_single_substream_group != 1 && presentation_config == 6) {
			b_add_emdf_substreams = 1; // implied
		} else {
			if (parentAc4Toc.getBitstream_version() != 1) {
				mdcompat = bs.readBits(3);
			}
			b_presentation_id = bs.readBits(1);
			if (b_presentation_id ==1) {
				presentation_id = bs.variable_bits(2);
			}
			frame_rate_multiply_info(bs,parentAc4Toc.getFrame_rate_index());
			frame_rate_fractions_info(bs,parentAc4Toc.getFrame_rate_index());
			emdf_info = new EmdfInfo(bs);
			b_presentation_filter = bs.readBits(1);
			if (b_presentation_filter == 1) {
				b_enable_presentation= bs.readBits(1);
			}
		
			if (b_single_substream_group == 1) {
				ac4_sgi_specifier_list.add(new AC4SgiSpecifier(bs,parentAc4Toc.getBitstream_version()));
				n_substream_groups = 1;
			}else { 
		
				b_multi_pid = bs.readBits(1);
				switch (presentation_config) {
					case 0:
					/* Music and Effects + Dialogue */
						ac4_sgi_specifier_list.add(new AC4SgiSpecifier(bs,parentAc4Toc.getBitstream_version()));
						ac4_sgi_specifier_list.add(new AC4SgiSpecifier(bs,parentAc4Toc.getBitstream_version()));
						n_substream_groups = 2;
					break;
					case 1:
					/* Main + DE */
						ac4_sgi_specifier_list.add(new AC4SgiSpecifier(bs,parentAc4Toc.getBitstream_version()));
						ac4_sgi_specifier_list.add(new AC4SgiSpecifier(bs,parentAc4Toc.getBitstream_version()));
						n_substream_groups = 1;
					break;
					case 2:
					/* Main + Associated Audio */
						ac4_sgi_specifier_list.add(new AC4SgiSpecifier(bs,parentAc4Toc.getBitstream_version()));
						ac4_sgi_specifier_list.add(new AC4SgiSpecifier(bs,parentAc4Toc.getBitstream_version()));
						n_substream_groups = 2;
					break;
					case 3:
						/* Music and Effects + Dialogue + Associated Audio */
						ac4_sgi_specifier_list.add(new AC4SgiSpecifier(bs,parentAc4Toc.getBitstream_version()));
						ac4_sgi_specifier_list.add(new AC4SgiSpecifier(bs,parentAc4Toc.getBitstream_version()));
						ac4_sgi_specifier_list.add(new AC4SgiSpecifier(bs,parentAc4Toc.getBitstream_version()));
						n_substream_groups = 3;
					break;
					case 4:
						/* Main + DE + Associated Audio */
						ac4_sgi_specifier_list.add(new AC4SgiSpecifier(bs,parentAc4Toc.getBitstream_version()));
						ac4_sgi_specifier_list.add(new AC4SgiSpecifier(bs,parentAc4Toc.getBitstream_version()));
						ac4_sgi_specifier_list.add(new AC4SgiSpecifier(bs,parentAc4Toc.getBitstream_version()));
						n_substream_groups = 2;
					break;
					case 5:
						/* Arbitrary number of roles and substream groups */
						n_substream_groups_minus2=bs.readBits(2);
						n_substream_groups = n_substream_groups_minus2 + 2;
						if (n_substream_groups == 5) {
							n_substream_groups += bs.variable_bits(2);
						}
						for (int sg = 0; sg < n_substream_groups; sg++) {
							ac4_sgi_specifier_list.add(new AC4SgiSpecifier(bs,parentAc4Toc.getBitstream_version()));
						}
					break;
					default:
						/* EMDF and other data */
						//presentation_config_ext_info();
						logger.warning("presentation_config_ext_info not implemented");
						break;
				}
			}
			b_pre_virtualized= bs.readBits(1);
			b_add_emdf_substreams= bs.readBits(1);
			ac4_presentation_substream_info = new AC4PresentationSubstreamInfo(bs);
		}
		if (b_add_emdf_substreams==1) {
			n_add_emdf_substreams= bs.readBits(2);
			if (n_add_emdf_substreams == 0) {
				n_add_emdf_substreams = bs.variable_bits(2) + 4;
			}
			for (int i = 0; i < n_add_emdf_substreams; i++) {
				EmdfInfo sub_emdf_info = new EmdfInfo(bs);
				substreamsEmdfInfo .add(sub_emdf_info);
			}
		}
		
			
	}



	/**
	 * @param frame_rate_index 
	 * @param bs 
	 * 
	 */
	private void frame_rate_fractions_info(BitSource bs, int frame_rate_index) {
		frame_rate_fraction = 1;
		if(Arrays.asList(5, 6, 7, 8, 9).contains(frame_rate_index)) { // if (frame_rate_index in [5, 6, 7, 8, 9])
			if (frame_rate_factor == 1) {
				b_frame_rate_fraction= bs.readBits(1);
				if (b_frame_rate_fraction == 1) {
					frame_rate_fraction = 2;
				}
			}
		}
		if(Arrays.asList(10, 11, 12).contains(frame_rate_index)) { //if (frame_rate_index in [10, 11, 12]) {
			b_frame_rate_fraction= bs.readBits(1);
			if (b_frame_rate_fraction == 1) {
				b_frame_rate_fraction_is_4 = bs.readBits(1);
				if (b_frame_rate_fraction_is_4 == 1) {
					frame_rate_fraction = 4;
				} else {
					frame_rate_fraction = 2;
				}
			}
		}
	}

	/**
	 * @param frame_rate_index
	 * @return
	 */
	private void frame_rate_multiply_info(BitSource bs,int frame_rate_index) {
		// default
		frame_rate_factor = 1; // TS 103 190-1 V1.3.1 (2018-02) 4.3.3.5.3 frame_rate_factor - frame rate factor

		switch (frame_rate_index) {
		case 2:
		case 3:
		case 4:
		if ((b_multiplier = bs.readBits(1))==1) {
			multiplier_bit = bs.readBits(1);
			if(multiplier_bit==0) {
				frame_rate_factor = 2; 
			}else {
				frame_rate_factor = 4;
			}
		}
		break;
		case 0:
		case 1:
		case 7:
		case 8:
		case 9:
			b_multiplier = bs.readBits(1);
			// TS 103 190-1 V1.3.1 (2018-02) 4.3.3.5.3 frame_rate_factor - frame rate factor
			if(b_multiplier==1) {
				frame_rate_factor = 2; 
			}
		break;
		default:
		break;
		}
	}

	/**
	 * Based on ETSI TS 103 190-1 V1.3.1 (2018-02) ch 4.2.3.3 presentation_version - Presentation version information
	 * @param bs
	 * @return
	 */
	private static int getPresentationVersion(BitSource bs) {
		int val = 0;
		@SuppressWarnings("unused")
		int b_tmp; 
		while ((b_tmp = bs.readBits(1)) == 1) {
			val++;
		}
		return val;
	}

	@Override
	public KVP getJTreeNode(int modus) {
		KVP t = new KVP("ac4_presentation_v1_info");
		t.add(new KVP("b_single_substream_group",b_single_substream_group));
		if (b_single_substream_group != 1) {
			t.add(new KVP("presentation_config",presentation_config));
		}
		if (parentAc4Toc.getBitstream_version() != 1) {
			t.add(new KVP("presentation_version",presentation_version));
		}
		
		if (b_single_substream_group != 1 && presentation_config == 6) {
			t.add(new KVP("b_add_emdf_substreams",b_add_emdf_substreams,"(implied)"));
		} else {
			if (parentAc4Toc.getBitstream_version() != 1) {
				t.add(new KVP("mdcompat",mdcompat));
			}
			t.add(new KVP("b_presentation_id",b_presentation_id));
			if (b_presentation_id ==1) {
				t.add(new KVP("presentation_id",presentation_id));
			}
			
			t.add(getFrameRateMultiplyInfoNode());
			t.add(getFrameRateFractionsInfoNode());
			t.add(emdf_info.getJTreeNode(modus));
			
			t.add(new KVP("b_presentation_filter",b_presentation_filter));
			
			if (b_presentation_filter == 1) {
				t.add(new KVP("b_enable_presentation",b_enable_presentation));
			}
			if (b_single_substream_group == 1) {
				t.add(ac4_sgi_specifier_list.get(0).getJTreeNode(modus) );
				t.add(new KVP("n_substream_groups",n_substream_groups));
			}else {
				t.add(new KVP("b_multi_pid",b_multi_pid));
				t.add(new KVP("presentation_config",presentation_config,presentation_config_list.get(presentation_config, "Reserved")));
				if(presentation_config==5) {
					t.add(new KVP("n_substream_groups_minus2",n_substream_groups_minus2));
				}
				
				addListJTree(t, ac4_sgi_specifier_list, modus, "ac4_sgi_specifier(s)");
				t.add(new KVP("n_substream_groups",n_substream_groups));
				if(presentation_config>5) {
					t.add(GuiUtils.getNotImplementedKVP("presentation_config>5, /* EMDF and other data */, presentation_config_ext_info()"));
					return t;
				}
			}
			t.add(new KVP("b_pre_virtualized",b_pre_virtualized));
			t.add(new KVP("b_add_emdf_substreams",b_add_emdf_substreams));
			t.add(ac4_presentation_substream_info.getJTreeNode(modus));
			
		}
		if (b_add_emdf_substreams==1) {
			t.add(new KVP("n_add_emdf_substreams",n_add_emdf_substreams));
			addListJTree(t, substreamsEmdfInfo, modus, "emdf_info(s)");
		}

		return t;
	}

	private KVP getFrameRateFractionsInfoNode() {
		KVP frame_rate_fractions_info_node = new KVP("frame_rate_fractions_info (frame_rate_fraction)",frame_rate_fraction);
		
		if(Arrays.asList(5, 6, 7, 8, 9).contains(parentAc4Toc.getFrame_rate_index())) { // if (frame_rate_index in [5, 6, 7, 8, 9])
			if (frame_rate_factor == 1) {
				frame_rate_fractions_info_node.add(new KVP("b_frame_rate_fraction",b_frame_rate_fraction));
			}
		}
		if(Arrays.asList(10, 11, 12).contains(parentAc4Toc.getFrame_rate_index())) { //if (frame_rate_index in [10, 11, 12]) {
			frame_rate_fractions_info_node.add(new KVP("b_frame_rate_fraction",b_frame_rate_fraction));
			if (b_frame_rate_fraction == 1) {
				frame_rate_fractions_info_node.add(new KVP("b_frame_rate_fraction_is_4",b_frame_rate_fraction_is_4));
			}
		}
		return frame_rate_fractions_info_node;
	}

	private KVP getFrameRateMultiplyInfoNode() {
		KVP frame_rate_multiply_info_node = new KVP("frame_rate_multiply_info (frame_rate_factor)",frame_rate_factor);
		switch (parentAc4Toc.getFrame_rate_index()) {
		case 2:
		case 3:
		case 4:
			frame_rate_multiply_info_node.add(new KVP("b_multiplier",b_multiplier));
			if (b_multiplier==1) {
				frame_rate_multiply_info_node.add(new KVP("multiplier_bit",multiplier_bit));
			}
		break;
		case 0:
		case 1:
		case 7:
		case 8:
		case 9:
			frame_rate_multiply_info_node.add(new KVP("b_multiplier",b_multiplier));
		break;
		default:
		break;
		}
		return frame_rate_multiply_info_node;
	}


	public List<AC4SgiSpecifier> getAc4_sgi_specifier_list() {
		return ac4_sgi_specifier_list;
	}

}
