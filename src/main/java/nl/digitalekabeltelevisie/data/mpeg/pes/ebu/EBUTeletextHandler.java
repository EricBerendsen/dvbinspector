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
package nl.digitalekabeltelevisie.data.mpeg.pes.ebu;

import static nl.digitalekabeltelevisie.util.Utils.addListJTree;
import static nl.digitalekabeltelevisie.util.Utils.invtab;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JMenuItem;
import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.PesPacketData;
import nl.digitalekabeltelevisie.data.mpeg.pes.GeneralPesHandler;
import nl.digitalekabeltelevisie.gui.DVBtree;
import nl.digitalekabeltelevisie.gui.SaveAble;

/**
 * @author Eric Berendsen
 *
 * based on EN 300 706
 *
 */
public class EBUTeletextHandler extends GeneralPesHandler implements SaveAble{

	private static final Logger logger = Logger.getLogger(EBUTeletextHandler.class.getName());


	private TxtService txtService = null;
	private List<VPSDataField> vps = null;
	private List<WSSDataField> wss = null;

	public static String getObjectTypeString(final int objectType) {

		switch (objectType) {
		case 0x00: return "No default object required";
		case 0x01: return "Active";
		case 0x02: return "Adaptive";
		case 0x03: return "Passive";
		default: return "illegal value";
		}
	}

	public static String getObjectSourceString(final int objectSource) {

		switch (objectSource) {
		case 0x00: return "Illegal";
		case 0x01: return "Local";
		case 0x02: return "POP";
		case 0x03: return "GPOP";
		default: return "illegal value";
		}
	}

	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.controller.TreeNode#getJTreeNode(int)
	 */
	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final KVP kvp = new KVP("EBU PES Data");
		final DefaultMutableTreeNode s=new DefaultMutableTreeNode(kvp);
		
		final JMenuItem objectMenu = new JMenuItem("Save Txt Service as .t42");
		objectMenu.setActionCommand(DVBtree.T42);
		kvp.setSubMenuAndOwner(objectMenu, this);

		addListJTree(s,pesPackets,modus,"PES Packets");

		final DefaultMutableTreeNode t=new DefaultMutableTreeNode(new KVP("EBU Data"));
		if(txtService!=null){ t.add(txtService.getJTreeNode(modus));}
		if(vps!=null){
			addListJTree(t,vps,modus,"VPS");
		}

		if(wss!=null){
			addListJTree(t,wss,modus,"WSS");
		}
		s.add(t);
		return s;
	}

	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.data.mpeg.pes.GeneralPesHandler#porcessPesDataBytes(int, byte[], int, int)
	 */
	@Override
	protected void processPesDataBytes(final PesPacketData pesData) {

		final EBUPESDataField pesDataField = new EBUPESDataField(pesData);
		pesPackets.add(pesDataField);
		final List<EBUDataField> lines = pesDataField.getFieldList();
		for(final EBUDataField ebuData: lines){
			if(ebuData instanceof TxtDataField txtDataField) {
				if(txtService==null){
					txtService = new TxtService(getTransportStream());
				}
				txtService.addTxtDataField(txtDataField);
			}else if(ebuData instanceof VPSDataField vpsDataField){
				if(vps==null){
					vps = new ArrayList<>();
				}
				add(vpsDataField, vps);
			}else if(ebuData instanceof WSSDataField wssDataField){
				if(wss==null){
					wss = new ArrayList<>();
				}
				add(wssDataField, wss);
			}
		}
	}

	/**
	 * @param ebuData
	 * @param list
	 */
	protected static <T extends EBUDataField> void  add(final T ebuData, final List<T> v) {

		final int i = v.indexOf(ebuData);
		if(i==-1){ // not found
			v.add(ebuData);
		}else{
			v.get(i).incCount(1); // increase occurence count
		}
	}

	public TxtService getTxtService() {
		return txtService;
	}

	public List<VPSDataField> getVps() {
		return vps;
	}

	public List<WSSDataField> getWss() {
		return wss;
	}

	@Override
	public void save(File file) {
		
		try (FileOutputStream out = new FileOutputStream(file)) {
			for(PesPacketData packet:pesPackets) {
				if(packet instanceof EBUPESDataField ebuDataField) {
					for(EBUDataField field:ebuDataField.getFieldList()) {
						saveLineT42(out, field);
					}
				}
			}
		} catch (IOException e) {
			logger.log(Level.WARNING, "could not write file", e);
		}

		
	}

	public static void saveLineT42(FileOutputStream out, EBUDataField line) throws IOException {
		if (line != null) {
			byte[] d = line.getData_block();
	
			int offset = line.getOffset();
			for (int i = 4; i < 46; i++) {
				out.write(invtab[Byte.toUnsignedInt(d[offset + i])]);
			}
		}
	}



}
