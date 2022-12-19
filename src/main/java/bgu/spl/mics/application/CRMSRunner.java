package bgu.spl.mics.application;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.objects.CPU;
import bgu.spl.mics.application.objects.Cluster;
import bgu.spl.mics.application.objects.ConferenceInformation;
import bgu.spl.mics.application.objects.Developer;
import bgu.spl.mics.application.objects.GPU;
import bgu.spl.mics.application.services.CPUService;
import bgu.spl.mics.application.services.ConferenceService;
import bgu.spl.mics.application.services.DeveloperService;
import bgu.spl.mics.application.services.GPUService;
import bgu.spl.mics.application.services.TimeService;

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

            Path input = Path.of(args[0]);

            if (!Files.exists(input)) {
                throw new IllegalArgumentException("Given path doesn't exist: " + input.toString());
            }

            InputJson inputJson;

            try {
                inputJson = readInputJson(input);
            } catch (IOException e) {
                throw new IllegalArgumentException("Given path doesn't contain valid json");
            }

            Path output = Path.of(args[1]);
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

    public static OutputJson run(InputJson input) throws InterruptedException {
        List<MicroService> microServices = new ArrayList<>();

        // build microservices
        for (Developer developer : input.developers) {
            DeveloperService developerService = new DeveloperService("developer-" +
                    developer.getName() + "-" + developer.getDepartment() + "-service-" + UUID.randomUUID(), developer);
            microServices.add(developerService);
        }

        for (GPU gpu : input.gpus) {
            GPUService gpuService = new GPUService("gpu-" + gpu.getType() + "-service-" + UUID.randomUUID(), gpu);
            microServices.add(gpuService);
        }

        for (CPU cpu : input.cpus) {
            CPUService cpuService = new CPUService("cpu-" + cpu.getCores() + "-service-" + UUID.randomUUID(), cpu);
            microServices.add(cpuService);
        }

        for (ConferenceInformation conferenceInformation : input.conferenceInformations) {
            ConferenceService conferenceService = new ConferenceService(
                    "conference-" + conferenceInformation.getName() + "-service-" + UUID.randomUUID(),
                    conferenceInformation);
            microServices.add(conferenceService);
        }

        // start microservices
        List<Thread> threads = new ArrayList<>();
        for (MicroService microService : microServices) {
            Thread thread = new Thread(microService);
            thread.start();
            threads.add(thread);
        }

        // wait for all services to be registered
        while (!MessageBusImpl
                .getInstance()
                .isRegistered(microServices.toArray(new MicroService[0]))) {

            Thread.sleep(100);
        }

        // start time service
        TimeService timeService = new TimeService("time-service", input.TickTime, input.Duration);
        microServices.add(timeService);
        Thread timeServiceThread = new Thread(timeService);
        timeServiceThread.start();
        threads.add(timeServiceThread);

        // wait for all services to stop
        System.out.println("Waiting for threads to finish...");
        for (Thread thread : threads) {
            thread.join();
        }

        // build output json
        OutputJson output = new OutputJson(
                input.developers,
                input.conferenceInformations,
                Cluster.getInstance().getGpuTimeUnitsUsed(),
                Cluster.getInstance().getCpuTimeUnitsUsed());

        return output;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Args parsedArgs = Args.fromArgs(args);
        InputJson inputGson = parsedArgs.input;
        Path output = parsedArgs.outputFile;

        OutputJson outputJson = run(inputGson);

        FileWriter writer = new FileWriter(output.toString());
        new Gson().toJson(outputJson, writer);
        writer.close();
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
