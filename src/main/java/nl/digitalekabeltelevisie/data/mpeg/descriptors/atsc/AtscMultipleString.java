/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2026 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.descriptors.atsc;

import static nl.digitalekabeltelevisie.util.Utils.MASK_8BITS;
import static nl.digitalekabeltelevisie.util.Utils.addListJTree;
import static nl.digitalekabeltelevisie.util.Utils.getISO8859_1String;
import static nl.digitalekabeltelevisie.util.Utils.getInt;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.util.Utils;

public class AtscMultipleString implements TreeNode {

	private final int numberStrings;
	private final List<StringEntry> strings = new ArrayList<>();

	public AtscMultipleString(final byte[] data, final int offset, final int length) {
		if (length <= 0) {
			numberStrings = 0;
			return;
		}
		numberStrings = getInt(data, offset, 1, MASK_8BITS);
		int localOffset = offset + 1;
		int end = offset + length;
		for (int i = 0; (i < numberStrings) && (localOffset < end); i++) {
			StringEntry stringEntry = new StringEntry(data, localOffset, end);
			strings.add(stringEntry);
			localOffset += stringEntry.getLength();
		}
	}

	@Override
	public KVP getJTreeNode(final int modus) {
		KVP t = new KVP("multiple_string_structure", getText());
		t.add(new KVP("number_strings", numberStrings));
		addListJTree(t, strings, modus, "strings");
		return t;
	}

	public String getText() {
		if (strings.isEmpty()) {
			return "";
		}
		return strings.getFirst().getText();
	}

	public int getNumberStrings() {
		return numberStrings;
	}

	public List<StringEntry> getStrings() {
		return strings;
	}

	public static String getCompressionTypeString(final int compressionType) {
		return switch (compressionType) {
			case 0x00 -> "No compression";
			case 0x01 -> "Huffman coding using standard encode/decode tables C.4 and C.5";
			case 0x02 -> "Huffman coding using standard encode/decode tables C.6 and C.7";
			default -> {
				if ((compressionType >= 0x03) && (compressionType <= 0xAF)) {
					yield "reserved";
				}
				yield "used in other systems";
			}
		};
	}

	public static String getModeString(final int mode) {
		if (isUnicodeRangeMode(mode)) {
			int low = mode << 8;
			int high = low | 0xFF;
			return "Select Unicode Code Range " + Utils.toHexString(low, 4) + " - " + Utils.toHexString(high, 4);
		}
		return switch (mode) {
			case 0x3E -> "Select Standard Compression Scheme for Unicode (SCSU)";
			case 0x3F -> "Select Unicode, UTF-16 Form";
			case 0x40, 0x41 -> "assigned to ATSC standard for Taiwan";
			case 0x48 -> "assigned to ATSC standard for South Korea";
			case 0xFF -> "not applicable";
			default -> {
				if ((mode >= 0xE0) && (mode <= 0xFE)) {
					yield "used in other systems";
				}
				yield "reserved for future ATSC use";
			}
		};
	}

	private static boolean isUnicodeRangeMode(final int mode) {
		return (mode <= 0x06) || ((0x09 <= mode) && (mode <= 0x10))
				|| ((0x20 <= mode) && (mode <= 0x27)) || ((0x30 <= mode) && (mode <= 0x33));
	}

	public static class StringEntry implements TreeNode {

		private final String iso639LanguageCode;
		private final int numberSegments;
		private final List<Segment> segments = new ArrayList<>();
		private final int length;

		StringEntry(final byte[] data, final int offset, final int end) {
			iso639LanguageCode = getISO8859_1String(data, offset, 3);
			numberSegments = getInt(data, offset + 3, 1, MASK_8BITS);
			int localOffset = offset + 4;
			for (int i = 0; (i < numberSegments) && (localOffset < end); i++) {
				Segment segment = new Segment(data, localOffset, end);
				segments.add(segment);
				localOffset += segment.getLength();
			}
			length = localOffset - offset;
		}

		@Override
		public KVP getJTreeNode(final int modus) {
			KVP t = new KVP("string", getText());
			t.add(new KVP("ISO_639_language_code", iso639LanguageCode));
			t.add(new KVP("number_segments", numberSegments));
			addListJTree(t, segments, modus, "segments");
			return t;
		}

		public String getText() {
			StringBuilder text = new StringBuilder();
			for (Segment segment : segments) {
				text.append(segment.getText());
			}
			return text.toString();
		}

		public String getIso639LanguageCode() {
			return iso639LanguageCode;
		}

		public int getNumberSegments() {
			return numberSegments;
		}

		public List<Segment> getSegments() {
			return segments;
		}

		int getLength() {
			return length;
		}
	}

	public static class Segment implements TreeNode {

		private final int compressionType;
		private final int mode;
		private final int numberBytes;
		private final byte[] compressedStringBytes;
		private final String text;
		private final int length;

		Segment(final byte[] data, final int offset, final int end) {
			compressionType = getInt(data, offset, 1, MASK_8BITS);
			mode = getInt(data, offset + 1, 1, MASK_8BITS);
			numberBytes = getInt(data, offset + 2, 1, MASK_8BITS);
			int safeNumberBytes = Math.min(numberBytes, Math.max(0, end - offset - 3));
			compressedStringBytes = Utils.getBytes(data, offset + 3, safeNumberBytes);
			text = decodeText(compressionType, mode, compressedStringBytes);
			length = 3 + safeNumberBytes;
		}

		@Override
		public KVP getJTreeNode(final int modus) {
			KVP t = new KVP("segment", text);
			t.add(new KVP("compression_type", compressionType, getCompressionTypeString(compressionType)));
			t.add(new KVP("mode", mode, getModeString(mode)));
			t.add(new KVP("number_bytes", numberBytes));
			t.add(new KVP("compressed_string_bytes", compressedStringBytes));
			return t;
		}

		private static String decodeText(final int compressionType, final int mode, final byte[] bytes) {
			if (compressionType != 0x00) {
				return "";
			}
			if (isUnicodeRangeMode(mode)) {
				StringBuilder decoded = new StringBuilder();
				for (byte b : bytes) {
					decoded.append((char) ((mode << 8) | (b & 0xFF)));
				}
				return decoded.toString();
			}
			if (mode == 0x3F) {
				return new String(bytes, StandardCharsets.UTF_16BE);
			}
			return "";
		}

		public int getCompressionType() {
			return compressionType;
		}

		public int getMode() {
			return mode;
		}

		public int getNumberBytes() {
			return numberBytes;
		}

		public byte[] getCompressedStringBytes() {
			return compressedStringBytes;
		}

		public String getText() {
			return text;
		}

		int getLength() {
			return length;
		}
	}
}
