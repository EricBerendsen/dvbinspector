package nl.digitalekabeltelevisie.CLI;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import nl.digitalekabeltelevisie.data.mpeg.TransportStream;
import nl.digitalekabeltelevisie.gui.exception.NotAnMPEGFileException;
import nl.digitalekabeltelevisie.util.CLI.ExportData;
import nl.digitalekabeltelevisie.util.CLI.beans.PesData;

public class ExportDataTest {

	private TransportStream transportStream;

	final ExportData exportData = new ExportData();

	public void initTs() throws NotAnMPEGFileException, IOException {
		String fileName = "./src/test/resources/digitenne 482000 06-30 19-43-54.ts";
		final TransportStream ts = new TransportStream(fileName);
		transportStream = ts;
		transportStream.parseStream();
	}

	@Test
	public void getPidWPesTest() throws NotAnMPEGFileException, IOException {

		initTs();
		List<Integer> pidWPes = exportData.getPidWPes(transportStream);

		Integer[] expected = { 7011, 7012, 7013, 7021, 7022, 7023, 7031, 7032, 7033, 7041, 7042, 7043, 7112, 7122, 7132, 7142, 7152, 7162, 7172, 7182, 7192 };

		for(int i=0; i< expected.length; i++){
			Assert.assertEquals(expected[i], pidWPes.get(i));
		}
	}

	@Test
	public void collectPesTest() throws NotAnMPEGFileException, IOException {
		
		initTs();
		List<Integer> pids = new ArrayList<Integer>();
		List<Integer> falsePids = new ArrayList<Integer>();
		pids.add(7011);
		pids.add(7012);
		pids.add(7013);
		pids.add(33);
		
		falsePids.add(31);
		falsePids.add(32);
		falsePids.add(33);

		List<PesData> pesData = exportData.collectPes(transportStream, pids);
		List<PesData> falsePesData = exportData.collectPes(transportStream, falsePids);

		Assert.assertEquals(3,pesData.size());
		Assert.assertEquals(0,falsePesData.size());
	}
}
