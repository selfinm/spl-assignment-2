package bgu.spl.mics.application;

import java.util.List;

import bgu.spl.mics.application.CRMSRunner.InputJson;
import bgu.spl.mics.application.objects.CPU;
import bgu.spl.mics.application.objects.ConferenceInformation;
import bgu.spl.mics.application.objects.Data;
import bgu.spl.mics.application.objects.Developer;
import bgu.spl.mics.application.objects.Developer.Status;
import bgu.spl.mics.application.objects.GPU;
import bgu.spl.mics.application.objects.Model;

public class SharedTestData {
    public static InputJson getInputJson() {
        List<Developer> developers = getDevelopers();
        List<GPU> gpus = getGpus();
        List<CPU> cpus = getCpus();
        List<ConferenceInformation> conferenceInformations = getConferenceInformations();

        return new CRMSRunner.InputJson(developers, gpus, cpus, conferenceInformations,
                1, 100_000);
    }

    public static List<ConferenceInformation> getConferenceInformations() {
        List<ConferenceInformation> expectedConferenceInformations = List.of(
                new ConferenceInformation("ICML", 20000),
                new ConferenceInformation("NeurIPS", 25000),
                new ConferenceInformation("CVPR", 30000),
                new ConferenceInformation("ECCV", 40000),
                new ConferenceInformation("AISTATS", 50000));
        return expectedConferenceInformations;
    }

    public static List<CPU> getCpus() {
        List<CPU> expectedCpus = List.of(
                new CPU(32),
                new CPU(32),
                new CPU(32),
                new CPU(16),
                new CPU(16),
                new CPU(16),
                new CPU(16));
        return expectedCpus;
    }

    public static List<GPU> getGpus() {
        List<GPU> expectedGpus = List.of(
                new GPU(GPU.Type.GTX1080),
                new GPU(GPU.Type.RTX3090),
                new GPU(GPU.Type.RTX2080),
                new GPU(GPU.Type.GTX1080));
        return expectedGpus;
    }

    public static List<Developer> getDevelopers() {
        List<Developer> expectedDevelopers = List.of(
                new Developer(
                        "Dev-1",
                        "Dep-1",
                        Status.Intern,
                        List.of(
                                new Model("YOLO10", new Data(Data.Type.Images, 200000)),
                                new Model("ResNet9000",
                                        new Data(Data.Type.Images, 200000)),
                                new Model("LessEfficientNet",
                                        new Data(Data.Type.Images, 20000)),
                                new Model("DensestNet",
                                        new Data(Data.Type.Images, 200000)))),
                new Developer(
                        "Dev-2",
                        "Dep-2",
                        Status.Junior,
                        List.of(
                                new Model("VIT", new Data(Data.Type.Images,
                                        100000000)))),
                new Developer(
                        "Dev-3",
                        "Dep-1",
                        Status.Senior,
                        List.of(
                                new Model("Bert", new Data(Data.Type.Text, 1000000)),
                                new Model("GPT4", new Data(Data.Type.Text, 1000000)),
                                new Model("GPT5", new Data(Data.Type.Text, 200000)),
                                new Model("GPT10", new Data(Data.Type.Text, 50000)))),
                new Developer(
                        "Dev-4",
                        "Dep-2",
                        Status.Intern,
                        List.of(
                                new Model("Percepetron",
                                        new Data(Data.Type.Tabular, 1000000)),
                                new Model("GNN", new Data(Data.Type.Tabular, 1000000)),
                                new Model("MoreStyleGAN",
                                        new Data(Data.Type.Images, 100000)),
                                new Model("ConditionalGAN",
                                        new Data(Data.Type.Images, 500000)))),
                new Developer(
                        "Dev-5",
                        "Dep-3",
                        Status.Junior,
                        List.of(
                                new Model("YOLO9000",
                                        new Data(Data.Type.Images, 100000)),
                                new Model("VIT2", new Data(Data.Type.Images, 1000000)),
                                new Model("MuchMoreEfficientNet",
                                        new Data(Data.Type.Images, 20000)),
                                new Model("DenserNet",
                                        new Data(Data.Type.Images, 100000)))));
        return expectedDevelopers;
    }
}
