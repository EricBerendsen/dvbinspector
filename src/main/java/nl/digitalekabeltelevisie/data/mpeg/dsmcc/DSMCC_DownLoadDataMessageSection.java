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

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.PID;
import nl.digitalekabeltelevisie.data.mpeg.PsiSectionData;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSectionExtendedSyntax;
import nl.digitalekabeltelevisie.util.Utils;

import static java.util.Arrays.copyOfRange;
import static nl.digitalekabeltelevisie.util.Utils.getInt;
import static nl.digitalekabeltelevisie.util.Utils.getLong;

/**
 * @author Eric Berendsen
 * Based on TR 101 202 V1.2.1 Implementation guidelines for Data Broadcasting Annex A
 * See also {@link http://www.mhp-interactive.org/tutorials/dtv_intro/dsmcc} for a clear introduction
 */
public class DSMCC_DownLoadDataMessageSection extends TableSectionExtendedSyntax {


	private final DSMCCMessageHeader header;
	private final int moduleId;
	private final int moduleVersion ;
	private final int reserved;
	private final int blockNumber;
	private final byte[] blockDataByte;

	/**
	 * This DSMCCMessageHeader is different form the one in DSMCC_UNMessageSection because the field transactionId is called downloadID in this one.
	 *
	 * @author Eric
	 */
		public record DSMCCMessageHeader(int protocolDiscriminator, int dsmccType, int messageId, long downloadId,
										 int reserved, int adaptationLength, int messageLength) implements TreeNode {

		public KVP getJTreeNode(int modus) {
				KVP t = new KVP("DSM-CC Message Header");
				t.add(new KVP("protocolDiscriminator", protocolDiscriminator));
				t.add(new KVP("dsmccType", dsmccType, getDSMCCTypeString(dsmccType)));
				t.add(new KVP("messageId", messageId, getMessageIDString(dsmccType, messageId)));
				t.add(new KVP("downloadId", downloadId));
				t.add(new KVP("reserved", reserved));
				t.add(new KVP("adaptationLength", adaptationLength));
				t.add(new KVP("messageLength", messageLength));

				return t;
			}


	}



	public DSMCC_DownLoadDataMessageSection(PsiSectionData raw_data, PID parent){
		super(raw_data, parent);

		int protocolDiscriminator = getInt(raw_data.getData(), 8, 1, Utils.MASK_8BITS);
		int dsmccType = getInt(raw_data.getData(), 9, 1, Utils.MASK_8BITS);
		int messageId = getInt(raw_data.getData(), 10, 2, Utils.MASK_16BITS);
		long downloadId = getLong(raw_data.getData(), 12, 4, Utils.MASK_32BITS);
		int reserved1 = getInt(raw_data.getData(), 16, 1, Utils.MASK_8BITS);
		int adaptationLength = getInt(raw_data.getData(), 17, 1, Utils.MASK_8BITS);
		int messageLength = getInt(raw_data.getData(), 18, 2, Utils.MASK_16BITS);
		header = new DSMCCMessageHeader(protocolDiscriminator, dsmccType, messageId, downloadId, reserved1, adaptationLength, messageLength);
		moduleId = getInt(raw_data.getData(), 20, 2, Utils.MASK_16BITS);
		moduleVersion = getInt(raw_data.getData(), 22, 1, Utils.MASK_8BITS);
		reserved = getInt(raw_data.getData(), 23, 1, Utils.MASK_8BITS);
		blockNumber = getInt(raw_data.getData(), 24, 2, Utils.MASK_16BITS);
		byte[] original = raw_data.getData();
		blockDataByte = copyOfRange(original, 26, 20 + header.messageLength()); //to = 26+messagelength - 6

	}

	public int getPayLoadLength(){
		if(header!=null){
			return  header.messageLength()-6;
		}
		return 0;
	}


	public byte[] getPayLoad(){
        return copyOfRange(raw_data.getData(), 26, (26+header.messageLength())-6);
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
		t.add(header.getJTreeNode(modus));
		t.add(new KVP("moduleId",moduleId));
		t.add(new KVP("moduleVersion",moduleVersion));
		t.add(new KVP("reserved",reserved));
		t.add(new KVP("blockNumber",blockNumber));
		t.add(new KVP("blockDataByte",blockDataByte));


		return t;
	}

	private static String getDSMCCTypeString(int dsmccType) {
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
			}
			return "reserved";

		}
	}


	private static String getMessageIDString(int dsmccType, int messageId) {
        return switch (dsmccType) {
            //		case 0x01:
            //			return "User-to-Network configuration message";
            //		case 0x02:
            //			return "User-to-Network session message";
            case 0x03 ->
                // "Download message";
                    switch (messageId) {
                        case 0x1001 -> "DownloadInfoRequest";
                        case 0x1002 -> "DownloadInfoIndication";
                        case 0x1003 -> "DownloadDataBlock";
                        case 0x1004 -> "DownloadDataRequest";
                        case 0x1005 -> "DownloadCancel";
                        case 0x1006 -> "DownloadServerInitiate";
                        default -> null;
                    };
            //		case 0x04:
            //			return "SDB Channel Change Protocol message";
            //		case 0x05:
            //			return "User-to- Network pass-thru message";

            default -> null;
        };
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
