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
	private static final Object o = new Object();

	@BeforeClass
	public static void setUp() throws NotAnMPEGFileException, IOException, URISyntaxException {
	
		//
		final URL resource = o.getClass().getResource("/NPO12HD.ts");
		// spaces in dirname...
		final File ts =  new File(resource.toURI());
		transportStream = new TransportStream(ts);
		transportStream.parseStream();
	
		final Map<Integer, GeneralPesHandler> map = new HashMap<>();
	
		addPesHandler(map,NPO1_H264_PID);
		addPesHandler(map,NPO1_EBU_PID);
		addPesHandler(map,NPO2_H264_PID);
		addPesHandler(map,NPO1_AUD_PID);
		addPesHandler(map,NPO2_SUB_PID);
		addPesHandler(map,TVE_H222_PID);
		
		transportStream.parseStream(null, map);
	}

	private static void addPesHandler(final Map<Integer, GeneralPesHandler> map, Integer pid) {
		map.put(pid, transportStream.getPID(pid).getPesHandler());
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
