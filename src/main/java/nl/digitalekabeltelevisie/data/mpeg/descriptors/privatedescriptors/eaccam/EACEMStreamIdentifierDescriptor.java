/**
 * 
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 * 
 *  This code is Copyright 2009-2022 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.eaccam;

import static nl.digitalekabeltelevisie.util.Utils.MASK_8BITS;
import static nl.digitalekabeltelevisie.util.Utils.getInt;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

public class EACEMStreamIdentifierDescriptor extends Descriptor {


	private final int  version_byte;

	public EACEMStreamIdentifierDescriptor(byte[] b, TableSection parent) {
		super(b, parent);
		version_byte = getInt(b, 2, 1, MASK_8BITS);
	}


	@Override
	public String toString() {
		return super.toString() + "version_byte="+version_byte;
	}


	@Override
	public String getDescriptorname(){
		return "Eacem Stream Identifier Descriptor";
	}



	@Override
	public KVP getJTreeNode(int modus){
		KVP t = super.getJTreeNode(modus);
		t.add(new KVP("version_byte",version_byte ,"This 8 bit integer identifies the version of IEC 62216 that broadcasts conform to"));
		return t;
	}

}
