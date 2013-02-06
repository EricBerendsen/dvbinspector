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
import nl.digitalekabeltelevisie.util.Utils;


/**
 * @author Eric Berendsen
 *
 */
public class SequenceHeader extends VideoMPEG2Section {

	private final int horizontal_size_value;
	private final int vertical_size_value;
	private final int aspect_ratio_information;
	private final int frame_rate_code;
	private final int bit_rate_value;
	private final int marker_bit;
	private final int vbv_buffer_size_value;
	private final int constrained_parameters_flag;
	private final int load_intra_quantiser_matrix;
	private final int [] intra_quantiser_matrix = new int[64];
	private final int load_non_intra_quantiser_matrix;
	private final int [] non_intra_quantiser_matrix = new int[64];

	/**
	 * @param data
	 * @param offset
	 */
	public SequenceHeader(final byte[] data, final int offset) {
		super(data, offset);



		horizontal_size_value = bs.readBits(12);
		vertical_size_value =  bs.readBits(12);
		aspect_ratio_information =  bs.readBits(4);
		frame_rate_code =  bs.readBits(4);
		bit_rate_value =  bs.readBits(18);
		marker_bit =  bs.readBits(1);
		vbv_buffer_size_value =  bs.readBits(10);
		constrained_parameters_flag =  bs.readBits(1);
		load_intra_quantiser_matrix =  bs.readBits(1);
		if(load_intra_quantiser_matrix==1){
			for (int i = 0; i < intra_quantiser_matrix.length; i++) {
				intra_quantiser_matrix[i] = bs.readBits(8);
			}
		}
		load_non_intra_quantiser_matrix =  bs.readBits(1);
		if(load_non_intra_quantiser_matrix==1){
			for (int i = 0; i < non_intra_quantiser_matrix.length; i++) {
				non_intra_quantiser_matrix[i] = bs.readBits(8);
			}
		}
	}

	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.controller.TreeNode#getJTreeNode(int)
	 */
	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("horizontal_size_value",horizontal_size_value,null)));
		t.add(new DefaultMutableTreeNode(new KVP("vertical_size_value",vertical_size_value,null)));
		t.add(new DefaultMutableTreeNode(new KVP("aspect_ratio_information",aspect_ratio_information,Utils.getAspectRatioInformationString(aspect_ratio_information))));
		t.add(new DefaultMutableTreeNode(new KVP("frame_rate_code",frame_rate_code,getFrameRateCodeString(frame_rate_code))));
		t.add(new DefaultMutableTreeNode(new KVP("bit_rate_value",bit_rate_value,(bit_rate_value*400)+" bit/s")));
		t.add(new DefaultMutableTreeNode(new KVP("marker_bit",marker_bit,null)));
		t.add(new DefaultMutableTreeNode(new KVP("vbv_buffer_size_value",vbv_buffer_size_value,null)));
		t.add(new DefaultMutableTreeNode(new KVP("constrained_parameters_flag",constrained_parameters_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("load_intra_quantiser_matrix",load_intra_quantiser_matrix,null)));
		if(load_intra_quantiser_matrix==1){
			final DefaultMutableTreeNode intra_quantiser_matrixNode = new DefaultMutableTreeNode(new KVP("intra_quantiser_matrix"));
			for (int i = 0; i < intra_quantiser_matrix.length; i++) {
				intra_quantiser_matrixNode.add(new DefaultMutableTreeNode(new KVP("intra_quantiser_matrix["+i+"]",intra_quantiser_matrix[i],null)));
			}
			t.add(intra_quantiser_matrixNode);
		}
		t.add(new DefaultMutableTreeNode(new KVP("load_non_intra_quantiser_matrix",load_non_intra_quantiser_matrix,null)));
		if(load_non_intra_quantiser_matrix==1){
			final DefaultMutableTreeNode non_intra_quantiser_matrixNode = new DefaultMutableTreeNode(new KVP("non_intra_quantiser_matrix"));
			for (int i = 0; i < non_intra_quantiser_matrix.length; i++) {
				non_intra_quantiser_matrixNode.add(new DefaultMutableTreeNode(new KVP("non_intra_quantiser_matrix["+i+"]",non_intra_quantiser_matrix[i],null)));
			}
			t.add(non_intra_quantiser_matrixNode);

		}
		return t;
	}

	public static String getFrameRateCodeString(final int s) {

		switch (s) {
		case 0: return "forbidden";
		case 1: return "23,976";
		case 2 : return "24";
		case 3 : return "25";
		case 4 : return "29,97";
		case 5 : return "30";
		case 6 : return "50";
		case 7 : return "59,94";
		case 8 : return "60";

		default:
			return "reserved";
		}
	}


	/**
	 * @return the aspect_ratio_information
	 */
	public int getAspect_ratio_information() {
		return aspect_ratio_information;
	}


	/**
	 * @return the bit_rate_value
	 */
	public int getBit_rate_value() {
		return bit_rate_value;
	}


	/**
	 * @return the frame_rate_code
	 */
	public int getFrame_rate_code() {
		return frame_rate_code;
	}


	/**
	 * @return the horizontal_size_value
	 */
	public int getHorizontal_size_value() {
		return horizontal_size_value;
	}


	/**
	 * @return the vertical_size_value
	 */
	public int getVertical_size_value() {
		return vertical_size_value;
	}

}
