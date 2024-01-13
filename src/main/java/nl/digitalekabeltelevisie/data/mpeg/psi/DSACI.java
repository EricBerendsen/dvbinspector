package nl.digitalekabeltelevisie.data.mpeg.psi;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

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

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.commons.lang3.ArrayUtils;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.PSI;

//based on EN 303 560 V1.1.1 (2018-05) 

public class DSACI extends AbstractPSITabel {

	private final Map<Integer, DSACISection []> dfits = new TreeMap<>();

	private int pid = 0;

	protected DSACI(PSI parentPSI) {
		super(parentPSI);
	}

	public void update(DSACISection section) {
		pid=section.getParentPID().getPid();
		
		final int fontId = section.getCurrent_DSA_group_id();
		DSACISection [] sections= dfits.computeIfAbsent(fontId, f -> new DSACISection[section.getSectionLastNumber()+1]);

		if(sections[section.getSectionNumber()]==null){
			sections[section.getSectionNumber()] = section;
		}else{
			final TableSection last = sections[section.getSectionNumber()];
			updateSectionVersion(section, last);
		}

	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(int modus) {
		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("DSACI PID="+pid ));
		
		for(Entry<Integer, DSACISection[]> dfit:dfits.entrySet()) {
			
			//byte[] dsaci_file_data = new byte[0];
			
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			
			final int current_DSA_group_id= dfit.getKey();
			final DSACISection [] sections = dfit.getValue();
			final DefaultMutableTreeNode n = new DefaultMutableTreeNode(new KVP("DSACI, current_DSA_group_id",current_DSA_group_id, null));
			for (final DSACISection tsection : sections) {
				if(tsection!= null){
					addSectionVersionsToJTree(n, tsection, modus);
				}
				
				try {
					outputStream.write( tsection.getPrivateData());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			byte[] dsaci_file_data = outputStream.toByteArray();
			
			
			n.add(new DefaultMutableTreeNode(new KVP("dsaci_file_data",dsaci_file_data,null)));
//			
//			Inflater decompresser = new Inflater();
//			decompresser.setInput(dsaci_file_data);
		//	decompresser.inflate
			
			
			Inflater inflater = new Inflater();
		    inflater.setInput(dsaci_file_data,0,dsaci_file_data.length -4);
			try {

		    ByteArrayOutputStream outputStream2 = new ByteArrayOutputStream();
		    byte[] buffer = new byte[1024];

		    while (!inflater.finished()) {
		        int decompressedSize;
					decompressedSize = inflater.inflate(buffer);
		        outputStream2.write(buffer, 0, decompressedSize);
		    }

		    byte [] uncompressed =  outputStream2.toByteArray();
		    
		    n.add(new DefaultMutableTreeNode(new KVP("uncompressed",uncompressed,null)));
			} catch (DataFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			t.add(n);
		}

		return t;
	}


}
