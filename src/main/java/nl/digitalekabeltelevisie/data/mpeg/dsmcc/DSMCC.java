/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2020 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.dsmcc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.PSI;
import nl.digitalekabeltelevisie.data.mpeg.dsmcc.DSMCC_UNMessageSection.ModuleInfo;
import nl.digitalekabeltelevisie.data.mpeg.psi.AbstractPSITabel;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSectionExtendedSyntax;

/**
 * Contains all DSM-CC information from single PID. Make a separation by TableID (all other sections contain just one table ID, this can have 0x38 - 0x3f)
 *
 * single PID may be shared by multiple carousels
 *
 * table_id: 0x3B (59) => DSM-CC - U-N messages (DSI or DII) (first implemented
 * table_id: 0x3C (60) => DSM-CC - Download Data Messages (DDB)
 *
 * @author Eric Berendsen
 *
 */
public class DSMCC extends AbstractPSITabel{

	private final Map<Integer, DSMCC_UNMessageSection []> unMessages = new HashMap<>();
	private final Map<Integer, DSMCC_DownLoadDataMessageSection []> downloadMessages = new HashMap<>();
	private final Map<Integer, DSMCC_StreamDescriptorList[]> eventStreams = new HashMap<>();
	private int pid = 0;
	private boolean isObjectCarousel = true; // set to false for SSU

	private final static Logger logger = Logger.getLogger(DSMCC.class.getName());

	public DSMCC(final PSI parent, final boolean isObjectCarousel){
		super(parent);
		this.isObjectCarousel = isObjectCarousel;

	}

	public void update(final TableSectionExtendedSyntax section){
		pid=section.getParentPID().getPid();

		final int tableID  = section.getTableId();
		if(tableID==0x3b){ // DSM-CC - U-N messages (DSI or DII)
			// TODO DSI or DII can be only one single section, so no need for []
			final int tableIdExt = section.getTableIdExtension();
			DSMCC_UNMessageSection [] sections= unMessages.get(tableIdExt);
			final DSMCC_UNMessageSection unMessage = new DSMCC_UNMessageSection(section.getRaw_data(), section.getParentPID(),isObjectCarousel);

			if(sections==null){
				sections = new DSMCC_UNMessageSection[section.getSectionLastNumber()+1];
				unMessages.put(tableIdExt, sections);
			}
			if(sections[section.getSectionNumber()]==null){
				sections[section.getSectionNumber()] = unMessage;
			}else{
				final TableSection last = sections[section.getSectionNumber()];
				updateSectionVersion(unMessage, last);
			}
		}else if(tableID==0x3c){ // DSM-CC Download data message

			final int moduleID = section.getTableIdExtension();
			final DSMCC_DownLoadDataMessageSection downloadMessage = new DSMCC_DownLoadDataMessageSection(section.getRaw_data(), section.getParentPID());
			DSMCC_DownLoadDataMessageSection[] sections = downloadMessages.computeIfAbsent(moduleID, k -> new DSMCC_DownLoadDataMessageSection[section.getSectionLastNumber() + 1]);

			if(section.getSectionNumber()<=section.getSectionLastNumber()){ // this should always be the case, but Ziggo managed to break this rule...
				if(section.getSectionLastNumber()>=sections.length){ //new version has getSectionLastNumber > previous version, resize
					sections = Arrays.copyOf(sections, section.getSectionLastNumber()+1);
					downloadMessages.put(moduleID, sections);
				}
				if(sections[section.getSectionNumber()]==null){
					sections[section.getSectionNumber()] = downloadMessage;
				}else{
					final TableSection last = sections[section.getSectionNumber()];
					updateSectionVersion(downloadMessage, last);
				}
			}else{
				logger.info("SectionNumber > lastSectionNumber: pid:"+pid+",section.getSectionNumber:"+section.getSectionNumber()+" section.getSectionLastNumber:"+section.getSectionLastNumber()+",tableID:"+tableID);
			}
		}else if(tableID==0x3d){ // DSM-CC - stream descriptorlist;

			final int eventID = section.getTableIdExtension();
			final DSMCC_StreamDescriptorList streamDescriptorListSection = new DSMCC_StreamDescriptorList(section.getRaw_data(), section.getParentPID());
			DSMCC_StreamDescriptorList[] sections = eventStreams.computeIfAbsent(eventID, k -> new DSMCC_StreamDescriptorList[section.getSectionLastNumber() + 1]);

			if(sections[section.getSectionNumber()]==null){
				sections[section.getSectionNumber()] = streamDescriptorListSection;
			}else{
				final TableSection last = sections[section.getSectionNumber()];
				updateSectionVersion(streamDescriptorListSection, last);
			}

		}else{
			logger.info("Unhandled tableId: pid:"+pid+",section.getSectionNumber:"+section.getSectionNumber()+" section.getSectionLastNumber:"+section.getSectionLastNumber()+",tableID:"+tableID);
			
		}

	}

	public DefaultMutableTreeNode getJTreeNode(final int modus) {

		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("DSM-CC PID",pid,null ));
		TreeSet<Integer> s = new TreeSet<>(unMessages.keySet());

