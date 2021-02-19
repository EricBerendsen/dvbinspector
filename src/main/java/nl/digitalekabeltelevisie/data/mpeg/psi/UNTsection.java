/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2020 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

 *
 *
 */

package nl.digitalekabeltelevisie.data.mpeg.psi;

import static nl.digitalekabeltelevisie.util.Utils.*;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.PID;
import nl.digitalekabeltelevisie.data.mpeg.PsiSectionData;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.DescriptorFactory;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.untable.CompatibilityDescriptor;
import nl.digitalekabeltelevisie.util.Utils;

/**
 * @author Eric Berendsen Represents a section of the Updata Notification Table.
 * @see PsiSectionData#isUNTSection(int) based on TS 102 006, § 9.4
 */
public class UNTsection extends TableSectionExtendedSyntax {

	private final List<PlatformLoop>	platformLoopList	= new ArrayList<>();
	private final int					action_type;
	private final int					oui_hash;
	private final int					oui;
	private final int					processing_order;
	private final int					common_descriptor_loop_length;

	private final List<Descriptor>	common_descriptor_loop;

	public static class PlatformLoop implements TreeNode {

		private CompatibilityDescriptor	compatibilityDescriptor;
		private int						platform_loop_length;
		private List<TargetLoop>		target_loop;

		public DefaultMutableTreeNode getJTreeNode(final int modus) {

			final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("platform"));

			t.add(compatibilityDescriptor.getJTreeNode(modus));

			t.add(new DefaultMutableTreeNode(new KVP("platform_loop_length", platform_loop_length, null)));
			Utils.addListJTree(t, target_loop, modus, "target_loop");

			return t;
		}

		public CompatibilityDescriptor getCompatibilityDescriptor() {
			return compatibilityDescriptor;
		}

		public void setCompatibilityDescriptor(final CompatibilityDescriptor compatibilityDescriptor) {
			this.compatibilityDescriptor = compatibilityDescriptor;
		}

		public int getPlatform_loop_length() {
			return platform_loop_length;
		}

		public void setPlatform_loop_length(final int platform_loop_length) {
			this.platform_loop_length = platform_loop_length;
		}

		public List<TargetLoop> getTarget_loop() {
			return target_loop;
		}

