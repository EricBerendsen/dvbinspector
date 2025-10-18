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

package nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.ses;

import static nl.digitalekabeltelevisie.util.Utils.MASK_16BITS;
import static nl.digitalekabeltelevisie.util.Utils.getBytes;
import static nl.digitalekabeltelevisie.util.Utils.getInt;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

/**
 * 
 */
public class VirtualServiceIDDescriptor extends Descriptor {

	private int virtual_service_id;
	private byte[] reserved_future_use;

	public VirtualServiceIDDescriptor(byte[] b, TableSection parent) {
		super(b, parent);
		virtual_service_id = getInt(b, 2, 2, MASK_16BITS);
		reserved_future_use = getBytes(b, 4, descriptorLength -2);
	}

	
	@Override
	public String getDescriptorname(){
		return "Virtual Service ID Descriptor";
	}

	
	@Override
	public KVP getJTreeNode(int modus){

		KVP t = super.getJTreeNode(modus);
		t.add(new KVP("virtual_service_id", virtual_service_id));
		t.add(new KVP("reserved_future_use", reserved_future_use));
		return t;
	}


	public int getVirtual_service_id() {
		return virtual_service_id;
	}


	public byte[] getReserved_future_use() {
		return reserved_future_use;
	}

}
