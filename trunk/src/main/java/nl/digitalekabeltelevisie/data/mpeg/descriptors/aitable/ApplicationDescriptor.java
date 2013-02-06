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

package nl.digitalekabeltelevisie.data.mpeg.descriptors.aitable;

import static nl.digitalekabeltelevisie.util.Utils.*;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;


/**
 *
 */
public class ApplicationDescriptor extends AITDescriptor {

	private int application_profiles_length;
	private List<ApplicationProfile> applicationProfiles = new ArrayList<ApplicationProfile>();
	private int service_bound_flag;
	private int visibility;
	private int application_priority;
	private List<TransportProtocolLabel>transport_protocol_labels = new ArrayList<TransportProtocolLabel>();;

	public static class TransportProtocolLabel implements TreeNode{
		private int transport_protocol_label;

		/**
		 * @param transport_protocol_label
		 */
		public TransportProtocolLabel(final int transport_protocol_label) {
			super();
			this.transport_protocol_label = transport_protocol_label;
		}

		public DefaultMutableTreeNode getJTreeNode(final int modus){
			return new DefaultMutableTreeNode(new KVP("transport_protocol_label",transport_protocol_label,null));
		}
	}

	public static class ApplicationProfile implements TreeNode{

		private int application_profile;
		private int version_major;
		private int version_minor;
		private int version_micro;

		/**
		 * @param application_profile
		 * @param version_major
		 * @param version_minor
		 * @param version_micro
		 */
		public ApplicationProfile(final int application_profile, final int version_major, final int version_minor, final int version_micro) {
			super();
			this.application_profile = application_profile;
			this.version_major = version_major;
			this.version_minor = version_minor;
			this.version_micro = version_micro;
		}

		/* (non-Javadoc)
		 * @see nl.digitalekabeltelevisie.controller.TreeNode#getJTreeNode(int)
		 */
		public DefaultMutableTreeNode getJTreeNode(final int modus){
			final DefaultMutableTreeNode s=new DefaultMutableTreeNode(new KVP("application_profile ("+version_major+"."+version_minor+"."+version_micro+")"));
			s.add(new DefaultMutableTreeNode(new KVP("application_profile",application_profile,null)));
			s.add(new DefaultMutableTreeNode(new KVP("version_major",version_major,null)));
			s.add(new DefaultMutableTreeNode(new KVP("version_minor",version_minor,null)));
			s.add(new DefaultMutableTreeNode(new KVP("version_micro",version_micro,null)));
			return s;
		}
	}



	/**
	 * @param b
	 * @param offset
	 * @param parent
	 */
	public ApplicationDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset, parent);

		application_profiles_length = getInt(b, offset+2, 1, MASK_8BITS);
		int t=0;
		while (t<application_profiles_length) {
			final int application_profile = getInt(b, offset+3 +t, 2, MASK_16BITS);
			final int version_major = getInt(b, offset+5 +t, 1, MASK_8BITS);
			final int version_minor = getInt(b, offset+6 +t, 1, MASK_8BITS);
			final int version_micro = getInt(b, offset+7 +t, 1, MASK_8BITS);
			final ApplicationProfile prof = new ApplicationProfile(application_profile,version_major,version_minor,version_micro);
			applicationProfiles.add(prof);
			t += 5;
		}
		service_bound_flag = getInt(b, offset+3 +t, 1, 0x80)>>7;
		visibility = getInt(b, offset+3 +t, 1, 0x60)>>5;
		application_priority = getInt(b, offset+4 +t, 1, MASK_8BITS);
		int r= 3+t;
		while(r<descriptorLength){
			final int transport_protocol_label = getInt(b, offset+r+2, 1, MASK_8BITS);
			final TransportProtocolLabel transportProtocolLabel = new TransportProtocolLabel(transport_protocol_label);
			transport_protocol_labels.add(transportProtocolLabel);
			r++;
		}
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("application_profiles_length", application_profiles_length, null)));
		addListJTree(t,applicationProfiles,modus,"application_profiles");
		t.add(new DefaultMutableTreeNode(new KVP("service_bound_flag", service_bound_flag, service_bound_flag==1?"the application is only associated with the current service":"application not associated with the current service")));
		t.add(new DefaultMutableTreeNode(new KVP("visibility", visibility, getVisibilityString(visibility))));
		t.add(new DefaultMutableTreeNode(new KVP("application_priority", application_priority, null)));
		addListJTree(t,transport_protocol_labels,modus,"transport_protocol_labels");

		return t;
	}

	public static String getVisibilityString(final int v){

		switch (v) {
		case 0x00: return "NOT_VISIBLE_ALL (This application shall not be visible either to applications via an application listing API or to users via the navigator)"; //
		case 0x01: return "NOT_VISIBLE_USERS (This application shall not be visible to users but shall be visible to applications via an application listing API)";//
		case 0x02: return "reserved_future_use"; //
		case 0x03: return "VISIBLE_ALL (This application can be visible to users and shall be visible to applications via an application listing API)";
		default:

			return "illegal value";

		}
	}

}
