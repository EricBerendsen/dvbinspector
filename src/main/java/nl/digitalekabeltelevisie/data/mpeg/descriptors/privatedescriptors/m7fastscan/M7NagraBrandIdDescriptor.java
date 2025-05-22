/**
 * 
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 * 
 *  This code is Copyright 2009-2020 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.m7fastscan;

import static nl.digitalekabeltelevisie.util.Utils.*;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

public class M7NagraBrandIdDescriptor extends M7Descriptor {

	private final int nagra_brand_id;
	private final int ca_system_ID;
	private final int emm_stored;
	private final int reserved;
	byte[] emm_brand_ids = new byte[0];
	
	public M7NagraBrandIdDescriptor(byte[] b, TableSection parent) {
		super(b, parent);
		nagra_brand_id = getInt(b, 2, 1, MASK_8BITS);
		ca_system_ID = getInt(b, 3, 2, MASK_16BITS);
		emm_stored = getInt(b, 5, 1, 0x80) >>> 7;
		reserved = getInt(b, 5, 1, MASK_7BITS);
		if (emm_stored == 0) {
			emm_brand_ids = getBytes(b, 6, descriptorLength - 4);
		}
	}

	@Override
	public KVP getJTreeNode(final int modus) {
		final KVP t = super.getJTreeNode(modus);
		t.add(new KVP("nagra_brand_id", nagra_brand_id));
		t.add(new KVP("CA_system_ID", ca_system_ID));
		t.add(new KVP("emm_stored", emm_stored));
		t.add(new KVP("reserved", reserved));
		if (emm_stored == 0) {
			t.add(new KVP("emm_brand_ids", emm_brand_ids));
		}
		return t;
	}

}
