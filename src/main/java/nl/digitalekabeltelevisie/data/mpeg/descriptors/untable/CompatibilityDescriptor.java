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

package nl.digitalekabeltelevisie.data.mpeg.descriptors.untable;

import static java.util.Arrays.copyOfRange;
import static nl.digitalekabeltelevisie.util.Utils.addListJTree;

import java.util.ArrayList;
import java.util.List;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.util.Utils;

/**
 * @author Eric Berendsen
 */
public class CompatibilityDescriptor implements TreeNode {

	private final int compatibilityDescriptorLength;
	private int compatibilityDescriptorCount;

	private final List<CompDescriptor> descriptors = new ArrayList<>();

	public static record CompDescriptor(int descriptorType, int descriptorLength, int specifierType, int specifierData, int model, int version,
			int subDescriptorCount, List<SubDescriptor> subDescriptors) implements TreeNode {

		@Override
		public KVP getJTreeNode(final int modus) {
			final KVP t = new KVP("Descriptor: " + getDescriptorTypeString(descriptorType));

			t.add(new KVP("descriptorType", descriptorType, getDescriptorTypeString(descriptorType)));
			t.add(new KVP("descriptorLength", descriptorLength));
			t.add(new KVP("specifierType", specifierType, getSpecifierTypeString(specifierType)));
			t.add(new KVP("specifierData", specifierData, specifierType == 1 ? Utils.getOUIString(specifierData) : null));
			t.add(new KVP("model", model));
			t.add(new KVP("version", version));
			t.add(new KVP("sub_descriptor_count", subDescriptorCount));
			Utils.addListJTree(t, subDescriptors, modus, "SubDescriptors");
			return t;
		}

		private static String getSpecifierTypeString(final int specifierType2) {
			if (specifierType2 == 0) {
				return "ISO/IEC 13818-6 reserved";
			} else if (specifierType2 == 1) {
				return "IEEE OUI.";
			} else if ((specifierType2 >= 2) && (specifierType2 <= 0x7f)) {
				return "ISO/IEC 13818-6 reserved";
			}
			return "User Defined";
		}

	}

	public record SubDescriptor(int subDescriptorType, int subDescriptorLength,
			byte[] additionalInformation) implements TreeNode {


		@Override
		public KVP getJTreeNode(int modus) {
			final KVP t = new KVP("SubDescriptor");

			t.add(new KVP("subDescriptorType", subDescriptorType));
			t.add(new KVP("subDescriptorLength", subDescriptorLength));
			t.add(new KVP("additionalInformation", additionalInformation));
			return t;
		}
	}

	/**
	 *
	 */
	public CompatibilityDescriptor(final byte[] data, final int offset) {
		compatibilityDescriptorLength = Utils.getInt(data, offset, 2, Utils.MASK_16BITS);
		if (compatibilityDescriptorLength > 0) {
			compatibilityDescriptorCount = Utils.getInt(data, offset + 2, 2, Utils.MASK_16BITS);
			int t = offset + 4;
			for (int i = 0; i < compatibilityDescriptorCount; i++) {
				int descriptorType = Utils.getInt(data, t, 1, Utils.MASK_8BITS);
				int descriptorLength = Utils.getInt(data, t + 1, 1, Utils.MASK_8BITS);
				int specifierType = Utils.getInt(data, t + 2, 1, Utils.MASK_8BITS); // should always be 1 for UNT
				int specifierData = Utils.getInt(data, t + 3, 3, Utils.MASK_24BITS); // oui
				int model = Utils.getInt(data, t + 6, 2, Utils.MASK_16BITS);
				int version = Utils.getInt(data, t + 8, 2, Utils.MASK_16BITS);
				int subDescriptorCount = Utils.getInt(data, t + 10, 1, Utils.MASK_8BITS);

				t +=11;
				//  subDescriptors
				List<SubDescriptor>	subDescriptors	= new ArrayList<>();
				for (int j = 0; j < subDescriptorCount; j++) {
					int subDescriptorType = Utils.getInt(data, t, 1, Utils.MASK_8BITS);
					int subDescriptorLength = Utils.getInt(data, t + 1, 1, Utils.MASK_8BITS);
					byte [] additionalInformation = copyOfRange(data, t + 2, t + 2+subDescriptorLength);
					SubDescriptor subDesc= new SubDescriptor(subDescriptorType, subDescriptorLength, additionalInformation);
					subDescriptors.add(subDesc);
					t += 2 + subDescriptorLength;
				}

				descriptors.add(new CompDescriptor(descriptorType, descriptorLength, specifierType, specifierData,
						model, version, subDescriptorCount,subDescriptors));

			}

		}
	}

	@Override
	public KVP getJTreeNode(int modus) {

		if (compatibilityDescriptorLength > 0) {
			KVP t = new KVP("CompatibilityDescriptor");
			t.add(new KVP("compatibily_descriptor_length", compatibilityDescriptorLength));
			t.add(new KVP("compatibily_descriptor_count", compatibilityDescriptorCount));
			addListJTree(t, descriptors, modus, "descriptors");
			return t;
		}
		return new KVP("compatibily_descriptor_length", compatibilityDescriptorLength);
	}

	public static String getDescriptorTypeString(int t) {

		if ((t >= 0x03) && (t <= 0x3f)) {
			return "ISO/IEC 13818-6 reserved";
		}
		if ((t >= 0x40) && (t <= 0x7f)) {
			return "DVB reserved";
		}

		return switch (t) {
		case 0x00 -> "Pad descriptor";
		case 0x01 -> "System Hardware descriptor";
		case 0x02 -> "System Software descriptor";
		default -> "User defined";
		};
	}

	public int getCompatibilityDescriptorCount() {
		return compatibilityDescriptorCount;
	}

	public int getCompatibilityDescriptorLength() {
		return compatibilityDescriptorLength;
	}

	public List<CompDescriptor> getDescriptors() {
		return descriptors;
	}

}
