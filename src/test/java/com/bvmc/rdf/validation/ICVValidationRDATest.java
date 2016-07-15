package com.bvmc.rdf.validation;

import static com.complexible.common.openrdf.util.ExpressionFactory.cardinality;
import static com.complexible.common.openrdf.util.ExpressionFactory.dataProperty;
import static com.complexible.common.openrdf.util.ExpressionFactory.domain;
import static com.complexible.common.openrdf.util.ExpressionFactory.min;
import static com.complexible.common.openrdf.util.ExpressionFactory.range;
import static com.complexible.common.openrdf.util.ExpressionFactory.subClassOf;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;

import org.junit.Test;
import org.openrdf.model.IRI;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.BindingSet;
import org.openrdf.rio.RDFFormat;

import com.complexible.common.base.CloseableIterator;
import com.complexible.common.openrdf.model.Models2;
import com.complexible.common.openrdf.vocabulary.Vocabulary;
import com.complexible.common.protocols.server.Server;
import com.complexible.common.rdf.model.Values;
import com.complexible.stardog.ContextSets;
import com.complexible.stardog.Stardog;
import com.complexible.stardog.StardogException;
import com.complexible.stardog.api.Connection;
import com.complexible.stardog.api.ConnectionConfiguration;
import com.complexible.stardog.api.admin.AdminConnection;
import com.complexible.stardog.api.admin.AdminConnectionConfiguration;
import com.complexible.stardog.icv.Constraint;
import com.complexible.stardog.icv.ConstraintFactory;
import com.complexible.stardog.icv.ConstraintViolation;
import com.complexible.stardog.icv.ICV;
import com.complexible.stardog.icv.api.ICVConnection;
import com.complexible.stardog.protocols.snarl.SNARLProtocolConstants;
import com.google.common.base.Strings;

/**
 * <p>Source code for the examples in the Stardog ICV documentation.</p>
 *
 * @author  Gustavo Candela
 * @since   0.1
 * @version 1.0
 */
public class ICVValidationRDATest {
	
	private static ICVConnection aValidator; 
	private static final Vocabulary rdacVocab = new Vocabulary("http://rdaregistry.info/Elements/c/");
	private static final Vocabulary rdawVocab = new Vocabulary("http://rdaregistry.info/Elements/w/");
	private static final Vocabulary rdaeVocab = new Vocabulary("http://rdaregistry.info/Elements/e/");
	private static final Vocabulary rdamVocab = new Vocabulary("http://rdaregistry.info/Elements/m/");
	//private static final Vocabulary rdaiVocab = new Vocabulary("http://rdaregistry.info/Elements/i/");
	private static final Vocabulary rdaaVocab = new Vocabulary("http://rdaregistry.info/Elements/a/");
	private static final Vocabulary rdauVocab = new Vocabulary("http://rdaregistry.info/Elements/u/");
	private static final Vocabulary dcVocab = new Vocabulary("http://purl.org/dc/elements/1.1/");
	private static final Vocabulary madsVocab = new Vocabulary("http://www.loc.gov/mads/rdf/v1#");
	private static final Vocabulary skosVocab = new Vocabulary("http://www.w3.org/2004/02/skos/core#");
	private static final Vocabulary timeVocab = new Vocabulary("http://www.w3.org/2006/time#");
	 	
	// RDA models
	private static final IRI Work = rdacVocab.term("Work");
	private static final IRI Expression = rdacVocab.term("Expression");
	private static final IRI Manifestation = rdacVocab.term("Manifestation");
	private static final IRI Item = rdacVocab.term("Item");
	private static final IRI Agent = rdacVocab.term("Agent");
	private static final IRI Person = rdacVocab.term("Person");
	private static final IRI CorporateBody = rdacVocab.term("CorporateBody");
	private static final IRI Family = rdacVocab.term("Family");
	private static final IRI Concept = skosVocab.term("Concept");
	private static final IRI Instant = timeVocab.term("Instant");
	
	// LOC models
	private static final IRI Language = madsVocab.term("Language");
	
	// Work models
	private static final IRI expressionOfWork = rdawVocab.term("expressionOfWork");
	private static final IRI manifestationOfWork = rdawVocab.term("manifestationOfWork");
	private static final IRI titleOfTheWork = rdawVocab.term("titleOfTheWork");
	private static final IRI formOfWork = rdawVocab.term("formOfWork");
	private static final IRI author = rdawVocab.term("author");
	private static final IRI director = rdawVocab.term("director");
	
