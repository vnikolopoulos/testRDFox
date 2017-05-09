package di.madgik;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import uk.ac.ox.cs.JRDFox.JRDFoxException;

import java.net.URISyntaxException;

public class Main {

    public static void main(String[] args) throws OWLOntologyCreationException, JRDFoxException, URISyntaxException {
        System.out.println("<|*_*|>");
        Benchmark benchmark = new Benchmark();
        benchmark.simpleQuery();
        System.out.println("<|*_*|>");
    }

}


