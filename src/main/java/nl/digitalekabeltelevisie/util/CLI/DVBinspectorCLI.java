package nl.digitalekabeltelevisie.util.CLI;

import nl.digitalekabeltelevisie.data.mpeg.*;
import nl.digitalekabeltelevisie.gui.exception.NotAnMPEGFileException;
import nl.digitalekabeltelevisie.main.DVBinspector;
import nl.digitalekabeltelevisie.util.CLI.beans.*;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.commons.cli.*;

/**
 * Handle options and run instance of DVB Inspector CLI
 */
public class DVBinspectorCLI {

	private static final Logger LOGGER = Logger.getLogger(DVBinspector.class.getName());

	private TransportStream transportStream;
	final private DisplayData displayData = new DisplayData();
	final private ExportData exportData = new ExportData();
	final private CommandLine cmd;
	final private HelpFormatter formatter = new HelpFormatter();
	private String splitFileName;

	private RangeData lengthValues;
	private RangeData offsetValues;

	private boolean isSplitFile = false;

	public DVBinspectorCLI(final String[] args) {
		this.cmd = parseOptions(args);
	}

	/**
	 * Run an instance of DVB Inspector CLI
	 * 
	 * @throws Exception
	 */
	public void run() throws Exception {

		// Check if options are ok
		if (cmd == null) {
			return;
		}

		// get input filename
		final String inputFileName = cmd.getOptionValue("filename");

		// Check if file exists
		if (!new File(inputFileName).exists()) {
			LOGGER.log(Level.SEVERE, "Exit. No file corresponding to the given filename.");
			return;
		}

		// Parsing a chunk of initial file - Split and parse the chunked file
		if (cmd.hasOption("extractStart") || cmd.hasOption("extractLength")) {

			final boolean check = handleExtractOptions(cmd, inputFileName);
			if (!check) {
				System.out.println("Exiting.");
				return;
			}
			// Parse the initial stream
		} else {
			try {
				parseStream(inputFileName);
			} catch (final Throwable e) {
				LOGGER.log(Level.SEVERE, "error parsing transportStream", e);
				return;
			}
		}

		// Dump and Display
		System.out.println("\n\n#########################");
		System.out.println("### DVB INSPECTOR CLI ###");
		System.out.println("#########################");

		final Utility utility = new Utility(exportData, cmd, transportStream);

		// display values given
		if (cmd.hasOption("extractStart") || cmd.hasOption("extractLength")) {
			System.out.println("\nFile parsed with :");
			System.out.println("	offset : " + offsetValues.getDisplayedValue() + " " + offsetValues.getUnit());
			System.out.println("	length : " + lengthValues.getDisplayedValue() + " " + lengthValues.getUnit());
		}

		// display available options to use with options
		if (cmd.hasOption("values")) {
			displayAvailableValues(utility);
			System.exit(1);
			return;
		}

		// build final json file
		final TsData tsData = buildFileToDump(cmd, transportStream, utility);
		if (tsData == null) {
			return;
		}

		// manage output file name and location
		final String output = handleOutputName(cmd, inputFileName);

		// export Json file to the target
		utility.getExportData().export(tsData, output);

		// display info in bash if option is activated
		if (cmd.hasOption("dumpInTerminal")) {
			displayData.displayAllInformation(tsData);
		}

		// in terminal information line
		System.out.println("\n" + inputFileName + " parsed. Dump JSON available at target/" + output);

		// delete split file
		if (isSplitFile) {
			deleteFile(splitFileName);
		}
		System.out.println("\n");
	}

	/**
	 * Read and parse options from terminal
	 * 
	 * @param args - Arguments from user command line
	 * @return CommandLine - Contains values given by user
	 */
	public CommandLine parseOptions(final String[] args) {
		// Setting up parameters
		final Options options = setOptions();

		// initialize values

		final CommandLineParser parser = new DefaultParser();
		final CommandLine cmd;

		// parsing parameters
		try {
			cmd = parser.parse(options, args);
		} catch (Exception e) {
			System.out.println(" ");
			System.out.println(e.getMessage());
			System.out.println(" ");
			formatter.printHelp("Available options ", options);
			return null;
		}

		// help management
		if (cmd.hasOption("help")) {
			System.out.println(" ");
			formatter.printHelp("Available parameters to use with DVB Inspector CMD : ", options);
			System.out.println(" ");
			System.exit(1);
			return null;
		}
		return cmd;
	}