	// DC models
	private static final IRI identifier = dcVocab.term("identifier");
	
	// Expression model
	private static final IRI languageOfExpression = rdaeVocab.term("languageOfExpression");
	private static final IRI workExpressed = rdaeVocab.term("workExpressed");
	private static final IRI manifestationOfExpression = rdaeVocab.term("manifestationOfExpression");
	private static final IRI contributor = rdaeVocab.term("contributor");
	private static final IRI translator = rdaeVocab.term("translator");
	private static final IRI illustrator = rdaeVocab.term("illustrator");
	private static final IRI imitationOfExpression = rdaeVocab.term("imitationOfExpression");
	private static final IRI abridgementOfExpression = rdaeVocab.term("abridgementOfExpression");
	
    // Manifestation model
	private static final IRI mediaType = rdamVocab.term("mediaType");
	private static final IRI contentType = rdamVocab.term("contentType");
	private static final IRI carrierType = rdamVocab.term("carrierType");
	private static final IRI expressionManifested = rdamVocab.term("expressionManifested");
	private static final IRI exemplarOfManifestation = rdamVocab.term("exemplarOfManifestation");
	private static final IRI digitalTransferOfManifestation = rdamVocab.term("digitalTransferOfManifestation");
	
	// RDAU properties
	private static final IRI revisionOf = rdauVocab.term("revisionOf");
	private static final IRI supplementTo = rdauVocab.term("supplementTo");
	
