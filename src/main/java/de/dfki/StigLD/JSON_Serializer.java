/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.StigLD;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import java.util.List;
import java.util.Vector;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;

/**
 *
 * @author tospie
 */
public class JSON_Serializer {

    private Topos[][] topoi;

    private class Topos {

	int x;
	int y;
	double NegFeedback;
	double TransportStigma;
	double DiffusionTrace;
	Machine machine;
	Transporter transporter;
    }

    private class Machine {

	int Orders;
	int Waiting;
    }

    private class Transporter {

	int TimeToPickup;
    }

    public String getModelAsJson(Model model) {

	QueryExecution q = QueryExecutionFactory.create(getTopoi, model);
	ResultSet r = q.execSelect();
	QuerySolution solution = r.next();
	int x = solution.getLiteral("max_x").getInt() + 1;
	int y = solution.getLiteral("max_y").getInt() + 1;

	topoi = new Topos[y][x];
	setNegativeFeedback(model);
	return "NOT IMPLEMENTED";
    }

    private void setNegativeFeedback(Model model) {
	QueryExecution q = QueryExecutionFactory.create(getNegFeedback, model);
	ResultSet r = q.execSelect();
	r.forEachRemaining(s -> {
	    int x = s.getLiteral("x").getInt();
	    int y = s.getLiteral("y").getInt();
	    double level = s.getLiteral("lvl").getDouble();

	    if (topoi[y][x] == null) {
		topoi[y][x] = new Topos();
	    }

	    topoi[y][x].NegFeedback = level;
	});
    }

    private void setTransport(Model model) {
	QueryExecution q = QueryExecutionFactory.create(getTransport, model);
	ResultSet r = q.execSelect();
	r.forEachRemaining(s -> {
	    int x = s.getLiteral("x").getInt();
	    int y = s.getLiteral("y").getInt();
	    double level = s.getLiteral("lvl").getDouble();

	    if (topoi[y][x] == null) {
		topoi[y][x] = new Topos();
	    }

	    topoi[y][x].TransportStigma = level;
	});
    }

    private void setDiffusion(Model model) {
	QueryExecution q = QueryExecutionFactory.create(getDiffusion, model);
	ResultSet r = q.execSelect();
	r.forEachRemaining(s -> {
	    int x = s.getLiteral("x").getInt();
	    int y = s.getLiteral("y").getInt();
	    double level = s.getLiteral("lvl").getDouble();

	    if (topoi[y][x] == null) {
		topoi[y][x] = new Topos();
	    }

	    topoi[y][x].DiffusionTrace = level;
	});
    }

    private final String getTopoi = "PREFIX ex:<http://example.org/>\n"
	    + "PREFIX pos: <http://example.org/property/position#>\n"
	    + "SELECT ?x ?y ?lvl  (MAX(?y) as ?max_y) WHERE { ?s a st:Topos ; pos:xPos ?x ; pos:yPos ?y }";

    private final String getNegFeedback = "PREFIX ex:<http://example.org/>\n"
	    + "PREFIX pos: <http://example.org/property/position#>\n"
	    + "PREFIX st:  <http://example.org/stigld/>\n"
	    + "SELECT ?x ?y ?lvl  WHERE { ?s a st:Topos ; pos:xPos ?x ; pos:yPos ?y ; st:carries [ a ex:NegFeedback ; st:level ?lvl ] }";

    private final String getTransport = "PREFIX ex:<http://example.org/>\n"
	    + "PREFIX pos: <http://example.org/property/position#>\n"
	    + "PREFIX st:  <http://example.org/stigld/>\n"
	    + "SELECT ?x ?y ?lvl  WHERE { ?s a st:Topos ; pos:xPos ?x ; pos:yPos ?y ; st:carries [ a ex:TransportStigma; st:level ?lvl ] }";

    private final String getDiffusion = "PREFIX ex:<http://example.org/>\n"
	    + "PREFIX pos: <http://example.org/property/position#>\n"
	    + "PREFIX st:  <http://example.org/stigld/>\n"
	    + "SELECT ?x ?y ?lvl  WHERE { ?s a st:Topos ; pos:xPos ?x ; pos:yPos ?y ; st:carries [ a ex:DiffusionTrace; st:level ?lvl ] }";
}
