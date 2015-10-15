package it.polito.nexa.pasteur.triplifiers;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.RDF;

import java.io.IOException;
import java.util.*;

public class PasteurTriplifier implements JSONTriplifier{

    private static String BASE_URI = "http://roarmap.nexacenter.org/id/";

    /**
     * Create a general list of Jena Statements from a JSON
     * @param inputJSON Input file
     * @param dataModel Ontology Mapper
     * @return A list of Jena Statements
     *
     */
    public List<Statement> triplifyJSON(String inputJSON, String dataModel) {

        List<Statement> results = new ArrayList<>();
        ObjectMapper inputJSONMapper = new ObjectMapper();
        ObjectMapper dataModelMapper = new ObjectMapper();

        try {
            JsonNode rootInputJSON = inputJSONMapper.readValue(inputJSON, JsonNode.class);
            JsonNode rootDataModel = dataModelMapper.readValue(dataModel, JsonNode.class);
            JsonNode mapperDataModel = rootDataModel.get(1).get("propertiesToMap").get(0);

            // Static statements
            Statement eprintIdentifier = ResourceFactory.createStatement(
                    ResourceFactory.createResource("http://roarmap.nexacenter.org/id/property/eprintIdentifier"),
                    ResourceFactory.createProperty("http://www.w3.org/2000/01/rdf-schema#subPropertyOf"),
                    ResourceFactory.createResource("http://purl.org/dc/terms/identifier"));

            results.add(eprintIdentifier);

            Statement policyComment = ResourceFactory.createStatement(
                    ResourceFactory.createResource("http://roarmap.nexacenter.org/id/property/policyComment"),
                    ResourceFactory.createProperty("http://www.w3.org/2000/01/rdf-schema#subPropertyOf"),
                    ResourceFactory.createResource("http://www.w3.org/2000/01/rdf-schema#comment"));

            results.add(policyComment);

            for (JsonNode node : rootInputJSON) {
                // Create eprint URI
                Resource eprintEntity = ResourceFactory.createResource(getValue("uri", node).replace("\"", ""));
                Resource eprintType = ResourceFactory.createResource(BASE_URI + "ontology/" + getValue("type", node));
                Statement eprint = ResourceFactory.createStatement(eprintEntity, RDF.type, eprintType);
                results.add(eprint);

                results.addAll(createSimpleLiterals(eprintEntity, node, mapperDataModel));
                results.addAll(createPlaceTriples(eprintEntity,
                                                  node.get("country_inclusive"),
                                                  mapperDataModel.get("country_inclusive").get(0)));
                results.addAll(createMandateTriples(eprintEntity,
                                                    node.get("mandate_content_types"),
                                                    mapperDataModel.get("mandate_content_types").get(0)));
            }
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return results;
    }

    public List<Statement> createSimpleLiterals(Resource subject, JsonNode node, JsonNode mapper) {
        List<Statement> results = new ArrayList<>();
        Iterator<Map.Entry<String, JsonNode>> nodeIterator = node.fields();
        while (nodeIterator.hasNext()) {
            Map.Entry<String, JsonNode> entry = (Map.Entry<String, JsonNode>) nodeIterator.next();
            String propertyMap = getValue(entry.getKey().toString(), mapper);

            if(!propertyMap.equals("") && !propertyMap.equals("notSet")) {
                Property property =  ResourceFactory.createProperty(getValue(entry.getKey().toString(), mapper));
                Literal literal = ResourceFactory.createPlainLiteral(entry.getValue().toString().replace("\"", ""));
                Statement basicStatement = ResourceFactory.createStatement(subject, property, literal);
                results.add(basicStatement);
            }
        }
        return results;
    }

    public List<Statement> createPlaceTriples(Resource subject, JsonNode node, JsonNode mapper) {
        List<Statement> results = new ArrayList<>();
        int index = 0;
        for(JsonNode key : node) {
            if(index > 0) {
                Property property = ResourceFactory.createProperty(mapper.get("value"+index).toString().replace("\"", ""));
                Resource value = ResourceFactory.createResource(BASE_URI + "UnGeoscheme/"+node.get(index).toString());
                Statement statement = ResourceFactory.createStatement(subject, property, value);
                results.add(statement);
            }
            index++;
        }
        return results;
    }

    public List<Statement> createMandateTriples(Resource subject, JsonNode node, JsonNode mapper) {
        List<Statement> results = new ArrayList<>();
        int index = 0;
        for(JsonNode key : node) {
            Property property = ResourceFactory.createProperty(mapper.get("content_type").toString().replace("\"", ""));
            Resource value = ResourceFactory.createResource(BASE_URI + "ontology/" + node.get(index).toString().replace("\"", ""));
            Statement statement = ResourceFactory.createStatement(subject, property, value);
            results.add(statement);
            index++;
        }
        return results;
    }

    public String cleanString(String s) {
        s = s.replaceAll("´", "'")
                .replaceAll("’", "")
                .replaceAll("'", "")
                .replaceAll("[“”]", "\"")
                .replaceAll("\"", "")
                .replaceAll("–", "-")
                .replaceAll("\t{2,}", "\t")
                .replaceAll(":", "")
                .replaceAll("°", "")
                .replaceAll("\\?", "")
                .replaceAll("[()]", "")
                .replaceAll("-", "")
                .replaceAll("\\.", "_")
                .replaceAll("\\[", "")
                .replaceAll("\\]","")
                .replaceAll(",", "")
                .replace(" ", "_")
                .replace("/", "_")
                .replaceAll("__", "_")
                .toLowerCase();
        return s;
    }

    private String getValue (String string, JsonNode record) {
        return record.get(string) != null ? record.get(string).asText() : "";
    }

}