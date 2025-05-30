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

package nl.digitalekabeltelevisie.gui.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import nl.digitalekabeltelevisie.util.PreferencesManager;

/**
 * @author Eric
 *
 */
public final class RecentFiles {
	
	private List<String> recentFiles = new ArrayList<>();
	
	private static final int MAX_ENTRIES = 10;

    private static final class InstanceHolder {
        private static final RecentFiles instance = new RecentFiles();
    }

    public static RecentFiles getInstance() {

        return InstanceHolder.instance;
	}
	
	private RecentFiles() {
		init(PreferencesManager.getRecentFiles());
	}

	private void init(String allFiles) {
		
		recentFiles.clear();
		if(!allFiles.isEmpty()) {
			recentFiles.addAll(Arrays.asList(allFiles.split(File.pathSeparator)));
		}
		if(recentFiles.size()>MAX_ENTRIES) {
			recentFiles = recentFiles.subList(0, MAX_ENTRIES);
		}
	}
	
	private String getString() {
		return recentFiles.stream().collect(Collectors.joining (File.pathSeparator));
	}
	
	public void addOrMoveToBegin(String fileName) {
		recentFiles.remove(fileName);
		recentFiles.addFirst(fileName);
		if(recentFiles.size()>MAX_ENTRIES) {
			recentFiles = recentFiles.subList(0, MAX_ENTRIES);
		}
		
		PreferencesManager.setRecentFiles(getString());
	}
	
	public void reset() {
		recentFiles = new ArrayList<>();
		PreferencesManager.setRecentFiles(getString());
	}
	
	public int getNumberOfEntries() {
		return recentFiles.size();
	}
	
	public boolean remove(String fileName) {
		boolean result = recentFiles.remove(fileName);
		PreferencesManager.setRecentFiles(getString());
		return result;
	}

	
	public List<String> getRecentFiles(){
		return recentFiles;
	}
}
