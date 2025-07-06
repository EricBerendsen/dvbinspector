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

package nl.digitalekabeltelevisie.data.mpeg.dsmcc;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.util.Utils;

import static java.util.Arrays.copyOfRange;
import static nl.digitalekabeltelevisie.util.Utils.getInt;
import static nl.digitalekabeltelevisie.util.Utils.getLong;

public class BIOPObjectLocation extends LiteComponent {
	protected long carouselId;
	protected int moduleId;
	protected int version_major;
	protected int version_minor;
	protected int objectKey_length;
	protected byte[] objectKey_data_byte;


	public BIOPObjectLocation(byte[] data, int offset) {
		super(data, offset);
		carouselId= getLong(data, offset+5, 4, Utils.MASK_32BITS);
		moduleId= getInt(data, offset+9, 2, Utils.MASK_16BITS);
		version_major= getInt(data, offset+11, 1, Utils.MASK_8BITS);
		version_minor= getInt(data, offset+12, 1, Utils.MASK_8BITS);
		objectKey_length= getInt(data, offset+13, 1, Utils.MASK_8BITS);
		objectKey_data_byte = copyOfRange(data, offset + 14, offset + 14 + objectKey_length);

	}


	@Override
	public KVP getJTreeNode(int modus) {
		KVP t = new KVP("BIOP::ObjectLocation");
		t.add(new KVP("component_tag",component_tag, getComponentTagString((int)component_tag)));
		t.add(new KVP("component_data_length",component_data_length));
		t.add(new KVP("carouselId",carouselId));
		t.add(new KVP("moduleId",moduleId));
		t.add(new KVP("version.major",version_major));
		t.add(new KVP("version.minor",version_minor));
		t.add(new KVP("objectKey_length",objectKey_length));
		t.add(new KVP("objectKey_data_byte",objectKey_data_byte));
		return t;
	}


	public long getCarouselId() {
		return carouselId;
	}


	public int getModuleId() {
		return moduleId;
	}


	public int getVersion_major() {
		return version_major;
	}


	public int getVersion_minor() {
		return version_minor;
	}


	public int getObjectKey_length() {
		return objectKey_length;
	}


	public byte[] getObjectKey_data_byte() {
		return objectKey_data_byte;
	}

}
