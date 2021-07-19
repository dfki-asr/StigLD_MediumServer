/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.StigLD.Benchmark;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;

/**
 *
 * @author tospie
 */
public class Benchmark {

    private int stepCounter = 0;
    String endpoint = "http://localhost:3230/ds/";
    ScenarioStatistics scenarioStatistics = new ScenarioStatistics();
    ObjectMapper mapper = new ObjectMapper();
    private boolean initialized = false;

    public Benchmark() {
    }

    public void init() {
	scenarioStatistics.read(endpoint);
	initialized = true;
    }

    public void measure() {
	if (!initialized) {
	    init();
	}
	stepCounter++;
	/// send benchmarking queries
	Transporter.countTransporterSteps(endpoint);
	WorkstationLoads.getWorkstationStatistics(endpoint);

	/// If no more orders are open, write log file
	checkDoneAndFinalize();
    }

    private void checkDoneAndFinalize() {
	try {
	    if (!QueryExecutionFactory.sparqlService(endpoint, QueryFactory.create(Queries.open_orders)).execAsk()) {
		System.out.println("[StigLD Benchmark] ------ FINISHED! ALL ORDERS PROCESSED ------ ");
		writeResults();
	    }
	} catch (JsonProcessingException e) {
	    System.out.println("[StigLD Benchmark] Something went wrong when serilazing the benchmark results: " + e.getMessage());
	} catch (FileNotFoundException e) {
	    System.out.println("[StigLD Benchmark] Something went wrong when accessing the file to save the log: " + e.getMessage());
	}

    }

    private void writeResults() throws JsonProcessingException, FileNotFoundException {
	String basicStatistics = mapper.writeValueAsString(scenarioStatistics);
	String transporterStatistics = mapper.writeValueAsString(Transporter.Statistics);
	String workstationStatistics = mapper.writeValueAsString(WorkstationLoads.Statistics);
	PrintWriter printWriter = new PrintWriter("logs/benchmark-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm")) + ".log");
	printWriter.println("Scenario Setup: ");
	printWriter.println(" ");
	printWriter.println(basicStatistics);
	printWriter.println(" ");
	printWriter.println("Total number of steps: " + stepCounter);
	printWriter.println(" ");
	printWriter.println("Transporter Statistics: ");
	printWriter.println(" ");
	printWriter.println(transporterStatistics);
	printWriter.println("Workstation Statistics: ");
	printWriter.println(" ");
	printWriter.println(workstationStatistics);
	printWriter.close();
    }
}
