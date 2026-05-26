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

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.pes.ac3.AbstractAC3SyncFrame;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

public class AtscEnhancedAC3AudioDescriptor extends AtscDescriptor {

	private final int reserved1;
	private final int bsidFlag;
	private final int mainidFlag;
	private final int asvcFlag;
	private final int mixinfoexists;
	private final int substream1Flag;
	private final int substream2Flag;
	private final int substream3Flag;
	private final int reserved2;
	private final int fullServiceFlag;
	private final int audioServiceType;
	private final int numberOfChannels;
	private final int languageFlag;
	private final int languageFlag2;
	private final int reserved3;
	private final int bsidOrZeroBits;
	private Integer priority;
	private Integer mainid;
	private Integer asvc;
	private Integer substream1;
	private Integer substream2;
	private Integer substream3;
	private String language;
	private String language2;
	private String substream1Lang;
	private String substream2Lang;
	private String substream3Lang;
	private byte[] additionalInfo;

	public AtscEnhancedAC3AudioDescriptor(final byte[] b, final TableSection parent) {
		super(b, parent);
		reserved1 = getInt(b, 2, 1, 0x80) >> 7;
		bsidFlag = getInt(b, 2, 1, 0x40) >> 6;
		mainidFlag = getInt(b, 2, 1, 0x20) >> 5;
		asvcFlag = getInt(b, 2, 1, 0x10) >> 4;
		mixinfoexists = getInt(b, 2, 1, 0x08) >> 3;
		substream1Flag = getInt(b, 2, 1, 0x04) >> 2;
		substream2Flag = getInt(b, 2, 1, 0x02) >> 1;
		substream3Flag = getInt(b, 2, 1, 0x01);
		reserved2 = getInt(b, 3, 1, 0x80) >> 7;
		fullServiceFlag = getInt(b, 3, 1, 0x40) >> 6;
		audioServiceType = getInt(b, 3, 1, 0x38) >> 3;
		numberOfChannels = getInt(b, 3, 1, 0x07);
		languageFlag = getInt(b, 4, 1, 0x80) >> 7;
		languageFlag2 = getInt(b, 4, 1, 0x40) >> 6;
		reserved3 = getInt(b, 4, 1, 0x20) >> 5;
		bsidOrZeroBits = getInt(b, 4, 1, 0x1F);

		int offset = 5;
		final int end = 2 + descriptorLength;
		if (mainidFlag == 1 && offset < end) {
			priority = getInt(b, offset, 1, 0x18) >> 3;
			mainid = getInt(b, offset++, 1, 0x07);
		}
		if (asvcFlag == 1 && offset < end) {
			asvc = getInt(b, offset++, 1, MASK_8BITS);
		}
		if (substream1Flag == 1 && offset < end) {
			substream1 = getInt(b, offset++, 1, MASK_8BITS);
		}
		if (substream2Flag == 1 && offset < end) {
			substream2 = getInt(b, offset++, 1, MASK_8BITS);
		}
		if (substream3Flag == 1 && offset < end) {
			substream3 = getInt(b, offset++, 1, MASK_8BITS);
		}
		if (languageFlag == 1 && offset + 3 <= end) {
			language = getISO8859_1String(b, offset, 3);
			offset += 3;
		}
		if (languageFlag2 == 1 && offset + 3 <= end) {
			language2 = getISO8859_1String(b, offset, 3);
			offset += 3;
		}
		if (substream1Flag == 1 && offset + 3 <= end) {
			substream1Lang = getISO8859_1String(b, offset, 3);
			offset += 3;
		}
		if (substream2Flag == 1 && offset + 3 <= end) {
			substream2Lang = getISO8859_1String(b, offset, 3);
			offset += 3;
		}
		if (substream3Flag == 1 && offset + 3 <= end) {
			substream3Lang = getISO8859_1String(b, offset, 3);
			offset += 3;
		}
		if (offset < end) {
			additionalInfo = getBytes(b, offset, end - offset);
		}
	}