	/**
	 * Parse a stream given by the filename
	 * 
	 * @param filename - name of the file to be parsed
	 * @throws NotAnMPEGFileException
	 * @throws IOException
	 */
	public void parseStream(final String filename)
			throws NotAnMPEGFileException, IOException {
		transportStream = new TransportStream(filename);
		transportStream.parseStream();
	}

	/**
	 * Setup parameters that can be passed to DVB Ins. command Line.
	 * 
	 * @return Available options
	 */
	private Options setOptions() {
		// Command line parameters
		final Options options = new Options();

		// available parameters
		final Option help = new Option("h", "help", false, "Help for DVB Inspector CMD");
		final Option values = new Option("v", "values", false, "Available values to use with parameters");

		final Option psiParam = Option.builder("psi")
				.longOpt("psiOnly")
				.desc("Display only PSI tables")
				.required(false)
				.build();

		final Option es = Option.builder("es")
				.longOpt("extractStart")
				.desc("Offset to start parsing (units : none=Bytes, k=KiloBytes, m=MegaBytes, g=GigaBytes, s=seconds, p=numberOfPackets)")
				.required(false)
				.hasArg(true)
				.build();

		final Option el = Option.builder("el")
				.longOpt("extractLength")
				.desc("Length of the file to parse (units : none=Bytes, k=KiloBytes, m=MegaBytes, g=GigaBytes, s=seconds, p=numberOfPackets)")
				.required(false)
				.hasArg(true)
				.build();

		final Option output = Option.builder("out")
				.longOpt("output")
				.desc("Filename for the dump")
				.hasArg(true)
				.required(false)
				.build();

		final Option filename = Option.builder("f")
				.longOpt("filename")
				.desc("File to be parsed")
				.hasArg(true)
				.required(true)
				.build();

		final Option tsPackets = Option.builder("pa")
				.optionalArg(true)
				.hasArgs()
				.longOpt("tsPackets")
				.desc("Display TS packets (can take some time) - Pids can be specified")
				.required(false)
				.build();

		final Option tsPacketsRange = Option.builder("par")
				.longOpt("tsPacketsRange")
				.hasArgs()
				.desc("Display TS packets in given range (can take some time)")
				.required(false)
				.build();

		final Option pidNumber = Option.builder("pid")
				.longOpt("pidOnly")
				.desc("Select specific PIDs")
				.optionalArg(true)
				.hasArgs()
				.required(false)
				.build();

		final Option pes = Option.builder("pes")
				.longOpt("pesOnly")
				.desc("Display specific PES tables - Pids can be specified")
				.optionalArg(true)
				.hasArgs()
				.required(false)
				.build();

		final Option dumpInTerminal = Option.builder("d")
				.longOpt("dumpInTerminal")
				.desc("Display parsed data in terminal")
				.hasArg(false)
				.required(false)
				.build();

		options.addOption(help);
		options.addOption(values);
		options.addOption(filename);
		options.addOption(output);
		options.addOption(psiParam);
		options.addOption(tsPackets);
		options.addOption(tsPacketsRange);
		options.addOption(pidNumber);
		options.addOption(pes);
		options.addOption(dumpInTerminal);
		options.addOption(es);
		options.addOption(el);

		return options;
	}

	/**
	 * Handle Start and Length values for chunk parsing
	 * 
	 * @param cmd           - CommandLine parsed from the terminal
	 * @param inputFileName - Name of the input file
	 * @throws NotAnMPEGFileException
	 * @throws IOException
	 */
	public boolean handleExtractOptions(final CommandLine cmd, final String inputFileName)
			throws NotAnMPEGFileException, IOException {

		final TransportStream ts;
		// get amount of bits from file
		final int len = (int) new File(inputFileName).length();

		// default values
		final String start;
		final String length;

		// set values from options
		if (cmd.getOptionValue("extractStart") != null) {
			start = cmd.getOptionValue("extractStart");
		} else {
			start = "0";
		}
		if (cmd.getOptionValue("extractLength") != null) {
			length = cmd.getOptionValue("extractLength");
		} else {
			length = String.valueOf(len);
		}

		// parsing initial file if second or packet unit is used
		if (start.contains("p") || length.contains("p") || start.contains("s") || length.contains("s")) {
			ts = new TransportStream(inputFileName);
			parseStream(inputFileName);
		} else {
			ts = null;
		}

		// extract and set start and length values
		offsetValues = extractOffsetValues(start, ts);
		lengthValues = extractOffsetValues(length, ts);

		if (offsetValues == null || lengthValues == null) {

			LOGGER.log(Level.SEVERE,
					"Error. Invalid value. Extract values must be in [int][unit] format.");
			return false;
		}

		// check validity of offset
		if (offsetValues.getByteValue() >= len) {
			LOGGER.log(Level.SEVERE, "Wrong value. Offset value greater than file size.");
			return false;
		}

		// split initial stream with given values
		splitFileName = splitFile(inputFileName, offsetValues.getByteValue(), lengthValues.getByteValue());
		isSplitFile = true;

		// Parsing split stream
		try {
			parseStream(splitFileName);
		} catch (final Throwable e) {
			LOGGER.log(Level.SEVERE, "error parsing transportStream", e);
			return false;
		}
		return true;
	}

