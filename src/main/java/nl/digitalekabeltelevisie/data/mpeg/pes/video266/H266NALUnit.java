/**
 * 
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 * 
 *  This code is Copyright 2009-2019 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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
package nl.digitalekabeltelevisie.data.mpeg.pes.video266;


import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.pes.video26x.AbstractNALUnit;
import nl.digitalekabeltelevisie.data.mpeg.pes.video26x.RBSP;

public class H266NALUnit extends AbstractNALUnit{

	private static final Logger logger = Logger.getLogger(H266NALUnit.class.getName());

	
	private final int forbidden_zero_bit;
	private final int nuh_reserved_zero_bit;
	private final H266NALUnitType nal_unit_type;
	private final int nuh_layer_id;
	private final int nuh_temporal_id_plus1;


	/**
	 * @param bytes
	 * @param offset
	 * @param len
	 */
	public H266NALUnit(final byte[] bytes, final int offset, final int len) {
		super(bytes, offset, len);

		forbidden_zero_bit = bs.f(1);
		nuh_reserved_zero_bit = bs.u(1);
		nuh_layer_id = bs.u(6);
		nal_unit_type = H266NALUnitType.getByType(bs.u(5));
		nuh_temporal_id_plus1 = bs.u(3);

		readRBSPBytes();
		createRBSP();
	}

	public KVP getJTreeNode(int modus) {
		KVP t = new KVP("NALUnit ("+nal_unit_type+")");
		t.add(new KVP("bytes",bytes,offset,numBytesInNALunit));
		t.add(new KVP("numBytesInNALunit",numBytesInNALunit));
		t.add(new KVP("forbidden_zero_bit",forbidden_zero_bit));
		t.add(new KVP("nuh_reserved_zero_bit",nuh_reserved_zero_bit));
		t.add(new KVP("nuh_layer_id",nuh_layer_id));
		t.add(new KVP("nal_unit_type",nal_unit_type.getType(),nal_unit_type.getDescription()));
		t.add(new KVP("nuh_temporal_id_plus1",nuh_temporal_id_plus1));

		t.add(new KVP("rbsp_byte",rbsp_byte,0,numBytesInRBSP));
		t.add(new KVP("NumBytesInRBSP",numBytesInRBSP));
		if(rbsp!=null){
			t.add(rbsp.getJTreeNode(modus));
		}
		return t;
	}



	public int getForbidden_zero_bit() {
		return forbidden_zero_bit;
	}


	public int getNuh_layer_id() {
		return nuh_layer_id;
	}

	public int getNuh_temporal_id_plus1() {
		return nuh_temporal_id_plus1;
	}

	@Override
	public String getNALUnitTypeString(int nal_unit_type) {
		return H266NALUnitType.getByType(nal_unit_type).getDescription();
	}

	@Override
	protected void createRBSP() {

		Class<? extends RBSP> rbspClass = nal_unit_type.getClazz();

		if (rbspClass != null) {
			try {
				Constructor<? extends RBSP> constr = rbspClass.getDeclaredConstructor(new Class[] { byte[].class, int.class });
				rbsp = constr.newInstance(rbsp_byte, numBytesInRBSP);
			} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			}
		}else {
			logger.info("unimplemented nal_unit_type:" +nal_unit_type.getType() + ", " + nal_unit_type.getDescription());

		}
	}

	public int getNuh_reserved_zero_bit() {
		return nuh_reserved_zero_bit;
	}

	public H266NALUnitType getNal_unit_type() {
		return nal_unit_type;
	}

}
