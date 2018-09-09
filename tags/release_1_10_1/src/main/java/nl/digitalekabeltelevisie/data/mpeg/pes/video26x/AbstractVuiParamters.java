/**
 * 
 * http://www.digitalekabeltelevisie.nl/dvb_inspector
 * 
 * This code is Copyright 2009-2015 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
 * 
 * This file is part of DVB Inspector.
 * 
 * DVB Inspector is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * DVB Inspector is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with DVB Inspector. If not, see
 * <http://www.gnu.org/licenses/>.
 * 
 * The author requests that he be notified of any application, applet, or other binary that makes use of this code, but
 * that's more out of curiosity than anything and is not required.
 * 
 */

package nl.digitalekabeltelevisie.data.mpeg.pes.video26x;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.util.BitSource;

/**
 * @author Eric
 *
 */
public class AbstractVuiParamters {

	protected int aspect_ratio_info_present_flag;
	protected int aspect_ratio_idc;
	protected int sar_width;
	protected int sar_height;
	protected int overscan_info_present_flag;
	protected int overscan_appropriate_flag;
	protected int video_signal_type_present_flag;
	protected int video_format;
	protected int video_full_range_flag;
	protected int colour_description_present_flag;
	protected int colour_primaries;
	protected int transfer_characteristics;
	protected int matrix_coefficients;
	protected int chroma_loc_info_present_flag;
	protected int chroma_sample_loc_type_top_field;
	protected int chroma_sample_loc_type_bottom_field;


	/**
	 * @param bitSource
	 * 
	 */
	public AbstractVuiParamters(final BitSource bitSource) {
		super();
		aspect_ratio_info_present_flag=bitSource.u(1);
		if( aspect_ratio_info_present_flag!=0 ) {
			aspect_ratio_idc =bitSource.u(8);
			if( aspect_ratio_idc == 255 ) { //Extended_SAR
				sar_width =bitSource.u(16);
				sar_height =bitSource.u(16);
			}
		}
		overscan_info_present_flag =bitSource.u(1);
		if( overscan_info_present_flag!=0 ){
			overscan_appropriate_flag =bitSource.u(1);
		}
		video_signal_type_present_flag =bitSource.u(1);
		if( video_signal_type_present_flag!=0 ){
			video_format =bitSource.u(3);
			video_full_range_flag =bitSource.u(1);
			colour_description_present_flag =bitSource.u(1);
			if( colour_description_present_flag!=0 ) {
				colour_primaries =bitSource.u(8);
				transfer_characteristics =bitSource.u(8);
				matrix_coefficients =bitSource.u(8);
			}
		}

		chroma_loc_info_present_flag  =bitSource.u(1);
		if( chroma_loc_info_present_flag!=0) {
			chroma_sample_loc_type_top_field  =bitSource.ue();
			chroma_sample_loc_type_bottom_field  =bitSource.ue();
		}
	}

	public int getAspect_ratio_info_present_flag() {
		return aspect_ratio_info_present_flag;
	}

	public int getAspect_ratio_idc() {
		return aspect_ratio_idc;
	}

	public int getSar_width() {
		return sar_width;
	}

	public int getSar_height() {
		return sar_height;
	}

	public int getOverscan_info_present_flag() {
		return overscan_info_present_flag;
	}

	public int getOverscan_appropriate_flag() {
		return overscan_appropriate_flag;
	}

	public int getVideo_signal_type_present_flag() {
		return video_signal_type_present_flag;
	}

	public int getVideo_format() {
		return video_format;
	}

	public int getVideo_full_range_flag() {
		return video_full_range_flag;
	}

	public int getColour_description_present_flag() {
		return colour_description_present_flag;
	}

	public int getColour_primaries() {
		return colour_primaries;
	}

	public int getTransfer_characteristics() {
		return transfer_characteristics;
	}

	public int getMatrix_coefficients() {
		return matrix_coefficients;
	}

	public int getChroma_loc_info_present_flag() {
		return chroma_loc_info_present_flag;
	}

