package nl.digitalekabeltelevisie.data.mpeg.descriptors.dsmcc;

import static nl.digitalekabeltelevisie.util.Utils.*;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;

public class CachingPrioriyDescriptor extends DSMCCDescriptor {

	private final int priority_value;
	private final int transparency_level;

	public CachingPrioriyDescriptor(final byte[] b, final int offset) {
		super(b, offset);
		priority_value = getInt(b, offset + 2, 1,MASK_8BITS);
		transparency_level = getInt(b, offset + 3, 1,MASK_8BITS);

	}


	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("priority_value", priority_value, null)));
		t.add(new DefaultMutableTreeNode(new KVP("transparency_level", transparency_level, getTransParencyLevelString(transparency_level))));
		return t;
	}



	public static String getTransParencyLevelString(final int trans){
		switch (trans) {
		case 0: return "reserved";
		case 1: return "Transparent caching";
		case 2: return "Semi-transparent caching";
		case 3: return "Static caching";


		default:
			return "reserved for future use";
		}
	}
}
