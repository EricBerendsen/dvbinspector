/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2026 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.descriptors.atsc;

import static nl.digitalekabeltelevisie.util.Utils.MASK_8BITS;
import static nl.digitalekabeltelevisie.util.Utils.addListJTree;
import static nl.digitalekabeltelevisie.util.Utils.getISO8859_1String;
import static nl.digitalekabeltelevisie.util.Utils.getInt;

import java.util.ArrayList;
import java.util.List;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

public class CaptionServiceDescriptor extends AtscDescriptor {

	private final int numberOfServices;
	private final List<CaptionService> services = new ArrayList<>();

	public CaptionServiceDescriptor(final byte[] b, final TableSection parent) {
		super(b, parent);
		numberOfServices = getInt(b, PRIVATE_DATA_OFFSET, 1, 0x1F);
		int offset = PRIVATE_DATA_OFFSET + 1;
		for (int i = 0; i < numberOfServices; i++) {
			CaptionService service = new CaptionService(b, offset);
			services.add(service);
			offset += 6;
		}
	}

	@Override
	public KVP getJTreeNode(final int modus) {
		KVP t = super.getJTreeNode(modus);
		t.add(new KVP("number_of_services", numberOfServices));
		addListJTree(t, services, modus, "services");
		return t;
	}

	public int getNumberOfServices() {
		return numberOfServices;
	}

	public List<CaptionService> getServices() {
		return services;
	}

	public static class CaptionService implements TreeNode {

		private final String language;
		private final int digitalCc;
		private final int line21Field;
		private final int captionServiceNumber;
		private final int easyReader;
		private final int wideAspectRatio;

		CaptionService(final byte[] data, final int offset) {
			language = getISO8859_1String(data, offset, 3);
			int captionFlags = getInt(data, offset + 3, 1, MASK_8BITS);
			digitalCc = (captionFlags >> 7) & 0x01;
			line21Field = digitalCc == 0 ? captionFlags & 0x01 : -1;
			captionServiceNumber = digitalCc != 0 ? captionFlags & 0x3F : -1;
			int serviceFlags = getInt(data, offset + 4, 1, MASK_8BITS);
			easyReader = (serviceFlags >> 7) & 0x01;
			wideAspectRatio = (serviceFlags >> 6) & 0x01;
		}

		@Override
		public KVP getJTreeNode(final int modus) {
			KVP t = new KVP("caption_service", getServiceDescription());
			t.add(new KVP("language", language));
			t.add(new KVP("digital_cc", digitalCc, getDigitalCcString(digitalCc)));
			if (digitalCc == 0) {
				t.add(new KVP("line21_field", line21Field));
			} else {
				t.add(new KVP("caption_service_number", captionServiceNumber));
			}
			t.add(new KVP("easy_reader", easyReader));
			t.add(new KVP("wide_aspect_ratio", wideAspectRatio, getWideAspectRatioString(wideAspectRatio)));
			return t;
		}

		private String getServiceDescription() {
			if (digitalCc == 0) {
				return language + " CEA-608";
			}
			return language + " CEA-708 service " + captionServiceNumber;
		}

		public String getLanguage() {
			return language;
		}

		public int getDigitalCc() {
			return digitalCc;
		}

		public int getLine21Field() {
			return line21Field;
		}

		public int getCaptionServiceNumber() {
			return captionServiceNumber;
		}

		public int getEasyReader() {
			return easyReader;
		}

		public int getWideAspectRatio() {
			return wideAspectRatio;
		}
	}

	public static String getDigitalCcString(final int digitalCc) {
		return digitalCc == 0 ? "CEA-608" : "CEA-708";
	}

	public static String getWideAspectRatioString(final int wideAspectRatio) {
		return wideAspectRatio == 0 ? "4:3" : "16:9";
	}
}
