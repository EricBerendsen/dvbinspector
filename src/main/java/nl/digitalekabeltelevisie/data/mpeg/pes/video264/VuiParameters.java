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

import static nl.digitalekabeltelevisie.data.mpeg.pes.video.common.VideoHandler.getClockTickString;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.pes.video26x.AbstractVuiParamters;
import nl.digitalekabeltelevisie.util.BitSource;

public class VuiParameters extends AbstractVuiParamters implements TreeNode {


	private final int timing_info_present_flag;
	private int num_units_in_tick;
	private int time_scale;
	private int fixed_frame_rate_flag;


	private final int nal_hrd_parameters_present_flag;
	private HrdParameters nal_hrd_parameters;
	private final int vcl_hrd_parameters_present_flag;
	private HrdParameters vcl_hrd_parameters;
	private int low_delay_hrd_flag;
	private final int pic_struct_present_flag;
	private final int bitstream_restriction_flag;
	private int motion_vectors_over_pic_boundaries_flag;
	private int max_bytes_per_pic_denom;
	private int max_bits_per_mb_denom;
	private int log2_max_mv_length_horizontal;
	private int log2_max_mv_length_vertical;
	private int num_reorder_frames;
	private int max_dec_frame_buffering;


	public VuiParameters(final BitSource bitSource) {
		super(bitSource);
		timing_info_present_flag =bitSource.u(1);
		if( timing_info_present_flag !=0) {
			num_units_in_tick  =bitSource.u(32);
			time_scale =bitSource.u(32);
			fixed_frame_rate_flag  =bitSource.u(1);
		}

		nal_hrd_parameters_present_flag=bitSource.u(1);
		if( nal_hrd_parameters_present_flag!=0 ){
			nal_hrd_parameters = new HrdParameters(bitSource);
		}
		vcl_hrd_parameters_present_flag=bitSource.u(1);
		if( vcl_hrd_parameters_present_flag!=0 ){
			vcl_hrd_parameters = new HrdParameters(bitSource);
		}
		if( (nal_hrd_parameters_present_flag!=0) || (vcl_hrd_parameters_present_flag!=0) ){
			low_delay_hrd_flag=bitSource.u(1);
		}
		pic_struct_present_flag=bitSource.u(1);
		bitstream_restriction_flag=bitSource.u(1);
		if( bitstream_restriction_flag!=0 ) {
			motion_vectors_over_pic_boundaries_flag=bitSource.u(1);
			max_bytes_per_pic_denom=bitSource.ue();
			max_bits_per_mb_denom=bitSource.ue();
			log2_max_mv_length_horizontal=bitSource.ue();
			log2_max_mv_length_vertical=bitSource.ue();
			num_reorder_frames=bitSource.ue();
			max_dec_frame_buffering=bitSource.ue();
		}
	}

	@Override
	public KVP getJTreeNode(int modus) {
		KVP t = new KVP("vui_parameters");
		addCommonFields(t);
		t.add(new KVP("timing_info_present_flag",timing_info_present_flag));
		if( timing_info_present_flag !=0) {
			t.add(new KVP("num_units_in_tick",num_units_in_tick));
			t.add(new KVP("time_scale",time_scale,getClockTickString(num_units_in_tick, time_scale)));
			t.add(new KVP("fixed_frame_rate_flag",fixed_frame_rate_flag));
		}


		t.add(new KVP("nal_hrd_parameters_present_flag",nal_hrd_parameters_present_flag));
		if( nal_hrd_parameters_present_flag!=0 ){
			t.add(nal_hrd_parameters.getJTreeNode(modus));
		}
		t.add(new KVP("vcl_hrd_parameters_present_flag",vcl_hrd_parameters_present_flag));
		if( vcl_hrd_parameters_present_flag!=0 ){
			t.add(vcl_hrd_parameters.getJTreeNode(modus));
		}
		if( (nal_hrd_parameters_present_flag!=0) || (vcl_hrd_parameters_present_flag!=0) ){
			t.add(new KVP("low_delay_hrd_flag",low_delay_hrd_flag));
		}
		t.add(new KVP("pic_struct_present_flag",pic_struct_present_flag));

		t.add(new KVP("bitstream_restriction_flag",bitstream_restriction_flag));
		if(bitstream_restriction_flag!=0){
			t.add(new KVP("motion_vectors_over_pic_boundaries_flag",motion_vectors_over_pic_boundaries_flag));
			t.add(new KVP("max_bytes_per_pic_denom",max_bytes_per_pic_denom));
			t.add(new KVP("max_bits_per_mb_denom",max_bits_per_mb_denom));
			t.add(new KVP("log2_max_mv_length_horizontal",log2_max_mv_length_horizontal));
			t.add(new KVP("log2_max_mv_length_vertical",log2_max_mv_length_vertical));
			t.add(new KVP("num_reorder_frames",num_reorder_frames));
			t.add(new KVP("max_dec_frame_buffering",max_dec_frame_buffering));
		}

		return t;
	}

	public int getTiming_info_present_flag() {
		return timing_info_present_flag;
	}

	public int getNum_units_in_tick() {
		return num_units_in_tick;
	}

	public int getTime_scale() {
		return time_scale;
	}

	public int getFixed_frame_rate_flag() {
		return fixed_frame_rate_flag;
	}

	public int getNal_hrd_parameters_present_flag() {
		return nal_hrd_parameters_present_flag;
	}

	public int getVcl_hrd_parameters_present_flag() {
		return vcl_hrd_parameters_present_flag;
	}

	public int getLow_delay_hrd_flag() {
		return low_delay_hrd_flag;
	}

	public int getPic_struct_present_flag() {
		return pic_struct_present_flag;
	}

	public int getBitstream_restriction_flag() {
		return bitstream_restriction_flag;
	}

	public int getMotion_vectors_over_pic_boundaries_flag() {
		return motion_vectors_over_pic_boundaries_flag;
	}

	public int getMax_bytes_per_pic_denom() {
		return max_bytes_per_pic_denom;
	}

	public int getMax_bits_per_mb_denom() {
		return max_bits_per_mb_denom;
	}

	public int getLog2_max_mv_length_horizontal() {
		return log2_max_mv_length_horizontal;
	}

	public int getLog2_max_mv_length_vertical() {
		return log2_max_mv_length_vertical;
	}

	public int getNum_reorder_frames() {
		return num_reorder_frames;
	}

	public int getMax_dec_frame_buffering() {
		return max_dec_frame_buffering;
	}

}
