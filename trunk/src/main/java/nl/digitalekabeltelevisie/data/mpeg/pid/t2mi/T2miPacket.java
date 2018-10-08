/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2018 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.pid.t2mi;


import static nl.digitalekabeltelevisie.util.Utils.*;

import java.util.*;
import java.util.logging.Logger;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.*;
import nl.digitalekabeltelevisie.data.mpeg.CRCcheck;
import nl.digitalekabeltelevisie.gui.utils.GuiUtils;
import nl.digitalekabeltelevisie.util.*;



public class T2miPacket implements TreeNode {
	

	public class IndividualAddressingData implements TreeNode{
		
		public class Function  implements TreeNode{
			
			
			private int function_tag;
			private int function_length; // defines the total length of the function() in bytes, including the function_tag, function_length and function_body() fields
			private byte[] body;

			public Function(BitSource bs) {
				
				function_tag = bs.readBits(8);
				function_length = bs.readBits(8); 
				
				body = bs.readBytes(function_length-2);
			}

			@Override
			public DefaultMutableTreeNode getJTreeNode(int modus) {
				DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("Function"));
				t.add(new DefaultMutableTreeNode(new KVP("function_tag",function_tag,function_type_list.get(function_tag))));
				t.add(new DefaultMutableTreeNode(new KVP("function_length",function_length," defines the total length of the function() in bytes, including the function_tag, function_length and function_body() fields")));
				if(body!=null) {
					t.add(new DefaultMutableTreeNode(new KVP("body",body,null)));
				}
				return t;
			}

			public void setFunction_tag(int function_tag) {
				this.function_tag = function_tag;
			}

			public void setFunction_length(int function_length) {
				this.function_length = function_length;
			}

			public void setBody(byte[] body) {
				this.body = body;
			}

			public int getFunction_tag() {
				return function_tag;
			}

			public int getFunction_length() {
				return function_length;
			}

			public byte[] getBody() {
				return body;
			}
			
		}
		
		private int tx_identifier;
		private int function_loop_length;
		private List<Function> functionList = new ArrayList<>();


		public IndividualAddressingData(BitSource bs) {
			tx_identifier = bs.readBits(16);
			function_loop_length = bs.readBits(8);
			int bytesRead = 0;
			while(bytesRead < function_loop_length) {
				Function f = new Function(bs);
				bytesRead += f.getFunction_length();
				functionList.add(f);
			}
		}
		

