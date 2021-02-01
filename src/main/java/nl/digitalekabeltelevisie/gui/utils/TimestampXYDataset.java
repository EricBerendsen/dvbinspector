/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2021 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

import java.util.*;
import java.util.logging.Logger;

import org.jfree.data.DomainOrder;
import org.jfree.data.general.*;
import org.jfree.data.xy.XYDataset;

import nl.digitalekabeltelevisie.controller.ViewContext;
import nl.digitalekabeltelevisie.data.mpeg.*;
import nl.digitalekabeltelevisie.data.mpeg.psi.*;
import nl.digitalekabeltelevisie.data.mpeg.psi.PMTsection.Component;

public class TimestampXYDataset implements XYDataset {
	
	/** The group that the dataset belongs to. */
    private DatasetGroup group;

    private ArrayList<List<TimeStamp>> seriesList = new ArrayList<>();
    ArrayList<String> seriesKeys = new ArrayList<>(); 
    ArrayList<Integer> seriesOffset = new ArrayList<>();
    ArrayList<Integer> seriesViewContextLength = new ArrayList<>();
    
    int startPacket;
    int endPacket;
    
	private static final Logger logger = Logger.getLogger(TimestampXYDataset.class.getName());

    

	public TimestampXYDataset(PMTsection pmt, TransportStream transportStream, ViewContext viewContext) {
		
		short pcrPid= (short)pmt.getPcrPid();

		startPacket = viewContext.getStartPacket();
		endPacket = viewContext.getEndPacket();

		if(transportStream.getPID(pcrPid)!=null){ // should not happen, however leave it up to UPC to fuck up...
			String pcrLabel = pcrPid+" - "+transportStream.getShortLabel(pcrPid)+" PCR";
			addToSeriesList(transportStream.getPID(pcrPid).getPcrList(),pcrLabel);
		}
		
		boolean hasSCTE35 = PsiSectionData.hasSCTE35RegistrationDescriptor(pmt.getDescriptorList());
		for(Component component:pmt.getComponentenList()){
			final PID pid = transportStream.getPID((short) component.getElementaryPID());
			if(pid!=null){
				addToSeriesList(pid.getPtsList(),getComponentLabel(transportStream, component)+" PTS");
				addToSeriesList(pid.getDtsList(),getComponentLabel(transportStream, component)+" DTS");
				
				findSCTE35Points(transportStream, hasSCTE35, component);
			}
		}
		
	}

	private void findSCTE35Points(TransportStream transportStream, boolean hasSCTE35, Component component) {
		if(hasSCTE35 && component.getStreamtype()==0x86){
			List<TimeStamp> exitPoints = new ArrayList<>();
			List<TimeStamp> returnPoints = new ArrayList<>();
			List<TimeStamp> timeSignalPoints = new ArrayList<>();
			SpliceInfoSections spliceSections = transportStream.getPsi().getScte35_table().getSpliceInfoSections((short) component.getElementaryPID());
			if(spliceSections!=null){
				findSpliceInserts(exitPoints, returnPoints, timeSignalPoints,spliceSections);
				addToSeriesList(exitPoints, getComponentLabel(transportStream, component) + " Exit Point");
				addToSeriesList(returnPoints, getComponentLabel(transportStream, component) + " Return Point");
				addToSeriesList(timeSignalPoints, getComponentLabel(transportStream, component) + " time_signal");
			}
		}
	}

	private static void findSpliceInserts(List<TimeStamp> exitPoints, List<TimeStamp> returnPoints, List<TimeStamp> timeSignalPoints,
			SpliceInfoSections spliceSections) {
		List<SpliceInfoSection> spliceInfoSectionList = spliceSections.getSpliceInfoSectionList();
		for (SpliceInfoSection spliceSection : spliceInfoSectionList) {
			if (spliceSection.getSplice_command_type() == 5) {
				SpliceInfoSection.SpliceInsert spliceInsert = (SpliceInfoSection.SpliceInsert) spliceSection
						.getSplice_command();
				// handle Program Splice Point whereby all PIDs/components of the program are to be spliced.
				if ((spliceInsert.getProgram_splice_flag() == 1) && (spliceInsert.getSplice_immediate_flag() == 0)) {
					handleProgramSplicePoint(exitPoints, returnPoints, spliceSection, spliceInsert);
				}else{
					// TODO handle Component Splice Mode whereby each component that is intended to be spliced will be listed separately
					logger.warning("SCTE35 Component Splice Mode not yet supported, please report");
				}
			} else if (spliceSection.getSplice_command_type() == 6) {
				SpliceInfoSection.TimeSignal timeSignal = (SpliceInfoSection.TimeSignal) spliceSection
						.getSplice_command();
				SpliceInfoSection.SpliceTime spliceTime = timeSignal.getSplice_time();
				if ((spliceTime != null) && (spliceTime.getTime_specified_flag() == 1)) {
					TimeStamp ts = new TimeStamp(spliceSection.getPacket_no(), spliceTime.getSpliceTimeAdjusted());
					timeSignalPoints.add(ts);
				}
			}
		}
	}

	private static void handleProgramSplicePoint(List<TimeStamp> exitPoints, List<TimeStamp> returnPoints,
			SpliceInfoSection spliceSection, SpliceInfoSection.SpliceInsert spliceInsert) {
		SpliceInfoSection.SpliceTime spliceTime = spliceInsert.getSplice_time();
		if ((spliceTime != null) && (spliceTime.getTime_specified_flag() == 1)) {
			TimeStamp ts = new TimeStamp(spliceSection.getPacket_no(), spliceTime.getSpliceTimeAdjusted());
			if (spliceInsert.getOut_of_network_indicator() == 1) {
				exitPoints.add(ts);
			} else {
				returnPoints.add(ts);
			}
		}
	}


	private static String getComponentLabel(TransportStream transportStream, Component component) {
		return (short) component.getElementaryPID()+" - "+transportStream.getShortLabel((short) component.getElementaryPID());
	}

	private void addToSeriesList(final List<TimeStamp> list, String componentLabel) {
		if((list!=null)&&(list.size()>0)){
			seriesList.add(list);
			seriesKeys.add(componentLabel);
			TimeStamp startKey = new TimeStamp(startPacket, 0);
			TimeStamp endKey = new TimeStamp(endPacket, Long.MAX_VALUE);
			Comparator<TimeStamp> comperator = Comparator
											.comparingInt(TimeStamp::getPacketNo)
											.thenComparingLong(TimeStamp::getTime);
			
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
	public Comparable<?> getSeriesKey(int series) {
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
		return seriesViewContextLength.get(series);
	}

	@Override
	public Number getX(int series, int item) {
		return getTimestamp(series, item).getPacketNo();
	}

	private TimeStamp getTimestamp(int series, int item) {
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
