package nl.digitalekabeltelevisie.util.CLI.beans;

import java.util.List;

import nl.digitalekabeltelevisie.data.mpeg.TSPacket;

public class TsPacketsData {

    private List<Integer> pids;
    private List<TSPacket> tsPackets;

    public TsPacketsData(List<Integer> pids, List<TSPacket> tsPackets) {
        this.pids = pids;
        this.tsPackets = tsPackets;
    }

    public List<Integer> getPids() {
        return pids;
    }

    public List<TSPacket> getTsPackets() {
        return tsPackets;
    }
}
