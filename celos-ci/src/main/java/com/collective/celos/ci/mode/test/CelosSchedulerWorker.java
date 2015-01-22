package com.collective.celos.ci.mode.test;

import com.collective.celos.ScheduledTime;
import com.collective.celos.SlotState;
import com.collective.celos.WorkflowID;
import com.collective.celos.ci.mode.test.client.CelosClient;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Created by akonopko on 9/18/14.
 */
public class CelosSchedulerWorker {

    private final CelosClient client;

    public CelosSchedulerWorker(CelosClient client) {
        this.client = client;
    }

    public void runCelosScheduler(TestCase testConfig) throws Exception {
        Set<WorkflowID> workflowList;
        if (testConfig.getTargetWorkflows().isEmpty()) {
            workflowList = client.getWorkflowList();
        } else {
            workflowList = testConfig.getTargetWorkflows();
        }

        ScheduledTime startTime = testConfig.getSampleTimeStart().plusSeconds(1);
        ScheduledTime actualTime = startTime;
        ScheduledTime endTime = testConfig.getSampleTimeEnd().plusSeconds(1);

        System.out.println("Starting scheduler for: " + StringUtils.join(workflowList, ", "));
        client.iterateScheduler(actualTime, testConfig.getTargetWorkflows());

        while (!actualTime.getDateTime().isAfter(endTime.getDateTime())) {
            String workflowStatuses = StringUtils.join(getWorkflowStatusesInfo(workflowList, actualTime), " ");
            System.out.println("Workflow statuses: " + workflowStatuses);
            if (!isThereAnyRunningWorkflows(workflowList, actualTime)) {
                actualTime = new ScheduledTime(actualTime.getDateTime().plusHours(1));
            } else {
                Thread.sleep(2000);
            }
            client.iterateScheduler(actualTime, testConfig.getTargetWorkflows());
        }
    }

    boolean isThereAnyRunningWorkflows(Set<WorkflowID> workflowList, ScheduledTime schedTime) throws IOException {
        for (WorkflowID workflowID : workflowList) {
            List<SlotState> slotStates = client.getWorkflowStatus(workflowID, schedTime);
            for (SlotState slotState : slotStates) {
                if (slotState.getStatus() == SlotState.Status.READY || slotState.getStatus() == SlotState.Status.RUNNING) {
                    return true;
                }
            }
        }
        return false;
    }

    Set<String> getWorkflowStatusesInfo(Set<WorkflowID> workflowList, ScheduledTime schedTime) throws IOException {
        Set<String> messages = Sets.newHashSet();
        for (WorkflowID workflowID : workflowList) {
            List<SlotState> slotStates = client.getWorkflowStatus(workflowID, schedTime);
            for (SlotState slotState : slotStates) {
                if (slotState.getStatus() != SlotState.Status.WAITING) {
                    messages.add(slotState.toString());
                }
            }
        }
        return messages;
    }

}