	/**
	 * Display values usable as parameters
	 * 
	 * @param utility
	 */
	public void displayAvailableValues(final Utility utility) {
		System.out.println("\nAvailable options to use with parameters : ");
		System.out.println(" - PIDs list : " + Arrays.toString(transportStream.getUsedPids()));
		System.out.println(" - PIDs with PES list : "
				+ Arrays.toString((utility.getExportData().getPidWPes(transportStream).toArray())));
		System.out.println(" - Number of TS packets : " + transportStream.getNo_packets());
		System.out.println(" - Length of file : " + transportStream.getLen() + " bytes / "
				+ transportStream.getLength() + " secs");
		System.out.println(" ");
	}

	/**
	 * Building file to dump with given TS and parameters
	 * 
	 * @param cmd - command line containing user options
	 * @param ts  - transportStream to be parsed
	 * @return TsData
	 */
	private TsData buildFileToDump(final CommandLine cmd, final TransportStream ts, Utility utility) {

		// Construct tsData with given TransportStream - TS info in all dump
		final TsData tsData = new TsData(ts);

		int count = (int) Stream.of(cmd.hasOption("pid"), cmd.hasOption("psiOnly"), cmd.hasOption("pesOnly"),
				cmd.hasOption("tsPackets"), cmd.hasOption("tsPacketsRange")).filter(b -> b).count();

		// multiple options selected
		if (count > 1) {
			LOGGER.log(Level.SEVERE,
					"Conflict between options. Options -pid, -psi, -pes and -pa(r) are not usable at the same time.");
			return null;

		} else {
			// adding specific pid to export file
			if (cmd.hasOption("pid")) {
				final List<PID> listPids = utility.handlePidExport();
				if (listPids == null) {
					System.out.println("Exiting.");
					return null;
				}
				tsData.setPids(listPids);

				// export PSI tables only
			} else if (cmd.hasOption("psiOnly")) {
				final PsiData psi = utility.getExportData().collectPsiInformation(ts);
				tsData.setPsi(psi);
				System.out.println("\nExporting only PSI tables");

				// export PES tables only
			} else if (cmd.hasOption("pesOnly")) {
				final List<PesData> listPesPackets = utility.handlePesExport();
				if (listPesPackets == null) {
					System.out.println("Exiting.");
					return null;
				}
				tsData.setPes(listPesPackets);

				// adding packets to export file
			} else if ((cmd.hasOption("tsPackets") || cmd.hasOption("tsPacketsRange"))) {
				final List<TsPacketsData> listTsPackets = utility.handleTsPacketsExport();
				if (listTsPackets == null) {
					System.out.println("Exiting.");
					return null;
				}
				tsData.setTsPackets(listTsPackets);

				// export PIDs and PSI tables
			} else {
				System.out.println("\nExporting PIDs and PSI tables");
				// adding psi to export file
				final PsiData psi = utility.getExportData().collectPsiInformation(ts);
				tsData.setPsi(psi);

				// adding pids to export file
				final List<PID> listPids = utility.getExportData().collectPidsInformation(ts, null);
				tsData.setPids(listPids);
			}
			return tsData;
		}
	}

	/**
	 * Handle output file name of the dump
	 * 
	 * @param cmd
	 * @param filename
	 * @return
	 * @throws IOException
	 */
	public String handleOutputName(final CommandLine cmd, final String filename) throws IOException {
		// Create folder to dump into
		final Path path = Paths.get("jsDump");
		if (!Files.exists(path)) {
			Files.createDirectory(path);
		}
		// Set output filename - custom or default
		final String output;
		if (cmd.hasOption("output")) {
			output = path.getFileName() + "/" + cmd.getOptionValue("output");
		} else {
			output = path.getFileName() + "/" + filename.substring(0, filename.indexOf('.'))
					+ "_dump.json";
		}
		return output;
	}

