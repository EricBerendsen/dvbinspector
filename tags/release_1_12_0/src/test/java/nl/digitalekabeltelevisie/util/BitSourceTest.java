/**
 * 
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 * 
 *  This code is Copyright 2009-2018 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.util;

import static org.junit.Assert.*;

import org.junit.*;

public class BitSourceTest {

	
	BitSource bitSource;
	BitSource emptySource;
	byte[] rawBits = {0b0000_0001, (byte)0b1111_1111, (byte)0b1100_0000, 0b0000_0000};
	byte[] emptyBits = {};
	
	@Before
	public void setUp(){
		bitSource = new BitSource(rawBits, 0);
		emptySource = new BitSource(emptyBits, 0);
	}

	@Test
	public void simpleTest() {
		assertEquals(bitSource.available(),32);
		assertTrue(bitSource.isByteAligned());
		assertEquals(bitSource.readBits(7) ,0);
		assertEquals(bitSource.available(),25);
		assertFalse(bitSource.isByteAligned());
		assertEquals(bitSource.readBits(11) ,0b0111_1111_1111);
		assertEquals(bitSource.available(),14);
		assertEquals(bitSource.readBits(14) ,0);
	}
	
	@Test
	public void emptyTest() {
		assertEquals(emptySource.available(), 0);
		assertTrue(emptySource.isByteAligned());
	}
	
	@Test
	public void copyConstructor() {
		BitSource copy = new BitSource(bitSource);
		assertEquals(bitSource.readBits(9),3);
		assertEquals(bitSource.available(),23);
		assertEquals(copy.available(),32);
		assertEquals(copy.readBits(7) ,0);
		assertEquals(copy.available(),25);
		assertFalse(copy.isByteAligned());
		assertEquals(copy.readBits(11) ,0b0111_1111_1111);

	}
}
