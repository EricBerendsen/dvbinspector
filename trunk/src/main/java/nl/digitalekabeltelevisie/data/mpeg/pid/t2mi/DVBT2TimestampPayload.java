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

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.util.*;

public class DVBT2TimestampPayload extends Payload {

	private static LookUpList bw_list = new LookUpList.Builder().
			add(0x00 ,"Bandwidth: 1,7 MHz, T2 Elementary period T: 71/131 µs, subseconds unit, Tsub µs: 1/131 µs").
			add(0x01 ,"Bandwidth: 5 MHz, T2 Elementary period T: 7/40 µs, subseconds unit, Tsub µs: 1/40 µs").
			add(0x02 ,"Bandwidth: 6 MHz, T2 Elementary period T: 7/48 µs, subseconds unit, Tsub µs: 1/48 µs").
			add(0x03 ,"Bandwidth: 7 MHz, T2 Elementary period T: 7/56 µs, subseconds unit, Tsub µs: 1/56 µs").
			add(0x04 ,"Bandwidth: 8 MHz, T2 Elementary period T: 7/64 µs, subseconds unit, Tsub µs: 1/64 µs").
			add(0x05 ,"Bandwidth: 10 MHz, T2 Elementary period T: 7/80  µs, subseconds unit, Tsub µs: 1/80 µs").
			build();

	public DVBT2TimestampPayload(byte[] data) {
		super(data);
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(int modus) {
		DefaultMutableTreeNode payloadNode = new DefaultMutableTreeNode(new KVP("payload"));
		payloadNode.add(new DefaultMutableTreeNode(new KVP("bw",getBW(),getBWString(getBW()))));
		payloadNode.add(new DefaultMutableTreeNode(new KVP("seconds_since_2000",getSecondsSince2000(),null)));
		payloadNode.add(new DefaultMutableTreeNode(new KVP("subseconds",getSubSeconds(),null)));
		payloadNode.add(new DefaultMutableTreeNode(new KVP("utco",getUtco(),null)));
		return payloadNode;
	}

	
	public int getBW() {
		return Byte.toUnsignedInt(data[6]) & Utils.MASK_4BITS;
	}
	
	public static String getBWString(int bw) {
		return bw_list.get(bw);
	}


	public long getSecondsSince2000() {
		return getLong(data, 7, 5, MASK_40BITS);
	}

	public long getSubSeconds() {
		return getLong(data, 12, 4, MASK_32BITS)>>3;
	}

	public int getUtco() {
		return getInt(data, 15, 2, MASK_13BITS);
	}

	
}
