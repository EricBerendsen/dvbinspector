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

import static nl.digitalekabeltelevisie.util.Utils.getInt;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.DVBString;
import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

public class ServiceDescriptor extends Descriptor {

	private final int serviceType;
	private final DVBString  serviceProviderName;
	private final DVBString  serviceName;

	public ServiceDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset,parent);
		serviceType = getInt(b, offset+2, 1, 0xFF);
		final int serviceProviderNameLength = getInt(b, offset+3, 1, 0xFF);
		serviceProviderName = new DVBString(b,offset+3);
		serviceName = new DVBString(b,offset+4+serviceProviderNameLength);
	}

	public DVBString getServiceProviderName() {
		return serviceProviderName;
	}


	@Override
	public String toString() {
		return super.toString() + " service_type"+serviceType + "("+ getServiceTypeString(serviceType)+"), serviceProviderName="+getServiceProviderName()+ "serviceName="+getServiceName();
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("service_type",serviceType ,getServiceTypeString(serviceType))));
		t.add(new DefaultMutableTreeNode(new KVP("service_provider_name_encoding",serviceProviderName.getEncodingString() ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("service_provider_name_length",serviceProviderName.getLength() ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("service_provider_name",serviceProviderName ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("service_name_encoding",serviceName.getEncodingString() ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("service_name_length",serviceName.getLength() ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("service_name",serviceName ,null)));
		return t;
	}

	public DVBString getServiceName() {
		return serviceName;
	}

	public int getServiceType() {
		return serviceType;
	}

	public int getServiceNameLength() {
		return serviceName.getLength();
	}


	public int getServiceProviderNameLength() {
		return serviceProviderName.getLength();
	}

}
