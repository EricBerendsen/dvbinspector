/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2024 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

import static org.junit.Assert.*;

import java.awt.image.BufferedImage;
import java.math.BigInteger;
import java.util.HexFormat;

import org.junit.Test;

import nl.digitalekabeltelevisie.gui.HTMLSource;
import nl.digitalekabeltelevisie.gui.ImageSource;

public class KVPTest {

    @Test
    public void testKVPString() {
        assertEquals("Test", new KVP("Test").toString());
    }

    @Test
    public void testKVPStringImageSource() {
        ImageSource imgSource = new ImageSource() {

            @Override
            public BufferedImage getImage() {

                return null;
            }
        };
        KVP kvp = new KVP("Plaatje", imgSource);
        assertEquals("Plaatje", kvp.toString());
        assertEquals(1, kvp.getDetailViews().size());
        assertEquals(imgSource, kvp.getDetailViews().getFirst().detailSource());
        assertEquals("", kvp.getDetailViews().getFirst().label());
    }

    @Test
    public void testKVPLabelValueDescription() {
        KVP kvp = new KVP("Label1","Value232","Description!");
        assertEquals("Label1: Value232 => Description!",kvp.toString());
    }

    @Test
    public void testKVPStringIntString() {
        KVP kvp = new KVP("LabelForInt",42,"Explanation");
        KVP.setNumberDisplay(KVP.NUMBER_DISPLAY.DECIMAL);
        assertEquals("LabelForInt: 42 => Explanation",kvp.toString());
        KVP.setNumberDisplay(KVP.NUMBER_DISPLAY.BOTH);
        assertEquals("LabelForInt: 0x2A (42) => Explanation",kvp.toString());
        KVP.setNumberDisplay(KVP.NUMBER_DISPLAY.HEX);
        assertEquals("LabelForInt: 0x2A => Explanation",kvp.toString());

        assertEquals("LabelForInt:42",kvp.getCrumb());
    }

    @Test
    public void testKVPStringLongString() {
        KVP kvp = new KVP("LabelForLong",142L,"Loooooong Explanation");
        KVP.setNumberDisplay(KVP.NUMBER_DISPLAY.DECIMAL);
        assertEquals("LabelForLong: 142 => Loooooong Explanation",kvp.toString());
        KVP.setNumberDisplay(KVP.NUMBER_DISPLAY.BOTH);
        assertEquals("LabelForLong: 0x8E (142) => Loooooong Explanation",kvp.toString());
        KVP.setNumberDisplay(KVP.NUMBER_DISPLAY.HEX);
        assertEquals("LabelForLong: 0x8E => Loooooong Explanation",kvp.toString());
        kvp = new KVP("LabelForLong",142L,null);
        KVP.setNumberDisplay(KVP.NUMBER_DISPLAY.DECIMAL);
        assertEquals("LabelForLong: 142",kvp.toString());
        KVP.setNumberDisplay(KVP.NUMBER_DISPLAY.BOTH);
        assertEquals("LabelForLong: 0x8E (142)",kvp.toString());
        KVP.setNumberDisplay(KVP.NUMBER_DISPLAY.HEX);
        assertEquals("LabelForLong: 0x8E",kvp.toString());
    }

    @Test
    public void testKVPStringBooleanString() {
        KVP kvp = new KVP("BooleanLabel",true,"Boooolean Explanation");
        KVP.setNumberDisplay(KVP.NUMBER_DISPLAY.HEX);
        assertEquals("BooleanLabel: 0x1 => Boooolean Explanation",kvp.toString());
        KVP.setNumberDisplay(KVP.NUMBER_DISPLAY.BOTH);
        assertEquals("BooleanLabel: 0x1 (1) => Boooolean Explanation",kvp.toString());
        kvp = new KVP("BooleanLabel",false,"Boooolean false Explanation");
        assertEquals("BooleanLabel: 0x0 (0) => Boooolean false Explanation",kvp.toString());
        KVP.setNumberDisplay(KVP.NUMBER_DISPLAY.DECIMAL);
        assertEquals("BooleanLabel: 0 => Boooolean false Explanation",kvp.toString());
    }

    @Test
    public void testHtmlLabelString() {
        KVP kvp = new KVP("<h1>Head</h1>","<b>bold label?</b>");
        KVP.setStringDisplay(KVP.STRING_DISPLAY.HTML_AWT);
        assertEquals("<html><h1>Head</h1></html>",kvp.toString());
        KVP.setStringDisplay(KVP.STRING_DISPLAY.PLAIN);
        assertEquals("<b>bold label?</b>",kvp.toString());
        KVP.setStringDisplay(KVP.STRING_DISPLAY.HTML_FRAGMENTS);
        assertEquals("<h1>Head</h1>",kvp.toString());
        KVP.setStringDisplay(KVP.STRING_DISPLAY.JAVASCRIPT);
        assertEquals("",kvp.toString());
    }

