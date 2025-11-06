/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2023 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.pes.video266;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.pes.video26x.RBSP;
import nl.digitalekabeltelevisie.gui.utils.GuiUtils;
import nl.digitalekabeltelevisie.util.Utils;

/**
 * Based on Rec. ITU-T H.266 (04/2022) 7.3.2.3 Video parameter set RBSP syntax
 * @author Eric
 *
 */
public class Video_parameter_set_rbsp extends RBSP {
	
	private static final Logger logger = Logger.getLogger(Video_parameter_set_rbsp.class.getName());


	private int vps_video_parameter_set_id;
	private int vps_max_layers_minus1;
	private int vps_max_sublayers_minus1;
	// 7.4.3.3 Video parameter set RBSP semantics
	
	private int vps_default_ptl_dpb_hrd_max_tid_flag = 1;
	private int vps_all_independent_layers_flag = 1;
	private int[] vps_layer_id;
	private int[] vps_independent_layer_flag;
	private int[] vps_max_tid_ref_present_flag;
	private int[][] vps_direct_ref_layer_flag;
	private int[][] vps_max_tid_il_ref_pics_plus1;
	private int vps_each_layer_is_an_ols_flag;
	private int vps_ols_mode_idc = 2;
	private int vps_num_output_layer_sets_minus2;
	private int[][] vps_ols_output_layer_flag;
	private int vps_num_ptls_minus1;
	private int[] vps_pt_present_flag;
	private int[] vps_ptl_max_tid;
	private List<ProfileTierLevel> profile_tier_level_list = new ArrayList<>();
	private int[] vps_ols_ptl_idx;
	private int TotalNumOlss;
	private int vps_num_dpb_params_minus1;
	private int vps_sublayer_dpb_params_present_flag;
	private int[] vps_dpb_max_tid;
	private int VpsNumDpbParams;


	private int vps_extension_data_flag;

