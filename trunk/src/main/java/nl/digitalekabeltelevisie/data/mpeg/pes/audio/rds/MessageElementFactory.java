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

package nl.digitalekabeltelevisie.data.mpeg.pes.audio.rds;

import java.util.logging.Logger;

import nl.digitalekabeltelevisie.util.Utils;

public final class MessageElementFactory {

	/**
	 *
	 */
	private MessageElementFactory() {
		// static methods only
	}

	private static final Logger	logger	= Logger.getLogger(MessageElementFactory.class.getName());

	public static MessageElement createMessageElement(final byte[] data, final int start){

		final int messageElementCode = Utils.getInt(data, start, 1, Utils.MASK_8BITS);
		if(messageElementCode==1){
			return new MessageElementPI(data, start);
		}else if(messageElementCode==2){
			return new MessageElementPS(data, start);
		}else if(messageElementCode==3){
			return new MessageElementTATP(data, start);
		}else if(messageElementCode==7){
			return new MessageElementPTY(data, start);
		}else if(messageElementCode==0x0A){
			return new MessageElementRT(data, start);
		}else if(messageElementCode==0x0D){
			return new MessageElementRealTimeClock(data, start);
		}else if(messageElementCode==0x13){
			return new MessageElementAF(data, start);
		}else if(messageElementCode==0x19){
			return new MessageElementCTOnOff(data, start);
		}else if(messageElementCode==0x1C){
			return new MessageElementDataSetSelect(data, start);
		}else if(messageElementCode==0x24){
			return new MessageElementFreeFormatGroup(data, start);
		}else if(messageElementCode==0x25){
			return new MessageElementIH(data, start);
		}else if(messageElementCode==0x30){
			return new MessageElementTMC(data, start);
		}else if(messageElementCode==0x40){
			return new MessageElementODAConfigurationAndShortMessageCommand(data, start);
		}else if(messageElementCode==0x42){
			return new MessageElementODAFreeFormatGroup(data, start);


		}else if(messageElementCode==0x46){
			return new MessageElementODADataCommand(data, start);


		}else{
			logger.info("No implementation for MessageElement code:"+messageElementCode+", type: "+MessageElement.getMessageElementCodeString(messageElementCode));
			return new MessageElement(data, start);
		}
	}

}
