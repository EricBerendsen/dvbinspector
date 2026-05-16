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

package nl.digitalekabeltelevisie.data.mpeg.psi.atsc;

import static nl.digitalekabeltelevisie.util.Utils.MASK_10BITS;
import static nl.digitalekabeltelevisie.util.Utils.MASK_16BITS;
import static nl.digitalekabeltelevisie.util.Utils.MASK_24BITS;
import static nl.digitalekabeltelevisie.util.Utils.MASK_32BITS;
import static nl.digitalekabeltelevisie.util.Utils.MASK_6BITS;
import static nl.digitalekabeltelevisie.util.Utils.MASK_8BITS;
import static nl.digitalekabeltelevisie.util.Utils.addListJTree;
import static nl.digitalekabeltelevisie.util.Utils.getInt;
import static nl.digitalekabeltelevisie.util.Utils.getLong;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.PID;
import nl.digitalekabeltelevisie.data.mpeg.PsiSectionData;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.DescriptorFactory;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSectionExtendedSyntax;

public class VCTsection extends TableSectionExtendedSyntax {

	private final boolean cable;
	private final int protocolVersion;
	private final int numChannelsInSection;
	private final List<VirtualChannel> virtualChannels;
	private final int additionalDescriptorsLength;
	private final List<Descriptor> additionalDescriptorList;

	protected VCTsection(final PsiSectionData rawData, final PID parent, final boolean cable) {
		super(rawData, parent);
		this.cable = cable;

		byte[] data = rawData.getData();
		protocolVersion = getInt(data, 8, 1, MASK_8BITS);
		numChannelsInSection = getInt(data, 9, 1, MASK_8BITS);
		virtualChannels = new ArrayList<>();

		int offset = 10;
		for (int i = 0; i < numChannelsInSection; i++) {
			VirtualChannel channel = new VirtualChannel(data, offset, this, cable);
			virtualChannels.add(channel);
			offset += channel.getLength();
		}

		additionalDescriptorsLength = getInt(data, offset, 2, MASK_10BITS);
		additionalDescriptorList = DescriptorFactory.buildDescriptorList(data, offset + 2, additionalDescriptorsLength, this);
	}

	@Override
	public KVP getJTreeNode(final int modus) {
		KVP t = super.getJTreeNode(modus);
		t.add(new KVP("protocol_version", protocolVersion));
		t.add(new KVP("num_channels_in_section", numChannelsInSection));
		addListJTree(t, virtualChannels, modus, "virtual_channels");
		t.add(new KVP("additional_descriptors_length", additionalDescriptorsLength));
		addListJTree(t, additionalDescriptorList, modus, "additional_descriptors");
		return t;
	}

	@Override
	protected String getTableIdExtensionLabel() {
		return "transport_stream_id";
	}

	public boolean isCable() {
		return cable;
	}

	public int getProtocolVersion() {
		return protocolVersion;
	}

	public int getNumChannelsInSection() {
		return numChannelsInSection;
	}

	public List<VirtualChannel> getVirtualChannels() {
		return virtualChannels;
	}

	public int getAdditionalDescriptorsLength() {
		return additionalDescriptorsLength;
	}

	public List<Descriptor> getAdditionalDescriptorList() {
		return additionalDescriptorList;
	}

	public static String getModulationModeString(final int modulationMode) {
		return switch (modulationMode) {
			case 0x00 -> "reserved";
			case 0x01 -> "Analog";
			case 0x02 -> "SCTE mode 1";
			case 0x03 -> "SCTE mode 2";
			case 0x04 -> "ATSC 8-VSB";
			case 0x05 -> "ATSC 16-VSB";
			default -> {
				if ((0x06 <= modulationMode) && (modulationMode <= 0x7F)) {
					yield "reserved for future use by ATSC";
				}
				yield "user private";
			}
		};
	}

	public static String getEtmLocationString(final int etmLocation) {
		return switch (etmLocation) {
			case 0x00 -> "No ETM";
			case 0x01 -> "ETM located in this PTC";
			case 0x02 -> "ETM located in the PTC specified by channel_TSID";
			case 0x03 -> "reserved for future ATSC use";
			default -> "Illegal value";
		};
	}

	public static String getAtscServiceTypeString(final int serviceType) {
		return switch (serviceType) {
			case 0x00 -> "reserved";
			case 0x01 -> "analog television";
			case 0x02 -> "ATSC digital television";
			case 0x03 -> "ATSC audio";
			default -> "defined by other ATSC standards or reserved";
		};
	}

	public static class VirtualChannel implements TreeNode {

		private final String shortName;
		private final int majorChannelNumber;
		private final int minorChannelNumber;
		private final int modulationMode;
		private final long carrierFrequency;
		private final int channelTsid;
		private final int programNumber;
		private final int etmLocation;
		private final int accessControlled;
		private final int hidden;
		private final int pathSelect;
		private final int outOfBand;
		private final int hideGuide;
		private final int serviceType;
		private final int sourceId;
		private final int descriptorsLength;
		private final List<Descriptor> descriptorList;
		private final int length;
		private final boolean cable;