	protected Video_parameter_set_rbsp(byte[] rbsp_bytes, int numBytesInRBSP) {
		super(rbsp_bytes, numBytesInRBSP);
		vps_video_parameter_set_id = bitSource.u(4);
		vps_max_layers_minus1 = bitSource.u(6);
		vps_max_sublayers_minus1 = bitSource.u(3);
		if (vps_max_layers_minus1 > 0 && vps_max_sublayers_minus1 > 0) {
			vps_default_ptl_dpb_hrd_max_tid_flag = bitSource.u(1);
		}
		
		
		
		if (vps_max_layers_minus1 > 0) {
			vps_all_independent_layers_flag = bitSource.u(1);
		}
		
		
		vps_layer_id = new int[vps_max_layers_minus1 + 1];
		vps_independent_layer_flag = new int[vps_max_layers_minus1 + 1];
		vps_max_tid_ref_present_flag = new int[vps_max_layers_minus1 + 1];
		vps_direct_ref_layer_flag = new int[vps_max_layers_minus1 + 1][vps_max_layers_minus1 + 1];
		vps_max_tid_il_ref_pics_plus1 = new int[vps_max_layers_minus1 + 1][vps_max_layers_minus1 + 1];
		
		for (int i = 0; i <= vps_max_layers_minus1; i++) {
			vps_layer_id[i] = bitSource.u(6);
			if (i > 0 && (vps_all_independent_layers_flag == 0)) {
				vps_independent_layer_flag[i] = bitSource.u(1);
				if (vps_independent_layer_flag[i] == 0) {
					vps_max_tid_ref_present_flag[i] = bitSource.u(1);
					for (int j = 0; j < i; j++) {
						vps_direct_ref_layer_flag[i][j] = bitSource.u(1);
						if (vps_max_tid_ref_present_flag[i] != 0 && vps_direct_ref_layer_flag[i][j] != 0)
							vps_max_tid_il_ref_pics_plus1[i][j] = bitSource.u(3);
					}
				}
			}
		}
		
		// 7.4.3.3 Video parameter set RBSP semantics p.97,
		// If vps_max_layers_minus1 is equal to 0, the value of
		// vps_each_layer_is_an_ols_flag is inferred to be equal to 1.
		if (vps_max_layers_minus1 == 0) {
			vps_each_layer_is_an_ols_flag = 1;
			// Otherwise, when vps_all_independent_layers_flag is equal to 0, the value of
			// vps_each_layer_is_an_ols_flag is inferred to be equal to 0.
		} else if (vps_all_independent_layers_flag == 0) {
			vps_each_layer_is_an_ols_flag = 1;
		}
		if (vps_max_layers_minus1 > 0) {
			if (vps_all_independent_layers_flag != 0) {
				vps_each_layer_is_an_ols_flag = bitSource.u(1);
			}
			if (vps_each_layer_is_an_ols_flag == 0) {
				if (vps_all_independent_layers_flag == 0) {
					vps_ols_mode_idc = bitSource.u(2);
				}
				if (vps_ols_mode_idc == 2) {
					vps_num_output_layer_sets_minus2 = bitSource.u(8);
					vps_ols_output_layer_flag = new int[vps_num_output_layer_sets_minus2 + 2][vps_max_layers_minus1
							+ 1];
					for (int i = 1; i <= vps_num_output_layer_sets_minus2 + 1; i++) {
						for (int j = 0; j <= vps_max_layers_minus1; j++) {
							vps_ols_output_layer_flag[i][j] = bitSource.u(1);
						}
					}
				}
			}
			vps_num_ptls_minus1 = bitSource.u(8);
		}

		vps_pt_present_flag = new int[vps_num_ptls_minus1 + 1];
		vps_pt_present_flag[ 0 ] = 1;
		vps_ptl_max_tid = new int[vps_num_ptls_minus1 + 1];
		
		// 7.4.3.3 Video parameter set RBSP semantics
		//
		// vps_default_ptl_dpb_hrd_max_tid_flag equal to 1 specifies that the syntax
		// elements vps_ptl_max_tid[ i ], vps_dpb_max_tid[ i ], and vps_hrd_max_tid[ i ]
		// are not present and are inferred to be equal to the default value
		// vps_max_sublayers_minus1.
		//
		
		if(vps_default_ptl_dpb_hrd_max_tid_flag == 1) {
			Arrays.fill(vps_ptl_max_tid, vps_max_sublayers_minus1);
		}

		
		for (int i = 0; i <= vps_num_ptls_minus1; i++) {
			if (i > 0)
				vps_pt_present_flag[i] = bitSource.u(1);
			if (vps_default_ptl_dpb_hrd_max_tid_flag == 0)
				vps_ptl_max_tid[i] = bitSource.u(3);
		}

		bitSource.skiptoByteBoundary();
		ProfileTierLevel profile_tier_level;
		for (int i = 0; i <= vps_num_ptls_minus1; i++) {
			profile_tier_level = new ProfileTierLevel(vps_pt_present_flag[i], vps_ptl_max_tid[i], bitSource);
			profile_tier_level_list.add(profile_tier_level);
		}		
		
		
		// 7.4.3.3 Video parameter set RBSP semantics (30) 
		int olsModeIdc = calculateHelperOlsModeIdc();		
		
		// 7.4.3.3 Video parameter set RBSP semantics (31) 
		TotalNumOlss = calculateHelperTotalNumOlss(olsModeIdc);
		
		
		vps_ols_ptl_idx = new int[TotalNumOlss];
		
		// 7.4.3.3 Video parameter set RBSP semantics
		//
		// vps_default_ptl_dpb_hrd_max_tid_flag equal to 1 specifies that the syntax
		// elements vps_ptl_max_tid[ i ], vps_dpb_max_tid[ i ], and vps_hrd_max_tid[ i ]
		// are not present and are inferred to be equal to the default value
		// vps_max_sublayers_minus1.
		//
		
		if(vps_default_ptl_dpb_hrd_max_tid_flag == 1) {
			Arrays.fill(vps_ols_ptl_idx, vps_max_sublayers_minus1);
		}

		
		for (int i = 0; i < TotalNumOlss; i++) {
			if (vps_num_ptls_minus1 > 0 && vps_num_ptls_minus1 + 1 != TotalNumOlss) {
				vps_ols_ptl_idx[i] = bitSource.u(8);
			}
		}
		
		if(vps_each_layer_is_an_ols_flag == 0) {
			
			logger.info("if(!vps_each_layer_is_an_ols_flag) Not implemented");
		}else {
			vps_extension_data_flag	 = bitSource.u(1);
		}
//			vps_num_dpb_params_minus1
//			= bitSource.ue();
//			if( vps_max_sublayers_minus1 > 0 ) {
//			vps_sublayer_dpb_params_present_flag
//			= bitSource.u(1);}
//			
//			 //(34)
//			VpsNumDpbParams = calculateHelperVpsNumDpbParams();
//
//			for (int i = 0; i < VpsNumDpbParams; i++) {
//				if (vps_default_ptl_dpb_hrd_max_tid_flag == 0) {
//					vps_dpb_max_tid[i] = bitSource.u(3);
//				}
//			}
//			
//		}
		//	dpb_parameters( vps_dpb_max_tid[ i ], vps_sublayer_dpb_params_present_flag )
//			}
//			for(int i = 0; i < NumMultiLayerOlss; i++ ) {
//			vps_ols_dpb_pic_width[ i ]
//					= bitSource.ue();
//			vps_ols_dpb_pic_height[ i ]
//					= bitSource.ue();
//			vps_ols_dpb_chroma_format[ i ]
//					= bitSource.u(2);
//			vps_ols_dpb_bitdepth_minus8[ i ]
//					= bitSource.ue();
//			if( VpsNumDpbParams > 1 && VpsNumDpbParams != NumMultiLayerOlss )
//			vps_ols_dpb_params_idx[ i ]
//					= bitSource.ue();
//			}
//			vps_timing_hrd_params_present_flag
//			= bitSource.u(1);
//			if( vps_timing_hrd_params_present_flag != 0) {
//			general_timing_hrd_parameters( )
//			if( vps_max_sublayers_minus1 > 0 )
//			vps_sublayer_cpb_params_present_flag
//			= bitSource.u(1);
//			vps_num_ols_timing_hrd_params_minus1
//			= bitSource.ue();
//			for(int i = 0; i <= vps_num_ols_timing_hrd_params_minus1; i++ ) {
//			if( vps_default_ptl_dpb_hrd_max_tid_flag ==0)
//			vps_hrd_max_tid[ i ]
//					= bitSource.u(3);
//			firstSubLayer = vps_sublayer_cpb_params_present_flag !=0  ? 0 : vps_hrd_max_tid[ i ]
//					ols_timing_hrd_parameters( firstSubLayer, vps_hrd_max_tid[ i ] )
//					}
//					if( vps_num_ols_timing_hrd_params_minus1 > 0 && vps_num_ols_timing_hrd_params_minus1 + 1 != NumMultiLayerOlss )
//					for(int i = 0; i < NumMultiLayerOlss; i++ )
//					vps_ols_timing_hrd_idx[ i ]
//							= bitSource.ue();
//					}
//					}

	}


