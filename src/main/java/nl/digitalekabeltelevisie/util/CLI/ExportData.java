package nl.digitalekabeltelevisie.util.CLI;

import nl.digitalekabeltelevisie.data.mpeg.PID;
import nl.digitalekabeltelevisie.data.mpeg.TSPacket;
import nl.digitalekabeltelevisie.data.mpeg.TransportStream;
import nl.digitalekabeltelevisie.data.mpeg.dsmcc.DSMCC;
import nl.digitalekabeltelevisie.data.mpeg.pes.GeneralPesHandler;
import nl.digitalekabeltelevisie.data.mpeg.pes.GeneralPidHandler;
import nl.digitalekabeltelevisie.data.mpeg.psi.AIT;
import nl.digitalekabeltelevisie.data.mpeg.psi.AITsection;
import nl.digitalekabeltelevisie.data.mpeg.psi.BATsection;
import nl.digitalekabeltelevisie.data.mpeg.psi.CAsection;
import nl.digitalekabeltelevisie.data.mpeg.psi.DFIT;
import nl.digitalekabeltelevisie.data.mpeg.psi.EITsection;
import nl.digitalekabeltelevisie.data.mpeg.psi.INTsection;
import nl.digitalekabeltelevisie.data.mpeg.psi.NITsection;
import nl.digitalekabeltelevisie.data.mpeg.psi.NetworkSync;
import nl.digitalekabeltelevisie.data.mpeg.psi.PATsection;
import nl.digitalekabeltelevisie.data.mpeg.psi.PMTsection;
import nl.digitalekabeltelevisie.data.mpeg.psi.RCT;
import nl.digitalekabeltelevisie.data.mpeg.psi.SCTE35;
import nl.digitalekabeltelevisie.data.mpeg.psi.SDTsection;
import nl.digitalekabeltelevisie.data.mpeg.psi.SITsection;
import nl.digitalekabeltelevisie.data.mpeg.psi.TDTsection;
import nl.digitalekabeltelevisie.data.mpeg.psi.TOTsection;
import nl.digitalekabeltelevisie.data.mpeg.psi.TSDTsection;
import nl.digitalekabeltelevisie.data.mpeg.psi.UNT;
import nl.digitalekabeltelevisie.data.mpeg.psi.nonstandard.M7Fastscan;
import nl.digitalekabeltelevisie.util.CLI.beans.PesData;
import nl.digitalekabeltelevisie.util.CLI.beans.PsiData;
import nl.digitalekabeltelevisie.util.CLI.beans.TsData;
import nl.digitalekabeltelevisie.util.CLI.beans.TsPacketsData;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Collect information from a TransportStream
 */
public class ExportData {

