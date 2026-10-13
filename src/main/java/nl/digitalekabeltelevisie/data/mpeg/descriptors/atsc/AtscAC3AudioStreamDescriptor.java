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
import static nl.digitalekabeltelevisie.util.Utils.getBytes;
import static nl.digitalekabeltelevisie.util.Utils.getISO8859_1String;
import static nl.digitalekabeltelevisie.util.Utils.getInt;

import java.nio.charset.StandardCharsets;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.pes.ac3.AbstractAC3SyncFrame;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

public class AtscAC3AudioStreamDescriptor extends AtscDescriptor {

	private final int sampleRateCode;
	private final int bsid;
	private final int bitRateCode;
	private final int surroundMode;
	private final int bsmod;
	private final int numChannels;
	private final int fullSvc;
	private Integer langcod;
	private Integer langcod2;
	private Integer mainid;
	private Integer priority;
	private Integer reserved;
	private Integer asvcflags;
	private Integer textlen;
	private Integer textCode;
	private String text;
	private Integer languageFlag;
	private Integer languageFlag2;
	private Integer languageFlagsReserved;
	private String language;
	private String language2;
	private byte[] additionalInfo;

	public AtscAC3AudioStreamDescriptor(final byte[] b, final TableSection parent) {
		super(b, parent);
		sampleRateCode = getInt(b, 2, 1, 0xE0) >> 5;
		bsid = getInt(b, 2, 1, 0x1F);
		bitRateCode = getInt(b, 3, 1, 0xFC) >> 2;
		surroundMode = getInt(b, 3, 1, 0x03);
		bsmod = getInt(b, 4, 1, 0xE0) >> 5;
		numChannels = getInt(b, 4, 1, 0x1E) >> 1;
		fullSvc = getInt(b, 4, 1, 0x01);

		int offset = 5;
		final int end = 2 + descriptorLength;
		if (offset < end) {
			langcod = getInt(b, offset++, 1, MASK_8BITS);
		}
		if (numChannels == 0 && offset < end) {
			langcod2 = getInt(b, offset++, 1, MASK_8BITS);
		}
		if (offset < end) {
			if (bsmod < 2) {
				mainid = getInt(b, offset, 1, 0xE0) >> 5;
				priority = getInt(b, offset, 1, 0x18) >> 3;
				reserved = getInt(b, offset++, 1, 0x07);
			} else {
				asvcflags = getInt(b, offset++, 1, MASK_8BITS);
			}
		}
		if (offset < end) {
			textlen = getInt(b, offset, 1, 0xFE) >> 1;
			textCode = getInt(b, offset++, 1, 0x01);
			int textBytes = Math.min(textlen, end - offset);
			if (textCode == 1) {
				text = getISO8859_1String(b, offset, textBytes);
			} else {
				text = new String(b, offset, textBytes, StandardCharsets.UTF_16BE);
			}
			offset += textBytes;
		}
		if (offset < end) {
			languageFlag = getInt(b, offset, 1, 0x80) >> 7;
			languageFlag2 = getInt(b, offset, 1, 0x40) >> 6;
			languageFlagsReserved = getInt(b, offset++, 1, 0x3F);
		}
		if (Integer.valueOf(1).equals(languageFlag) && offset + 3 <= end) {
			language = getISO8859_1String(b, offset, 3);
			offset += 3;
		}
		if (Integer.valueOf(1).equals(languageFlag2) && offset + 3 <= end) {
			language2 = getISO8859_1String(b, offset, 3);
			offset += 3;
		}
		if (offset < end) {
			additionalInfo = getBytes(b, offset, end - offset);
		}
	}

