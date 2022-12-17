package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;

/**
 * CPU service is responsible for handling the {@link DataPreProcessEvent}.
 * This class may not hold references for objects which it is not responsible
 * for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class CPUService extends MicroService {
    public CPUService(String name) {
        super("CPUService");
    }

    @Override
    protected void initialize() {

    }
}