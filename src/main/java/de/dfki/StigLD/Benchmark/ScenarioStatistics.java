/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.StigLD.Benchmark;

import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;

/**
 *
 * @author tospie
 */
public class ScenarioStatistics {

    public int numOrders;
    public int numWorkstations;
    public int numTransporters;

    public ScenarioStatistics() {
    }

    public void read(String endpoint) {
	ResultSet results = QueryExecutionFactory.sparqlService(endpoint, QueryFactory.create(Queries.count_artifacts)).execSelect();
	while (results.hasNext()) {
	    var line = results.next();
	    numOrders = line.getLiteral("orders").getInt();
	    numWorkstations = line.getLiteral("workstations").getInt();
	    numTransporters = line.getLiteral("transporters").getInt();
	}
    }
}
