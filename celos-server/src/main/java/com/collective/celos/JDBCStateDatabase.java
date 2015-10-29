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

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class JDBCStateDatabase implements StateDatabase {

    public static final String SELECT_SINGLE_SLOT = "SELECT STATUS, EXTERNALID, RETRYCOUNT FROM SLOTSTATE WHERE WORKFLOWID = ? AND DATE = ?";
    public static final String INSERT_SLOT_STATE = "INSERT INTO SLOTSTATE(WORKFLOWID, DATE, STATUS, EXTERNALID, RETRYCOUNT) VALUES (?, ?, ?, ?, ?);";
    public static final String UPDATE_SLOT_STATE = "UPDATE SLOTSTATE SET STATUS=?, EXTERNALID=?, RETRYCOUNT=? WHERE WORKFLOWID=? AND DATE=?;";
    public static final String SELECT_SLOTS_BY_PERIOD = "SELECT STATUS, EXTERNALID, RETRYCOUNT, DATE FROM SLOTSTATE WHERE WORKFLOWID = ? AND DATE >= ? AND DATE < ?";
    public static final String SELECT_SLOTS_BY_TIMESTAMPS = "SELECT STATUS, EXTERNALID, RETRYCOUNT, DATE FROM SLOTSTATE WHERE WORKFLOWID = ? AND DATE IN (???)";
    public static final String INSERT_RERUN_SLOT = "INSERT INTO RERUNSLOT(WORKFLOWID, DATE) VALUES (?, ?);";
    public static final String SELECT_RERUN_SLOTS = "SELECT DATE FROM RERUNSLOT WHERE WORKFLOWID = ?";
    public static final String DELETE_RERUN_SLOTS = "DELETE FROM RERUNSLOT WHERE WORKFLOWID = ? AND DATE = ?";

    public static final String STATUS_PARAM = "status";
    public static final String EXTERNAL_ID_PARAM = "externalId";
    public static final String RETRY_COUNT_PARAM = "retryCount";
    public static final String DATE_PARAM = "date";

    private final Connection connection;

    public JDBCStateDatabase(String url, String name, String password) throws SQLException {
        this.connection = DriverManager.getConnection(url, name, password);
    }

    @Override
    public List<SlotState> getSlotStates(WorkflowID id, ScheduledTime start, ScheduledTime end) throws Exception {
        try (PreparedStatement preparedStatement = connection.prepareStatement(SELECT_SLOTS_BY_PERIOD)) {
            preparedStatement.setString(1, id.toString());
            preparedStatement.setTimestamp(2, Util.toTimestamp(start));
            preparedStatement.setTimestamp(3, Util.toTimestamp(end));
            List<SlotState> slotStates = Lists.newArrayList();
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    SlotState.Status status = SlotState.Status.valueOf(resultSet.getString(STATUS_PARAM));
                    String externalId = resultSet.getString(EXTERNAL_ID_PARAM);
                    int retryCount = resultSet.getInt(RETRY_COUNT_PARAM);
                    ScheduledTime date = Util.toScheduledTime(resultSet.getTimestamp(DATE_PARAM));
                    slotStates.add(new SlotState(new SlotID(id, date), status, externalId, retryCount));
                }
            }
            return slotStates;
        }
    }

    @Override
    public List<SlotState> getSlotStates(WorkflowID id, Collection<ScheduledTime> times) throws Exception {
        String questionMarks = StringUtils.join(times.stream().map(t -> "?").collect(Collectors.toList()), ", ");
        String query = SELECT_SLOTS_BY_TIMESTAMPS.replace("???", questionMarks);
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            int i = 1;
            preparedStatement.setString(i++, id.toString());
            for (ScheduledTime time : times) {
                preparedStatement.setTimestamp(i++, Util.toTimestamp(time));
            }
            List<SlotState> slotStates = Lists.newArrayList();
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    SlotState.Status status = SlotState.Status.valueOf(resultSet.getString(STATUS_PARAM));
                    String externalId = resultSet.getString(EXTERNAL_ID_PARAM);
                    int retryCount = resultSet.getInt(RETRY_COUNT_PARAM);
                    ScheduledTime date = Util.toScheduledTime(resultSet.getTimestamp(DATE_PARAM));
                    slotStates.add(new SlotState(new SlotID(id, date), status, externalId, retryCount));
                }
            }
            return slotStates;
        }
    }

    @Override
    public SlotState getSlotState(SlotID slotId) throws Exception {
        try (PreparedStatement preparedStatement = connection.prepareStatement(SELECT_SINGLE_SLOT)) {
            preparedStatement.setString(1, slotId.getWorkflowID().toString());
            preparedStatement.setTimestamp(2, Util.toTimestamp(slotId.getScheduledTime()));
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                SlotState.Status status = SlotState.Status.valueOf(resultSet.getString(STATUS_PARAM));
                String externalId = resultSet.getString(EXTERNAL_ID_PARAM);
                int retryCount = resultSet.getInt(RETRY_COUNT_PARAM);
                return new SlotState(slotId, status, externalId, retryCount);
            }
        }
    }

    @Override
    public void putSlotState(SlotState state) throws Exception {
        SlotState slotState = getSlotState(state.slotID);
        if (slotState == null) {
            try (PreparedStatement statement = connection.prepareStatement(INSERT_SLOT_STATE)) {
                statement.setString(1, state.getSlotID().getWorkflowID().toString());
                statement.setTimestamp(2, Util.toTimestamp(state.getSlotID().getScheduledTime()));
                statement.setString(3, state.getStatus().toString());
                statement.setString(4, state.getExternalID());
                statement.setInt(5, state.getRetryCount());
                statement.execute();
            }
        } else {
            try (PreparedStatement statement = connection.prepareStatement(UPDATE_SLOT_STATE)) {
                statement.setString(1, state.getStatus().toString());
                statement.setString(2, state.getExternalID());
                statement.setInt(3, state.getRetryCount());
                statement.setString(4, state.getSlotID().getWorkflowID().toString());
                statement.setTimestamp(5, Util.toTimestamp(state.getSlotID().getScheduledTime()));
                statement.execute();
            }
        }

    }

    @Override
    public void markSlotForRerun(SlotID slot, ScheduledTime now) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_RERUN_SLOT)) {
            statement.setString(1, slot.getWorkflowID().toString());
            statement.setTimestamp(2, Util.toTimestamp(slot.getScheduledTime()));
            statement.execute();
        }
    }

    @Override
    public SortedSet<ScheduledTime> getTimesMarkedForRerun(WorkflowID workflowID, ScheduledTime now) throws Exception {
        TreeSet<ScheduledTime> rerunTimes = new TreeSet<>();
        List<RerunState> rerunStates = Lists.newArrayList();
        try (PreparedStatement statement = connection.prepareStatement(SELECT_RERUN_SLOTS)) {
            statement.setString(1, workflowID.toString());
            try(ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    ScheduledTime time = Util.toScheduledTime(resultSet.getTimestamp(DATE_PARAM));
                    RerunState rerunState = new RerunState(time);
                    rerunTimes.add(time);
                    if (rerunState.isExpired(now)) {
                        rerunStates.add(rerunState);
                    }
                }
            }
        }
        try (PreparedStatement statement = connection.prepareStatement(DELETE_RERUN_SLOTS)) {
            for (RerunState rerunState : rerunStates) {
                statement.setString(1, workflowID.toString());
                statement.setTimestamp(2, Util.toTimestamp(rerunState.getRerunTime()));
                statement.execute();
            }
        }

        return rerunTimes;
    }

    @Override
    public boolean isPaused(WorkflowID workflowID) {
        return false;
    }

    @Override
    public void setPaused(WorkflowID workflowID, boolean paused) throws IOException {

    }
}