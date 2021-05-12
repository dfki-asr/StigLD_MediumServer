/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.StigLD;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;

/**
 *
 * @author tospie
 */
public class JSON_Serializer {

    private Topos[][] topoi;
    private ObjectMapper objectMapper = new ObjectMapper();

    public class Topos {

	@Getter
	double NegFeedback;

	@Getter
	double TransportStigma;

	@Getter
	double DiffusionTrace;

	@Getter
	Machine Machine;

	@Getter
	Transporter Transporter;
    }

    public class Machine {

	@Getter
	int Orders;
	@Getter
	int Waiting;
    }

    public class Transporter {

	@Getter
	double TimeToPickup;
    }

    public String getModelAsJson(Model model) throws JsonProcessingException {

	QueryExecution q = QueryExecutionFactory.create(getTopoi, model);
	ResultSet r = q.execSelect();
	QuerySolution solution = r.next();
	int x = solution.getLiteral("max_x").getInt() + 1;
	int y = solution.getLiteral("max_y").getInt() + 1;

	topoi = new Topos[y][x];
	setNegativeFeedback(model);
	setTransport(model);
	setDiffusion(model);
	setMachines(model);
	setTransporters(model);
	return objectMapper.writeValueAsString(topoi);
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
	QueryExecution q = QueryExecutionFactory.create(getTransportStigma, model);
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

    private void setMachines(Model model) {
	QueryExecution q = QueryExecutionFactory.create(getMachines, model);
	ResultSet r = q.execSelect();
	r.forEachRemaining(s -> {
	    int x = s.getLiteral("x").getInt();
	    int y = s.getLiteral("y").getInt();
	    int scheduled = s.getLiteral("scheduled").getInt();
	    int waiting = s.getLiteral("waiting").getInt();

	    if (topoi[y][x] == null) {
		topoi[y][x] = new Topos();
	    }

	    Machine m = new Machine();
	    m.Orders = scheduled;
	    m.Waiting = waiting;
	    topoi[y][x].Machine = m;
	});
    }

    private void setTransporters(Model model) {
	QueryExecution q = QueryExecutionFactory.create(getTransporters, model);
	ResultSet r = q.execSelect();
	r.forEachRemaining(s -> {

	    int x = s.getLiteral("x").getInt();
	    int y = s.getLiteral("y").getInt();
	    double remaining = 0;
	    if (s.contains("remaining"))
			remaining = s.getLiteral("remaining").getDouble();
	    if (topoi[y][x] == null) {
		topoi[y][x] = new Topos();
	    }

	    Transporter t = new Transporter();
	    t.TimeToPickup = remaining;
	    topoi[y][x].Transporter = t;
	});
    }

    private final String getTopoi = "PREFIX ex:<http://example.org/>\n"
	    + "PREFIX pos: <http://example.org/property/position#>\n"
	    + "PREFIX st:  <http://example.org/stigld/>\n"
	    + "SELECT (MAX(?x) as ?max_x)  (MAX(?y) as ?max_y) WHERE { ?s a st:Topos ; pos:xPos ?x ; pos:yPos ?y }";

    private final String getNegFeedback = "PREFIX ex:<http://example.org/>\n"
	    + "PREFIX pos: <http://example.org/property/position#>\n"
	    + "PREFIX st:  <http://example.org/stigld/>\n"
	    + "SELECT ?x ?y ?lvl  WHERE { ?s a st:Topos ; pos:xPos ?x ; pos:yPos ?y ; st:carries [ a ex:NegFeedback ; st:level ?lvl ] }";

    private final String getTransportStigma = "PREFIX ex:<http://example.org/>\n"
	    + "PREFIX pos: <http://example.org/property/position#>\n"
	    + "PREFIX st:  <http://example.org/stigld/>\n"
	    + "SELECT ?x ?y ?lvl  WHERE { ?s a st:Topos ; pos:xPos ?x ; pos:yPos ?y ; st:carries [ a ex:TransportStigma; st:level ?lvl ] }";

    private final String getDiffusion = "PREFIX ex:<http://example.org/>\n"
	    + "PREFIX pos: <http://example.org/property/position#>\n"
	    + "PREFIX st:  <http://example.org/stigld/>\n"
	    + "SELECT ?x ?y ?lvl  WHERE { ?s a st:Topos ; pos:xPos ?x ; pos:yPos ?y ; st:carries [ a ex:DiffusionTrace; st:level ?lvl ] }";

    private final String getMachines = "PREFIX ex:<http://example.org/>\n"
	    + "PREFIX pos: <http://example.org/property/position#>\n"
	    + "PREFIX st:  <http://example.org/stigld/>\n"
	    + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
	    + "\n"
	    + "SELECT DISTINCT ?scheduled ?waiting ?x ?y WHERE{\n"
	    + "                ?m a ex:ProductionArtifact ;  ex:located [ pos:xPos ?x ; pos:yPos ?y ] . \n"
	    + "                \n"
	    + "                {SELECT (COUNT(?q) as ?scheduled ) ?m WHERE {\n"
	    + "                    ?m a ex:ProductionArtifact.\n"
	    + "                    OPTIONAL { ?m ex:queue ?q.  }\n"
	    + "                } GROUP BY ?m }\n"
	    + "                \n"
	    + "                {SELECT (COUNT(?prod) as ?waiting ) ?m WHERE {\n"
	    + "                    ?m a ex:ProductionArtifact ; ex:outputPort ?o . \n"
	    + "                    ?o ex:located ?o_pos .\n"
	    + "                   OPTIONAL { ?prod a ex:Product ; ex:located ?o_pos .  }\n"
	    + "                } GROUP BY ?m }\n"
	    + "}";

    private final String getTransporters = "PREFIX ex:<http://example.org/>\n"
	    + "PREFIX pos: <http://example.org/property/position#>\n"
	    + "PREFIX st:  <http://example.org/stigld/>\n"
	    + "SELECT DISTINCT ?t ?x ?y ?remaining WHERE\n"
	    + "    { \n"
	    + "        ?t a ex:Transporter ; ex:located [ pos:xPos ?x ; pos:yPos ?y] .\n"
	    + "        OPTIONAL {\n"
	    + "            ?t ex:queue [a ex:PickUpTask ; ex:endTime ?end ] .\n"
	    + "            BIND(NOW() as ?now)\n"
	    + "            BIND(IF(?now > ?end, 0, (hours(?end-?now) * 24 * 60 + minutes(?end-?now) * 60 + seconds(?end-?now) ) ) as ?remaining )\n"
	    + "        }        \n"
	    + "    }";
}
