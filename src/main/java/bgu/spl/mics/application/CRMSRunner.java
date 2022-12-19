package bgu.spl.mics.application;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

import bgu.spl.mics.application.objects.CPU;
import bgu.spl.mics.application.objects.ConferenceInformation;
import bgu.spl.mics.application.objects.Developer;
import bgu.spl.mics.application.objects.GPU;

/**
 * This is the Main class of Compute Resources Management System application.
 * You should parse the input file,
 * create the different instances of the objects, and run the system.
 * In the end, you should output a text file.
 */
public class CRMSRunner {
    static class InputJson {
        public List<Developer> developers;
        public List<GPU> gpus;
        public List<CPU> cpus;
        public List<ConferenceInformation> conferenceInformations;
        public int TickTime;
        public int Duration;

        public InputJson(List<Developer> developers, List<GPU> gpus, List<CPU> cpus,
                List<ConferenceInformation> conferenceInformations, int tickTime, int duration) {
            this.developers = developers;
            this.gpus = gpus;
            this.cpus = cpus;
            this.conferenceInformations = conferenceInformations;
            TickTime = tickTime;
            Duration = duration;
        }
    }

    static class OutputJson {
        public List<Developer> developers;
        public List<ConferenceInformation> conferenceInformations;
        public Integer gpuTimeUsed;
        public Integer cpuTimeUsed;

        public OutputJson(List<Developer> developers, List<ConferenceInformation> conferenceInformations,
                Integer gpuTimeUsed, Integer cpuTimeUsed) {
            this.developers = developers;
            this.conferenceInformations = conferenceInformations;
            this.gpuTimeUsed = gpuTimeUsed;
            this.cpuTimeUsed = cpuTimeUsed;
        }
    }

    static class Args {
        public InputJson input;
        public Path outputFile;

        public static Args fromArgs(String[] args) {
            if (args.length != 2) {
                throw new IllegalArgumentException("CRMSRunner expecting a single argument: input file");
            }

            Path input = Path.of(args[0]).toAbsolutePath();

            if (!Files.exists(input)) {
                throw new IllegalArgumentException("Given path doesn't exist: " + input.toString());
            }

            InputJson inputJson;

            try {
                inputJson = readInputJson(input);
            } catch (IOException e) {
                throw new IllegalArgumentException("Given path doesn't contain valid json");
            }

            Path output = Path.of(args[1]).toAbsolutePath();
            if (Files.exists(output)) {
                throw new IllegalArgumentException("Given output path already exist: " + output.toString());
            }

            return new Args(inputJson, output);
        }

        public Args(InputJson input, Path outputFile) {
            this.input = input;
            this.outputFile = outputFile;
        }
    }

    public static OutputJson run(InputJson input) {
        // 1. start all microservices except TimeService
        // DeveloperService
        // GpuService
        // CpuService
        // ConferenceService

        // 2. wait for all microservices to be registered
        // start TimeService

        // 3. Wait for TimeService to finish duration
        return null;
    }

    public static void main(String[] args) throws IOException {
        Args parsedArgs = Args.fromArgs(args);
        InputJson inputGson = parsedArgs.input;
        Path output = parsedArgs.outputFile;

        OutputJson outputJson = run(inputGson);

        // 4. create output file
        new Gson().toJson(outputJson, new FileWriter(output.toString()));

    }

    private static InputJson readInputJson(Path inputFile) throws IOException {
        // create Gson instance
        Gson gson = new Gson();

        // create a reader
        Reader reader = Files.newBufferedReader(inputFile);

        // convert JSON file to map
        InputJson inputJson = gson.fromJson(reader, InputJson.class);

        // close reader
        reader.close();

        return inputJson;
    }

}
