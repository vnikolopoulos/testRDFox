package di.madgik;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import uk.ac.ox.cs.JRDFox.JRDFoxException;

import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;

public class Main {

    public static void main(String[] args) throws OWLOntologyCreationException, JRDFoxException, URISyntaxException {
        System.out.println("<|*_*|>");
        Instant start = Instant.now();
        Benchmark benchmark = new Benchmark();
       // benchmark.simpleQuery();
        Generator generator = new Generator();
        //generator.generateV2("data/generated1000000V2-0.0000001.ttl",0.0000001, 1000000);
        //generator.generateV2("data/generated1000000V2-0.000001.ttl",0.000001, 1000000);
        //
        String selectivity = "0.0001";
        String inputsize = "100000";
        String generatorVersion = "2";
        String inputDataFile = "data/generated"+inputsize+"V"+generatorVersion+"-"+selectivity+".ttl";
        String resultsFile = "data/result"+inputsize+"V"+generatorVersion+"-"+selectivity+".ttl";
        String headsFile = "data/result"+inputsize+"V"+generatorVersion+"-"+selectivity+"-heads.ttl";
        //generator.generateV2(inputDataFile, Double.parseDouble(selectivity), Integer.parseInt(inputsize));
        benchmark.simpleQueryBench(inputDataFile);
        benchmark.simpleQueryBenchUsingHeads(inputDataFile, headsFile);
        benchmark.simpleAlternativeQueryBench(resultsFile);
        Instant end = Instant.now();
        System.out.println("Total execution time: " + Duration.between(start, end));
        System.out.println("<|*_*|>");
    }

}


