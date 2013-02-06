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

 *
 *
 */

package nl.digitalekabeltelevisie.data.mpeg.dsmcc;

import static nl.digitalekabeltelevisie.util.Utils.addListJTree;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.PID;
import nl.digitalekabeltelevisie.data.mpeg.PsiSectionData;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.dsmcc.DSMCCDescriptorFactory;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.untable.CompatibilityDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.Utils;

/**
 * @author Eric Berendsen
 * Based on TR 101 202 V1.2.1 Implementation guidelines for Data Broadcasting Annex A
 * represents either a DownloadServerInitiate (DSI) or a DownloadInfoIndication (DII) message
 */
public class DSMCC_UNMessageSection extends TableSection {

	private boolean isObjectCarousel = true; // TODO later also support dataCarousel, for now assume everything is objectCarousel



	private final DSMCCMessageHeader header;

	// DSI
	private byte[] serverId;
	private CompatibilityDescriptor compatibilityDescriptor;
	private int privateDataLength;
	private byte[] privateDataByte;

	private IOR serviceGatewayIOR;
	private int downloadTaps_count;
	private int serviceContextList_count;
	private int userInfoLength;

	// DSI SSU
	private final List<GroupInfo> groupInfoIndication = new ArrayList<DSMCC_UNMessageSection.GroupInfo>();


	// DII

	private long downloadId;
	private int blockSize;
	private int windowSize;
	private int ackPeriod;
	private long tCDownloadWindow;
	private long tCDownloadScenario;
	private int numberOfModules;

	private final List<ModuleInfo> modules = new ArrayList<DSMCC_UNMessageSection.ModuleInfo>();



	private int numberOfGroups;



	private int groupInfoLength;



	private byte[] groupInfoByte;



	private int privateDataLen2;



	private byte[] privateDataByte2;




	/**
	 *
	 * Only used in case of objectCarousel DII
	 * @author Eric
	 *
	 */
	public class ModuleInfo implements TreeNode{

		private List<Descriptor> descriptors;


		/**
		 * @param moduleId
		 * @param moduleSize
		 * @param moduleVersion
		 * @param moduleInfoLength
		 * @param moduleInfoByte
		 */
		public ModuleInfo(final int moduleId, final long moduleSize, final int moduleVersion,
				final int moduleInfoLength, final byte[] moduleInfoByte) {
			super();
			this.moduleId = moduleId;
			this.moduleSize = moduleSize;
			this.moduleVersion = moduleVersion;
			this.moduleInfoLength = moduleInfoLength;
			this.moduleInfoByte = moduleInfoByte;
			if(moduleInfoLength>0){
				if(isObjectCarousel){
					this.biopModuleInfo = new BIOPModuleInfo(moduleInfoByte, 0);
				}else{
					descriptors = DSMCCDescriptorFactory.buildDescriptorList(moduleInfoByte, 0, moduleInfoLength);
				}
			}
		}


		private final int moduleId;
		private final long moduleSize;
		private final int moduleVersion;
		private final int moduleInfoLength;
		private final byte[] moduleInfoByte;
		private BIOPModuleInfo biopModuleInfo;


		public DefaultMutableTreeNode getJTreeNode(final int modus) {
			final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("Module",moduleId,null));
			t.add(new DefaultMutableTreeNode(new KVP("moduleId",moduleId,null)));
			t.add(new DefaultMutableTreeNode(new KVP("moduleSize",moduleSize,null)));
			t.add(new DefaultMutableTreeNode(new KVP("moduleVersion",moduleVersion,null)));
			t.add(new DefaultMutableTreeNode(new KVP("moduleInfoLength",moduleInfoLength,null)));
			t.add(new DefaultMutableTreeNode(new KVP("moduleInfoByte",moduleInfoByte,null)));
			if(moduleInfoLength>0){
				if(isObjectCarousel){
					t.add(biopModuleInfo.getJTreeNode(modus));
				}else{
					Utils.addListJTree(t,descriptors,modus,"descriptors");
				}
			}
			return t;
		}


