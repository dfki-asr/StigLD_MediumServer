package de.dfki.StigLD;


import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;


@RestController
@RequestMapping("/sparql/")
public class Controller {
    @GetMapping("/getModel")
    public String getModel(@RequestParam(name = "query") String query) throws IOException {

        return query;
    }

    @PostMapping("/query")
    public String query(@RequestBody String query) throws IOException, UnirestException {
        initEvolve();
        Unirest.setTimeouts(0, 0);
        HttpResponse<String> response = Unirest.post("http://localhost:3230/ds/")
                .header("Content-Type", "application/sparql-query")
                .header("Accept", "text/turtle")
                .body(query)
                .asString();
        String resp;
        try{
            resp = response.getBody().toString();
        }
        catch (NullPointerException e)
        {
            resp = "No response";
        }
        return  resp;
    }

    @PostMapping("/update")
    public String postUpdate(@RequestBody String query) throws IOException, UnirestException {
        initEvolve();
        Unirest.setTimeouts(0, 0);
        HttpResponse<String> response = Unirest.post("http://localhost:3230/ds/")
                .header("Content-Type", "application/sparql-update")
                .body(query)
                .asString();
        String resp;
        try{
            resp = response.getBody().toString();
        }
        catch (NullPointerException e)
        {
            resp = "No response";
        }
        return  resp;
    }

    @PostMapping("/evolve")
    public String evolve(@RequestBody String query) throws IOException, UnirestException {
        System.out.println(query);
        Unirest.setTimeouts(0, 0);
        HttpResponse<String> response = Unirest.post("http://localhost:3230/ds/")
                .header("Content-Type", "application/sparql-update")
                .body(evolve)
                .asString();
        String resp;
        try{
            resp = response.getBody().toString();
        }
        catch (NullPointerException e)
        {
            resp = "No response";
        }
        return  resp;
    }

    public void initEvolve() throws UnirestException {
        Unirest.setTimeouts(0, 0);
        HttpResponse<String> response = Unirest.post("http://localhost:3230/ds/")
                .header("Content-Type", "application/sparql-update")
                .body(evolve)
                .asString();
    }

    public  String prefixes = "prefix ord:   <http://example.org/orders#> \n" +
            "prefix st:    <http://example.org/stigld/> \n" +
            "prefix ex:    <http://example.org/> \n" +
            "prefix law:   <http://example.org/rules#> \n" +
            "prefix task:  <http://example.org/tasks#> \n" +
            "prefix pos:   <http://example.org/property/position#> \n" +
            "prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
            "prefix xsd:   <http://www.w3.org/2001/XMLSchema#> \n" +
            "prefix en:    <http://example.org/entities#> \n" +
            "prefix stigFN: <http://www.dfki.de/func#>\n" +
            "prefix topos: <http://example.org/gridPoint/> \n";

    public String evolve = "PREFIX ex:<http://example.org/>\n" +
            "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
            "PREFIX dct: <http://purl.org/dc/terms/>\n" +
            "PREFIX st:  <http://example.org/stigld/>\n" +
            "PREFIX stigFN: <http://www.dfki.de/func#>\n" +
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
            "\n" +
            "DELETE {\n" +
            "  ?stigma a ex:negFeedback ; ?p ?o .\n" +
            "  ?topos st:carries ?stigma .\n" +
            "}\n" +
            "  INSERT {\n" +
            "  ?topos st:carries ?new .\n" +
            "  ?new a ex:negFeedback ; st:level ?c ; st:created ?now ; st:decayRate \"0.5\"^^xsd:double .\n" +
            "}\n" +
            "WHERE {\n" +
            "  BIND(NOW() as ?now)\n" +
            "  ?topos a st:Topos ; st:carries ?stigma .\n" +
            "  ?stigma a ex:negFeedback ; ?p ?o .\n" +
            "  FILTER (isBlank(?stigma))\n" +
            "\n" +
            "  {SELECT distinct ?topos (BNODE() as ?new) WHERE {\n" +
            "    ?topos a st:Topos .\n" +
            "    FILTER EXISTS { ?topos st:carries [ a ex:negFeedback ] .}\n" +
            "  }}\n" +
            "\n" +
            "  {SELECT (SUM(?c_i) as ?c) ?topos WHERE {\n" +
            "    ?topos st:carries [ a ex:negFeedback ; st:level ?lvl; st:created ?then; st:decayRate ?d ].\n" +
            "    BIND(stigFN:linear_decay(?then, now(), ?d, ?lvl) as ?c_i)\n" +
            "  } GROUP BY ?topos}\n" +
            "}";
}
