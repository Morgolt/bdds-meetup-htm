package com.epam.bdcc.meetup.htm;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.numenta.nupic.Parameters;
import org.numenta.nupic.algorithms.Anomaly;
import org.numenta.nupic.algorithms.SpatialPooler;
import org.numenta.nupic.algorithms.TemporalMemory;
import org.numenta.nupic.encoders.DateEncoder;
import org.numenta.nupic.encoders.GeospatialCoordinateEncoder;
import org.numenta.nupic.encoders.MultiEncoder;
import org.numenta.nupic.network.Inference;
import org.numenta.nupic.network.Network;
import org.numenta.nupic.network.Persistence;
import org.numenta.nupic.network.PersistenceAPI;
import org.numenta.nupic.serialize.SerialConfig;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

public class AnomalyDetection {
    private static final MultiEncoder ENCODER = createEncoder();
    private static final String NETWORK_FILENAME_PATTERN = "%s_network";
    private static final String OUTPUT_FILENAME_PATTERN = "./output/%s_scores.csv";
    private static final String TRAINED_NETWORK_PATH = "trained_network";
    private static final String DIR_PATH = "Documents\\network";
    private static final PersistenceAPI PERSISTENCE_API = Persistence.get();
    private static final AtomicInteger counter = new AtomicInteger(0);
    private static Logger LOGGER = Logger.getRootLogger();
    private static final AtomicReference<String> prevSequence = new AtomicReference<>("");

    public static void main(String[] args) throws IOException {
        LOGGER.addAppender(new ConsoleAppender());
        LOGGER.setLevel(Level.INFO);
        switch (args[0]) {
            case "eval":
                try (Stream<Path> stream = Files.list(Paths.get("./data"))) {
                    stream.parallel().forEach(AnomalyDetection::runAnomalyDetection);
                }
                break;
            case "train":
                SerialConfig PERSISTENCE_CONFIG = new SerialConfig(
                        TRAINED_NETWORK_PATH,
                        DIR_PATH,
                        null
                );
                PERSISTENCE_API.setConfig(PERSISTENCE_CONFIG);
                LOGGER.setLevel(Level.DEBUG);
                processCSVFile(Paths.get(args[1]), Paths.get("output/reversed_15s_10seq.csv"), false, createNetwork());
                break;
            default:
                throw new RuntimeException("Choose mode of execution: eval or train");
        }
    }

    private static Network createNetwork() {
        Parameters allParams = HTMParameters.getNetworkParameters();
        return Network
                .create("GEOAnomalyDetectionNetwork", allParams)
                .add(Network.createRegion("MainRegion")
                        .add(Network.createLayer("Layer", allParams)
                                .add(Anomaly.create(HTMParameters.getAnomalyParameters()))
                                .add(new SpatialPooler())
                                .add(new TemporalMemory())));
    }

    private static MultiEncoder createEncoder() {
        MultiEncoder result = MultiEncoder.builder().build();
        GeospatialCoordinateEncoder geoEncoder = GeospatialCoordinateEncoder
                .geobuilder()
                .timestep(15)
                .scale(5)
                .n(2048)
                .w(51)
                .name("location")
                .build();
        result.addEncoder("location", geoEncoder);
        return result;
    }

    private static void runAnomalyDetection(Path path) {
        String id = path.getFileName().toString().replaceAll("[^0-9_]", "");
        SerialConfig PERSISTENCE_CONFIG = new SerialConfig(
                String.format(NETWORK_FILENAME_PATTERN, id),
                DIR_PATH,
                null
        );
        PERSISTENCE_API.setConfig(PERSISTENCE_CONFIG);
        LOGGER.info(String.format("Trying loading existing network from %s", TRAINED_NETWORK_PATH));
        try {
            Network network = PERSISTENCE_API.load(TRAINED_NETWORK_PATH);
            LOGGER.info(String.format("Network %s loaded successfully.", network.getName()));
            Path outputFile = Paths.get(String.format(OUTPUT_FILENAME_PATTERN, id));
            processCSVFile(path, outputFile, true, network);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static double processRecord(Map<String, Object> record, String sequenceId, Network network) {
        boolean newSequence = !prevSequence.get().equals(sequenceId);
        if (newSequence) {
            LOGGER.info("Detected new sequence, resetting the learning.");
            prevSequence.set(sequenceId);
            network.reset();
        }
        int out[] = new int[ENCODER.getW()];
        ENCODER.encodeIntoArray(record, out);

        Inference inference = network.computeImmediate(out);
        return inference.getAnomalyScore();
    }

    private static void processCSVFile(Path input, Path output, boolean persistNetwork, final Network network) throws IOException {
        DateTime start = DateTime.now();
        try (BufferedWriter writer = Files.newBufferedWriter(output, StandardOpenOption.CREATE)) {
            writer.write(ResultRecord.HEADER);
            LOGGER.info(String.format("Started calculations for file %s", input.toString()));
            Files.lines(input)
                    .map(ResultRecord::parseLine)
                    .peek(x -> {
                        double anomalyScore = processRecord(x.getAnomalyDetectionFeatures(), x.getTripId(), network);
                        LOGGER.debug(String.format("Anomaly score for %s: %s",
                                x.getTripId(), anomalyScore));
                        x.setAnomalyScore(anomalyScore);
                    })
                    .forEachOrdered(x -> {
                        try {
                            writer.write(x.toString());
                            if (counter.incrementAndGet() % 100 == 0) {
                                LOGGER.info(String.format("Processed %s records", counter.get()));
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
            Duration duration = new Duration(start, DateTime.now());
            LOGGER.info(String.format("Finished %s in %s:%s:%s:%s",
                    input.toString(),
                    duration.getStandardHours(),
                    duration.getStandardMinutes(),
                    duration.getStandardSeconds(),
                    duration.getMillis()));
            if (persistNetwork) {
                LOGGER.info("Persisting the network ");
                PERSISTENCE_API.store(network);
            }
        }
    }

}