	// Person model
	private static final IRI authorOf = rdaaVocab.term("authorOf");
	private static final IRI nameOfThePerson = rdaaVocab.term("nameOfThePerson");
	private static final IRI identifierForThePerson = rdaaVocab.term("identifierForThePerson");
	private static final IRI dateOfDeath = rdaaVocab.term("dateOfDeath");
	private static final IRI dateOfBirth = rdaaVocab.term("dateOfBirth");
	private static final IRI variantNameForThePerson = rdaaVocab.term("variantNameForThePerson");
	private static final IRI otherPFCManifestationOf = rdaaVocab.term("otherPFCManifestationOf");
	private static final IRI publisherOf = rdaaVocab.term("publisherOf");
	private static final IRI directorOf = rdaaVocab.term("directorOf");
	
	
	@Test
	public void rdfConstraintTest() throws Exception {
		Server aServer = Stardog
			                 .buildServer()
			                 .bind(SNARLProtocolConstants.EMBEDDED_ADDRESS)
			                 .start();
		
		// create a database for the example (if there is already a database with such a name,
		// drop it first)
		try (AdminConnection dbms = AdminConnectionConfiguration.toEmbeddedServer()
		                                                        .credentials("admin", "admin")
		                                                        .connect()) {
			if (dbms.list().contains("testICVDocs")) {
				dbms.drop("testICVDocs");
			}

			dbms.createMemory("testICVDocs");
		}
		
		// obtain a connection to the database
		try (Connection aConn = ConnectionConfiguration
			.to("testICVDocs")				// the name of the db to connect to
			.reasoning(true)	            // need reasoning for ICV
			.credentials("admin", "admin")  // credentials to use while connecting
 			.connect()) {                   // now open the connection

			// now we create a validator to use
			aValidator = aConn.as(ICVConnection.class);

			// declare that agent is superclass 
			insert(aConn, Models2.newModel(statement(Person, RDFS.SUBCLASSOF, Agent)));
			insert(aConn, Models2.newModel(statement(CorporateBody, RDFS.SUBCLASSOF, Agent)));
			insert(aConn, Models2.newModel(statement(Family, RDFS.SUBCLASSOF, Agent)));
			
			// load the RDF files into the database 
			aConn.begin();
			Files.walk(Paths.get("src/test/resources/rdf")).forEach(filePath -> {
			    if (Files.isRegularFile(filePath)) {
			    	aConn.add().io().format(RDFFormat.RDFXML).file(filePath);
			    }
			});
			aConn.commit();

			addWorkConstraints();
			//addExpressionConstraints();
			//addManifestationConstraints();
			//addAgentConstraints();
			
			printValidity(aValidator);
			
			clear(aValidator);
		}

		// you MUST stop the server if you've started it!
		aServer.stop();
    }
	
	
	/**
	 * Declaration of Agent constraints
	 */
	private void addAgentConstraints(){
		System.out.println("(1) Domain-Range constraints: author of");
		System.out.println(Strings.repeat("-", 25) + "\n");

		Constraint aDomainConstraint = ConstraintFactory.constraint(domain(authorOf, Agent));
		Constraint aRangeConstraint = ConstraintFactory.constraint(range(authorOf, Work));
		
        addConstraint(aValidator, aDomainConstraint, aRangeConstraint);
		
		System.out.println("(2) Cardinality constraints: Exactly one identifierForThePerson property per Person");
		System.out.println(Strings.repeat("-", 25) + "\n");
		
		Constraint aIdentifierPersonExactlyOneConstraint = ConstraintFactory.constraint(subClassOf(Person, cardinality(identifierForThePerson, 1)));
		
		addConstraint(aValidator, aIdentifierPersonExactlyOneConstraint);
		
		System.out.println("(3) Domain-Range constraints: director of");
		System.out.println(Strings.repeat("-", 25) + "\n");

		Constraint aDomainConstraint3 = ConstraintFactory.constraint(domain(directorOf, Agent));
		Constraint aRangeConstraint3 = ConstraintFactory.constraint(range(directorOf, Work));
		
        addConstraint(aValidator, aDomainConstraint3, aRangeConstraint3);
        
        System.out.println("(4) Domain-Range constraints: publisher of");
		System.out.println(Strings.repeat("-", 25) + "\n");

		Constraint aDomainConstraint4 = ConstraintFactory.constraint(domain(publisherOf, Agent));
		Constraint aRangeConstraint4 = ConstraintFactory.constraint(range(publisherOf, Expression));
		
        addConstraint(aValidator, aDomainConstraint4, aRangeConstraint4);
				
		System.out.println("(5) Domain-Range constraints: Only works can be manifested by manifestations");
		System.out.println(Strings.repeat("-", 25) + "\n");

		Constraint aDomainConstraint5 = ConstraintFactory.constraint(domain(manifestationOfWork, Work));
		Constraint aRangeConstraint5 = ConstraintFactory.constraint(range(manifestationOfWork, Manifestation));
		
        addConstraint(aValidator, aDomainConstraint5, aRangeConstraint5);
        
        System.out.println("(6) Domain-Range constraints: name of the person");
		System.out.println(Strings.repeat("-", 25) + "\n");

		Constraint aDomainConstraint6 = ConstraintFactory.constraint(domain(nameOfThePerson, Person));
				
        addConstraint(aValidator, aDomainConstraint6);
        
        System.out.println("(7) Domain-Range constraints: variant name of the person");
		System.out.println(Strings.repeat("-", 25) + "\n");

		Constraint aDomainConstraint7 = ConstraintFactory.constraint(domain(variantNameForThePerson, Person));
		
        addConstraint(aValidator, aDomainConstraint7);

        System.out.println("(8) Domain-Range constraints: Date of birth");
		System.out.println(Strings.repeat("-", 25) + "\n");

        Constraint aDomainConstraint8 = ConstraintFactory.constraint(domain(dateOfBirth, Agent));
        Constraint aRangeConstraint8 = ConstraintFactory.constraint(range(dateOfBirth, Instant));
        
        addConstraint(aValidator, aDomainConstraint8, aRangeConstraint8);
        
        System.out.println("(9) Domain-Range constraints: Date of death");
		System.out.println(Strings.repeat("-", 25) + "\n");

        Constraint aDomainConstraint9 = ConstraintFactory.constraint(domain(dateOfDeath, Agent));
        Constraint aRangeConstraint9 = ConstraintFactory.constraint(range(dateOfDeath, Instant));
        
        addConstraint(aValidator, aDomainConstraint9, aRangeConstraint9);
        
        System.out.println("(10) Domain-Range constraints: otherPFCManifestationOf");
		System.out.println(Strings.repeat("-", 25) + "\n");

        Constraint aDomainConstraint10 = ConstraintFactory.constraint(domain(otherPFCManifestationOf, Agent));
        Constraint aRangeConstraint10 = ConstraintFactory.constraint(range(otherPFCManifestationOf, Manifestation));
        
        addConstraint(aValidator, aDomainConstraint10, aRangeConstraint10);
	}
	
