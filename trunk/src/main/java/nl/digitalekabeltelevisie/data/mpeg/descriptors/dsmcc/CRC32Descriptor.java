package nl.digitalekabeltelevisie.data.mpeg.descriptors.dsmcc;

import static nl.digitalekabeltelevisie.util.Utils.*;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;

public class CRC32Descriptor extends DSMCCDescriptor {

	private final long crc_32;

	public CRC32Descriptor(final byte[] b, final int offset) {
		super(b, offset);
		crc_32 = getLong(b, offset + 2, 4,MASK_32BITS);

	}


	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("CRC_32", crc_32, null)));
		return t;
	}

}
