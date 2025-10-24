/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  (C) RIEDEL Communications Canada, Inc. All rights reserved
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

package nl.digitalekabeltelevisie.data.mpeg.pes.video.jpegxs;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.PesPacketData;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.extension.mpeg.JpegXsVideoDescriptor;
import nl.digitalekabeltelevisie.util.BitSource;

/**
 * @author Simon Provost
 */
public class JpegXsDataField extends PesPacketData {

    private final long jxes_length;
    private final long jxes_box_code;
    private final long brat;
    private final long frat;
    private final long schar;
    private final long ppih;
    private final long plev;
    private final long color_primaries;
    private final long transfer_characteristics;
    private final long matrix_coefficients;
    private final int video_full_range_flag;
    private final long tcod;

    public JpegXsDataField(PesPacketData pesPacket) {
        super(pesPacket);

        BitSource reader = new BitSource(data, pesDataStart);
        jxes_length = reader.readBitsLong(32);
        jxes_box_code = reader.readBitsLong(32);
        brat = reader.readBitsLong(32);
        frat = reader.readBitsLong(32);
        schar = reader.readBitsLong(16);
        ppih = reader.readBitsLong(16);
        plev = reader.readBitsLong(16);
        color_primaries = reader.readBitsLong(8);
        transfer_characteristics = reader.readBitsLong(8);
        matrix_coefficients = reader.readBitsLong(8);
        video_full_range_flag = reader.readBits(1);
        reader.skiptoByteBoundary();
        tcod = reader.readBitsLong(4);
    }

    @Override
	public KVP getJTreeNode(int modus) {
		final KVP jxes_node = new KVP("JPEG-XS payload");
		jxes_node.add(new KVP("JXES Length", jxes_length));
		jxes_node.add(new KVP("JXES Box Code", jxes_box_code).setDescription("\"jxes\""));
		jxes_node.add(new KVP("brat", brat, "Bit Rate (MBits/s)"));
		jxes_node.add(JpegXsVideoDescriptor.buildFratNode(frat));
		jxes_node.add(JpegXsVideoDescriptor.buildScharNode(schar));
		jxes_node.add(JpegXsVideoDescriptor.buildPpihNode(ppih));
		jxes_node.add(JpegXsVideoDescriptor.buildPlevNode(plev));
		jxes_node.add(new KVP("colour_primaries", color_primaries));
		jxes_node.add(new KVP("transfer_characteristics", transfer_characteristics));
		jxes_node.add(new KVP("matrix_coefficients", matrix_coefficients));
		jxes_node.add(new KVP("video_full_range_flag", video_full_range_flag));
		jxes_node.add(new KVP("tcod", tcod));

		final KVP parent_node = super.getJTreeNode(modus, new KVP("JPEG-XS PES Packet"));
		parent_node.add(jxes_node);
		return parent_node;
	}
}
