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

package nl.digitalekabeltelevisie.data.mpeg.descriptors;

import static nl.digitalekabeltelevisie.util.Utils.*;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.Utils;

public class DataBroadcastIDDescriptor extends Descriptor {
	// TODO refactor, common code DataBroadcastIDDescriptor and DataBroadcastDescriptor should not be duplicated

	private final List<OUIEntry> ouiList = new ArrayList<OUIEntry>();
	private final List<ApplicationType> applicationTypeList = new ArrayList<ApplicationType>();
	private final List<MHEG5ApplicationType> mheg5ApplicationTypeList = new ArrayList<MHEG5ApplicationType>();

	private final List<Platform> platformList = new ArrayList<Platform>();

	/**
	 * broadcast IDs that will be interpreted as indicating an object carousel.
	 */
	public static final int[] OBJECT_CAROUSEL_BROADCASTID={
		0x0007, // DVB object carousel
		0x00f0,	//MHP Object
		0x0106,	//MHEG5(The Digital Network)
		0x0123,	//HBBTV Carousel
		0x0150, //OIPF Object Carousel
		0xBBB2	//BBG Object Carousel
	};

	public static class Platform implements TreeNode{
		/**
		 *
		 */
		private final int platform_id ;
		private final int action_type ;
		private final int INT_versioning_flag ;
		private final int INT_version ;



		public Platform(final int platform_id, final int action_type, final int int_versioning_flag, final int int_version) {
			super();
			this.platform_id = platform_id;
			this.action_type = action_type;
			INT_versioning_flag = int_versioning_flag;
			INT_version = int_version;
		}



		public DefaultMutableTreeNode getJTreeNode(final int modus){
			final DefaultMutableTreeNode s=new DefaultMutableTreeNode(new KVP("platforms"));
			s.add(new DefaultMutableTreeNode(new KVP("platform_id",platform_id,getPlatformIDString(platform_id))));
			s.add(new DefaultMutableTreeNode(new KVP("action_type",action_type,getActionTypeString(action_type))));
			s.add(new DefaultMutableTreeNode(new KVP("INT_versioning_flag",INT_versioning_flag,null)));
			s.add(new DefaultMutableTreeNode(new KVP("INT_version",INT_version,null)));
			return s;
		}
	}


	public static class ApplicationType implements TreeNode{
		private final int applicationType;

		public ApplicationType(final int applicationType) {
			super();
			this.applicationType = applicationType;
		}

		public DefaultMutableTreeNode getJTreeNode(final int modus){
			return new DefaultMutableTreeNode(new KVP("application_type",applicationType,getAppTypeIDString(applicationType)));
		}

	}

	public static class MHEG5ApplicationType implements TreeNode{
		private final int applicationTypeCode;
		private final int boot_priority_hint;
		private final int application_specific_data_length;
		private final byte [] application_specific_data_byte;


		public MHEG5ApplicationType(final int applicationType, final int bootPriorityHint, final int applicationSpecificDataLength, final byte[] data) {
			super();
			this.applicationTypeCode = applicationType;
			this.boot_priority_hint = bootPriorityHint;
			this.application_specific_data_length = applicationSpecificDataLength;
			application_specific_data_byte = data;
		}

		public DefaultMutableTreeNode getJTreeNode(final int modus){
			final DefaultMutableTreeNode s=new DefaultMutableTreeNode(new KVP("MHEG5ApplicationType"));
			s.add(new DefaultMutableTreeNode(new KVP("application_type_code",applicationTypeCode,getMHEG5ApplicationTypeString(applicationTypeCode))));
			s.add(new DefaultMutableTreeNode(new KVP("boot_priority_hint",boot_priority_hint,null)));
			s.add(new DefaultMutableTreeNode(new KVP("application_specific_data_length",application_specific_data_length,null)));
			s.add(new DefaultMutableTreeNode(new KVP("application_specific_data_byte",application_specific_data_byte,null)));

			return s;
		}

	}
	public class OUIEntry implements TreeNode{

		private final int oui;
		private final int updateType;
		private final int updateVersioningFlag;
		private final int updateVersion;
		private final int selectorLength;
		private final byte[] ouiSelectorBytes;



		public OUIEntry(final int oui, final int updateType, final int updateVersioningFlag, final int updateVersion, final int selectorLength, final byte[] selectorByte) {
			super();
			this.oui = oui;
			this.updateType = updateType;
			this.updateVersioningFlag = updateVersioningFlag;
			this.updateVersion = updateVersion;
			this.selectorLength = selectorLength;
			this.ouiSelectorBytes = selectorByte;
		}