	/**
	 * Extract one chunk of the initial file
	 * 
	 * @param sourceFileName - name of the file to be split
	 * @param offset         - start of the split file
	 * @param length         - length of the split file
	 * @return String - name of the split file
	 * @throws IOException
	 */
	public String splitFile(final String sourceFileName, final long offset, long length)
			throws IOException {

		if ((length <= 0) || (offset < 0)) {
			System.out.println("\nOffset : " + offset);
			System.out.println("Length : " + length);
			throw new IllegalArgumentException("Values must be greater than zero");
		}

		// Set output file
		final Path outputFileName;
		try {
			// Create a temporary file
			outputFileName = Files.createTempFile(
					Paths.get(sourceFileName.substring(0, sourceFileName.indexOf('.'))).toString(),
					sourceFileName.substring(sourceFileName.indexOf('.'), sourceFileName.length()));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		// Read source file
		try (final RandomAccessFile sourceFile = new RandomAccessFile(sourceFileName, "r");
				final FileChannel sourceChannel = sourceFile.getChannel()) {

			// Write output file
			try (final RandomAccessFile toFile = new RandomAccessFile(outputFileName.toFile(), "rw");
					final FileChannel toChannel = toFile.getChannel()) {
				// Set the offset on the source file
				sourceChannel.transferTo(offset, length, toChannel);
			}
		}
		return outputFileName.toString();
	}

	/**
	 * Delete file associated to the given filename
	 * 
	 * @param filename - Name of the file to delete
	 */
	private void deleteFile(final String filename) {

		final File file = new File(filename);

		if (file.delete()) {
			System.out.println("\nFile " + filename + " deleted successfully.");
		} else {
			System.out.println("\nError - file " + filename + " not deleted.");
		}
	}

	/**
	 * Extract values with units
	 * 
	 * @param parsedValues - value to parse and extract
	 * @param ts           - Transport stream
	 * @return RangeData - Value and unit / null - if invalid parsedValue
	 * 
	 */
	public RangeData extractOffsetValues(final String parsedValue, final TransportStream ts) {

		long byteValue = 0;
		Integer displayedValue = 0;
		String unit = "";

		final String valueWithoutUnit = parsedValue.substring(0, parsedValue.length() - 1);

		if (Pattern.matches("[0-9]+[A-z]", parsedValue)) {

			if (Pattern.matches("[0-9]+[spkmg]", parsedValue)) {

				// values unit are seconds (time)
				if (parsedValue.contains("s")) {
					// using transportStream from class
					final int bitrate = (int) transportStream.getBitRate();
					byteValue = (long) (bitrate * Integer.parseInt(valueWithoutUnit)) / 8;
					displayedValue = Integer.parseInt(valueWithoutUnit);
					unit = "seconds";

					// values unit are packets
				} else if (parsedValue.contains("p")) {
					final int packetLength = (int) ts.getPacketLenghth();
					byteValue = (long) (packetLength * Long.parseLong(valueWithoutUnit));
					displayedValue = Integer.parseInt(valueWithoutUnit);
					unit = "packets";

					// values units are kb, mb, gb
				} else if (parsedValue.contains("k")) {
					byteValue = Long.parseLong(valueWithoutUnit) * 1024;
					displayedValue = Integer.parseInt(valueWithoutUnit);
					unit = "Kb";

				} else if (parsedValue.contains("m")) {
					byteValue = Long.parseLong(valueWithoutUnit) * 1024 * 1024;
					displayedValue = Integer.parseInt(valueWithoutUnit);
					unit = "Mb";

				} else if (parsedValue.contains("g")) {
					byteValue = Long.parseLong(valueWithoutUnit) * 1024 * 1024 * 1024;
					displayedValue = Integer.parseInt(valueWithoutUnit);
					unit = "Gb";
				}
			} else {
				return null;
			}
			// value unit is bytes
		} else if (Pattern.matches("[0-9]+", parsedValue)) {
			byteValue = Long.parseLong(parsedValue);
			displayedValue = Integer.parseInt(parsedValue);
			unit = "bytes";
		} else {
			return null;
		}
		return new RangeData(byteValue, displayedValue, unit);
	}
}