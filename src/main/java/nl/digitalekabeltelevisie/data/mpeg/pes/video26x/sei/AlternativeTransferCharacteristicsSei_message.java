package nl.digitalekabeltelevisie.data.mpeg.pes.video26x.sei;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.util.BitSource;

/**
 * based on Rec. ITU-T H.265 v7 (08/2021) D.2.38 Alternative transfer
 * characteristics information SEI message syntax
 */
public class AlternativeTransferCharacteristicsSei_message extends Sei_message {

	private final int preferred_transfer_characteristics;

	/**
	 * @param bitSource
	 */
	public AlternativeTransferCharacteristicsSei_message(BitSource bitSource) {
		super(bitSource);

		// bitSource has been read by super(), so now convert payload back into BitSource
		var bitSourcePayload = new BitSource(payload, 0);

		preferred_transfer_characteristics = bitSourcePayload.u(8);
	}

	@Override
	public KVP getJTreeNode(int modus) {
		KVP s = super.getJTreeNode(modus);
		s.add(new KVP("preferred_transfer_characteristics",
				preferred_transfer_characteristics, getDescription(preferred_transfer_characteristics)));

		return s;
	}

	public int getPreferred_transfer_characteristics() {
		return preferred_transfer_characteristics;
	}

	private static String getDescription(int i) {
		if ((i == 0) || (i == 3) || (i > 19)) {
			return "reserved (" + i + ")";
		} else if (i == 2) {
			return "unspecified (" + i + ")";
		} else {
			return "(" + i + ")";
		}
	}
}
