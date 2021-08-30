/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.StigLD.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;

/**
 *
 * @author tospie
 */
public class JSON_Serializer_Ant {

    private final ObjectMapper objectMapper = new ObjectMapper();

    ResponseObject response;

    public class ResponseObject {

	@Getter
	private final Topos[][] topoi;

	public ResponseObject(int x_size, int y_size) {
	    this.topoi = new Topos[y_size][x_size];
	}
    }

    public class Topos {

	@Getter
	Ant Ant;

	@Getter
	double Pheromone;

	@Getter
	FoodSource FoodSource;

	@Getter
	Hive Hive;

    }

    public class Ant {

	@Getter
	@Setter
	int Carries;
    }

    public class FoodSource {

	@Getter
	@Setter
	int Capacity;

	@Getter
	@Setter
	int NutritionalValue;
    }

    public class Hive {

	@Getter
	int Stock;
    }

    public String getModelAsJson(Model model) throws JsonProcessingException {

	QueryExecution q = QueryExecutionFactory.create(getTopoi, model);
	ResultSet r = q.execSelect();
	QuerySolution solution = r.next();
	int x = solution.getLiteral("max_x").getInt() + 1;
	int y = solution.getLiteral("max_y").getInt() + 1;
	response = new ResponseObject(x, y);

	setPheromone(model);
	setFoodSources(model);
	setAnts(model);
	setHives(model);
	// setDiffusion(model); // ** May need later if decide that food emits scent
	return objectMapper.writeValueAsString(response);
    }

    private void setPheromone(Model model) {
	QueryExecution q = QueryExecutionFactory.create(qGetPheromone, model);
	ResultSet r = q.execSelect();
	r.forEachRemaining(s -> {
	    int x = s.getLiteral("x").getInt();
	    int y = s.getLiteral("y").getInt();
	    double level = s.getLiteral("lvl").getDouble();

	    if (response.topoi[y][x] == null) {
		response.topoi[y][x] = new Topos();
	    }

	    response.topoi[y][x].Pheromone = level;
	});
    }

    /**
     * private void setDiffusion(Model model) { QueryExecution q =
     * QueryExecutionFactory.create(getDiffusion, model); ResultSet r =
     * q.execSelect(); r.forEachRemaining(s -> { int x =
     * s.getLiteral("x").getInt(); int y = s.getLiteral("y").getInt(); double
     * level = s.getLiteral("total").getDouble();
     *
     * if (response.topoi[y][x] == null) { response.topoi[y][x] = new Topos(); }
     *
     * response.topoi[y][x].DiffusionTrace = level; }); }
     */
    private void setFoodSources(Model model) {
	QueryExecution q = QueryExecutionFactory.create(qGetFoodSources, model);
	ResultSet r = q.execSelect();
	r.forEachRemaining(s -> {
	    int x = s.getLiteral("x").getInt();
	    int y = s.getLiteral("y").getInt();
	    int capacity = 0;
	    int nutritionalValue = 0;
	    if (s.contains("capacity")) {
		capacity = s.getLiteral("capacity").getInt();
	    }
	    if (s.contains("nutri")) {
		nutritionalValue = s.getLiteral("nutri").getInt();
	    }
	    if (response.topoi[y][x] == null) {
		response.topoi[y][x] = new Topos();
	    }

	    FoodSource foodSource = new FoodSource();
	    foodSource.Capacity = capacity;
	    foodSource.NutritionalValue = nutritionalValue;
	    response.topoi[y][x].FoodSource = foodSource;
	}
	);
    }

    private void setHives(Model model) {
	QueryExecution q = QueryExecutionFactory.create(qGetHives, model);
	ResultSet r = q.execSelect();
	r.forEachRemaining(s -> {
	    if (s.contains("stock")) {
		int x = s.getLiteral("x").getInt();
		int y = s.getLiteral("y").getInt();
		if (response.topoi[y][x] == null) {
		    response.topoi[y][x] = new Topos();
		}

		Hive hive = new Hive();
		hive.Stock = s.getLiteral("stock").getInt();
		response.topoi[y][x].Hive = hive;
	    }
	});
    }

