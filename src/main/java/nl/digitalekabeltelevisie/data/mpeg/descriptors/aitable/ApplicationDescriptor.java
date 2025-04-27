/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2025 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;


/**
 *
 */
public class ApplicationDescriptor extends AITDescriptor {

	private int application_profiles_length;
	private List<ApplicationProfile> applicationProfiles = new ArrayList<>();
	private int service_bound_flag;
	private int visibility;
	private int application_priority;
	private List<TransportProtocolLabel>transport_protocol_labels = new ArrayList<>();

	record TransportProtocolLabel(int transport_protocol_label) implements TreeNode{

		@Override
		public KVP getJTreeNode(int modus){
			return new KVP("transport_protocol_label",transport_protocol_label);
		}
	}

	record ApplicationProfile(int application_profile, int version_major, int version_minor, int version_micro) implements TreeNode{

		@Override
		public KVP getJTreeNode(int modus){
			KVP s = new KVP("application_profile ("+version_major+"."+version_minor+"."+version_micro+")");
			s.add(new KVP("application_profile",application_profile));
			s.add(new KVP("version_major",version_major));
			s.add(new KVP("version_minor",version_minor));
			s.add(new KVP("version_micro",version_micro));
			return s;
		}
	}



	/**
     */
	public ApplicationDescriptor(byte[] b, TableSection parent) {
		super(b, parent);

		application_profiles_length = getInt(b, 2, 1, MASK_8BITS);
		int t = 0;
		while (t < application_profiles_length) {
			int application_profile = getInt(b, 3 + t, 2, MASK_16BITS);
			int version_major = getInt(b, 5 + t, 1, MASK_8BITS);
			int version_minor = getInt(b, 6 + t, 1, MASK_8BITS);
			int version_micro = getInt(b, 7 + t, 1, MASK_8BITS);
			ApplicationProfile prof = new ApplicationProfile(application_profile, version_major, version_minor, version_micro);
			applicationProfiles.add(prof);
			t += 5;
		}
		service_bound_flag = getInt(b, 3 + t, 1, 0x80) >> 7;
		visibility = getInt(b, 3 + t, 1, 0x60) >> 5;
		application_priority = getInt(b, 4 + t, 1, MASK_8BITS);
		int r = 3 + t;
		while (r < descriptorLength) {
			int transport_protocol_label = getInt(b, r + 2, 1, MASK_8BITS);
			TransportProtocolLabel transportProtocolLabel = new TransportProtocolLabel(transport_protocol_label);
			transport_protocol_labels.add(transportProtocolLabel);
			r++;
		}
	}

	@Override
	public KVP getJTreeNode(int modus) {
		KVP t = super.getJTreeNode(modus);
		t.add(new KVP("application_profiles_length", application_profiles_length));
		addListJTree(t,applicationProfiles,modus,"application_profiles");
		t.add(new KVP("service_bound_flag", service_bound_flag, service_bound_flag==1?"the application is only associated with the current service":"application not associated with the current service"));
		t.add(new KVP("visibility", visibility, getVisibilityString(visibility)));
		t.add(new KVP("application_priority", application_priority));
		addListJTree(t,transport_protocol_labels,modus,"transport_protocol_labels");

		return t;
	}

	private static String getVisibilityString(int v){

        return switch (v) {
            case 0x00 ->
                    "NOT_VISIBLE_ALL (This application shall not be visible either to applications via an application listing API or to users via the navigator)"; //
            case 0x01 ->
                    "NOT_VISIBLE_USERS (This application shall not be visible to users but shall be visible to applications via an application listing API)";//
            case 0x02 -> "reserved_future_use"; //
            case 0x03 ->
                    "VISIBLE_ALL (This application can be visible to users and shall be visible to applications via an application listing API)";
            default -> "illegal value";
        };
	}

}
