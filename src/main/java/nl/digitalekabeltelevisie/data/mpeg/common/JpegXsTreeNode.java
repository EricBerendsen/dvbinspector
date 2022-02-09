/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  (C) RIEDEL Communications Canada, Inc. All rights reserved
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
 */

package nl.digitalekabeltelevisie.data.mpeg.common;

import nl.digitalekabeltelevisie.controller.KVP;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Contains common code used when building a {@link DefaultMutableTreeNode} with
 * information related to JPEG-XS header.
 *
 * @author Simon Provost
 */
public class JpegXsTreeNode {

    public static DefaultMutableTreeNode buildFratNode(long frat) {
        final DefaultMutableTreeNode fratNode = new DefaultMutableTreeNode(new KVP("frat", frat, "Frame rate"));

        final long interlaceMode = (frat >> 30) & 0x3;
        final String interlaceText = interlaceMode == 0 ? "Progressive frame (frame contains one full-height picture)"
                : interlaceMode == 1 ? "Interlaced frame (picture is first video field)"
                : interlaceMode == 2 ? "Interlaced frame (picture is second video field)"
                : "Reserved";
        fratNode.add(new DefaultMutableTreeNode(new KVP("interlace_mode", interlaceMode, interlaceText)));

        final long framerateDenominator = (frat >> 24) & 0x3F;
        final String denominatorText = framerateDenominator == 1 ? "Value=1.000"
                : framerateDenominator == 2 ? "Value=1.001"
                : "Reserved";
        fratNode.add(new DefaultMutableTreeNode(new KVP("framerate_denominator", framerateDenominator, denominatorText)));

        final long framerateNumerator = frat & 0xFFFF;
        final String numeratorText = "Frames/sec";
        fratNode.add(new DefaultMutableTreeNode(new KVP("framerate_numerator", framerateNumerator, numeratorText)));

        return fratNode;
    }

    public static DefaultMutableTreeNode buildScharNode(long schar) {
        final DefaultMutableTreeNode scharNode = new DefaultMutableTreeNode(new KVP("schar", schar, "Sampling characteristics"));

        final long validFlag = (schar >> 15) & 0x1;
        final String validText = validFlag == 0 ? "Invalid" : "Valid";
        scharNode.add(new DefaultMutableTreeNode(new KVP("valid_flag", validFlag, validText)));

        final long sampleBitDepth = (schar >> 4) & 0xF;
        scharNode.add(new DefaultMutableTreeNode(new KVP("sample_bitdepth", sampleBitDepth, null)));

        final long samplingStructure = schar & 0xF;
        final String samplingStructureText = samplingStructureToString(samplingStructure);
        scharNode.add(new DefaultMutableTreeNode(new KVP("sampling_structure", samplingStructure, samplingStructureText)));

        return scharNode;
    }

    public static DefaultMutableTreeNode buildPpihNode(long ppih) {
        return new DefaultMutableTreeNode(new KVP("ppih", ppih, ppihToString(ppih)));
    }

    public static DefaultMutableTreeNode buildPlevNode(long plev) {
        DefaultMutableTreeNode plevNode = new DefaultMutableTreeNode(new KVP("plev", plev, null));

        final long level = (plev >> 8) & 0xFF;
        final String levelText = getLevelText(level);
        plevNode.add(new DefaultMutableTreeNode(new KVP("level", level, levelText)));

        final long subLevel = plev & 0xFF;
        final String subLevelText = getSubLevelText(subLevel);
        plevNode.add(new DefaultMutableTreeNode(new KVP("sub_level", subLevel, subLevelText)));

        return plevNode;
    }

    private static String samplingStructureToString(long samplingStructure) {
        return switch ((int) samplingStructure) {
            case 0 -> "4:2:2 (YCbCr)";
            case 1 -> "4:4:4 (YCbCr)";
            case 2 -> "4:4:4 (RGB)";
            case 4 -> "4:2:2:4 (YCbCrAux)";
            case 5 -> "4:4:4:4 (YCbCrAux)";
            case 6 -> "4:4:4:4 (RGBAux)";
            default -> "Reserved";
        };
    }

    private static String ppihToString(long ppih) {
        return switch ((int) ppih) {
            case 0x0000 -> "Unrestricted";
            case 0x1500 -> "Light 422.10";
            case 0x1A00 -> "Light 444.12";
            case 0x2500 -> "Light-Subline 422.10";
            case 0x2540 -> "Main 422.10";
            case 0x3A40 -> "Main 444.12";
            case 0x3E40 -> "Main 4444.12";
            case 0x4A40 -> "High 444.12";
            case 0x4E40 -> "High 4444.12";
            default -> "Reserved";
        };
    }

    private static String getLevelText(long level) {
        return switch ((int) level) {
            case 0b0000_0000 -> "Unrestricted";
            case 0b0001_0000 -> "2k-1";
            case 0b0010_0000 -> "4k-1";
            case 0b0010_0100 -> "4k-2";
            case 0b0010_1000 -> "4k-3";
            case 0b0011_0000 -> "8k-1";
            case 0b0011_0100 -> "8k-2";
            case 0b0011_1000 -> "8k-3";
            case 0b0100_0000 -> "10k-1";
            default -> "Reserved";
        };
    }

    private static String getSubLevelText(long subLevel) {
        return switch ((int) subLevel) {
            case 0b0000_0000 -> "Unrestricted";
            case 0b1000_0000 -> "Full";
            case 0b0001_0000 -> "Sublev12bpp";
            case 0b0000_1100 -> "Sublev9bpp";
            case 0b0000_1000 -> "Sublev6bpp";
            case 0b0000_0100 -> "Sublev3bpp";
            default -> "Reserved";
        };
    }
}
