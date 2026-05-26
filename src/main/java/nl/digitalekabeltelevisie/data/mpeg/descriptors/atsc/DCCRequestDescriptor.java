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
import static nl.digitalekabeltelevisie.util.Utils.getInt;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

public class DCCRequestDescriptor extends AtscDescriptor {

	private final int requestType;
	private final int requestTextLength;
	private final AtscMultipleString requestText;

	public DCCRequestDescriptor(final byte[] b, final TableSection parent) {
		super(b, parent);
		requestType = descriptorLength > 0 ? getInt(b, PRIVATE_DATA_OFFSET, 1, MASK_8BITS) : 0;
		requestTextLength = descriptorLength > 1 ? getInt(b, PRIVATE_DATA_OFFSET + 1, 1, MASK_8BITS) : 0;
		int safeTextLength = Math.min(requestTextLength, Math.max(0, descriptorLength - 2));
		requestText = new AtscMultipleString(b, PRIVATE_DATA_OFFSET + 2, safeTextLength);
	}

	@Override
	public KVP getJTreeNode(final int modus) {
		KVP t = super.getJTreeNode(modus);
		String prefix = descriptorTag == 0xA8 ? "dcc_departing_request" : "dcc_arriving_request";
		t.add(new KVP(prefix + "_type", requestType, getRequestTypeString(descriptorTag, requestType)));
		t.add(new KVP(prefix + "_text_length", requestTextLength));
		t.add(new KVP(prefix + "_text", getRequestText()));
		t.add(requestText.getJTreeNode(modus));
		return t;
	}

	public static String getRequestTypeString(final int descriptorTag, final int requestType) {
		if (descriptorTag == 0xA8) {
			return switch (requestType) {
				case 0x01 -> "cancel outstanding departing request and perform channel change";
				case 0x02 -> "display departing request text for a minimum of 10 seconds";
				case 0x03 -> "display departing request text indefinitely";
				default -> "reserved";
			};
		}
		return switch (requestType) {
			case 0x01 -> "display arriving request text for a minimum of 10 seconds";
			case 0x02 -> "display arriving request text indefinitely";
			default -> "reserved";
		};
	}

	public int getRequestType() {
		return requestType;
	}

	public int getRequestTextLength() {
		return requestTextLength;
	}

	public AtscMultipleString getRequestTextStructure() {
		return requestText;
	}

	public String getRequestText() {
		return requestText.getText();
	}
}
