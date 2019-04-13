/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2019 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.pes.video;



import static nl.digitalekabeltelevisie.util.Utils.addListJTree;

import java.awt.Color;
import java.awt.Paint;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.ChartLabel;
import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.PesPacketData;
import nl.digitalekabeltelevisie.data.mpeg.pes.GeneralPesHandler;
import nl.digitalekabeltelevisie.gui.ImageSource;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DefaultKeyedValues2DDataset;

/**
 * @author Eric Berendsen
 *
 */
public class Video138182Handler  extends GeneralPesHandler implements ImageSource{

	/**
	 * Helper class for the getImage method, to color the bars in the bar chart different for I, B and P frames.
	 *
	 * This really should not be here, that is what you get for mixing model and presentation code.
	 *
	 * @author Eric
	 *
	 */
	public class DifferenceBarRenderer extends BarRenderer {
		  public DifferenceBarRenderer() {
		    super();
		  }
		  public Paint getItemPaint(int x_row, int x_col) {
		    CategoryDataset l_jfcDataset = getPlot().getDataset();
		    ChartLabel l_colKey = (ChartLabel)l_jfcDataset.getColumnKey(x_col);
		    if("I".equals(l_colKey.getLabel())){
		    	return Color.RED;
		    }else if("P".equals(l_colKey.getLabel())){
		    	return Color.BLUE;
		    }else if("B".equals(l_colKey.getLabel())){
		    	return Color.GREEN;
		    }
		    // unknown type
		    return Color.YELLOW;

		  }
		}


	/**
	 * Meta Iterator to iterate over all VideoMPEG2Sections in this PES stream, regardless of grouping in PES Packets
	 * TODO does not handle PES packets which contain no VideoMPEG2Section, like in iso/np.ts
	 * In general this does not work for streams with no alignment, So every VideoMPEG2Section should be contained in a PES packet.
	 * Most broadcast streams are aligned, some ISO test streams are not. So we can live with this for now.
	 * @author Eric
	 *
	 */
	private class MPEG2SectionIterator{// implements Iterator<VideoMPEG2Section> {

		Iterator<PesPacketData> pesIterator = null;
		VideoMPEG2Section nextSection = null;
		private Iterator<VideoMPEG2Section> sectionIter;

		public MPEG2SectionIterator() {
			pesIterator = pesPackets.iterator();
			sectionIter = getNextSectionIter();
			if(sectionIter!=null){
				nextSection = sectionIter.next();
			}
		}

		private Iterator<VideoMPEG2Section> getNextSectionIter(){

			Iterator<VideoMPEG2Section> result = null;
			do {
				VideoPESDataField pesPacket = (VideoPESDataField)pesIterator.next();
				result = pesPacket.getSections().iterator();

			} while (((result==null)||!result.hasNext())&&(pesIterator.hasNext()));
			return result;

		}

		public VideoMPEG2Section next() {
			VideoMPEG2Section result = nextSection;
			if((sectionIter!=null)&&sectionIter.hasNext()){
				nextSection = sectionIter.next();
			}else if(pesIterator.hasNext()){
				sectionIter= getNextSectionIter();
				if(sectionIter.hasNext()){
					nextSection = sectionIter.next();
				}else{
					nextSection = null;
				}
			}else{
				nextSection = null;
			}

			return result;
		}


	}

	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.data.mpeg.pes.GeneralPesHandler#processPesDataBytes(int, byte[], int, int)
	 */
	@Override
	protected void processPesDataBytes(final PesPacketData pesData){
		pesPackets.add(new VideoPESDataField(pesData));

	}


	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.data.mpeg.pes.GeneralPesHandler#getJTreeNode(int)
	 */
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode s=new DefaultMutableTreeNode(new KVP("13818-2 PES Data",this));
		addListJTree(s,pesPackets,modus,"PES Packets");

		return s;
	}

	/**
	 * find IFrame closest to the supplied pts, and return it's image in the requested size (height * width)
	 *
	 * @param height
	 * @param width
	 * @param pts
	 * @return
	 */
	public BufferedImage getImage(int height, int width, long pts) {
		VideoPESDataField resultPES = null;

		long diff = Long.MAX_VALUE;
		for (PesPacketData pesPacket : pesPackets) { // iterate over all video frames, in case pts wraps around
			VideoPESDataField video = (VideoPESDataField)pesPacket;
			if(video.hasIFrame() && (Math.abs(video.getPesHeader().getPts() - pts) <diff)){
				resultPES = video;
				diff = Math.abs(video.getPesHeader().getPts() - pts);
			}
		}
		if(resultPES!=null){
			return resultPES.getImage(width,height);
		}else{
			return null;
		}
	}

	public MPEG2SectionIterator getSectionIterator(){

		return new MPEG2SectionIterator();
	}

	/**
	 * Create a image of a BarChart which shows the size of individual Frames
	 *
	 * @see nl.digitalekabeltelevisie.gui.ImageSource#getImage()
	 */
	@Override
	public BufferedImage getImage() {

		List<ChartLabel> labels = new ArrayList<ChartLabel>();
		ChartLabel label = null;
		List<Integer> frameSize  = new ArrayList<Integer>();

			int length = 0;
			int count = 0;

			MPEG2SectionIterator iter = getSectionIterator();
			VideoMPEG2Section section = iter.next();
			while(section!=null){
				while((section!=null)&&(section.startCode==00)){ // new frame
					label =new ChartLabel(((PictureHeader)section).getPictureCodingTypeShortString(), (short)count);
					length = section.getLength();
					section = iter.next();
					while((section!=null)&&
							// user date, or extensions before slice data
							((section.startCode==0xB2)||(section.startCode==0xB5))){
						length += section.getLength();
						section = iter.next();
					}
					while((section!=null)&&
							// slice data
							((section.startCode>=0x01)&&(section.startCode<=0xAF))){
						length += section.getLength();
						section = iter.next();
					}
					// end slice data, is end of picture
					labels.add(label);
					frameSize.add(length);
					count++;
					length = 0;
					// start looking for next picture_start_code, ignore everything else
					// (sequence headers, sequence extensions, group_start_code
					while((section!=null)&&
							// not start of picture, ignore
							(section.startCode!=0x00)){
						section = iter.next();
					}
				}
				// initial sections before first picture_start_code, or
				// sequence headers, sequence extensions, group_start_code after slice
				//r.append(section).append("<br>");
				section = iter.next();
			}

			// all done


			Iterator<Integer> frameSizeIter = frameSize.iterator();
			DefaultKeyedValues2DDataset dataset = new DefaultKeyedValues2DDataset();
			BarRenderer renderer = new DifferenceBarRenderer();
			renderer.setShadowVisible(false);
			renderer.setDrawBarOutline(false);
			renderer.setItemMargin(0.0);
			int displayCount = 0;
			for(ChartLabel l:labels){
				if(frameSizeIter.hasNext()){
					dataset.setValue(frameSizeIter.next(), "",l);
					displayCount++;
				}
			}

			final CategoryAxis categoryAxis = new CategoryAxis("time");
			categoryAxis.setCategoryMargin(0.0);
			categoryAxis.setUpperMargin(0.0);
			categoryAxis.setLowerMargin(0.0);
			final ValueAxis valueAxis = new NumberAxis("frame size (bytes)");

			final CategoryPlot plot = new CategoryPlot(dataset, categoryAxis, valueAxis,renderer);
			String title = getPID().getShortLabel()+" (Transmission Order)";
			JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT,plot, false);

			return chart.createBufferedImage((displayCount*18)+100, 640);

		}

}
