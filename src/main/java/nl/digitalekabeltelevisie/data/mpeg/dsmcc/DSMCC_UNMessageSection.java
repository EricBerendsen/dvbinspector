/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2023 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

import static java.util.Arrays.copyOfRange;
import static nl.digitalekabeltelevisie.util.Utils.*;

import java.util.ArrayList;
import java.util.List;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.PID;
import nl.digitalekabeltelevisie.data.mpeg.PsiSectionData;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.dsmcc.DSMCCDescriptorFactory;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.untable.CompatibilityDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSectionExtendedSyntax;

/**
 * @author Eric Berendsen
 * Based on TR 101 202 V1.2.1 Implementation guidelines for Data Broadcasting Annex A
 * represents either a DownloadServerInitiate (DSI) or a DownloadInfoIndication (DII) message
 */
public class DSMCC_UNMessageSection extends TableSectionExtendedSyntax {

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
	private final List<GroupInfo> groupInfoIndication = new ArrayList<>();


	// DII

	private long downloadId;
	private int blockSize;
	private int windowSize;
	private int ackPeriod;
	private long tCDownloadWindow;
	private long tCDownloadScenario;
	private int numberOfModules;

	private final List<ModuleInfo> modules = new ArrayList<>();

	private int numberOfGroups;
	private int groupInfoLength;
	private byte[] groupInfoByte;
	private int privateDataLen2;
	private byte[] privateDataByte2;

	/**
	 *
	 * Only used in case of objectCarousel DII
	 *
	 */
	public class ModuleInfo implements TreeNode{

		private List<Descriptor> descriptors;

