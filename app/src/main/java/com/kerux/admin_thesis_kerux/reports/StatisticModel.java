package com.kerux.admin_thesis_kerux.reports;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class StatisticModel {

    private int QueuesServed;
    private int QueuesCancelled;
    private String HighestDoctorQueues;
    private String HighestDeptQueues;
    private SharedPreferences prefs;

    public StatisticModel(Context cntx) {
        // TODO Auto-generated constructor stub
        prefs = PreferenceManager.getDefaultSharedPreferences(cntx);
    }

    public StatisticModel(int queuesServed, int queuesCancelled, String highestDoctorQueues, String highestDeptQueues) {
        QueuesServed = queuesServed;
        QueuesCancelled = queuesCancelled;
        HighestDoctorQueues = highestDoctorQueues;
        HighestDeptQueues = highestDeptQueues;
    }

    public int getQueuesServed() {
        return QueuesServed;
    }

    public void setQueuesServed(int queuesServed) {
        QueuesServed = queuesServed;
    }

    public int getQueuesCancelled() {
        return QueuesCancelled;
    }

    public void setQueuesCancelled(int queuesCancelled) {
        QueuesCancelled = queuesCancelled;
    }

    public String getHighestDoctorQueues() {
        return HighestDoctorQueues;
    }

    public void setHighestDoctorQueues(String highestDoctorQueues) {
        HighestDoctorQueues = highestDoctorQueues;
    }

    public String getHighestDeptQueues() {
        return HighestDeptQueues;
    }

    public void setHighestDeptQueues(String highestDeptQueues) {
        HighestDeptQueues = highestDeptQueues;
    }
}