		VirtualChannel(final byte[] data, final int offset, final VCTsection parent, final boolean cable) {
			this.cable = cable;
			shortName = getShortName(data, offset);
			int channelNumbers = getInt(data, offset + 14, 3, MASK_24BITS);
			majorChannelNumber = (channelNumbers >> 10) & MASK_10BITS;
			minorChannelNumber = channelNumbers & MASK_10BITS;
			modulationMode = getInt(data, offset + 17, 1, MASK_8BITS);
			carrierFrequency = getLong(data, offset + 18, 4, MASK_32BITS);
			channelTsid = getInt(data, offset + 22, 2, MASK_16BITS);
			programNumber = getInt(data, offset + 24, 2, MASK_16BITS);

			int flags = getInt(data, offset + 26, 2, MASK_16BITS);
			etmLocation = (flags >> 14) & 0x03;
			accessControlled = (flags >> 13) & 0x01;
			hidden = (flags >> 12) & 0x01;
			pathSelect = cable ? ((flags >> 11) & 0x01) : -1;
			outOfBand = cable ? ((flags >> 10) & 0x01) : -1;
			hideGuide = (flags >> 9) & 0x01;
			serviceType = flags & MASK_6BITS;
			sourceId = getInt(data, offset + 28, 2, MASK_16BITS);
			descriptorsLength = getInt(data, offset + 30, 2, MASK_10BITS);
			descriptorList = DescriptorFactory.buildDescriptorList(data, offset + 32, descriptorsLength, parent);
			length = 32 + descriptorsLength;
		}

		private static String getShortName(final byte[] data, final int offset) {
			String name = new String(data, offset, 14, StandardCharsets.UTF_16BE);
			int nul = name.indexOf('\0');
			if (nul >= 0) {
				return name.substring(0, nul);
			}
			return name;
		}

		@Override
		public KVP getJTreeNode(final int modus) {
			KVP t = new KVP("virtual_channel", getChannelNumberString());
			t.add(new KVP("short_name", shortName));
			t.add(new KVP("major_channel_number", majorChannelNumber));
			t.add(new KVP("minor_channel_number", minorChannelNumber));
			if (cable && isOnePartChannelNumber()) {
				t.add(new KVP("one_part_channel_number", getOnePartChannelNumber()));
			}
			t.add(new KVP("modulation_mode", modulationMode, getModulationModeString(modulationMode)));
			t.add(new KVP("carrier_frequency", carrierFrequency));
			t.add(new KVP("channel_TSID", channelTsid));
			t.add(new KVP("program_number", programNumber));
			t.add(new KVP("ETM_location", etmLocation, getEtmLocationString(etmLocation)));
			t.add(new KVP("access_controlled", accessControlled));
			t.add(new KVP("hidden", hidden));
			if (cable) {
				t.add(new KVP("path_select", pathSelect));
				t.add(new KVP("out_of_band", outOfBand));
			}
			t.add(new KVP("hide_guide", hideGuide));
			t.add(new KVP("service_type", serviceType, getAtscServiceTypeString(serviceType)));
			t.add(new KVP("source_id", sourceId));
			t.add(new KVP("descriptors_length", descriptorsLength));
			addListJTree(t, descriptorList, modus, "descriptors");
			return t;
		}

		public String getChannelNumberString() {
			if (cable && isOnePartChannelNumber()) {
				return Integer.toString(getOnePartChannelNumber());
			}
			return majorChannelNumber + "." + minorChannelNumber;
		}

		public boolean isOnePartChannelNumber() {
			return (majorChannelNumber & 0x3F0) == 0x3F0;
		}

		public int getOnePartChannelNumber() {
			return ((majorChannelNumber & 0x00F) << 10) + minorChannelNumber;
		}

		public String getShortName() {
			return shortName;
		}

		public int getMajorChannelNumber() {
			return majorChannelNumber;
		}

		public int getMinorChannelNumber() {
			return minorChannelNumber;
		}

		public int getModulationMode() {
			return modulationMode;
		}

		public long getCarrierFrequency() {
			return carrierFrequency;
		}

		public int getChannelTsid() {
			return channelTsid;
		}

		public int getProgramNumber() {
			return programNumber;
		}

		public int getEtmLocation() {
			return etmLocation;
		}

		public int getAccessControlled() {
			return accessControlled;
		}

		public int getHidden() {
			return hidden;
		}

		public int getPathSelect() {
			return pathSelect;
		}

		public int getOutOfBand() {
			return outOfBand;
		}

		public int getHideGuide() {
			return hideGuide;
		}

		public int getServiceType() {
			return serviceType;
		}

		public int getSourceId() {
			return sourceId;
		}

		public int getDescriptorsLength() {
			return descriptorsLength;
		}

		public List<Descriptor> getDescriptorList() {
			return descriptorList;
		}

		int getLength() {
			return length;
		}
	}
}
