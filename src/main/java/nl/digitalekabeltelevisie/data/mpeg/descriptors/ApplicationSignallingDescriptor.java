/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2020 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.descriptors;

import static nl.digitalekabeltelevisie.util.Utils.addListJTree;
import static nl.digitalekabeltelevisie.util.Utils.getAppTypeIDString;
import static nl.digitalekabeltelevisie.util.Utils.getInt;

import java.util.ArrayList;
import java.util.List;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.Utils;

/**
 * Based on ETSI TS 102 809 V1.1.1 (2010-01) "Signalling and carriage of interactive applications and services in Hybrid broadcast/broadband environments"
 * 5.3.5.1 Application signalling descriptor
 * @author Eric
 *
 */
public class ApplicationSignallingDescriptor extends Descriptor{

	private List<ApplicationType> applicationTypeList = new ArrayList<>();

	public record ApplicationType(int applicationType, int aitVersionNumber) implements TreeNode {

		public KVP getJTreeNode(final int modus) {
			KVP s = new KVP("AIT");
			s.add(new KVP("application_type", applicationType, getAppTypeIDString(applicationType)));
			s.add(new KVP("AIT_version_number", aitVersionNumber));
			return s;
		}

	}

	public ApplicationSignallingDescriptor(byte[] b, TableSection parent) {
		super(b, parent);
		int r =0;
		while (r<descriptorLength) {
			int application_type = getInt(b, 2+r, 2, Utils.MASK_15BITS);
			int ait_version = getInt(b, 4+r, 1, Utils.MASK_5BITS);

			ApplicationType applicationEntry = new ApplicationType(application_type,ait_version);
			applicationTypeList.add(applicationEntry);
			r=r+3;
		}
	}

	@Override
	public KVP getJTreeNode(final int modus){
		final KVP t = super.getJTreeNode(modus);
		addListJTree(t,applicationTypeList,modus,"Application Information Table");

		return t;
	}

	public List<ApplicationType> getApplicationTypeList() {
		return applicationTypeList;
	}
}
