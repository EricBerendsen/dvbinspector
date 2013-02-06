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

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.util.Utils;

/**
 * @author Eric Berendsen
 */
public class CompatibilityDescriptor implements TreeNode {

	private final int						compatibilityDescriptorLength;
	private int						compatibilityDescriptorCount;

	private final List<CompDescriptor>	descriptors	= new ArrayList<CompDescriptor>();

	public static class CompDescriptor implements TreeNode {

		private final int	descriptorType;
		private final int	descriptorLength;
		private final int	specifierType;
		private final int	specifierData;		// oui
		private final int	model;
		private final int	version;
		private final int	subDescriptorCount;
		private List<SubDescriptor>	subDescriptors	= new ArrayList<SubDescriptor>();

		public CompDescriptor(final int descriptorType, final int descriptorLength, final int specifierType, final int specifierData,
				final int model, final int version, final int subDescriptorCount, final List<SubDescriptor> subDescriptors) {
			super();
			this.descriptorType = descriptorType;
			this.descriptorLength = descriptorLength;
			this.specifierType = specifierType;
			this.specifierData = specifierData;
			this.model = model;
			this.version = version;
			this.subDescriptorCount = subDescriptorCount;
			this.subDescriptors = subDescriptors;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see nl.digitalekabeltelevisie.controller.TreeNode#getJTreeNode(int)
		 */
		public DefaultMutableTreeNode getJTreeNode(final int modus) {
			final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("Descriptor: "
					+ getDescriptorTypeString(descriptorType)));

			t.add(new DefaultMutableTreeNode(new KVP("descriptorType", descriptorType,
					getDescriptorTypeString(descriptorType))));
			t.add(new DefaultMutableTreeNode(new KVP("descriptorLength", descriptorLength, null)));
			t.add(new DefaultMutableTreeNode(new KVP("specifierType", specifierType, getSpecifierTypeString(specifierType))));
			t.add(new DefaultMutableTreeNode(new KVP("specifierData", specifierData, specifierType==1?Utils.getOUIString(specifierData):null)));
			t.add(new DefaultMutableTreeNode(new KVP("model", model, null)));
			t.add(new DefaultMutableTreeNode(new KVP("version", version, null)));
			t.add(new DefaultMutableTreeNode(new KVP("sub_descriptor_count", subDescriptorCount, null)));
			Utils.addListJTree(t, subDescriptors, modus, "SubDescriptors");
			return t;
		}

		private String getSpecifierTypeString(final int specifierType2) {
			if(specifierType2==0){
				return "ISO/IEC 13818-6 reserved";
			}else if(specifierType2==1){
				return "IEEE OUI.";
			}else if((specifierType2>=2)&&(specifierType2<=0x7f)){
				return "ISO/IEC 13818-6 reserved";
			}
			return "User Defined";
		}

	}


	public static class SubDescriptor implements TreeNode {

		/**
		 * @param subDescriptorType
		 * @param subDescriptorLength
		 * @param additionalInformation
		 */
		public SubDescriptor(final int subDescriptorType, final int subDescriptorLength,
				final byte[] additionalInformation) {
			super();
			this.subDescriptorType = subDescriptorType;
			this.subDescriptorLength = subDescriptorLength;
			this.additionalInformation = additionalInformation;
		}


		private final int	subDescriptorType;
		private final int	subDescriptorLength;
		private final byte [] additionalInformation;


		public DefaultMutableTreeNode getJTreeNode(final int modus) {
			final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("SubDescriptor"));

			t.add(new DefaultMutableTreeNode(new KVP("subDescriptorType", subDescriptorType,null)));
			t.add(new DefaultMutableTreeNode(new KVP("subDescriptorLength", subDescriptorLength, null)));
			t.add(new DefaultMutableTreeNode(new KVP("additionalInformation", additionalInformation, null)));
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
				final int descriptorType = Utils.getInt(data, t, 1, Utils.MASK_8BITS);
				final int descriptorLength = Utils.getInt(data, t + 1, 1, Utils.MASK_8BITS);
				final int specifierType = Utils.getInt(data, t + 2, 1, Utils.MASK_8BITS); // should always be 1 for UNT
				final int specifierData = Utils.getInt(data, t + 3, 3, Utils.MASK_24BITS); // oui
				final int model = Utils.getInt(data, t + 6, 2, Utils.MASK_16BITS);
				final int version = Utils.getInt(data, t + 8, 2, Utils.MASK_16BITS);
				final int subDescriptorCount = Utils.getInt(data, t + 10, 1, Utils.MASK_8BITS);

				t +=11;
				//  subDescriptors
				final List<SubDescriptor>	subDescriptors	= new ArrayList<SubDescriptor>();
				for (int j = 0; j < subDescriptorCount; j++) {
					final int subDescriptorType = Utils.getInt(data, t, 1, Utils.MASK_8BITS);
					final int subDescriptorLength = Utils.getInt(data, t + 1, 1, Utils.MASK_8BITS);
					final byte [] additionalInformation = Utils.copyOfRange(data,  t + 2,  t + 2+subDescriptorLength);
					final SubDescriptor subDesc= new SubDescriptor(subDescriptorType, subDescriptorLength, additionalInformation);
					subDescriptors.add(subDesc);
					t += 2 + subDescriptorLength;
				}

				descriptors.add(new CompDescriptor(descriptorType, descriptorLength, specifierType, specifierData,
						model, version, subDescriptorCount,subDescriptors));

			}

		}
	}

	public DefaultMutableTreeNode getJTreeNode(final int modus) {

		DefaultMutableTreeNode t;
		if (compatibilityDescriptorLength > 0) {
			t= new DefaultMutableTreeNode(new KVP("CompatibilityDescriptor"));
			t.add(new DefaultMutableTreeNode(new KVP("compatibily_descriptor_length", compatibilityDescriptorLength,
					null)));
			t.add(new DefaultMutableTreeNode(
					new KVP("compatibily_descriptor_count", compatibilityDescriptorCount, null)));
			Utils.addListJTree(t, descriptors, modus, "descriptors");
		}else{
			t = new DefaultMutableTreeNode(new KVP("compatibily_descriptor_length", compatibilityDescriptorLength,
					null));
		}
		return t;
	}

	public static String getDescriptorTypeString(final int t) {

		if ((t >= 0x03) && (t <= 0x3f)) {
			return "ISO/IEC 13818-6 reserved";
		}
		if ((t >= 0x40) && (t <= 0x7f)) {
			return "DVB reserved";
		}

		switch (t) {
		case 0x00:
			return "Pad descriptor";
		case 0x01:
			return "System Hardware descriptor";
		case 0x02:
			return "System Software descriptor";

		default:
			return "User defined";
		}
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
