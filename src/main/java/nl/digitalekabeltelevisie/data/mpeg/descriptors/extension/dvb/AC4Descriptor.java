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
import nl.digitalekabeltelevisie.util.LookUpList;

public class AC4Descriptor extends DVBExtensionDescriptor {
	
	private LookUpList ac4_channel_mode_list = new LookUpList.Builder().
			add(0 ,"Mono content").
			add(1 ,"Stereo content").
			add(2 ,"Multichannel content").
			add(3 ,"Reserved for future use").
			build();

    private int ac4_config_flag;
	private int ac4_toc_flag;
	private int reserved_zero_future_use;
	private int ac4_dialog_enhancement_enabled;
	private int ac4_channel_mode;
	private int reserved_zero_future_use2;
	private int ac4_toc_len;
	private byte[] ac4_dsi_byte;
	private byte[] additional_info_byte;
	

	public AC4Descriptor(byte[] b, TableSection parent) {
		super(b, parent);

		ac4_config_flag = getInt(b,  3, 1, 0x80) >> 7;
		ac4_toc_flag = getInt(b, 3, 1, 0x40) >> 6;
		reserved_zero_future_use = getInt(b, 3, 1, MASK_6BITS);
		int off = 4;
		if (ac4_config_flag == 1) {
			ac4_dialog_enhancement_enabled = getInt(b, off, 1, 0x80) >> 7;
			ac4_channel_mode = getInt(b, off, 1, 0x60) >> 5;
			reserved_zero_future_use2 = getInt(b, off, 1, MASK_5BITS);
			off++;
		}
		if (ac4_toc_flag == 1) {
			ac4_toc_len = getInt(b, off++, 1, MASK_8BITS);
			ac4_dsi_byte = getBytes(b, off, ac4_toc_len);
			off += ac4_toc_len;
		}
		if (off < (descriptorLength + 2)) {
			additional_info_byte = getBytes(b, off, descriptorLength + 2 - off);
		}

	}

	@Override
	public KVP getJTreeNode(int modus) {

		KVP t = super.getJTreeNode(modus);
		t.add(new KVP("ac4_config_flag", ac4_config_flag));
		t.add(new KVP("ac4_toc_flag", ac4_toc_flag));
		t.add(new KVP("reserved_zero_future_use", reserved_zero_future_use));

		if (ac4_config_flag == 1) {
			t.add(new KVP("ac4_dialog_enhancement_enabled", ac4_dialog_enhancement_enabled));
			t.add(new KVP("ac4_channel_mode", ac4_channel_mode, ac4_channel_mode_list.get(ac4_channel_mode)));
			t.add(new KVP("reserved_zero_future_use", reserved_zero_future_use2));
		}
		
		if (ac4_toc_flag == 1) {
			t.add(new KVP("ac4_toc_len", ac4_toc_len));
			t.add(new KVP("ac4_dsi_byte", ac4_dsi_byte));
		}
		if(additional_info_byte!=null) {
			t.add(new KVP("additional_info_byte", additional_info_byte));
		}

		return t;
	}

}
