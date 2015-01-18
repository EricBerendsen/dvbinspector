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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 * Helper to get newlines in clipboard when copying (CTRL-C) text from JEditorPanel with HTML content
 * Adds
 *
 * @see <a href="http://stackoverflow.com/questions/7745087/making-a-jeditorpane-with-html-put-correctly-formatted-text-in-clipboard">http://stackoverflow.com/questions/7745087/making-a-jeditorpane-with-html-put-correctly-formatted-text-in-clipboard</a>
 * @author Steve McLeod
 *
 */
public class MyTransferable implements Transferable {

    private static final DataFlavor[] supportedFlavors;

    static {
        try {
            supportedFlavors = new DataFlavor[]{
                    new DataFlavor("text/html;class=java.lang.String"),
                    new DataFlavor("text/plain;class=java.lang.String")
            };
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final String plainData;
    private final String htmlData;

    public MyTransferable(String plainData, String htmlData) {
        this.plainData = plainData;
        this.htmlData = htmlData;
    }

    public DataFlavor[] getTransferDataFlavors() {
        return supportedFlavors;
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
        for (DataFlavor supportedFlavor : supportedFlavors) {
            if (supportedFlavor == flavor) {
                return true;
            }
        }
        return false;
    }

    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (flavor.equals(supportedFlavors[0])) {
            return htmlData;
        }
        if (flavor.equals(supportedFlavors[1])) {
            return plainData;
        }
        throw new UnsupportedFlavorException(flavor);
    }
}