    private void setAnts(Model model) {
	QueryExecution q = QueryExecutionFactory.create(qGetAnts, model);
	ResultSet r = q.execSelect();
	r.forEachRemaining(s -> {

	    int x = s.getLiteral("x").getInt();
	    int y = s.getLiteral("y").getInt();
	    int carries = 0;

	    if (s.contains("carries")) {
		carries = s.getLiteral("pickremaining").getInt();
	    }

	    if (response.topoi[y][x] == null) {
		response.topoi[y][x] = new Topos();
	    }

	    Ant ant = new Ant();
	    ant.Carries = carries;
	    response.topoi[y][x].Ant = ant;
	});
    }

    private final String getTopoi = "PREFIX ex:<http://example.org/>\n"
	    + "PREFIX pos: <http://example.org/property/position#>\n"
	    + "PREFIX st:  <http://example.org/stigld/>\n"
	    + "SELECT (MAX(?x) as ?max_x)  (MAX(?y) as ?max_y) WHERE { ?s a st:Topos ; pos:xPos ?x ; pos:yPos ?y }";

    private final String qGetPheromone = "PREFIX ex:<http://example.org/>\n"
	    + "PREFIX pos: <http://example.org/property/position#>\n"
	    + "PREFIX st:  <http://example.org/stigld/>\n"
	    + "SELECT ?x ?y ?lvl  WHERE { ?s a st:Topos ; pos:xPos ?x ; pos:yPos ?y ; st:carries [ a ex:PheromoneTrace ; st:level ?lvl ] }";

    private final String getDiffusion = "PREFIX ex:<http://example.org/>\n"
	    + "PREFIX pos: <http://example.org/property/position#>\n"
	    + "PREFIX st:  <http://example.org/stigld/>\n"
	    + "SELECT  ?x ?y (SUM(?lvl) as ?total) where {\n"
	    + "  ?s a st:Topos;\n"
	    + "    pos:xPos ?x;\n"
	    + "    pos:yPos ?y;\n"
	    + "    st:carries ?stigma.\n"
	    + "  \n"
	    + "  ?stigma a ex:DiffusionTrace;\n"
	    + "          st:level ?lvl.\n"
	    + "} \n"
	    + "group by ?s ?x ?y";

    private final String qGetFoodSources = "PREFIX ex:<http://example.org/>\n"
	    + "PREFIX pos: <http://example.org/property/position#>\n"
	    + "PREFIX st:  <http://example.org/stigld/>\n"
	    + "SELECT ?x ?y ?nutri ?capacity WHERE {"
	    + "?source a ex:FoodSource ; ex:nutritionalValue ?nutri ; ex:totalAmount ?capacity ; "
	    + "	st:locatedAt [ a st:Topos ; pos:xPos ?x; pos:yPos ?y ] ."
	    + "}";

    private final String qGetAnts = "PREFIX ex:<http://example.org/>\n"
	    + "PREFIX pos: <http://example.org/property/position#>\n"
	    + "PREFIX st:  <http://example.org/stigld/>\n"
	    + "SELECT ?carries ?ant WHERE {\n"
	    + "?ant a ex:Ant ; st:locatedAt [ a st:Topos ; pos:xPos ?x ; pos:yPos ?y ] .\n"
	    + "OPTIONAL {?ant ex:carries ?carries}	\n"
	    + "}";

    private final String qGetHives = "PREFIX ex:<http://example.org/>\n"
	    + "PREFIX pos: <http://example.org/property/position#>\n"
	    + "PREFIX st:  <http://example.org/stigld/>\n"
	    + "\n"
	    + "SELECT ?hive ?stock WHERE {\n"
	    + "?hive a ex:AntHive ; st:locatedAt [ a st:Topos ; pos:xPos ?x ; pos:yPos ?y ] .\n"
	    + "OPTIONAL {?hive ex:foodStock ?stock}	\n"
	    + "}";

}
