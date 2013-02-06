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

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.PID;
import nl.digitalekabeltelevisie.data.mpeg.PsiSectionData;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.Utils;

/**
 * @author Eric Berendsen
 * Based on TR 101 202 V1.2.1 Implementation guidelines for Data Broadcasting Annex A
 * See also {@link http://www.mhp-interactive.org/tutorials/dtv_intro/dsmcc} for a clear introduction
 */
public class DSMCC_DownLoadDataMessageSection extends TableSection {


	private final DSMCCMessageHeader header;
	private final int moduleId;
	private final int moduleVersion ;
	private final int reserved;
	private final int blockNumber;
	private final byte[] blockDataByte;

	/**
	 * This DSMCCMessageHeader is different form the one in DSMCC_UNMessageSection because the field transactionId is called downloadID in this one.
	 * @author Eric
	 *
	 */
	public static class DSMCCMessageHeader implements TreeNode{
		public DSMCCMessageHeader(final int protocolDiscriminator, final int dsmccType,
				final int messageId, final long downloadId, final int reserved,
				final int adaptationLength, final int messageLength) {
			super();
			this.protocolDiscriminator = protocolDiscriminator;
			this.dsmccType = dsmccType;
			this.messageId = messageId;
			this.downloadId = downloadId;
			this.reserved = reserved;
			this.adaptationLength = adaptationLength;
			this.messageLength = messageLength;
		}

		private final int protocolDiscriminator;
		private final int dsmccType;
		private final int messageId;
		private final long downloadId;
		private final int reserved;
		private final int adaptationLength;
		private final int messageLength;


		public DefaultMutableTreeNode getJTreeNode(final int modus) {
			final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("DSM-CC Message Header"));
			t.add(new DefaultMutableTreeNode(new KVP("protocolDiscriminator",protocolDiscriminator,null)));
			t.add(new DefaultMutableTreeNode(new KVP("dsmccType",dsmccType,getDSMCCTypeString(dsmccType))));
			t.add(new DefaultMutableTreeNode(new KVP("messageId",messageId,getMessageIDString(dsmccType, messageId))));
			t.add(new DefaultMutableTreeNode(new KVP("downloadId",downloadId,null)));
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

		private int getMessageLength() {
			return messageLength;
		}

	}



	public DSMCC_DownLoadDataMessageSection(final PsiSectionData raw_data, final PID parent){
		super(raw_data, parent);
		final int protocolDiscriminator = Utils.getInt(raw_data.getData(), 8, 1, Utils.MASK_8BITS);
		final int dsmccType = Utils.getInt(raw_data.getData(), 9, 1, Utils.MASK_8BITS);
		final int messageId = Utils.getInt(raw_data.getData(), 10, 2, Utils.MASK_16BITS);
		final long downloadId = Utils.getLong(raw_data.getData(), 12, 4, Utils.MASK_32BITS);
		final int reserved1 = Utils.getInt(raw_data.getData(), 16, 1, Utils.MASK_8BITS);
		final int adaptationLength = Utils.getInt(raw_data.getData(), 17, 1, Utils.MASK_8BITS);
		final int messageLength = Utils.getInt(raw_data.getData(), 18, 2, Utils.MASK_16BITS);
		header = new DSMCCMessageHeader(protocolDiscriminator, dsmccType, messageId, downloadId, reserved1, adaptationLength, messageLength);
		moduleId = Utils.getInt(raw_data.getData(), 20, 2, Utils.MASK_16BITS);
		moduleVersion = Utils.getInt(raw_data.getData(), 22, 1, Utils.MASK_8BITS);
		reserved = Utils.getInt(raw_data.getData(), 23, 1, Utils.MASK_8BITS);
		blockNumber = Utils.getInt(raw_data.getData(), 24, 2, Utils.MASK_16BITS);
		blockDataByte = Utils.copyOfRange(raw_data.getData(), 26, 20+header.getMessageLength()); //to = 26+messagelength - 6

	}

	public int getPayLoadLength(){
		if(header!=null){
			return  header.getMessageLength()-6;
		}
		return 0;
	}


	public byte[] getPayLoad(){
		return Utils.copyOfRange(raw_data.getData(), 26, (26+header.getMessageLength())-6);
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
		t.add(header.getJTreeNode(modus));
		t.add(new DefaultMutableTreeNode(new KVP("moduleId",moduleId,null)));
		t.add(new DefaultMutableTreeNode(new KVP("moduleVersion",moduleVersion,null)));
		t.add(new DefaultMutableTreeNode(new KVP("reserved",reserved,null)));
		t.add(new DefaultMutableTreeNode(new KVP("blockNumber",blockNumber,null)));
		t.add(new DefaultMutableTreeNode(new KVP("blockDataByte",blockDataByte,null)));


		return t;
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
		//		case 0x01:
		//			return "User-to-Network configuration message";
		//		case 0x02:
		//			return "User-to-Network session message";
		case 0x03:
			// "Download message";
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
			//		case 0x04:
			//			return "SDB Channel Change Protocol message";
			//		case 0x05:
			//			return "User-to- Network pass-thru message";

		default:
			return null;

		}
	}


	public DSMCCMessageHeader getHeader() {
		return header;
	}


	public int getModuleId() {
		return moduleId;
	}


	public int getModuleVersion() {
		return moduleVersion;
	}


	public int getReserved() {
		return reserved;
	}


	public int getBlockNumber() {
		return blockNumber;
	}


	public byte[] getBlockDataByte() {
		return blockDataByte;
	}

}
