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
package nl.digitalekabeltelevisie.data.mpeg.descriptors.afdescriptors;

import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;

/**
 * Based on ISO/IEC 13818-1:2015/Amd.1/Cor.2:2016 (E) / Rec. ITU-T H.222.0 (2015)/Amd.1/Cor.2 (07/2016) 
 * "Amendment 1: Delivery of timeline for external data
 * Technical Corrigendum 2: Clarifications and
 * corrections on pause flag, URL construction
 * and adaptation field syntax"
 * 
 * @author Eric
 *
 */

public class AFDescriptor extends Descriptor {


	/**
	 * @param b
	 * @param offset
	 * @param parent
	 */
	public AFDescriptor(byte[] b) {
		super(b, null);
	}

	@Override
	public String getDescriptorname(){
		return getDescriptorname(descriptorTag);
	}

	public static String getDescriptorname(int tag){


		if((tag>=0x00)&&(tag<=0x03)){
			return "Rec. ITU-T H.222.0 | ISO/IEC 13818-1 Reserved";
		}
		if((tag>=0x0e)&&(tag<=0x7f)){
			return "Rec. ITU-T H.222.0 | ISO/IEC 13818-1 Reserved";
		}

        return switch (tag) {
            case 0x04 -> "Timeline Descriptor";
            case 0x05 -> "Location Descriptor";
            case 0x06 -> "BaseURL Descriptor";
            case 0x07 -> "Cets_byte_range_descriptor"; //ISO/IEC 13818-1:2015/Amd.1/Cor.2:2016 (E)
            case 0x08 ->
                    "MPEG-H_3dAudio_extStreamID_descriptor"; //Rec. ITU-T H.222.0 (2014)/Amd.7 (03/2017) – Prepublished version
            case 0x09 ->
                    "AF_MPEG-H_3dAudio_multi-stream_descriptor"; //Rec. ITU-T H.222.0 (2014)/Amd.7 (03/2017) – Prepublished version
            case 0x0a ->
                    "AF_MPEG-H_3dAudio_command_descriptor"; //Rec. ITU-T H.222.0 (2014)/Amd.7 (03/2017) – Prepublished version
            case 0x0b -> "Boundary Descriptor"; //Rec. ITU-T H.222.0 (2014)/Amd.7 (03/2017) – Prepublished version
            case 0x0c -> "Labeling Descriptor"; //Rec. ITU-T H.222.0 (2014)/Amd.7 (03/2017) – Prepublished version
            case 0x0d -> "HEVC_tile_substream_af_descriptor"; //ISO/IEC 13818-1:2021 - Rec. ITU-T H.222.0 (06/2021)
            default -> "User Private";
        };
	}


}
