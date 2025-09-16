package pgl.LAW.tmp.vmap4;

/**
 * Simple data class to hold BAM header information
 * Used by TaxaBamMap to store taxa name and sequencing run number
 */
public class BamHeader {
    public final String taxa;
    public final String runNumber;
    public final String bamId;

    public BamHeader(String taxa, String runNumber, String bamId) {
        this.taxa = taxa;
        this.runNumber = runNumber;
        this.bamId = bamId;
    }

    @Override
    public String toString() {
        return "BamHeader{taxa='" + taxa + "', runNumber='" + runNumber + "'}";
    }
}

