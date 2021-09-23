package de.dfki.StigLD;


import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


@RestController
@RequestMapping("/sparql/")
public class Controller {
    @GetMapping("/getModel")
    public String getModel() throws IOException, UnirestException {
        initEvolve();
        Unirest.setTimeouts(0, 0);
        HttpResponse<String> response = Unirest.post("http://localhost:3230/ds/")
                .header("Content-Type", "application/sparql-query")
                .header("Accept", "text/turtle")
                .body(getAllTriples)
                .asString();
        String resp;
        try{
            resp = response.getBody();
        }
        catch (NullPointerException e)
        {
            resp = "No response";
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Model model = ModelFactory.createDefaultModel();
        model.read(IOUtils.toInputStream(resp, "UTF-8"), null, "TTL");
        model.write(stream, "TTL");
        String ret = stream.toString();
        return ret;
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
            resp = response.getBody();
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
            resp = response.getBody();
        }
        catch (NullPointerException e)
        {
            resp = "No response";
        }
        return  resp;
    }

    public void initEvolve() throws UnirestException {
        Unirest.setTimeouts(0, 0);
        HttpResponse<String> location_response = Unirest.post("http://localhost:3230/ds/")
                .header("Content-Type", "application/sparql-update")
                .body(evolve)
                .asString();
    }
    
    public String evolve = "prefix ex: <http://example.org/>\n"
	    + "prefix xsd: <http://www.w3.org/2001/XMLSchema#>\n"
	    + "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
	    + "prefix st: <http://dfki.de/stigld#>\n"
	    + "prefix stigFN: <http://www.dfki.de/func#>\n"
	    + "\n"
	    + "### DELETE OLD URGENCY STIGMATA\n"
	    + "DELETE {\n"
	    + "  ?package st:carries ?urgency .\n"
	    + "  ?stigma a st:Stigma , ex:UrgencyStigma ; st:concentration ?value ; ex:forTruck ?truck .\n"
	    + "}\n"
	    + "WHERE {\n"
	    + "  ?package st:carries ?urgency .\n"
	    + "  ?stigma a st:Stigma , ex:UrgencyStigma ; st:concentration ?value ; ex:forTruck ?truck .\n"
	    + "  FILTER(isBlank(?stigma))\n"
	    + "} ;\n"
	    + "\n"
	    + "### RECALCULATE LOCATION BASED STIGMATA ON CURRENT TIME\n"
	    + "INSERT {\n"
	    + "  ?package st:carries ?urgency .\n"
	    + "  ?urgency a st:Stigma , ex:UrgencyStigma ; st:concentration ?value ; ex:forTruck ?truck .\n"
	    + "} WHERE {\n"
	    + "  {SELECT DISTINCT (BNODE() as ?urgency) ?truck ?package WHERE {\n"
	    + "       ?truck a ex:Truck .\n"
	    + "       ?package a ex:Package .\n"
	    + "   }}\n"
	    + "\n"
	    + "  ex:confidence a ex:confidenceFactor ; rdf:value ?confidence .\n"
	    + "  ex:start a ex:startTime; rdf:value ?startTime .\n"
	    + "  BIND((stigFN:duration_secs(?startTime, NOW())) as ?timePassed)\n"
	    + "\n"
	    + "  ?package a ex:Package ; ex:located ?pickupLocation .\n"
	    + "\n"
	    + "  OPTIONAL { ?truck a ex:Truck ; ex:located [ a ex:Location ; ex:driveTime [ ex:destination ?pickupLocation ; rdf:value ?t ] ] .}\n"
	    + "  BIND(IF(bound(?t),?t,0) as ?driveTimeToPickup)"
	    + "  ?goal a ex:Goal ; ex:payload ?package ; ex:deadline ?deadline ; ex:destination [a ex:Location ; ex:driveTime [ ex:destination ?pickupLocation ; rdf:value ?driveTimeToDeliver ] ].\n"
	    + "  BIND ((?driveTimeToPickup + ?driveTimeToDeliver) as ?totalDriveTime)\n"
	    + "  BIND((?deadline - ?timePassed - ?totalDriveTime ) as ?remaining)\n"
	    + "  BIND((?totalDriveTime * ?confidence) as ?confidentRequired)\n"
	    + "\n"
	    + "  ## A parcel counts as \"urgent\" as soon as the remaining time is below the confidence threshold\n"
	    + "  FILTER(?remaining < ?confidentRequired)\n"
	    + "\n"
	    + "  BIND(IF(?remaining < 0 , 0, ?remaining) as ?r)\n"
	    + "  BIND((1.0 / (1.0+?r)) as ?value)\n"
	    + "} ;"
	    + ""
	    + ""
	    + "### RECALCULATE AREA BASED STIGMATA BASED ON CURRENT TIME\n"
	    + "INSERT {\n"
	    + "  ?package st:carries ?urgency .\n"
	    + "  ?urgency a st:Stigma , ex:UrgencyStigma ; st:concentration ?value ; ex:forTruck ?truck .\n"
	    + "} WHERE {\n"
	    + "  {SELECT DISTINCT (BNODE() as ?urgency) ?truck ?package WHERE {\n"
	    + "       ?truck a ex:Truck .\n"
	    + "       ?package a ex:Package .\n"
	    + "   }}\n"
	    + "\n"
	    + "   ex:confidence a ex:confidenceFactor ; rdf:value ?confidence .\n"
	    + "   ex:start a ex:startTime; rdf:value ?start .\n"
	    + "   ?truck a ex:Truck ; ex:areas ?area ; ex:located ?location .\n"
	    + "   ?package a ex:Package ; ex:located ?area .\n"
	    + "   ?location ex:driveTime [ ex:destination ?destination ; rdf:value ?driveTime ] .\n"
	    + "   ?goal a ex:Goal ; ex:payload ?package ; ex:destination ?destination ; ex:deadline ?deadline .\n"
	    + "\n"
	    + "   BIND(stigFN:duration_secs(?start, NOW()) as ?timePassed)\n"
	    + "   BIND((?deadline-?timePassed) as ?timeRemaining)\n"
	    + "   BIND(?confidence * ?driveTime as ?estimateRequired)\n"
	    + "   BIND(IF(?timeRemaining - ?driveTime > 0 , ?timeRemaining - ?driveTime , 0) as ?estimateLeft)\n"
	    + "   BIND((1.0/(1.0 + ?estimateLeft)) as ?value)\n"
	    + "   FILTER(?timeRemaining < ?estimateRequired)\n"
	    + "   FILTER(?location != ?destination)\n"
	    + "}";
            public String getAllTriples = "prefix ord:   <http://example.org/orders#>\n" +
                    "prefix st:    <http://example.org/stigld/> \n" +
                    "prefix ex:    <http://example.org/> \n" +
                    "prefix law:   <http://example.org/rules#> \n" +
                    "prefix task:  <http://example.org/tasks#> \n" +
                    "prefix pos:   <http://example.org/property/position#> \n" +
                    "prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
                    "prefix xsd:   <http://www.w3.org/2001/XMLSchema#> \n" +
                    "prefix en:    <http://example.org/entities#> \n" +
                    "prefix topos: <http://example.org/gridPoint/> \n"+
                    "DESCRIBE * WHERE {?s ?p ?o.}\n";

}
