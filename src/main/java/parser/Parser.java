package parser;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static java.lang.System.currentTimeMillis;

/*
    results (average value of 10 measures):
        8 threads  ~ 634ms
        4 threads  ~ 662ms
        2 threads  ~ 830ms

 */
public class Parser {
    private static final Lock LOCK = new ReentrantLock();
    private static final List<ViolationDTO> violationDTOs = new ArrayList<>();
    private static final String TARGET_PATH = "src/main/resources/test/";
    private static final String OUTPUT_PATH = "src/main/resources/output/";

    public static void main(String[] args) throws FileNotFoundException {
        startMultiThreadingParse(6);
    }

    public static void startMultiThreadingParse(int threadAmount) throws FileNotFoundException {
        File sourceDir = new File(TARGET_PATH);
        if (!sourceDir.exists() || !sourceDir.isDirectory()) throw new FileNotFoundException();
        ExecutorService executor = Executors.newFixedThreadPool(threadAmount);
        long start = currentTimeMillis();
        for (File file : Objects.requireNonNull(sourceDir.listFiles())) {
            CompletableFuture.supplyAsync(() -> file, executor)
                    .thenAccept(Parser::parseFile)
                    .thenAccept(x -> System.out.println(Thread.currentThread().getName() + " done"));
        }
        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        List<ViolationDTO> reducedData = reduceList(violationDTOs);
        reducedData.forEach(System.out::println);
        objectToJson(reducedData, new File(OUTPUT_PATH + "/result.json"));
        System.out.println(threadAmount + " thread(s) -> " + (currentTimeMillis() - start));
    }


    public static void parseFile(File file) {
        List<Violation> violations;
        if (file.getName().toLowerCase().endsWith(".xml")) {
            violations = parseXML(file);
            collect(convert(violations));
        } else {
            violations = parseJSON(file);
            collect(convert(violations));
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
            participantJsonList = objectMapper.readValue(file, new TypeReference<>() {
            });
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
        return participantJsonList;
    }

    private static List<ViolationDTO> convert(List<Violation> violations) {
        return violations.parallelStream()
                .collect(Collectors.groupingBy(Violation::getType,
                        Collectors.summarizingDouble(Violation::getFineAmount)))
                .entrySet()
                .stream()
                .map(x -> new ViolationDTO(x.getKey(), x.getValue().getSum()))
                .sorted(Comparator.comparing(ViolationDTO::getAmount).reversed())
                .toList();
    }
    private static List<ViolationDTO> reduceList(List<ViolationDTO> violations) {
        return violations.parallelStream()
                .collect(Collectors.groupingBy(ViolationDTO::getType,
                        Collectors.summarizingDouble(ViolationDTO::getAmount)))
                .entrySet()
                .stream()
                .map(x -> new ViolationDTO(x.getKey(), x.getValue().getSum()))
                .sorted(Comparator.comparing(ViolationDTO::getAmount).reversed())
                .toList();
    }

    private static void collect(List<ViolationDTO> newValues) {
        LOCK.lock();
        violationDTOs.addAll(newValues);
        LOCK.unlock();
    }


    private static void objectToJson(List<ViolationDTO> violations, File file) {
        ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        try {
            objectMapper.writer().writeValue(file, violations);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void objectToXML(List<ViolationDTO> violations, File file) {
        XmlMapper mapper = new XmlMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        try {
            mapper.writer().writeValue(file, violations);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