	@Override
	public KVP getJTreeNode(final int modus) {
		KVP t = super.getJTreeNode(modus);
		t.add(new KVP("sample_rate_code", sampleRateCode, getSampleRateCodeString(sampleRateCode)));
		t.add(new KVP("bsid", bsid, AbstractAC3SyncFrame.getBsidString(bsid)));
		t.add(new KVP("bit_rate_code", bitRateCode, getBitRateCodeString(bitRateCode)));
		t.add(new KVP("surround_mode", surroundMode, getSurroundModeString(surroundMode)));
		t.add(new KVP("bsmod", bsmod));
		t.add(new KVP("num_channels", numChannels, getNumChannelsString(numChannels)));
		t.add(new KVP("full_svc", fullSvc, getFullSvcString(fullSvc)));
		addOptional(t, "langcod", langcod);
		addOptional(t, "langcod2", langcod2);
		addOptional(t, "mainid", mainid);
		if (priority != null) {
			t.add(new KVP("priority", priority, getPriorityString(priority)));
		}
		addOptional(t, "reserved", reserved);
		addOptional(t, "asvcflags", asvcflags);
		addOptional(t, "textlen", textlen);
		addOptional(t, "text_code", textCode);
		if (text != null) {
			t.add(new KVP("text", text));
		}
		addOptional(t, "language_flag", languageFlag);
		addOptional(t, "language_flag_2", languageFlag2);
		addOptional(t, "reserved", languageFlagsReserved);
		if (language != null) {
			t.add(new KVP("language", language));
		}
		if (language2 != null) {
			t.add(new KVP("language_2", language2));
		}
		if (additionalInfo != null) {
			t.add(new KVP("additional_info", additionalInfo));
		}
		return t;
	}

	private static void addOptional(final KVP parent, final String label, final Integer value) {
		if (value != null) {
			parent.add(new KVP(label, value));
		}
	}

	public static String getSampleRateCodeString(final int sampleRateCode) {
		return switch (sampleRateCode) {
			case 0 -> "48 kHz";
			case 1 -> "44.1 kHz";
			case 2 -> "32 kHz";
			case 4 -> "48 or 44.1 kHz";
			case 5 -> "48 or 32 kHz";
			case 6 -> "44.1 or 32 kHz";
			case 7 -> "48 or 44.1 or 32 kHz";
			default -> "reserved";
		};
	}

	public static String getBitRateCodeString(final int bitRateCode) {
		int[] rates = { 32, 40, 48, 56, 64, 80, 96, 112, 128, 160, 192, 224, 256, 320, 384, 448, 512, 576, 640 };
		int index = bitRateCode & 0x1F;
		if (index >= rates.length) {
			return "reserved";
		}
		return ((bitRateCode & 0x20) == 0 ? "exact " : "upper limit ") + rates[index] + " kbit/s";
	}

	public static String getSurroundModeString(final int surroundMode) {
		return switch (surroundMode) {
			case 0 -> "not indicated";
			case 1 -> "not Dolby surround encoded";
			case 2 -> "Dolby surround encoded";
			default -> "reserved";
		};
	}

	public static String getNumChannelsString(final int numChannels) {
		return switch (numChannels) {
			case 0 -> "1 + 1";
			case 1 -> "1/0";
			case 2 -> "2/0";
			case 3 -> "3/0";
			case 4 -> "2/1";
			case 5 -> "3/1";
			case 6 -> "2/2";
			case 7 -> "3/2";
			case 8 -> "1 channel";
			case 9 -> "<= 2 channels";
			case 10 -> "<= 3 channels";
			case 11 -> "<= 4 channels";
			case 12 -> "<= 5 channels";
			case 13 -> "<= 6 channels";
			default -> "reserved";
		};
	}

	public static String getFullSvcString(final int fullSvc) {
		return fullSvc == 1 ? "full service" : "partial service";
	}

	public static String getPriorityString(final int priority) {
		return switch (priority) {
			case 1 -> "primary audio";
			case 2 -> "other audio";
			case 3 -> "not specified";
			default -> "reserved";
		};
	}

	public int getSampleRateCode() {
		return sampleRateCode;
	}

	public int getBsid() {
		return bsid;
	}

	public int getBitRateCode() {
		return bitRateCode;
	}

	public int getSurroundMode() {
		return surroundMode;
	}

	public int getBsmod() {
		return bsmod;
	}

	public int getNumChannels() {
		return numChannels;
	}

	public int getFullSvc() {
		return fullSvc;
	}

	public Integer getLangcod() {
		return langcod;
	}

	public Integer getLangcod2() {
		return langcod2;
	}

	public Integer getMainid() {
		return mainid;
	}

	public Integer getPriority() {
		return priority;
	}

	public Integer getAsvcflags() {
		return asvcflags;
	}

	public String getText() {
		return text;
	}

	public String getLanguage() {
		return language;
	}

	public String getLanguage2() {
		return language2;
	}

	public byte[] getAdditionalInfo() {
		return additionalInfo;
	}
}
