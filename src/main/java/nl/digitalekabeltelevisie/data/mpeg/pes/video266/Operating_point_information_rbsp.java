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

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.pes.video26x.RBSP;

/**
 * Based on Rec. ITU-T H.266 (04/2022) 7.3.2.2 Operating point information RBSP syntax
 *
 */
public class Operating_point_information_rbsp extends RBSP {

	private int opi_ols_info_present_flag;
	private int opi_htid_info_present_flag;
	private int opi_ols_idx;
	private int opi_htid_plus1;
	private int opi_extension_flag;

	protected Operating_point_information_rbsp(byte[] rbsp_bytes, int numBytesInRBSP) {
		super(rbsp_bytes, numBytesInRBSP);

		opi_ols_info_present_flag = bitSource.u(1);
		opi_htid_info_present_flag = bitSource.u(1);
		if (opi_ols_info_present_flag != 0)
			opi_ols_idx = bitSource.ue();
		if (opi_htid_info_present_flag != 0) {
			opi_htid_plus1 = bitSource.u(3);
		}
		opi_extension_flag = bitSource.u(1);
		//		if( opi_extension_flag != 0) {
//		while( more_rbsp_data( ) ) {
//		opi_extension_data_flag
//		= bitSource.u(1);
//		}}
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(int modus) {
		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("operating_point_information_rbsp"));
		t.add(new DefaultMutableTreeNode(new KVP("opi_ols_info_present_flag",opi_ols_info_present_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("opi_htid_info_present_flag",opi_htid_info_present_flag,null)));
		if (opi_ols_info_present_flag != 0)
			t.add(new DefaultMutableTreeNode(new KVP("opi_ols_idx",opi_ols_idx,null)));
		if (opi_htid_info_present_flag != 0) {
			t.add(new DefaultMutableTreeNode(new KVP("opi_htid_plus1",opi_htid_plus1,null)));
		}
		t.add(new DefaultMutableTreeNode(new KVP("opi_extension_flag",opi_extension_flag,null)));
		return t;
	}

}
