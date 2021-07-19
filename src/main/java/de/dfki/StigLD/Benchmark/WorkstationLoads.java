/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.StigLD.Benchmark;

import java.util.HashMap;
import java.util.Map;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

/**
 *
 * @author tospie
 */
public class WorkstationLoads {

    public static Map<String, WorkstationStatistics> Statistics = new HashMap<>();

    public static void getWorkstationStatistics(String endpoint) {
	ResultSet results = QueryExecutionFactory.sparqlService(endpoint, QueryFactory.create(Queries.workstation_tasks)).execSelect();
	while (results.hasNext()) {
	    updateWorkstation(results.next());
	}
    }

    private static void updateWorkstation(QuerySolution n) {
	String wsUri = n.getResource("machine").getLocalName();
	int currentLoad = n.getLiteral("n").getInt();
	WorkstationStatistics old = Statistics.getOrDefault(wsUri, new WorkstationStatistics(currentLoad, currentLoad));
	WorkstationStatistics current = new WorkstationStatistics(currentLoad, currentLoad > old.maxLoad ? currentLoad : old.maxLoad);
	Statistics.put(wsUri, current);
    }
}
