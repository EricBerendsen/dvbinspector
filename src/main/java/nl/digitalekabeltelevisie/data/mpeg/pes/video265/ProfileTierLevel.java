/**
 * 
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 * 
 *  This code is Copyright 2009-2015 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.pes.video265;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.*;
import nl.digitalekabeltelevisie.util.BitSource;

/**
 * Based on 7.3.3 Profile, tier and level syntax Rec. ITU-T H.265 v2 (10/2014)
 * @author Eric
 *
 */
public class ProfileTierLevel implements TreeNode{
	final int profilePresentFlag;

	private final int max_num_sub_layers_minus1;

	private int general_profile_space;
	private int general_tier_flag;
	private int general_profile_idc;
	private final int []general_profile_compatibility_flag = new int[32];

	private int general_progressive_source_flag;
	private int general_interlaced_source_flag;
	private int general_non_packed_constraint_flag;
	private int general_frame_only_constraint_flag;

	private int general_max_12bit_constraint_flag;
	private int general_max_10bit_constraint_flag;
	private int general_max_8bit_constraint_flag;
	private int general_max_422chroma_constraint_flag;
	private int general_max_420chroma_constraint_flag;
	private int general_max_monochrome_constraint_flag;
	private int general_intra_constraint_flag;
	private int general_one_picture_only_constraint_flag;
	private int general_lower_bit_rate_constraint_flag;
	private long general_reserved_zero_34bits;

	private long general_reserved_zero_43bits;

	private int general_inbld_flag;
	private int general_reserved_zero_bit;

	private final int general_level_idc;

	private final int[] sub_layer_profile_present_flag;
	private final int[] sub_layer_level_present_flag;

	private final int[] reserved_zero_2bits;

	private final int[] sub_layer_profile_space;
	private final int[] sub_layer_tier_flag;
	private final int[] sub_layer_profile_idc;

	private final int[] []sub_layer_profile_compatibility_flag;
	private final int[] sub_layer_progressive_source_flag;
	private final int[] sub_layer_interlaced_source_flag;
	private final int[] sub_layer_non_packed_constraint_flag;
	private final int[] sub_layer_frame_only_constraint_flag;


	private final int[]sub_layer_max_12bit_constraint_flag;
	private final int[]sub_layer_max_10bit_constraint_flag;
	private final int[]sub_layer_max_8bit_constraint_flag;
	private final int[]sub_layer_max_422chroma_constraint_flag;
	private final int[]sub_layer_max_420chroma_constraint_flag;
	private final int[]sub_layer_max_monochrome_constraint_flag;
	private final int[]sub_layer_intra_constraint_flag;
	private final int[]sub_layer_one_picture_only_constraint_flag;
	private final int[]sub_layer_lower_bit_rate_constraint_flag;
	private final long[]sub_layer_reserved_zero_34bits;
	private final long[]sub_layer_reserved_zero_43bits;

	private final int[]sub_layer_inbld_flag;
	private final int[]sub_layer_reserved_zero_bit;

	private final int[] sub_layer_level_idc;


