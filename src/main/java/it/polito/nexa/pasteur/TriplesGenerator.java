package it.polito.nexa.pasteur;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import it.polito.nexa.pasteur.importers.DefaultJSONImporter;
import it.polito.nexa.pasteur.triplifiers.PasteurTriplifier;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class TriplesGenerator {

    public static void main(String[] args) throws IOException {

        DefaultJSONImporter dji = new DefaultJSONImporter();
        String roarModel = dji.getJSON("src/main/resources/roarmap-model.json", "FILE");
        String roarFunders = dji.getJSON("http://roarmap.eprints.org/cgi/exportview/policymaker_type/funder/JSON/funder.js", "URL");
        String roarsUniversities = dji.getJSON("http://roarmap.eprints.org/cgi/exportview/policymaker_type/research=5Forg/JSON/research=5Forg.js", "URL");
        PasteurTriplifier pt = new PasteurTriplifier();
        Model baseModel = createBaseModel();
        baseModel.add(pt.triplifyJSON(roarFunders, roarModel));
        baseModel.add(pt.triplifyJSON(roarsUniversities, roarModel));
        publishRDF("output/rdf.nt", baseModel);
        //publishOnVirtuoso();
    }

    private static Model createBaseModel(){
        Model result = ModelFactory.createDefaultModel();
        Map<String, String> prefixMap = new HashMap<String, String>();

        prefixMap.put("rdfs", RDFS.getURI());
        prefixMap.put("geo", "http://www.w3.org/2003/01/geo/wgs84_pos#");
        prefixMap.put("schema", "http://schema.org/");
        prefixMap.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        prefixMap.put("gn", "http://www.geonames.org/ontology#");
        prefixMap.put("rdf", RDF.getURI());
        prefixMap.put("dcterms", DCTerms.getURI());

        result.setNsPrefixes(prefixMap);

        return result;
    }

    private static void publishRDF(String filePath, Model model) throws FileNotFoundException {
        File file = new File(filePath.replaceAll("(.+)/[^/]+", "$1"));
        file.mkdirs();
        OutputStream outTurtle = new FileOutputStream(new File(filePath));
        RDFDataMgr.write(outTurtle, model, RDFFormat.NTRIPLES);
    }

    private static void publishOnVirtuoso() throws IOException {
        URL url = new URL("http://localhost:8890/sparql-graph-crud-auth?graph-uri=test:put");
        HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
        httpCon.setDoOutput(true);
        httpCon.setRequestProperty("user", "dba:dba");
        httpCon.setRequestMethod("PUT");
        OutputStreamWriter out = new OutputStreamWriter(
                httpCon.getOutputStream());
        out.write("Resource content");
        out.close();
        try {
            httpCon.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
