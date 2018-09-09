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
 * Based on ISO/IEC 13818-2: 1995 (E), 6.2.2.4 Sequence display extension
 *
 */
public class SequenceDisplayExtension extends ExtensionHeader {

	private final int video_format;
	private final int colour_description;
	private int colour_primaries;
	private int transfer_characteristics;
	private int matrix_coefficients;
	private final int display_horizontal_size;
	private final int marker_bit;
	private final int display_vertical_size;

	/**
	 * @param data
	 * @param offset
	 */
	public SequenceDisplayExtension(final byte[] data, final int offset) {
		super(data, offset);
		video_format = bs.readBits(3);
		colour_description = bs.readBits(1);
		if(colour_description==1){
			colour_primaries = bs.readBits(8);
			transfer_characteristics = bs.readBits(8);
			matrix_coefficients = bs.readBits(8);

		}
		display_horizontal_size=bs.readBits(14);
		marker_bit=bs.readBits(1);
		display_vertical_size=bs.readBits(14);
	}

	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.controller.TreeNode#getJTreeNode(int)
	 */
	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.setUserObject(new KVP("Sequence Display extension")); //  Ugly hack to overwrite the name given in the super-super class,
		t.add(new DefaultMutableTreeNode(new KVP("video_format",video_format,getVideoFormatString(video_format))));
		t.add(new DefaultMutableTreeNode(new KVP("colour_description",colour_description,null)));
		if(colour_description==1){
			t.add(new DefaultMutableTreeNode(new KVP("colour_primaries",colour_primaries,getColourPrimariesString(colour_primaries))));
			t.add(new DefaultMutableTreeNode(new KVP("transfer_characteristics",transfer_characteristics,getTransferCharacteristicsString(transfer_characteristics))));
			t.add(new DefaultMutableTreeNode(new KVP("matrix_coefficients",matrix_coefficients,getMatrixCoefficientsString(matrix_coefficients))));
		}
		t.add(new DefaultMutableTreeNode(new KVP("display_horizontal_size",display_horizontal_size,null)));
		t.add(new DefaultMutableTreeNode(new KVP("marker_bit",marker_bit,null)));
		t.add(new DefaultMutableTreeNode(new KVP("display_vertical_size",display_vertical_size,null)));

		return t;
	}




	public static String getVideoFormatString(final int videoFromat) {

		switch (videoFromat) {
		case 0: return "component";
		case 1: return "PAL";
		case 2: return "NTSC";
		case 3: return "SECAM";
		case 4: return "MAC";
		case 5: return "Unspecified video format";
		default:
			return "reserved";
		}

	}

	public static String getColourPrimariesString(final int profile) {

		switch (profile) {
		case 0: return "(forbidden)";
		case 1: return "Recommendation ITU-R BT.709";
		case 2: return "Unspecified Video";
		case 3: return "reserved";
		case 4: return "Recommendation ITU-R BT.470-2 System M";
		case 5: return "Recommendation ITU-R BT.470-2 System B, G";
		case 6: return "SMPTE 170M";
		case 7: return "SMPTE 240M (1987)";
		default:
			return "reserved";
		}
	}

	public static String getTransferCharacteristicsString(final int profile) {

		switch (profile) {
		case 0: return "(forbidden)";
		case 1: return "Recommendation ITU-R BT.709";
		case 2: return "Unspecified Video";
		case 3: return "reserved";
		case 4: return "Recommendation ITU-R BT.470-2 System M";
		case 5: return "Recommendation ITU-R BT.470-2 System B, G";
		case 6: return "SMPTE 170M";
		case 7: return "SMPTE 240M (1987)";
		case 8: return "Linear transfer characteristics";
		default:
			return "reserved";
		}
	}

	public static String getMatrixCoefficientsString(final int profile) {

		switch (profile) {
		case 0: return "(forbidden)";
		case 1: return "Recommendation ITU-R BT.709";
		case 2: return "Unspecified Video";
		case 3: return "reserved";
		case 4: return "FCC";
		case 5: return "Recommendation ITU-R BT.470-2 System B, G";
		case 6: return "SMPTE 170M";
		case 7: return "SMPTE 240M (1987)";
		default:
			return "reserved";
		}
	}





}