		public void setTarget_loop(final List<TargetLoop> target_loop) {
			this.target_loop = target_loop;
		}

	}

	public static class TargetLoop implements TreeNode {

		private int			target_descriptor_loop_length;
		private int			operational_descriptor_loop_length;

		private List<Descriptor>	target_descriptor_loop;
		private List<Descriptor>	operational_descriptor_loop;

		public DefaultMutableTreeNode getJTreeNode(final int modus) {

			final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("target"));

			t.add(new DefaultMutableTreeNode(new KVP("target_descriptor_loop_length", target_descriptor_loop_length,
					null)));
			Utils.addListJTree(t, target_descriptor_loop, modus, "target_descriptor_loop");
			t.add(new DefaultMutableTreeNode(new KVP("operational_descriptor_loop_length",
					operational_descriptor_loop_length, null)));
			Utils.addListJTree(t, operational_descriptor_loop, modus, "operational_descriptor_loop");

			return t;
		}

		public List<Descriptor> getOperational_descriptor_loop() {
			return operational_descriptor_loop;
		}

		public void setOperational_descriptor_loop(final List<Descriptor> operational_descriptor_loop) {
			this.operational_descriptor_loop = operational_descriptor_loop;
		}

		public int getOperational_descriptor_loop_length() {
			return operational_descriptor_loop_length;
		}

		public void setOperational_descriptor_loop_length(final int operational_descriptor_loop_length) {
			this.operational_descriptor_loop_length = operational_descriptor_loop_length;
		}

		public List<Descriptor> getTarget_descriptor_loop() {
			return target_descriptor_loop;
		}

		public void setTarget_descriptor_loop(final List<Descriptor> target_descriptor_loop) {
			this.target_descriptor_loop = target_descriptor_loop;
		}

		public int getTarget_descriptor_loop_length() {
			return target_descriptor_loop_length;
		}

		public void setTarget_descriptor_loop_length(final int target_descriptor_loop_length) {
			this.target_descriptor_loop_length = target_descriptor_loop_length;
		}

	}

	public UNTsection(final PsiSectionData raw_data, final PID parent){
		super(raw_data, parent);
		action_type = Utils.getInt(raw_data.getData(), 3, 1, Utils.MASK_8BITS); // tableIdExtension first byte
		oui_hash = Utils.getInt(raw_data.getData(), 4, 1, Utils.MASK_8BITS); // tableIdExtension first byte

		oui = Utils.getInt(raw_data.getData(), 8, 3, Utils.MASK_24BITS);
		processing_order = Utils.getInt(raw_data.getData(), 11, 1, Utils.MASK_8BITS);

		common_descriptor_loop_length = Utils.getInt(raw_data.getData(), 12, 2, Utils.MASK_12BITS);
		common_descriptor_loop = DescriptorFactory.buildDescriptorList(raw_data.getData(), 14,
				common_descriptor_loop_length, this);

		int t = 0;
		while (t < (sectionLength - 18 - common_descriptor_loop_length)) {

			final PlatformLoop pf = buildPlatformLoop(raw_data.getData(), 14 + t + common_descriptor_loop_length,
					sectionLength - 18 - common_descriptor_loop_length - t);
			platformLoopList.add(pf);
			t += pf.getPlatform_loop_length() + pf.getCompatibilityDescriptor().getCompatibilityDescriptorLength() + 4;

		}
	}

	@Override
	protected String getTableIdExtensionLabel() {
		return "action_type (8)/ OUI_hash (8)";
	}

	@Override
	public String toString() {
		final StringBuilder b = new StringBuilder("UNTsection section=");
		b.append(getSectionNumber()).append(", lastSection=").append(getSectionLastNumber()).append(", tableType=")
		.append(getTableType(tableId)).append(", ");

		return b.toString();
	}

	private PlatformLoop buildPlatformLoop(final byte[] data, final int offset, final int length) {
		final PlatformLoop p = new PlatformLoop();

		final CompatibilityDescriptor cd = new CompatibilityDescriptor(data, offset);
		p.setCompatibilityDescriptor(cd);
		final int compLength = cd.getCompatibilityDescriptorLength();
		final int platform_loop_length = Utils.getInt(data, offset + compLength + 2, 2, Utils.MASK_16BITS);
		p.setPlatform_loop_length(platform_loop_length);
		p.setTarget_loop(buildTargetLoopList(data, offset + compLength + 4, platform_loop_length));

		return p;
	}

	private List<TargetLoop> buildTargetLoopList(final byte[] data, final int i, final int programInfoLength) {
		final ArrayList<TargetLoop> r = new ArrayList<>();
		int t = 0;
		while (t < programInfoLength) {
			final TargetLoop c = new TargetLoop();
			c.setTarget_descriptor_loop_length(Utils.getInt(data, i + t, 2, Utils.MASK_12BITS));
			c.setTarget_descriptor_loop(DescriptorFactory.buildDescriptorList(data, i + t + 2, c
					.getTarget_descriptor_loop_length(), this));

			t += 2 + c.getTarget_descriptor_loop_length();
			c.setOperational_descriptor_loop_length(Utils.getInt(data, i + t, 2, Utils.MASK_12BITS));
			c.setOperational_descriptor_loop(DescriptorFactory.buildDescriptorList(data, i + t + 2, c
					.getOperational_descriptor_loop_length(), this));
			r.add(c);
			t += 2 + c.getOperational_descriptor_loop_length();

		}

		return r;
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {

		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("action_type", action_type, getUNTActionTypeString(action_type))));
		t.add(new DefaultMutableTreeNode(new KVP("oui_hash", oui_hash, null)));
		t.add(new DefaultMutableTreeNode(new KVP("oui", oui, Utils.getOUIString(oui))));
		t.add(new DefaultMutableTreeNode(new KVP("processing_order", processing_order,
				getUNTProcessingOrderString(processing_order))));
		t
		.add(new DefaultMutableTreeNode(new KVP("common_descriptor_loop_length", common_descriptor_loop_length,
				null)));
		Utils.addListJTree(t, common_descriptor_loop, modus, "common_descriptor_loop");
		Utils.addListJTree(t, platformLoopList, modus, "platform_loop");
		return t;
	}

	public int getOui() {
		return oui;
	}

	public int getAction_type() {
		return action_type;
	}

}
