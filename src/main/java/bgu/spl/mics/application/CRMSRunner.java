package bgu.spl.mics.application;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import com.google.gson.Gson;

/**
 * This is the Main class of Compute Resources Management System application.
 * You should parse the input file,
 * create the different instances of the objects, and run the system.
 * In the end, you should output a text file.
 */
public class CRMSRunner {
    private static Path input;

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

    private static Gson parseArgs(String[] args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("CRMSRunner expecting a single argument: input file");
        }

        input = Path.of(args[0]).toAbsolutePath();

        if (!Files.exists(input)) {
            throw new IllegalArgumentException("Given path doesn't exist: " + input.toString());
        }

        Gson inputGson;

        try {
            inputGson = readGson(input);
        } catch (IOException e) {
            throw new IllegalArgumentException("Given path doesn't contain valid json");
        }
        return inputGson;
    }

    public static void main(String[] args) {
        Gson inputGson = parseArgs(args);

        // 1. start all microservices except TimeService
        // DeveloperService
        // GpuService
        // CpuService
        // ConferenceService

        // 2. wait for all microservices to be registered
        // start TimeService

        // 3. Wait for TimeService to finish duration

        // 4. create output file
    }

}