	/**
	 * Declaration of Work constraints
	 */
	private void addWorkConstraints(){
		System.out.println("(1) Domain-Range constraints: Only works can be expressed by expressions");
		System.out.println(Strings.repeat("-", 25) + "\n");

		Constraint aDomainConstraint = ConstraintFactory.constraint(domain(expressionOfWork, Work));
		Constraint aRangeConstraint = ConstraintFactory.constraint(range(expressionOfWork, Expression));
		
        addConstraint(aValidator, aDomainConstraint, aRangeConstraint);
		
		System.out.println("(2) Cardinality constraints: Exactly one titleOfTheWork property per work");
		System.out.println(Strings.repeat("-", 25) + "\n");
		
		Constraint aTitleofTheWorkExactlyOneConstraint = ConstraintFactory.constraint(subClassOf(Work, cardinality(titleOfTheWork, 1)));
		
		addConstraint(aValidator, aTitleofTheWorkExactlyOneConstraint);
		
		System.out.println("(3) Cardinality constraints: Exactly one identifier property per work");
		System.out.println(Strings.repeat("-", 25) + "\n");
		
		Constraint aIdentifierWorkExactlyOneConstraint = ConstraintFactory.constraint(subClassOf(Work, cardinality(identifier, 1)));
		
		addConstraint(aValidator, aIdentifierWorkExactlyOneConstraint);
		
		System.out.println("(4) Cardinality constraints: At least one manifestation per work");
		System.out.println(Strings.repeat("-", 25) + "\n");
		
		Constraint aManifestationOfWorkAtLeastOneConstraint = ConstraintFactory.constraint(subClassOf(Work, min(manifestationOfWork, 1)));
		
		addConstraint(aValidator, aManifestationOfWorkAtLeastOneConstraint);
		
		System.out.println("(5) Domain-Range constraints: Only works can be manifested by manifestations");
		System.out.println(Strings.repeat("-", 25) + "\n");

		Constraint aDomainConstraint5 = ConstraintFactory.constraint(domain(manifestationOfWork, Work));
		Constraint aRangeConstraint5 = ConstraintFactory.constraint(range(manifestationOfWork, Manifestation));
		
        addConstraint(aValidator, aDomainConstraint5, aRangeConstraint5);
        
        System.out.println("(6) Domain-Range constraints: Only works can have the role author");
		System.out.println(Strings.repeat("-", 25) + "\n");

		Constraint aDomainConstraint6 = ConstraintFactory.constraint(domain(author, Work));
		Constraint aRangeConstraint6 = ConstraintFactory.constraint(range(author, Agent));
		
        addConstraint(aValidator, aDomainConstraint6, aRangeConstraint6);
        
        System.out.println("(7) Domain-Range constraints: Only works can have the role director");
		System.out.println(Strings.repeat("-", 25) + "\n");

		Constraint aDomainConstraint7 = ConstraintFactory.constraint(domain(director, Work));
		Constraint aRangeConstraint7 = ConstraintFactory.constraint(range(director, Agent));
		
        addConstraint(aValidator, aDomainConstraint7, aRangeConstraint7);

        System.out.println("(8) Domain-Range constraints: Form of Work must be a Resource from LOC genre forms");
		System.out.println(Strings.repeat("-", 25) + "\n");

        Constraint aDomainConstraint8 = ConstraintFactory.constraint(domain(formOfWork, Work));
        Constraint aRangeConstraint8 = ConstraintFactory.constraint(range(dataProperty(formOfWork), XMLSchema.ANYURI));
        
        addConstraint(aValidator, aDomainConstraint8, aRangeConstraint8);
	}
	
