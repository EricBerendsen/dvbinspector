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

package nl.digitalekabeltelevisie.data.mpeg.psi;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.PID;
import nl.digitalekabeltelevisie.data.mpeg.PsiSectionData;
import nl.digitalekabeltelevisie.util.Utils;


public class TDTsection extends TableSection {

	private byte[] UTC_time;

	public TDTsection(final PsiSectionData raw_data, final PID parent){
		super(raw_data,parent);
		if(!isCrc_error()){
			UTC_time= Utils.copyOfRange(raw_data.getData(),3,8 );
		}
	}



	@Override
	public String toString(){
		final StringBuilder b = new StringBuilder("TDTsection UTC_Time=");
		b.append(Utils.toHexString(UTC_time)).append(", UTC_timeString=").append(getUTC_timeString()).append(", length=").append(getSectionLength());
		return b.toString();
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){

		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("UTC_time",UTC_time,Utils.getUTCFormattedString(UTC_time))));
		return t;
	}


	public byte[] getUTC_time() {
		return UTC_time;
	}



	public String getUTC_timeString() {
		return Utils.getUTCFormattedString(UTC_time);
	}

}
