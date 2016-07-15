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

### And coding style tests

Explain what these tests test and why

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

This project is licensed under the MIT License.

