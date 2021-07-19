/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.StigLD.Benchmark;

/**
 *
 * @author tospie
 */
public class WorkstationStatistics {

    public int currentLoad;
    public int maxLoad;

    public WorkstationStatistics(int current, int max) {
	this.currentLoad = current;
	this.maxLoad = max;
    }
}
