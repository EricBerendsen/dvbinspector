/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2013 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.gui;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.TransferHandler;
import javax.swing.text.BadLocationException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;


/**
 * Helper to get newlines in clipboard when copying (CTRL-C) text from JEditorPanel with HTML content
 * Adds "\n" for each &lt;br&gt; and &lt;/p&gt;.
 *
 * based on  <a href="http://stackoverflow.com/questions/7745087/making-a-jeditorpane-with-html-put-correctly-formatted-text-in-clipboard">Making a JEditorPane with html put correctly formatted text in clipboard</a>, this version only puts the selected text on the clipboard (original version put entire text of JEditorPane on clipboard)
 *
 * @see <a href="http://stackoverflow.com/questions/7745087/making-a-jeditorpane-with-html-put-correctly-formatted-text-in-clipboard">http://stackoverflow.com/questions/7745087/making-a-jeditorpane-with-html-put-correctly-formatted-text-in-clipboard</a>
 * @author Steve McLeod, Eric Berendsen
 *
 */
public class MyTransferHandler extends TransferHandler {


    protected Transferable createTransferable(JComponent c) {
        final JEditorPane pane = (JEditorPane) c;

        int start = pane.getSelectionStart();
        int end = pane.getSelectionEnd();

        //first get the selected HTML text
        HTMLDocument doc =(HTMLDocument) pane.getDocument();
        StringWriter sw = new StringWriter(end - start);
        try{
        	pane.getEditorKit().write(sw, doc, start, end - start);
    	} catch (BadLocationException ble) {

    	} catch (IOException e) {

		}
        String htmlString = sw.toString();
        // now exctract plain text from HTML,  replacing &lt;br&gt; and &lt;/p&gt; with newlines.
        final String plainText = extractText(new StringReader(htmlString));
        return new MyTransferable(plainText, htmlString);
    }

    public String extractText(Reader reader) {
        final ArrayList<String> list = new ArrayList<String>();

        HTMLEditorKit.ParserCallback parserCallback = new HTMLEditorKit.ParserCallback() {
            public void handleText(final char[] data, final int pos) {
                list.add(new String(data));
            }

            public void handleStartTag(HTML.Tag tag, MutableAttributeSet attribute, int pos) {
            }

            public void handleEndTag(HTML.Tag t, final int pos) {
            	if (t.equals(HTML.Tag.P)) {
                    list.add("\n");
                }
            }

            public void handleSimpleTag(HTML.Tag t, MutableAttributeSet a, final int pos) {
                if (t.equals(HTML.Tag.BR)) {
                    list.add("\n");
                }
            }

            public void handleComment(final char[] data, final int pos) {
            }

            public void handleError(final String errMsg, final int pos) {
            }
        };
        try {
            new ParserDelegator().parse(reader, parserCallback, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        StringBuilder result = new StringBuilder();
        for (String s : list) {
        	result.append(s);
        }
        return result.toString();
    }


    @Override
    public void exportToClipboard(JComponent comp, Clipboard clip, int action) throws IllegalStateException {
        if (action == COPY) {
            clip.setContents(this.createTransferable(comp), null);
        }
    }

    @Override
    public int getSourceActions(JComponent c) {
        return COPY;
    }
}
