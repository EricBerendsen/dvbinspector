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

package nl.digitalekabeltelevisie.data.mpeg.descriptors.aitable;

import static nl.digitalekabeltelevisie.data.mpeg.psi.AITsection.getApplicationIDString;
import static nl.digitalekabeltelevisie.util.Utils.*;

import java.util.*;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.*;
import nl.digitalekabeltelevisie.data.mpeg.psi.*;
import nl.digitalekabeltelevisie.util.Utils;


 // based on ETSI TS 102 809 V1.3.1 (2017-06) 5.3.5.7 External application authorization descriptor 

public class ExternalApplicationAuthorizationDescriptor extends AITDescriptor {
	private List<ExternalAuthorization> externalAuthorizations = new ArrayList<>();



	public static class ExternalAuthorization implements TreeNode{

		private final byte[] application_identifier ;
		private final int application_priority ;
	


		public ExternalAuthorization(final byte[] application_identifier, final int application_priority){
			this.application_identifier = application_identifier;
			this.application_priority = application_priority;
		}


		public DefaultMutableTreeNode getJTreeNode(final int modus){
			final DefaultMutableTreeNode s=new DefaultMutableTreeNode(new KVP("external authorization"));
			
			long organisation_id = Utils.getLong(application_identifier, 0, 4, Utils.MASK_32BITS);
			int application_id = Utils.getInt(application_identifier, 4, 2, Utils.MASK_16BITS);
			
			DefaultMutableTreeNode applicationIdNode = new DefaultMutableTreeNode(new KVP("application_identifier",application_identifier,null));
			applicationIdNode.add(new DefaultMutableTreeNode(new KVP("organisation_id", organisation_id, getMHPOrganistionIdString(organisation_id))));
			applicationIdNode.add(new DefaultMutableTreeNode(new KVP("application_id", application_id, getApplicationIDString(application_id))));

			s.add(applicationIdNode);
			s.add(new DefaultMutableTreeNode(new KVP("application_priority",application_priority,null)));
			return s;
		}


	}

	public ExternalApplicationAuthorizationDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset,parent);
		int t=0;
		while (t<descriptorLength) {
			byte [] application_identifier = Arrays.copyOfRange(b, offset+t+2, offset+t+2+6); //  application_identifier = 48 bits

			final int application_priority = getInt(b, offset+t+2+6+1, 1, MASK_8BITS);

			final ExternalAuthorization s = new ExternalAuthorization(application_identifier, application_priority);
			externalAuthorizations.add(s);
			t+=7;
		}
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		addListJTree(t,externalAuthorizations,modus,"external authorizations");
		return t;
	}
}
