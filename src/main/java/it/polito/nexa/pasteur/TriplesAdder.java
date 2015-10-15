package it.polito.nexa.pasteur;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Statement;

import java.util.List;

public interface TriplesAdder {

    public Model addTriples(Model model, List<Statement> statementList);

}
