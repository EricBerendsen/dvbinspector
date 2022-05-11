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

package nl.digitalekabeltelevisie.data.mpeg.pes.video26x.sei;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import nl.digitalekabeltelevisie.util.BitSource;

/**
 * @author Eric
 *
 */
public class Sei_messageFactory {

	private static final Logger	logger	= Logger.getLogger(Sei_messageFactory.class.getName());

	private static List<Sei_message> sei_messages;

	public static List<Sei_message> buildSei_messageList(BitSource bitSource){

		sei_messages = new ArrayList<Sei_message>();
		while((bitSource.available()>=8)&&( bitSource.nextBits(8)!= 0x80)){ // 0x80
			// TODO implement fall back } catch (final RuntimeException iae) {
			// in case there is an error in our code (constructor of a Sei_message), OR the stream is invalid.
			// fall back to a standard Sei_message (this is highly unlikely to fail), so processing can continue
			// requires a unread in BitSource
			Sei_message sei_message;
			int nextPayloadType = bitSource.nextBits(8);
			try {
			switch (nextPayloadType) {
				case 0x4:
					sei_message = new UserDataRegisteredItuT35Sei_message(bitSource);
					break;
				case 0x89:
					sei_message = new MasteringDisplayColourVolumeSei_message(bitSource);
					break;
				case 0x90:
					sei_message = new ContentLightLevelInformationSei_message(bitSource);
					break;
				case 0x93:
					sei_message = new AlternativeTransferCharacteristicsSei_message(bitSource);
					break;
				default:
					sei_message = new Sei_message(bitSource);
					break;
				}
			sei_messages.add(sei_message);
			
			} catch (final RuntimeException iae) {
				// this can happen because there is an error in our code (constructor of a sei_message), OR the stream is invalid.
				// No point in using default constructo, bitSource has already been read. 
				// TODO add mark/reset to BitSource.
				
				logger.warning("Fall back for sei_message:" + nextPayloadType + " ("
						+ Sei_message.getPayloadTypeString(nextPayloadType)
						+ "), RuntimeException:"+iae);
			}


		}

		return sei_messages;
	}

}
