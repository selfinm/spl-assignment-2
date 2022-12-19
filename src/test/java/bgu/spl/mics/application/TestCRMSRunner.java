package bgu.spl.mics.application;

import org.junit.Before;
import org.junit.Test;

import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.application.CRMSRunner.InputJson;
import bgu.spl.mics.application.CRMSRunner.OutputJson;

public class TestCRMSRunner {
    MessageBusImpl m;

    @Before
    public void before() {
        MessageBusImpl.shutdown();
        m = MessageBusImpl.getInstance();
    }

    @Test
    public void testCRMSRunner() {
        InputJson input = SharedTestUtils.getInputJson();
        OutputJson expected = new OutputJson(input.developers, input.conferenceInformations, 0, 0);

        OutputJson actual = new CRMSRunner().run(input);

        // Assert.assertEquals(expected.developers.size(), actual.developers.size());
        for (int i = 0; i < expected.developers.size(); i++) {
        }
    }

}