    /**
     * Export collected data from parsed file in JSON file
     * 
     * @param tsData data parsed from input ts file
     * @param target name of file to export to
     */
    public void export(final TsData tsData, final String target) {

        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(Paths.get(target).toFile(), tsData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Collect PSI information from a specified TS
     * 
     * @param ts - transportStream to be parsed
     * @return PsiData
     */
    public PsiData collectPsiInformation(final TransportStream ts) {

        final PATsection[] pat = (ts.getPsi().getPat().getPATsections());
        final CAsection[] cat = (ts.getPsi().getCat().getCat()); // not sure if working

        // BAT
        final Map<Integer, BATsection[]> mapBat = ts.getPsi().getBat().getNetworks();
        final List<BATsection> listBat = new ArrayList<BATsection>();
        for (final BATsection[] batSections : mapBat.values()) {

            for (final BATsection badSection : batSections) {
                listBat.add(badSection);
            }
        }
        final List<BATsection> bat = (listBat);

        // TSDT
        final TSDTsection[] tsdt = (ts.getPsi().getTsdt().getTsdt());

        // PMT
        final Map<Integer, PMTsection[]> mapPmts = ts.getPsi().getPmts().getPmts();
        final List<PMTsection> listPmt = new ArrayList<PMTsection>();
        for (final PMTsection[] pmtSections : mapPmts.values()) {

            for (final PMTsection pmtSection : pmtSections) {
                listPmt.add(pmtSection);
            }
        }
        final List<PMTsection> pmt = (listPmt);

        // NIT
        final Map<Integer, NITsection[]> network = ts.getPsi().getNit().getNetworks();
        final List<NITsection> listNit = new ArrayList<NITsection>();
        for (final NITsection[] nitSections : network.values()) {

            for (final NITsection nitSection : nitSections) {
                listNit.add(nitSection);
            }
        }
        final List<NITsection> nit = (listNit);

        // SDT
        final SDTsection[] sdt = (ts.getPsi().getSdt().getActualTransportStreamSDT());

        // EIT
        final Map<Integer, TreeMap<Integer, TreeMap<Integer, TreeMap<Integer, EITsection[]>>>> newEit = ts.getPsi()
                .getEit()
                .getNewEit();
        final List<EITsection> listEit = new ArrayList<EITsection>();

        for (final TreeMap<Integer, TreeMap<Integer, TreeMap<Integer, EITsection[]>>> eitMap : newEit.values()) {

            for (final Map.Entry<Integer, TreeMap<Integer, TreeMap<Integer, EITsection[]>>> eitTreeMap : eitMap
                    .entrySet()) {

                for (final Map.Entry<Integer, TreeMap<Integer, EITsection[]>> eitTreeMap2 : eitTreeMap.getValue()
                        .entrySet()) {
                    for (final Map.Entry<Integer, EITsection[]> eitTreeMap3 : eitTreeMap2.getValue().entrySet()) {

                        for (final EITsection eitSection : eitTreeMap3.getValue()) {

                            if (eitSection != null) {
                                listEit.add(eitSection);
                            }
                        }
                    }
                }
            }
        }
        final List<EITsection> eit = (listEit);

        final List<TDTsection> tdt = (ts.getPsi().getTdt().getTdtSectionList());
        final List<TOTsection> tot = (ts.getPsi().getTot().getTotSectionList());
        final SITsection[] sit = (ts.getPsi().getSit().getSit()); // not sure if working
        final NetworkSync networkSync = (ts.getPsi().getNetworkSync());

        // INT
        final Map<Integer, INTsection[]> intNetwork = ts.getPsi().getInt().getNetworks();
        final List<INTsection> listInt = new ArrayList<INTsection>();
        for (final INTsection[] intSections : intNetwork.values()) {
            for (final INTsection intSection : intSections) {
                listInt.add(intSection);
            }
        }
        final List<INTsection> intSection = (listInt);

        // UNT
        final Map<Integer, UNT> unts = ts.getPsi().getUnts().getUnts();
        final List<UNT> listUnt = new ArrayList<UNT>();
        for (final UNT unt : unts.values()) {
            listUnt.add(unt);
        }
        final List<UNT> unt = (listUnt);

        // AIT
        final List<AITsection> listAitSection = new ArrayList<AITsection>();
        final Map<Integer, AIT> mapAit = ts.getPsi().getAits().getAits();
        for (final AIT ait : mapAit.values()) {
            final Map<Integer, AITsection[]> mapAitSection = ait.getAits();

            for (final AITsection[] sec : mapAitSection.values()) {
                for (final AITsection s : sec) {

                    System.out.println(s.getSectionLength());
                    listAitSection.add(s);
                }
            }
        }
        final List<AITsection> ait = (listAitSection);

        // RCT
        final Map<Integer, RCT> rcts = ts.getPsi().getRcts().getRcts();
        final List<RCT> listRct = new ArrayList<RCT>();
        for (final RCT rct : rcts.values()) {
            listRct.add(rct);
        }
        final List<RCT> rct = (listRct);

        // DSMCC
        final Map<Integer, DSMCC> dsmccs = ts.getPsi().getDsms().getDsmccs();
        final List<DSMCC> listDsmcc = new ArrayList<DSMCC>();
        for (final DSMCC dsmcc : dsmccs.values()) {
            listDsmcc.add(dsmcc);
        }
        final List<DSMCC> dsmcc = (listDsmcc);

        // SCTE35
        final SCTE35 scte = (ts.getPsi().getScte35_table());

        // DFIT
        final Map<Integer, DFIT> dfits = ts.getPsi().getDfit_table().getDfits();
        final List<DFIT> listDfit = new ArrayList<DFIT>();
        for (final DFIT dfit : dfits.values()) {
            listDfit.add(dfit);
        }
        final List<DFIT> dfit = (listDfit);

        // M7Fastscan
        M7Fastscan m7Fastscan = null;
        if (ts.getPsi().getM7fastscan().getOntSections() != null) {
            m7Fastscan = (ts.getPsi().getM7fastscan());
        }

        final PsiData psi = new PsiData(pat, cat, bat, tsdt, pmt, nit, sdt, eit, tdt, tot, sit, networkSync, intSection,
                unt, ait, rct, dsmcc, scte, dfit, m7Fastscan);
        return psi;
    }

    /**
     * Collect PID information from a specified TS
     * 
     * @param ts    - transportStream to be parsed
     * @param pidNo - list of PIDs number to collect information from - can be null
     * @return List<PID>
     */
    public List<PID> collectPidsInformation(final TransportStream ts, final List<Integer> pidNo) {
        // PIDs management
        final List<PID> allPids = new ArrayList<PID>();

        if (pidNo == null) {
            // loop throughout pids elements from TS
            for (final PID pid : ts.getPids()) {
                if (pid != null) {
                    final int pidNumber = pid.getPid();
                    // add info from pid to the dumped list.
                    allPids.add(ts.getPids()[pidNumber]);
                }
            }
            // if specific pids are given
        } else {
            for (final PID pid : ts.getPids()) {
                if (pid != null) {
                    for (final Integer pArg : pidNo) {
                        if (pid.getPid() == pArg) {
                            allPids.add(ts.getPids()[pArg]);
                            break;
                        }
                    }
                }

            }
        }
        return allPids;
    }

    /**
     * Collect all packets from a TS
     * 
     * @param ts - transportStream to be parsed
     * @return List<TsPacketsData> - All TS packets
     */
    public List<TsPacketsData> collectTsPackets(final TransportStream ts) {
        // call generic method with ts - return all packets
        return collectTsPackets(ts, null, null, null);
    }

    /**
     * Collect packets from specified PIDs of a TS
     * 
     * @param ts   - transportStream to be parsed
     * @param pids - list of PIDs number to collect information from - can be null
     * @return - List<TsPacketsData> from specified PIDs
     */
    public List<TsPacketsData> collectTsPackets(final TransportStream ts, final List<Integer> pids) {
        // call generic method with ts and PIDs - return packets from given PIDs
        return collectTsPackets(ts, null, null, pids);
    }

    /**
     * Collect packets from a given range of a TS
     * 
     * @param ts  - transportStream to be parsed
     * @param min - minimal value of the range
     * @param max - maximal value of the range
     * @return - List<TsPacketsData> contained in range [min,max]
     */
    public List<TsPacketsData> collectTsPackets(final TransportStream ts, final Integer min, final Integer max) {
        // call generic method with ts and range values - return packets in range
        return collectTsPackets(ts, min, max, null);
    }

    /**
     * Collect TS packets - Handle collect of packets with given parameters
     * 
     * @param ts   - transportStream to be parsed
     * @param min  - minimal value of the range - can be null
     * @param max  - maximal value of the range - can be null
     * @param pids - list of PIDs number to collect information from - can be null
     * @return - List<TsPacketsData> depending on the input parameters
     */
    public List<TsPacketsData> collectTsPackets(final TransportStream ts, final Integer min, final Integer max,
            final List<Integer> pids) {

        // Unsupported operation
        if (min != null && max != null && pids != null) {
			System.out.println("\nUnsupported operation.");
            return null;
        }

        final List<TSPacket> packets = new ArrayList<TSPacket>();
        List<Integer> exportedPid = new ArrayList<Integer>();
        final List<TsPacketsData> packetsDataList = new ArrayList<TsPacketsData>();
        final int nbPackets = ts.getNo_packets();

        // Packets in range [min-max]
        if (min != null && max != null) {

            // Add packets in specified range
            for (int i = min; i <= max; i++) {
                packets.add(ts.getTSPacket(i));
                // get list of pids represented by the packets
                if (!exportedPid.contains((int) ts.getTSPacket(i).getPID())) {
                    exportedPid.add((int) (ts.getTSPacket(i).getPID()));
                }
            }
            // sort in ascendant order
            Collections.sort(exportedPid);
            // set data
            final TsPacketsData packetsData = new TsPacketsData(exportedPid, packets);
            packetsDataList.add(packetsData);
            return packetsDataList;

            // packets from pid list
        } else if (pids != null) {
            if (!pids.isEmpty()) {

                for (final int p : pids) {

                    // Reset pid and packets list for each pids
                    exportedPid = new ArrayList<Integer>();
                    exportedPid.add(p);

                    // Add packets that are from specified pid p
                    for (int i = 0; i < nbPackets; i++) {
                        if (ts.getPacket_pid(i) == p) {
                            packets.add(ts.getTSPacket(i));
                        } else {
                            continue;
                        }
                    }
                    // Set data
                    final TsPacketsData packetsData = new TsPacketsData(exportedPid, packets);
                    packetsDataList.add(packetsData);
                }
            }
            return packetsDataList;
        }
        // All packets
        else {

            // Add all packets from the ts
            for (int i = 0; i < nbPackets; i++) {
                packets.add(ts.getTSPacket(i));
                if (!exportedPid.contains((int) ts.getTSPacket(i).getPID())) {
                    exportedPid.add((int) (ts.getTSPacket(i).getPID()));
                }
            }
            // Sort and set data
            Collections.sort(exportedPid);
            final TsPacketsData packetsData = new TsPacketsData(exportedPid, packets);
            packetsDataList.add(packetsData);
            return packetsDataList;

        }
    }

    /**
     * Collect PES
     * 
     * @param ts    - transportStream to be parsed
     * @param pidNo - list of PIDs number to collect information from - can be null
     * @return - List<PesData> - PES data
     * @throws IOException
     */
    public List<PesData> collectPes(final TransportStream ts, final List<Integer> pidNo) throws IOException {

        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        // SET UP
        final List<Integer> pidWPes = getPidWPes(ts);
        final Map<Integer, GeneralPidHandler> map = new HashMap<>();

        for (final int pid : pidWPes) {
            map.put(pid, ts.getPID(pid).getPidHandler());
        }

        ts.parsePidStreams(map);
        // END SET UP

        final List<PesData> pesPackets = new ArrayList<PesData>();

        // Collect Pes packets for all PIDs with type = PES
        if (pidNo == null) {

            for (final int pid : pidWPes) {
                final PID pidData = ts.getPID(pid);
                final GeneralPidHandler pidHandler = pidData.getPidHandler();
                final GeneralPesHandler pesHandler = (GeneralPesHandler) pidHandler;

                final PesData pes = new PesData(pid, pesHandler.getPesPackets());
                pesPackets.add(pes);
            }
            // Collect Pes packets for specified PIDs
        } else {
            for (final int pid : pidNo) {
                for (final Integer i : pidWPes) {
                    // Check if PID contains PES packets
                    if (pid == i) {
                        final PID pidData = ts.getPID(pid);
                        final GeneralPidHandler pidHandler = pidData.getPidHandler();
                        final GeneralPesHandler pesHandler = (GeneralPesHandler) pidHandler;

                        final PesData pes = new PesData(pid, pesHandler.getPesPackets());
                        pesPackets.add(pes);
                    } else {
                        continue;
                    }
                }
            }
        }
        return pesPackets;

    }

    /**
     * Collect PIDs containing PES data
     * 
     * @param ts - transportStream to be parsed
     * @return List<Integer> - PIDs that contained PES
     */
    public List<Integer> getPidWPes(final TransportStream ts) {

        final List<PID> allPids = collectPidsInformation(ts, null);
        final List<Integer> pidWPesList = new ArrayList<Integer>();

        // Check type of all pids
        for (final PID pid : allPids) {
            // Add pid if type is PES
            if (pid.getType() == PID.PES) {
                pidWPesList.add((pid.getPid()));
            } else {
                continue;
            }
        }
        return pidWPesList;
    }
}