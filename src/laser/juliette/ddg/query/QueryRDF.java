package laser.juliette.ddg.query;

import java.util.Iterator;

import laser.ddg.persist.Properties;
import laser.ddg.persist.RdfModelFactory;
import laser.juliette.ddg.persist.JenaLoader;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;

/**
 * Performs queries on a given database
 *
 * @author Sophia. Created Dec 6, 2011.
 */
public class QueryRDF {
	
	/**
	 * Returns snipped of DDG representing the first execution of a Little-JIL step
	 * @param stepName the name of the Little-JIL step
	 * @param modelDir the directory of the RDF database being queried
	 */
	public void getFirstExecQuery(String stepName, String modelDir){
//		/*
//		 * // create an empty model Model model = RdfModelFactory.getNewModel();
//		 * /* // use the FileManager to find the input file InputStream in =
//		 * FileManager.get().open(
//		 * "C:/Users/Sophia/workspace/juliette-ddgbuilder/pexample.txt" ); if
//		 * (in == null) { throw new IllegalArgumentException( "File: " +
//		 * "C:/Users/Sophia/workspace/juliette-ddgbuilder/pexample.txt" +
//		 * " not found"); }
//		 * 
//		 * // read the RDF/XML file model.read(in, null);
//		 * 
//		 * // write it to standard out model.write(System.out);
//		 */
//		
//		//Model model = RdfModelFactory
//		//		.getModel("C:/Users/Sophia/Jena/db1325877456447");
//		Model model = RdfModelFactory
//						.getModel(modelDir);
//		
//		// find first time a given little-JIL step was executed
//		String allStartNodesWithName = "PREFIX  rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
//				+ "PREFIX  j.1:  <http://allnodes/sins/>"
//				+ "PREFIX  j.0:  <http://allnodes/dins/>"
//				+ "SELECT ?s WHERE  { ?s j.1:propertiesStep \"" + stepName + "\" . ?s j.1:propertiesSinType \"Start\"}";
//
//		Query query0 = QueryFactory.create(allStartNodesWithName);
//
//	  // Execute the query and obtain results
//		QueryExecution qe0 = QueryExecutionFactory.create(query0, model);
//		ResultSet results0 = qe0.execSelect();
//
//		ResultSetRewindable rewindableRes0 = ResultSetFactory
//				.makeRewindable(results0);
//		ResultSetFormatter.out(System.out, rewindableRes0, query0);
//		rewindableRes0.reset();
//		int minStartId = (int) model.size();
//		Properties p = new Properties(model, null);
//		while (rewindableRes0.hasNext()) {
//			QuerySolution qs0 = rewindableRes0.next();
//			Iterator<String> sinVarNames = qs0.varNames();
//			String currentName = sinVarNames.next();
//			//Resource currentRes = qs0.getResource(currentName);
//			// Literal namePropertytoChar = retrieveSinName(qs0, currentName);
//			Literal idPropertytoChar = DisplayRdfDb.retrieveSinId(qs0,
//					currentName);
//			int currentId = Integer.parseInt(idPropertytoChar.getLexicalForm());
//	
//			if (currentId < minStartId) {
//				minStartId = currentId;
//			}
//		}
//	
//		String allFinishNodesWithName = "PREFIX  rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
//				+ "PREFIX  j.1:  <http://allnodes/sins/>"
//				+ "PREFIX  j.0:  <http://allnodes/dins/>"
//				+ "SELECT ?s WHERE  { ?s j.1:propertiesStep \"" + stepName + "\". ?s j.1:propertiesSinType \"Finish\"}";
//
//		Query query2 = QueryFactory.create(allFinishNodesWithName);
//	  
//		  // Execute the query and obtain results
//		  QueryExecution qe2 = QueryExecutionFactory.create(query2, model);
//		  ResultSet results2 = qe2.execSelect();
//	
//		ResultSetRewindable rewindableRes2 = ResultSetFactory
//				.makeRewindable(results2);
//		ResultSetFormatter.out(System.out, rewindableRes2, query2);
//		rewindableRes2.reset();
//		int minFinishId= (int) model.size();
//		while (rewindableRes2.hasNext()) {
//			QuerySolution qs2 = rewindableRes2.next();
//			Iterator<String> sinVarNames = qs2.varNames();
//			String currentName = sinVarNames.next();
//			// Resource currentRes = qs2.getResource(currentName);
//			Literal idPropertytoChar = DisplayRdfDb.retrieveSinId(qs2,
//					currentName);
//			int currentId = Integer.parseInt(idPropertytoChar.getLexicalForm());
//
//			if (currentId < minFinishId) {
//				minFinishId = currentId;
//			}
//		}
//	
//		// get first execution of the given Little-JIL step
//		DisplayRdfDb drd = new DisplayRdfDb(model, minStartId, minFinishId);
//		drd.display();
//		/*
//		 * String queryString =
//		 * "PREFIX  rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
//		 * "PREFIX  j.1:  <http://allnodes/sins/>" +
//		 * "PREFIX  j.0:  <http://allnodes/dins/>" +
//		 * "SELECT ?s WHERE  { ?din j.0:propertiesDinUsers ?s . ?din j.0:propertiesDinDDGId \"4\"}"
//		 * ; // "SELECT ?s WHERE  { ?s j.1:propertiesSinName \"Root Interm\"}";
//		 * 
//		 * Query query = QueryFactory.create(queryString);
//		 * 
//		 * // Execute the query and obtain results QueryExecution qe =
//		 * QueryExecutionFactory.create(query, model); ResultSet results =
//		 * qe.execSelect();
//		 * 
//		 * ResultSetFormatter.out(System.out, results, query); qe.close();
//		 * 
//		 * 
//		 * // get the producer of DIN4 String queryString1 =
//		 * "PREFIX  rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
//		 * "PREFIX  j.1:  <http://allnodes/sins/>" +
//		 * "PREFIX  j.0:  <http://allnodes/dins/>" +
//		 * "SELECT ?dinproducer WHERE  { ?din j.0:propertiesDinProducer ?dinproducer . ?din j.0:propertiesDinDDGId \"4\"}"
//		 * ;
//		 * 
//		 * Query query1 = QueryFactory.create(queryString1);
//		 * 
//		 * QueryExecution qe1 = QueryExecutionFactory.create(query1, model);
//		 * ResultSet results1 = qe1.execSelect(); ResultSetRewindable
//		 * rewindableRes1 = ResultSetFactory .makeRewindable(results1);
//		 * ResultSetFormatter.out(System.out, rewindableRes1, query1); //
//		 * qe1.close(); rewindableRes1.reset(); QuerySolution qs =
//		 * rewindableRes1.next(); Iterator<String> sinVarNames = qs.varNames();
//		 * 
//		 * while (sinVarNames.hasNext()) { String currentName =
//		 * sinVarNames.next(); Statement nameProperty =
//		 * qs.getResource(currentName).getProperty(
//		 * laser.juliette.persist.Properties.SinName); Literal
//		 * namePropertytoChar = nameProperty.getLiteral(); // get first
//		 * predecessors of the din's producer String queryString2 =
//		 * "PREFIX  rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
//		 * "PREFIX  j.1:  <http://allnodes/sins/>" +
//		 * "PREFIX  j.0:  <http://allnodes/dins/>" +
//		 * "SELECT ?pred1 WHERE  { ?sin j.1:propertiesSinPredecessors ?pred1 . ?sin j.1:propertiesSinName \""
//		 * + namePropertytoChar + "\"}"; Query query2 =
//		 * QueryFactory.create(queryString2); // Execute the query and obtain
//		 * results QueryExecution qe2 = QueryExecutionFactory.create(query2,
//		 * model); ResultSet results2 = qe2.execSelect(); }
//		 * 
//		 * // Output query results ResultSetFormatter.out(System.out, results1,
//		 * query1);
//		 * 
//		 * // Important - free up resources used running the query qe1.close();
//		 */
	}
	
	
	
	
	
	
	
	
	
	
	
//	public void testQueryRDF(){
//		/*
//		 * // create an empty model Model model = RdfModelFactory.getNewModel();
//		 * /* // use the FileManager to find the input file InputStream in =
//		 * FileManager.get().open(
//		 * "C:/Users/Sophia/workspace/juliette-ddgbuilder/pexample.txt" ); if
//		 * (in == null) { throw new IllegalArgumentException( "File: " +
//		 * "C:/Users/Sophia/workspace/juliette-ddgbuilder/pexample.txt" +
//		 * " not found"); }
//		 * 
//		 * // read the RDF/XML file model.read(in, null);
//		 * 
//		 * // write it to standard out model.write(System.out);
//		 */
//		Model model = RdfModelFactory
//				.getModel("C:/Users/Sophia/Jena/db1325877456447");
//
//		// find first time a given little-JIL step was executed
//		String allStartNodesWithName = "PREFIX  rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
//				+ "PREFIX  j.1:  <http://allnodes/sins/>"
//				+ "PREFIX  j.0:  <http://allnodes/dins/>"
//				+ "SELECT ?s WHERE  { ?s j.1:propertiesStep \"ParallelParent\" . ?s j.1:propertiesSinType \"Start\"}";
//
//		Query query0 = QueryFactory.create(allStartNodesWithName);
//
//	  // Execute the query and obtain results
//		QueryExecution qe0 = QueryExecutionFactory.create(query0, model);
//		ResultSet results0 = qe0.execSelect();
//
//		ResultSetRewindable rewindableRes0 = ResultSetFactory
//				.makeRewindable(results0);
//		ResultSetFormatter.out(System.out, rewindableRes0, query0);
//		rewindableRes0.reset();
//		int minStartId = (int) model.size();
//		Properties p = new Properties(model, null);
//		while (rewindableRes0.hasNext()) {
//			QuerySolution qs0 = rewindableRes0.next();
//			Iterator<String> sinVarNames = qs0.varNames();
//			String currentName = sinVarNames.next();
//			//Resource currentRes = qs0.getResource(currentName);
//			// Literal namePropertytoChar = retrieveSinName(qs0, currentName);
//			Literal idPropertytoChar = DisplayRdfDb.retrieveSinId(qs0,
//					currentName);
//			int currentId = Integer.parseInt(idPropertytoChar.getLexicalForm());
//	
//			if (currentId < minStartId) {
//				minStartId = currentId;
//			}
//		}
//	
//		String allFinishNodesWithName = "PREFIX  rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
//				+ "PREFIX  j.1:  <http://allnodes/sins/>"
//				+ "PREFIX  j.0:  <http://allnodes/dins/>"
//				+ "SELECT ?s WHERE  { ?s j.1:propertiesStep \"ParallelParent\" . ?s j.1:propertiesSinType \"Finish\"}";
//
//		Query query2 = QueryFactory.create(allFinishNodesWithName);
//	  
//		  // Execute the query and obtain results
//		  QueryExecution qe2 = QueryExecutionFactory.create(query2, model);
//		  ResultSet results2 = qe2.execSelect();
//	
//		ResultSetRewindable rewindableRes2 = ResultSetFactory
//				.makeRewindable(results2);
//		ResultSetFormatter.out(System.out, rewindableRes2, query2);
//		rewindableRes2.reset();
//		int minFinishId= (int) model.size();
//		while (rewindableRes2.hasNext()) {
//			QuerySolution qs2 = rewindableRes2.next();
//			Iterator<String> sinVarNames = qs2.varNames();
//			String currentName = sinVarNames.next();
//			// Resource currentRes = qs2.getResource(currentName);
//			Literal idPropertytoChar = DisplayRdfDb.retrieveSinId(qs2,
//					currentName);
//			int currentId = Integer.parseInt(idPropertytoChar.getLexicalForm());
//
//			if (currentId < minFinishId) {
//				minFinishId = currentId;
//			}
//		}
//	
//		// get first execution of the given Little-JIL step
//		DisplayRdfDb drd = new DisplayRdfDb(model, minStartId, minFinishId);
//		drd.display();
//		/*
//		 * String queryString =
//		 * "PREFIX  rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
//		 * "PREFIX  j.1:  <http://allnodes/sins/>" +
//		 * "PREFIX  j.0:  <http://allnodes/dins/>" +
//		 * "SELECT ?s WHERE  { ?din j.0:propertiesDinUsers ?s . ?din j.0:propertiesDinDDGId \"4\"}"
//		 * ; // "SELECT ?s WHERE  { ?s j.1:propertiesSinName \"Root Interm\"}";
//		 * 
//		 * Query query = QueryFactory.create(queryString);
//		 * 
//		 * // Execute the query and obtain results QueryExecution qe =
//		 * QueryExecutionFactory.create(query, model); ResultSet results =
//		 * qe.execSelect();
//		 * 
//		 * ResultSetFormatter.out(System.out, results, query); qe.close();
//		 * 
//		 * 
//		 * // get the producer of DIN4 String queryString1 =
//		 * "PREFIX  rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
//		 * "PREFIX  j.1:  <http://allnodes/sins/>" +
//		 * "PREFIX  j.0:  <http://allnodes/dins/>" +
//		 * "SELECT ?dinproducer WHERE  { ?din j.0:propertiesDinProducer ?dinproducer . ?din j.0:propertiesDinDDGId \"4\"}"
//		 * ;
//		 * 
//		 * Query query1 = QueryFactory.create(queryString1);
//		 * 
//		 * QueryExecution qe1 = QueryExecutionFactory.create(query1, model);
//		 * ResultSet results1 = qe1.execSelect(); ResultSetRewindable
//		 * rewindableRes1 = ResultSetFactory .makeRewindable(results1);
//		 * ResultSetFormatter.out(System.out, rewindableRes1, query1); //
//		 * qe1.close(); rewindableRes1.reset(); QuerySolution qs =
//		 * rewindableRes1.next(); Iterator<String> sinVarNames = qs.varNames();
//		 * 
//		 * while (sinVarNames.hasNext()) { String currentName =
//		 * sinVarNames.next(); Statement nameProperty =
//		 * qs.getResource(currentName).getProperty(
//		 * laser.juliette.persist.Properties.SinName); Literal
//		 * namePropertytoChar = nameProperty.getLiteral(); // get first
//		 * predecessors of the din's producer String queryString2 =
//		 * "PREFIX  rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
//		 * "PREFIX  j.1:  <http://allnodes/sins/>" +
//		 * "PREFIX  j.0:  <http://allnodes/dins/>" +
//		 * "SELECT ?pred1 WHERE  { ?sin j.1:propertiesSinPredecessors ?pred1 . ?sin j.1:propertiesSinName \""
//		 * + namePropertytoChar + "\"}"; Query query2 =
//		 * QueryFactory.create(queryString2); // Execute the query and obtain
//		 * results QueryExecution qe2 = QueryExecutionFactory.create(query2,
//		 * model); ResultSet results2 = qe2.execSelect(); }
//		 * 
//		 * // Output query results ResultSetFormatter.out(System.out, results1,
//		 * query1);
//		 * 
//		 * // Important - free up resources used running the query qe1.close();
//		 */
//}
}