		@Override
		public DefaultMutableTreeNode getJTreeNode(int modus) {
			DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("IndividualAddressingData"));
			t.add(new DefaultMutableTreeNode(new KVP("tx_identifier",tx_identifier,null)));
			t.add(new DefaultMutableTreeNode(new KVP("function_loop_length",function_loop_length,null)));
			Utils.addListJTree(t,functionList,modus,"Functions");
			return t;
		}
		
		
	}
	private static final Logger LOGGER = Logger.getLogger(T2miPacket.class.getName());

	private static LookUpList packet_type_list = new LookUpList.Builder().
			add(0x00 ,"Baseband Frame").
			add(0x01 ,"Auxiliary stream I/Q data").
			add(0x02 ,"Arbitrary cell insertion").
			add(0x10 ,"L1-current").
			add(0x11 ,"L1-future").
			add(0x12 ,"P2 bias balancing cells").
			add(0x20 ,"DVB-T2 timestamp").
			add(0x21 ,"Individual addressing").
			add(0x30 ,"FEF part: Null").
			add(0x31 ,"FEF part: I/Q data").
			add(0x32 ,"FEF part: composite").
			add(0x33 ,"FEF sub-part"). 
			build();
	
	private static LookUpList bw_list = new LookUpList.Builder().
			add(0x00 ,"Bandwidth: 1,7 MHz, T2 Elementary period T: 71/131 µs, subseconds unit, Tsub µs: 1/131 µs").
			add(0x01 ,"Bandwidth: 5 MHz, T2 Elementary period T: 7/40 µs, subseconds unit, Tsub µs: 1/40 µs").
			add(0x02 ,"Bandwidth: 6 MHz, T2 Elementary period T: 7/48 µs, subseconds unit, Tsub µs: 1/48 µs").
			add(0x03 ,"Bandwidth: 7 MHz, T2 Elementary period T: 7/56 µs, subseconds unit, Tsub µs: 1/56 µs").
			add(0x04 ,"Bandwidth: 8 MHz, T2 Elementary period T: 7/64 µs, subseconds unit, Tsub µs: 1/64 µs").
			add(0x05 ,"Bandwidth: 10 MHz, T2 Elementary period T: 7/80  µs, subseconds unit, Tsub µs: 1/80 µs").
			build();
	
	private static LookUpList freq_source_list = new LookUpList.Builder().
			add(0x00 ,"the FREQUENCY field(s) of the DVB-T2 signal shall be according to the signalled value(s) in the L1-current data field of the T2-MI signal ").
			add(0x01 ,"the FREQUENCY field(s) of the DVB-T2 signal shall be according to the T2-MI frequency individual addressing function ").
			add(0x02 ," the FREQUENCY field(s) of the DVB-T2 signal shall be according to the manually set value(s) for each modulator. ").
			add(0x03 ,"reserved for future use").
			build();
	
	private static final LookUpList function_type_list = new LookUpList.Builder().
			add(0x00 ,"Transmitter time offset").
			add(0x01 ,"Transmitter frequency offset").
			add(0x02 ,"Transmitter power").
			add(0x03 ,"Private data").
			add(0x04 ,"Cell id").
			add(0x05 ,"Enable").
			add(0x06 ,"Bandwidth").
			add(0x10 ,"ACE-PAPR").
			add(0x11 ,"Transmitter MISO group").
			add(0x12 ,"TR-PAPR").
			add(0x13 ,"L1-ACE-PAPR").
			add(0x14 ,"TX-SIG FEF: Sequence Numbers").
			add(0x15 ,"TX-SIG Aux stream: Transmitter ID").
			add(0x16 ,"Frequency").
			build();

	
	// based on ETSI TS 102 773 V1.4.1 (2016-03)
	

	
	private byte[] payload;

	private int l1DynCurrLen;

	private int l1ExtLen;

	private int packetNo;
	
	

	public T2miPacket(byte[] result, int packetNo) {
		payload = result;
		this.packetNo = packetNo;
	}



	@Override
	public DefaultMutableTreeNode getJTreeNode(int modus) {
		DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("T2MI Packet"));
		t.add(new DefaultMutableTreeNode(new KVP("Start TS Packet No", packetNo, null)));
		t.add(new DefaultMutableTreeNode(new KVP("data", payload, null)));
		t.add(new DefaultMutableTreeNode(new KVP("packet_type",getPacketType(),getPacketTypeString(getPacketType()))));
		t.add(new DefaultMutableTreeNode(new KVP("packet_count",getPacketCount(),null)));
		t.add(new DefaultMutableTreeNode(new KVP("superframe_idx",getSuperframeIdx(),null)));
		t.add(new DefaultMutableTreeNode(new KVP("t2mi_stream_id",getT2miStreamId(),null)));
		int payloadLenBits = getPayloadLen();
		t.add(new DefaultMutableTreeNode(new KVP("payload_len",payloadLenBits,getLenInBytes(payloadLenBits))));
		DefaultMutableTreeNode payloadNode = new DefaultMutableTreeNode(new KVP("payload"));
		switch (getPacketType()) {
		case 0x00: // Baseband Frame 
			addBaseBandFrameTreeNode(payloadNode);
			break;
		case 0x10: // L1-current T2-MI packets
			addL1CurrentT2MIPacketsTreeNode(modus, payloadNode);
			break;
		case 0x20: // DVB-T2 timestamp
			addDVBT2TimestampTreeNode(payloadNode);
			break;
		case 0x21: // Individual addressing
			addIndividualAddressingTreeNode(modus, payloadNode);
			break;
		default:
			addNotImplementedTreeNode(payloadNode);
			break;
		}
		t.add(payloadNode);
		long crcResult = getCRCresult();
		String crcMsg = null;
		if(crcResult!= 0) {
			LOGGER.warning("CRC check failed for t2mi, result:"+crcResult+", packetNo:"+packetNo+", packetType:"+getPacketType());
			crcMsg = "CRC check failed!";
		}
		t.add(new DefaultMutableTreeNode(new KVP("crc32", payload, payload.length-4, 4 ,crcMsg)));
		return t;
	}



	/**
	 * @param payloadNode
	 */
	public void addNotImplementedTreeNode(DefaultMutableTreeNode payloadNode) {
		String packetType = getPacketTypeString(getPacketType());
		LOGGER.warning(packetType+" not implemented, T2mi packet start at TSD Packet:"+packetType);
		payloadNode.add(new DefaultMutableTreeNode(GuiUtils.getNotImplementedKVP(packetType)));
	}



	/**
	 * @param modus
	 * @param payloadNode
	 */
	public void addIndividualAddressingTreeNode(int modus, DefaultMutableTreeNode payloadNode) {
		int individual_addressing_length = getIndividualAddressingLength();
		payloadNode.add(new DefaultMutableTreeNode(new KVP("individual_addressing_length",individual_addressing_length,null)));
		BitSource bs1 = new BitSource(payload, 8, payload.length-4); // crc (4)
		List<IndividualAddressingData> individualAddressingDataList= new ArrayList<>();
		while(bs1.available()>0) {
			IndividualAddressingData individualAddressingData = new IndividualAddressingData(bs1);
			individualAddressingDataList.add(individualAddressingData);
		}
		
		Utils.addListJTree(payloadNode,individualAddressingDataList,modus,"Individual addressing");
	}



	/**
	 * @param payloadNode
	 */
	public void addDVBT2TimestampTreeNode(DefaultMutableTreeNode payloadNode) {
		payloadNode.add(new DefaultMutableTreeNode(new KVP("bw",getBW(),getBWString(getBW()))));
		payloadNode.add(new DefaultMutableTreeNode(new KVP("seconds_since_2000",getSecondsSince2000(),null)));
		payloadNode.add(new DefaultMutableTreeNode(new KVP("subseconds",getSubSeconds(),null)));
		payloadNode.add(new DefaultMutableTreeNode(new KVP("utco",getUtco(),null)));
	}



	/**
	 * @param modus
	 * @param payloadNode
	 */
	public void addL1CurrentT2MIPacketsTreeNode(int modus, DefaultMutableTreeNode payloadNode) {
		payloadNode.add(new DefaultMutableTreeNode(new KVP("frame_idx",getFrameIdx(),null)));
		payloadNode.add(new DefaultMutableTreeNode(new KVP("freq_source",getFreqSource(),getFreqSourceString(getFreqSource()))));
		DefaultMutableTreeNode l1currentData = new DefaultMutableTreeNode(new KVP("L1-current_data"));
		payloadNode.add(l1currentData);
		BitSource bs = new BitSource(payload, 8, payload.length-4); // crc (4)
		L1PreSignallingData l1PreSignallingData = new L1PreSignallingData(bs);
		l1currentData.add(l1PreSignallingData.getJTreeNode(modus));
		int l1ConfLen = bs.readBits(16);
		l1currentData.add(new DefaultMutableTreeNode(new KVP("L1CONF_LEN",l1ConfLen,getLenInBytes(l1ConfLen))));
		Configurable1PostSignalling configurable1PostSignalling = new Configurable1PostSignalling(bs, l1PreSignallingData);
		l1currentData.add(configurable1PostSignalling.getJTreeNode(modus));
		bs.skiptoByteBoundary();
		l1DynCurrLen = bs.readBits(16);
		l1currentData.add(new DefaultMutableTreeNode(new KVP("L1DYN_CURR_LEN",l1DynCurrLen,getLenInBytes(l1DynCurrLen))));
		DynamicL1PostSignalling dynamicL1PostSignalling = new DynamicL1PostSignalling(bs, configurable1PostSignalling);
		l1currentData.add(dynamicL1PostSignalling.getJTreeNode(modus));
		bs.skiptoByteBoundary();
		l1ExtLen = bs.readBits(16);
		l1currentData.add(new DefaultMutableTreeNode(new KVP("L1EXT_LEN",l1ExtLen,getLenInBytes(l1ExtLen))));
		if(l1ExtLen!=0){
			LOGGER.warning("L1-post extension field not implemented");
			l1currentData.add(new DefaultMutableTreeNode(GuiUtils.getNotImplementedKVP("L1-post extension field")));
		}
		LOGGER.warning("L1-current T2-MI packets bs.available:"+bs.available());
	}



	/**
	 * @param payloadNode
	 */
	public void addBaseBandFrameTreeNode(DefaultMutableTreeNode payloadNode) {
		payloadNode.add(new DefaultMutableTreeNode(new KVP("frame_idx",getFrameIdx(),null)));
		payloadNode.add(new DefaultMutableTreeNode(new KVP("plp_id",getPlpId(),null)));
		payloadNode.add(new DefaultMutableTreeNode(new KVP("intl_frame_start",getIntlFrameStart(),null)));
		
		// TODO add BBHEADER , see ETSI EN 302 307  Second generation framing structure 5.1.6 Base-Band Header insertion 
	}



	/**
	 * @return
	 */
	public long getCRCresult() {
		return CRCcheck.crc32(payload, payload.length);
	}



	/**
	 * @param payloadLenBits
	 * @return
	 */
	private static String getLenInBytes(int payloadLenBits) {
		return "bits = "+((payloadLenBits + 7) / 8) + " Bytes";
	}

	public int getPacketType() {
		return Byte.toUnsignedInt(payload[0]);
	}
	
	public static String getPacketTypeString(int type) {
		return packet_type_list.get(type);
	}
	
	public int getPacketCount() {
		return Byte.toUnsignedInt(payload[1]);
	}
	
	public int getSuperframeIdx() {
		return Byte.toUnsignedInt(payload[2])>>4;
	}
	
	public int getT2miStreamId() {
		return Byte.toUnsignedInt(payload[4])&Utils.MASK_3BITS;
	}
	
	public int getPayloadLen() {
		return 256 * Byte.toUnsignedInt(payload[4]) + Byte.toUnsignedInt(payload[5]);
		
	}

	// Baseband Frame 
	public int getFrameIdx() {
		return Byte.toUnsignedInt(payload[6]);
	}

	public int getPlpId() {
		return Byte.toUnsignedInt(payload[7]);
	}
	
	public int getIntlFrameStart() {
		return (Byte.toUnsignedInt(payload[8]) &0b1000_0000)>>7 ;
	}

	// L1-current T2-MI packets
	
	public int getFreqSource() {
		return Byte.toUnsignedInt(payload[7])>>6; 
	}

	public static String getFreqSourceString(int freq_source) {
		return freq_source_list.get(freq_source);
	}

	// DVB-T2 timestamp

	public int getBW() {
		return Byte.toUnsignedInt(payload[6]) & Utils.MASK_4BITS;
	}
	
	public static String getBWString(int bw) {
		return bw_list.get(bw);
	}


	public long getSecondsSince2000() {
		return getLong(payload, 7, 5, MASK_40BITS);
	}

	public long getSubSeconds() {
		return getLong(payload, 12, 4, MASK_32BITS)>>3;
	}

	public int getUtco() {
		return getInt(payload, 15, 2, MASK_13BITS);
	}
	
	public int getIndividualAddressingLength() {
		return getInt(payload, 7, 1, MASK_8BITS);
	}

}
