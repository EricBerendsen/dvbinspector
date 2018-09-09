/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2014 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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
 * @author Eric
 */

package nl.digitalekabeltelevisie.data.mpeg.descriptors;

import static nl.digitalekabeltelevisie.util.Utils.*;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

public class FTAContentManagmentDescriptor extends Descriptor {


	private final int user_defined;
	private int reserved_future_use;
	private int do_not_scramble;
	private int control_remote_access_over_internet;
	private int do_not_apply_revocation;


	public FTAContentManagmentDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset,parent);
		user_defined = getInt(b, offset+2, 1, 0X80)>>7;
		reserved_future_use = getInt(b, offset+2, 1, 0X70)>>4;
		do_not_scramble = getInt(b, offset+2, 1, 0X08)>>3;
		control_remote_access_over_internet = getInt(b, offset+2, 1, 0X06)>>1;
		do_not_apply_revocation = getInt(b, offset+2, 1, 0X01);
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){

		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("user_defined",user_defined,null)));
		t.add(new DefaultMutableTreeNode(new KVP("reserved_future_use",reserved_future_use,null)));
		t.add(new DefaultMutableTreeNode(new KVP("do_not_scramble",do_not_scramble,do_not_scramble==1?"scrambling shall not be appied for the purpose of content protection":"scrambling shall be appied where applicable for content protection")));
		t.add(new DefaultMutableTreeNode(new KVP("control_remote_access_over_internet",control_remote_access_over_internet,getControlRemoteAccesOverInternetString(control_remote_access_over_internet))));
		t.add(new DefaultMutableTreeNode(new KVP("do_not_apply_revocation",do_not_apply_revocation,do_not_apply_revocation==1?"content revocation process shall not be applied":"content revocation process shall be applied")));

		return t;
	}

	public static String getControlRemoteAccesOverInternetString(final int control_remote_access_over_internet) {
		switch (control_remote_access_over_internet) {

		case 0x00:
			 return "Redistribution over the Internet is enabled";
		case 0x01:
			return "Redistribution over the Internet is enabled but only within a managed domain";
		case 0x02:
			 return "Redistribution over the Internet is enabled but only within a managed domain and after a certain short period of time (e.g. 24 hours)";
		case 0x03:
			return "Redistribution over the Internet is not allowed with the following exception: Redistribution over the Internet within a managed domain is enabled after a specified long (possibly indefinite) period of time";
		default:
			return "Illegal value";

		}
	}
}
