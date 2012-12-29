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
 * @author Asif Raza
 */

package nl.digitalekabeltelevisie.data.mpeg.descriptors;

import static nl.digitalekabeltelevisie.util.Utils.*;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

public class AACMpeg2Descriptor extends Descriptor {


	private final int mpeg2_aac_profile;
	private int mpeg2_aac_channel_configuration;
	private int additional_info;


	public AACMpeg2Descriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset,parent);
		mpeg2_aac_profile = getInt(b, offset+2, 1, MASK_8BITS);
		mpeg2_aac_channel_configuration = getInt(b, offset+3, 1, MASK_8BITS);
		additional_info=getInt(b, offset+4, 1, MASK_8BITS);
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){

		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("MPEG-2_AAC_profile",mpeg2_aac_profile,getProfileString(mpeg2_aac_profile))));
		t.add(new DefaultMutableTreeNode(new KVP("MPEG-2_AAC_channel_configuration",mpeg2_aac_channel_configuration,getChannelConfigurationString(mpeg2_aac_channel_configuration))));
		t.add(new DefaultMutableTreeNode(new KVP("MPEG-2_AAC_additional_information",additional_info,getAdditionalInfoString(additional_info))));

		return t;
	}

	public static String getProfileString(final int profile) {
		switch (profile) {

		case 0x00:
			 return "Main profile";
		case 0x01:
			return "Low Complexity profile (LC)";
		case 0x02:
			 return "Scalable Sampling Rate profile (SSR)";
		case 0x03:
			return "Reserved";
		default:
			return "Unknown";

		}
	}

	public static String getAdditionalInfoString(final int code) {
		switch (code) {

		case 0x00:
			 return "AAC data according to ISO/IEC 13818-7:2003";
		case 0x01:
			return "AAC data with Bandwidth Extension data present according to ISO/IEC 13818-7:2003/AMD1:2004";
		default:
			return "Reserved";
		}
	}

	public static String getChannelConfigurationString(final int code) {
		switch (code) {

		case 0x01:
			 return "Mono (center front speaker)";
		case 0x02:
			return "Stereo (left, right front speakers)";
		case 0x03:
			 return "3 speakers (center front speaker left, right front speakers)";
		case 0x04:
			return "4 speakers (center front speaker left, right center front speakers, rear surround)";
		case 0x05:
			return "5 speakers (center front speaker left, right front speakers, left surround, right surround rear speakers)";
		case 0x06:
			return "5.1";
		case 0x07:
			return "7.1";
		default:
			return "Unknown configuration";

		}
	}
}
