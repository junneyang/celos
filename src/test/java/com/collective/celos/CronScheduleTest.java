package com.collective.celos;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class CronScheduleTest {

    @Test
    public void cronScheduleEmpty() {
        Schedule sch = makeCronSchedule("0 0 12 * * ?");
        ScheduledTime t = new ScheduledTime("2013-11-25T20:00Z");
        Set<ScheduledTime> times = sch.getScheduledTimes(t, t);
        Assert.assertEquals(0, times.size());
    }

    @Test
    public void cronScheduleOneHourEmpty() {
        Schedule sch = makeCronSchedule("0 0 12 * * ?");
        ScheduledTime t1 = new ScheduledTime("2013-11-25T20:00Z");
        ScheduledTime t2 = new ScheduledTime("2013-11-25T21:00Z");
        Set<ScheduledTime> times = sch.getScheduledTimes(t1, t2);
        Assert.assertEquals(0, times.size());
    }

    @Test
    public void cronScheduleOneHourBorderWrongBorderVals() {
        Schedule sch = makeCronSchedule("0 0 12 * * ?");
        ScheduledTime t1 = new ScheduledTime("2013-11-25T14:00Z");
        ScheduledTime t2 = new ScheduledTime("2013-11-25T11:00Z");
        Set<ScheduledTime> times = sch.getScheduledTimes(t1, t2);

        Assert.assertEquals(0, times.size());
    }


    @Test
    public void cronScheduleOneHourBorderIncluded() {
        Schedule sch = makeCronSchedule("0 0 12 * * ?");
        ScheduledTime t1 = new ScheduledTime("2013-11-25T12:00Z");
        ScheduledTime t2 = new ScheduledTime("2013-11-25T13:00Z");
        Set<ScheduledTime> times = sch.getScheduledTimes(t1, t2);

        List<ScheduledTime> expectedMinutes = Arrays.asList(new ScheduledTime("2013-11-25T12:00Z"));
        Assert.assertEquals(new TreeSet<ScheduledTime>(expectedMinutes), times);
    }

    @Test
    public void cronScheduleComplexStuff() {
        //Fire every 5 minutes starting at 2:00 PM and ending at 2:55 PM, every day
        Schedule sch = makeCronSchedule("0 0/15 14 * * ?");
        ScheduledTime t1 = new ScheduledTime("2013-11-25T12:00Z");
        ScheduledTime t2 = new ScheduledTime("2013-11-25T18:00Z");
        Set<ScheduledTime> times = sch.getScheduledTimes(t1, t2);

        List<ScheduledTime> expectedMinutes = Arrays.asList(
                new ScheduledTime("2013-11-25T14:00Z"),
                new ScheduledTime("2013-11-25T14:15Z"),
                new ScheduledTime("2013-11-25T14:30Z"),
                new ScheduledTime("2013-11-25T14:45Z")
        );
        Assert.assertEquals(new TreeSet<ScheduledTime>(expectedMinutes), times);
    }


    @Test
    public void cronScheduleOneHourBorderExcludedEmpty() {
        Schedule sch = makeCronSchedule("0 0 12 * * ?");
        ScheduledTime t1 = new ScheduledTime("2013-11-25T11:00Z");
        ScheduledTime t2 = new ScheduledTime("2013-11-25T12:00Z");
        Set<ScheduledTime> hours = sch.getScheduledTimes(t1, t2);
        Assert.assertEquals(0, hours.size());
    }

    @Test
    public void cronScheduleInvalidConfig() {

        boolean failed = false;
        try {
            makeCronSchedule("0 0 12 * * blah");
        } catch (Exception exc) {
            failed = true;
        }
        Assert.assertTrue(failed);
    }


    @Test
    public void cronScheduleOneHour() {
        Schedule sch = makeCronSchedule("0 0 12 * * ?");
        ScheduledTime t1 = new ScheduledTime("2013-11-25T11:30Z");
        ScheduledTime t2 = new ScheduledTime("2013-11-25T12:30Z");
        Set<ScheduledTime> hours = sch.getScheduledTimes(t1, t2);
        Assert.assertEquals(1, hours.size());
    }

    private CronSchedule makeCronSchedule(String config) {
        return new CronSchedule(Util.newObjectNode().put("cron_config", config));
    }
}
