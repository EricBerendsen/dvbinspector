/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2014 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.pes.audio;

import static nl.digitalekabeltelevisie.util.Utils.*;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JMenuItem;
import javax.swing.SwingWorker;
import javax.swing.tree.DefaultMutableTreeNode;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.decoder.Obuffer;
import javazoom.jl.decoder.SampleBuffer;
import javazoom.jl.player.AudioDevice;
import javazoom.jl.player.FactoryRegistry;
import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackListener;
import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.PesPacketData;
import nl.digitalekabeltelevisie.data.mpeg.pes.GeneralPesHandler;
import nl.digitalekabeltelevisie.data.mpeg.pes.audio.rds.UECP;
import nl.digitalekabeltelevisie.gui.ImageSource;

/**
 * @author Eric Berendsen
 *
 */
public class Audio138183Handler extends GeneralPesHandler implements ImageSource{

	final static int PIX_PER_SEC = 500;
	final static int LEFT_MARGIN = 60;
	final static int LEGEND_HEIGHT = 20;
	final static int GRAPH_HEIGHT = 400;

	private static final Logger logger = Logger.getLogger(Audio138183Handler.class.getName());


	class SwingPlayer extends SwingWorker<Void, Void>{

		AdvancedPlayer player;

		/* (non-Javadoc)
		 * @see javax.swing.SwingWorker#doInBackground()
		 */
		@Override
		protected Void doInBackground() throws Exception {
			try {
				AudioAccessUnit firstUnit = audioAccessUnits.get(0);
				int unitSize = firstUnit.getFrameSize();
				byte[] esData = getESData(unitSize);

				InputStream is = new ByteArrayInputStream(esData);
				AudioDevice dev = FactoryRegistry.systemRegistry().createAudioDevice();
				player = new AdvancedPlayer(is, dev);
				player.setPlayBackListener(new PlaybackListener() { // not interested in events. just to prevent nullpointers
				});
				player.play();
			} catch (JavaLayerException e) {
				logger.log(Level.WARNING,"error while playing sound",e);
			}
			return null;

		}

	      @Override
	       protected void done() {
	  		JMenuItem objectMenu = new JMenuItem("Play Audio");
			objectMenu.setActionCommand("play");
			kvp.setSubMenu(objectMenu);


	       }

		/**
		 *
		 */
		public void stop() {
			player.stop();

		}


	}

	private byte[] rdsData=new byte[0];
	private final List<AudioAccessUnit> audioAccessUnits = new ArrayList<AudioAccessUnit>();

	private SwingPlayer swPlayer = null;
	private KVP kvp = null;

	/**
	 * @param ancillaryDataidentifier
	 */
	public Audio138183Handler(final int ancillaryDataidentifier) {
		super();
		this.ancillaryDataIdentifier = ancillaryDataidentifier;
	}

