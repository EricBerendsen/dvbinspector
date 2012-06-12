package nl.digitalekabeltelevisie.data.mpeg.descriptors.dsmcc;

import static nl.digitalekabeltelevisie.util.Utils.*;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;

public class CompressedModuleDescriptor extends DSMCCDescriptor {

	private final int compression_method;
	private final long original_size;

	public CompressedModuleDescriptor(final byte[] b, final int offset) {
		super(b, offset);
		compression_method = getInt(b, offset + 2, 1,MASK_8BITS);
		original_size = getLong(b, offset + 3, 4,MASK_32BITS);


	}


	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("compression_method", compression_method, null)));
		t.add(new DefaultMutableTreeNode(new KVP("original_size", original_size, null)));
		return t;
	}


	public int getCompression_method() {
		return compression_method;
	}


	public long getOriginal_size() {
		return original_size;
	}

}
