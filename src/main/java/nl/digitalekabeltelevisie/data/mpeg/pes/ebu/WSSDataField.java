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


	public WSSDataField(byte[] data, int offset, int len, long pts) {
		super(data, offset, len, pts);
	}
	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.controller.TreeNode#getJTreeNode(int)
	 */
	@Override
	public KVP getJTreeNode(int modus) {
		KVP s = super.getJTreeNode(modus);
		int wss_data_block = getInt(data_block, offset+3, 2, 0xFFFC)>>2;
		s.add(new KVP("wss_data_block",wss_data_block));
		int aspectRatio = getInt(data_block, offset+3, 1, 0xF0)>>4;
		s.add(new KVP("Aspect Ratio",aspectRatio,getAspectRatioString(aspectRatio)));
		int filmBit = getInt(data_block, offset+3, 1, 0x08)>>3;
		s.add(new KVP("Film Bit",filmBit,getFilmBitString(filmBit)));
		int colourCodingBit = getInt(data_block, offset+3, 1, 0x04)>>2;
		s.add(new KVP("Colour coding Bit",colourCodingBit,getColourCodingBitString(colourCodingBit)));
		int helperBit = getInt(data_block, offset+3, 1, 0x02)>>1;
		s.add(new KVP("Helper Bit",helperBit,getHelperBitString(helperBit)));
		int subtitlesWithinTeletextBit = getInt(data_block, offset+4, 1, 0x80)>>7;
		s.add(new KVP("Subtitles within Teletext",subtitlesWithinTeletextBit,getSubtitlesWithinTeletextString(subtitlesWithinTeletextBit)));
		int subtitlingMode = getInt(data_block, offset+4, 1, 0x60)>>5;
		s.add(new KVP("Subtitling mode",subtitlingMode,getSubtitlingModeString(subtitlingMode)));
		int surroundSoundBit = getInt(data_block, offset+4, 1, 0x10)>>4;
		s.add(new KVP("Surround sound bit",surroundSoundBit,getSurroundSoundBitString(surroundSoundBit)));
		int copyrightBit = getInt(data_block, offset+4, 1, 0x08)>>3;
		s.add(new KVP("Copyright bit",copyrightBit,getCopyrightBitString(copyrightBit)));
		int generationBit = getInt(data_block, offset+4, 1, 0x04)>>2;
		s.add(new KVP("Generation bit",generationBit,getGenerationBitString(generationBit)));
		return s;
	}


	public static String getAspectRatioString(int flag) {
        return switch (flag) {
            case 1 -> "4:3 full format";
            case 8 -> "14:9 letterbox centre";
            case 4 -> "14:9 letterbox top";
            case 13 -> "16:9 letterbox centre";
            case 2 -> "16:9 letterbox top";
            case 11 -> "> 16:9 letterbox centre";
            case 7 -> "14:9 full format";
            case 14 -> "16:9 full format";
            default -> "Parity error";
        };
	}

	public static String getFilmBitString(int flag) {
        return switch (flag) {
            case 0 -> "Camera mode";
            case 1 -> "Film mode";
            default -> "Illegal Value";
        };
	}

	public static String getColourCodingBitString(int flag) {
        return switch (flag) {
            case 0 -> "standard coding";
            case 1 -> "Motion Adaptive Colour Plus";
            default -> "Illegal Value";
        };
	}

	public static String getHelperBitString(int flag) {
        return switch (flag) {
            case 0 -> "No helper";
            case 1 -> "Modulated helper";
            default -> "Illegal Value";
        };
	}
	public static String getSubtitlesWithinTeletextString(int flag) {
        return switch (flag) {
            case 0 -> "no subtitles within Teletext";
            case 1 -> "subtitles within Teletext";
            default -> "Illegal Value";
        };
	}

	public static String getSubtitlingModeString(int flag) {
        return switch (flag) {
            case 0 -> "no open subtitles";
            case 1 -> "subtitles out of active image area";
            case 2 -> "subtitles in active image area";
            case 3 -> "reserved";
            default -> "Illegal Value";
        };
	}

	public static String getSurroundSoundBitString(int flag) {
        return switch (flag) {
            case 0 -> "no surround sound information";
            case 1 -> "surround sound mode";
            default -> "Illegal Value";
        };
	}

	public static String getCopyrightBitString(int flag) {
        return switch (flag) {
            case 0 -> "no copyright asserted or status unknown";
            case 1 -> "copyright asserted";
            default -> "Illegal Value";
        };
	}
	public static String getGenerationBitString(int flag) {
        return switch (flag) {
            case 0 -> "copying not restricted";
            case 1 -> "copying restricted";
            default -> "Illegal Value";
        };
	}
}
