/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2012 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.PSI;
import nl.digitalekabeltelevisie.data.mpeg.psi.AbstractPSITabel;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

/**
 * Contains all DSM-CC information from single PID. Make a separation by TableID (all other sections contain just one table ID, this can have 0x38 - 0x3f)
 *
 * single PID may be shared by multiple carousels
 *
 * table_id: 0x3B (59) => DSM-CC - U-N messages (DSI or DII) (first implemented
 * table_id: 0x3C (60) => DSM-CC - Download Data Messages (DDB) (not implemented yet)
 *
 * @author Eric Berendsen
 *
 */
public class DSMCC extends AbstractPSITabel{

	private Map<Integer, DSMCC_UNMessageSection []> unMessages = new HashMap<Integer, DSMCC_UNMessageSection []>();
	private Map<Integer, DSMCC_DownLoadDataMessageSection []> downloadMessages = new HashMap<Integer, DSMCC_DownLoadDataMessageSection []>();
	private Map<Integer, TableSection[]> eventStreams = new HashMap<Integer, TableSection[]>();
	private int pid = 0;
	private boolean isObjectCarousel = true; // set to false for SSU

	public DSMCC(final PSI parent, final boolean isObjectCarousel){
		super(parent);
		this.isObjectCarousel = isObjectCarousel;

	}

	public void update(final TableSection section){
		count++;
		pid=section.getParentPID().getPid();

		if(section.isCrc_error()){
			return;
		}

		final int tableID  = section.getTableId();
		if(tableID==0x3b){ // DSM-CC - U-N messages (DSI or DII)
			// TODO DSI or DII can be only one single section, so no need for []
			final int tableIdExt = section.getTableIdExtension();
			final DSMCC_UNMessageSection unMessage = new DSMCC_UNMessageSection(section.getRaw_data(), section.getParentPID(),isObjectCarousel);
			DSMCC_UNMessageSection [] sections= unMessages.get(tableIdExt);

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
			DSMCC_DownLoadDataMessageSection [] sections= downloadMessages.get(moduleID);

			if(sections==null){
				sections = new DSMCC_DownLoadDataMessageSection[section.getSectionLastNumber()+1];
				downloadMessages.put(moduleID, sections);
			}
			if(section.getSectionNumber()<=section.getSectionLastNumber()){ // this always be the case, but Ziggo managed to break this rule...
				if(sections[section.getSectionNumber()]==null){
					sections[section.getSectionNumber()] = downloadMessage;
				}else{
					final TableSection last = sections[section.getSectionNumber()];
					updateSectionVersion(downloadMessage, last);
				}
			}
		}else if(tableID==0x3d){ // DSM-CC - stream descriptorlist;

			// TODO make wrapper Stream DescriptorList , if we can find specs..
			final int eventID = section.getTableIdExtension();
			TableSection [] sections= eventStreams.get(eventID);

			if(sections==null){
				sections = new TableSection[section.getSectionLastNumber()+1];
				eventStreams.put(eventID, sections);
			}
			if(sections[section.getSectionNumber()]==null){
				sections[section.getSectionNumber()] = section;
			}else{
				final TableSection last = sections[section.getSectionNumber()];
				updateSectionVersion(section, last);
			}

		}

	}

	public DefaultMutableTreeNode getJTreeNode(final int modus) {

		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("DSM-CC PID="+pid ));
		TreeSet<Integer> s = new TreeSet<Integer>(unMessages.keySet());

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
		s = new TreeSet<Integer>(downloadMessages.keySet());

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


		s = new TreeSet<Integer>(eventStreams.keySet());

		i = s.iterator();
		while(i.hasNext()){
			final Integer type=i.next();
			final TableSection [] sections = eventStreams.get(type);
			final DefaultMutableTreeNode n = new DefaultMutableTreeNode(new KVP("DSM-CC Stream Descriptor List",type, null));
			for (final TableSection tsection : sections) {
				if(tsection!= null){
					addSectionVersionsToJTree(n, tsection, modus);
				}
			}
			t.add(n);

		}

		return t;
	}

	public DSMCC_UNMessageSection getDSI(){
		// DIS has table extension 0x0 or 0x1
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
		// DIS has table extension 0x0 or 0x1
		final DSMCC_UNMessageSection [] res = unMessages.get(transactionID);
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
		// how many bytes do we need
		int len =0;
		for(final DSMCC_DownLoadDataMessageSection s:sections){
			if(s!=null){
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

}
