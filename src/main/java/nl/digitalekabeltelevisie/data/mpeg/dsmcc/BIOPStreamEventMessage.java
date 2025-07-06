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

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.copyOfRange;
import static nl.digitalekabeltelevisie.util.Utils.*;

public class BIOPStreamEventMessage extends BIOPMessage {

	public static class Binding implements TreeNode{

		private final BIOPName biopName;
		private final int bindingType;
		private final IOR ior;
		private final int objectInfo_length;
		private final byte[] objectInfo_data_byte;

		private int len = 0;

		public Binding(byte[] data, int offset) {
			biopName = new BIOPName(data, offset);
			len=biopName.getLen();

			bindingType  = getInt(data, offset+len, 1, MASK_8BITS);
			len+=1;
			ior = new IOR(data, offset+len);
			len+=ior.getLength();
			objectInfo_length  = getInt(data, offset+len, 2, MASK_16BITS);
			len+=2;
			objectInfo_data_byte = copyOfRange(data, offset + len, offset + len + objectInfo_length);
			len+=objectInfo_length;
		}

		public KVP getJTreeNode(int modus) {
			KVP t = new KVP("Binding",biopName.getName());
			t.add(biopName.getJTreeNode(modus));
			t.add(new KVP("bindingType",bindingType ,getBindingTypeString(bindingType)));
			t.add(ior.getJTreeNode(modus));

			return t;
		}



		public int length(){
			return len;
		}

		public BIOPName getBiopName() {
			return biopName;
		}

		public int getBindingType() {
			return bindingType;
		}

		public IOR getIor() {
			return ior;
		}

		public int getObjectInfo_length() {
			return objectInfo_length;
		}

		public byte[] getObjectInfo_data_byte() {
			return objectInfo_data_byte;
		}

		public int getLen() {
			return len;
		}

	}

	public record EventName(int eventName_length, byte[] eventName_data_byte) implements TreeNode {


			public KVP getJTreeNode(int modus) {
				KVP t =new KVP("EventName");
				t.add(new KVP("eventName_length", eventName_length));
				t.add(new KVP("eventName_data_byte", eventName_data_byte));
				return t;
			}


	}

	public record EventId(int eventId) implements TreeNode {


		public KVP getJTreeNode(int modus) {
				return new KVP("eventId", eventId);
			}

		}


	public record ServiceContext(long context_id, int context_data_length,
								 byte[] context_data_byte) implements TreeNode {


		public KVP getJTreeNode(int modus) {
				KVP t = new KVP("ServiceContext");
				t.add(new KVP("context_id", context_id));
				t.add(new KVP("context_data_length", context_data_length));
				t.add(new KVP("context_data_byte", context_data_byte));
				return t;
			}

		}

	protected byte[] objectInfo_data_byte;
	protected int serviceContextList_count;
	private final List<ServiceContext> serviceContextList = new ArrayList<>();
	protected long messageBody_length;
	protected int bindings_count;
	private final List<Binding> bindingList = new ArrayList<>();
	private final int aDescription_length;
	private final byte[] aDescription_bytes;
	private final long duration_aSeconds;
	private final long duration_aMicroSeconds; // ETSI TR 101 202 V1.2.1 (2003-01) says 16 bits, appears to be 32. Makes more sense, 1000000 microsSeconds does not fit in 16 bits
	private final int audio;
	private final int video;
	private final int data1;
	private final int eventNames_count;
	private final List<EventName> eventNames = new ArrayList<>();
	private final int taps_count;
	private final List<Tap> taps= new ArrayList<>();
	private final int eventIds_count;

	private final List<EventId> eventIds = new ArrayList<>();


