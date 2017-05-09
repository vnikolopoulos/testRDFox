package di.madgik;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import uk.ac.ox.cs.JRDFox.JRDFoxException;
import uk.ac.ox.cs.JRDFox.Prefixes;
import uk.ac.ox.cs.JRDFox.store.Resource;
import uk.ac.ox.cs.JRDFox.store.DataStore.Format;
import uk.ac.ox.cs.JRDFox.store.DataStore;
import uk.ac.ox.cs.JRDFox.store.TupleIterator;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Created by vaggelis on 9/5/2017.
 */
public class Benchmark {
    public void simpleQuery() throws JRDFoxException {
        System.out.println("Running a very simple Query on RDFox");
        DataStore store = new DataStore(DataStore.StoreType.ParallelSimpleNN);
        DataStore store1 = new DataStore(DataStore.StoreType.ParallelSimpleNN);
        try {
            System.out.println("Setting the number of threads...");
            store.setNumberOfThreads(2);
            store.importFiles(new File[] {new File("data/simple1.ttl")});
            System.out.println("Number of tuples after import: " + store.getTriplesCount());

            Prefixes prefixes = Prefixes.DEFAULT_IMMUTABLE_INSTANCE;
            System.out.println("Retrieving all properties before materialisation.");
            TupleIterator tupleIterator = store.compileQuery(
                    "SELECT  ?x ?y ?z ?w WHERE{ ?x <r1> ?y . ?x <r2> ?z . ?z <r3> ?w}", prefixes);

            evaluateAndPrintResults(prefixes, tupleIterator);
            evaluateAndPrintResultsAsTriples(prefixes, tupleIterator);
            store1.setNumberOfThreads(2);
            store1.importFiles(new File[] {new File("data/result1.ttl")});
            System.out.println("Number of tuples after import: " + store1.getTriplesCount());

            System.out.println("This should be the same as");
            tupleIterator = store1.compileQuery(
                    "SELECT  ?x ?y ?z ?w WHERE{ ?ans <term0> ?x . ?ans <term1> ?y . ?ans <term2> ?z . ?ans <term3> ?w}", prefixes);
            evaluateAndPrintResults(prefixes, tupleIterator);
        } finally {
            // When no longer needed, the data store should be disposed so that all related resources are released.
            store.dispose();
            store1.dispose();
        }
    }

    public void demo() throws JRDFoxException, URISyntaxException, OWLOntologyCreationException {
        // This example shows how to instantiate JRDFox in Java, load an RDF file in Turtle, evaluate
        // a query, perform reasoning (i.e., extend the set of triples with all implicit ones), and
        // then re-evaluate the query (and get more results as a the result of reasoning).

        // Unlike most RDF stores, JRDFox currently maintains a separation between RDF data and the
        // ontology. That is, the data (i.e., the ABox) and the ontology (i.e., the TBox) are currently
        // best maintained in separate files. This is quite different to many (most?) existing RDF
        // stores, where both the ontology and the data are kept in a single file. Eventually, we will
        // further develop JRDFox so that this is not necessary (i.e., so that only one RDF file,
        // containing both the data and the ontology can be loaded); however, we are not there yet.
        // Thus, in this example we will use two kinds of axioms for reasoning:
        //
        // - axioms from an OWL ontology stored in file called univ-bench.owl, and
        //
        // - manually created rules written in a format proprietary to RDFox and stored in a file
        //   called additional-rules.txt.
        //
        // The data will be loaded from a file called lubm1.ttl. All files are located in the
        // source directory and will be loaded through Java class loaders -- please refer to Java
        // documentation for more details.

        // In this example, the rules are kept in a file separate from the ontology. JRDFox supports
        // SWRL rules; thus, it is possible to store the rules into the OWL ontology. However, JRDFox
        // does not (yet) support SWRL built-in predicates, so any rules involving built-in predicates
        // should be written in the native format of RDFox. The format of the rules should be obvious
        // from the example. Built-in functions are invoked using the BIND and FILTER syntax from
        // SPARQL, and most SPARQL built-in functions are supported.

        // We first load an OWL ontology. This is done using the OWL API -- a well-known API for
        // manipulating OWL ontologies. The details of how to use OWL API are out of scope of this
        // example; please refer to http://owlapi.sourceforge.net/ for more information. Since the
        // ontology is loaded through the OWL API, it can be in any format (i.e., not only Turtle).
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File("data/univ-bench.owl"));
        String additionalRules = "PREFIX a1: <http://swat.cse.lehigh.edu/onto/univ-bench.owl#>"
                + "a1:publicationsAddress(?P,?WA) :- a1:Person(?P), a1:memberOf(?P,?O), a1:name(?P,?N), BIND(CONCAT(STR(?O), \"/\", ?N, \"/publications/\") AS ?WA) .";

