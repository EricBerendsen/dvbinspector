/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2019 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.pes.video26x.sei;

import static nl.digitalekabeltelevisie.util.Utils.getInt;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.pes.video.common.AuxiliaryData;
import nl.digitalekabeltelevisie.data.mpeg.pes.video.common.SlHdrInfo;
import nl.digitalekabeltelevisie.util.BitSource;
import nl.digitalekabeltelevisie.util.Utils;

/**
 * @author Eric
 *
 */
public class UserDataRegisteredItuT35Sei_message extends Sei_message {

	int itu_t_t35_country_code = 0;
	int itu_t_t35_country_code_extension_byte;
	int Itu_t_t35_provider_code = 0;
	AuxiliaryData auxData = null;
	SlHdrInfo sl_hdr_info = null;

	/**
	 * @param bitSource
	 */
	public UserDataRegisteredItuT35Sei_message(final BitSource bitSource) {
		super(bitSource);
		int offset=0;

		itu_t_t35_country_code = getInt(payload, 0,1,Utils.MASK_8BITS);
		if(itu_t_t35_country_code != 0xFF){
			offset = 1;
		}else{
			itu_t_t35_country_code_extension_byte = getInt(payload, 1,1,Utils.MASK_8BITS);
			offset = 2;
		}
		Itu_t_t35_provider_code = getInt(payload, offset,2,Utils.MASK_16BITS);
		offset += 2;
		if((itu_t_t35_country_code==0xB5)
				&& (Itu_t_t35_provider_code==0x31)){
			auxData = new AuxiliaryData(payload, offset, payloadSize-offset);

		}else if((itu_t_t35_country_code==0xB5)
				&& (Itu_t_t35_provider_code==0x3a)){
			sl_hdr_info = new SlHdrInfo(payload, offset, payloadSize-offset);
		}

	}

	@Override
	public KVP getJTreeNode(int modus) {
		KVP s = super.getJTreeNode(modus);
		s.add(new KVP("UserDataRegisteredItuT35Sei_message"));
		s.add(new KVP("itu_t_t35_country_code", itu_t_t35_country_code, Utils.getItu35CountryCodeString(itu_t_t35_country_code)));
		if (itu_t_t35_country_code == 0xFF) {
			s.add(new KVP("itu_t_t35_country_code_extension_byte", itu_t_t35_country_code_extension_byte));
		}
		s.add(new KVP("Itu_t_t35_provider_code", Itu_t_t35_provider_code));
		if ((itu_t_t35_country_code == 0xB5) && (Itu_t_t35_provider_code == 0x31)) {
			s.add(auxData.getJTreeNode(modus));
		}
		if ((itu_t_t35_country_code == 0xB5) && (Itu_t_t35_provider_code == 0x3a)) {
			s.add(new KVP("SL-HDR data (Annex A of TS 103 433-1)"));
			s.add(sl_hdr_info.getJTreeNode(modus));
		}
		return s;
	}

	public int getItu_t_t35_country_code() {
		return itu_t_t35_country_code;
	}


	public int getItu_t_t35_country_code_extension_byte() {
		return itu_t_t35_country_code_extension_byte;
	}


	public int getItu_t_t35_provider_code() {
		return Itu_t_t35_provider_code;
	}


	public AuxiliaryData getAuxData() {
		return auxData;
	}

}
