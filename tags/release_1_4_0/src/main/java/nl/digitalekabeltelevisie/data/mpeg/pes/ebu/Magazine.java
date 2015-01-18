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

package nl.digitalekabeltelevisie.data.mpeg.pes.ebu;


import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;

public class Magazine implements TreeNode{
	/**
	 * 
	 */
	private final TxtService txtServiceHandler; // the containing text service


	private int magazineNo;
	private int currentPageNo=-1;

	private final TxtDataField[] pageEnhanceMentDataPackes = new TxtDataField[16]; // lines

	private Page currentPage;
	private final Page[] pageList = new Page[256];
	/**
	 * @param mag
	 * @param service
	 */
	public Magazine(final TxtService service, final int mag) {
		txtServiceHandler = service;
		magazineNo = mag;
	}
	/**
	 * @param txtDataField
	 */
	public void addTxtDataField(final TxtDataField txtDataField) {
		if(txtDataField.getPacketNo()==0){
			currentPageNo = txtDataField.getPageNumber();
			currentPage = pageList[currentPageNo];
			if(currentPage==null){
				currentPage = new Page(this, currentPageNo);
				pageList[currentPageNo]=currentPage;
			}
			currentPage.setHeader(txtDataField);
		}else if(txtDataField.getPacketNo()==29){ // 9.5 Magazine-Related Page Enhancement Data Packets
			final int designationCode = txtDataField.getDesignationCode();
			pageEnhanceMentDataPackes[designationCode]= txtDataField;
		}else if(currentPage!=null){
			currentPage.addLine(txtDataField);
		}

	}


	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.controller.TreeNode#getJTreeNode(int)
	 */
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode s=new DefaultMutableTreeNode(new KVP("Magazine "+magazineNo));
		for(final TxtDataField txtDatafield: pageEnhanceMentDataPackes){
			if(txtDatafield!=null){
				s.add(txtDatafield.getJTreeNode(modus));
			}
		}

		for (final Page p: pageList) {
			if(p!=null){
				s.add(p.getJTreeNode(modus));
			}
		}
		return s;
	}

	public Page getPage(final int n){
		return pageList[n];
	}

	/**
	 * @return the magazineNo
	 */
	public int getMagazineNo() {
		return magazineNo;
	}

	/**
	 * @return the pageEnhanceMentDataPackes
	 */
	public TxtDataField[] getPageEnhanceMentDataPackes() {
		return pageEnhanceMentDataPackes;
	}

	public TxtDataField getPageEnhanceMentDataPackes(final int destinationCode) {
		return pageEnhanceMentDataPackes[destinationCode];
	}

	/**
	 * @return the txtServiceHandler
	 */
	public TxtService getTxtService() {
		return txtServiceHandler;
	}
}