		public DefaultMutableTreeNode getJTreeNode(final int modus){
			final DefaultMutableTreeNode s=new DefaultMutableTreeNode(new KVP("OUI"));
			s.add(new DefaultMutableTreeNode(new KVP("oui",oui,Utils.getOUIString(oui))));
			s.add(new DefaultMutableTreeNode(new KVP("update_type",updateType,getUpdateTypeString(updateType))));
			s.add(new DefaultMutableTreeNode(new KVP("update_versioning_flag",updateVersioningFlag,updateVersioningFlag==0?"no relevant versioning information is carried in the version field":"version field reflects changes in the system software update service component"))); // ETSI TS 102 006 V1.2.1  p7.1
			s.add(new DefaultMutableTreeNode(new KVP("update_version",updateVersion,null)));
			s.add(new DefaultMutableTreeNode(new KVP("selector_length",selectorLength,null)));
			s.add(new DefaultMutableTreeNode(new KVP("selector_bytes",ouiSelectorBytes,null)));
			return s;
		}



		public int getOui() {
			return oui;
		}



		public byte[] getSelectorByte() {
			return ouiSelectorBytes;
		}



		public int getSelectorLength() {
			return selectorLength;
		}



		public int getUpdateType() {
			return updateType;
		}



		public int getUpdateVersion() {
			return updateVersion;
		}



		public int getUpdateVersioningFlag() {
			return updateVersioningFlag;
		}

	}

	private final int dataBroadcastId;
	private int OUI_data_length;

	private int platform_id_data_length;

	private byte[] selectorByte;
	private byte[] privateDataByte;

	private int MAC_address_range;
	private int MAC_IP_mapping_flag;
	private int alignment_indicator;
	private int max_sections_per_datagram ;


