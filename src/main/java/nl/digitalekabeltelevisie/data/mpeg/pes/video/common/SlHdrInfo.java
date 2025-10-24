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

import java.util.logging.Logger;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.gui.utils.GuiUtils;
import nl.digitalekabeltelevisie.util.BitSource;

/**
 * 
 * Based on TS 103 433-1 V1.2.1 (2017-08) A.2.2.2 SL-HDR information SEI message syntax 
 * 
 * @author Eric
 *
 */
public class SlHdrInfo implements TreeNode {
	
	private static final Logger	logger	= Logger.getLogger(SlHdrInfo.class.getName());

	private int terminal_provider_oriented_code_message_idc;
	private int sl_hdr_mode_value_minus1;
	private int sl_hdr_spec_major_version_idc;
	private int sl_hdr_spec_minor_version_idc;
	private int sl_hdr_cancel_flag;
	private int sl_hdr_persistence_flag;
	private int coded_picture_info_present_flag;
	private int target_picture_info_present_flag;
	private int src_mdcv_info_present_flag;
	private int sl_hdr_extension_present_flag;
	private int sl_hdr_payload_mode;
	private int coded_picture_primaries;
	private int coded_picture_max_luminance;
	private int coded_picture_min_luminance;
	private int target_picture_primaries;
	private int target_picture_max_luminance;
	private int target_picture_min_luminance;
	private int[] src_mdcv_primaries_x = new int[3];
	private int[] src_mdcv_primaries_y = new int[3];
	private int src_mdcv_ref_white_x;
	private int src_mdcv_ref_white_y;
	private int src_mdcv_max_mastering_luminance;
	private int src_mdcv_min_mastering_luminance;
	private int[] matrix_coefficient_value = new int[4];
	private int[] chroma_to_luma_injection = new int[2];
	private int[] k_coefficient_value = new int[3];
	private int tone_mapping_input_signal_black_level_offset;
	private int tone_mapping_input_signal_white_level_offset;
	private int shadow_gain_control;
	private int highlight_gain_control;
	private int mid_tone_width_adjustment_factor;
	private int tone_mapping_output_fine_tuning_num_val;
	private int saturation_gain_num_val;
	private int[] tone_mapping_output_fine_tuning_x;
	private int[] tone_mapping_output_fine_tuning_y;
	private int[] saturation_gain_x;
	private int[] saturation_gain_y;
	private int lm_uniform_sampling_flag;
	private int luminance_mapping_num_val;
	private int[] luminance_mapping_x;
	private int[] luminance_mapping_y;
	private int cc_uniform_sampling_flag;
	private int colour_correction_num_val;
	private int[] colour_correction_x;
	private int[] colour_correction_y;
	private boolean gamutMappingParamsOrSlHdrExtensionPresent;

