/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2014 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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
import nl.digitalekabeltelevisie.data.mpeg.descriptors.aitable.ApplicationNameDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.aitable.ApplicationNameDescriptor.ApplicationName;
import nl.digitalekabeltelevisie.data.mpeg.psi.AITsection.Application;
import nl.digitalekabeltelevisie.util.Utils;

/**
 * @author Eric Berendsen
 */
public class RCTsection extends TableSectionExtendedSyntax {

	private int year_offset;

	private int link_count;

	private int descriptor_loop_length;
	private List<Descriptor>descriptor_loop;

	private int application_loop_length;
	private List<LinkInfo>	links;

	public static class LinkInfo implements TreeNode {
		private long organisation_id;
		private int application_id;

		private int application_control_code;
		private int application_descriptors_loop_length;

		private List<Descriptor> applicationDescriptors;

		public DefaultMutableTreeNode getJTreeNode(final int modus) {
			// try to find application name
			StringBuilder label = new StringBuilder("LinkInfo");

			final List<ApplicationNameDescriptor> applicationNameDescriptors = Descriptor.findGenericDescriptorsInList(applicationDescriptors, ApplicationNameDescriptor.class); //0x01 = applicationNameDescriptor
			if((applicationNameDescriptors!=null)&&(applicationNameDescriptors.size()>0)){
				final ApplicationNameDescriptor appNameDesc = applicationNameDescriptors.get(0);
				final List<ApplicationName> appNames = appNameDesc.getApplicationNames();
				if((appNames!=null)&&(appNames.size()>0)){
					final ApplicationName appName = appNames.get(0);
					label.append(" (").append(appName.getApplication_name().toString()).append(")");
				}
			}

			final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP(label.toString()));
			t.add(new DefaultMutableTreeNode(new KVP("organisation_id", organisation_id, getMHPOrganistionIdString(organisation_id))));
//			t.add(new DefaultMutableTreeNode(new KVP("application_id", application_id, getApplicationIDString(application_id))));
//			t.add(new DefaultMutableTreeNode(new KVP("application_control_code", application_control_code, getApplicationControlCodeString(application_control_code))));
			t.add(new DefaultMutableTreeNode(new KVP("application_descriptors_loop_length", application_descriptors_loop_length, null)));

			Utils.addListJTree(t,applicationDescriptors,modus,"application_descriptors");

			return t;
		}


		/**
		 * @return the application_control_code
		 */
		public int getApplication_control_code() {
			return application_control_code;
		}


		/**
		 * @param application_control_code the application_control_code to set
		 */
		public void setApplication_control_code(final int application_control_code) {
			this.application_control_code = application_control_code;
		}


		/**
		 * @return the application_descriptors_loop_lengt
		 */
		public int getApplication_descriptors_loop_length() {
			return application_descriptors_loop_length;
		}


		/**
		 * @param application_descriptors_loop_lengt the application_descriptors_loop_lengt to set
		 */
		public void setApplication_descriptors_loop_length(final int application_descriptors_loop_lengt) {
			this.application_descriptors_loop_length = application_descriptors_loop_lengt;
		}


		/**
		 * @return the application_id
		 */
		public int getApplication_id() {
			return application_id;
		}


		/**
		 * @param application_id the application_id to set
		 */
		public void setApplication_id(final int application_id) {
			this.application_id = application_id;
		}


		/**
		 * @return the applicationDescriptor
		 */
		public List<Descriptor> getApplicationDescriptors() {
			return applicationDescriptors;
		}


		/**
		 * @param applicationDescriptor the applicationDescriptor to set
		 */
		public void setApplicationDescriptors(final List<Descriptor> applicationDescriptor) {
			this.applicationDescriptors = applicationDescriptor;
		}


		/**
		 * @return the organisation_id
		 */
		public long getOrganisation_id() {
			return organisation_id;
		}


		/**
		 * @param organisation_id the organisation_id to set
		 */
		public void setOrganisation_id(final long organisation_id) {
			this.organisation_id = organisation_id;
		}

	}

	public RCTsection(final PsiSectionData raw_data, final PID parent){
		super(raw_data, parent);

		byte[] b = raw_data.getData();

		year_offset = Utils.getInt(b, 8, 2, Utils.MASK_16BITS);
		link_count = Utils.getInt(b, 10, 1, Utils.MASK_8BITS);
		int localOffset = 11;
		for (int i = 0; i < link_count; i++) {
			int link_info_length = Utils.getInt(b, localOffset, 2, Utils.MASK_12BITS);
			localOffset +=2+link_info_length;
		}

		descriptor_loop_length = Utils.getInt(b, localOffset, 2, Utils.MASK_12BITS);
		localOffset += 2;
//
		descriptor_loop = DescriptorFactory.buildDescriptorList(b, localOffset,
				descriptor_loop_length, this);
//
//		application_loop_length = Utils.getInt(raw_data.getData(), 10+common_descriptors_length, 2, Utils.MASK_12BITS);
//
//		applications = buildApplicationList(raw_data.getData(), 12+common_descriptors_length , application_loop_length);
//
	}

	public List<Application> buildApplicationList(final byte[] data, final int i, final int length) {
		final ArrayList<Application> r = new ArrayList<Application>();
		int t =0;
		while(t<length){
			final Application a = new Application();
			a.setOrganisation_id(Utils.getLong(data, i+t, 4, Utils.MASK_32BITS));
			a.setApplication_id(Utils.getInt(data, i+t+4, 2, Utils.MASK_16BITS));
			a.setApplication_control_code(Utils.getInt(data, i+t+6, 1, Utils.MASK_8BITS));
			a.setApplication_descriptors_loop_length(Utils.getInt(data, i+t+7, 2, Utils.MASK_12BITS));
			a.setApplicationDescriptors(DescriptorFactory.buildDescriptorList(data,i+t+9,a.getApplication_descriptors_loop_length(),this));

			r.add(a);
			t+=9+a.getApplication_descriptors_loop_length();

		}

		return r;
	}




	@Override
	public String toString() {
		final StringBuilder b = new StringBuilder("AITsection section=");
		b.append(getSectionNumber()).append(", lastSection=").append(getSectionLastNumber()).append(", tableType=")
		.append(getTableType(tableId)).append(", ");

		return b.toString();
	}



	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {

		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("year_offset", year_offset, null)));
		t.add(new DefaultMutableTreeNode(new KVP("link_count", link_count, null)));
		t.add(new DefaultMutableTreeNode(new KVP("descriptor_loop_length", descriptor_loop_length, null)));
		Utils.addListJTree(t,descriptor_loop,modus,"descriptor_loop");
//
//		Utils.addListJTree(t,applications,modus,"applications");
		return t;
	}




}
