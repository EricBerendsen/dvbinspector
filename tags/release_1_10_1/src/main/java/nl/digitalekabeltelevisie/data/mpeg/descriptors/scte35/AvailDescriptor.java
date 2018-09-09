package nl.digitalekabeltelevisie.data.mpeg.descriptors.scte35;

import static nl.digitalekabeltelevisie.util.Utils.getBytes;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

public class AvailDescriptor extends SCTE35Descriptor {

	private byte[] provider_avail_id;


	public AvailDescriptor(byte[] b, int offset, TableSection parent) {
		super(b, offset, parent);
		provider_avail_id = getBytes(b,offset+6,4);
	}

	
	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("provider_avail_id",provider_avail_id ,null)));
		return t;
	}

}
