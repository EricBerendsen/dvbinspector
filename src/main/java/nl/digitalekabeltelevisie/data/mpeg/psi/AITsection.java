/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2025 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

import static nl.digitalekabeltelevisie.util.Utils.addListJTree;
import static nl.digitalekabeltelevisie.util.Utils.getAppTypeIDString;
import static nl.digitalekabeltelevisie.util.Utils.getMHPOrganistionIdString;

import java.util.ArrayList;
import java.util.List;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.PID;
import nl.digitalekabeltelevisie.data.mpeg.PsiSectionData;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.DescriptorFactory;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.aitable.ApplicationNameDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.aitable.ApplicationNameDescriptor.ApplicationName;
import nl.digitalekabeltelevisie.util.Utils;

/**
 * @author Eric Berendsen Represents a section of the Application Information Table.
 * @see PsiSectionData#isAITSection(int) based on ETSI ES 201 812 V1.1.1, ETSI TS 102 809 V1.1.1, ETSI TS 102 796 V1.1.1
 */
public class AITsection extends TableSectionExtendedSyntax {

	private int test_application_flag;

	private int application_type;

	private int common_descriptors_length;
	private List<Descriptor>	common_descriptor_loop;

	private int application_loop_length;
	private List<Application>	applications;

	public static class Application implements TreeNode {
		private long organisation_id;
		private int application_id;

		private int application_control_code;
		private int application_descriptors_loop_length;

		private List<Descriptor> applicationDescriptors;