	/**
	 * @param i
	 * @param sps_max_sub_layers_minus1
	 * @param bitSource
	 */
	public ProfileTierLevel(final int profilePresentFlag, final int max_num_sub_layers_minus1, final BitSource bitSource) {

		this.profilePresentFlag= profilePresentFlag;
		this.max_num_sub_layers_minus1 = max_num_sub_layers_minus1;
		reserved_zero_2bits = new int[8];

		sub_layer_profile_space = new int[max_num_sub_layers_minus1];
		sub_layer_tier_flag = new int[max_num_sub_layers_minus1];
		sub_layer_profile_idc = new int[max_num_sub_layers_minus1];

		sub_layer_profile_compatibility_flag = new int[max_num_sub_layers_minus1][32];
		sub_layer_progressive_source_flag = new int[max_num_sub_layers_minus1];
		sub_layer_interlaced_source_flag = new int[max_num_sub_layers_minus1];
		sub_layer_non_packed_constraint_flag = new int[max_num_sub_layers_minus1];
		sub_layer_frame_only_constraint_flag = new int[max_num_sub_layers_minus1];

		sub_layer_max_12bit_constraint_flag = new int[max_num_sub_layers_minus1];
		sub_layer_max_10bit_constraint_flag = new int[max_num_sub_layers_minus1];
		sub_layer_max_8bit_constraint_flag = new int[max_num_sub_layers_minus1];
		sub_layer_max_422chroma_constraint_flag = new int[max_num_sub_layers_minus1];
		sub_layer_max_420chroma_constraint_flag = new int[max_num_sub_layers_minus1];
		sub_layer_max_monochrome_constraint_flag = new int[max_num_sub_layers_minus1];
		sub_layer_intra_constraint_flag = new int[max_num_sub_layers_minus1];
		sub_layer_one_picture_only_constraint_flag = new int[max_num_sub_layers_minus1];
		sub_layer_lower_bit_rate_constraint_flag = new int[max_num_sub_layers_minus1];
		sub_layer_reserved_zero_34bits = new long[max_num_sub_layers_minus1];
		sub_layer_reserved_zero_43bits = new long[max_num_sub_layers_minus1];

		sub_layer_inbld_flag = new int[max_num_sub_layers_minus1];
		sub_layer_reserved_zero_bit = new int[max_num_sub_layers_minus1];

		sub_layer_level_idc = new int[max_num_sub_layers_minus1];

		if(profilePresentFlag==1)
		{
			general_profile_space = bitSource.u(2);
			general_tier_flag = bitSource.u(1);
			general_profile_idc = bitSource.u(5);
			for (int j = 0; j < 32; j++) {
				general_profile_compatibility_flag[j] =  bitSource.u(1);

			}
			general_progressive_source_flag = bitSource.u(1);
			general_interlaced_source_flag = bitSource.u(1);
			general_non_packed_constraint_flag = bitSource.u(1);
			general_frame_only_constraint_flag = bitSource.u(1);

			if((general_profile_idc == 4) || (general_profile_compatibility_flag[ 4 ]==1)
					|| (general_profile_idc == 5) || (general_profile_compatibility_flag[5]==1)
					|| (general_profile_idc == 6) || (general_profile_compatibility_flag[6]==1)
					|| (general_profile_idc == 7) || (general_profile_compatibility_flag[7]==1))
			{
				general_max_12bit_constraint_flag = bitSource.u(1);
				general_max_10bit_constraint_flag = bitSource.u(1);
				general_max_8bit_constraint_flag = bitSource.u(1);
				general_max_422chroma_constraint_flag = bitSource.u(1);
				general_max_420chroma_constraint_flag = bitSource.u(1);
				general_max_monochrome_constraint_flag = bitSource.u(1);
				general_intra_constraint_flag = bitSource.u(1);
				general_one_picture_only_constraint_flag = bitSource.u(1);
				general_lower_bit_rate_constraint_flag = bitSource.u(1);
				general_reserved_zero_34bits = bitSource.readBitsLong(34);
			}else{
				general_reserved_zero_43bits = bitSource.readBitsLong(43);

			}
			if(((general_profile_idc >= 1) &&(general_profile_idc <= 5 ))
					|| (general_profile_compatibility_flag[1]==1)
					|| (general_profile_compatibility_flag[2]==1)
					|| (general_profile_compatibility_flag[3]==1)
					|| (general_profile_compatibility_flag[4]==1)
					|| (general_profile_compatibility_flag[5]==1)) /* The number of bits in this syntax structure is not affected by this condition */
			{

				general_inbld_flag = bitSource.u(1);
			}else{
				general_reserved_zero_bit = bitSource.u(1);
			}
		}
		general_level_idc= bitSource.u(8);

		sub_layer_profile_present_flag = new int[max_num_sub_layers_minus1];
		sub_layer_level_present_flag = new int[max_num_sub_layers_minus1];
		for (int i = 0; i < max_num_sub_layers_minus1; i++) {
			sub_layer_profile_present_flag[i] = bitSource.u(1);
			sub_layer_level_present_flag[i] = bitSource.u(1);
		}
		if( max_num_sub_layers_minus1 > 0 ) {
			for(int i = max_num_sub_layers_minus1; i < 8; i++ ) {
				reserved_zero_2bits[ i ] = bitSource.u(2);
			}
		}

		for(int i = 0; i < max_num_sub_layers_minus1; i++ ) {
			if (sub_layer_profile_present_flag[i] == 1) {
				sub_layer_profile_space[i] = bitSource.u(2);
				sub_layer_tier_flag[i] = bitSource.u(1);
				sub_layer_profile_idc[i] = bitSource.u(5);
				for (int j = 0; j < 32; j++) {
					sub_layer_profile_compatibility_flag[i][j] = bitSource.u(1);
				}
				sub_layer_progressive_source_flag[i] = bitSource.u(1);
				sub_layer_interlaced_source_flag[i] = bitSource.u(1);
				sub_layer_non_packed_constraint_flag[i] = bitSource.u(1);
				sub_layer_frame_only_constraint_flag[i] = bitSource.u(1);
				if ((sub_layer_profile_idc[i] == 4) || (sub_layer_profile_compatibility_flag[i][4] == 1)
						|| (sub_layer_profile_idc[i] == 5) || (sub_layer_profile_compatibility_flag[i][5] == 1)
						|| (sub_layer_profile_idc[i] == 6) || (sub_layer_profile_compatibility_flag[i][6] == 1)
						|| (sub_layer_profile_idc[i] == 7) || (sub_layer_profile_compatibility_flag[i][7] == 1)) {
					sub_layer_max_12bit_constraint_flag[i] = bitSource.u(1);
					sub_layer_max_10bit_constraint_flag[i] = bitSource.u(1);
					sub_layer_max_8bit_constraint_flag[i] = bitSource.u(1);
					sub_layer_max_422chroma_constraint_flag[i] = bitSource.u(1);
					sub_layer_max_420chroma_constraint_flag[i] = bitSource.u(1);
					sub_layer_max_monochrome_constraint_flag[i] = bitSource.u(1);
					sub_layer_intra_constraint_flag[i] = bitSource.u(1);
					sub_layer_one_picture_only_constraint_flag[i] = bitSource.u(1);
					sub_layer_lower_bit_rate_constraint_flag[i] = bitSource.u(1);
					sub_layer_reserved_zero_34bits[i] = bitSource.readBitsLong(34);
				} else{
					sub_layer_reserved_zero_43bits[i] = bitSource.readBitsLong(43);
				}
				// EB changed sub_layer_profile_compatibility_flag[ 1 ] of spec to sub_layer_profile_compatibility_flag[i][1], best guess..
				if( ((sub_layer_profile_idc[i] >= 1)&& (sub_layer_profile_idc[i] <= 5 ))
						||(sub_layer_profile_compatibility_flag[i][1]==1)
						||(sub_layer_profile_compatibility_flag[i][2]==1)
						||(sub_layer_profile_compatibility_flag[i][3]==1)
						||(sub_layer_profile_compatibility_flag[i][4]==1)
						||(sub_layer_profile_compatibility_flag[i][5]==1)){ /* The number of bits in this syntax structure is not affected by this condition */
					sub_layer_inbld_flag[i] = bitSource.u(1);
				}else{
					sub_layer_reserved_zero_bit[i] = bitSource.u(1);
				}
			}
			if( sub_layer_level_present_flag[i]==1 ){
				sub_layer_level_idc[i]= bitSource.u(8);
			}
		}
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("Profile_tier_level(fprofilePresentFlag="+profilePresentFlag+", max_num_sub_layers_minus1="+max_num_sub_layers_minus1+")"));
		t.add(new DefaultMutableTreeNode(new KVP("general_profile_space",general_profile_space,null)));
		t.add(new DefaultMutableTreeNode(new KVP("general_tier_flag",general_tier_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("general_profile_idc",general_profile_idc,null)));
		for (int j = 0; j < 32; j++) {
			t.add(new DefaultMutableTreeNode(new KVP("general_profile_compatibility_flag["+j+"]",general_profile_compatibility_flag[j],null)));
		}
		t.add(new DefaultMutableTreeNode(new KVP("general_progressive_source_flag",general_progressive_source_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("general_interlaced_source_flag",general_interlaced_source_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("general_non_packed_constraint_flag",general_non_packed_constraint_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("general_frame_only_constraint_flag",general_frame_only_constraint_flag,null)));

		if((general_profile_idc == 4) || (general_profile_compatibility_flag[ 4 ]==1)
				|| (general_profile_idc == 5) || (general_profile_compatibility_flag[5]==1)
				|| (general_profile_idc == 6) || (general_profile_compatibility_flag[6]==1)
				|| (general_profile_idc == 7) || (general_profile_compatibility_flag[7]==1))
		{
			t.add(new DefaultMutableTreeNode(new KVP("general_max_12bit_constraint_flag",general_max_12bit_constraint_flag,null)));
			t.add(new DefaultMutableTreeNode(new KVP("general_max_10bit_constraint_flag",general_max_10bit_constraint_flag,null)));
			t.add(new DefaultMutableTreeNode(new KVP("general_max_8bit_constraint_flag",general_max_8bit_constraint_flag,null)));
			t.add(new DefaultMutableTreeNode(new KVP("general_max_422chroma_constraint_flag",general_max_422chroma_constraint_flag,null)));
			t.add(new DefaultMutableTreeNode(new KVP("general_max_420chroma_constraint_flag",general_max_420chroma_constraint_flag,null)));
			t.add(new DefaultMutableTreeNode(new KVP("general_max_monochrome_constraint_flag",general_max_monochrome_constraint_flag,null)));
			t.add(new DefaultMutableTreeNode(new KVP("general_intra_constraint_flag",general_intra_constraint_flag,null)));
			t.add(new DefaultMutableTreeNode(new KVP("general_one_picture_only_constraint_flag",general_one_picture_only_constraint_flag,null)));
			t.add(new DefaultMutableTreeNode(new KVP("general_lower_bit_rate_constraint_flag",general_lower_bit_rate_constraint_flag,null)));
			t.add(new DefaultMutableTreeNode(new KVP("general_reserved_zero_34bits",general_reserved_zero_34bits,null)));
		}else{
			t.add(new DefaultMutableTreeNode(new KVP("general_reserved_zero_43bits",general_reserved_zero_43bits,null)));
		}

		if(((general_profile_idc >= 1) &&(general_profile_idc <= 5 ))
				|| (general_profile_compatibility_flag[1]==1)
				|| (general_profile_compatibility_flag[2]==1)
				|| (general_profile_compatibility_flag[3]==1)
				|| (general_profile_compatibility_flag[4]==1)
				|| (general_profile_compatibility_flag[5]==1)) /* The number of bits in this syntax structure is not affected by this condition */
		{
			t.add(new DefaultMutableTreeNode(new KVP("general_inbld_flag",general_inbld_flag,null)));
		}else{
			t.add(new DefaultMutableTreeNode(new KVP("general_reserved_zero_bit",general_reserved_zero_bit,null)));
		}

		t.add(new DefaultMutableTreeNode(new KVP("general_level_idc",general_level_idc,null)));

		for (int i = 0; i < max_num_sub_layers_minus1; i++) {
			t.add(new DefaultMutableTreeNode(new KVP("sub_layer_profile_present_flag["+i+"]",sub_layer_profile_present_flag[i],null)));
			t.add(new DefaultMutableTreeNode(new KVP("sub_layer_level_present_flag["+i+"]",sub_layer_level_present_flag[i],null)));
		}
		if( max_num_sub_layers_minus1 > 0 ) {
			for(int i = max_num_sub_layers_minus1; i < 8; i++ ) {
				t.add(new DefaultMutableTreeNode(new KVP("reserved_zero_2bits["+i+"]",reserved_zero_2bits[i],null)));
			}
		}

		for(int i = 0; i < max_num_sub_layers_minus1; i++ ) {
			if (sub_layer_profile_present_flag[i] == 1) {
				t.add(new DefaultMutableTreeNode(new KVP("sub_layer_profile_space["+i+"]",sub_layer_profile_space[i],null)));
				t.add(new DefaultMutableTreeNode(new KVP("sub_layer_tier_flag["+i+"]",sub_layer_tier_flag[i],null)));
				t.add(new DefaultMutableTreeNode(new KVP("sub_layer_profile_idc["+i+"]",sub_layer_profile_idc[i],null)));
				for (int j = 0; j < 32; j++) {
					t.add(new DefaultMutableTreeNode(new KVP("sub_layer_profile_compatibility_flag["+i+"]["+j+"]",sub_layer_profile_compatibility_flag[i][j],null)));
				}
				t.add(new DefaultMutableTreeNode(new KVP("sub_layer_progressive_source_flag["+i+"]",sub_layer_progressive_source_flag[i],null)));
				t.add(new DefaultMutableTreeNode(new KVP("sub_layer_interlaced_source_flag["+i+"]",sub_layer_interlaced_source_flag[i],null)));
				t.add(new DefaultMutableTreeNode(new KVP("sub_layer_non_packed_constraint_flag["+i+"]",sub_layer_non_packed_constraint_flag[i],null)));
				t.add(new DefaultMutableTreeNode(new KVP("sub_layer_frame_only_constraint_flag["+i+"]",sub_layer_frame_only_constraint_flag[i],null)));
				if ((sub_layer_profile_idc[i] == 4) || (sub_layer_profile_compatibility_flag[i][4] == 1)
						|| (sub_layer_profile_idc[i] == 5) || (sub_layer_profile_compatibility_flag[i][5] == 1)
						|| (sub_layer_profile_idc[i] == 6) || (sub_layer_profile_compatibility_flag[i][6] == 1)
						|| (sub_layer_profile_idc[i] == 7) || (sub_layer_profile_compatibility_flag[i][7] == 1)) {
					t.add(new DefaultMutableTreeNode(new KVP("sub_layer_max_12bit_constraint_flag["+i+"]",sub_layer_max_12bit_constraint_flag[i],null)));
					t.add(new DefaultMutableTreeNode(new KVP("sub_layer_max_10bit_constraint_flag["+i+"]",sub_layer_max_10bit_constraint_flag[i],null)));
					t.add(new DefaultMutableTreeNode(new KVP("sub_layer_max_8bit_constraint_flag["+i+"]",sub_layer_max_8bit_constraint_flag[i],null)));
					t.add(new DefaultMutableTreeNode(new KVP("sub_layer_max_422chroma_constraint_flag["+i+"]",sub_layer_max_422chroma_constraint_flag[i],null)));
					t.add(new DefaultMutableTreeNode(new KVP("sub_layer_max_420chroma_constraint_flag["+i+"]",sub_layer_max_420chroma_constraint_flag[i],null)));
					t.add(new DefaultMutableTreeNode(new KVP("sub_layer_max_monochrome_constraint_flag["+i+"]",sub_layer_max_monochrome_constraint_flag[i],null)));
					t.add(new DefaultMutableTreeNode(new KVP("sub_layer_intra_constraint_flag["+i+"]",sub_layer_intra_constraint_flag[i],null)));
					t.add(new DefaultMutableTreeNode(new KVP("sub_layer_one_picture_only_constraint_flag["+i+"]",sub_layer_one_picture_only_constraint_flag[i],null)));
					t.add(new DefaultMutableTreeNode(new KVP("sub_layer_lower_bit_rate_constraint_flag["+i+"]",sub_layer_lower_bit_rate_constraint_flag[i],null)));
					t.add(new DefaultMutableTreeNode(new KVP("sub_layer_max_monochrome_constraint_flag["+i+"]",sub_layer_max_monochrome_constraint_flag[i],null)));
					t.add(new DefaultMutableTreeNode(new KVP("sub_layer_reserved_zero_34bits["+i+"]",sub_layer_reserved_zero_34bits[i],null)));
				} else{
					t.add(new DefaultMutableTreeNode(new KVP("sub_layer_reserved_zero_43bits["+i+"]",sub_layer_reserved_zero_43bits[i],null)));
				}
				// EB changed sub_layer_profile_compatibility_flag[ 1 ] of spec to sub_layer_profile_compatibility_flag[i][1], best guess..
				if( ((sub_layer_profile_idc[i] >= 1)&& (sub_layer_profile_idc[i] <= 5 ))
						||(sub_layer_profile_compatibility_flag[i][1]==1)
						||(sub_layer_profile_compatibility_flag[i][2]==1)
						||(sub_layer_profile_compatibility_flag[i][3]==1)
						||(sub_layer_profile_compatibility_flag[i][4]==1)
						||(sub_layer_profile_compatibility_flag[i][5]==1)){ /* The number of bits in this syntax structure is not affected by this condition */
					t.add(new DefaultMutableTreeNode(new KVP("sub_layer_inbld_flag["+i+"]",sub_layer_inbld_flag[i],null)));
				}else{
					t.add(new DefaultMutableTreeNode(new KVP("sub_layer_reserved_zero_bit["+i+"]",sub_layer_reserved_zero_bit[i],null)));
				}
			}
			if( sub_layer_level_present_flag[i]==1 ){
				t.add(new DefaultMutableTreeNode(new KVP("sub_layer_level_idc["+i+"]",sub_layer_level_idc[i],null)));
			}
		}


		return t;
	}

}