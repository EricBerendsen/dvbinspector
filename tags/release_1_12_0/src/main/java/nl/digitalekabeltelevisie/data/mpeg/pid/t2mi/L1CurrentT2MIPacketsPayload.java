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

import static nl.digitalekabeltelevisie.data.mpeg.pid.t2mi.T2miPacket.getLenInBytes;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.gui.utils.GuiUtils;
import nl.digitalekabeltelevisie.util.*;

public class L1CurrentT2MIPacketsPayload extends Payload {

	private static LookUpList freq_source_list = new LookUpList.Builder().
			add(0x00 ,"the FREQUENCY field(s) of the DVB-T2 signal shall be according to the signalled value(s) in the L1-current data field of the T2-MI signal ").
			add(0x01 ,"the FREQUENCY field(s) of the DVB-T2 signal shall be according to the T2-MI frequency individual addressing function ").
			add(0x02 ," the FREQUENCY field(s) of the DVB-T2 signal shall be according to the manually set value(s) for each modulator. ").
			add(0x03 ,"reserved for future use").
			build();
	
	private L1PreSignallingData l1PreSignallingData;
	private int l1ConfLen;
	private Configurable1PostSignalling configurable1PostSignalling;

	private int l1DynCurrLen;
	private DynamicL1PostSignalling dynamicL1PostSignalling;

	private int l1ExtLen;

	public L1CurrentT2MIPacketsPayload(byte[] data) {
		super(data);
		
		BitSource bs = new BitSource(data, 8, data.length-4); // crc (4)
		l1PreSignallingData = new L1PreSignallingData(bs);
		l1ConfLen = bs.readBits(16);
		configurable1PostSignalling = new Configurable1PostSignalling(bs, l1PreSignallingData);
		bs.skiptoByteBoundary();
		l1DynCurrLen = bs.readBits(16);
		dynamicL1PostSignalling = new DynamicL1PostSignalling(bs, configurable1PostSignalling);
		bs.skiptoByteBoundary();
		l1ExtLen = bs.readBits(16);

	}
	

	@Override
	public DefaultMutableTreeNode getJTreeNode(int modus) {
		DefaultMutableTreeNode payloadNode = new DefaultMutableTreeNode(new KVP("payload"));
		payloadNode.add(new DefaultMutableTreeNode(new KVP("frame_idx",getFrameIdx(),null)));
		payloadNode.add(new DefaultMutableTreeNode(new KVP("freq_source",getFreqSource(),getFreqSourceString(getFreqSource()))));
		DefaultMutableTreeNode l1currentData = new DefaultMutableTreeNode(new KVP("L1-current_data"));
		payloadNode.add(l1currentData);
		l1currentData.add(l1PreSignallingData.getJTreeNode(modus));
		l1currentData.add(new DefaultMutableTreeNode(new KVP("L1CONF_LEN",l1ConfLen,getLenInBytes(l1ConfLen))));
		l1currentData.add(configurable1PostSignalling.getJTreeNode(modus));
		l1currentData.add(new DefaultMutableTreeNode(new KVP("L1DYN_CURR_LEN",l1DynCurrLen,getLenInBytes(l1DynCurrLen))));
		l1currentData.add(dynamicL1PostSignalling.getJTreeNode(modus));
		l1currentData.add(new DefaultMutableTreeNode(new KVP("L1EXT_LEN",l1ExtLen,getLenInBytes(l1ExtLen))));
		if(l1ExtLen!=0){
			l1currentData.add(new DefaultMutableTreeNode(GuiUtils.getNotImplementedKVP("L1-post extension field")));
		}
		return payloadNode;
	}

	public int getFreqSource() {
		return Byte.toUnsignedInt(data[7])>>6; 
	}

	public static String getFreqSourceString(int freq_source) {
		return freq_source_list.get(freq_source);
	}

}
