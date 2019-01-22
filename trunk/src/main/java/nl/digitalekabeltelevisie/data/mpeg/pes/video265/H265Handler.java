/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2016 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.pes.video265;



import static nl.digitalekabeltelevisie.util.Utils.addListJTree;

import java.awt.image.BufferedImage;
import java.util.*;

import javax.swing.tree.DefaultMutableTreeNode;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.data.general.DefaultKeyedValues2DDataset;

import nl.digitalekabeltelevisie.controller.*;
import nl.digitalekabeltelevisie.data.mpeg.PesPacketData;
import nl.digitalekabeltelevisie.data.mpeg.pes.video26x.*;
import nl.digitalekabeltelevisie.gui.ImageSource;

/**
 * @author Eric Berendsen
 *
 */
public class H265Handler extends H26xHandler<Video265PESDataField, H265NALUnit> implements ImageSource{


	private final int MAX_TYPES =64; // incl potential extensions
	
	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.data.mpeg.pes.GeneralPesHandler#processPesDataBytes(int, byte[], int, int)
	 */
	@Override
	protected void processPesDataBytes(final PesPacketData pesData){
		pesPackets.add(new Video265PESDataField(pesData));

	}


	

	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.gui.HTMLSource#getHTML()
	 */
	@Override
	public BufferedImage getImage() {

		final List<int[]> frameSize  = new ArrayList<int[]>();
		final List<ChartLabel> labels = new ArrayList<ChartLabel>();

		int[] accessUnitData = new int[MAX_TYPES]; 
		
		boolean [] typeUsed  = new boolean[MAX_TYPES]; 
		
		int count = 0;

		final NALUnitIterator nalIter = new NALUnitIterator();
		H265NALUnit unit = nalIter.next();
		while(unit!=null){
			final RBSP rbsp = unit.getRbsp();
			if(rbsp!=null){
				if(( rbsp instanceof Access_unit_delimiter_rbsp) ||
						( rbsp instanceof Seq_parameter_set_rbsp) ||
						( rbsp instanceof Pic_parameter_set_rbsp) ||
						( rbsp instanceof Video_parameter_set_rbsp) ||
						( rbsp instanceof Sei_rbsp)){

					count = drawBarAccessUnit(frameSize, labels, accessUnitData, count);
					accessUnitData = new int[MAX_TYPES];
				}else if( rbsp instanceof Slice_segment_layer_rbsp){
					final int slice_type = unit.getNal_unit_type().getType();
					final int size = unit.getNumBytesInRBSP();
					accessUnitData[slice_type] += size;
					typeUsed[slice_type] = true;
				}else if( rbsp instanceof Filler_data_rbsp){
					final int size = unit.getNumBytesInRBSP();
					int sliceType = NALUnitType.FD_NUT.getType();
					accessUnitData[sliceType] += size;
					typeUsed[sliceType] = true;
				}
			}
			unit =  nalIter.next();
		}
		// last unit is not followed by delimiter
		count = drawBarAccessUnit(frameSize, labels, accessUnitData, count);

		final DefaultKeyedValues2DDataset dataset = new DefaultKeyedValues2DDataset();


		for (int i = 0; i < MAX_TYPES; i++) {
			NALUnitType nalType = NALUnitType.getByType(i);
			if (nalType != null) {
				if(typeUsed[i]) {
					final String typeName = nalType.name();
					final Iterator<int[]> frameSizeIter = frameSize.iterator();
					for (final ChartLabel l : labels) {
						if (frameSizeIter.hasNext()) {
							final int[] v = frameSizeIter.next();
							dataset.setValue(v[i], typeName, l);
						}
					}
				}
			}
		}

		final StackedBarRenderer renderer = new StackedBarRenderer();
		renderer.setShadowVisible(false);
		renderer.setDrawBarOutline(false);
		renderer.setItemMargin(0.0);

//		renderer.setSeriesPaint(0, Color.BLUE); // p
//		renderer.setSeriesPaint(1, Color.GREEN); // b
//		renderer.setSeriesPaint(2, Color.RED); // i
//		renderer.setSeriesPaint(3, Color.YELLOW); //SP
//		renderer.setSeriesPaint(4, Color.PINK); //SI
//		renderer.setSeriesPaint(5, Color.GRAY); // filler
//
		final CategoryAxis categoryAxis = new CategoryAxis("time");
		categoryAxis.setCategoryMargin(0.0);
		categoryAxis.setUpperMargin(0.0);
		categoryAxis.setLowerMargin(0.0);
		categoryAxis.setCategoryLabelPositions(CategoryLabelPositions.DOWN_90);
		final ValueAxis valueAxis = new NumberAxis("frame size (bytes)");

		final CategoryPlot plot = new CategoryPlot(dataset, categoryAxis, valueAxis, renderer);
		final String title = getPID().getShortLabel()+" (Access Units, Transmission Order)";
		final JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT,plot, true	);

		return chart.createBufferedImage(( frameSize.size()*18)+100, 640);
	}

	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.data.mpeg.pes.GeneralPesHandler#getJTreeNode(int)
	 */
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode s=new DefaultMutableTreeNode(new KVP("H.265 PES Data",this));
		addListJTree(s,pesPackets,modus,"PES Packets");

		return s;
	}



	@Override
	protected Video265PESDataField createH26xPESDataField(PesPacketData pesData) {
		return new Video265PESDataField(pesData);
	}

}
