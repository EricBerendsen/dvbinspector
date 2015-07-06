/**
 * 
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 * 
 *  This code is Copyright 2009-2015 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
 * 
 *  This file is part of DVB Inspector.
 * 
 *  DVB Inspector is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  DVB Inspector is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with DVB Inspector.  If not, see <http://www.gnu.org/licenses/>.
 * 
 *  The author requests that he be notified of any application, applet, or
 *  other binary that makes use of this code, but that's more out of curiosity
 *  than anything and is not required.
 * 
 */

package nl.digitalekabeltelevisie.data.mpeg.pes.video264;

import static org.junit.Assert.*;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

import nl.digitalekabeltelevisie.data.mpeg.*;
import nl.digitalekabeltelevisie.data.mpeg.pes.GeneralPesHandler;
import nl.digitalekabeltelevisie.data.mpeg.pes.video26x.H26xHandler;
import nl.digitalekabeltelevisie.gui.exception.NotAnMPEGFileException;

import org.junit.*;
//import static org.junit.Assert.assertNotNull;

/**
 * @author Eric
 *
 */
public class Video14496HandlerWritePES {

	/**
	 * 
	 */
	private static TransportStream transportStream;
	private static final Integer NPO1_H264_PID = 2001;
	private static final Integer NPO2_H264_PID = 2301;
	private final static Object o = new Object();

	@BeforeClass
	public static void setUp() throws NotAnMPEGFileException, IOException, URISyntaxException{

		//
		final URL resource = o.getClass().getResource("/NPO12HD.ts");
		// spaces in dirname...
		final File ts =  new File(resource.toURI());
		transportStream = new TransportStream(ts);
		transportStream.parseStream();

		final Map<Integer, GeneralPesHandler> map = new HashMap<>();

		final PID p1= transportStream.getPID(NPO1_H264_PID);
		map.put(NPO1_H264_PID, p1.getPesHandler());
		final PID p2= transportStream.getPID(NPO2_H264_PID);
		map.put(NPO2_H264_PID, p2.getPesHandler());
		transportStream.parseStream(null, map);
	}


	@Test
	public void testNPO1() {

		assertNotNull("transportStream = null",transportStream);
		final PID npo1 = transportStream.getPID(NPO1_H264_PID);
		assertNotNull("npo1 = null",npo1);

		assertEquals("number of TS packets",36538, npo1.getPackets());
		assertEquals("PID Type",PID.PES, npo1.getType());


		assertEquals("PCR Count",125, npo1.getPcr_count());

		final GeneralPesHandler pesHandler = npo1.getPesHandler();
		assertEquals(Video14496Handler.class, pesHandler.getClass());

		final H26xHandler video14496Handler = (H26xHandler) pesHandler;

		final List<PesPacketData> pesPackets = video14496Handler.getPesPackets();

		final String FILE_NAME = "d:\\8\\Npo1.es";

		System.out.println("No of Pes Packets;"+pesPackets.size());
		int i = 0;
		long l = 0;
		try {
			final Path path = Paths .get(FILE_NAME);
			Files.createFile(path);
			for (final PesPacketData packet : pesPackets) {
				final int start = packet.getPesDataStart();
				final int len = packet.getPesDataLen();
				l += len;
				i++;
				System.out.println("i;"+ (i++)+", l;"+l);

				final byte[] data = packet.getData();
				final byte[] aBytes = Arrays.copyOfRange(data, start, start+len);
				final int resLen = aBytes.length;
				System.out.println("i;"+ (i++)+", l;"+l + ", packet len:"+len+", resLen:"+resLen );
				Files.write(path, aBytes,StandardOpenOption.WRITE, StandardOpenOption.APPEND);
			}
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


}
