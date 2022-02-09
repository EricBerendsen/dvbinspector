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
package nl.digitalekabeltelevisie.data.mpeg.descriptors.extension.mpeg;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.common.JpegXsTreeNode;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.ByteBufferReader;
import nl.digitalekabeltelevisie.util.Utils;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class responsible for decoding a JPEG-XS video descriptor from a byte array
 * into a {@link DefaultMutableTreeNode}.
 *
 * @author Simon Provost
 */
public class JpegXsVideoDescriptor extends MPEGExtensionDescriptor {

    private static final Logger logger = Logger.getLogger(JpegXsVideoDescriptor.class.getName());

    private long descriptor_version;
    private long horizontal_size;
    private long vertical_size;
    private long brat;
    private long frat;
    private long schar;
    private long ppih;
    private long plev;
    private long max_buffer_size;
    private long buffer_model_type;
    private long colour_primaries;
    private long transfer_characteristics;
    private long matrix_coefficients;
    private boolean video_full_range_flag;
    private boolean still_mode;
    private boolean mdm_flag;
    private byte[] private_data;

    private boolean parsed = false;

    public JpegXsVideoDescriptor(byte[] b, int offset, TableSection parent) {
        super(b, offset, parent);
        try {
            ByteBufferReader reader = new ByteBufferReader(b);
            reader.setReadOffset(offset + 3);
            descriptor_version = reader.read(1);
            horizontal_size = reader.read(2);
            vertical_size = reader.read(2);
            brat = reader.read(4);
            frat = reader.read(4);
            schar = reader.read(2);
            ppih = reader.read(2);
            plev = reader.read(2);
            max_buffer_size = reader.read(4);
            buffer_model_type = reader.read(1);
            colour_primaries = reader.read(1);
            transfer_characteristics = reader.read(1);
            matrix_coefficients = reader.read(1);
            byte video_full_range_flag_byte = (byte) reader.read(1);
            video_full_range_flag = Utils.getBitAsBoolean(video_full_range_flag_byte, 1);
            byte still_mode_and_mdm_flag = (byte) reader.read(1);
            still_mode = Utils.getBitAsBoolean(still_mode_and_mdm_flag, 1);
            mdm_flag = Utils.getBitAsBoolean(still_mode_and_mdm_flag, 2);
            private_data = reader.readRemaining();

            parsed = true;
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to parse JPEG-XS descriptor.", e);
        }
    }

    @Override
    public DefaultMutableTreeNode getJTreeNode(int modus) {
        final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("JPEG-XS Descriptor"));
        if (parsed) {
            t.add(new DefaultMutableTreeNode(new KVP("descriptor_version", descriptor_version, null)));
            t.add(new DefaultMutableTreeNode(new KVP("horizontal_size", horizontal_size, null)));
            t.add(new DefaultMutableTreeNode(new KVP("vertical_size", vertical_size, null)));
            t.add(new DefaultMutableTreeNode(new KVP("brat", brat, "Bit Rate (MBits/s)")));
            t.add(JpegXsTreeNode.buildFratNode(frat));
            t.add(JpegXsTreeNode.buildScharNode(schar));
            t.add(JpegXsTreeNode.buildPpihNode(ppih));
            t.add(JpegXsTreeNode.buildPlevNode(plev));
            t.add(new DefaultMutableTreeNode(new KVP("max_buffer_size", max_buffer_size, "Maximum buffer size (Mbits/s)")));
            t.add(new DefaultMutableTreeNode(new KVP("buffer_model_type", buffer_model_type, null)));
            t.add(new DefaultMutableTreeNode(new KVP("colour_primaries", colour_primaries, null)));
            t.add(new DefaultMutableTreeNode(new KVP("transfer_characteristics", transfer_characteristics, null)));
            t.add(new DefaultMutableTreeNode(new KVP("matrix_coefficients", matrix_coefficients, null)));
            t.add(new DefaultMutableTreeNode(new KVP("video_full_range_flag", video_full_range_flag, null)));
            t.add(new DefaultMutableTreeNode(new KVP("still_mode", still_mode, null)));
            t.add(new DefaultMutableTreeNode(new KVP("mdm_flag", mdm_flag, null)));
            t.add(new DefaultMutableTreeNode(new KVP("private_data", private_data, null)));
        } else {
            t.add(new DefaultMutableTreeNode(new KVP("Parsing error")));
        }
        final DefaultMutableTreeNode parentNode = super.getJTreeNode(modus);
        parentNode.add(t);
        return parentNode;
    }
}
