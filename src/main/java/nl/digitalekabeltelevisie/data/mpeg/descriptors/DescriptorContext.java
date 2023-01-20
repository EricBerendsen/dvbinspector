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

package nl.digitalekabeltelevisie.data.mpeg.descriptors;

public class  DescriptorContext {
	
	public int original_network_id;
	public int transport_stream_id;
	private int network_id;
	long private_data_specifier;
	
	 public DescriptorContext() {
		 super();
	 }
	 
	 public DescriptorContext(int original_network_id, int transport_stream_id) {
		 super();
		 this.original_network_id = original_network_id;
		 this.transport_stream_id = transport_stream_id;
	 }
	
	 public DescriptorContext(int original_network_id, int transport_stream_id, int network_id) {
		 super();
		 this.original_network_id = original_network_id;
		 this.transport_stream_id = transport_stream_id;
		 this.network_id = network_id;
	 }
	 
	 public boolean hasOnidTsid() {
		 return (original_network_id != 0 && transport_stream_id !=0);
	 }

	public int getOriginal_network_id() {
		return original_network_id;
	}

	public void setOriginal_network_id(int original_network_id) {
		this.original_network_id = original_network_id;
	}

	public int getTransport_stream_id() {
		return transport_stream_id;
	}

	public void setTransport_stream_id(int transport_stream_id) {
		this.transport_stream_id = transport_stream_id;
	}

	public long getPrivate_data_specifier() {
		return private_data_specifier;
	}

	public void setPrivate_data_specifier(long private_data_specifier) {
		this.private_data_specifier = private_data_specifier;
	}

	public int getNetwork_id() {
		return network_id;
	}
}