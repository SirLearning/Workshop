package pgl.LAW.tmp.vmap4;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;
import pgl.AppAbstract;
import pgl.infra.utils.IOUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class TaxaBamMap extends AppAbstract {
    private String bamFiles = "";
    private String depthS;
    private String bamS;
    private String outFile;
    private String taxaRunFile;

    public TaxaBamMap(String[] args) {
        creatAppOptions();
        retrieveAppParameters(args);
        createTaxaBamMap();
    }

    public static void main(String[] args) {
        String[] wapArgs = new String[] {
                "-d", "/data/home/dazheng/vmap4/01ctDepth",
                "-b", "/data/home/dazheng/vmap4/00data/bam_ABD",
                "-o", "/data/home/dazheng/vmap4/WAP.taxaBamMap.txt",
                "-o", "/data/home/dazheng/vmap4/WAP.taxaRunMap.txt"
        };
        String[] subArgs = new String[] {
                "-d", "/data/home/dazheng/vmap4/01ctDepth",
                "-b", "/data/home/dazheng/vmap4/02subBams",
                "-o", "/data/home/dazheng/vmap4/test.taxaBamMap.txt"
        };
        String[] chr1Args = new String[] {
                "-d", "/data/dazheng/01projects/vmap4/00data/04depth/01A",
                "-b", "/data/dazheng/01projects/vmap4/00data/02bam/bam1/A",
                "-o", "/data/dazheng/01projects/vmap4/00data/05taxaBamMap/chr1.taxaBamMap.txt",
                "-t", "/data/dazheng/01projects/vmap4/00data/05taxaBamMap/chr1.taxaRunMap.txt"
        };
        String[] testArgs = new String[] {
                "-d", "/data/dazheng/01projects/vmap4/01testData/02bamDepth/01test",
                "-b", "/data/dazheng/01projects/vmap4/01testData/02bamDepth/01test",
                "-o", "/data/dazheng/01projects/vmap4/00data/05taxaBamMap/test.taxaBamMap.txt",
                "-t", "/data/dazheng/01projects/vmap4/00data/05taxaBamMap/test.taxaRunMap.txt"
        };
        // new TaxaBamMap(wapArgs);
        // new TaxaBamMap(subArgs);
//        new TaxaBamMap(chr1Args);
//        new TaxaBamMap(testArgs);

        new TaxaBamMap(args);
    }

    private static List<File> getPureBamFiles(List<File> input) {
        List<File> output = new ArrayList<>();
        for (File file : input) {
            if (file.getName().endsWith("bam")) output.add(file);
        }
        return output;
    }

    private String[] getBamFilesFromFile() throws IOException {
        BufferedReader reader = IOUtils.getTextReader(bamFiles);
        List<String> bamList = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            bamList.add(line);
        }
        return bamList.toArray(new String[0]);
    }

    private static File findBamBasedOnDepth(List<File> bams, File depth) {
        String factor = depth.getName().split("\\.")[0];
        File returnBam = null;
        for (File bam: bams) {
            if (bam.getName().startsWith(factor)) returnBam = bam;
        }
        return returnBam;
    }

    @Override
    public void creatAppOptions() {
        options.addOption("f", true, "Bam files list file");
        options.addOption("d", true, "Input depth files data site");
        options.addOption("b", true, "Input bam files data site");
        options.addOption("o", true, "Output taxaBamMap.txt");
        options.addOption("t", true, "Output file of taxa and sequencing run number");
    }

    @Override
    public void retrieveAppParameters(String[] args) {
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine line = parser.parse(options, args);
            this.bamFiles = line.getOptionValue("f");
            this.depthS = line.getOptionValue("d");
            this.bamS = line.getOptionValue("b");
            this.outFile = line.getOptionValue("o");
            this.taxaRunFile = line.getOptionValue("t");
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void printInstructionAndUsage() {

    }

    private void writeBamHeader(String outFile) throws IOException {
        File file = new File(outFile);
        boolean needHeader = !file.exists() || file.length() == 0;
        if (needHeader) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
                bw.write("Taxa\tCoverage-Of-All-Bams\tBams(A list of bams of the taxon, seperated by the delimiter of Tab)\n");
            }
        }
    }

    private void writeOtherHeader(String outFile) throws IOException {
        File file = new File(outFile);
        boolean needHeader = !file.exists() || file.length() == 0;
        if (needHeader) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
                bw.write("Taxa_id\tRun_id\tBams_id\n");
            }
        }
    }

    private void createTaxaBamMap() {
        String cmd = "samtools view -H ";
        List<File> depthList = IOUtils.getFileListInDirContains(depthS, "summary");
        List<File> bamList = List.of();
        try (
                BufferedWriter bw = new BufferedWriter(new FileWriter(outFile, true));
                BufferedWriter bw1 = new BufferedWriter(new FileWriter(taxaRunFile, true))
        ) {   // append content to the file before
//            writeBamHeader(outFile);
//            writeOtherHeader(taxaRunFile);
            if (bamFiles == null || bamFiles.isEmpty()) {
                bamList = getPureBamFiles(IOUtils.getFileListInDirContains(bamS, "bam"));
            } else {
                String[] bamFiles = getBamFilesFromFile();
                for (String bamFile : bamFiles) {
                    bamList.add(new File(bamS, bamFile));
                }
            }
            File depth;
            File bam;
            ExecutorService pool = Executors.newFixedThreadPool(10);
            List<Future<BamHeader>> futureList = new ArrayList<>();
            for (File file : depthList) {
                depth = file;
                bam = findBamBasedOnDepth(bamList, depth);
                BufferedReader br = IOUtils.getTextReader(depth.getAbsolutePath());
                TaxonRead tr = new TaxonRead(cmd + bam.getAbsolutePath());
                Future<BamHeader> f = pool.submit(tr);
                BamHeader tmpBamHeader;
                futureList.add(f);
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.startsWith("total")) {
                        String[] elements = line.split("\t");
                        //System.out.println(elements[3]);
                        //System.out.println(f.get().taxa);
                        //System.out.println(f.get().taxa + "\t" + elements[3] + "\t" + bam.getAbsolutePath());
                        tmpBamHeader = f.get();
//                        if (tmpBamHeader.taxa != null) {
////                            if (tmpBamHeader.taxa.equals("Triticum")) {
////                                bw.write(tmpBamHeader.bamId + "\t" + elements[3] + "\t" + bam.getAbsolutePath() + "\n");
////                                continue;
////                            }
//                            bw.write(tmpBamHeader.bamId + "\t" + elements[3] + "\t" + bam.getAbsolutePath() + "\n");
////                        } else if (tmpBamHeader.bamId != null) {
////                            bw.write(tmpBamHeader.bamId + "\t" + elements[3] + "\t" + bam.getAbsolutePath() + "\n");
//                        } else {
//                            System.err.println("There is a sample without bam id: " + bam.getName());
//                        }

                        bw.write(tmpBamHeader.bamId + "\t" + elements[3] + "\t" + bam.getAbsolutePath() + "\n");
                        bw1.write(tmpBamHeader.taxa + "\t" + tmpBamHeader.runNumber + "\t" + tmpBamHeader.bamId + "\n");
                    }
                }
                br.close();
                //pool.shutdownNow();
            }
            pool.close();
        } catch (IOException | ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    static class TaxonRead implements Callable<BamHeader> {
        String command;
        public TaxonRead (String command) {
            this.command = command;
        }

        @Override
        public BamHeader call() throws Exception {
            BamHeader bh;
            String taxon = null;
            String run = null;
            String bamId = null;
            try {
                Runtime rt = Runtime.getRuntime();
                Process p = rt.exec(command);
                System.out.println(command);
                String temp;
                BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
                //System.out.println(br.readLine());
                while ((temp = br.readLine()) != null) {
                    if (temp.startsWith("@RG")) {
                        String[] elements = temp.split("\t");
                        for (String element : elements) {
                            if (element.startsWith("SM")) {
                                taxon = element.split(":")[1];
                                break;
                            }
                        }
                    }
                    if (temp.startsWith("@PG")) {
                        String[] elements = temp.split("\t");
                        for (String element : elements) {
                            if (element.startsWith("CL") && element.endsWith(".gz") && run == null) {
                                run = element.split("/")[element.split("/").length - 1].split("_R2")[0];
                            }
                            if (element.startsWith("CL:samtools view -H") && element.endsWith(".bam") && bamId == null) {
                               String[] parts1 = element.split("\\s+");
                               if (parts1.length > 0) {
                                   String[] parts2 = parts1[parts1.length - 1].split("/");
                                   String parts3 = parts2[parts2.length - 1].split("\\.")[0];
                                   if (parts3.endsWith("_rmdup")) {
                                       parts3 = parts3.replace("_rmdup", "");
                                   }
                                   if (parts3.endsWith("_deduped")) {
                                       parts3 = parts3.replace("_deduped", "");
                                   }
                                   bamId = parts3;
                               }
                            }
                        }
                    }
                }
                bh = new BamHeader(taxon, run, bamId);
                p.waitFor();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return bh;
        }
    }
}
