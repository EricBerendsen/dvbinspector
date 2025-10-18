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
 */

package nl.digitalekabeltelevisie.data.mpeg.descriptors.dsmcc;

// based on 10.2.11 Compressed module descriptor ETSI EN 301 192 V1.4.2 (2008-04)
import static nl.digitalekabeltelevisie.util.Utils.MASK_32BITS;
import static nl.digitalekabeltelevisie.util.Utils.MASK_8BITS;
import static nl.digitalekabeltelevisie.util.Utils.getInt;
import static nl.digitalekabeltelevisie.util.Utils.getLong;

import nl.digitalekabeltelevisie.controller.KVP;

public class CompressedModuleDescriptor extends DSMCCDescriptor {

	private final int compression_method;
	private final long original_size;

	public CompressedModuleDescriptor(byte[] b) {
		super(b);
		compression_method = getInt(b,  2, 1,MASK_8BITS);
		original_size = getLong(b,  3, 4,MASK_32BITS);
	}

	@Override
	public KVP getJTreeNode(int modus) {
		KVP t = super.getJTreeNode(modus);
		t.add(new KVP("compression_method", compression_method));
		t.add(new KVP("original_size", original_size));
		return t;
	}

	public int getCompression_method() {
		return compression_method;
	}


	public long getOriginal_size() {
		return original_size;
	}

}
