package nl.digitalekabeltelevisie.data.mpeg.descriptors.dsmcc;

import static nl.digitalekabeltelevisie.util.Utils.MASK_16BITS;
import static nl.digitalekabeltelevisie.util.Utils.MASK_8BITS;
import static nl.digitalekabeltelevisie.util.Utils.getInt;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.util.LookUpList;

/**
 * 
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 * 
 *  This code is Copyright 2020 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

//based on 10.2.5 Module link descriptor, ETSI EN 301 192 V1.6.1 (2015-08)

public class ModuleLinkDescriptor extends DSMCCDescriptor {
	
	private static LookUpList positionList = new LookUpList.Builder()
			.add(0, "first module of the list")
			.add(1, "intermediate  module in the list")
			.add(2, "last module of the list")
			.build();

	private int position;
	private int module_id;
	
	public ModuleLinkDescriptor(byte[] b) {
		super(b);
		position = getInt(b,  2, 1,MASK_8BITS);
		module_id = getInt(b, 3, 2,MASK_16BITS);
	}

	@Override
	public KVP getJTreeNode(int modus) {
		KVP t = super.getJTreeNode(modus);
		t.add(new KVP("position", position, positionList.get(position, "illegal value")));
		t.add(new KVP("module_id", module_id));
		return t;
	}

}