		@Override
		public KVP getJTreeNode(int modus) {
			// try to find application name
			StringBuilder label = new StringBuilder("application");

			List<ApplicationNameDescriptor> applicationNameDescriptors = Descriptor.findGenericDescriptorsInList(applicationDescriptors, ApplicationNameDescriptor.class); //0x01 = applicationNameDescriptor
			if(applicationNameDescriptors.size()>0){
				ApplicationNameDescriptor appNameDesc = applicationNameDescriptors.get(0);
				List<ApplicationName> appNames = appNameDesc.getApplicationNames();
				if((appNames!=null)&&(appNames.size()>0)){
					ApplicationName appName = appNames.get(0);
					label.append(" (").append(appName.application_name().toString()).append(")");
				}
			}

			KVP t = new KVP(label.toString());
			t.add(new KVP("organisation_id", organisation_id, getMHPOrganistionIdString(organisation_id)));
			t.add(new KVP("application_id", application_id, getApplicationIDString(application_id)));
			t.add(new KVP("application_control_code", application_control_code, getApplicationControlCodeString(application_control_code)));
			t.add(new KVP("application_descriptors_loop_length", application_descriptors_loop_length));

			addListJTree(t,applicationDescriptors,modus,"application_descriptors");

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
		public void setApplication_control_code(int application_control_code) {
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
		public void setApplication_descriptors_loop_length(int application_descriptors_loop_lengt) {
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
		public void setApplication_id(int application_id) {
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
		public void setApplicationDescriptors(List<Descriptor> applicationDescriptor) {
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
		public void setOrganisation_id(long organisation_id) {
			this.organisation_id = organisation_id;
		}

	}

	public AITsection(PsiSectionData raw_data, PID parent){
		super(raw_data, parent);

		test_application_flag = Utils.getInt(raw_data.getData(), 3, 1, 0x80)>>7; // tableIdExtension first byte first bit
		application_type = Utils.getInt(raw_data.getData(), 3, 2, Utils.MASK_15BITS); // tableIdExtension rest

		common_descriptors_length = Utils.getInt(raw_data.getData(), 8, 2, Utils.MASK_12BITS);

		common_descriptor_loop = DescriptorFactory.buildDescriptorList(raw_data.getData(), 10,
				common_descriptors_length, this);

		application_loop_length = Utils.getInt(raw_data.getData(), 10+common_descriptors_length, 2, Utils.MASK_12BITS);

		applications = buildApplicationList(raw_data.getData(), 12+common_descriptors_length , application_loop_length);

	}

	private List<Application> buildApplicationList(byte[] data, int i, int length) {
		ArrayList<Application> r = new ArrayList<>();
		int t =0;
		while(t<length){
			Application a = new Application();
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
		StringBuilder b = new StringBuilder("AITsection section=");
		b.append(getSectionNumber()).append(", lastSection=").append(getSectionLastNumber()).append(", tableType=")
		.append(getTableType(tableId)).append(", ");

		return b.toString();
	}



	@Override
	public KVP getJTreeNode(int modus) {

		KVP t = super.getJTreeNode(modus);
		t.add(new KVP("common_descriptors_length", common_descriptors_length));
		addListJTree(t,common_descriptor_loop,modus,"common_descriptor_loop");
		addListJTree(t,applications,modus,"applications");
		return t;
	}


	@Override
	protected String getTableIdExtensionLabel() {
		return "test_application_flag (1 bit) / application_type (15 bits)";
	}

	protected String getTableIdExtensionDescription(int tableIdExtension) {
		int test_application_flag = (tableIdExtension & 0b1000_0000_0000_0000)>>15;
		int application_type = tableIdExtension & 0b0111_1111_1111_1111;
		return "test_application_flag="+test_application_flag+", application_type="+application_type + " ("+getAppTypeIDString(application_type)+")";
	}

	/**
	 * @return the application_loop_length
	 */
	public int getApplication_loop_length() {
		return application_loop_length;
	}


	/**
	 * @param application_loop_length the application_loop_length to set
	 */
	public void setApplication_loop_length(int application_loop_length) {
		this.application_loop_length = application_loop_length;
	}


	/**
	 * @return the application_type
	 */
	public int getApplication_type() {
		return application_type;
	}


	/**
	 * @param application_type the application_type to set
	 */
	public void setApplication_type(int application_type) {
		this.application_type = application_type;
	}


	/**
	 * @return the applications
	 */
	public List<Application> getApplications() {
		return applications;
	}


	/**
	 * @param applications the applications to set
	 */
	public void setApplications(List<Application> applications) {
		this.applications = applications;
	}


	/**
	 * @return the common_descriptor_loop
	 */
	public List<Descriptor> getCommon_descriptor_loop() {
		return common_descriptor_loop;
	}


	/**
	 * @param common_descriptor_loop the common_descriptor_loop to set
	 */
	public void setCommon_descriptor_loop(List<Descriptor> common_descriptor_loop) {
		this.common_descriptor_loop = common_descriptor_loop;
	}


	/**
	 * @return the common_descriptors_length
	 */
	public int getCommon_descriptors_length() {
		return common_descriptors_length;
	}


	/**
	 * @param common_descriptors_length the common_descriptors_length to set
	 */
	public void setCommon_descriptors_length(int common_descriptors_length) {
		this.common_descriptors_length = common_descriptors_length;
	}

	public static String getApplicationControlCodeString(int i){

		switch (i) {
		case 0x00: return "reserved_future_use";
		case 0x01: return "AUTOSTART";
		case 0x02: return "PRESENT";
		case 0x03: return "DESTROY";
		case 0x04: return "KILL";
		case 0x05: return "PREFETCH";
		case 0x06: return "REMOTE";
		case 0x07: return "DISABLED";
		case 0x08: return "PLAYBACK_AUTOSTART";
		default: return "reserved for future use";
		}
	}

	public static String getApplicationIDString(int i){
		if(i==0x0000){
			return "Shall not be used";
		}
		if((i>=0x0001)&&(i<=0x3fff)){
			return "Application_ids for unsigned applications";
		}
		if((i>=0x4000)&&(i<=0x7fff)){
			return "Application_ids for signed applications";
		}
		if((i>=0x8000)&&(i<=0x9fff)){
			return "Application_ids for privileged applications";
		}
		if((i>=0xa000)&&(i<=0xfffd)){
			return "Reserved for future use by DVB";
		}
		if(i==0xfffe){
			return "Special wildcard value for signed applications of an organization";
		}
		if(i==0xffff){
			return "Special wildcard value for all applications of an organization";
		}
		return "unknown";

	}

	public int getTest_application_flag() {
		return test_application_flag;
	}


}
