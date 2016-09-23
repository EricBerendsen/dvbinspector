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

package nl.digitalekabeltelevisie.data.mpeg.pes.ebu;

import static nl.digitalekabeltelevisie.util.Utils.getInt;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;

/**
 * @author Eric Berendsen
 * 
 * Based on
 * ETSI EN 300 294 V1.4.1 (2003-04)
 * Television systems;
 * 625-line television
 * Wide Screen Signalling (WSS)
 *
 */
public class WSSDataField extends EBUDataField {

	/**
	 * @param data
	 * @param offset
	 * @param len
	 */
	public WSSDataField(final byte[] data, final int offset, final int len, final long pts) {
		super(data, offset, len, pts);
	}
	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.controller.TreeNode#getJTreeNode(int)
	 */
	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode s = super.getJTreeNode(modus);
		final int wss_data_block = getInt(data_block, offset+3, 2, 0xFFFC)>>2;
		s.add(new DefaultMutableTreeNode(new KVP("wss_data_block",wss_data_block,null)));
		final int aspectRatio = getInt(data_block, offset+3, 1, 0xF0)>>4;
		s.add(new DefaultMutableTreeNode(new KVP("Aspect Ratio",aspectRatio,getAspectRatioString(aspectRatio))));
		final int filmBit = getInt(data_block, offset+3, 1, 0x08)>>3;
		s.add(new DefaultMutableTreeNode(new KVP("Film Bit",filmBit,getFilmBitString(filmBit))));
		final int colourCodingBit = getInt(data_block, offset+3, 1, 0x04)>>2;
		s.add(new DefaultMutableTreeNode(new KVP("Colour coding Bit",colourCodingBit,getColourCodingBitString(colourCodingBit))));
		final int helperBit = getInt(data_block, offset+3, 1, 0x02)>>1;
		s.add(new DefaultMutableTreeNode(new KVP("Helper Bit",helperBit,getHelperBitString(helperBit))));
		final int subtitlesWithinTeletextBit = getInt(data_block, offset+4, 1, 0x80)>>7;
		s.add(new DefaultMutableTreeNode(new KVP("Subtitles within Teletext",subtitlesWithinTeletextBit,getSubtitlesWithinTeletextString(subtitlesWithinTeletextBit))));
		final int subtitlingMode = getInt(data_block, offset+4, 1, 0x60)>>5;
		s.add(new DefaultMutableTreeNode(new KVP("Subtitling mode",subtitlingMode,getSubtitlingModeString(subtitlingMode))));
		final int surroundSoundBit = getInt(data_block, offset+4, 1, 0x10)>>4;
		s.add(new DefaultMutableTreeNode(new KVP("Surround sound bit",surroundSoundBit,getSurroundSoundBitString(surroundSoundBit))));
		final int copyrightBit = getInt(data_block, offset+4, 1, 0x08)>>3;
		s.add(new DefaultMutableTreeNode(new KVP("Copyright bit",copyrightBit,getCopyrightBitString(copyrightBit))));
		final int generationBit = getInt(data_block, offset+4, 1, 0x04)>>2;
		s.add(new DefaultMutableTreeNode(new KVP("Generation bit",generationBit,getGenerationBitString(generationBit))));
		return s;
	}


	public static String getAspectRatioString(final int flag) {
		switch (flag) {
		case 1:
			return "4:3 full format";
		case 8:
			return "14:9 letterbox centre";
		case 4:
			return "14:9 letterbox top";
		case 13:
			return "16:9 letterbox centre";
		case 2:
			return "16:9 letterbox top";
		case 11:
			return "> 16:9 letterbox centre";
		case 7:
			return "14:9 full format";
		case 14:
			return "16:9 full format";
		default:
			return "Parity error";
		}
	}

	public static String getFilmBitString(final int flag) {
		switch (flag) {
		case 0:
			return "Camera mode";
		case 1:
			return "Film mode";
		default:
			return "Illegal Value";
		}
	}

	public static String getColourCodingBitString(final int flag) {
		switch (flag) {
		case 0:
			return "standard coding";
		case 1:
			return "Motion Adaptive Colour Plus";
		default:
			return "Illegal Value";
		}
	}

	public static String getHelperBitString(final int flag) {
		switch (flag) {
		case 0:
			return "No helper";
		case 1:
			return "Modulated helper";
		default:
			return "Illegal Value";
		}
	}
	public static String getSubtitlesWithinTeletextString(final int flag) {
		switch (flag) {
		case 0:
			return "no subtitles within Teletext";
		case 1:
			return "subtitles within Teletext";
		default:
			return "Illegal Value";
		}
	}

	public static String getSubtitlingModeString(final int flag) {
		switch (flag) {
		case 0:
			return "no open subtitles";
		case 1:
			return "subtitles out of active image area";
		case 2:
			return "subtitles in active image area";
		case 3:
			return "reserved";
		default:
			return "Illegal Value";
		}
	}

	public static String getSurroundSoundBitString(final int flag) {
		switch (flag) {
		case 0:
			return "no surround sound information";
		case 1:
			return "surround sound mode";
		default:
			return "Illegal Value";
		}
	}

	public static String getCopyrightBitString(final int flag) {
		switch (flag) {
		case 0:
			return "no copyright asserted or status unknown";
		case 1:
			return "copyright asserted";
		default:
			return "Illegal Value";
		}
	}
	public static String getGenerationBitString(final int flag) {
		switch (flag) {
		case 0:
			return "copying not restricted";
		case 1:
			return "copying restricted";
		default:
			return "Illegal Value";
		}
	}
}
