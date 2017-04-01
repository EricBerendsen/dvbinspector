/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2017 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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
 *  Based on http://stackoverflow.com/questions/811248/how-can-i-use-drag-and-drop-in-swing-to-get-file-path
 *
 */

package nl.digitalekabeltelevisie.gui;

import java.awt.datatransfer.*;
import java.io.*;
import java.util.List;
import java.util.logging.*;

import javax.swing.TransferHandler;

import nl.digitalekabeltelevisie.main.DVBinspector;

public class FileDropHandler extends TransferHandler{

	private static final Logger logger = Logger.getLogger(FileDropHandler.class.getName());

	public FileDropHandler(DVBinspector controller) {
		super();
		this.controller = controller;
	}

	private DVBinspector controller = null;

    @Override
    public boolean canImport(TransferHandler.TransferSupport support) {
        for (DataFlavor flavor : support.getDataFlavors()) {
            if (flavor.isFlavorJavaFileListType()) {
                return true;
            }
        }
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean importData(TransferHandler.TransferSupport support) {
        if (!this.canImport(support))
            return false;

        List <File> files;
        try {
            files = (List<File>) support.getTransferable()
                    .getTransferData(DataFlavor.javaFileListFlavor);
        } catch (UnsupportedFlavorException | IOException ex) {
            // should never happen (or JDK is buggy)
        	logger.log(Level.WARNING, "File drophandler error, should never happen (or JDK is buggy)", ex);
            return false;
        }

        if(!files.isEmpty()){ 
        	File file = files.get(0);// ignore extra files, only 1th is loaded
        	System.out.println("file dropped: "+file.getName());
        	if(file.isFile()){
    			final TSLoader tsLoader = new TSLoader(file,controller);
    			tsLoader.execute();

        	}
        }
        return true;
    }
}
