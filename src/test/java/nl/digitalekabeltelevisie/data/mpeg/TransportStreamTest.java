package nl.digitalekabeltelevisie.data.mpeg;

import static org.junit.Assert.*;

import java.io.*;
import java.net.*;

import org.junit.*;

import nl.digitalekabeltelevisie.gui.exception.NotAnMPEGFileException;
import nl.digitalekabeltelevisie.util.PreferencesManager;

public class TransportStreamTest {

	private static TransportStream transportStream;

	@BeforeClass
	public static void setUp() throws NotAnMPEGFileException, IOException, URISyntaxException{

		PreferencesManager.setPacketLengthModus(0);
		final URL resource = TransportStreamTest.class.getResource("/digitenne 482000 06-30 19-43-54.ts");
		// spaces in dirname...
		final File ts =  new File(resource.toURI());
		transportStream = new TransportStream(ts);
		transportStream.parseStream();

	}
	
	
	@Test
	public void generalHealthTest() {
		
		assertNotNull("transportStream = null",transportStream);
		assertEquals("size",64587776, transportStream.getLen());
		assertEquals("TS Packets",343552, transportStream.getNo_packets());
		assertEquals("packet size",188, transportStream.getPacketLenghth());
		assertEquals("Error packets",0, transportStream.getError_packets());
		assertEquals("Sync Errors",0, transportStream.getSync_errors());
		assertEquals("bitrate",14929413, transportStream.getBitRate());
		
		assertEquals("PIDs used",43,transportStream.getNoPIDS());
	}

	@Test
	public void packet0Test() {
		TSPacket packet0 = transportStream.getTSPacket(0);
		assertNotNull(packet0);
		assertEquals("PID packet0",7011, packet0.getPID());
		assertEquals("payload_unit_start_indicator",0,packet0.getPayloadUnitStartIndicator());
		assertEquals("adaptation_field_control",1, packet0.getAdaptationFieldControl());
		assertEquals("continuity_counter",1, packet0.getContinuityCounter());
		assertEquals("file offset",0, packet0.getPacketOffset());
	}

	@Test
	public void packet140022Test() {
		TSPacket packet140022 = transportStream.getTSPacket(140022);
		assertNotNull(packet140022);
		assertEquals("PID packet140022",7042, packet140022.getPID());
		assertEquals("payload_unit_start_indicator",0,packet140022.getPayloadUnitStartIndicator());
		assertEquals("adaptation_field_control",3, packet140022.getAdaptationFieldControl());
		assertEquals("continuity_counter",14, packet140022.getContinuityCounter());
		assertEquals("file offset",26324136, packet140022.getPacketOffset());
	}

	@Test
	public void packet243867Test() {
		TSPacket packet243867 = transportStream.getTSPacket(243867);
		assertNotNull(packet243867);
		assertEquals("PID packet243867",7172, packet243867.getPID());
		assertEquals("payload_unit_start_indicator",1,packet243867.getPayloadUnitStartIndicator());
		assertEquals("adaptation_field_control",3, packet243867.getAdaptationFieldControl());
		assertEquals("continuity_counter",13, packet243867.getContinuityCounter());
		assertEquals("file offset",45846996, packet243867.getPacketOffset());
	}

	@Test
	public void packet328081Test() {
		TSPacket packet328081 = transportStream.getTSPacket(328081);
		assertNotNull(packet328081);
		assertEquals("PID packet328081",7011, packet328081.getPID());
		assertEquals("payload_unit_start_indicator",0,packet328081.getPayloadUnitStartIndicator());
		assertEquals("adaptation_field_control",1, packet328081.getAdaptationFieldControl());
		assertEquals("continuity_counter",8, packet328081.getContinuityCounter());
		assertEquals("file offset",61679228, packet328081.getPacketOffset());
	}

}