	/**
	 * Declaration of Expression constraints
	 */
	private void addExpressionConstraints(){
		System.out.println("(1) Domain-Range constraints: Language of Expression must be a Language");
		System.out.println(Strings.repeat("-", 25) + "\n");

        Constraint aDomainConstraint = ConstraintFactory.constraint(domain(languageOfExpression, Expression));
        Constraint aRangeConstraint = ConstraintFactory.constraint(range(languageOfExpression, Language));
        
        addConstraint(aValidator, aDomainConstraint, aRangeConstraint);
        
        System.out.println("(2) Domain-Range constraints: Work of Expression must be a Work entity");
		System.out.println(Strings.repeat("-", 25) + "\n");

        Constraint aDomainConstraint2 = ConstraintFactory.constraint(domain(workExpressed, Expression));
        Constraint aRangeConstraint2 = ConstraintFactory.constraint(range(dataProperty(workExpressed), Work));
        
        addConstraint(aValidator, aDomainConstraint2, aRangeConstraint2);
        
        System.out.println("(3) Domain-Range constraints: contributor ");
		System.out.println(Strings.repeat("-", 25) + "\n");

        Constraint aDomainConstraint3 = ConstraintFactory.constraint(domain(contributor, Expression));
        Constraint aRangeConstraint3 = ConstraintFactory.constraint(range(contributor, Agent));
        
        addConstraint(aValidator, aDomainConstraint3, aRangeConstraint3);
        
        System.out.println("(4) Domain-Range constraints: Manifestation of Expression ");
		System.out.println(Strings.repeat("-", 25) + "\n");

        Constraint aDomainConstraint4 = ConstraintFactory.constraint(domain(manifestationOfExpression, Expression));
        Constraint aRangeConstraint4 = ConstraintFactory.constraint(range(dataProperty(manifestationOfExpression), Manifestation));
        
        addConstraint(aValidator, aDomainConstraint4, aRangeConstraint4);    
        
        System.out.println("(5) Cardinality constraints: Exactly one identifier property per expression");
		System.out.println(Strings.repeat("-", 25) + "\n");
		
		Constraint aIdentifierExpressionExactlyOneConstraint = ConstraintFactory.constraint(subClassOf(Expression, cardinality(identifier, 1)));
		
		addConstraint(aValidator, aIdentifierExpressionExactlyOneConstraint);
		
        System.out.println("(6) Domain-Range constraints: illustrator ");
		System.out.println(Strings.repeat("-", 25) + "\n");

        Constraint aDomainConstraint6 = ConstraintFactory.constraint(domain(illustrator, Expression));
        Constraint aRangeConstraint6 = ConstraintFactory.constraint(range(illustrator, Agent));
        
        addConstraint(aValidator, aDomainConstraint6, aRangeConstraint6);
		
        System.out.println("(7) Domain-Range constraints: translator ");
		System.out.println(Strings.repeat("-", 25) + "\n");

        Constraint aDomainConstraint7 = ConstraintFactory.constraint(domain(translator, Expression));
        Constraint aRangeConstraint7 = ConstraintFactory.constraint(range(translator, Agent));
        
        addConstraint(aValidator, aDomainConstraint7, aRangeConstraint7);
        
        System.out.println("(8) Domain-Range constraints: imitationOfExpression ");
		System.out.println(Strings.repeat("-", 25) + "\n");

        Constraint aDomainConstraint8 = ConstraintFactory.constraint(domain(imitationOfExpression, Expression));
        Constraint aRangeConstraint8 = ConstraintFactory.constraint(range(imitationOfExpression, Expression));
        
        addConstraint(aValidator, aDomainConstraint8, aRangeConstraint8);
        
        System.out.println("(9) Domain-Range constraints: abridgementOfExpression ");
		System.out.println(Strings.repeat("-", 25) + "\n");

        Constraint aDomainConstraint9 = ConstraintFactory.constraint(domain(abridgementOfExpression, Expression));
        Constraint aRangeConstraint9 = ConstraintFactory.constraint(range(abridgementOfExpression, Expression));
        
        addConstraint(aValidator, aDomainConstraint9, aRangeConstraint9);

	}

