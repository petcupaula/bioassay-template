BioAssay Template 
=================

An editor for specifying how the BioAssay Ontology, and other related vocabularies, are to be used to describe bioassays.

Primary author: Dr. Alex M. Clark (alex@collaborativedrug.com)

Released under the Gnu Public License 2.0

Based on Java 8 using Eclipse, JavaFX and Apache Jena.

The [BioAssay Ontology](http://bioassayontology.org) is included as part of the distribution.

The source code contains functionality for manipulating the data model used for templates, which guide the use of the
BioAssay Ontology (BAO) for annotating bioassay protocols. It also includes the BioAssay Schema Editor, which is an
interactive desktop application that allows editing of template schemata, and a preliminary interface for using the
current template to annotate assays.

The project is a continuation of the work begun in:
"Fast and accurate semantic annotation of bioassays exploiting a hybrid of machine learning and user confirmation":
Alex M. Clark; Barry A. Bunin; Nadia K. Litterman; Stephan C. Schürer; Ubbo Visser, _PeerJ_ 524 (2014) [link](https://peerj.com/articles/524)

A second manuscript, which describes this application, has been submitted to _PeerJ_, and is currently under review.

Installation
============

To compile and run the package, download the files or synchronize with **git**. The deliverable can be compiled using **ant** (the
`build.xml` file is provided), or it can be opened as an **Eclipse** project. Java 8 is required, but all other dependencies are
included in the project.

To get started quickly without compiling, download two files:

  * the pre-built package: [pkg/BioAssayTemplate.jar](https://github.com/cdd/bioassay-template/blob/master/pkg/BioAssayTemplate.jar)
  * the Common Assay Template: [data/template/schema.ttl](https://github.com/cdd/bioassay-template/blob/master/data/template/schema.ttl)
  
Run the application by double clicking on the `BioAssayTemplate.jar` file within the appropriate file manager tool, or run it with the
command line syntax: `java -jar BioAssayTemplate.jar`. As long as you have Java 8 installed, the interface should appear, with a blank 
schema window as the default. Use File|Open to locate and load the `schema.ttl` file.