        // We now create the data store. RDFox supports different types of stores described in DataStore.StoreType,
        // each with the option of native equality reasoning.
        DataStore store = new DataStore(DataStore.StoreType.ParallelSimpleNN);

        // Many classes in JRDFox use various kinds of resources. For example, the DataStore class can
        // use quite a bit of memory for triple storage. These resources can be released explicitly by
        // calling the dispose() method on the relevant class. This method will be automatically called
        // when an object is garbage collected; however, Java provides no guarantees on when/if this would
        // be done. Therefore, it is good practice to explicitly call the dispose() methods whenever one
        // has finished using the object. This can often be achieved using a try - finally block, as shown.
        try {
            // We next specify how many threads the store should use during import of data and reasoning.
            // This method is only applicable for stores NarrowParallelHead and WideParallelHead
            System.out.println("Setting the number of threads...");
            store.setNumberOfThreads(2);

            // We next import the RDF data into the store. At present, only Turtle/N-triples files are supported.
            // At the moment, please convert RDF/XML files into Turtle format to load into JRDFox, and rest
            // assured that we are working on an RDF/XML parser.
            System.out.println("Importing RDF data...");
            store.importFiles(new File[] {new File("data/lubm1.ttl")});
            System.out.println("Number of tuples after import: " + store.getTriplesCount());

            // We now evaluate a SPARQL query. Queries need to be compiled into a TupleIterator before they
            // can be evaluated. The DataStore.compileQuery() method accepts the query string and two
            // additional parameters. The first one is an instance of the Prefixes object which can define
            // IRI prefixes not explicitly mentioned in the query. These can be included into the query using
            // the 'PREFIX' directive, but in some applications it can be easier to just pass them around in
            // an object. The second parameter is an instance of the Parameters class, which determines various
            // aspects of query evaluation. For the moment, it is best to use the default parameters.
            Prefixes prefixes = Prefixes.DEFAULT_IMMUTABLE_INSTANCE;
            System.out.println("Retrieving all properties before materialisation.");
            TupleIterator tupleIterator = store.compileQuery("SELECT DISTINCT ?y WHERE{ ?x ?y ?z }", prefixes);
            try {
                // The iterator is typically used in a loop as shown in the evauateAndPrintResults() method. The
                // concept is similar to the ResultSet object from JDBC. An iterator can be seen as a collection
                // of rows, each of which consists of a certain number of columns; this number is called the arity
                // of the iterator. The value in each column of a row is an object of type GroundTerm, and it can
                // be an Individual, a BlankNode, or a Literal. Each row also has a multiplicity, which says how
                // many times does the row occur in the result set. That is, if the query contains the 'DISTINCT'
                // clause, then repetitions of the same tuples of value are represented as just one row in the
                // iterator, but with the multiplicity set to the number of occurrences of the tuple in the query
                // answer. The TupleIterator.getNext() method returns the multiplicity of the next tuple, or 0 if
                // no such tuple exists. Before reading the query result, the iterator must be opened; the result
                // value is the multiplicity of the first tuple. The same iterator can be opened multiple times;
                // hence, we will keep the iterator around and re-execute the query later in this example.
                evaluateAndPrintResults(prefixes, tupleIterator);

                // We now add the ontology and the custom rules to the data. The DataStore.addOntology() method
                // takes as arguments the ontology converts it into rules and adds those rules to the store.
                // Additionally, one can use the method DataStore.importRules() to import the rules that reside
                // in a file and the method DataStore.addRules() to add rules stored in a string.
                System.out.println("Adding the ontology to the store...");
                store.importOntology(ontology);
                System.out.println("Importing rules from a file...");
                store.importFiles(new File[] { new File("data/additional-rules.txt") } );
                System.out.println("Adding rules from code...");
                store.importText(additionalRules);
                System.out.println("Apply the rules in the store against the current facts...");
                store.applyReasoning();
                System.out.println("Number of tuples after materialization: " + store.getTriplesCount());

                // We now evaluate the same query again. The result shows that the set of facts changed as a
                // consequence of materialization. Note that we do not need to create the iterator again: we
                // can simply reuse the existing one.
                evaluateAndPrintResults(prefixes, tupleIterator);

                // RDFox supports incremental reasoning. One can import facts into the store incrementally by
                // calling DataStore.importTurtleFiles() with additional argument DataStore.UpdateType.ScheduleForAddition.
                System.out.println("Import triples for incremental reasoning");
                store.importFiles(new File[] { new File("data/lubm1-new.ttl") }, DataStore.UpdateType.ScheduleForAddition);
                // The triples scheduled for addition become visible for querying only after incremental reasoning has taken place.
                // Hence the number of triples is the same as before the scheduling operation.
                System.out.println("Number of tuples after scheduling for addition: " + store.getTriplesCount());
                // One can now apply the rules incrementally by calling DataStore.applyReasoning() with incrementally=true.
                System.out.println("Applying the rules incrementally");
                store.applyReasoning(true);
                System.out.println("Number of tuples after incremental reasoning: " + store.getTriplesCount());
                // One can export the facts from the current store into a file as follows
                File finalFactsFile = File.createTempFile("final-facts", ".ttl");
                System.out.print("Exporting facts to file '" + finalFactsFile + "' ... ");
                store.export(finalFactsFile , DataStore.Format.NTriples);
                System.out.println("done.");
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                // When no longer needed, the iterator should be disposed so that all related resources are released.
                tupleIterator.dispose();
            }
        }
        finally {
            // When no longer needed, the data store should be disposed so that all related resources are released.
            store.dispose();
        }
        System.out.println("This is the end of the example!");
    }

    public void evaluateAndPrintResults(Prefixes prefixes, TupleIterator tupleIterator) throws JRDFoxException {
        int numberOfRows = 0;
        System.out.println();
        System.out.println("=======================================================================================");
        int arity = tupleIterator.getArity();
        // We iterate trough the result tuples
        for (long multiplicity = tupleIterator.open(); multiplicity != 0; multiplicity = tupleIterator.advance()) {
            // We iterate trough the terms of each tuple

            for (int termIndex = 0; termIndex < arity; ++termIndex) {
                if (termIndex != 0)
                    System.out.print("  ");
                // For each term we get a Resource object that contains the lexical form and the data type of the term.
                // One can also access terms as GroundTerm objects from the uk.ac.ox.cs.JRDFox.model package using the
                // method TupleIterator.getGroundTerm(int termIndex). Using objects form the uk.ac.ox.cs.JRDFox.model
                // package has the benefit of ensuring that at any point each term is represented by at most one Java
                // object. This benefit, however, comes at a price, since, unlike in the case of Resource objects, the
                // creation of GroundTerm objects involves a hashtable lookup, which in some cases can lead to a
                // significant overhead.
                Resource resource = tupleIterator.getResource(termIndex);
                System.out.print(resource.toString(prefixes));
            }
            System.out.print(" * ");
            System.out.print(multiplicity);
            System.out.println();
            ++numberOfRows;
        }
        System.out.println("---------------------------------------------------------------------------------------");
        System.out.println("  The number of rows returned: " + numberOfRows);
        System.out.println("=======================================================================================");
        System.out.println();
    }

    public void evaluateAndPrintResultsAsTriples(Prefixes prefixes, TupleIterator tupleIterator) throws JRDFoxException {
        int numberOfRows = 0;
        System.out.println();
        System.out.println("=======================================================================================");
        int arity = tupleIterator.getArity();
        // We iterate trough the result tuples
        for (long multiplicity = tupleIterator.open(); multiplicity != 0; multiplicity = tupleIterator.advance()) {
            // We iterate trough the terms of each tuple
            ++numberOfRows;
            for (int termIndex = 0; termIndex < arity; ++termIndex) {

                Resource resource = tupleIterator.getResource(termIndex);
                System.out.println("<ans" + numberOfRows +"> <term" + termIndex  + "> " +
                        resource.toString(prefixes) + " .");
            }

        }
        System.out.println("---------------------------------------------------------------------------------------");
        System.out.println("  The number of rows returned: " + numberOfRows);
        System.out.println("=======================================================================================");
        System.out.println();
    }
}
