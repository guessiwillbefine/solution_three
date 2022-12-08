package parser;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import static java.lang.System.currentTimeMillis;
/*
    results (average value of 10 measures) (ryzen5600x 6-core 12-threads):
        10 threads ~ 612ms
        8 threads  ~ 644ms
        6 threads  ~ 626ms
        4 threads  ~ 662ms
        2 threads  ~ 708ms
        1 thread   ~ 837ms
 */
public class Parser {
    private static final String TARGET_PATH = "src/main/resources/input";
    private static final String OUTPUT_PATH = "src/main/resources/output/";

    public static void main(String[] args) throws FileNotFoundException {
        startMultiThreadingParse(10);
    }
    public static void startMultiThreadingParse(int threadAmount) throws FileNotFoundException {
        File sourceDir = new File(TARGET_PATH);
        if (!sourceDir.exists() || !sourceDir.isDirectory()) throw new FileNotFoundException();
        ExecutorService executor = Executors.newFixedThreadPool(threadAmount);
        long start = currentTimeMillis();
        for (File file : sourceDir.listFiles()) {
            CompletableFuture.supplyAsync(() -> file, executor)
                    .thenAccept(Parser::parseFile)
                    .thenAccept(x-> System.out.println(Thread.currentThread().getName() + " done"));

        }
        executor.shutdown();
        try {
            executor.awaitTermination(1,TimeUnit.DAYS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }
        System.out.println(threadAmount + " thread(s) -> " + (currentTimeMillis() - start));
    }


    public static void parseFile(File file) {
        List<Violation> violations;
        if (file.getName().toLowerCase().endsWith(".xml")) {
            violations = parseXML(file);
            objectToJson(convert(violations), file);
        } else {
            violations = parseJSON(file);
            objectToXML(convert(violations), file);
        }
    }

    private static List<Violation> parseXML(File file) {
        List<Violation> participantJsonList;
        XmlMapper mapper = new XmlMapper();
        try {
            participantJsonList = mapper.readValue(file, new TypeReference<>() {
            });
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
        return participantJsonList;
    }

    private static List<Violation> parseJSON(File file) {
        List<Violation> participantJsonList;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            participantJsonList = objectMapper.readValue(file, new TypeReference<>() {});
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
        return participantJsonList;
    }

    private static List<ViolationDTO> convert(List<Violation> violations) {
        return violations.stream()
                .collect(Collectors.groupingBy(Violation::getType,
                        Collectors.summarizingDouble(Violation::getFineAmount)))
                .entrySet()
                .stream()
                .map(x -> new ViolationDTO(x.getKey(), x.getValue().getSum()))
                .toList();
    }


    private static void objectToJson(List<ViolationDTO> violations, File file) {
        ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        try {
            objectMapper.writer().writeValue(new File(OUTPUT_PATH + file.getName().split("\\.")[0] + "output.json"), violations);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void objectToXML(List<ViolationDTO> violations, File file) {
        XmlMapper mapper = new XmlMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        try {
            mapper.writer().writeValue(new File(OUTPUT_PATH + file.getName().split("\\.")[0] + "output.xml"), violations);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
