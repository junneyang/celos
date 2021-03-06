/*
 * Copyright 2015 Collective, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.collective.celos;

import org.joda.time.DateTime;

/**
 * Formatting utilities for times.
 */
public class ScheduledTimeFormatter {

    public String replaceTimeTokens(String string, ScheduledTime t) {
        return string
            .replace("${year}", formatYear(t))
            .replace("${month}", formatMonth(t))
            .replace("${day}", formatDay(t))
            .replace("${hour}", formatHour(t))
            .replace("${minute}", formatMinute(t))
            .replace("${second}", formatSecond(t))
            .replace("${millisecond}", formatMillisecond(t));
    }

    public String formatMillisecond(ScheduledTime t) {
        return String.format("%03d", t.getMillisecond());
    }

    public String formatSecond(ScheduledTime t) {
        return String.format("%02d", t.getSecond());
    }

    public String formatMinute(ScheduledTime t) {
        return String.format("%02d", t.getMinute());
    }

    public String formatHour(ScheduledTime t) {
        return String.format("%02d", t.getHour());
    }

    public String formatDay(ScheduledTime t) {
        return String.format("%02d", t.getDay());
    }

    public String formatMonth(ScheduledTime t) {
        return String.format("%02d", t.getMonth());
    }

    public String formatYear(ScheduledTime t) {
        return String.format("%04d", t.getYear());
    }
    
    public String formatTimestamp(ScheduledTime t) {
        return formatHour(t) + ":" + formatMinute(t) + ":" + formatSecond(t) + "." + formatMillisecond(t) + "Z";
    }
    
    public String formatDatestamp(ScheduledTime t) {
        return formatYear(t) + "-" + formatMonth(t) + "-" + formatDay(t);
    }

    /** Only show seconds and milliseconds if they're not 0. */
    public String formatPretty(ScheduledTime t) {
        DateTime dt = t.getDateTime();
        String timestamp = formatDatestamp(t) + "T" + formatHour(t) + ":" + formatMinute(t);
        if (Util.isFullMinute(dt)) {
            return timestamp + "Z";
        } else {
            if (Util.isFullSecond(dt)) {
                return timestamp + ":" + formatSecond(t) + "Z";
            } else {
                return timestamp + ":" + formatSecond(t) + "." + formatMillisecond(t) + "Z";
            }
        }
    }
    
}
