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

package nl.digitalekabeltelevisie.data.mpeg.pes.headers;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.common.JpegXsTreeNode;
import nl.digitalekabeltelevisie.util.ByteBufferReader;
import nl.digitalekabeltelevisie.util.Utils;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of {@link ElementaryStreamHeader} used when the stream
 * type is JPEG-XS (stream type 0x32).
 *
 * @author Simon Provost
 */
public class JxesHeader implements ElementaryStreamHeader {

    private static final Logger logger = Logger.getLogger(JxesHeader.class.getName());
    private static final long JXES_BOX_CODE = 0x6a786573;

    private long jxes_length;
    private long jxes_box_code;
    private long brat;
    private long frat;
    private long schar;
    private long ppih;
    private long plev;
    private long color_primaries;
    private long transfer_characteristics;
    private long matrix_coefficients;
    private boolean video_full_range_flag;
    private long tcod;

    private boolean parsed = false;

    @Override
    public void parse(byte[] buffer, int offset) {
        parsed = false;
        try {
            if (buffer.length < offset + 8) {
                return;
            }

            ByteBufferReader reader = new ByteBufferReader(buffer, offset);
            jxes_length = reader.read(4);
            jxes_box_code = reader.read(4);
            if (buffer.length < jxes_length + offset || jxes_box_code != JXES_BOX_CODE) {
                return;
            }

            brat = reader.read(4);
            frat = reader.read(4);
            schar = reader.read(2);
            ppih = reader.read(2);
            plev = reader.read(2);
            color_primaries = reader.read(1);
            transfer_characteristics = reader.read(1);
            matrix_coefficients = reader.read(1);
            video_full_range_flag = Utils.getBitAsBoolean((byte) reader.read(1), 1);
            tcod = reader.read(4);

            parsed = true;
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to parse JPEG-XS elementary stream header.", e);
        }
    }

    @Override
    public DefaultMutableTreeNode getJTreeNode() {
        final DefaultMutableTreeNode node = new DefaultMutableTreeNode(new KVP("JXES Header"));
        if (parsed) {
            node.add(new DefaultMutableTreeNode(new KVP("JXES Length", jxes_length, null)));
            node.add(new DefaultMutableTreeNode(new KVP("JXES Box Code", jxes_box_code, "\"jxes\"")));
            node.add(new DefaultMutableTreeNode(new KVP("brat", brat, "Bit Rate (MBits/s)")));
            node.add(JpegXsTreeNode.buildFratNode(frat));
            node.add(JpegXsTreeNode.buildScharNode(schar));
            node.add(JpegXsTreeNode.buildPpihNode(ppih));
            node.add(JpegXsTreeNode.buildPlevNode(plev));
            node.add(new DefaultMutableTreeNode(new KVP("colour_primaries", color_primaries, null)));
            node.add(new DefaultMutableTreeNode(new KVP("transfer_characteristics", transfer_characteristics, null)));
            node.add(new DefaultMutableTreeNode(new KVP("matrix_coefficients", matrix_coefficients, null)));
            node.add(new DefaultMutableTreeNode(new KVP("video_full_range_flag", video_full_range_flag, null)));
            node.add(new DefaultMutableTreeNode(new KVP("tcod", tcod, null)));
        } else {
            node.add(new DefaultMutableTreeNode(new KVP("Parsing error")));
        }
        return node;
    }

    @Override
    public int getLength() {
        return (int) jxes_length;
    }
}
