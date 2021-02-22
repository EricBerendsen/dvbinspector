/**
 * 
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 * 
 *  This code is Copyright 2009-2018 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.pes;

import static org.junit.Assert.*;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.tree.DefaultMutableTreeNode;

import org.junit.BeforeClass;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.*;
import nl.digitalekabeltelevisie.gui.ImageSource;
import nl.digitalekabeltelevisie.gui.exception.NotAnMPEGFileException;
import nl.digitalekabeltelevisie.util.PreferencesManager;

public class PesHandlerSetup {

	/**
	 * 
	 */
	protected static TransportStream transportStream;
	protected static final Integer NPO1_H264_PID = 2001;
	protected static final Integer NPO1_EBU_PID = 2101;
	protected static final Integer NPO1_AUD_PID = 2013;
	protected static final Integer NPO2_H264_PID = 2301;
	protected static final Integer NPO2_SUB_PID = 2403;
	protected static final Integer TVE_H222_PID = 2901;

	@BeforeClass
	public static void setUp() throws NotAnMPEGFileException, IOException, URISyntaxException {
	
		PreferencesManager.setPacketLengthModus(0);
		final URL resource = PesHandlerSetup.class.getResource("/NPO12HD.ts");
		// spaces in dirname...
		final File ts =  new File(resource.toURI());
		transportStream = new TransportStream(ts);
		transportStream.parseStream();
	
		final Map<Integer, GeneralPidHandler> map = new HashMap<>();
	
		addPesHandler(map,NPO1_H264_PID);
		addPesHandler(map,NPO1_EBU_PID);
		addPesHandler(map,NPO2_H264_PID);
		addPesHandler(map,NPO1_AUD_PID);
		addPesHandler(map,NPO2_SUB_PID);
		addPesHandler(map,TVE_H222_PID);
		
		transportStream.parsePidStreams(map);
	}

	private static void addPesHandler(final Map<Integer, GeneralPidHandler> map, Integer pid) {
		map.put(pid, transportStream.getPID(pid).getPidHandler());
	}

	protected static void validatePreviewImageSize(DefaultMutableTreeNode tree, int height, int width, String msg) {
		Object o = tree.getUserObject();
		assertNotNull(msg,o);
		
		assertEquals(KVP.class, o.getClass());
		KVP kvp = (KVP)o;
		ImageSource imgSrc = kvp.getImageSource();
		assertNotNull(msg,imgSrc);
		BufferedImage img = imgSrc.getImage();
		assertNotNull(msg,img);
		assertEquals(msg+" img height",height,img.getHeight());
		assertEquals(msg+" img width",width,img.getWidth());
	}

}
