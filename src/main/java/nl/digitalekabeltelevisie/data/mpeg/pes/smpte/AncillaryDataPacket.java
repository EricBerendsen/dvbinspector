package nl.digitalekabeltelevisie.data.mpeg.pes.smpte;

import nl.digitalekabeltelevisie.util.BitSource;
import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

public class AncillaryDataPacket implements TreeNode {

    private final int b0;
    private final int c_not_y_channel_flag;
    private final int line_number;
    private final int horizontal_offset;
    private final int DID;
    private final int SDID;
    private final int data_count;
    private final List<Integer> user_data_word = new ArrayList<Integer>();
    private final int checksum_word;

    /**
     * Constructor of an AncillaryDataPacket contained in a Pes packet
     * 
     * @param data   : bytes to parse
     * @param offset : where to begin the parsing
     * @throws Exception
     */
    protected AncillaryDataPacket(final byte[] data, final int offset) {

        // make sure that there is enough bytes to read
        if (offset + 7 < data.length) {

            final BitSource bs = new BitSource(data, offset);
            b0 = bs.readBits(6);
            c_not_y_channel_flag = bs.readBits(1);
            line_number = bs.readBits(11);
            horizontal_offset = bs.readBits(12);
            DID = bs.readBits(10) & 0xFF;
            SDID = bs.readBits(10) & 0xFF;
            data_count = bs.readBits(10) & 0xFF;

            // Read data_count amount of data words
            for (int i = 0; i < data_count; i++) {

                // read data_word and add it to the list
                final int data_word = bs.readBits(10) & 0xFF;
                user_data_word.add(data_word);

            }
            // read checksum value
            checksum_word = bs.readBits(10);

            // if error
        } else {
            b0 = -1;
            c_not_y_channel_flag = -1;
            line_number = -1;
            horizontal_offset = -1;
            DID = -1;
            SDID = -1;
            data_count = -1;
            checksum_word = -1;
        }
    }

    public int getB0() {
        return b0;
    }

    public int getC_not_y_channel_flag() {
        return c_not_y_channel_flag;
    }

    public int getLine_number() {
        return line_number;
    }

    public int getHorizontal_offset() {
        return horizontal_offset;
    }

    public int getDID() {
        return DID;
    }

    public int getSDID() {
        return SDID;
    }

    public int getData_count() {
        return data_count;
    }

    public List<Integer> getUser_data_word() {
        return user_data_word;
    }

    public int getChecksum_word() {
        return checksum_word;
    }

    @Override
    public DefaultMutableTreeNode getJTreeNode(int modus) {
        final DefaultMutableTreeNode s = new DefaultMutableTreeNode(new KVP("Ancillary Data Packet"));
        s.add(new DefaultMutableTreeNode(new KVP("b0", b0, null)));

        s.add(new DefaultMutableTreeNode(new KVP("c_not_y_channelFlag", c_not_y_channel_flag,
                "SD streams: should be 0 / HD streams:"
                        + (c_not_y_channel_flag == 1 ? " ANC data corresponds to the color difference channel"
                                : " ANC data corresponds to the luminance channel"))));

        s.add(new DefaultMutableTreeNode(new KVP("line_number", line_number, null)));
        s.add(new DefaultMutableTreeNode(new KVP("horizontal_offset", horizontal_offset, null)));
        s.add(new DefaultMutableTreeNode(new KVP("DID", DID, null)));
        s.add(new DefaultMutableTreeNode(new KVP("SDID", SDID, null)));
        s.add(new DefaultMutableTreeNode(new KVP("data_count", data_count, null)));

        // create a folder for user_data_word
        final DefaultMutableTreeNode d = new DefaultMutableTreeNode(new KVP("User data words"));
        for (Integer dataWord : user_data_word) {
            d.add(new DefaultMutableTreeNode(new KVP("user_data_word", dataWord, null)));
        }
        s.add(d);

        s.add(new DefaultMutableTreeNode(new KVP("checksum_word", checksum_word, null)));
        return s;
    }

}