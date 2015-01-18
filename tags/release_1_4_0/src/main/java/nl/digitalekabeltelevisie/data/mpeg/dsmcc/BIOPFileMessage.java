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

import static nl.digitalekabeltelevisie.util.Utils.addListJTree;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.util.Utils;

public class BIOPFileMessage extends BIOPMessage {

	public static class ServiceContext implements TreeNode{

		/**
		 * @param context_id
		 * @param context_data_length
		 * @param context_data_byte
		 */
		public ServiceContext(final long context_id, final int context_data_length,
				final byte[] context_data_byte) {
			super();
			this.context_id = context_id;
			this.context_data_length = context_data_length;
			this.context_data_byte = context_data_byte;
		}

		private final long context_id;
		private final int context_data_length;
		private final byte[] context_data_byte;

		public DefaultMutableTreeNode getJTreeNode(final int modus) {
			final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("ServiceContext"));
			t.add(new DefaultMutableTreeNode(new KVP("context_id",context_id,null)));
			t.add(new DefaultMutableTreeNode(new KVP("context_data_length",context_data_length,null)));
			t.add(new DefaultMutableTreeNode(new KVP("context_data_byte",context_data_byte,null)));
			return t;
		}

	}

	protected byte[] objectInfo_data_byte = new byte[0];
	protected int serviceContextList_count;
	private final List<ServiceContext> serviceContextList = new ArrayList<ServiceContext>();
	protected long messageBody_length;
	private byte[] dsmFileContentSize;
	private final long content_length;
	private final int contentStartOffset;
	//	private byte[] content_data_byte;  // no need to make a copy, refer to data[]




	public BIOPFileMessage(final byte[] data, final int offset) {
		super(data, offset);
		if(objectInfo_length>=8){
			dsmFileContentSize=Utils.copyOfRange(data,byte_counter,byte_counter+8);
		}

		if(objectInfo_length>8){
			objectInfo_data_byte = Utils.copyOfRange(data,byte_counter+8,byte_counter+(objectInfo_length-8));
		}
		byte_counter += objectInfo_length;

		serviceContextList_count =  Utils.getInt(data, byte_counter, 1, Utils.MASK_8BITS);
		byte_counter += 1;
		for (int i = 0; i < serviceContextList_count; i++) {
			final long context_id = Utils.getLong(data, byte_counter, 4, Utils.MASK_32BITS);
			byte_counter += 4;
			final int  context_data_length = Utils.getInt(data, byte_counter, 2, Utils.MASK_16BITS);
			byte_counter += 2;
			final byte[] context_data_byte = Utils.copyOfRange(data,byte_counter,byte_counter+context_data_length);
			byte_counter += context_data_length;
			final ServiceContext serviceContext = new ServiceContext(context_id, context_data_length, context_data_byte);
			serviceContextList.add(serviceContext);
		}
		messageBody_length = Utils.getLong(data, byte_counter, 4, Utils.MASK_32BITS);
		byte_counter += 4;
		content_length = Utils.getLong(data, byte_counter, 4, Utils.MASK_32BITS);
		byte_counter += 4;
		contentStartOffset =byte_counter;


	}


	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus, final String label) {
		final DefaultMutableTreeNode t = super.getJTreeNode(modus,label);
		t.add(new DefaultMutableTreeNode(new KVP("dsmFileContentSize",dsmFileContentSize ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("objectInfo_data_byte",objectInfo_data_byte ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("serviceContextList_count",serviceContextList_count ,null)));
		addListJTree(t,serviceContextList,modus,"ServiceContextList");
		t.add(new DefaultMutableTreeNode(new KVP("messageBody_length",messageBody_length ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("content_length",content_length ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("content_data_byte",data, contentStartOffset, (int)content_length ,null)));



		return t;
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		return getJTreeNode(modus,"");
	}



	public byte[] getObjectInfo_data_byte() {
		return objectInfo_data_byte;
	}


	public int getServiceContextList_count() {
		return serviceContextList_count;
	}


	public List<ServiceContext> getServiceContextList() {
		return serviceContextList;
	}


	public long getMessageBody_length() {
		return messageBody_length;
	}


	public byte[] getDsmFileContentSize() {
		return dsmFileContentSize;
	}


	public long getContent_length() {
		return content_length;
	}


	public int getContentStartOffset() {
		return contentStartOffset;
	}



}