	private int calculateHelperVpsNumDpbParams() {
		int VpsNumDpbParams;
		
		if( vps_each_layer_is_an_ols_flag !=0) {
			VpsNumDpbParams = 0;
		} else{						
			VpsNumDpbParams = vps_num_dpb_params_minus1 + 1;
		}
		return VpsNumDpbParams;
	}


	// (31)
	private int calculateHelperTotalNumOlss(int olsModeIdc) {
		
		if (olsModeIdc == 4 || olsModeIdc == 0 || olsModeIdc == 1) {
			return vps_max_layers_minus1 + 1;
		} else if (olsModeIdc == 2) {
			return vps_num_output_layer_sets_minus2 + 2;
		}
		return 0;  // Not described
	}

	
	// (30)
	private int calculateHelperOlsModeIdc() {

		if (vps_each_layer_is_an_ols_flag == 0) {
			return vps_ols_mode_idc;
		}
		return 4;
	}

	@Override
	public KVP getJTreeNode(int modus) {
		KVP t = new KVP("video_parameter_set_rbsp");
		t.add(new KVP("vps_video_parameter_set_id",vps_video_parameter_set_id));
		t.add(new KVP("vps_max_layers_minus1",vps_max_layers_minus1));

		t.add(new KVP("vps_max_sublayers_minus1",vps_max_sublayers_minus1));
		if (vps_max_layers_minus1 > 0 && vps_max_sublayers_minus1 > 0) {
			t.add(new KVP("vps_default_ptl_dpb_hrd_max_tid_flag",vps_default_ptl_dpb_hrd_max_tid_flag));
		}
		if (vps_max_layers_minus1 > 0) {
			t.add(new KVP("vps_all_independent_layers_flag",vps_all_independent_layers_flag));
		}
		
		for (int i = 0; i <= vps_max_layers_minus1; i++) {
			KVP vps_layer_id_node = new KVP("vps_layer_id["+i+"]",vps_layer_id[i]);
			t.add(vps_layer_id_node);
			if (i > 0 && (vps_all_independent_layers_flag == 0)) {
				vps_layer_id_node.add(new KVP("vps_independent_layer_flag["+i+"]",vps_independent_layer_flag[i]));
				if (vps_independent_layer_flag[i] == 0) {
					vps_layer_id_node.add(new KVP("vps_max_tid_ref_present_flag["+i+"]",vps_max_tid_ref_present_flag[i]));
					for (int j = 0; j < i; j++) {
						vps_layer_id_node.add(new KVP("vps_direct_ref_layer_flag["+i+"]["+j+"]",vps_direct_ref_layer_flag[i][j]));
						if (vps_max_tid_ref_present_flag[i] != 0 && vps_direct_ref_layer_flag[i][j] != 0)
							vps_layer_id_node.add(new KVP("vps_max_tid_il_ref_pics_plus1["+i+"]["+j+"]",vps_max_tid_il_ref_pics_plus1[i][j]));
					}
				}
			}
		}
		if (vps_max_layers_minus1 > 0) {
			if (vps_all_independent_layers_flag != 0) {
				t.add(
						new KVP("vps_each_layer_is_an_ols_flag", vps_each_layer_is_an_ols_flag));
			}
			if (vps_each_layer_is_an_ols_flag == 0) {
				if (vps_all_independent_layers_flag == 0) {
					t.add(new KVP("vps_ols_mode_idc", vps_ols_mode_idc));
				}
				if (vps_ols_mode_idc == 2) {
					KVP vps_num_output_layer_sets_minus2_node = 
							new KVP("vps_num_output_layer_sets_minus2", vps_num_output_layer_sets_minus2);
					t.add(vps_num_output_layer_sets_minus2_node);
					for (int i = 1; i <= vps_num_output_layer_sets_minus2 + 1; i++) {
						for (int j = 0; j <= vps_max_layers_minus1; j++) {
							vps_num_output_layer_sets_minus2_node.add(
									new KVP("vps_ols_output_layer_flag[" + i + "][" + j + "]",
											vps_ols_output_layer_flag[i][j]));
						}
					}
				}
			}
			t.add(new KVP("vps_num_ptls_minus1", vps_num_ptls_minus1));
		}	
		
		for (int i = 0; i <= vps_num_ptls_minus1; i++) {
			if (i > 0)
				t.add(new KVP("vps_pt_present_flag["+i+"]",vps_pt_present_flag[i]));
			if (vps_default_ptl_dpb_hrd_max_tid_flag == 0)
				t.add(new KVP("vps_ptl_max_tid["+i+"]",vps_ptl_max_tid[i]));
		}
		Utils.addListJTree(t, profile_tier_level_list, modus, "profile_tier_level(s)");

		
		for (int i = 0; i < TotalNumOlss; i++) {
			if (vps_num_ptls_minus1 > 0 && vps_num_ptls_minus1 + 1 != TotalNumOlss) {
				t.add(new KVP("vps_ols_ptl_idx["+i+"]",vps_ols_ptl_idx[i]));
			}
		}
		
		t.add(new KVP("vps_each_layer_is_an_ols_flag", vps_each_layer_is_an_ols_flag));

		
		if(vps_each_layer_is_an_ols_flag == 0) {
			
			t.add(GuiUtils.getNotImplementedKVP("if(!vps_each_layer_is_an_ols_flag)"));
			return t; // ignore rest
		}
		t.add(new KVP("vps_extension_data_flag", vps_extension_data_flag));

		
		return t;
	}

}
