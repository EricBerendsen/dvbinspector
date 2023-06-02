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

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.util.BitSource;
import nl.digitalekabeltelevisie.util.LookUpList;

/**
 * @author Eric
 *
 * Based on Rec. ITU-T H.266 (04/2022) 7.3.3.1 General profile, tier, and level syntax
 *
 */
public class ProfileTierLevel implements TreeNode {
	
	public static final LookUpList general_profile_idc_list = new LookUpList.Builder()
			.add(1, "Main 10 profile")
			.add(2, "Main 12 profile")
			.add(10, "Main 12 Intra profile")
			.add(17, "Multilayer Main 10 pr")
			.add(33, "Main 10 4:4:4 profile")
			.add(34, "Main 12 4:4:4 profile")
			.add(35, "Main 16 4:4:4 profile")
			.add(42, "Main 12 4:4:4 Intra profile")
			.add(43, "Main 16 4:4:4 Intra profile")
			.add(49, "Multilayer Main 10 4:4:4 profile")
			.add(65, "Main 10 Still Picture profile")
			.add(66, "Main 12 Still Picture profile")
			.add(97, "Main 10 4:4:4 Still Picture profile")
			.add(98, "Main 12 4:4:4 Still Picture profile")
			.add(99, "Main 16 4:4:4 Still Picture profile")
			.build();

	final int profileTierPresentFlag;

	private final int max_num_sub_layers_minus1;

	private int general_profile_idc;
	private int general_tier_flag;

	private int general_level_idc;

	private int ptl_frame_only_constraint_flag;

	private int ptl_multilayer_enabled_flag;

	private GeneralConstraintsInfo general_constraints_info;

	private int[] ptl_sublayer_level_present_flag;

	private int[] sublayer_level_idc;

	private int ptl_num_sub_profiles ;

	private int[] general_sub_profile_idc;


	public ProfileTierLevel(final int profileTierPresentFlag, final int max_num_sub_layers_minus1, final BitSource bitSource) {

		this.profileTierPresentFlag = profileTierPresentFlag;
		this.max_num_sub_layers_minus1 = max_num_sub_layers_minus1;


		ptl_sublayer_level_present_flag = new int[max_num_sub_layers_minus1];
		sublayer_level_idc = new int[max_num_sub_layers_minus1];

		if(profileTierPresentFlag==1)
		{
			general_profile_idc = bitSource.u(7);
			general_tier_flag = bitSource.u(1);
		}

		general_level_idc = bitSource.u(8);
		ptl_frame_only_constraint_flag = bitSource.u(1);
		ptl_multilayer_enabled_flag = bitSource.u(1);
		if( profileTierPresentFlag ==1) {
			general_constraints_info = new GeneralConstraintsInfo(bitSource);
		}

		for(int i = max_num_sub_layers_minus1 - 1; i >= 0; i-- ) {
			ptl_sublayer_level_present_flag[ i ] = bitSource.u(1);
		}
		bitSource.skiptoByteBoundary();

		for (int i = max_num_sub_layers_minus1 - 1; i >= 0; i--) {
			if (ptl_sublayer_level_present_flag[i] == 1) {
				sublayer_level_idc[i] = bitSource.u(8);
			}
		}

		if (profileTierPresentFlag == 1) {
			ptl_num_sub_profiles = bitSource.u(8);
			general_sub_profile_idc = new int[ptl_num_sub_profiles];
			for (int i = 0; i < ptl_num_sub_profiles; i++) {
				general_sub_profile_idc[i] = bitSource.u(32);
			}
		}

	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("Profile_tier_level(profileTierPresentFlag="+profileTierPresentFlag+", max_num_sub_layers_minus1="+max_num_sub_layers_minus1+")"));
		if(profileTierPresentFlag==1)
		{
			t.add(new DefaultMutableTreeNode(new KVP("general_profile_idc",general_profile_idc,general_profile_idc_list.get(general_profile_idc, "unknown/reserved"))));
			t.add(new DefaultMutableTreeNode(new KVP("general_tier_flag",general_tier_flag,null)));
		}
		t.add(new DefaultMutableTreeNode(new KVP("general_level_idc",general_level_idc,null)));
		t.add(new DefaultMutableTreeNode(new KVP("ptl_frame_only_constraint_flag",ptl_frame_only_constraint_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("ptl_multilayer_enabled_flag",ptl_multilayer_enabled_flag,null)));

		if( profileTierPresentFlag ==1) {
			t.add(general_constraints_info.getJTreeNode(modus));
		}
		
		for(int i = max_num_sub_layers_minus1 - 1; i >= 0; i-- ) {
			t.add(new DefaultMutableTreeNode(new KVP("ptl_sublayer_level_present_flag["+i+"]",ptl_sublayer_level_present_flag[ i ] ,null)));
		}

		for (int i = max_num_sub_layers_minus1 - 1; i >= 0; i--) {
			if (ptl_sublayer_level_present_flag[i] == 1) {
				t.add(new DefaultMutableTreeNode(new KVP("sublayer_level_idc["+i+"]",sublayer_level_idc[ i ] ,null)));
			}
		}

		if (profileTierPresentFlag == 1) {
			t.add(new DefaultMutableTreeNode(new KVP("ptl_num_sub_profiles",ptl_num_sub_profiles,null)));
			for (int i = 0; i < ptl_num_sub_profiles; i++) {
				t.add(new DefaultMutableTreeNode(new KVP("general_sub_profile_idc["+i+"]",general_sub_profile_idc[ i ] ,null)));
			}
		}



		return t;
	}

}
