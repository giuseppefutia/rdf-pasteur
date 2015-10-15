package it.polito.nexa.pasteur;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;

import java.util.List;

public class DefaultTriplesAdder implements TriplesAdder {

    @Override
    public Model addTriples(Model model, List<Statement> statementList) {
        Model result = ModelFactory.createDefaultModel();
        result.add(statementList);
        return result;
    }
}
