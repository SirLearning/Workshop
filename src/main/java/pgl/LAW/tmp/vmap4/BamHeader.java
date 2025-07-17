package pgl.LAW.tmp.vmap4;

public class BamHeader {
    private String command;
    String taxa;
    String runNumber;

    public BamHeader(String taxon, String runNumber) {
        this.taxa = taxon;
        this.runNumber = runNumber;
    }
}
