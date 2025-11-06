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

import static nl.digitalekabeltelevisie.util.Utils.addListJTree;

import java.util.ArrayList;
import java.util.List;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.pes.video26x.RBSP;

/**
 * @author Eric
 *
 */
public class Decoding_capability_information_rbsp extends RBSP {

	private int dci_reserved_zero_4bits;
	private int dci_num_ptls_minus1;
	private List<ProfileTierLevel> profile_tier_level_list = new ArrayList<>();


	private int dci_extension_flag;	protected Decoding_capability_information_rbsp(byte[] rbsp_bytes, int numBytesInRBSP) {
		super(rbsp_bytes, numBytesInRBSP);

		dci_reserved_zero_4bits = bitSource.u(4);
		dci_num_ptls_minus1 = bitSource.u(4);
		for (int i = 0; i <= dci_num_ptls_minus1; i++) {
			ProfileTierLevel profile_tier_level = new ProfileTierLevel(1, 0, bitSource);
			profile_tier_level_list.add(profile_tier_level);
		}
		dci_extension_flag = bitSource.u(1);
//		if( dci_extension_flag != 0 )
//		while( more_rbsp_data( ) )
//		dci_extension_data_flag
//		= bitSource.u(1);
	}

	@Override
	public KVP getJTreeNode(int modus) {
		KVP t = new KVP("decoding_capability_information_rbsp");
		t.add(new KVP("dci_reserved_zero_4bits", dci_reserved_zero_4bits));
		t.add(new KVP("dci_num_ptls_minus1", dci_num_ptls_minus1));
		addListJTree(t, profile_tier_level_list, modus, "profile_tier_level_list");
		t.add(new KVP("dci_extension_flag", dci_extension_flag));
		return t;
	}

}
