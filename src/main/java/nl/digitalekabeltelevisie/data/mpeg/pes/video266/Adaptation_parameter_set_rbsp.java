/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2023 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.pes.video26x.RBSP;
import nl.digitalekabeltelevisie.util.LookUpList;

/**
 * @author Eric
 * 7.3.2.6 Adaptation parameter set RBSP syntax
 */
public class Adaptation_parameter_set_rbsp extends RBSP {
	
	LookUpList aps_params_type_list = new LookUpList.Builder().
			add(0, "ALF_APS").
			add(1, "LMCS_APS").
			add(2, "SCALING_APS").
			build();
	
	private int aps_params_type;
	private int aps_adaptation_parameter_set_id;
	private int aps_chroma_present_flag;


	protected Adaptation_parameter_set_rbsp(byte[] rbsp_bytes, int numBytesInRBSP) {
		super(rbsp_bytes, numBytesInRBSP);
		
		aps_params_type = bitSource.u(3);
		aps_adaptation_parameter_set_id = bitSource.u(5);
		aps_chroma_present_flag = bitSource.u(1);
	}

	@Override
	public KVP getJTreeNode(int modus) {
		KVP t = new KVP("adaptation_parameter_set_rbsp");
		t.add(new KVP("aps_params_type", aps_params_type, aps_params_type_list.get(aps_params_type, "Reserved")));
		t.add(new KVP("aps_adaptation_parameter_set_id", aps_adaptation_parameter_set_id));
		t.add(new KVP("aps_chroma_present_flag", aps_chroma_present_flag,
				aps_chroma_present_flag == 1 ? "APS NAL unit could include chroma related syntax elements"
						: "APS NAL unit does not include chroma related syntax elements"));

		return t;
	}

}
