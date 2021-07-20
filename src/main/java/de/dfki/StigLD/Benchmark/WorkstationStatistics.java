/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.StigLD.Benchmark;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author tospie
 */
public class WorkstationStatistics {

    int maxLoad = 0;
    int currentLoad = 0;
    int totalLoad = 0;

    private final Set<String> assignedTasks = new HashSet<>();

    public WorkstationStatistics() {
    }

    public WorkstationStatistics(int currentLoad, int maxLoad) {
	this.maxLoad = maxLoad;
	this.currentLoad = currentLoad;
    }

    public void putTask(String startTime) {
	if (!assignedTasks.contains(startTime)) {
	    assignedTasks.add(startTime);
	}
    }

    public int TaskCount() {
	return assignedTasks.size();
    }
}
