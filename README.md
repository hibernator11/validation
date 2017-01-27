# RDF Constraint validation for RDA elements

This project is an example of how to validate RDF files based on the <a href="http://www.rdaregistry.info/">RDA vocabulary</a>.

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.

### Prerequisities

It is necessary to add the license stardog-license-key.bin file at the root folder of the project. The stardog community version has been used in order to perform the validation of the RDF files. The community version only allows for the definition of 20 constraints. This is the reason why the constraints are defined and launched by classes. 

```
stardog-license-key.bin
```

## Running the tests

```
mvn test
```

## And coding style tests

First the type of entities are defined such as Work, Expression, Manifestatation and Agent:

```
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
```

Then, all the relationships that want to be testes must be defined:
```
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
```

Finally, the domain-range and cardinality constraints are defined: 
```
System.out.println("(1) Domain-Range constraints: Carrier type ");

Constraint aDomainConstraint = ConstraintFactory.constraint(domain(carrierType, Manifestation));
Constraint aRangeConstraint = ConstraintFactory.constraint(range(carrierType, Concept));

addConstraint(aValidator, aDomainConstraint, aRangeConstraint);
```

## running issues

Due to a Stardog bug, it is necessary to remove the system folder if you have a problem with the admin user. 

## Built With

* Java - 1.8 version
* Maven 
* Stardog Java library 

## License

GNU GENERAL PUBLIC LICENSE

