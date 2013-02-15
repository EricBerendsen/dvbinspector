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
public class SequenceExtension extends ExtensionHeader {

	private final int profile_and_level_indication;
	private final int progressive_sequence;
	private final int chroma_format;
	private final int horizontal_size_extension;
	private final int vertical_size_extension;
	private final int bit_rate_extension;
	private final int vbv_buffer_size_extension;
	private final int low_delay;
	private final int frame_rate_extension_n;
	private final int frame_rate_extension_d;

	/**
	 * @param data
	 * @param offset
	 */
	public SequenceExtension(final byte[] data, final int offset) {
		super(data, offset);
		profile_and_level_indication = Utils.getInt(data, offset+1, 2, 0x0FF0)>>4;
		progressive_sequence = Utils.getInt(data, offset+2, 1, 0x08)>>3;
		chroma_format = Utils.getInt(data, offset+2, 1, 0x06)>>1;
		horizontal_size_extension = Utils.getInt(data, offset+2, 2, 0x0180)>>7;
		vertical_size_extension = Utils.getInt(data, offset+3, 1, 0x60)>>5;
		bit_rate_extension = Utils.getInt(data, offset+3, 2, 0x1FFE)>>1;
		// skip marker bit
		vbv_buffer_size_extension = Utils.getInt(data, offset+5, 1, Utils.MASK_8BITS);
		low_delay = Utils.getInt(data, offset+6, 1, 0x80)>>8;
		frame_rate_extension_n = Utils.getInt(data, offset+6, 1, 0x60)>>5;
		frame_rate_extension_d = Utils.getInt(data, offset+6, 1, 0x60);
	}

	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.controller.TreeNode#getJTreeNode(int)
	 */
	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.setUserObject(new KVP("Sequence extension")); //  Ugly hack to overwrite the name given in the super-super class,
		t.add(new DefaultMutableTreeNode(new KVP("profile_and_level_indication",profile_and_level_indication,getProfileLevelString(profile_and_level_indication))));
		t.add(new DefaultMutableTreeNode(new KVP("progressive_sequence",progressive_sequence,progressive_sequence==1?"video sequence contains only progressive framepictures":"video sequence may contain both framepictures and field-pictures, and frame-picture may be progressive or interlaced frames")));
		t.add(new DefaultMutableTreeNode(new KVP("chroma_format",chroma_format,getChromaFormatString(chroma_format))));
		t.add(new DefaultMutableTreeNode(new KVP("horizontal_size_extension",horizontal_size_extension,null)));
		t.add(new DefaultMutableTreeNode(new KVP("vertical_size_extension",vertical_size_extension,null)));
		t.add(new DefaultMutableTreeNode(new KVP("bit_rate_extension",bit_rate_extension,null)));
		t.add(new DefaultMutableTreeNode(new KVP("vbv_buffer_size_extension",vbv_buffer_size_extension,null)));
		t.add(new DefaultMutableTreeNode(new KVP("low_delay",low_delay,low_delay==1?"sequence does not contain any B-pictures":"sequence may contain B-pictures")));
		t.add(new DefaultMutableTreeNode(new KVP("frame_rate_extension_n",frame_rate_extension_n,null)));
		t.add(new DefaultMutableTreeNode(new KVP("frame_rate_extension_d",frame_rate_extension_d,null)));
		return t;
	}



	public static String getChromaFormatString(final int startCode) {

		switch (startCode) {
		case 0: return "reserved";
		case 1: return "4:2:0";
		case 2 : return "4:2:2";
		case 3 : return "4:4:4";
		default:
			return "error";
		}
	}

	public static String getProfileLevelString(final int profileLevel) {

		final int escapeBit = (profileLevel&0x80)>>7;
		final int profile = (profileLevel&0x70)>>4;
		final int level = (profileLevel&0x0F);

		final StringBuilder b = new StringBuilder("Escape bit: ").append(escapeBit);
		b.append(", profile: ").append(getProfileString(profile));
		b.append(", level: ").append(getLevelString(level));

		return b.toString();

	}

	public static String getProfileString(final int profile) {

		switch (profile) {
		case 0: return "reserved";
		case 1: return "High";
		case 2: return "Spatially Scalable";
		case 3: return "SNR Scalable";
		case 4: return "Main";
		case 5: return "Simple";
		default:
			return "reserved";
		}
	}

	public static String getLevelString(final int level) {

		switch (level) {
		case 4: return "High";
		case 6: return "High 1440";
		case 8: return "Main";
		case 10: return "Low";
		default:
			return "reserved";
		}
	}


}
