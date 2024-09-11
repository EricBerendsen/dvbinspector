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
	private final int zero_bits;
	private int x_c0;
	private int y_c0;
	private int x_c1;
	private int y_c1;
	private int x_c2;
	private int y_c2;
	private int x_wp;
	private int y_wp;
	private long l_max;
	private long l_min;
	private int maxcll;
	private int maxfall;

    public JpegXsVideoDescriptor(byte[] b, TableSection parent) {
        super(b, parent);

        BitSource reader = new BitSource(b, 3);
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
        
        zero_bits = reader.readBits(6);
        if(mdm_flag) {
        	
        	x_c0 = reader.readBits(16);
        	y_c0 = reader.readBits(16);
        	x_c1 = reader.readBits(16);
        	y_c1 = reader.readBits(16);
        	x_c2 = reader.readBits(16);
        	y_c2 = reader.readBits(16);
        	x_wp = reader.readBits(16);
        	y_wp = reader.readBits(16);
        	l_max = reader.readBitsLong(32);
        	l_min= reader.readBitsLong(32);
        	maxcll = reader.readBits(16);
        	maxfall = reader.readBits(16);
        	
        }
        
        
        reader.skiptoByteBoundary();
        private_data = reader.readBytes(reader.available() / 8);
    }

    @Override
    public KVP getJTreeNode(int modus) {
		final KVP t = super.getJTreeNode(modus);

        t.add(new KVP("descriptor_version", descriptor_version));
        t.add(new KVP("horizontal_size", horizontal_size));
        t.add(new KVP("vertical_size", vertical_size));
        t.add(new KVP("brat", brat, "Bit Rate (MBits/s)"));
        t.add(buildFratNode(frat));
        t.add(buildScharNode(schar));
        t.add(buildPpihNode(ppih));
        t.add(buildPlevNode(plev));
        t.add(new KVP("max_buffer_size", max_buffer_size, "Maximum buffer size (Mbits/s)"));
        t.add(new KVP("buffer_model_type", buffer_model_type));
        t.add(new KVP("colour_primaries", colour_primaries));
        t.add(new KVP("transfer_characteristics", transfer_characteristics));
        t.add(new KVP("matrix_coefficients", matrix_coefficients));
        t.add(new KVP("video_full_range_flag", video_full_range_flag, null));
        t.add(new KVP("still_mode", still_mode, null));
        t.add(new KVP("mdm_flag", mdm_flag, null));
        t.add(new KVP("zero_bits", zero_bits));
 
        if(mdm_flag) {
            t.add(new KVP("X_c0", x_c0));
            t.add(new KVP("Y_c0", y_c0));
            t.add(new KVP("X_c1", x_c1));
            t.add(new KVP("Y_c1", y_c1));
            t.add(new KVP("X_c2", x_c2));
            t.add(new KVP("Y_c2", y_c2));
            t.add(new KVP("X_wp", x_wp));
            t.add(new KVP("Y_wp", y_wp));
            t.add(new KVP("L_max", l_max));
            t.add(new KVP("L_min", l_min));
            t.add(new KVP("MaxCLL", maxcll));
            t.add(new KVP("MaxFALL", maxfall));
        	
        }
        	 
        
        t.add(new KVP("private_data", private_data));
        return t;
    }

    public static KVP buildFratNode(long frat) {
        final KVP fratNode = new KVP("frat", frat, "Frame rate");

        final long interlaceMode = (frat >> 30) & 0x3;
        final String interlaceText = interlaceMode == 0 ? "Progressive frame (frame contains one full-height picture)"
                : interlaceMode == 1 ? "Interlaced frame (picture is first video field)"
                : interlaceMode == 2 ? "Interlaced frame (picture is second video field)"
                : "Reserved";
        fratNode.add(new KVP("interlace_mode", interlaceMode, interlaceText));

        final long framerateDenominator = (frat >> 24) & 0x3F;
        final String denominatorText = framerateDenominator == 1 ? "Value=1.000"
                : framerateDenominator == 2 ? "Value=1.001"
                : "Reserved";
        fratNode.add(new KVP("framerate_denominator", framerateDenominator, denominatorText));

        final long framerateNumerator = frat & 0xFFFF;
        final String numeratorText = "Frames/sec";
        fratNode.add(new KVP("framerate_numerator", framerateNumerator, numeratorText));

        return fratNode;
    }

    public static KVP buildScharNode(long schar) {
        final KVP scharNode = new KVP("schar", schar, "Sampling characteristics");

        final long validFlag = (schar >> 15) & 0x1;
        final String validText = validFlag == 0 ? "Invalid" : "Valid";
        scharNode.add(new KVP("valid_flag", validFlag, validText));

        final long sampleBitDepth = (schar >> 4) & 0xF;
        scharNode.add(new KVP("sample_bitdepth", sampleBitDepth));

        final long samplingStructure = schar & 0xF;
        final String samplingStructureText = samplingStructureToString(samplingStructure);
        scharNode.add(new KVP("sampling_structure", samplingStructure, samplingStructureText));

        return scharNode;
    }

    public static KVP buildPpihNode(long ppih) {
        return new KVP("ppih", ppih, ppihToString(ppih));
    }

    public static KVP buildPlevNode(long plev) {
    	KVP plevNode = new KVP("plev", plev);

        final long level = (plev >> 8) & 0xFF;
        final String levelText = getLevelText(level);
        plevNode.add(new KVP("level", level, levelText));

        final long subLevel = plev & 0xFF;
        final String subLevelText = getSubLevelText(subLevel);
        plevNode.add(new KVP("sub_level", subLevel, subLevelText));

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