	/**
	 * Declaration of Expression constraints
	 */
	private void addManifestationConstraints(){
		System.out.println("(1) Domain-Range constraints: relationship digitalTransferOfManifestation");
		System.out.println(Strings.repeat("-", 25) + "\n");

        Constraint aDomainConstraint = ConstraintFactory.constraint(domain(digitalTransferOfManifestation, Manifestation));
        Constraint aRangeConstraint = ConstraintFactory.constraint(range(digitalTransferOfManifestation, Manifestation));
        
        addConstraint(aValidator, aDomainConstraint, aRangeConstraint);
        
        System.out.println("(2) Domain-Range constraints: Expression manifested");
		System.out.println(Strings.repeat("-", 25) + "\n");

        Constraint aDomainConstraint2 = ConstraintFactory.constraint(domain(expressionManifested, Manifestation));
        Constraint aRangeConstraint2 = ConstraintFactory.constraint(range(dataProperty(expressionManifested), Expression));
        
        addConstraint(aValidator, aDomainConstraint2, aRangeConstraint2);
        
        System.out.println("(3) Domain-Range constraints: Media type ");
		System.out.println(Strings.repeat("-", 25) + "\n");

        Constraint aDomainConstraint3 = ConstraintFactory.constraint(domain(mediaType, Manifestation));
        Constraint aRangeConstraint3 = ConstraintFactory.constraint(range(mediaType, Concept));
        
        addConstraint(aValidator, aDomainConstraint3, aRangeConstraint3);
        
        System.out.println("(4) Domain-Range constraints: exemplar Of Manifestation");
		System.out.println(Strings.repeat("-", 25) + "\n");

        Constraint aDomainConstraint4 = ConstraintFactory.constraint(domain(exemplarOfManifestation, Manifestation));
        Constraint aRangeConstraint4 = ConstraintFactory.constraint(range(exemplarOfManifestation, Item));
        
        addConstraint(aValidator, aDomainConstraint4, aRangeConstraint4);    
        
        System.out.println("(5) Cardinality constraints: Exactly one identifier property per manifestation");
		System.out.println(Strings.repeat("-", 25) + "\n");
		
		Constraint aIdentifierExpressionExactlyOneConstraint = ConstraintFactory.constraint(subClassOf(Manifestation, cardinality(identifier, 1)));
		
		addConstraint(aValidator, aIdentifierExpressionExactlyOneConstraint);
		
        System.out.println("(6) Domain-Range constraints: Carrier type ");
		System.out.println(Strings.repeat("-", 25) + "\n");

        Constraint aDomainConstraint6 = ConstraintFactory.constraint(domain(carrierType, Manifestation));
        Constraint aRangeConstraint6 = ConstraintFactory.constraint(range(carrierType, Concept));
        
        addConstraint(aValidator, aDomainConstraint6, aRangeConstraint6);
		
        System.out.println("(7) Domain-Range constraints: Content type ");
		System.out.println(Strings.repeat("-", 25) + "\n");

        Constraint aDomainConstraint7 = ConstraintFactory.constraint(domain(contentType, Manifestation));
        Constraint aRangeConstraint7 = ConstraintFactory.constraint(range(contentType, Concept));
        
        addConstraint(aValidator, aDomainConstraint7, aRangeConstraint7);
        
        System.out.println("(8) Domain-Range constraints: Revision ");
		System.out.println(Strings.repeat("-", 25) + "\n");

        Constraint aDomainConstraint8 = ConstraintFactory.constraint(domain(revisionOf, Manifestation));
        Constraint aRangeConstraint8 = ConstraintFactory.constraint(range(revisionOf, Manifestation));
        
        addConstraint(aValidator, aDomainConstraint8, aRangeConstraint8);
        
        System.out.println("(9) Domain-Range constraints: Supplement ");
		System.out.println(Strings.repeat("-", 25) + "\n");

        Constraint aDomainConstraint9 = ConstraintFactory.constraint(domain(supplementTo, Manifestation));
        Constraint aRangeConstraint9 = ConstraintFactory.constraint(range(supplementTo, Manifestation));
        
        addConstraint(aValidator, aDomainConstraint9, aRangeConstraint9);
		        
	}
	
	private static void addConstraint(final ICVConnection theValidator, final Constraint... theConstraint) throws StardogException {
		theValidator.addConstraint(theConstraint);
	}

	private static Value literal(final String theValue) {
		return Values.literal(theValue);
	}

	private static Statement statement(final IRI theSubj, final IRI thePred, final Value theObject) {
		return Values.statement(theSubj, thePred, theObject);
	}

	private static void printValidity(final ICVConnection theValidator) throws StardogException {
		final boolean isValid = theValidator.isValid(ContextSets.DEFAULT_ONLY);
		System.out.println("The data " + (isValid ? "is" : "is NOT") + " valid!");

		if (!isValid) {
			try (CloseableIterator<ConstraintViolation<BindingSet>> aViolationIter = theValidator.getViolationBindings(ContextSets.DEFAULT_ONLY)) {

				while (aViolationIter.hasNext()) {
					ConstraintViolation<BindingSet> aViolation = aViolationIter.next();

					// ICV.asIndividuals will close the `aViolation.getViolations()` for us
					Iterator<Resource> aViolatingIndividuals = ICV.asIndividuals(aViolation.getViolations());

					System.out.println("Each of these individuals violated the constraint: " + aViolation.getConstraint());

					while (aViolatingIndividuals.hasNext()) {
						System.out.println(aViolatingIndividuals.next());
					}
				}
			}
		}
		System.out.println();
	}

	private static void insert(final Connection theConn, final Model theGraph) throws StardogException {
		theConn.begin();
		theConn.add().graph(theGraph);
		theConn.commit();
	}

	private static void remove(final Connection theConn, final Model theGraph) throws StardogException {
		theConn.begin();
		theConn.remove().graph(theGraph);
		theConn.commit();
	}

	private static void clear(final ICVConnection theConn) throws StardogException {
		theConn.begin();
		theConn.remove().all();
		theConn.clearConstraints();
		theConn.commit();
	}
}
