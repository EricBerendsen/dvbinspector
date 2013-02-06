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

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;


/**
 * @author Eric Berendsen
 *
 */
public class PictureHeader extends VideoMPEG2Section {

	public static class ExtraInformationPicture{
		/**
		 * @param extra_bit_picture
		 * @param extra_information_picture
		 */
		public ExtraInformationPicture(final int extra_bit_picture,
				final int extra_information_picture) {
			super();
			this.extra_bit_picture = extra_bit_picture;
			this.extra_information_picture = extra_information_picture;
		}
		private int extra_bit_picture;
		private int extra_information_picture;

		public int getExtra_bit_picture() {
			return extra_bit_picture;
		}
		public void setExtra_bit_picture(final int extra_bit_picture) {
			this.extra_bit_picture = extra_bit_picture;
		}
		public int getExtra_information_picture() {
			return extra_information_picture;
		}
		public void setExtra_information_picture(final int extra_information_picture) {
			this.extra_information_picture = extra_information_picture;
		}

	}

	private final int temporal_reference;
	private final int picture_coding_type;
	private final int vbv_delay;
	private int full_pel_forward_vector;
	private int forward_f_code;
	private int full_pel_backward_vector;
	private int backward_f_code;
	private final List<ExtraInformationPicture> extraPicture = new ArrayList<PictureHeader.ExtraInformationPicture>();
	private final int extra_bit_picture;

	/**
	 * @param data
	 * @param offset
	 */
	public PictureHeader(final byte[] data, final int offset) {
		super(data, offset);
		temporal_reference = bs.readBits(10);
		picture_coding_type = bs.readBits(3);
		vbv_delay = bs.readBits(16);
		if ((picture_coding_type == 2) || (picture_coding_type == 3)) {
			full_pel_forward_vector = bs.readBits(1);
			forward_f_code = bs.readBits(3);
		}
		if ( picture_coding_type == 3 ) {
			full_pel_backward_vector = bs.readBits(1);
			backward_f_code = bs.readBits(3);
		}
		// TODO dvbsnoop shows extra info where we don't find it.
		int nextBits = bs.readBits(1);
		while(nextBits==1){
			final ExtraInformationPicture p = new ExtraInformationPicture(nextBits, bs.readBits(8));
			extraPicture.add(p);
			nextBits = bs.readBits(1);
		}
		extra_bit_picture = nextBits;
	}

	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.controller.TreeNode#getJTreeNode(int)
	 */
	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("temporal_reference",temporal_reference,null)));
		t.add(new DefaultMutableTreeNode(new KVP("picture_coding_type",picture_coding_type,getPictureCodingTypeString(picture_coding_type))));
		t.add(new DefaultMutableTreeNode(new KVP("vbv_delay",vbv_delay,null)));
		if ((picture_coding_type == 2) || (picture_coding_type == 3)) {
			t.add(new DefaultMutableTreeNode(new KVP("full_pel_forward_vector",full_pel_forward_vector,null)));
			t.add(new DefaultMutableTreeNode(new KVP("forward_f_code",forward_f_code,null)));
		}
		if ( picture_coding_type == 3 ) {
			t.add(new DefaultMutableTreeNode(new KVP("full_pel_backward_vector",full_pel_backward_vector,null)));
			t.add(new DefaultMutableTreeNode(new KVP("backward_f_code",backward_f_code,null)));
		}
		for(final ExtraInformationPicture pic :extraPicture){
			t.add(new DefaultMutableTreeNode(new KVP("extra_bit_picture",pic.getExtra_bit_picture(),null)));
			t.add(new DefaultMutableTreeNode(new KVP("extra_information_picture",pic.getExtra_information_picture(),null)));

		}
		t.add(new DefaultMutableTreeNode(new KVP("extra_bit_picture",extra_bit_picture,null)));
		return t;
	}

	public static String getPictureCodingTypeString(final int startCode) {

		switch (startCode) {
		case 0: return "forbidden";
		case 1: return "intra-coded (I)";
		case 2 : return "predictive-coded (P)";
		case 3 : return "bidirectionally-predictive-coded (B)";
		case 4 : return "shall not be used (dc intra-coded (D) in ISO/IEC11172-2)";
		case 5 : return "reserved";
		case 6 : return "reserved";
		case 7 : return "reserved";
		default:
			return "error";
		}
	}


	/**
	 * @return the picture_coding_type
	 */
	public int getPicture_coding_type() {
		return picture_coding_type;
	}

	/**
	 * @return the picture_coding_type
	 */
	public String getPictureCodingTypeShortString() {

		switch (picture_coding_type) {
		case 0: return "?";
		case 1: return "I";
		case 2 : return "P";
		case 3 : return "B";
		case 4 : return "D";
		default:
			return "?";
		}
	}

	/**
	 * @return the temporal_reference
	 */
	public int getTemporal_reference() {
		return temporal_reference;
	}


	/**
	 * @return the vbv_delay
	 */
	public int getVbv_delay() {
		return vbv_delay;
	}

}
