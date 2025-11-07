/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2025 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import nl.digitalekabeltelevisie.controller.KVP;

/**
 * 
 */
public class DefaultMutableTreeNodePreorderEnumarationTest {
	
	
	
	DefaultMutableTreeNodePreorderEnumaration enumaration;
	@Before
	public void setup() {

		KVP root = new KVP("root");
		KVP a = new KVP("a");
		a.add(new KVP("1"));
		a.add(new KVP("2"));
		root.add(a);
		KVP b = new KVP("b");
		b.add(new KVP("1"));
		b.add(new KVP("2"));
		b.add(new KVP("3"));
		b.add(new KVP("4"));
		root.add(b);
		KVP c = new KVP("c");
		KVP c1 = new KVP("1");
		c1.add(new KVP("i"));
		c1.add(new KVP("ii"));
		c1.add(new KVP("iii"));
		c.add(c1);
		root.add(c);

		KVP c2 = new KVP("2");
		c2.add(new KVP("first"));
		c2.add(new KVP("second"));
		c2.add(new KVP("third"));
		c.add(c2);
		KVP c3 = new KVP("3");
		c.add(c3);
		
		
		enumaration = new DefaultMutableTreeNodePreorderEnumaration(root);
	}

	@Test
	public void enumarationTest() {
		
		int i = 0;
		String[] t = {"root", 
						"a", 
							"1", 
							"2", 
						"b", 
							"1",
							"2",
							"3",
							"4",
						"c",
							"1",
								"i",
								"ii",
								"iii",
							"2",
								"first",
								"second",
								"third",
							"3"};
		while(enumaration.hasMoreElements()) {
			assertEquals(t[i], enumaration.nextElement().getPlainText());
			i++;
		}
	}

	
}
