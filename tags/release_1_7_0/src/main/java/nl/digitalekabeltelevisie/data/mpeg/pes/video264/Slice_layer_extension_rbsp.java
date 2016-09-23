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

package nl.digitalekabeltelevisie.data.mpeg.pes.video264;

import java.util.logging.Logger;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.pes.video26x.RBSP;
import nl.digitalekabeltelevisie.gui.utils.GuiUtils;

/**
 * @author Eric
 *
 */
public class Slice_layer_extension_rbsp extends RBSP {

	private static final Logger	logger	= Logger.getLogger(Slice_layer_extension_rbsp.class.getName());
	
	private int svc_extension_flag;
	private int avc_3d_extension_flag;

	private Slice_header slice_header;

	protected Slice_layer_extension_rbsp(byte[] rbsp_bytes,
			int numBytesInRBSP, int svc_extension_flag, int avc_3d_extension_flag) {
		super(rbsp_bytes, numBytesInRBSP);
		this.svc_extension_flag = svc_extension_flag;
		this.avc_3d_extension_flag = avc_3d_extension_flag;
		
		if( svc_extension_flag == 1 ) {
			logger.warning("svc_extension_flag == 1 not implemented");
			
//			slice_header_in_scalable_extension( ) /* specified in Annex G */
//			if( slice_skip_flag !=1 ){
//				slice_data_in_scalable_extension( ) /* specified in Annex G */
//			}
		} else if( avc_3d_extension_flag == 1 ) {
			logger.warning("avc_3d_extension_flag == 1 not implemented");
//			slice_header_in_3davc_extension( ) /* specified in Annex J */
//			slice_data_in_3davc_extension( ) /* specified in Annex J */
		} else {
			slice_header = new Slice_header(bitSource);
		}
	}

	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.controller.TreeNode#getJTreeNode(int)
	 */
	@Override
	public DefaultMutableTreeNode getJTreeNode(int modus) {
		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("slice_layer_extension_rbsp"));
		
		if( svc_extension_flag == 1 ) {
			t.add(new DefaultMutableTreeNode(GuiUtils.getNotImplementedKVP("svc_extension_flag == 1")));
//			slice_header_in_scalable_extension( ) /* specified in Annex G */
//			if( slice_skip_flag !=1 ){
//				slice_data_in_scalable_extension( ) /* specified in Annex G */
//			}
		} else if( avc_3d_extension_flag == 1 ) {
			//logger.warning("avc_3d_extension_flag == 1 not implemented");
			t.add(new DefaultMutableTreeNode(GuiUtils.getNotImplementedKVP("avc_3d_extension_flag == 1")));
//			slice_header_in_3davc_extension( ) /* specified in Annex J */
//			slice_data_in_3davc_extension( ) /* specified in Annex J */
		} else {
			t.add(slice_header.getJTreeNode(modus));
		}

		
		return t;
	}

	public Slice_header getSlice_header() {
		return slice_header;
	}

}
