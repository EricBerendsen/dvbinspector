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
 *
 */
package nl.digitalekabeltelevisie.data.mpeg.descriptors.extension.mpeg;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.BitSource;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Class responsible for decoding a JPEG-XS video descriptor from a byte array
 * into a {@link DefaultMutableTreeNode}.
 *
 * @author Simon Provost
 */
public class JpegXsVideoDescriptor extends MPEGExtensionDescriptor {

    private final long descriptor_version;
    private final long horizontal_size;
    private final long vertical_size;
    private final long brat;
    private final long frat;
    private final long schar;
    private final long ppih;
    private final long plev;
    private final long max_buffer_size;
    private final long buffer_model_type;
    private final long colour_primaries;
    private final long transfer_characteristics;
    private final long matrix_coefficients;
    private final boolean video_full_range_flag;
    private final boolean still_mode;
    private final boolean mdm_flag;
    private final byte[] private_data;

    public JpegXsVideoDescriptor(byte[] b, int offset, TableSection parent) {
        super(b, offset, parent);

        BitSource reader = new BitSource(b, offset + 3);
        descriptor_version = reader.readBitsLong(8);
        horizontal_size = reader.readBitsLong(16);
        vertical_size = reader.readBitsLong(16);
        brat = reader.readBitsLong(32);
        frat = reader.readBitsLong(32);
        schar = reader.readBitsLong(16);
        ppih = reader.readBitsLong(16);
        plev = reader.readBitsLong(16);
        max_buffer_size = reader.readBitsLong(32);
        buffer_model_type = reader.readBitsLong(8);
        colour_primaries = reader.readBitsLong(8);
        transfer_characteristics = reader.readBitsLong(8);
        matrix_coefficients = reader.readBitsLong(8);
        video_full_range_flag = reader.readBits(1) == 1;
        reader.skiptoByteBoundary();
        still_mode = reader.readBits(1) == 1;
        mdm_flag = reader.readBits(1) == 1;
        reader.skiptoByteBoundary();
        private_data = reader.readBytes(reader.available() / 8);
    }

    @Override
    public DefaultMutableTreeNode getJTreeNode(int modus) {
        final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("JPEG-XS Descriptor"));
        t.add(new DefaultMutableTreeNode(new KVP("descriptor_version", descriptor_version, null)));
        t.add(new DefaultMutableTreeNode(new KVP("horizontal_size", horizontal_size, null)));
        t.add(new DefaultMutableTreeNode(new KVP("vertical_size", vertical_size, null)));
        t.add(new DefaultMutableTreeNode(new KVP("brat", brat, "Bit Rate (MBits/s)")));
        t.add(buildFratNode(frat));
        t.add(buildScharNode(schar));
        t.add(buildPpihNode(ppih));
        t.add(buildPlevNode(plev));
        t.add(new DefaultMutableTreeNode(new KVP("max_buffer_size", max_buffer_size, "Maximum buffer size (Mbits/s)")));
        t.add(new DefaultMutableTreeNode(new KVP("buffer_model_type", buffer_model_type, null)));
        t.add(new DefaultMutableTreeNode(new KVP("colour_primaries", colour_primaries, null)));
        t.add(new DefaultMutableTreeNode(new KVP("transfer_characteristics", transfer_characteristics, null)));
        t.add(new DefaultMutableTreeNode(new KVP("matrix_coefficients", matrix_coefficients, null)));
        t.add(new DefaultMutableTreeNode(new KVP("video_full_range_flag", video_full_range_flag, null)));
        t.add(new DefaultMutableTreeNode(new KVP("still_mode", still_mode, null)));
        t.add(new DefaultMutableTreeNode(new KVP("mdm_flag", mdm_flag, null)));
        t.add(new DefaultMutableTreeNode(new KVP("private_data", private_data, null)));
        final DefaultMutableTreeNode parentNode = super.getJTreeNode(modus);
        parentNode.add(t);
        return parentNode;
    }

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