	/**
	 * @param payload
	 * @param i
	 * @param j
	 */
	public SlHdrInfo(byte[] payload, int offset, int j) {
		
		var bitSource = new BitSource(payload, offset);
		terminal_provider_oriented_code_message_idc = bitSource.u(8);
		sl_hdr_mode_value_minus1 = bitSource.u(4);
		sl_hdr_spec_major_version_idc = bitSource.u(4);
		sl_hdr_spec_minor_version_idc = bitSource.u(7);
		sl_hdr_cancel_flag = bitSource.u(1);
		if (sl_hdr_cancel_flag != 1) {
			sl_hdr_persistence_flag = bitSource.u(1);
			coded_picture_info_present_flag = bitSource.u(1);
			target_picture_info_present_flag = bitSource.u(1);
			src_mdcv_info_present_flag = bitSource.u(1);
			sl_hdr_extension_present_flag = bitSource.u(1);
			sl_hdr_payload_mode = bitSource.u(3);
			
			if (coded_picture_info_present_flag == 1) {
				coded_picture_primaries = bitSource.u(8);
				coded_picture_max_luminance = bitSource.u(16);
				coded_picture_min_luminance = bitSource.u(16);
			}
			if (target_picture_info_present_flag == 1) {
				target_picture_primaries = bitSource.u(8);
				target_picture_max_luminance = bitSource.u(16);
				target_picture_min_luminance = bitSource.u(16);
			}
			
			if (src_mdcv_info_present_flag == 1) {
				for (int c = 0; c < 3; c++) {
					src_mdcv_primaries_x[c] = bitSource.u(16);
					src_mdcv_primaries_y[c] = bitSource.u(16);
				}
				src_mdcv_ref_white_x = bitSource.u(16);
				src_mdcv_ref_white_y = bitSource.u(16);
				src_mdcv_max_mastering_luminance = bitSource.u(16);
				src_mdcv_min_mastering_luminance = bitSource.u(16);
			}
			
			for (int i = 0; i < 4; i++) {
				matrix_coefficient_value[i] = bitSource.u(16);
			}
			for (int i = 0; i < 2; i++) {
				chroma_to_luma_injection[i] = bitSource.u(16);
			}
			for (int i = 0; i < 3; i++) {
				k_coefficient_value[i] = bitSource.u(8);
			}
			
			if (sl_hdr_payload_mode == 0) {
				tone_mapping_input_signal_black_level_offset = bitSource.u(8);
				tone_mapping_input_signal_white_level_offset = bitSource.u(8);
				shadow_gain_control = bitSource.u(8);
				highlight_gain_control = bitSource.u(8);
				mid_tone_width_adjustment_factor = bitSource.u(8);
				tone_mapping_output_fine_tuning_num_val = bitSource.u(4);
				saturation_gain_num_val = bitSource.u(4);
				
				tone_mapping_output_fine_tuning_x = new int[tone_mapping_output_fine_tuning_num_val];
				tone_mapping_output_fine_tuning_y = new int[tone_mapping_output_fine_tuning_num_val];
				for (int i = 0; i < tone_mapping_output_fine_tuning_num_val; i++) {
					tone_mapping_output_fine_tuning_x[i] = bitSource.u(8);
					tone_mapping_output_fine_tuning_y[i] = bitSource.u(8);
				}
				saturation_gain_x = new int[saturation_gain_num_val];
				saturation_gain_y = new int[saturation_gain_num_val];
				for (int i = 0; i < saturation_gain_num_val; i++) {
					saturation_gain_x[i] = bitSource.u(8);
					saturation_gain_y[i] = bitSource.u(8);
				}
			} else if (sl_hdr_payload_mode == 1) {
				lm_uniform_sampling_flag = bitSource.u(1);
				luminance_mapping_num_val = bitSource.u(7);
				luminance_mapping_x = new int[luminance_mapping_num_val];
				luminance_mapping_y = new int[luminance_mapping_num_val];
				
				for (int i = 0; i < luminance_mapping_num_val; i++) {

					if (lm_uniform_sampling_flag == 0) {
						luminance_mapping_x[i] = bitSource.u(16);
					}
					luminance_mapping_y[i] = bitSource.u(16);
				}
				
				cc_uniform_sampling_flag = bitSource.u(1);
				colour_correction_num_val = bitSource.u(7);
				for (int i = 0; i < colour_correction_num_val; i++) {
					if (cc_uniform_sampling_flag == 0) {
						colour_correction_x[i] = bitSource.u(16);
					}
					colour_correction_y[i] = bitSource.u(16);
				}
			}
			if(bitSource.available()>0) {
				gamutMappingParamsOrSlHdrExtensionPresent = true;
				logger.warning("GamutMappingEnabledFlag or sl_hdr_extension_present_flag set, not implemented");
			}
			
			



		}
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(int modus) {
		
		KVP t = new KVP("sl_hdr_info");
		t.add(new KVP("terminal_provider_oriented_code_message_idc",terminal_provider_oriented_code_message_idc));
		t.add(new KVP("sl_hdr_mode_value_minus1",sl_hdr_mode_value_minus1));
		t.add(new KVP("sl_hdr_spec_major_version_idc",sl_hdr_spec_major_version_idc));
		t.add(new KVP("sl_hdr_spec_minor_version_idc",sl_hdr_spec_minor_version_idc));

		t.add(new KVP("sl_hdr_cancel_flag",sl_hdr_cancel_flag));

		if (sl_hdr_cancel_flag != 1) {
			t.add(new KVP("sl_hdr_persistence_flag",sl_hdr_persistence_flag));
			t.add(new KVP("coded_picture_info_present_flag",coded_picture_info_present_flag));
			t.add(new KVP("target_picture_info_present_flag",target_picture_info_present_flag));
			t.add(new KVP("src_mdcv_info_present_flag",src_mdcv_info_present_flag));
			t.add(new KVP("sl_hdr_extension_present_flag",sl_hdr_extension_present_flag));
			t.add(new KVP("sl_hdr_payload_mode",sl_hdr_payload_mode));
			
			if (coded_picture_info_present_flag == 1) {
				t.add(new KVP("coded_picture_primaries",coded_picture_primaries));
				t.add(new KVP("coded_picture_max_luminance",coded_picture_max_luminance));
				t.add(new KVP("coded_picture_min_luminance",coded_picture_min_luminance));
			}
			if (target_picture_info_present_flag == 1) {
				t.add(new KVP("target_picture_primaries",target_picture_primaries));
				t.add(new KVP("target_picture_max_luminance",target_picture_max_luminance));
				t.add(new KVP("target_picture_min_luminance",target_picture_min_luminance));
			}

			if (src_mdcv_info_present_flag == 1) {
				for (int c = 0; c < 3; c++) {
					t.add(new KVP("src_mdcv_primaries_x["+c+"]",src_mdcv_primaries_x[c]));
					t.add(new KVP("src_mdcv_primaries_y["+c+"]",src_mdcv_primaries_y[c]));
				}
				t.add(new KVP("src_mdcv_ref_white_x",src_mdcv_ref_white_x));
				t.add(new KVP("src_mdcv_ref_white_y",src_mdcv_ref_white_y));
				t.add(new KVP("src_mdcv_max_mastering_luminance",src_mdcv_max_mastering_luminance));
				t.add(new KVP("src_mdcv_min_mastering_luminance",src_mdcv_min_mastering_luminance));
			}
			for (int i = 0; i < 4; i++) {
				t.add(new KVP("matrix_coefficient_value["+i+"]",matrix_coefficient_value[i]));
			}
			for (int i = 0; i < 2; i++) {
				t.add(new KVP("chroma_to_luma_injection["+i+"]",chroma_to_luma_injection[i]));
			}
			for (int i = 0; i < 3; i++) {
				t.add(new KVP("k_coefficient_value["+i+"]",k_coefficient_value[i]));
			}

			if (sl_hdr_payload_mode == 0) {
				t.add(new KVP("tone_mapping_input_signal_black_level_offset",tone_mapping_input_signal_black_level_offset));
				t.add(new KVP("tone_mapping_input_signal_white_level_offset",tone_mapping_input_signal_white_level_offset));
				t.add(new KVP("shadow_gain_control",shadow_gain_control));
				t.add(new KVP("highlight_gain_control",highlight_gain_control));
				t.add(new KVP("mid_tone_width_adjustment_factor",mid_tone_width_adjustment_factor));
				t.add(new KVP("tone_mapping_output_fine_tuning_num_val",tone_mapping_output_fine_tuning_num_val));
				t.add(new KVP("saturation_gain_num_val",saturation_gain_num_val));
				
				for (int i = 0; i < tone_mapping_output_fine_tuning_num_val; i++) {
					t.add(new KVP("tone_mapping_output_fine_tuning_x["+i+"]",tone_mapping_output_fine_tuning_x[i]));
					t.add(new KVP("tone_mapping_output_fine_tuning_y["+i+"]",tone_mapping_output_fine_tuning_y[i]));
				}

				for (int i = 0; i < saturation_gain_num_val; i++) {
					t.add(new KVP("saturation_gain_x["+i+"]",saturation_gain_x[i]));
					t.add(new KVP("saturation_gain_y["+i+"]",saturation_gain_y[i]));
				}
			}else if (sl_hdr_payload_mode == 1) {
				t.add(new KVP("lm_uniform_sampling_flag",lm_uniform_sampling_flag));
				t.add(new KVP("luminance_mapping_num_val",luminance_mapping_num_val));
				
				for (int i = 0; i < luminance_mapping_num_val; i++) {

					if (lm_uniform_sampling_flag == 0) {
						t.add(new KVP("luminance_mapping_x["+i+"]",luminance_mapping_x[i]));
					}
					t.add(new KVP("luminance_mapping_y["+i+"]",luminance_mapping_y[i]));
				}
				
				t.add(new KVP("cc_uniform_sampling_flag",cc_uniform_sampling_flag));
				t.add(new KVP("colour_correction_num_val",colour_correction_num_val));
				for (int i = 0; i < colour_correction_num_val; i++) {
					if (cc_uniform_sampling_flag == 0) {
						t.add(new KVP("colour_correction_x["+i+"]",colour_correction_x[i]));
					}
					t.add(new KVP("colour_correction_y["+i+"]",colour_correction_y[i]));
				}
			}
			
			if(gamutMappingParamsOrSlHdrExtensionPresent) {
				t.add(GuiUtils.getNotImplementedKVP("GamutMappingEnabledFlag or sl_hdr_extension_present_flag set"));
			}

			

		}

		return t;
	}

}