	public DataBroadcastIDDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset, parent);
		dataBroadcastId = Utils.getInt(b, offset+2, 2, Utils.MASK_16BITS);
		if(dataBroadcastId==0x0005){ //en 301192 7.2.1
			MAC_address_range = Utils.getInt(b, offset+4, 1, 0xE0)>>>5;
			MAC_IP_mapping_flag = Utils.getInt(b, offset+4, 1, 0x10)>>>4;
			alignment_indicator = Utils.getInt(b, offset+4, 1, 0x08)>>>3;
			max_sections_per_datagram =Utils.getInt(b, offset+5, 1, Utils.MASK_8BITS);
		}else if(dataBroadcastId==0x000a){
			OUI_data_length = getInt(b,offset+4,1,MASK_8BITS);
			int r =0;
			while (r<OUI_data_length) {
				final int oui = getInt(b,offset+ 5+r, 3, Utils.MASK_24BITS);
				final int updateType = getInt(b,offset+ 8+r, 1, Utils.MASK_4BITS);
				final int updateVersioningFlag = getInt(b,offset+ 9+r, 1, 0x20)>>5;
				final int updateVersion = getInt(b,offset+ 9+r, 1, Utils.MASK_5BITS);
				final int selectorLength= getInt(b, offset+r+10, 1, MASK_8BITS);
				final byte[] selector_byte = copyOfRange(b, offset+r+11, offset+r+11+selectorLength);
				final OUIEntry ouiEntry = new OUIEntry(oui,updateType,updateVersioningFlag,updateVersion, selectorLength,selector_byte);
				ouiList.add(ouiEntry);
				r=r+6+selectorLength;
			}
			privateDataByte = Utils.copyOfRange(b, offset+5+r, offset+descriptorLength+2);
		}else if(dataBroadcastId==0x000b){ //IP/MAC_notification_info structure ETSI EN 301 192 V1.4.2
			platform_id_data_length = getInt(b,offset+4,1,MASK_8BITS);
			int r =0;
			while (r<platform_id_data_length) {
				final int platform_id = getInt(b,offset+ 5+r, 3, Utils.MASK_24BITS);
				final int action_type  = getInt(b,offset+ 8+r, 1, Utils.MASK_8BITS);

				final int INT_versioning_flag = getInt(b,offset+ 9+r, 1, 0x20)>>>5;
				final int INT_version = getInt(b,offset+ 9+r, 1, Utils.MASK_5BITS);

				final Platform p = new Platform(platform_id,action_type,INT_versioning_flag,INT_version);
				platformList.add(p);
				r=r+5;
			}
			privateDataByte = Utils.copyOfRange(b, offset+5+r, offset+descriptorLength+2);


		}else if((dataBroadcastId==0x00f0)||(dataBroadcastId==0x00f1)){ // MHP
			int r =0;
			while (r<(descriptorLength-2)){
				final int at = getInt(b,offset+ 4+r, 2, Utils.MASK_15BITS);
				final ApplicationType appT = new ApplicationType(at);
				applicationTypeList.add(appT);
				r+=2;
			}

		}else if(dataBroadcastId==0x0106){ //ETSI ES 202 184 V2.2.1 (2011-03) 9.3.2.1 data_broadcast_id_descriptor
			int r =0;
			while (r<(descriptorLength-2)){
				final int at = getInt(b,offset+ 4+r, 2, Utils.MASK_16BITS);
				final int bootPrio = getInt(b,offset+ 6+r, 1, Utils.MASK_8BITS);
				final int appDataLen = getInt(b,offset+ 7+r, 1, Utils.MASK_8BITS);
				final byte [] appData = copyOfRange(b, offset+r+8, offset+r+8+appDataLen);
				final MHEG5ApplicationType mheg5App = new MHEG5ApplicationType(at, bootPrio, appDataLen, appData);
				mheg5ApplicationTypeList.add(mheg5App);
				r=r+4+appDataLen;

			}
		}else{
			selectorByte = Utils.copyOfRange(b, offset+4, offset+descriptorLength+2);
		}

	}






	public String getUpdateTypeString(final int updateType) {

		switch (updateType) {
		case 0x00: return "proprietary update solution";
		case 0x01: return "standard update carousel (i.e. without notification table) via broadcast";
		case 0x02: return "system software Update with Notification Table (UNT) via broadcast";
		case 0x03: return "system software update using return channel with UNT";
		default: return "reserved for future use";
		}
	}


	@Override
	public String toString() {

		return super.toString() + "dataBroadcastId="+dataBroadcastId;
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);

		t.add(new DefaultMutableTreeNode(new KVP("data_broadcast_id",dataBroadcastId ,Utils.getDataBroadCastIDString(dataBroadcastId))));

		if(dataBroadcastId==0x0005){ //en 301192 7.2.1
			t.add(new DefaultMutableTreeNode(new KVP("MAC_address_range",MAC_address_range,DataBroadcastDescriptor.getvalidMACaddressBytesString(MAC_address_range))));
			t.add(new DefaultMutableTreeNode(new KVP("MAC_IP_mapping_flag",MAC_IP_mapping_flag,MAC_IP_mapping_flag==1?"uses IP to MAC mapping as described in RFC 1112 and RFC 2464":"mapping not defined")));
			t.add(new DefaultMutableTreeNode(new KVP("alignment_indicator",alignment_indicator,alignment_indicator==1?"alignment in bits: 32":"alignment in bits: 8 (default)")));
			t.add(new DefaultMutableTreeNode(new KVP("max_sections_per_datagram",max_sections_per_datagram,null)));
		}else if(dataBroadcastId==0x000a){
			addListJTree(t,ouiList,modus,"Systems Software Update");
			t.add(new DefaultMutableTreeNode(new KVP("private_data_byte",privateDataByte ,null)));
		}else if(dataBroadcastId==0x000b){ //IP/MAC_notification_info structure ETSI EN 301 192 V1.4.2
			addListJTree(t,platformList,modus,"IP/MAC platform");
			t.add(new DefaultMutableTreeNode(new KVP("private_data_byte",privateDataByte ,null)));
		}else if((dataBroadcastId==0x00f0)||(dataBroadcastId==0x00f1)){ // MHP
			addListJTree(t,applicationTypeList,modus,"application_type");

		}else if(dataBroadcastId==0x0106){
			addListJTree(t,mheg5ApplicationTypeList,modus,"MHEG5 Applications");
		}else{
			t.add(new DefaultMutableTreeNode(new KVP("id_selector_bytes",selectorByte,null)));
		}

		return t;
	}






	public int getAlignment_indicator() {
		return alignment_indicator;
	}






	public List<ApplicationType> getApplicationTypeList() {
		return applicationTypeList;
	}






	public int getDataBroadcastId() {
		return dataBroadcastId;
	}






	public int getMAC_address_range() {
		return MAC_address_range;
	}






	public int getMAC_IP_mapping_flag() {
		return MAC_IP_mapping_flag;
	}






	public int getMax_sections_per_datagram() {
		return max_sections_per_datagram;
	}






	public int getOUI_data_length() {
		return OUI_data_length;
	}






	public List<OUIEntry> getOuiList() {
		return ouiList;
	}






	public int getPlatform_id_data_length() {
		return platform_id_data_length;
	}






	public List<Platform> getPlatformList() {
		return platformList;
	}






	public byte[] getPrivateDataByte() {
		return privateDataByte;
	}






	public byte[] getSelectorByte() {
		return selectorByte;
	}


	public static String getMHEG5ApplicationTypeString(final int appType) {

		switch (appType) {
		case 0x00: return "NULL_APPLICATION_TYPE";
		case 0x0101: return "UK_PROFILE_LAUNCH";
		case 0x0505: return "UK_PROFILE_BASELINE_1";
		default: return "unknown";
		}
	}


}