    @Test
    public void testKVPStringByteArrayString() {
        byte[] just_bytes = HexFormat.of().parseHex("D6E509290061A341");
        KVP kvp = new KVP("array_of_bytes",just_bytes,null);
        assertEquals("array_of_bytes: 0xD6E509290061A341 \"...).a.A\"",kvp.toString());
        kvp = new KVP("array_of_bytes",just_bytes,4,3,"details");
        assertEquals("array_of_bytes: 0x0061A3 \".a.\" => details",kvp.toString());

        // HTML View
        assertEquals(1,kvp.getDetailViews().size());
        assertTrue(kvp.getDetailViews().getFirst().detailSource() instanceof HTMLSource);

        assertEquals("<pre>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;0001 0203 0405 0607 0809 0A0B 0C0D 0E0F 0123456789ABCDEF<br>0x000000&nbsp;<span style=\"color:black; background-color: white;\">0061&nbsp;A3&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;.a.</span><br></pre>" ,((HTMLSource)kvp.getDetailViews().getFirst().detailSource()).getHTML());

        byte[] empty = new byte[0];
        kvp = new KVP("empty array",empty,"not a lot");
        assertEquals("empty array: - => not a lot", kvp.toString());
    }


    @Test
    public void testKVPStringDVBStringString() {
        final byte []rawString = {0x5,0x44,13,0x44,0x56,0x42,0x20,0x49,0x6e,0x73,0x70,0x65,0x63,0x74,0x6f,0x72,0x21,0x55};
        final byte []rawStringExtendedAsci = {0x5,0x44,5,-92,-68,-67,-66,-45};


        DVBString dvbString = new DVBString(rawString, 2);

        KVP kvp = new KVP("DVBStringTest",dvbString,"description");

        assertEquals("DVBStringTest: DVB Inspector => description", kvp.toString());


        dvbString = new DVBString(rawStringExtendedAsci, 2);

        kvp = new KVP("quarter",dvbString,"half");
        assertEquals("quarter: €¼½¾© => half", kvp.toString());
    }

    @Test
    public void testKVPStringHTMLSource() {
        String html = "\"pre <b> bold </b> post ' quote ";

        HTMLSource htmlSource = new HTMLSource() {

            @Override
            public String getHTML() {

                return html;
            }
        };
        KVP kvp = new KVP("htmlfragment",htmlSource);

        kvp.appendLabel(" [1]");
        assertEquals("htmlfragment [1]",kvp.toString());

        assertEquals("htmlfragment",kvp.getCrumb());

        // HTML View
        assertEquals(1,kvp.getDetailViews().size());
        assertTrue(kvp.getDetailViews().getFirst().detailSource() instanceof HTMLSource);

        assertEquals(html ,((HTMLSource)kvp.getDetailViews().getFirst().detailSource()).getHTML());


    }

    @Test
    public void testKVPStringBigIntegerString() {

        String biggy = "123456789012456789908976543232";

        BigInteger bg = new BigInteger(biggy);

        KVP kvp = new KVP("Big Number",bg,null);
        KVP.setNumberDisplay(KVP.NUMBER_DISPLAY.DECIMAL);

        assertEquals("Big Number: "+biggy, kvp.toString());

        // test non global formatting
        assertEquals("Big Number: 0x18EE90FF6C4FE9FCDC157A600", kvp.toString(KVP.STRING_DISPLAY.PLAIN,KVP.NUMBER_DISPLAY.HEX));
        assertEquals("Big Number: 0x18EE90FF6C4FE9FCDC157A600 ("+biggy +")" , kvp.toString(KVP.STRING_DISPLAY.PLAIN,KVP.NUMBER_DISPLAY.BOTH));

        KVP.setNumberDisplay(KVP.NUMBER_DISPLAY.HEX);
        assertEquals("Big Number: 0x18EE90FF6C4FE9FCDC157A600", kvp.toString());

        KVP.setNumberDisplay(KVP.NUMBER_DISPLAY.BOTH);
        assertEquals("Big Number: 0x18EE90FF6C4FE9FCDC157A600 ("+biggy +")" , kvp.toString());
    }

    @Test
    public void testDetailSources() {

        ImageSource imgSource = new ImageSource() {

            @Override
            public BufferedImage getImage() {

                return null;
            }
        };

        HTMLSource htmlSource = new HTMLSource() {

            @Override
            public String getHTML() {

                return "leeg";
            }
        };


        KVP kvp = new KVP("listlabel");
        kvp.addHTMLSource(htmlSource, "htmlSourceLabel");
        kvp.addImageSource(imgSource, "imageSourceLabel");

        assertEquals(2,kvp.getDetailViews().size());

        assertTrue(kvp.getDetailViews().getFirst().detailSource() instanceof HTMLSource);
        assertEquals(htmlSource, kvp.getDetailViews().getFirst().detailSource()) ;
        assertEquals( "htmlSourceLabel", kvp.getDetailViews().getFirst().label()) ;

        assertTrue(kvp.getDetailViews().get(1).detailSource() instanceof ImageSource);
        assertEquals(imgSource, kvp.getDetailViews().get(1).detailSource()) ;
        assertEquals( "imageSourceLabel", kvp.getDetailViews().get(1).label()) ;


    }

}
