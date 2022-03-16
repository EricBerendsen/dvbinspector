/**
 * 
 * http://www.digitalekabeltelevisie.nl/dvb_inspector
 * 
 * This code is Copyright 2009-2016 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
 * 
 * This file is part of DVB Inspector.
 * 
 * DVB Inspector is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * DVB Inspector is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with DVB Inspector. If not, see
 * <http://www.gnu.org/licenses/>.
 * 
 * The author requests that he be notified of any application, applet, or other binary that makes use of this code, but
 * that's more out of curiosity than anything and is not required.
 * 
 */

package nl.digitalekabeltelevisie.data.mpeg.pes.video26x;

import static nl.digitalekabeltelevisie.util.Utils.indexOf;

import java.util.*;
import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import nl.digitalekabeltelevisie.data.mpeg.PesPacketData;

/**
 * @author Eric
 *
 */
@JsonIgnoreProperties({"pesHandler"})
public abstract class H26xPESDataField<E extends AbstractNALUnit> extends PesPacketData {

	private static final Logger	logger	= Logger.getLogger(H26xPESDataField.class.getName());

	protected abstract E createNALUnitInstance( byte[] data, int i, int j);

	protected List<E> nalUnits = new ArrayList<E>();

	/**
	 * @param pesPacket
	 */
	public H26xPESDataField(final PesPacketData pesPacket) {
		super(pesPacket);
		int i = pesDataStart;
		while((i<(data.length))&&(i>=0)){
			i = indexOf(data, new byte[]{0,0,1},i);
			if(i>=0){ // found start_code_prefix_one_3bytes

				i+=3; // start of NAL unit
				// now look for end, either byte[]{0,0,1} or byte[]{0,0,0} or  TODO end of PES data
				final int zeroEnd = indexOf(data, new byte[]{0,0,0},i);
				final int oneEnd = indexOf(data, new byte[]{0,0,1},i);
				final int end;
				if(zeroEnd>=0){
					if(oneEnd>=0){
						end = Math.min(zeroEnd, oneEnd);
					}else{  // oneEnd not found, use zero Ende
						end = zeroEnd;
					}
				}else if(oneEnd>=0){
					end = oneEnd;
				}else{ // both not found, use pesLen
					end = pesDataLen;
				}
				if(end>i){
					try {
						final E nalUnit = createNALUnitInstance(data,i, end-i);
						nalUnits.add(nalUnit);
					} catch (final Exception e) {
						logger.info("Create NALUnit failed: data.length="+data.length+" i="+i+", end="+end+", exception="+e.getMessage());
					}
					i=end;
				}

			}
		}

	}



	public List<E> getNalUnits() {
		return nalUnits;
	}

}