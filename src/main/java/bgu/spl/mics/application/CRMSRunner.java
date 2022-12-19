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
    }

    static class Args {
        public Gson inputGson;
        public Path outputFile;

        public static Args fromArgs(String[] args) {
            if (args.length != 2) {
                throw new IllegalArgumentException("CRMSRunner expecting a single argument: input file");
            }

            Path input = Path.of(args[0]).toAbsolutePath();

            if (!Files.exists(input)) {
                throw new IllegalArgumentException("Given path doesn't exist: " + input.toString());
            }

            Gson inputGson;

            try {
                inputGson = readGson(input);
            } catch (IOException e) {
                throw new IllegalArgumentException("Given path doesn't contain valid json");
            }

            Path output = Path.of(args[1]).toAbsolutePath();
            if (Files.exists(output)) {
                throw new IllegalArgumentException("Given output path already exist: " + output.toString());
            }

            return new Args(inputGson, output);
        }

        public Args(Gson inputGson, Path outputFile) {
            this.inputGson = inputGson;
            this.outputFile = outputFile;
        }
    }

    public static Gson run(Gson input) {
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
        Gson inputGson = parsedArgs.inputGson;
        Path output = parsedArgs.outputFile;

        Gson outputGson = run(inputGson);

        // 4. create output file
        outputGson.toJson(new FileWriter(parsedArgs.outputFile.toString()));

    }

    private static Gson readGson(Path jsonFile) throws IOException {
        // create Gson instance
        Gson gson = new Gson();

        // create a reader
        Reader reader = Files.newBufferedReader(jsonFile);

        // convert JSON file to map
        Map<?, ?> map = gson.fromJson(reader, Map.class);

        // print map entries
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            System.out.println(entry.getKey() + "=" + entry.getValue());
        }

        // close reader
        reader.close();

        return gson;
    }

}
