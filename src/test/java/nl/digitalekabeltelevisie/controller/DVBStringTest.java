/**
 * 
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 * 
 *  This code is Copyright 2009-2015 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.controller;

import org.junit.Before;
import org.junit.Test;
/**
 * @author Eric
 *
 */
public class DVBStringTest {

	/**
	 * 
	 */
	private static final String DVB_INSPECTOR = "DVB Inspector";
	DVBString dvbString;
	DVBString extendedDvbString;
	DVBString explicitLengthDvbString;
	final byte []rawString = {0x5,0x44,13,0x44,0x56,0x42,0x20,0x49,0x6e,0x73,0x70,0x65,0x63,0x74,0x6f,0x72,0x21,0x55};
	final byte []rawStringExtendedAsci = {0x5,0x44,5,-92,-68,-67,-66,-45};
	final byte []rawStringWithoutLength = {0x54, 0x65, 0x73, 0x74};

	@Before
	public void setUp(){
		dvbString = new DVBString(rawString, 2);
		extendedDvbString = new DVBString(rawStringExtendedAsci, 2);
		
		explicitLengthDvbString = new DVBString(rawStringWithoutLength, 0,4);
	}


	@Test
	public void simpleString() {
		org.junit.Assert.assertEquals(DVB_INSPECTOR,dvbString.toString());
		org.junit.Assert.assertEquals(rawString,dvbString.getData());
		org.junit.Assert.assertEquals(13,dvbString.getLength());
		org.junit.Assert.assertNull(dvbString.getCharSet());

	}

	@Test

	public void extendedString() {
		org.junit.Assert.assertEquals("€¼½¾©",extendedDvbString.toString());
		org.junit.Assert.assertEquals(rawStringExtendedAsci,extendedDvbString.getData());
		org.junit.Assert.assertEquals(5,extendedDvbString.getLength());
		org.junit.Assert.assertNull(extendedDvbString.getCharSet());

	}
	
	
	@Test

	public void explicitLengthString() {
		org.junit.Assert.assertEquals("Test",explicitLengthDvbString.toString());
		org.junit.Assert.assertEquals(4,explicitLengthDvbString.getLength());
		org.junit.Assert.assertNull(explicitLengthDvbString.getCharSet());

	}

}
