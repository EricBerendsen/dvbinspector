/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2021 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

import java.util.prefs.Preferences;

import nl.digitalekabeltelevisie.main.DVBinspector;

public class PreferencesManager {

	final static Preferences prefs = Preferences.userNodeForPackage(DVBinspector.class);

	private static final String DEFAULT_G0_CHARACTER_SET = "defaultg0_character_set";
	private static final String DEFAULT_PRIVATE_DATA_SPECIFIER = "private_data_spcifier";
	private static final String DEFAULT_VIEW_MODUS = "view_modus";

	private static final String LAST_USED_DIR = "stream_directory";
	
	private static final String SAVE_DIR = "save_directory";

	/**
	 * keys for storage of last used window location/size in Preferences
	 */
	private static final String WINDOW_WIDTH = "window_width";
	private static final String WINDOW_HEIGHT = "window_height";
	private static final String WINDOW_X = "window_x";
	private static final String WINDOW_Y = "window_y";

	
	private static final String ENABLE_GENERIC_PSI = "enable_generic_psi";
	private static final String ENABLE_DSMCC = "enable_dsmcc";
	private static final String ENABLE_PCR_PTS = "enable_pcr_pts";
	private static final String ENABLE_M7_FASTSCAN = "enable_m7_fastscan";

	private static final String SELECT_MPEG_FILE_FILTER = "select_mpeg_file_filter";
	
	private static final String ENABLE_SECONDS_TIMESTAMP_FORMAT = "enable_seconds_timestamp_format";

	private static final String PACKET_LENGTH_MODUS = "packet_length_modus";

	// private constructor to avoid client applications to use constructor
	private PreferencesManager() {
	}

	public static int getDefaultG0CharacterSet() {
		return prefs.getInt(DEFAULT_G0_CHARACTER_SET, 0);
	}

	public static void setDefaultG0CharacterSet(int defaultG0CharacterSet) {
		prefs.putInt(DEFAULT_G0_CHARACTER_SET, defaultG0CharacterSet);
	}

	public static void setDefaultPrivateDataSpecifier(long specifier) {
		prefs.putLong(DEFAULT_PRIVATE_DATA_SPECIFIER, specifier);
	}

	public static long getDefaultPrivateDataSpecifier() {
		return prefs.getLong(DEFAULT_PRIVATE_DATA_SPECIFIER, 0);
	}

	public static int getDefaultViewModus() {
		return prefs.getInt(DEFAULT_VIEW_MODUS, 0);
	}

	public static void setDefaultViewModus(int mod) {
		prefs.putInt(DEFAULT_VIEW_MODUS, mod);
	}

	public static void setLastUsedDir(String dir) {
		prefs.put(LAST_USED_DIR, dir);
	}

	public static String getLastUsedDir() {
		return prefs.get(LAST_USED_DIR, null);
	}

	public static void setSaveDir(String dir) {
		prefs.put(SAVE_DIR, dir);
	}

	public static String getSaveDir() {
		return prefs.get(SAVE_DIR, null);
	}

	public static int getWindowX() {
		return prefs.getInt(WINDOW_X, 10);
	}

	public static int getWindowY() {
		return prefs.getInt(WINDOW_Y, 10);
	}

	public static int getWindowWidth() {
		return prefs.getInt(PreferencesManager.WINDOW_WIDTH, 980);
	}

	public static int getWindowHeight() {
		return prefs.getInt(PreferencesManager.WINDOW_HEIGHT, 700);
	}

	public static void setWindowX(int x) {
		prefs.putInt(WINDOW_X, x);
	}

	public static void setWindowY(int y) {
		prefs.putInt(WINDOW_Y, y);
	}

	public static void setWindowWidth(int width) {
		prefs.putInt(WINDOW_WIDTH, width);
	}

	public static void setWindowHeight(int height) {
		prefs.putInt(WINDOW_HEIGHT, height);
	}

	public static void setEnableGenericPSI(boolean enabled) {
		prefs.putBoolean(ENABLE_GENERIC_PSI, enabled);

	}

	public static boolean getEnableGenericPSI() {
		return prefs.getBoolean(ENABLE_GENERIC_PSI, true);
	}

	public static boolean isEnableGenericPSI() {
		return getEnableGenericPSI();
	}

	public static void setEnableDSMCC(boolean enabled) {
		prefs.putBoolean(ENABLE_DSMCC, enabled);
	}
	
	public static boolean getEnableDSMCC() {
		return prefs.getBoolean(ENABLE_DSMCC, true);
	}

	public static boolean isEnableDSMCC() {
		return getEnableDSMCC();
	}

	public static void setEnablePcrPtsView(boolean enabled) {
		prefs.putBoolean(ENABLE_PCR_PTS, enabled);
	}

	public static boolean getEnablePcrPtsView() {
		return prefs.getBoolean(ENABLE_PCR_PTS, true);
	}

	public static boolean isEnablePcrPtsView() {
		return getEnablePcrPtsView();
	}
	
	public static void setEnableM7Fastscan(boolean enabled) {
		prefs.putBoolean(ENABLE_M7_FASTSCAN, enabled);
	}

	public static boolean getEnableM7Fastscan() {
		return prefs.getBoolean(ENABLE_M7_FASTSCAN, true);
	}

	public static boolean isEnableM7Fastscan() {
		return getEnableM7Fastscan();
	}

	public static void setSelectMpegFileFilter(boolean enabled) {
		prefs.putBoolean(SELECT_MPEG_FILE_FILTER, enabled);

	}

	public static boolean getSelectMpegFileFilter() {
		return prefs.getBoolean(SELECT_MPEG_FILE_FILTER, true);
	}

	public static boolean isSelectMpegFileFilter() {
		return getSelectMpegFileFilter();
	}

	public static void setEnableSecondsTimestamp(boolean enabled) {
		prefs.putBoolean(ENABLE_SECONDS_TIMESTAMP_FORMAT, enabled);
	}

	public static boolean getEnableSecondsTimestamp() {
		return prefs.getBoolean(ENABLE_SECONDS_TIMESTAMP_FORMAT, false);
	}

	public static boolean isEnableSecondsTimestamp() {
		return getEnableSecondsTimestamp();
	}


	public static int getPacketLengthModus() {
		return prefs.getInt(PACKET_LENGTH_MODUS, 0);
	}

	public static void setPacketLengthModus(int mod) {
		prefs.putInt(PACKET_LENGTH_MODUS, mod);
	}

}