	@Override
	public KVP getJTreeNode(final int modus) {
		KVP t = super.getJTreeNode(modus);
		t.add(new KVP("reserved", reserved1));
		t.add(new KVP("bsid_flag", bsidFlag));
		t.add(new KVP("mainid_flag", mainidFlag));
		t.add(new KVP("asvc_flag", asvcFlag));
		t.add(new KVP("mixinfoexists", mixinfoexists));
		t.add(new KVP("substream1_flag", substream1Flag));
		t.add(new KVP("substream2_flag", substream2Flag));
		t.add(new KVP("substream3_flag", substream3Flag));
		t.add(new KVP("reserved", reserved2));
		t.add(new KVP("full_service_flag", fullServiceFlag, AtscAC3AudioStreamDescriptor.getFullSvcString(fullServiceFlag)));
		t.add(new KVP("audio_service_type", audioServiceType, getAudioServiceTypeString(audioServiceType)));
		t.add(new KVP("number_of_channels", numberOfChannels, getNumberOfChannelsString(numberOfChannels)));
		t.add(new KVP("language_flag", languageFlag));
		t.add(new KVP("language_flag_2", languageFlag2));
		t.add(new KVP("reserved", reserved3));
		if (bsidFlag == 1) {
			t.add(new KVP("bsid", bsidOrZeroBits, AbstractAC3SyncFrame.getBsidString(bsidOrZeroBits)));
		} else {
			t.add(new KVP("zero_bits", bsidOrZeroBits));
		}
		addOptional(t, "priority", priority, priority == null ? null : AtscAC3AudioStreamDescriptor.getPriorityString(priority));
		addOptional(t, "mainid", mainid, null);
		addOptional(t, "asvc", asvc, null);
		addOptional(t, "substream1", substream1, getSubstreamTypeString(substream1));
		addOptional(t, "substream2", substream2, getSubstreamTypeString(substream2));
		addOptional(t, "substream3", substream3, getSubstreamTypeString(substream3));
		addOptionalString(t, "language", language);
		addOptionalString(t, "language_2", language2);
		addOptionalString(t, "substream1_lang", substream1Lang);
		addOptionalString(t, "substream2_lang", substream2Lang);
		addOptionalString(t, "substream3_lang", substream3Lang);
		if (additionalInfo != null) {
			t.add(new KVP("additional_info", additionalInfo));
		}
		return t;
	}

	private static void addOptional(final KVP parent, final String label, final Integer value, final String description) {
		if (value != null) {
			parent.add(new KVP(label, value, description));
		}
	}

	private static void addOptionalString(final KVP parent, final String label, final String value) {
		if (value != null) {
			parent.add(new KVP(label, value));
		}
	}

	public static String getAudioServiceTypeString(final int audioServiceType) {
		return switch (audioServiceType) {
			case 0 -> "complete main";
			case 1 -> "music and effects";
			case 2 -> "visually impaired";
			case 3 -> "hearing impaired";
			case 4 -> "dialogue";
			case 5 -> "commentary";
			case 6 -> "emergency";
			default -> "voiceover or karaoke";
		};
	}

	public static String getNumberOfChannelsString(final int numberOfChannels) {
		return switch (numberOfChannels) {
			case 0 -> "mono";
			case 1 -> "1+1 mode";
			case 2 -> "2-channel stereo";
			case 3 -> "2-channel Dolby Surround encoded stereo";
			case 4 -> "multichannel audio (> 2 channels; <= 3/2 + LFE)";
			case 5 -> "multichannel audio (> 3/2 + LFE)";
			default -> "reserved";
		};
	}

	public static String getSubstreamTypeString(final Integer substream) {
		if (substream == null) {
			return null;
		}
		return nl.digitalekabeltelevisie.data.mpeg.descriptors.AC3Descriptor.getComponentTypeString(substream);
	}

	public int getBsidFlag() {
		return bsidFlag;
	}

	public int getMainidFlag() {
		return mainidFlag;
	}

	public int getAsvcFlag() {
		return asvcFlag;
	}

	public int getMixinfoexists() {
		return mixinfoexists;
	}

	public int getSubstream1Flag() {
		return substream1Flag;
	}

	public int getSubstream2Flag() {
		return substream2Flag;
	}

	public int getSubstream3Flag() {
		return substream3Flag;
	}

	public int getFullServiceFlag() {
		return fullServiceFlag;
	}

	public int getAudioServiceType() {
		return audioServiceType;
	}

	public int getNumberOfChannels() {
		return numberOfChannels;
	}

	public int getLanguageFlag() {
		return languageFlag;
	}

	public int getLanguageFlag2() {
		return languageFlag2;
	}

	public int getBsidOrZeroBits() {
		return bsidOrZeroBits;
	}

	public Integer getPriority() {
		return priority;
	}

	public Integer getMainid() {
		return mainid;
	}

	public Integer getAsvc() {
		return asvc;
	}

	public Integer getSubstream1() {
		return substream1;
	}

	public String getLanguage() {
		return language;
	}

	public String getLanguage2() {
		return language2;
	}

	public String getSubstream1Lang() {
		return substream1Lang;
	}

	public byte[] getAdditionalInfo() {
		return additionalInfo;
	}
}
