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

package nl.digitalekabeltelevisie.data.mpeg.pes.video264;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.util.BitSource;

public class Sei_message implements TreeNode{

	// based on 7.3.2.3.1 Supplemental enhancement information message syntax of Rec. ITU-T H.264 (03/2010) – Prepublished version

	int payloadType;
	int last_payload_type_byte;
	int payloadSize;
	int last_payload_size_byte;
	byte[] payload;

	public Sei_message(BitSource bitSource) {

		payloadType = 0;
		while( bitSource.nextBits(8) == 0xFF ) {
			/* int ff_byte = */ bitSource.f(8);
			payloadType += 255;
		}
		last_payload_type_byte = bitSource.u(8);
		payloadType += last_payload_type_byte;

		payloadSize = 0;
		while( bitSource.nextBits( 8 ) == 0xFF) {
			/* int ff_byte= */ bitSource.f(8); /* equal to 0xFF */
			payloadSize += 255;
		}
		last_payload_size_byte= bitSource.u(8);
		payloadSize += last_payload_size_byte;

		payload= bitSource.readBytes(payloadSize);


	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(int modus) {
		final DefaultMutableTreeNode s=new DefaultMutableTreeNode(new KVP("sei_message ("+getPayloadTypeString(payloadType)+")"));
		s.add(new DefaultMutableTreeNode(new KVP("payloadType",payloadType,getPayloadTypeString(payloadType))));
		s.add(new DefaultMutableTreeNode(new KVP("payloadSize",payloadSize,null)));
		s.add(new DefaultMutableTreeNode(new KVP("sei_payload",payload,null)));
		return s;
	}

	public static String getPayloadTypeString(int payloadType){
		switch (payloadType) {
		case 0: return "buffering_period";
		case 1: return "pic_timing";
		case 2: return "pan_scan_rect";
		case 3: return "filler_payload";
		case 4: return "user_data_registered_itu_t_t35";
		case 5: return "user_data_unregistered";
		case 6: return "recovery_point";
		case 7: return "dec_ref_pic_marking_repetition";
		case 8: return "spare_pic";
		case 9: return "scene_info";
		case 10: return "sub_seq_info";
		case 11: return "sub_seq_layer_characteristics";
		case 12: return "sub_seq_characteristics";
		case 13: return "full_frame_freeze";
		case 14: return "full_frame_freeze_release";
		case 15: return "full_frame_snapshot";
		case 16: return "progressive_refinement_segment_start";
		case 17: return "progressive_refinement_segment_end";
		case 18: return "motion_constrained_slice_group_set";
		case 19: return "film_grain_characteristics";
		case 20: return "deblocking_filter_display_preference";
		case 21: return "stereo_video_info";
		case 22: return "post_filter_hint";
		case 23: return "tone_mapping_info";
		case 24: return "scalability_info";
		case 25: return "sub_pic_scalable_layer";
		case 26: return "non_required_layer_rep";
		case 27: return "priority_layer_info";
		case 28: return "layers_not_present";
		case 29: return "layer_dependency_change";
		case 30: return "scalable_nesting";
		case 31: return "base_layer_temporal_hrd";
		case 32: return "quality_layer_integrity_check";
		case 33: return "redundant_pic_property";
		case 34: return "tl0_dep_rep_index";
		case 35: return "tl_switching_point";
		case 36: return "parallel_decoding_info";
		case 37: return "mvc_scalable_nesting";
		case 38: return "view_scalability_info";
		case 39: return "multiview_scene_info";
		case 40: return "multiview_acquisition_info";
		case 41: return "non_required_view_component";
		case 42: return "view_dependency_change";
		case 43: return "operation_points_not_present";
		case 44: return "base_view_temporal_hrd";
		case 45: return "frame_packing_arrangement";



		default:
			return "reserved_sei_message";
		}

	}

	public int getPayloadType() {
		return payloadType;
	}

	public int getLast_payload_type_byte() {
		return last_payload_type_byte;
	}

	public int getPayloadSize() {
		return payloadSize;
	}

	public int getLast_payload_size_byte() {
		return last_payload_size_byte;
	}


}