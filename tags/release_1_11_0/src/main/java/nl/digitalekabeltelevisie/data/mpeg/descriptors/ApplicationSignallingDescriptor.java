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

package nl.digitalekabeltelevisie.data.mpeg.descriptors;

import static nl.digitalekabeltelevisie.util.Utils.*;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

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
public class ApplicationSignallingDescriptor extends Descriptor {

	private List<ApplicationType> applicationTypeList = new ArrayList<ApplicationType>();

	public static class ApplicationType implements TreeNode{
		private final int applicationType;
		private final int aitVersionNumber;

		public ApplicationType(final int applicationType, final int versionNumber) {
			super();
			this.applicationType = applicationType;
			this.aitVersionNumber = versionNumber;
		}

		public DefaultMutableTreeNode getJTreeNode(final int modus){
			final DefaultMutableTreeNode s=new DefaultMutableTreeNode(new KVP("AIT"));
			s.add(new DefaultMutableTreeNode(new KVP("application_type",applicationType,getAppTypeIDString(applicationType))));
			s.add(new DefaultMutableTreeNode(new KVP("AIT_version_number",aitVersionNumber,null)));
			return s;
		}

	}



	public ApplicationSignallingDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset, parent);
		int r =0;
		while (r<descriptorLength) {
			final int application_type = getInt(b,offset+ 2+r, 2, Utils.MASK_15BITS);
			final int ait_version = getInt(b,offset+ 4+r, 1, Utils.MASK_5BITS);

			final ApplicationType applicationEntry = new ApplicationType(application_type,ait_version);
			applicationTypeList.add(applicationEntry);
			r=r+3;
		}
	}






	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);

		addListJTree(t,applicationTypeList,modus,"Application Information Table");

		return t;
	}
}
