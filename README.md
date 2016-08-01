# jimdb 

jimdb is a Java Library that provides access to databases created by 
jim (https://github.com/sjbutler/jim). jimdb provides an infrastructure to 
support the (relatively) rapid development and prototyping of tools to analyse 
identifier names.

## Copyright & Licence
jimdb is Copyright (C) 2010-2015 The Open University and
is released under the terms of the Apache Licence v2.

## Requirements
### Java
jimdb requires Java v8.
 
### Dependencies
* Apache Derby - v10.11.1.1 or greater
* intt - an identifier name tokeniser. intt can be found at 
  https://github.com/sjbutler/intt 
* SLF4J - jimdb uses SLF4J for logging and requires the slf4j-api-1.7.x jar 
  file to be on the classpath. You will also need the relevant slf4j jar file 
  for your chosen logging system.
* Apache Commons Collections - v4.0 or greater

## Documentation
The API is documented in the javadocs, which are in a zip archive in the docs 
folder. 

## Citation

If you use jimdb to support academic research please cite: 
Butler, Simon (2016) Analysing Java Identifier Names, PhD thesis, The Open University.


## Caveat

jimdb is research software. The API may not be stable, and there is no 
guarantee that the code will be maintained.


