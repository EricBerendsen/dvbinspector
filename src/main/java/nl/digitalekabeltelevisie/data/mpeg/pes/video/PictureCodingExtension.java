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

package nl.digitalekabeltelevisie.data.mpeg.pes.video;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;


/**
 * @author Eric Berendsen
 *
 */
public class PictureCodingExtension extends ExtensionHeader {

	private final int forward_horizontal_f_code;
	private final int forward_vertical_f_code;
	private final int backward_horizontal_f_code;
	private final int backward_vertical_f_code;

	private final int intra_dc_precision; // 2 uimsbf
	private final int picture_structure; // 2 uimsbf
	private final int top_field_first; // 1 uimsbf
	private final int frame_pred_frame_dct; // 1 uimsbf
	private final int concealment_motion_vectors; // 1 uimsbf
	private final int q_scale_type; // 1 uimsbf
	private final int intra_vlc_format; // 1 uimsbf
	private final int alternate_scan; // 1 uimsbf
	private final int repeat_first_field; // 1 uimsbf
	private final int chroma_420_type; // 1 uimsbf
	private final int progressive_frame; // 1 uimsbf
	private final int composite_display_flag; // 1 uimsbf
	private int v_axis; // 1 uimsbf
	private int field_sequence; // 3 uimsbf
	private int sub_carrier; // 1 uimsbf
	private int burst_amplitude; // 7 uimsbf
	private int sub_carrier_phase; // 8 uimsbf

	/**
	 * @param data
	 * @param offset
	 */
	public PictureCodingExtension(final byte[] data, final int offset) {
		super(data, offset);
		forward_horizontal_f_code = bs.readBits(4);
		forward_vertical_f_code = bs.readBits(4);
		backward_horizontal_f_code = bs.readBits(4);
		backward_vertical_f_code = bs.readBits(4);
		intra_dc_precision = bs.readBits(2);
		picture_structure = bs.readBits(2);
		top_field_first = bs.readBits(1);
		frame_pred_frame_dct = bs.readBits(1);
		concealment_motion_vectors = bs.readBits(1);
		q_scale_type = bs.readBits(1);
		intra_vlc_format = bs.readBits(1);
		alternate_scan = bs.readBits(1);
		repeat_first_field = bs.readBits(1);
		chroma_420_type = bs.readBits(1);
		progressive_frame = bs.readBits(1);
		composite_display_flag = bs.readBits(1);
		if ( composite_display_flag ==1) {
			v_axis = bs.readBits(1);
			field_sequence = bs.readBits(3);
			sub_carrier = bs.readBits(1);
			burst_amplitude = bs.readBits(7);
			sub_carrier_phase = bs.readBits(8);
		}
	}

	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.controller.TreeNode#getJTreeNode(int)
	 */
	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.setUserObject(new KVP("Picture Coding Extension")); //  Ugly hack to overwrite the name given in the super-super class,
		t.add(new DefaultMutableTreeNode(new KVP("f_code[0][0], forward horizontal",forward_horizontal_f_code,null)));
		t.add(new DefaultMutableTreeNode(new KVP("f_code[0][1], forward vertical",forward_vertical_f_code,null)));
		t.add(new DefaultMutableTreeNode(new KVP("f_code[1][0], backward horizontal",backward_horizontal_f_code,null)));
		t.add(new DefaultMutableTreeNode(new KVP("f_code[1][1], backward vertical",backward_vertical_f_code,null)));

		t.add(new DefaultMutableTreeNode(new KVP("intra_dc_precision",intra_dc_precision,(8+intra_dc_precision)+" bits")));
		t.add(new DefaultMutableTreeNode(new KVP("picture_structure",picture_structure,getPictureStructureString(picture_structure))));
		t.add(new DefaultMutableTreeNode(new KVP("top_field_first",top_field_first,null)));
		t.add(new DefaultMutableTreeNode(new KVP("frame_pred_frame_dct",frame_pred_frame_dct,(frame_pred_frame_dct==1?"only frame-DCT and frame prediction are used":""))));

		t.add(new DefaultMutableTreeNode(new KVP("concealment_motion_vectors",concealment_motion_vectors,(concealment_motion_vectors==1?"motion vectors are coded in intra macroblocks":"no motion vectors are coded in intra macroblocks"))));
		t.add(new DefaultMutableTreeNode(new KVP("q_scale_type",q_scale_type,null)));
		t.add(new DefaultMutableTreeNode(new KVP("intra_vlc_format",intra_vlc_format,null)));
		t.add(new DefaultMutableTreeNode(new KVP("alternate_scan",alternate_scan,null)));
		t.add(new DefaultMutableTreeNode(new KVP("repeat_first_field",repeat_first_field,null)));
		t.add(new DefaultMutableTreeNode(new KVP("chroma_420_type",chroma_420_type,null)));
		t.add(new DefaultMutableTreeNode(new KVP("progressive_frame",progressive_frame,(progressive_frame==1?"the two fields (of the frame) are from the same time instant as one another":"the two fields of the frame are interlaced fields in which an interval of time of the field period exists between (corresponding spatial samples) of the two fields"))));
		t.add(new DefaultMutableTreeNode(new KVP("composite_display_flag",composite_display_flag,null)));
		if ( composite_display_flag ==1) {
			t.add(new DefaultMutableTreeNode(new KVP("v_axis",v_axis,null)));
			t.add(new DefaultMutableTreeNode(new KVP("field_sequence",field_sequence,null)));
			t.add(new DefaultMutableTreeNode(new KVP("sub_carrier",sub_carrier,null)));

			t.add(new DefaultMutableTreeNode(new KVP("burst_amplitude",burst_amplitude,null)));
			t.add(new DefaultMutableTreeNode(new KVP("sub_carrier_phase",sub_carrier_phase,null)));

		}

		return t;
	}



	public static String getPictureStructureString(final int picture_structure) {
		switch (picture_structure) {
		case 0: return "reserved";
		case 1: return "Top Field";
		case 2: return "Bottom Field";
		case 3: return "Frame picture";

		default: return "error";
		}
	}


}
