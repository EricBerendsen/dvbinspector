package nl.digitalekabeltelevisie.util;
/**
*
*  http://www.digitalekabeltelevisie.nl/dvb_inspector
*
*  This code is Copyright 20020 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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


public class ServiceIdentification implements Comparable<ServiceIdentification> {

	
	private int originalNetworkId;
	private int transportStreamId;
	private int serviceId;
	
	public ServiceIdentification(int originalNetworkId, int transportStreamId, int serviceId){
		super();
		this.originalNetworkId = originalNetworkId;
		this.transportStreamId = transportStreamId;
		this.serviceId = serviceId;
	}

	public int getOriginalNetworkId() {
		return originalNetworkId;
	}
	
	public void setOriginalNetworkId(int originalNetworkId) {
		this.originalNetworkId = originalNetworkId;
	}
	
	public int getTransportStreamId() {
		return transportStreamId;
	}

	public void setTransportStreamId(int transportStreamId) {
		this.transportStreamId = transportStreamId;
	}

	public int getServiceId() {
		return serviceId;
	}

	public void setServiceId(int serviceId) {
		this.serviceId = serviceId;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + originalNetworkId;
		result = prime * result + serviceId;
		result = prime * result + transportStreamId;
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ServiceIdentification other = (ServiceIdentification) obj;
		if (originalNetworkId != other.originalNetworkId) {
			return false;
		}
		if (serviceId != other.serviceId) {
			return false;
		}
		if (transportStreamId != other.transportStreamId) {
			return false;
		}
		return true;
	}
	
	@Override
	public int compareTo(ServiceIdentification other) {

		if (this.originalNetworkId != other.originalNetworkId) {
			return this.originalNetworkId - other.originalNetworkId;
		} else if (this.transportStreamId != other.transportStreamId) {
			return this.transportStreamId - other.transportStreamId;
		}
		return this.serviceId - other.serviceId;
	}
	
}