	private final int ancillaryDataIdentifier; // used to identify RDS data

	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.data.mpeg.pes.GeneralPesHandler#processPesDataBytes(int, byte[], int, int)
	 */
	@Override
	public void processPesDataBytes(final PesPacketData pesData) {
		// AudioAccessUnit are not always aligned with PES Packet
		final AudioPESDataField audioPes = new AudioPESDataField(pesData);
		pesPackets.add(audioPes);


		copyIntoBuf(audioPes);

		final List<AudioAccessUnit> accessUnits = new ArrayList<AudioAccessUnit>();
		int i = bufStart;

		while ((i < (bufEnd)) && (i >= 0)) {
			i = indexOfSyncWord(pesDataBuffer,  i);
			if (i >= 0) { // found start,
				if ((i+4) <= bufEnd){ // at least 4 bytes, try to create an AudioAccessUnit
					AudioAccessUnit frame = new AudioAccessUnit(pesDataBuffer, i,audioPes.getPesHeader().getPts());
					int unitLen = frame.getFrameSize();
					if(unitLen<0) { // not a valid frame. start search again from next pos
						i++;
					}else if((i+unitLen+2)<bufEnd){  // see if at where next frame should start we also have syncword, and
						int nextIndex = indexOfSyncWord(pesDataBuffer,  i+unitLen);
						if(nextIndex==(i+unitLen)){
							accessUnits.add(frame);
							i = nextIndex;
							bufStart = nextIndex;
						}else{// not enough read, continu next time
							bufStart = i;
						}
					}else{// not enough read, continu next time
						bufStart = i;
						break;

					}
				}
			}
		}

		audioAccessUnits.addAll(accessUnits);

		if((ancillaryDataIdentifier & 0x40)!=0) {// RDS via UECP
			for(final AudioAccessUnit accessUnit:accessUnits){
				final AncillaryData ancillaryData = accessUnit.getAncillaryData();
				if((ancillaryData!=null)&&(ancillaryData.getSync()==0xFD)&&(ancillaryData.getDataFieldLength()!=0)){
					// append all data to single byte[]
					final byte[] b =ancillaryData.getDataByte();
					final byte[] newRdsData = new byte[rdsData.length + b.length];
					System.arraycopy(rdsData, 0, newRdsData, 0, rdsData.length);
					System.arraycopy(b, 0, newRdsData, rdsData.length, b.length);
					rdsData = newRdsData;
				}
			}
		}
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {

		kvp = new KVP("PES Data",this);
		if(swPlayer==null){
			JMenuItem objectMenu = new JMenuItem("Play Audio");
			objectMenu.setActionCommand("play");
			kvp.setSubMenuAndOwner(objectMenu,this);
		}else{
			final JMenuItem objectMenu = new JMenuItem("Stop Audio");
			objectMenu.setActionCommand("stop");
			kvp.setSubMenuAndOwner(objectMenu,this);
		}

		final DefaultMutableTreeNode s=new DefaultMutableTreeNode(kvp);

		addListJTree(s,pesPackets,modus,"PES Packets");

		addListJTree(s, audioAccessUnits, modus, "Audio Access Units");
		if((ancillaryDataIdentifier & 0x40)!=0) {// RDS via UECP
			final DefaultMutableTreeNode rdsNode = new DefaultMutableTreeNode(new KVP("RDS"));
			s.add(rdsNode);
			rdsNode.add(new DefaultMutableTreeNode(new KVP("RDS Data",rdsData,null)));
			final UECP uecp = new UECP(rdsData);
			rdsNode.add(uecp.getJTreeNode(modus));
		}

		return s;
	}


	/**
	 * Look for syncword (0xfff, 12 bits)
	 *
	 * @param source
	 * @param fromIndex
	 * @return
	 */
	private static int indexOfSyncWord(final byte[] source, final int fromIndex){
		if (fromIndex >= source.length) {
			return  -1;
		}

		final int max = source.length -1;

		for (int i = fromIndex; i < max; i++) {
			/* Look for first byte. */
			if (source[i] != -1) {
				while ((++i <= max) && (source[i] != -1)){ //0xFF
					// EMPTY body
				}
			}

			/* Found first byte, now look at second byteof 0xFFF */
			if ((i < max) && ((source[i+1] &0xF0)==0xF0 )) {
					/* Found whole string. */
					return i ;
				}
			}
		return -1;
	}

	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.gui.ImageSource#getImage()
	 */
	@Override
	public BufferedImage getImage() {
		if((audioAccessUnits==null)||(audioAccessUnits.size()==0)){
			return null;
		}

		AudioAccessUnit firstUnit = audioAccessUnits.get(0);
		int unitSize = firstUnit.getFrameSize();
		int samplingFreq = firstUnit.getSamplingFrequency();
		int channels = (firstUnit.getMode()==3)?1:2;

		int noUnits = audioAccessUnits.size();

		byte[] tmp = getESData(unitSize);

		InputStream is = new ByteArrayInputStream(tmp);
		Decoder decoder = new Decoder();

		Obuffer output = null;
		int height = channels * (GRAPH_HEIGHT+LEGEND_HEIGHT) ; // 512 for drawing, 2 for white border
		long noSamples = noUnits*1152L;
		int width =(int)( (noSamples*PIX_PER_SEC)/samplingFreq);  // force calculation in Long, truncate to int at the end

		int lengthSecs = (int)(noSamples/samplingFreq);
		BufferedImage img = new BufferedImage(width+LEFT_MARGIN, height,BufferedImage.TYPE_INT_ARGB);

		final Graphics2D gd = img.createGraphics();
		gd.setColor(Color.BLACK);
		gd.fillRect(0, 0, width+LEFT_MARGIN, height);

		//gd.setColor(Color.GRAY);
		for (int channel = 0; channel < channels; channel++) {
			gd.setColor(Color.WHITE);
			gd.fillRect(0, (GRAPH_HEIGHT * (1+channel)) + (LEGEND_HEIGHT * channel) , width+LEFT_MARGIN, LEGEND_HEIGHT);

			gd.setColor(Color.GRAY);
			for(int line=-30000;line<=30000;line+=5000){
				int y =  getY(channel, line);
				gd.drawLine(LEFT_MARGIN,y, LEFT_MARGIN+width, y);
				String label = ""+line;

				FontMetrics metrics = gd.getFontMetrics();
				int adv = metrics.stringWidth(label);
				gd.drawString(label, LEFT_MARGIN - adv - 5, y);
			}

			for(int t=0;t<=lengthSecs;t++){
				gd.setColor(Color.GRAY);
				gd.drawLine((t*PIX_PER_SEC)+LEFT_MARGIN, getY(channel, Short.MAX_VALUE), (t*PIX_PER_SEC)+LEFT_MARGIN, getY(channel, Short.MIN_VALUE));
				gd.setColor(Color.BLACK);
				String label = ""+t+" sec";
				gd.drawString(label, (t*PIX_PER_SEC)+LEFT_MARGIN, ((1+channel) * (GRAPH_HEIGHT +LEGEND_HEIGHT)) -4);
			}

		}

		gd.setColor(Color.RED);
		for (int channel = 0; channel < channels; channel++) {
			gd.drawLine(LEFT_MARGIN, getY(channel, 0), LEFT_MARGIN+width, getY(channel, 0));

		}

		Bitstream stream = new Bitstream(is);
		Header header;
		int runningX = 0;
		int minVal[] = {Integer.MAX_VALUE,Integer.MAX_VALUE};
		int maxVal[] = {Integer.MIN_VALUE,Integer.MIN_VALUE};
		int sampleNo = 0;
		try {
			while(true) {
				header = stream.readFrame();

				if (header==null){
					return img;
				}
				if (output==null){
					int freq = header.frequency();
					output = new SampleBuffer(freq,channels);
					decoder.setOutputBuffer(output);
				}
				SampleBuffer decoderOutput = (SampleBuffer)decoder.decodeFrame(header, stream);
				short [] buf = decoderOutput.getBuffer();

				gd.setColor(Color.GREEN);
				for (int i = 0; i < 1152; i++) { // TODO this assumes level 2
					for (int channel = 0; channel < channels; channel++) {
						short s = buf[(i*channels)+channel];
						maxVal[channel] = Math.max(s, maxVal[channel]);
						minVal[channel] = Math.min(s, minVal[channel]);

					}
					sampleNo++;
					int newX =(int) ((((long)sampleNo)*PIX_PER_SEC)/samplingFreq); // force calculation in long
					if(newX>runningX){
						for (int channel = 0; channel < channels; channel++) {
							gd.drawLine(runningX+LEFT_MARGIN, getY(channel,minVal[channel]), runningX+LEFT_MARGIN, getY(channel,maxVal[channel]));
							minVal[channel] = Integer.MAX_VALUE;
							maxVal[channel] = Integer.MIN_VALUE;
						}
						runningX = newX;
					}
				}
				stream.closeFrame();
			}
		} catch (JavaLayerException e) {
			logger.log(Level.WARNING,"error while decoding sound for drawing",e);
		}
		return img;
	}

	/**
	 * @param channel
	 * @param sample
	 * @return
	 */
	private static int getY(int channel, int sample) {
		int t =(int)(((long)sample * GRAPH_HEIGHT) / (2L * Short.MAX_VALUE));
		return ((GRAPH_HEIGHT/2) + (channel * (GRAPH_HEIGHT+LEGEND_HEIGHT)))- t;
	}

	/**
	 * @param unitSize
	 * @return
	 */
	private byte[] getESData(int unitSize) {
		ByteBuffer byteBuffer = ByteBuffer.allocate(audioAccessUnits.size()*unitSize);
		for (AudioAccessUnit unit : audioAccessUnits) {
			byteBuffer.put(unit.getData(),unit.getStart(),unit.getFrameSize());
		}

		byte[] tmp = byteBuffer.array();
		return tmp;
	}

	/**
	 *
	 */
	public void play() {
		swPlayer = new SwingPlayer();
		swPlayer.execute();
		final JMenuItem objectMenu = new JMenuItem("Stop Audio");
		objectMenu.setActionCommand("stop");
		kvp.setSubMenuAndOwner(objectMenu,this);
	}


	public void stop() {
		swPlayer.stop();
		JMenuItem objectMenu = new JMenuItem("Play Audio");
		objectMenu.setActionCommand("play");
		kvp.setSubMenuAndOwner(objectMenu,this);
	}

}
