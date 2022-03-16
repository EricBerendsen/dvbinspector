package nl.digitalekabeltelevisie.util.CLI.beans;

import java.util.List;

import nl.digitalekabeltelevisie.data.mpeg.dsmcc.DSMCC;
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

public class PsiData {

    private PATsection[] pat;
    private CAsection[] cat;
    private List<BATsection> bat;
    private TSDTsection[] tsdt;
    private List<PMTsection> pmt;
    private List<NITsection> nit;
    private SDTsection[] sdt;
    private List<EITsection> eit;
    private List<TDTsection> tdt;
    private List<TOTsection> tot;
    private SITsection[] sit;
    private NetworkSync networkSync;
    private List<INTsection> intSection;
    private List<UNT> unt;
    private List<AITsection> ait;
    private List<RCT> rct;
    private List<DSMCC> dsmcc;
    private SCTE35 scte;
    private List<DFIT> dfit;
    private M7Fastscan m7Fastscan;

    public PsiData(PATsection[] pat, CAsection[] cat, List<BATsection> bat, TSDTsection[] tsdt, List<PMTsection> pmt,
            List<NITsection> nit, SDTsection[] sdt, List<EITsection> eit, List<TDTsection> tdt, List<TOTsection> tot,
            SITsection[] sit, NetworkSync networkSync, List<INTsection> intSection, List<UNT> unt, List<AITsection> ait,
            List<RCT> rct, List<DSMCC> dsmcc, SCTE35 scte, List<DFIT> dfit, M7Fastscan m7Fastscan) {
        this.pat = pat;
        this.cat = cat;
        this.bat = bat;
        this.tsdt = tsdt;
        this.pmt = pmt;
        this.nit = nit;
        this.sdt = sdt;
        this.eit = eit;
        this.tdt = tdt;
        this.tot = tot;
        this.sit = sit;
        this.networkSync = networkSync;
        this.intSection = intSection;
        this.unt = unt;
        this.ait = ait;
        this.rct = rct;
        this.dsmcc = dsmcc;
        this.scte = scte;
        this.dfit = dfit;
        this.m7Fastscan = m7Fastscan;
    }

    public PATsection[] getPat() {
        return pat;
    }

    public CAsection[] getCat() {
        return cat;
    }

    public List<BATsection> getBat() {
        return bat;
    }

    public TSDTsection[] getTsdt() {
        return tsdt;
    }

    public List<PMTsection> getPmt() {
        return pmt;
    }

    public List<NITsection> getNit() {
        return nit;
    }

    public SDTsection[] getSdt() {
        return sdt;
    }

    public List<EITsection> getEit() {
        return eit;
    }

    public List<TDTsection> getTdt() {
        return tdt;
    }

    public List<TOTsection> getTot() {
        return tot;
    }

    public SITsection[] getSit() {
        return sit;
    }

    public NetworkSync getNetworkSync() {
        return networkSync;
    }

    public List<INTsection> getIntSection() {
        return intSection;
    }

    public List<AITsection> getAit() {
        return ait;
    }

    public List<RCT> getRct() {
        return rct;
    }

    public List<DFIT> getDfit() {
        return dfit;
    }

    public List<UNT> getUnt() {
        return unt;
    }

    public M7Fastscan getM7Fastscan() {
        return m7Fastscan;
    }

    public List<DSMCC> getDsmcc() {
        return dsmcc;
    }

    public SCTE35 getScte() {
        return scte;
    }
}
