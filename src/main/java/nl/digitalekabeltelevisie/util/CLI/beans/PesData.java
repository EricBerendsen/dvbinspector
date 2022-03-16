package nl.digitalekabeltelevisie.util.CLI.beans;

import java.util.List;

import nl.digitalekabeltelevisie.data.mpeg.PesPacketData;

public class PesData {

    private int pid;
    private List<PesPacketData> pes;

    public PesData(int pid, List<PesPacketData> pes) {
        this.pid = pid;
        this.pes = pes;
    }

    public int getPid() {
        return pid;
    }

    public List<PesPacketData> getPes() {
        return pes;
    }
}
