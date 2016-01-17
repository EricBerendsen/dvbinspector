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

package nl.digitalekabeltelevisie.data.mpeg.pes.video265;



import static nl.digitalekabeltelevisie.util.Utils.addListJTree;

import java.util.Iterator;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.PesPacketData;
import nl.digitalekabeltelevisie.data.mpeg.pes.GeneralPesHandler;

/**
 * @author Eric Berendsen
 *
 */
public class H265Handler extends GeneralPesHandler{


	/**
	 * Meta Iterator to iterate over all NALUnits in this PES stream, regardless of grouping in PES Packets
	 *
	 * In general this does not work for streams with no alignment, So every NALUnit should be contained in a PES packet.
	 *
	 * @author Eric
	 *
	 */
	private class NALUnitIterator{

		Iterator<PesPacketData> pesIterator = null;
		H265NALUnit nextSection = null;
		private Iterator<H265NALUnit> sectionIter;

		public NALUnitIterator() {
			pesIterator = pesPackets.iterator();
			sectionIter = getNextSectionIter();
			if(sectionIter!=null){
				nextSection = sectionIter.next();
			}
		}

		private Iterator<H265NALUnit> getNextSectionIter(){

			Iterator<H265NALUnit> result = null;
			do {
				final Video265PESDataField pesPacket = (Video265PESDataField )pesIterator.next();
				result = pesPacket.getNalUnits().iterator();

			} while (((result==null)||!result.hasNext())&&(pesIterator.hasNext()));
			return result;

		}

		public H265NALUnit next() {
			final H265NALUnit result = nextSection;
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
	public void processPesDataBytes(final PesPacketData pesData){
		pesPackets.add(new Video265PESDataField(pesData));

	}



	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.data.mpeg.pes.GeneralPesHandler#getJTreeNode(int)
	 */
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode s=new DefaultMutableTreeNode(new KVP("PES Data"));
		addListJTree(s,pesPackets,modus,"PES Packets");

		return s;
	}




}
