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
 * Based on TS 102 006
 */
package nl.digitalekabeltelevisie.data.mpeg.descriptors.aitable;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

/**
 * @author Eric Berendsen
 *
 */
public class AITDescriptor extends Descriptor {


	public AITDescriptor(byte[] b, TableSection parent) {
		super(b, parent);
	}

	@Override
	public String getDescriptorname(){
		return getDescriptorname(descriptorTag);
	}

	public static String getDescriptorname(int tag){

        return switch (tag) {
            case 0x00 -> "application_descriptor"; //
            case 0x01 -> "application_name_descriptor";//
            case 0x02 -> "transport_protocol_descriptor"; //
            case 0x03 -> "DVB-J_application_descriptor";
            case 0x04 -> "DVB-J_application_location_descriptor";
            case 0x05 -> "external_application_authorisation_descriptor"; //
            case 0x06 -> "routing_descriptor_IPv4 / application_recording_descriptor";  //ETSI TS 102 809 V1.1.1
            case 0x07 -> "routing_descriptor_IPv6";
            case 0x08 -> "DVB-HTML_application_descriptor";
            case 0x09 -> "DVB-HTML_application_location_descriptor";
            case 0x0A -> "DVB-HTML_application_boundary_descriptor";
            case 0x0B -> "application_icons_descriptor"; //
            case 0x0C -> "prefetch_descriptor"; //
            case 0x0D -> "DII_location_descriptor"; //
            case 0x0E -> "delegated application descriptor";
            case 0x0F -> "Plug-in descriptor";
            case 0x10 -> "Application storage descriptor";
            case 0x11 -> "ip_signalling_descriptor";//
            case 0x12 -> "provider_export_descriptor";// ETSI TS 101 162 V1.9.1 (2020-07)
            case 0x13 -> "provider_usage_descriptor";//ETSI TS 101 162 V1.9.1 (2020-07)
            case 0x14 -> "graphics_constraints_descriptor"; //ETSI TS 102 809 V1.1.1
            case 0x15 -> "simple_application_location_descriptor"; //ETSI TS 102 809 V1.1.1
            case 0x16 -> "application_usage_descriptor"; //ETSI TS 102 809 V1.1.1
            case 0x17 -> "simple_application_boundary_descriptor"; //ETSI TS 102 809 V1.1.1
            case 0x5F -> "private_data_specifier_descriptor"; //ETSI TS 101 162 V1.9.1 (2020-07)
            default -> "reserved to MHP";
        };
	}
	
	@Override
	public KVP getJTreeNode(int modus) {
		return (KVP) super.getJTreeNode(modus);
	}



}
