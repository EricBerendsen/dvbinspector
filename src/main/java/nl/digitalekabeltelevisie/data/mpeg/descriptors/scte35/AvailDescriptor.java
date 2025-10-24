package nl.digitalekabeltelevisie.data.mpeg.descriptors.scte35;

import static nl.digitalekabeltelevisie.util.Utils.getBytes;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

public class AvailDescriptor extends SCTE35Descriptor {

	private byte[] provider_avail_id;

	public AvailDescriptor(byte[] b, TableSection parent) {
		super(b, parent);
		provider_avail_id = getBytes(b, 6, 4);
	}

	@Override
	public KVP getJTreeNode(int modus) {
		KVP t = super.getJTreeNode(modus);
		t.add(new KVP("provider_avail_id", provider_avail_id));
		return t;
	}

}
