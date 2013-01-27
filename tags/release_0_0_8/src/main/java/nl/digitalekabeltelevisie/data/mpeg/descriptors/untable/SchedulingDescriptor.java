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

package nl.digitalekabeltelevisie.data.mpeg.descriptors.untable;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.Utils;

/**
 * @author Eric Berendsen
 *
 */
public class SchedulingDescriptor extends UNTDescriptor {

	private final byte[] start_date_time;
	private final byte[] end_date_time;


	private final int final_availability;
	private final int periodicity_flag;
	private final int period_unit;
	private final int duration_unit;
	private final int estimated_cycle_time_unit ;
	private final int period;
	private final int duration;
	private final int estimated_cycle_time;
	private final byte[] privateDataByte;

	/**
	 * @param b
	 * @param offset
	 * @param parent
	 */
	public SchedulingDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset, parent);

		start_date_time= Utils.copyOfRange(b,offset+2,offset+7 );
		end_date_time= Utils.copyOfRange(b,offset+7,offset+12 );
		final_availability = Utils.getInt(b, offset + 12, 1, 0x80)>>7;
		periodicity_flag = Utils.getInt(b, offset + 12, 1, 0x40)>>6;
		period_unit = Utils.getInt(b, offset + 12, 1, 0x30)>>4;
		duration_unit = Utils.getInt(b, offset + 12, 1, 0x0C)>>2;
		estimated_cycle_time_unit = Utils.getInt(b, offset + 12, 1, Utils.MASK_2BITS);
		period = Utils.getInt(b, offset + 13, 1, Utils.MASK_8BITS);
		duration = Utils.getInt(b, offset + 14, 1, Utils.MASK_8BITS);
		estimated_cycle_time = Utils.getInt(b, offset + 15, 1, Utils.MASK_8BITS);
		privateDataByte = Utils.copyOfRange(b, offset+16, offset+descriptorLength+2);



	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("start_date_time", start_date_time, Utils.getUTCFormattedString(start_date_time))));
		t.add(new DefaultMutableTreeNode(new KVP("end_date_time", end_date_time, Utils.getUTCFormattedString(end_date_time))));
		t.add(new DefaultMutableTreeNode(new KVP("final_availability", final_availability, null)));
		t.add(new DefaultMutableTreeNode(new KVP("periodicity_flag", periodicity_flag, null)));
		t.add(new DefaultMutableTreeNode(new KVP("period_unit", period_unit, getTimeUnitsCodingString(period_unit))));
		t.add(new DefaultMutableTreeNode(new KVP("duration_unit", duration_unit, getTimeUnitsCodingString(duration_unit))));
		t.add(new DefaultMutableTreeNode(new KVP("estimated_cycle_time_unit", estimated_cycle_time_unit, getTimeUnitsCodingString(estimated_cycle_time_unit))));
		t.add(new DefaultMutableTreeNode(new KVP("period", period, null)));
		t.add(new DefaultMutableTreeNode(new KVP("duration", duration, null)));
		t.add(new DefaultMutableTreeNode(new KVP("estimated_cycle_time", estimated_cycle_time, null)));
		t.add(new DefaultMutableTreeNode(new KVP("private_data_byte",privateDataByte ,null)));
		return t;
	}


	public static String getTimeUnitsCodingString(final int t){

		switch (t) {
		case 0x00: return"Second";
		case 0x01: return"Minute";
		case 0x02: return"Hour";
		case 0x03: return"Day";

		default:
			return "illegal value";
		}
	}


}
