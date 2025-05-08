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

import static nl.digitalekabeltelevisie.util.Utils.getBytes;

import nl.digitalekabeltelevisie.controller.DVBString;
import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

/**
 * 
 */
public class ServiceListNameDescriptor extends Descriptor {

	private byte[] iso_639_2_language_code;
	private DVBString service_list_name;

	public ServiceListNameDescriptor(byte[] b, TableSection parent) {
		super(b, parent);
		iso_639_2_language_code = getBytes(b, 2, 3);
		service_list_name = new DVBString(b, 5, descriptorLength - 3);
	}

	
	@Override
	public String getDescriptorname(){
		return "Service List Name Descriptor";
	}

	@Override
	public KVP getJTreeNode(final int modus){

		KVP t = (KVP)super.getJTreeNode(modus);
		t.add(new KVP("ISO_639-2_language_code", iso_639_2_language_code));
		t.add(new KVP("service_list_name", service_list_name));
		return t;
	}

	

}