	public BIOPStreamEventMessage(byte[] dataBytes, int offset) {
		super(dataBytes, offset);
		int objectInfoStart = byte_counter;


		aDescription_length =  getInt(dataBytes, byte_counter, 1, MASK_8BITS);
		byte_counter += 1;
		aDescription_bytes = copyOfRange(dataBytes, byte_counter, byte_counter + aDescription_length);
		byte_counter += aDescription_length;

		duration_aSeconds = getLong(dataBytes, byte_counter, 4, MASK_32BITS);
		byte_counter += 4;
		duration_aMicroSeconds = getLong(dataBytes, byte_counter, 4, MASK_32BITS);
		byte_counter += 4;
		audio =  getInt(dataBytes, byte_counter, 1, MASK_8BITS);
		byte_counter += 1;
		video =  getInt(dataBytes, byte_counter, 1, MASK_8BITS);
		byte_counter += 1;
		data1 =  getInt(dataBytes, byte_counter, 1, MASK_8BITS);
		byte_counter += 1;

		eventNames_count = getInt(dataBytes, byte_counter, 2, MASK_16BITS);
		byte_counter += 2;


		for (int i = 0; i < eventNames_count; i++) {
			int  eventName_length = getInt(dataBytes, byte_counter, 1, MASK_8BITS);
			byte_counter += 1;
			byte[] eventName_data_byte = copyOfRange(dataBytes,byte_counter,byte_counter+eventName_length);
			byte_counter += eventName_length;
			EventName eventName = new EventName(eventName_length,eventName_data_byte);
			eventNames.add(eventName);
		}

		objectInfo_data_byte = copyOfRange(dataBytes,byte_counter,objectInfoStart +objectInfo_length);
		byte_counter = objectInfoStart +objectInfo_length;


		serviceContextList_count =  getInt(data, byte_counter, 1, MASK_8BITS);
		byte_counter += 1;
		for (int i = 0; i < serviceContextList_count; i++) {
			long context_id = getLong(data, byte_counter, 4, MASK_32BITS);
			byte_counter += 4;
			int  context_data_length = getInt(data, byte_counter, 2, MASK_16BITS);
			byte_counter += 2;
			byte[] context_data_byte = copyOfRange(data,byte_counter,byte_counter+context_data_length);
			byte_counter += context_data_length;
			ServiceContext serviceContext = new ServiceContext(context_id, context_data_length, context_data_byte);
			serviceContextList.add(serviceContext);
		}
		messageBody_length = getLong(data, byte_counter, 4, MASK_32BITS);
		byte_counter += 4;

		taps_count= getInt(data, byte_counter, 1, MASK_8BITS);
		byte_counter += 1;


		for (int i = 0; i < taps_count; i++) {
			Tap tap =new Tap(data,byte_counter);
			taps.add(tap);
			byte_counter+= tap.getSelector_length()+7;

		}

		eventIds_count = getInt(data, byte_counter, 1, MASK_8BITS);
		byte_counter += 1;
		for (int i = 0; i < eventIds_count; i++) {
			EventId eventId = new EventId(getInt(data, byte_counter, 2, MASK_16BITS));
			eventIds.add(eventId);
			byte_counter +=2;
		}
	}


	@Override
	public KVP getJTreeNode(int modus, String label) {
		KVP t = super.getJTreeNode(modus,label);

		t.add(new KVP("aDescription_length",aDescription_length));
		t.add(new KVP("aDescription_bytes",aDescription_bytes));
		t.add(new KVP("duration.aSeconds",duration_aSeconds));
		t.add(new KVP("duration.aMicroSeconds",duration_aMicroSeconds));
		t.add(new KVP("audio",audio));
		t.add(new KVP("video",video));
		t.add(new KVP("data",data1));
		t.add(new KVP("eventNames_count",eventNames_count));

		addListJTree(t,eventNames,modus,"EventNames");
		t.add(new KVP("objectInfo_data_byte",objectInfo_data_byte));
		t.add(new KVP("serviceContextList_count",serviceContextList_count));
		addListJTree(t,serviceContextList,modus,"ServiceContextList");
		t.add(new KVP("messageBody_length",messageBody_length));
		addListJTree(t,taps,modus,"BIOP::Taps");

		t.add(new KVP("eventIds_count",eventIds_count));
		addListJTree(t,eventIds,modus,"EventIds");

		return t;
	}

	@Override
	public KVP getJTreeNode(int modus) {
		return getJTreeNode(modus,"");
	}

	private static String getBindingTypeString(int bindingType) {

		if(bindingType==0x1){
			return "nobject";
		}else if(bindingType==0x2){
			return "ncontext";
		}
		return null;
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


	public int getBindings_count() {
		return bindings_count;
	}


	public List<Binding> getBindingList() {
		return bindingList;
	}


	public long getDuration_aSeconds() {
		return duration_aSeconds;
	}


	public long getDuration_aMicroSeconds() {
		return duration_aMicroSeconds;
	}


	public int getAudio() {
		return audio;
	}


	public int getVideo() {
		return video;
	}


	public int getData1() {
		return data1;
	}


	public int getEventNames_count() {
		return eventNames_count;
	}


	public List<EventName> getEventNames() {
		return eventNames;
	}


	public int getTaps_count() {
		return taps_count;
	}


	public List<Tap> getTaps() {
		return taps;
	}


	public int getEventIds_count() {
		return eventIds_count;
	}


	public List<EventId> getEventIds() {
		return eventIds;
	}
}