		public int getModuleId() {
			return moduleId;
		}


		public long getModuleSize() {
			return moduleSize;
		}


		public int getModuleVersion() {
			return moduleVersion;
		}


		public int getModuleInfoLength() {
			return moduleInfoLength;
		}


		public byte[] getModuleInfoByte() {
			return moduleInfoByte;
		}


		public BIOPModuleInfo getBiopModuleInfo() {
			return biopModuleInfo;
		}

	}

	// for DSI for SSU
	public class GroupInfo implements TreeNode{

		// TODO should also contain
		//		GroupInfoLength 	2
		//		for (i=0; i < GroupInfoLength; i++) {
		//			GroupInfoByte 	1
		//		}

		// according to http://www.interactivetvweb.org/tutorials/dtv_intro/dsmcc/data_carousel
		// Table 6: GroupInfoIndication structure of 8.1.1 DownloadServerInitiate message (DSI) of ETSI TS 102 006 V1.3.2 (2008-07)
		// is not very clear, indentation is wrong

		public GroupInfo(final byte[]data, final int offset){
			groupId= Utils.getLong(data, offset, 4, Utils.MASK_32BITS);
			groupSize= Utils.getLong(data, offset+4, 4, Utils.MASK_32BITS);
			compatibilityDescriptor = new CompatibilityDescriptor(data, offset+8);
		}

		private final long groupId;
		private final long groupSize;
		private final CompatibilityDescriptor compatibilityDescriptor;


		public DefaultMutableTreeNode getJTreeNode(final int modus) {
			final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("GroupInfo"));
			t.add(new DefaultMutableTreeNode(new KVP("groupId",groupId,null)));
			t.add(new DefaultMutableTreeNode(new KVP("groupSize",groupSize,null)));
			t.add(compatibilityDescriptor.getJTreeNode(modus));
			return t;
		}

		public int len(){
			return compatibilityDescriptor.getCompatibilityDescriptorLength()+10; // 4 + 4 + 2
		}


		public long getGroupId() {
			return groupId;
		}


		public long getGroupSize() {
			return groupSize;
		}


		public CompatibilityDescriptor getCompatibilityDescriptor() {
			return compatibilityDescriptor;
		}

	}

	public class DSMCCMessageHeader implements TreeNode{
		public DSMCCMessageHeader(final int protocolDiscriminator, final int dsmccType,
				final int messageId, final long transactionID, final int reserved,
				final int adaptationLength, final int messageLength) {
			super();
			this.protocolDiscriminator = protocolDiscriminator;
			this.dsmccType = dsmccType;
			this.messageId = messageId;
			this.transactionID = transactionID;
			this.reserved = reserved;
			this.adaptationLength = adaptationLength;
			this.messageLength = messageLength;
		}

		private final int protocolDiscriminator;
		private final int dsmccType;
		private final int messageId;
		private final long transactionID;
		private final int reserved;
		private final int adaptationLength;
		private final int messageLength;


		public DefaultMutableTreeNode getJTreeNode(final int modus) {
			final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("DSM-CC Message Header"));
			t.add(new DefaultMutableTreeNode(new KVP("protocolDiscriminator",protocolDiscriminator,null)));
			t.add(new DefaultMutableTreeNode(new KVP("dsmccType",dsmccType,getDSMCCTypeString(dsmccType))));
			t.add(new DefaultMutableTreeNode(new KVP("messageId",messageId,getMessageIDString(dsmccType, messageId))));
			t.add(new DefaultMutableTreeNode(new KVP("transactionID",transactionID,null))); // TODO split into originator, version, identification and update toggle flag
			t.add(new DefaultMutableTreeNode(new KVP("reserved",reserved,null)));
			t.add(new DefaultMutableTreeNode(new KVP("adaptationLength",adaptationLength,null)));
			t.add(new DefaultMutableTreeNode(new KVP("messageLength",messageLength,null)));

			return t;
		}

		public int getProtocolDiscriminator() {
			return protocolDiscriminator;
		}

		public int getDsmccType() {
			return dsmccType;
		}

		public int getMessageId() {
			return messageId;
		}

		public long getDownloadId() {
			return downloadId;
		}

		public int getReserved() {
			return reserved;
		}

		public int getAdaptationLength() {
			return adaptationLength;
		}

		public int getMessageLength() {
			return messageLength;
		}

	}



	public DSMCC_UNMessageSection(final PsiSectionData raw_data, final PID parent, final boolean isObjectCarousel2){
		super(raw_data, parent);
		this.isObjectCarousel = isObjectCarousel2;
		final int protocolDiscriminator = Utils.getInt(raw_data.getData(), 8, 1, Utils.MASK_8BITS);
		final int dsmccType = Utils.getInt(raw_data.getData(), 9, 1, Utils.MASK_8BITS);
		final int messageId = Utils.getInt(raw_data.getData(), 10, 2, Utils.MASK_16BITS);
		final long transactionId = Utils.getLong(raw_data.getData(), 12, 4, Utils.MASK_32BITS);
		final int reserved = Utils.getInt(raw_data.getData(), 16, 1, Utils.MASK_8BITS);
		final int adaptationLength = Utils.getInt(raw_data.getData(), 17, 1, Utils.MASK_8BITS);
		final int messageLength = Utils.getInt(raw_data.getData(), 18, 2, Utils.MASK_16BITS);
		header = new DSMCCMessageHeader(protocolDiscriminator, dsmccType, messageId, transactionId, reserved, adaptationLength, messageLength);
		if(messageId==0x1006){ // DSI
			serverId=Utils.copyOfRange(raw_data.getData(), 20, 40); // should be FF

			compatibilityDescriptor = new CompatibilityDescriptor(raw_data.getData(), 40);
			int offs=42 + compatibilityDescriptor.getCompatibilityDescriptorLength();
			privateDataLength = Utils.getInt(raw_data.getData(), offs, 2, Utils.MASK_16BITS);
			offs +=2;
			privateDataByte =Utils.copyOfRange(raw_data.getData(), offs, offs+privateDataLength);
			if(isObjectCarousel){
				// create ServiceGatewayInfo from privateDataByte
				serviceGatewayIOR = new IOR(privateDataByte,0);
				final int len=serviceGatewayIOR.getLength();
				downloadTaps_count= Utils.getInt(privateDataByte, len, 1, Utils.MASK_8BITS);
				// Will fail if downloadTaps_count!=0
				serviceContextList_count= Utils.getInt(privateDataByte, len+1, 1, Utils.MASK_8BITS);
				// Will fail if serviceContextList_count!=0
				userInfoLength= Utils.getInt(privateDataByte, len+2, 2, Utils.MASK_16BITS);
				// Will fail if userInfoLength!=0

			}else{ // SSU
				int offset=0;
				numberOfGroups = Utils.getInt(privateDataByte,offset, 2, Utils.MASK_16BITS);
				offset += 2;
				for (int i = 0; i < numberOfGroups; i++) {
					final GroupInfo groupInfo = new GroupInfo(privateDataByte, offset);
					groupInfoIndication.add(groupInfo);
					offset += groupInfo.len();
				}
				groupInfoLength = Utils.getInt(privateDataByte,offset, 2, Utils.MASK_16BITS);
				offset += 2;
				groupInfoByte = Utils.getBytes(privateDataByte, offset, groupInfoLength);
				offset += groupInfoLength;
				privateDataLen2 = Utils.getInt(privateDataByte,offset, 2, Utils.MASK_16BITS); // this gives exception for nordig DVB-t 746000000.ts pid 8006.
				offset += 2;
				privateDataByte2 = Utils.getBytes(privateDataByte, offset, privateDataLen2);
				offset += privateDataLen2;


			}

		}else if(messageId== 0x1002){ // DownloadInfoIndication
			downloadId = Utils.getLong(raw_data.getData(), 20, 4, Utils.MASK_32BITS);
			blockSize = Utils.getInt(raw_data.getData(), 24, 2, Utils.MASK_16BITS);
			windowSize =  Utils.getInt(raw_data.getData(), 26, 1, Utils.MASK_8BITS);
			ackPeriod =  Utils.getInt(raw_data.getData(), 27, 1, Utils.MASK_8BITS);
			tCDownloadWindow = Utils.getLong(raw_data.getData(), 28, 4, Utils.MASK_32BITS);
			tCDownloadScenario = Utils.getLong(raw_data.getData(), 32, 4, Utils.MASK_32BITS);
			compatibilityDescriptor = new CompatibilityDescriptor(raw_data.getData(), 36); // start at 36, length-field is also part of descriptor
			int p=38+compatibilityDescriptor.getCompatibilityDescriptorLength();

			numberOfModules = Utils.getInt(raw_data.getData(), p, 2, Utils.MASK_16BITS);
			p+=2;

			if(true){ // isObjectCarousel, also SSU ??
				for (int i = 0; i < numberOfModules; i++) {
					final int moduleId= Utils.getInt(raw_data.getData(), p, 2, Utils.MASK_16BITS);
					p+=2;
					final long moduleSize = Utils.getLong(raw_data.getData(), p, 4, Utils.MASK_32BITS);
					p+=4;
					final int moduleVersion = Utils.getInt(raw_data.getData(), p++, 1, Utils.MASK_8BITS);
					final int moduleInfoLength = Utils.getInt(raw_data.getData(), p++, 1, Utils.MASK_8BITS);
					final byte[] moduleInfoByte = Utils.getBytes(raw_data.getData(), p, moduleInfoLength);
					p+=moduleInfoLength;
					final ModuleInfo mod = new ModuleInfo(moduleId, moduleSize, moduleVersion, moduleInfoLength, moduleInfoByte);
					modules.add(mod);
				}
			}
			privateDataLength = Utils.getInt(raw_data.getData(), p, 2, Utils.MASK_16BITS);
		}

	}





	@Override
	public String toString() {
		final StringBuilder b = new StringBuilder("DSMCCsection section=");
		b.append(getSectionNumber()).append(", lastSection=").append(getSectionLastNumber()).append(", tableType=")
		.append(getTableType(tableId)).append(", ");

		return b.toString();
	}



	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {

		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.setUserObject(isDSI()?"DSI":"DII");
		t.add(header.getJTreeNode(modus));
		if(isDSI()){ // DSI
			t.add(new DefaultMutableTreeNode(new KVP("serverId",serverId,null)));
			t.add(compatibilityDescriptor.getJTreeNode(modus));
			t.add(new DefaultMutableTreeNode(new KVP("privateDataLength",privateDataLength,null)));
			t.add(new DefaultMutableTreeNode(new KVP("privateDataByte",privateDataByte,null)));
			if(isObjectCarousel){
				t.add(serviceGatewayIOR.getJTreeNode(modus));
				t.add(new DefaultMutableTreeNode(new KVP("downloadTaps_count",downloadTaps_count,null)));
				t.add(new DefaultMutableTreeNode(new KVP("serviceContextList_count",serviceContextList_count,null)));
				t.add(new DefaultMutableTreeNode(new KVP("userInfoLength",userInfoLength,null)));

			}else { // SSU
				t.add(new DefaultMutableTreeNode(new KVP("numberOfGroups",numberOfGroups,null)));
				addListJTree(t,groupInfoIndication,modus,"GroupInfos");
				t.add(new DefaultMutableTreeNode(new KVP("groupInfoLength",groupInfoLength,null)));
				t.add(new DefaultMutableTreeNode(new KVP("groupInfoByte",groupInfoByte,null)));
				t.add(new DefaultMutableTreeNode(new KVP("privateDataLen",privateDataLen2,null)));
				t.add(new DefaultMutableTreeNode(new KVP("privateDataByte",privateDataByte2,null)));
			}


		}else if(isDII()){ // DownloadInfoIndication
			t.add(new DefaultMutableTreeNode(new KVP("downloadId",downloadId,null)));
			t.add(new DefaultMutableTreeNode(new KVP("blockSize",blockSize,null)));
			t.add(new DefaultMutableTreeNode(new KVP("windowSize",windowSize,null)));
			t.add(new DefaultMutableTreeNode(new KVP("ackPeriod",ackPeriod,null)));
			t.add(new DefaultMutableTreeNode(new KVP("tCDownloadWindow",tCDownloadWindow,null)));
			t.add(new DefaultMutableTreeNode(new KVP("tCDownloadScenario",tCDownloadScenario,null)));
			t.add(compatibilityDescriptor.getJTreeNode(modus));

			t.add(new DefaultMutableTreeNode(new KVP("numberOfModules",numberOfModules,null)));
			if(true){ // isObjectCarousel
				addListJTree(t,modules,modus,"Modules");
			}
			t.add(new DefaultMutableTreeNode(new KVP("privateDataLength",privateDataLength,null)));
		}


		return t;
	}





	public boolean isDII() {
		return header.getMessageId()== 0x1002;
	}





	public boolean isDSI() {
		return header.getMessageId()==0x1006;
	}

	private static String getDSMCCTypeString(final int dsmccType) {
		switch (dsmccType) {
		case 0x01:
			return "User-to-Network configuration message";
		case 0x02:
			return "User-to-Network session message";
		case 0x03:
			return "Download message";
		case 0x04:
			return "SDB Channel Change Protocol message";
		case 0x05:
			return "User-to- Network pass-thru message";

		default:
			if((dsmccType>=0x80)&&(dsmccType<=0xff)){
				return "User Defined message type";
			}else {
				return "reserved";
			}

		}
	}


	private static String getMessageIDString(final int dsmccType,final int messageId) {
		switch (dsmccType) {
		case 0x03:
			// Download message
			switch(messageId) {
			case 0x1001:
				return "DownloadInfoRequest";
			case 0x1002:
				return "DownloadInfoIndication";
			case 0x1003:
				return "DownloadDataBlock";
			case 0x1004:
				return "DownloadDataRequest";
			case 0x1005:
				return "DownloadCancel";
			case 0x1006:
				return "DownloadServerInitiate";
			default:
				return null;
			}
		default:
			return null;

		}
	}





	public boolean isObjectCarousel() {
		return isObjectCarousel;
	}





	public DSMCCMessageHeader getHeader() {
		return header;
	}





	public byte[] getServerId() {
		return serverId;
	}








	public int getPrivateDataLength() {
		return privateDataLength;
	}





	public byte[] getPrivateDataByte() {
		return privateDataByte;
	}





	public IOR getServiceGatewayIOR() {
		return serviceGatewayIOR;
	}





	public int getDownloadTaps_count() {
		return downloadTaps_count;
	}





	public int getServiceContextList_count() {
		return serviceContextList_count;
	}





	public int getUserInfoLength() {
		return userInfoLength;
	}





	public long getDownloadId() {
		return downloadId;
	}





	public int getBlockSize() {
		return blockSize;
	}





	public int getWindowSize() {
		return windowSize;
	}





	public int getAckPeriod() {
		return ackPeriod;
	}





	public long gettCDownloadWindow() {
		return tCDownloadWindow;
	}





	public long gettCDownloadScenario() {
		return tCDownloadScenario;
	}





	public int getNumberOfModules() {
		return numberOfModules;
	}





	public List<ModuleInfo> getModules() {
		return modules;
	}

	public ModuleInfo getModule(final int modId) {
		for(final ModuleInfo module: modules){
			if(module.getModuleId()==modId){
				return module;
			}
		}
		return null;
	}

}
