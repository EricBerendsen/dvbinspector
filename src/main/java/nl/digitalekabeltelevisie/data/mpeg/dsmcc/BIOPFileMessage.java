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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;

import static nl.digitalekabeltelevisie.util.Utils.*;

/**
 * see ETSI TS 102 809 V1.3.1 (2017-06) Table B.16: BIOP::FileMessage syntax
 *
 */
public class BIOPFileMessage extends BIOPMessage {

	public static class ServiceContext implements TreeNode{

		/**
		 * @param context_id
		 * @param context_data_length
		 * @param context_data_byte
		 */
		public ServiceContext(long context_id, int context_data_length,
                              byte[] context_data_byte) {
            this.context_id = context_id;
			this.context_data_length = context_data_length;
			this.context_data_byte = context_data_byte;
		}

		private final long context_id;
		private final int context_data_length;
		private final byte[] context_data_byte;

		public KVP getJTreeNode(int modus) {
			KVP t = new KVP("ServiceContext");
			t.add(new KVP("context_id",context_id));
			t.add(new KVP("context_data_length",context_data_length));
			t.add(new KVP("context_data_byte",context_data_byte));
			return t;
		}

	}

	protected byte[] objectInfo_data_byte = new byte[0];
	protected int serviceContextList_count;
	private final List<ServiceContext> serviceContextList = new ArrayList<>();
	protected long messageBody_length;
	private byte[] dsmFileContentSize;
	private final long content_length;
	private final int contentStartOffset;

	public BIOPFileMessage(byte[] data, int offset) {
		super(data, offset);
		if(objectInfo_length>=8){
			dsmFileContentSize= Arrays.copyOfRange(data, byte_counter, byte_counter + 8);
		}

		if(objectInfo_length>8){
			objectInfo_data_byte = Arrays.copyOfRange(data, byte_counter + 8, byte_counter + objectInfo_length);
		}
		byte_counter += objectInfo_length;

		serviceContextList_count =  getInt(data, byte_counter, 1, MASK_8BITS);
		byte_counter += 1;
		for (int i = 0; i < serviceContextList_count; i++) {
			long context_id = getLong(data, byte_counter, 4, MASK_32BITS);
			byte_counter += 4;
			int  context_data_length = getInt(data, byte_counter, 2, MASK_16BITS);
			byte_counter += 2;
			byte[] context_data_byte = Arrays.copyOfRange(data, byte_counter, byte_counter + context_data_length);
			byte_counter += context_data_length;
			ServiceContext serviceContext = new ServiceContext(context_id, context_data_length, context_data_byte);
			serviceContextList.add(serviceContext);
		}
		messageBody_length = getLong(data, byte_counter, 4, MASK_32BITS);
		byte_counter += 4;
		content_length = getLong(data, byte_counter, 4, MASK_32BITS);
		byte_counter += 4;
		contentStartOffset =byte_counter;


	}


	@Override
	public KVP getJTreeNode(int modus, String label) {
		KVP t = super.getJTreeNode(modus,label);
		t.add(new KVP("dsmFileContentSize",dsmFileContentSize));
		t.add(new KVP("objectInfo_data_byte",objectInfo_data_byte));
		t.add(new KVP("serviceContextList_count",serviceContextList_count));
		addListJTree(t,serviceContextList,modus,"ServiceContextList");
		t.add(new KVP("messageBody_length",messageBody_length));
		t.add(new KVP("content_length",content_length));
		t.add(new KVP("content_data_byte",data, contentStartOffset, (int)content_length));



		return t;
	}

	@Override
	public KVP getJTreeNode(int modus) {
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
