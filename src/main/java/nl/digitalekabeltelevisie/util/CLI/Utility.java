package nl.digitalekabeltelevisie.util.CLI;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.cli.CommandLine;

import nl.digitalekabeltelevisie.data.mpeg.PID;
import nl.digitalekabeltelevisie.data.mpeg.TransportStream;
import nl.digitalekabeltelevisie.util.CLI.beans.PesData;
import nl.digitalekabeltelevisie.util.CLI.beans.TsPacketsData;

/**
 * Handle data to export
 */
public class Utility {

    private ExportData exportData;
    private CommandLine cmd;
    private TransportStream ts;

    public Utility(ExportData exportData, CommandLine commandLine, TransportStream transportStream) {
        this.exportData = exportData;
        this.cmd = commandLine;
        this.ts = transportStream;
    }

    public ExportData getExportData() {
        return exportData;
    }

    public CommandLine getCmd() {
        return cmd;
    }

    public TransportStream getTs() {
        return ts;
    }

    /**
     * Handle options and export appropriate PID values
     * 
     * @return List<PID> - specific PIDs from transportStream
     */
    public List<PID> handlePidExport() {

        // get and convert PIDs used by ts
        short[] shortArray = ts.getUsedPids();
        List<Integer> usedPids = IntStream.range(0, shortArray.length)
                .mapToObj(s -> (int) shortArray[s])
                .collect(Collectors.toList());

        // dump all pids == pidOnly
        if (cmd.getOptionValue("pid") == null) {
            System.out.println("\nExporting all Pids");
            final List<PID> listPids = exportData.collectPidsInformation(ts, usedPids);

            return listPids;

            // export given PIDs
        } else {
            final List<Integer> pidValid = new ArrayList<Integer>();
            List<Integer> pidNotValid = new ArrayList<Integer>();

            // make sure that given PIDs are valid
            for (final String p : cmd.getOptionValues("pid")) {
                if (Pattern.matches("[0-9]+", p)) {
                    if (usedPids.contains(Integer.parseInt(p))) {
                        pidValid.add(Integer.parseInt(p));
                    } else {
                        pidNotValid.add(Integer.parseInt(p));
                    }
                } else {
                    System.out.println("Argument " + p + " invalid for -pid.");
                    return null;
                }
            }
            System.out.println(
                    "\nExporting Pids : " + pidValid.toString().substring(1, pidValid.toString().length() - 1));

            // collect requested PIDs
            final List<PID> listPids = exportData.collectPidsInformation(ts, pidValid);

            // Display invalid PIDs values
            if (!pidNotValid.isEmpty()) {
                pidNotValid = pidNotValid.stream()
                        .distinct()
                        .collect(Collectors.toList());
                System.out
                        .println(
                                "\nPIDs " + pidNotValid.toString().substring(1, pidNotValid.toString().length() - 1)
                                        + " not available.");
            }
            return listPids;
        }

    }

    /**
     * Handle options and export appropriate PES values
     * 
     * @return List<PesData> - All or part of Pes data
     */
    public List<PesData> handlePesExport() {

        List<PesData> pesPackets = new ArrayList<PesData>();

        // export all PES tables
        if (cmd.getOptionValue("pesOnly") == null) {

            try {
                pesPackets = exportData.collectPes(ts, null);
                System.out.println("\nExporting all PES tables");
                return pesPackets;
            } catch (IOException e) {
                e.printStackTrace();
            }

            // export selected PES tables
        } else {

            final List<Integer> pidForPes = new ArrayList<Integer>();
            final List<Integer> validPidForPes = new ArrayList<Integer>();
            final List<Integer> invalidPidForPes = new ArrayList<Integer>();

            // get pid with pes type
            for (final Integer integ : exportData.getPidWPes(ts)) {
                pidForPes.add(integ);
            }
            // loop throughout given parameters (pids numbers)
            for (final String p : cmd.getOptionValues("pesOnly")) {
                // make sure that the given pids are valid (pid type = pes)
                if (Pattern.matches("[0-9]+", p)) {

                    if (pidForPes.contains((Integer) Integer.parseInt(p))) {
                        validPidForPes.add(Integer.parseInt(p));
                    } else {
                        invalidPidForPes.add(Integer.parseInt(p));
                    }
                } else {
                    System.out.println("Argument " + p + " invalid for -pes.");
                    return null;
                }
            }
            System.out.println(
                    "\nExporting PES table of PIDs : "
                            + validPidForPes.toString().substring(1, validPidForPes.toString().length() - 1));

            // Display invalid PIDs values
            if (!invalidPidForPes.isEmpty()) {
                System.out
                        .println(
                                "\nPIDs "
                                        + invalidPidForPes.toString().substring(1,
                                                invalidPidForPes.toString().length() - 1)
                                        + " not available for PES.");
            }
            // collect PES for list of validated PIDs
            try {
                pesPackets = exportData.collectPes(ts, validPidForPes);
                return pesPackets;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Handle options and export appropriate TsPackets values
     * 
     * @return List<TsPacketsData> - All or part of tsPackets
     */
    public List<TsPacketsData> handleTsPacketsExport() {

        if (cmd.hasOption("tsPackets")) {

            if (cmd.getOptionValues("tsPackets") == null) {
                // no args
                System.out.println("\nExporting all packets");
                final List<TsPacketsData> listPackets = exportData.collectTsPackets(ts);
                return listPackets;

                // args : exporting packets for given pids
            } else {
                // collecting existing pids from the stream
                final List<Integer> usedPids = new ArrayList<Integer>();
                for (final int i : ts.getUsedPids()) {
                    usedPids.add(i);
                }

                final List<Integer> pidValid = new ArrayList<Integer>();
                final List<Integer> pidNotValid = new ArrayList<Integer>();

                // make sure that given PIDs are valid
                for (final String p : cmd.getOptionValues("tsPackets")) {
                    if (Pattern.matches("[0-9]+", p)) {

                        if (usedPids.contains(Integer.parseInt(p))) {
                            pidValid.add(Integer.parseInt(p));
                        } else {
                            pidNotValid.add(Integer.parseInt(p));
                        }
                    } else {
                        System.out.println("Argument " + p + " invalid for -pa.");
                        return null;
                    }
                }

                final List<TsPacketsData> listPackets = exportData.collectTsPackets(ts, pidValid);

                System.out.println(
                        "\nExporting packets of Pids : "
                                + pidValid.toString().substring(1, pidValid.toString().length() - 1));
                // Display invalid PIDs values
                if (!pidNotValid.isEmpty()) {
                    System.out
                            .println(
                                    "\nPackets are not available for PIDs : "
                                            + pidNotValid.toString().substring(1,
                                                    pidNotValid.toString().length() - 1)
                                            + ".");
                }
                return listPackets;
            }
        }

        // export packets in given range
        else if (cmd.hasOption("tsPacketsRange")) {

            final int min = Integer.parseInt(cmd.getOptionValues("tsPacketsRange")[0]);
            final int max = Integer.parseInt(cmd.getOptionValues(
                    "tsPacketsRange")[1]);
            System.out.println("\nExporting packets in range : [" + min + "," + max + "]");

            final List<TsPacketsData> listPackets = exportData.collectTsPackets(ts, min, max);
            return listPackets;
        } else {
            return null;
        }
    }
}