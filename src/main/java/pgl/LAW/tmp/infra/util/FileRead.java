package pgl.LAW.tmp.infra.util;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileRead {
    private String fileS;
    private String outputFile;

    public FileRead(String fileS, String outputFile) {
        this.fileS = fileS;
        this.outputFile = outputFile;
        try {
            readFiles(fileS, outputFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void readFiles(String fileS, String outputFile) throws IOException {
        Path filePath = Paths.get(fileS);
        File[] files = filePath.toFile().listFiles();
        BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
        for (int i = 0; i < files.length; i++) {
            BufferedReader inputStream = new BufferedReader(new FileReader(files[i]));
            String fileName = files[i].getName();
            if (!fileName.startsWith("P")) continue;
            if (fileName.contains("dist")) continue;
            String line;
            while ((line = inputStream.readLine()) != null) {
                if (line.startsWith("total")) {
                    String[] elements = line.split("\t");
                    if (elements.length > 3) {
                        bw.write(fileName + "\t" + elements[3] + "\n");
                    } else {
                        System.err.println("Error: Not enough elements in line: " + line + fileName);
                    }
                }
            }
            inputStream.close();
        }
        bw.close();
    }

    public static void createGoldenDictInput(String fileS, String outputFile) throws IOException {
        BufferedReader inputStream = new BufferedReader(new FileReader(fileS));
        BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
        String line;
        while ((line = inputStream.readLine()) != null) {
            if (!line.matches("\\s*<.*")) {
                String[] elements = line.split("\\s+");
                for (String element : elements) {
                    if (element.endsWith(")")) continue;
                    element = element.split(",")[0];
                    if (element.isEmpty()) continue;
                    bw.write("<headword>" + element + "</headword>\n");
                }
            } else bw.write(line + "\n");
        }
        inputStream.close();
        bw.close();
    }

    public static void main(String[] args) throws IOException {
        createGoldenDictInput("D:\\Zheng\\Documents\\2_NBS\\Java\\LAW\\Workshop\\src\\main\\resources\\1.xml",
                "D:\\Zheng\\Documents\\2_NBS\\Java\\LAW\\Workshop\\src\\main\\resources\\2.xml");
    }
}
