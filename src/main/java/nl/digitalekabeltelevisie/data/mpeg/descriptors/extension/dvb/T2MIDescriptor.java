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
 */

package nl.digitalekabeltelevisie.data.mpeg.descriptors.extension.dvb;

import static nl.digitalekabeltelevisie.util.Utils.*;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

public class T2MIDescriptor extends DVBExtensionDescriptor {


	// T2-MI descriptor 0x11

	private final int reserved_zero_future_use;
	private final int t2mi_stream_id;
	private final int reserved_zero_future_use2;
	private final int num_t2mi_streams_minus_one;
	private final int reserved_zero_future_use3;
	private final int pcr_iscr_common_clock_flag;
	private final byte[] reserved_zero_future_use4;

	public T2MIDescriptor(byte[] b, TableSection parent) {
		super(b, parent);
		reserved_zero_future_use = getInt(b, 3, 1, 0b1111_1000)>>3;
		t2mi_stream_id = getInt(b, 3, 1, MASK_3BITS);
		reserved_zero_future_use2 = getInt(b, 4, 1, 0b1111_1000)>>3;
		num_t2mi_streams_minus_one = getInt(b, 4, 1, MASK_3BITS);
		reserved_zero_future_use3 = getInt(b, 5, 1, 0b1111_1110)>>1;
		pcr_iscr_common_clock_flag = getInt(b, 5, 1, MASK_1BIT);
		reserved_zero_future_use4 =  getBytes(b, 6, descriptorLength-4);
	}

    @Override
    public KVP getJTreeNode(int modus) {

        KVP t = super.getJTreeNode(modus);
        t.add(new KVP("reserved_zero_future_use", reserved_zero_future_use));
        t.add(new KVP("t2mi_stream_id", t2mi_stream_id));
        t.add(new KVP("reserved_zero_future_use (2)", reserved_zero_future_use2));
        t.add(new KVP("num_t2mi_streams_minus_one", num_t2mi_streams_minus_one));
        t.add(new KVP("reserved_zero_future_use (3)", reserved_zero_future_use3));
        t.add(new KVP("pcr_iscr_common_clock_flag", pcr_iscr_common_clock_flag));
        t.add(new KVP("reserved_zero_future_use (4)", reserved_zero_future_use4));

        return t;
    }

}
