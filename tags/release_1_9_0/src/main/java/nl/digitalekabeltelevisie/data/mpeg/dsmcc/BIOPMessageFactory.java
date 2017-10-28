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
import java.util.List;

import nl.digitalekabeltelevisie.util.Utils;


final public class BIOPMessageFactory {


	/**
	 * only static helpers
	 */
	private BIOPMessageFactory() {
	}

	public static List<BIOPMessage> createBIOPMessages(final byte[] data, final int offset) {
		final List<BIOPMessage> messages = new ArrayList<BIOPMessage>();
		int l = offset;
		while(l<data.length){

			BIOPMessage message = new BIOPMessage(data, l);
			final byte[] objectKind= message.getObjectKind_data();

			if(Utils.equals(objectKind, 0, objectKind.length,IOR.TYPE_DIRECTORY,0,IOR.TYPE_DIRECTORY.length)){
				message =  new BIOPDirectoryMessage(data, l);
			}else if(Utils.equals(objectKind, 0, objectKind.length,IOR.TYPE_FILE,0,IOR.TYPE_FILE.length)){
				message =  new BIOPFileMessage(data, l);
			}else if(Utils.equals(objectKind, 0, objectKind.length,IOR.TYPE_SERVICEGATEWAY,0,IOR.TYPE_SERVICEGATEWAY.length)){
				message =  new BIOPDirectoryMessage(data, l);  //BIOP::ServiceGateway  is a Directory
			}else if(Utils.equals(objectKind, 0, objectKind.length,IOR.TYPE_STREAMEVENT,0,IOR.TYPE_STREAMEVENT.length)){
				message =  new BIOPStreamEventMessage(data, l); // DSM::StreamEvent
			}else{  // only stream not implemented yet
				message = new BIOPMessage(data, l);
			}
			messages.add(message);
			l += message.getMessage_size()+12;
		}
		return messages;
	}

}
