package nl.digitalekabeltelevisie.data.mpeg.pes.smpte;

import static nl.digitalekabeltelevisie.util.Utils.addListJTree;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.PesPacketData;
import nl.digitalekabeltelevisie.data.mpeg.pes.GeneralPesHandler;

public class Smpte2038Handler extends GeneralPesHandler {

	/**
	 * Process data to get pes packets from SMPTE 2038
	 */
	@Override
	protected void processPesDataBytes(final PesPacketData pesData) {
		Smpte2038PesDataField smpte2038PesDataField;
		smpte2038PesDataField = new Smpte2038PesDataField(pesData);
		pesPackets.add(smpte2038PesDataField);
	}

	/**
	 * Handle display of SMPTE 2038
	 */
	@Override
	public KVP getJTreeNode(int modus) {
		KVP s = new KVP("SMPTE 2038 PES Data");
		addListJTree(s, pesPackets, modus, "PES Packets");
		return s;
	}
}