	public int getChroma_sample_loc_type_top_field() {
		return chroma_sample_loc_type_top_field;
	}

	public int getChroma_sample_loc_type_bottom_field() {
		return chroma_sample_loc_type_bottom_field;
	}
	/**
	 * @param t
	 */
	protected void addCommonFields(final DefaultMutableTreeNode t) {
		t.add(new DefaultMutableTreeNode(new KVP("aspect_ratio_info_present_flag",aspect_ratio_info_present_flag,null)));
		if( aspect_ratio_info_present_flag!=0 ) {
			t.add(new DefaultMutableTreeNode(new KVP("aspect_ratio_idc",aspect_ratio_idc,getAspectRationIdcString(aspect_ratio_idc))));
			if( aspect_ratio_idc == 255 ) { //Extended_SAR
				t.add(new DefaultMutableTreeNode(new KVP("sar_width",sar_width,null)));
				t.add(new DefaultMutableTreeNode(new KVP("sar_height",sar_height,null)));
			}
		}
		t.add(new DefaultMutableTreeNode(new KVP("overscan_info_present_flag",overscan_info_present_flag,null)));
		if( overscan_info_present_flag!=0 ){
			t.add(new DefaultMutableTreeNode(new KVP("overscan_appropriate_flag",overscan_appropriate_flag,overscan_appropriate_flag==1?"suitable for display using overscan":"output should not be displayed using overscan")));
		}
		t.add(new DefaultMutableTreeNode(new KVP("video_signal_type_present_flag",video_signal_type_present_flag,null)));
		if( video_signal_type_present_flag!=0 ){
			t.add(new DefaultMutableTreeNode(new KVP("video_format",video_format,getVideoFormatString(video_format))));
	
			t.add(new DefaultMutableTreeNode(new KVP("video_full_range_flag",video_full_range_flag,null)));
			t.add(new DefaultMutableTreeNode(new KVP("colour_description_present_flag",colour_description_present_flag,null)));
			if( colour_description_present_flag!=0 ) {
				t.add(new DefaultMutableTreeNode(new KVP("colour_primaries",colour_primaries,null)));
				t.add(new DefaultMutableTreeNode(new KVP("transfer_characteristics",transfer_characteristics,null)));
				t.add(new DefaultMutableTreeNode(new KVP("matrix_coefficients",matrix_coefficients,null)));
			}
		}
	
		t.add(new DefaultMutableTreeNode(new KVP("chroma_loc_info_present_flag",chroma_loc_info_present_flag,null)));
		if( chroma_loc_info_present_flag!=0) {
			t.add(new DefaultMutableTreeNode(new KVP("chroma_sample_loc_type_top_field",chroma_sample_loc_type_top_field,null)));
			t.add(new DefaultMutableTreeNode(new KVP("chroma_sample_loc_type_bottom_field",chroma_sample_loc_type_bottom_field,null)));
		}
	}

	public static String getAspectRationIdcString(final int aspect_ratio_idc) {

		switch (aspect_ratio_idc) {
		case 0 : return "Unspecified";
		case 1 : return "1:1 (square)";
		case 2 : return "12:11";
		case 3 : return "10:11";
		case 4 : return "16:11";
		case 5 : return "40:33";
		case 6 : return "24:11";
		case 7 : return "20:11";
		case 8 : return "32:11";
		case 9 : return "80:33";
		case 10 : return "18:11";
		case 11 : return "15:11";
		case 12 : return "64:33";
		case 13 : return "160:99";
		case 14 : return "4:3";
		case 15 : return "3:2";
		case 16 : return "2:1";
		case 255 : return "Extended_SAR";

		default:
			return "reserved";
		}
	}

	public static String getVideoFormatString(final int video_format) {
	
		switch (video_format) {
		case 0  : return "Component";
		case 1  : return "PAL";
		case 2  : return "NTSC";
		case 3  : return "SECAM";
		case 4  : return "MAC";
		case 5  : return "Unspecified video format";
		default:
			return "reserved";
		}
	}

}