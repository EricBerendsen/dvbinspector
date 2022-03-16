package nl.digitalekabeltelevisie.util.CLI.beans;

import java.util.List;

import nl.digitalekabeltelevisie.data.mpeg.PID;
import nl.digitalekabeltelevisie.data.mpeg.TransportStream;

public class TsData {

    final private TransportStream ts;
    private PsiData psi;
    private List<PID> pids;
    private List<TsPacketsData> tsPackets;
    private List<PesData> pes;

    public TsData(TransportStream ts) {
        this.ts = ts;
    }

    public TransportStream getTs() {
        return ts;
    }

    public PsiData getPsi() {
        return psi;
    }

    public void setPsi(PsiData psi) {
        this.psi = psi;
    }

    public List<PID> getPids() {
        return pids;
    }

    public void setPids(List<PID> pids) {
        this.pids = pids;
    }

    public List<TsPacketsData> getTsPackets() {
        return tsPackets;
    }

    public void setTsPackets(List<TsPacketsData> tsPackets) {
        this.tsPackets = tsPackets;
    }

    public List<PesData> getPes() {
        return pes;
    }

    public void setPes(List<PesData> pes) {
        this.pes = pes;
    }
}
