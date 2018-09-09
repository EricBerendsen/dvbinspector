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

package nl.digitalekabeltelevisie.gui;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.io.StringWriter;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.TransferHandler;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;

import nl.digitalekabeltelevisie.util.Utils;


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
public class EditorTextHTMLTransferHandler extends TransferHandler {


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
        // now extract plain text from HTML,  replacing &lt;br&gt; and &lt;/p&gt; with newlines.
        final String plainText = Utils.extractTextFromHTML(htmlString);
        return new TextHTMLTransferable(plainText, htmlString);
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
