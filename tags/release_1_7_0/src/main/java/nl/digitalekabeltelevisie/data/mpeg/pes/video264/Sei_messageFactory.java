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

import java.util.ArrayList;
import java.util.List;

import nl.digitalekabeltelevisie.util.BitSource;

/**
 * @author Eric
 *
 */
public class Sei_messageFactory {

	private static List<Sei_message> sei_messages;

	public static List<Sei_message> buildSei_messageList(BitSource bitSource){

		sei_messages = new ArrayList<Sei_message>();
		while((bitSource.available()>=8)&&( bitSource.nextBits(8)!= 0x80)){ // 0x80
			// TODO implement fall back } catch (final RuntimeException iae) {
			// in case there is an error in our code (constructor of a Sei_message), OR the stream is invalid.
			// fall back to a standard Sei_message (this is highly unlikely to fail), so processing can continue
			// requires a unread in BitSource

			if(bitSource.nextBits(8)== 0x4){
				UserDataRegisteredItuT35Sei_message sei_message = new UserDataRegisteredItuT35Sei_message(bitSource);
				sei_messages.add(sei_message);
			}else{
				Sei_message sei_message = new Sei_message(bitSource);
				sei_messages.add(sei_message);
			}
		}

		return sei_messages;
	}

}
