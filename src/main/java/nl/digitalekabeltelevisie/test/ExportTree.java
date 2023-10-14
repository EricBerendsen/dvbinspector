/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2020 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.test;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.TransportStream;
import nl.digitalekabeltelevisie.gui.DVBtree;
import nl.digitalekabeltelevisie.util.PreferencesManager;

/**
 * This program parses all files under tsDir and sub directories, and if they happen to be a valid TS file, exports
 * the PSI Tree to  a .txt file to exportDir, in identical directory structure.
 * 
 * The files in exportDir can then be searched using grep or windows explorer, if you are looking for a .ts file that 
 * contains that one unique descriptor. 
 * 
 * @author Eric
 *
 */
public class ExportTree {

	public static String tsDir = "I:\\regressiontests";
	public static String exportDir = "I:\\regresssionexportrefactoreddsmcc20231014";
	static Path baseDirPath = Paths.get(tsDir);
	static Path exportDirPath = Paths.get(exportDir);
	static File logFile = new File(exportDir, "log.txt");
	
	static PrintWriter log; 

	public static void main(String[] args) throws IOException {
		
		log = new PrintWriter(logFile);
		
		PreferencesManager.setEnableDSMCC(true);
		PreferencesManager.setEnableGenericPSI(false);
		PreferencesManager.setEnablePcrPtsView(false);
	
		
		Path basePath = Paths.get(tsDir);
		Files.walk(basePath)
        .filter(Files::isRegularFile)
        .forEach(t -> exportFile(t));
		
		
		log.close();

	}
	
	static void exportFile(Path filePath) {
		System.out.println("Path:"+filePath);
		Path relativePath = baseDirPath.relativize(filePath);
		
		String newExtensionRelativePath =relativePath.toString()+".txt";
		Path exportFile = exportDirPath.resolve(newExtensionRelativePath);
		Path parent = exportFile.getParent();
		if(!parent.toFile().exists()) {
			System.out.println("exportFile parent: does not exist:"+parent);
			boolean succes = parent.toFile().mkdirs();
			System.out.println("dir created: "+succes);
		}
		File tsFile = filePath.toFile();
		
		try {
			TransportStream transportStream = new TransportStream(tsFile);
			transportStream.parseStream();
			
			DefaultMutableTreeNode node = transportStream.getJTreeNode(2); // psi only
			
			KVP kvp = (KVP)node.getUserObject();

			final String lineSep = System.getProperty("line.separator");
			final StringBuilder res = new StringBuilder(kvp.getPlainText());
			res.append(lineSep);

			res.append(DVBtree.getEntireTree(node,""));
			String treeString = res.toString();
			
			File exportTreeFile = exportFile.toFile();
			try (PrintWriter out = new PrintWriter(exportTreeFile,"UTF-8")) {
			    out.println(treeString);
			}
			log.println("Succes for  File:"+filePath);
			log.flush();
			
		} catch (Throwable e) {

			log.println("Parsing error in File:"+filePath+", exception:"+e);
			log.flush();
			System.out.println("Parsing error in File:"+filePath+"excoption:"+e);
		}
		
		
	}

}
