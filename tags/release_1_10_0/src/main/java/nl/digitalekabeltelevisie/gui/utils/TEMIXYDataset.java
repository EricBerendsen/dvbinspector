/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2017 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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
package nl.digitalekabeltelevisie.gui.utils;

import java.math.BigInteger;
import java.util.*;
import java.util.Map.Entry;

import org.jfree.data.DomainOrder;
import org.jfree.data.general.*;
import org.jfree.data.xy.XYDataset;

import nl.digitalekabeltelevisie.controller.ViewContext;
import nl.digitalekabeltelevisie.data.mpeg.*;
import nl.digitalekabeltelevisie.data.mpeg.psi.PMTsection;
import nl.digitalekabeltelevisie.data.mpeg.psi.PMTsection.Component;

public class TEMIXYDataset implements XYDataset {
	
	/** The group that the dataset belongs to. */
    private DatasetGroup group;

    private ArrayList<List<TemiTimeStamp>> seriesList = new ArrayList<>();
    ArrayList<String> seriesKeys = new ArrayList<>(); 
    ArrayList<Integer> seriesOffset = new ArrayList<>();
    ArrayList<Integer> seriesViewContextLength = new ArrayList<>();
    
    int startPacket;
    int endPacket;
    

	public TEMIXYDataset(PMTsection pmt, TransportStream transportStream, ViewContext viewContext) {

		startPacket = viewContext.getStartPacket();
		endPacket = viewContext.getEndPacket();

		for (Component c : pmt.getComponentenList()) {
			short componentPid = (short) c.getElementaryPID();
			String componentLabel = componentPid + " - " + transportStream.getShortLabel(componentPid);
			final PID pid = transportStream.getPID(componentPid);
			if (pid != null) {
				HashMap<Integer, ArrayList<TemiTimeStamp>> temiMap = pid.getTemiList();
				for (Entry<Integer, ArrayList<TemiTimeStamp>> entry : temiMap.entrySet()) {
					int time_line_id = entry.getKey();
					ArrayList<TemiTimeStamp> value = entry.getValue();
					addToSeriesList(value, componentLabel + " TEMI time_line_id:" + time_line_id);
				}
			}
		}
	}

	private void addToSeriesList(final List<TemiTimeStamp> list, String componentLabel) {
		if((list!=null)&&(list.size()>0)){
			seriesList.add(list);
			seriesKeys.add(componentLabel);
			TemiTimeStamp startKey = new TemiTimeStamp(startPacket,BigInteger.ZERO);
			TemiTimeStamp endKey = new TemiTimeStamp(endPacket, BigInteger.valueOf(Long.MAX_VALUE));
			Comparator<TemiTimeStamp> comperator = new Comparator<TemiTimeStamp>() {
				
				@Override
				public int compare(TemiTimeStamp o1, TemiTimeStamp o2) {
					if (o1.getPacketNo() < o2.getPacketNo()) {
						return -1;
					} else if (o1.getPacketNo() > o2.getPacketNo()) {
						return 1;
					} else{
						return (o1.getMediaTimeStamp().compareTo(o2.getMediaTimeStamp())); 
					}
				}
			};
			int startOffset = Collections.binarySearch(list, startKey, comperator);
			if(startOffset<0){ 
				startOffset = (-startOffset)-1;
			}
			int endRange = Collections.binarySearch(list, endKey, comperator);
			
			if(endRange<0){ 
				endRange = (-endRange)-1;
			}

			seriesOffset.add(startOffset);
			seriesViewContextLength.add(endRange-startOffset);
		}
	}

	@Override
	public int getSeriesCount() {
		return seriesList.size();
	}

	@Override
	public String getSeriesKey(int series) {
		return seriesKeys.get(series);
	}

	@Override
	public int indexOf(@SuppressWarnings("rawtypes") Comparable seriesKey) {
		return seriesKeys.indexOf(seriesKey);
	}

	@Override
	public void addChangeListener(DatasetChangeListener listener) {
		// NOT USED

	}

	@Override
	public void removeChangeListener(DatasetChangeListener listener) {
		// NOT USED

	}

	@Override
	public DatasetGroup getGroup() {
		return group;
	}

	@Override
	public void setGroup(DatasetGroup group) {
		this.group = group;
	}

	@Override
	public DomainOrder getDomainOrder() {
		return DomainOrder.ASCENDING;
	}

	@Override
	public int getItemCount(int series) {
		//return seriesList.get(series).size();
		return seriesViewContextLength.get(series);
	}

	@Override
	public Number getX(int series, int item) {
		return getTimestamp(series, item).getPacketNo();
	}

	protected TemiTimeStamp getTimestamp(int series, int item) {
		return seriesList.get(series).get(item+seriesOffset.get(series));
	}

	@Override
	public double getXValue(int series, int item) {
		return getTimestamp(series, item).getPacketNo();
	}

	@Override
	public Number getY(int series, int item) {
		return getTimestamp(series, item).getTime();
	}

	@Override
	public double getYValue(int series, int item) {
		return getTimestamp(series, item).getTime();
	}

}
