package com.collective.celos.trigger;

import com.collective.celos.ScheduledTime;
import com.collective.celos.Scheduler;
import org.joda.time.DateTime;

import java.util.Collections;

/**
 * A trigger that signals data availability for a given scheduled time
 * only if it is a configurable number of seconds past the current time.
 * 
 * In combination with an AND trigger, this allows to delay the firing
 * of another trigger, for example to clean up data after a day.
 * 
 * The following example shows a trigger that only fires if the given
 * HDFS path is available, and the current time is one day after 
 * the workflow's scheduled time.
 * 
 * var oneDay = 60 * 60 * 24;
 * andTrigger(delayTrigger(oneDay), hdfsCheckTrigger("/${year}/${month}/${day}/..."))
*/
public class DelayTrigger extends Trigger {

    private final int seconds;
    
    public DelayTrigger(int seconds) throws Exception {
        this.seconds = seconds;
    }

    @Override
    public TriggerStatusPOJO makeStatusObject(Scheduler scheduler, ScheduledTime now, ScheduledTime scheduledTime) throws Exception {
        DateTime nowDT = now.getDateTime();
        DateTime waitUntilDT = scheduledTime.getDateTime().plusSeconds(getSeconds());
        final boolean ready = nowDT.isAfter(waitUntilDT);
        final String description = this.humanReadableDescription(ready, scheduledTime);
        return new TriggerStatusPOJO(ready, description, Collections.<TriggerStatusPOJO>emptyList());
    }

    @Override
    public String humanReadableDescription(boolean ready, ScheduledTime scheduledTime) {
        return "Delay " + seconds + "seconds";
    }

    public int getSeconds() {
        return seconds;
    }

}
