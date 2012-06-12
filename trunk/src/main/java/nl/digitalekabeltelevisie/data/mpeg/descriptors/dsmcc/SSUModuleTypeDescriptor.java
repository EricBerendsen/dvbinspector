package nl.digitalekabeltelevisie.data.mpeg.descriptors.dsmcc;

import static nl.digitalekabeltelevisie.util.Utils.*;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;

public class SSUModuleTypeDescriptor extends DSMCCDescriptor {

	private final int ssu_module_type;

	public SSUModuleTypeDescriptor(final byte[] b, final int offset) {
		super(b, offset);
		ssu_module_type = getInt(b, offset + 2, 1,MASK_8BITS);

	}


	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("SSU_module_type", ssu_module_type, getSSUModuleTypeString(ssu_module_type))));
		return t;
	}



	public static String getSSUModuleTypeString(final int trans){
		switch (trans) {
		case 0: return "executable module type";
		case 1: return "memory mapped code module type";
		case 2: return "data module type";


		default:
			return "reserved for future use";
		}
	}
}
