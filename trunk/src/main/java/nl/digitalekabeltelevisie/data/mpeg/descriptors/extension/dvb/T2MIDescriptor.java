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

package nl.digitalekabeltelevisie.data.mpeg.descriptors.extension.dvb;

import static nl.digitalekabeltelevisie.util.Utils.*;

import javax.swing.tree.DefaultMutableTreeNode;

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
	private final int reserved_zero_future_use4;

	public T2MIDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset,parent);
		reserved_zero_future_use = getInt(b, offset+3, 1, 0b1111_1000)>>3;
		t2mi_stream_id = getInt(b, offset+3, 1, MASK_3BITS);
		reserved_zero_future_use2 = getInt(b, offset+4, 1, 0b1111_1000)>>3;
		num_t2mi_streams_minus_one = getInt(b, offset+4, 1, MASK_3BITS);
		reserved_zero_future_use3 = getInt(b, offset+5, 1, 0b1111_1110)>>1;
		pcr_iscr_common_clock_flag = getInt(b, offset+5, 1, MASK_1BIT);	
		//TODO real life test data shows this descriptor as having only 3 bytes after the descriptor_tag_extension, so this last reserved byte is missing
		reserved_zero_future_use4 = getInt(b, offset+6, 1, MASK_8BITS);	
	}


	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){

		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("reserved_zero_future_use",reserved_zero_future_use,null)));
		t.add(new DefaultMutableTreeNode(new KVP("t2mi_stream_id",t2mi_stream_id,null)));
		t.add(new DefaultMutableTreeNode(new KVP("reserved_zero_future_use (2)",reserved_zero_future_use2,null)));
		t.add(new DefaultMutableTreeNode(new KVP("num_t2mi_streams_minus_one",num_t2mi_streams_minus_one,null)));
		t.add(new DefaultMutableTreeNode(new KVP("reserved_zero_future_use (3)",reserved_zero_future_use3,null)));
		t.add(new DefaultMutableTreeNode(new KVP("pcr_iscr_common_clock_flag",pcr_iscr_common_clock_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("reserved_zero_future_use (4)",reserved_zero_future_use4,null)));

		return t;
	}




}