		Iterator<Integer> i = s.iterator();
		while(i.hasNext()){
			final Integer type=i.next();
			final DSMCC_UNMessageSection [] sections = unMessages.get(type);
			final DefaultMutableTreeNode n = new DefaultMutableTreeNode(new KVP("DSM-CC UN-Message",type, null));
			for (final DSMCC_UNMessageSection tsection : sections) {
				if(tsection!= null){
					addSectionVersionsToJTree(n, tsection, modus);
				}
			}
			t.add(n);

		}
		// TODO extract general method to add both DSMCC_DownLoadDataMessageSection and DSMCC_UNMessageSection to JTree
		s = new TreeSet<>(downloadMessages.keySet());

		i = s.iterator();
		while(i.hasNext()){
			final Integer type=i.next();
			final DSMCC_DownLoadDataMessageSection [] sections = downloadMessages.get(type);
			final DefaultMutableTreeNode n = new DefaultMutableTreeNode(new KVP("DSM-CC DownloadMessage ModuleID",type, null));
			for (final DSMCC_DownLoadDataMessageSection tsection : sections) {
				if(tsection!= null){
					addSectionVersionsToJTree(n, tsection, modus);
				}
			}
			t.add(n);

		}


		s = new TreeSet<>(eventStreams.keySet());

		i = s.iterator();
		while(i.hasNext()){
			final Integer type=i.next();
			final DSMCC_StreamDescriptorList [] sections = eventStreams.get(type);
			final DefaultMutableTreeNode n = new DefaultMutableTreeNode(new KVP("DSM-CC Stream Descriptor List",type, null));
			for (final DSMCC_StreamDescriptorList tsection : sections) {
				if(tsection!= null){
					addSectionVersionsToJTree(n, tsection, modus);
				}
			}
			t.add(n);

		}

		return t;
	}

	public DSMCC_UNMessageSection getDSI(){
		// DSI has table extension 0x0 or 0x1
		DSMCC_UNMessageSection [] res = unMessages.get(0);
		if(res==null){
			res=unMessages.get(1);
		}
		if(res!=null){
			return res[0];
		}else{
			return null;
		}
	}

	public DSMCC_UNMessageSection getDII(final int transactionID){
		// DII transaction_id may toggle bit 0, so check for both
		// See ETSI TR 101 202 V1.2.1 P.49
		DSMCC_UNMessageSection [] res = unMessages.get(transactionID);
		if(res==null){
			res = unMessages.get(transactionID^0x1);
		}
		if(res!=null){
			return res[0];
		}else{
			return null;
		}
	}

	public DSMCC_DownLoadDataMessageSection [] getDDM(final int modId){
		return downloadMessages.get(modId);
	}

	public byte[] getDDMbytes(final int modId){
		final DSMCC_DownLoadDataMessageSection [] sections =  downloadMessages.get(modId);
		if(sections==null){
			return null;
		}
		// how many bytes do we need,
		// if one of the sections is null,not complete data, return..
		int len =0;
		for(final DSMCC_DownLoadDataMessageSection s:sections){
			if((s!=null)){
				len+= s.getPayLoadLength();
			}else{
				return null;
			}
		}
		// reserve space
		final byte [] res = new byte[len];
		// copy
		int i = 0;
		for(final DSMCC_DownLoadDataMessageSection s:sections){
			final int thisLen=s.getPayLoadLength();
			System.arraycopy(s.getPayLoad(), 0, res, i, thisLen);
			i+= thisLen;
		}
		return res;

	}

	public byte[] getDDMbytes(ModuleInfo moduleInfo){
		int modId = moduleInfo.getModuleId();
		int version = moduleInfo.getModuleVersion();
		final DSMCC_DownLoadDataMessageSection [] sections =  downloadMessages.get(modId);
		if(sections==null){
			return null;
		}
		// how many bytes do we need, should be same as moduleInfo.getModuleSize()
		// if one of the sections is null,not complete data, return..
		// also check for correct version
		int len =0;
		for(DSMCC_DownLoadDataMessageSection s:sections){
			if(s==null){
				return null;
			}
			while((s!=null)&&(s.getModuleVersion()!=version)){ // start looking for right version
				s = (DSMCC_DownLoadDataMessageSection)s.getNextVersion();
			}
			if((s!=null)&&(s.getModuleVersion()==version)){ // do we have right version?
				len+= s.getPayLoadLength();
			}else{
				return null;
			}
		}
		if(len!=moduleInfo.getModuleSize()){
			return null;
		}
		// reserve space
		final byte [] res = new byte[len];
		// copy
		int i = 0;
		for(DSMCC_DownLoadDataMessageSection s:sections){
			while((s!=null)&&(s.getModuleVersion()!=version)){ // start looking for right version
				s = (DSMCC_DownLoadDataMessageSection)s.getNextVersion();
			}

			if(s!=null){
				final int thisLen=s.getPayLoadLength();
				System.arraycopy(s.getPayLoad(), 0, res, i, thisLen);
				i+= thisLen;
			}else{
				return null;
			}
		}
		return res;

	}

	public int getPid() {
		return pid;
	}

}