		public ModuleInfo(int moduleId, long moduleSize, int moduleVersion,
                          int moduleInfoLength, byte[] moduleInfoByte) {
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


		public KVP getJTreeNode(int modus) {
			KVP t = new KVP("Module",moduleId);
			t.add(new KVP("moduleId",moduleId));
			t.add(new KVP("moduleSize",moduleSize));
			t.add(new KVP("moduleVersion",moduleVersion));
			t.add(new KVP("moduleInfoLength",moduleInfoLength));
			t.add(new KVP("moduleInfoByte",moduleInfoByte));
			if(moduleInfoLength>0){
				if(isObjectCarousel){
					t.add(biopModuleInfo.getJTreeNode(modus));
				}else{
					addListJTree(t,descriptors,modus,"descriptors");
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
	public static class GroupInfo implements TreeNode{

		// TODO should also contain
		//		GroupInfoLength 	2
		//		for (i=0; i < GroupInfoLength; i++) {
		//			GroupInfoByte 	1
		//		}

		// according to http://www.interactivetvweb.org/tutorials/dtv_intro/dsmcc/data_carousel
		// Table 6: GroupInfoIndication structure of 8.1.1 DownloadServerInitiate message (DSI) of ETSI TS 102 006 V1.3.2 (2008-07)
		// is not very clear, indentation is wrong

		public GroupInfo(byte[]data, int offset){
			groupId= getLong(data, offset, 4, MASK_32BITS);
			groupSize= getLong(data, offset+4, 4, MASK_32BITS);
			compatibilityDescriptor = new CompatibilityDescriptor(data, offset+8);
		}

		private final long groupId;
		private final long groupSize;
		private final CompatibilityDescriptor compatibilityDescriptor;


		public KVP getJTreeNode(int modus) {
			KVP t = new KVP("GroupInfo");
			t.add(new KVP("groupId",groupId));
			t.add(new KVP("groupSize",groupSize));
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
		public DSMCCMessageHeader(int protocolDiscriminator, int dsmccType,
                                  int messageId, long transactionID, int reserved,
                                  int adaptationLength, int messageLength) {
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


		public KVP getJTreeNode(int modus) {
			KVP t = new KVP("DSM-CC Message Header");
			t.add(new KVP("protocolDiscriminator",protocolDiscriminator));
			t.add(new KVP("dsmccType",dsmccType,getDSMCCTypeString(dsmccType)));
			t.add(new KVP("messageId",messageId,getMessageIDString(dsmccType, messageId)));
			t.add(new KVP("transactionID",transactionID,getTransactionIDString(transactionID)));
			t.add(new KVP("reserved",reserved));
			t.add(new KVP("adaptationLength",adaptationLength));
			t.add(new KVP("messageLength",messageLength));

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

		public long getTransactionID() {
			return transactionID;
		}

	}



	public DSMCC_UNMessageSection(PsiSectionData raw_data, PID parent, boolean isObjectCarousel2){
		super(raw_data, parent);

		this.isObjectCarousel = isObjectCarousel2;
		int protocolDiscriminator = getInt(raw_data.getData(), 8, 1, MASK_8BITS);
		int dsmccType = getInt(raw_data.getData(), 9, 1, MASK_8BITS);
		int messageId = getInt(raw_data.getData(), 10, 2, MASK_16BITS);
		long transactionId = getLong(raw_data.getData(), 12, 4, MASK_32BITS);
		int reserved = getInt(raw_data.getData(), 16, 1, MASK_8BITS);
		int adaptationLength = getInt(raw_data.getData(), 17, 1, MASK_8BITS);
		int messageLength = getInt(raw_data.getData(), 18, 2, MASK_16BITS);
		header = new DSMCCMessageHeader(protocolDiscriminator, dsmccType, messageId, transactionId, reserved, adaptationLength, messageLength);
		if(messageId==0x1006){ // DSI
			serverId=copyOfRange(raw_data.getData(), 20, 40); // should be FF

			compatibilityDescriptor = new CompatibilityDescriptor(raw_data.getData(), 40);
			int offs=42 + compatibilityDescriptor.getCompatibilityDescriptorLength();
			privateDataLength = getInt(raw_data.getData(), offs, 2, MASK_16BITS);
			offs +=2;
			privateDataByte =copyOfRange(raw_data.getData(), offs, offs+privateDataLength);
			if(isObjectCarousel){
				// create ServiceGatewayInfo from privateDataByte
				serviceGatewayIOR = new IOR(privateDataByte,0);
				int len=serviceGatewayIOR.getLength();
				downloadTaps_count= getInt(privateDataByte, len, 1, MASK_8BITS);
				// Will fail if downloadTaps_count!=0
				serviceContextList_count= getInt(privateDataByte, len+1, 1, MASK_8BITS);
				// Will fail if serviceContextList_count!=0
				userInfoLength= getInt(privateDataByte, len+2, 2, MASK_16BITS);
				// Will fail if userInfoLength!=0

			}else{ // SSU
				int offset=0;
				numberOfGroups = getInt(privateDataByte,offset, 2, MASK_16BITS);
				offset += 2;
				for (int i = 0; i < numberOfGroups; i++) {
					GroupInfo groupInfo = new GroupInfo(privateDataByte, offset);
					groupInfoIndication.add(groupInfo);
					offset += groupInfo.len();
				}
				groupInfoLength = getInt(privateDataByte,offset, 2, MASK_16BITS);
				offset += 2;
				groupInfoByte = getBytes(privateDataByte, offset, groupInfoLength);
				offset += groupInfoLength;
				privateDataLen2 = getInt(privateDataByte,offset, 2, MASK_16BITS); // this gives exception for nordig DVB-t 746000000.ts pid 8006.
				offset += 2;
				privateDataByte2 = getBytes(privateDataByte, offset, privateDataLen2);
				offset += privateDataLen2;


			}

		}else if(messageId== 0x1002){ // DownloadInfoIndication
			downloadId = getLong(raw_data.getData(), 20, 4, MASK_32BITS);
			blockSize = getInt(raw_data.getData(), 24, 2, MASK_16BITS);
			windowSize =  getInt(raw_data.getData(), 26, 1, MASK_8BITS);
			ackPeriod =  getInt(raw_data.getData(), 27, 1, MASK_8BITS);
			tCDownloadWindow = getLong(raw_data.getData(), 28, 4, MASK_32BITS);
			tCDownloadScenario = getLong(raw_data.getData(), 32, 4, MASK_32BITS);
			compatibilityDescriptor = new CompatibilityDescriptor(raw_data.getData(), 36); // start at 36, length-field is also part of descriptor
			int p=38+compatibilityDescriptor.getCompatibilityDescriptorLength();

			numberOfModules = getInt(raw_data.getData(), p, 2, MASK_16BITS);
			p+=2;

			if(true){ // isObjectCarousel, also SSU ??
				for (int i = 0; i < numberOfModules; i++) {
					int moduleId= getInt(raw_data.getData(), p, 2, MASK_16BITS);
					p+=2;
					long moduleSize = getLong(raw_data.getData(), p, 4, MASK_32BITS);
					p+=4;
					int moduleVersion = getInt(raw_data.getData(), p++, 1, MASK_8BITS);
					int moduleInfoLength = getInt(raw_data.getData(), p++, 1, MASK_8BITS);
					byte[] moduleInfoByte = getBytes(raw_data.getData(), p, moduleInfoLength);
					p+=moduleInfoLength;
					ModuleInfo mod = new ModuleInfo(moduleId, moduleSize, moduleVersion, moduleInfoLength, moduleInfoByte);
					modules.add(mod);
				}
			}
			privateDataLength = getInt(raw_data.getData(), p, 2, MASK_16BITS);
		}

	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder("DSMCCsection section=");
		b.append(getSectionNumber()).append(", lastSection=").append(getSectionLastNumber()).append(", tableType=")
		.append(getTableType(tableId)).append(", ");

		return b.toString();
	}


	@Override
	public KVP getJTreeNode(int modus) {

		KVP t = super.getJTreeNode(modus);
		t.setUserObject(new KVP(isDSI()?"DSI":"DII"));
		t.add(header.getJTreeNode(modus));
		if(isDSI()){ // DSI
			t.add(new KVP("serverId",serverId));
			t.add(compatibilityDescriptor.getJTreeNode(modus));
			t.add(new KVP("privateDataLength",privateDataLength));
			t.add(new KVP("privateDataByte",privateDataByte));
			if(isObjectCarousel){
				t.add(serviceGatewayIOR.getJTreeNode(modus));
				t.add(new KVP("downloadTaps_count",downloadTaps_count));
				t.add(new KVP("serviceContextList_count",serviceContextList_count));
				t.add(new KVP("userInfoLength",userInfoLength));

			}else { // SSU
				t.add(new KVP("numberOfGroups",numberOfGroups));
				addListJTree(t,groupInfoIndication,modus,"GroupInfos");
				t.add(new KVP("groupInfoLength",groupInfoLength));
				t.add(new KVP("groupInfoByte",groupInfoByte));
				t.add(new KVP("privateDataLen",privateDataLen2));
				t.add(new KVP("privateDataByte",privateDataByte2));
			}


		}else if(isDII()){ // DownloadInfoIndication
			t.add(new KVP("downloadId",downloadId));
			t.add(new KVP("blockSize",blockSize));
			t.add(new KVP("windowSize",windowSize));
			t.add(new KVP("ackPeriod",ackPeriod));
			t.add(new KVP("tCDownloadWindow",tCDownloadWindow));
			t.add(new KVP("tCDownloadScenario",tCDownloadScenario));
			t.add(compatibilityDescriptor.getJTreeNode(modus));

			t.add(new KVP("numberOfModules",numberOfModules));
			if(true){ // isObjectCarousel
				addListJTree(t,modules,modus,"Modules");
			}
			t.add(new KVP("privateDataLength",privateDataLength));
		}


		return t;
	}


	public boolean isDII() {
		return header.getMessageId()== 0x1002;
	}



	public boolean isDSI() {
		return header.getMessageId()==0x1006;
	}

	private static String getDSMCCTypeString(int dsmccType) {
        return switch (dsmccType) {
            case 0x01 -> "User-to-Network configuration message";
            case 0x02 -> "User-to-Network session message";
            case 0x03 -> "Download message";
            case 0x04 -> "SDB Channel Change Protocol message";
            case 0x05 -> "User-to- Network pass-thru message";
            default -> {
                if ((dsmccType >= 0x80) && (dsmccType <= 0xff)) {
                    yield "User Defined message type";
                }
                yield "reserved";
            }
        };
	}


	private static String getMessageIDString(int dsmccType, int messageId) {
        return switch (dsmccType) {
            case 0x03 ->
                // Download message
                    switch (messageId) {
                        case 0x1001 -> "DownloadInfoRequest";
                        case 0x1002 -> "DownloadInfoIndication";
                        case 0x1003 -> "DownloadDataBlock";
                        case 0x1004 -> "DownloadDataRequest";
                        case 0x1005 -> "DownloadCancel";
                        case 0x1006 -> "DownloadServerInitiate";
                        default -> null;
                    };
            default -> null;
        };
	}


	/**
	 *
	 * Based on ETSI TR 101 202 V1.2.1 (2003-01), Table 4.20: Sub-fields of the transactionId
	 * @param transactionID
	 * @return
	 */
	public static String getTransactionIDString(long transactionID){
		StringBuilder s = new StringBuilder();
		s.append("Originator: ").		append((transactionID & 0xC0000000L)>>30); // bits 30 to 31 Bit 30 - zero, Bit 31 - non-zero
		s.append(", Version: ").		append((transactionID & 0x3FFF0000L)>>16); // bits 16 to 29 This must be incremented/changed every time the control message is updated.
		s.append(", Identification: ").	append((transactionID & 0x0000FFFEL)>>1); // bits 1 to 15 This must and can only be all zeros for the DownloadServerInitiate message. All other control messages must have one or more non-zero bit(s).
		s.append(", Updated flag: ").	append( transactionID & 0x00000001L); // bit 0
		return s.toString();
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

	public ModuleInfo getModule(int modId) {
		for(ModuleInfo module: modules){
			if(module.getModuleId()==modId){
				return module;
			}
		}
		return null;
	}

}
