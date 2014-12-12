![SaltNPepper project](./md/img/SaltNPepper_logo2010.png)
# pepperModules-GenericXMLModules
This project provides a an importer to import data coming from a wide range of xml formats for the linguistic converter framework Pepper (see https://github.com/korpling/pepper). A detailed description of that mapping can be found in section [GenericXMLImporter](#details).

Pepper is a pluggable framework to convert a variety of linguistic formats (like [TigerXML](http://www.ims.uni-stuttgart.de/forschung/ressourcen/werkzeuge/TIGERSearch/doc/html/TigerXML.html), the [EXMARaLDA format](http://www.exmaralda.org/), [PAULA](http://www.sfb632.uni-potsdam.de/paula.html) etc.) into each other. Furthermore Pepper uses Salt (see https://github.com/korpling/salt), the graph-based meta model for linguistic data, which acts as an intermediate model to reduce the number of mappings to be implemented. That means converting data from a format _A_ to format _B_ consists of two steps. First the data is mapped from format _A_ to Salt and second from Salt to format _B_. This detour reduces the number of Pepper modules from _n<sup>2</sup>-n_ (in the case of a direct mapping) to _2n_ to handle a number of n formats.

![n:n mappings via SaltNPepper](./md/img/puzzle.png)

In Pepper there are three different types of modules:
* importers (to map a format _A_ to a Salt model)
* manipulators (to map a Salt model to a Salt model, e.g. to add additional annotations, to rename things to merge data etc.)
* exporters (to map a Salt model to a format _B_).

For a simple Pepper workflow you need at least one importer and one exporter.

## Requirements
Since the here provided module is a plugin for Pepper, you need an instance of the Pepper framework. If you do not already have a running Pepper instance, click on the link below and download the latest stable version (not a SNAPSHOT):

> Note:
> Pepper is a Java based program, therefore you need to have at least Java 7 (JRE or JDK) on your system. You can download Java from https://www.oracle.com/java/index.html or http://openjdk.java.net/ .


## Install module
If this Pepper module is not yet contained in your Pepper distribution, you can easily install it. Just open a command line and enter one of the following program calls:

**Windows**
```
pepperStart.bat 
```

**Linux/Unix**
```
bash pepperStart.sh 
```

Then type in command *is* and the path from where to install the module:
```
pepper> update de.hu_berlin.german.korpling.saltnpepper::pepperModules-GenericXMLModules::https://korpling.german.hu-berlin.de/maven2/
```

## Usage
To use this module in your Pepper workflow, put the following lines into the workflow description file. Note the fixed order of xml elements in the workflow description file: &lt;importer/>, &lt;manipulator/>, &lt;exporter/>. The GenericXMLImporter is an importer module, which can be addressed by one of the following alternatives.
A detailed description of the Pepper workflow can be found on the [Pepper project site](https://github.com/korpling/pepper). 

### a) Identify the module by name

```xml
<importer name="GenericXMLImporter" path="PATH_TO_CORPUS"/>
```

### b) Identify the module by formats
```xml
<importer formatName="xml" formatVersion="1.0" path="PATH_TO_CORPUS"/>
```

### c) Use properties
```xml
<importer name="GenericXMLImporter" path="PATH_TO_CORPUS">
  <property key="PROPERTY_NAME">PROPERTY_VALUE</key>
</importer>
```

## Contribute
Since this Pepper module is under a free license, please feel free to fork it from github and improve the module. If you even think that others can benefit from your improvements, don't hesitate to make a pull request, so that your changes can be merged.
If you have found any bugs, or have some feature request, please open an issue on github. If you need any help, please write an e-mail to saltnpepper@lists.hu-berlin.de .

## Funders
This project has been funded by the [department of corpus linguistics and morphology](https://www.linguistik.hu-berlin.de/institut/professuren/korpuslinguistik/) of the Humboldt-Universität zu Berlin, the Institut national de recherche en informatique et en automatique ([INRIA](www.inria.fr/en/)) and the [Sonderforschungsbereich 632](https://www.sfb632.uni-potsdam.de/en/). 

## License
  Copyright 2009 Humboldt-Universität zu Berlin, INRIA.

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
 
  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.


# <a name="details">GenericXMLImporter</a>

The GenericXMLImporter imports data coming from any xml file to a Salt model in a customizable but generic way. This importer provides a wide range of customization possibilities via the here described set of properties. Before we talk about the possibility of customizing the mapping, we describe the general and default mapping from an xml file to a Salt model.

### Mapping to Salt

The GenericXMLImporter maps element-nodes, text-nodes and attribute-nodes to Salt model objects. Comment-nodes (&lt;-- ... -->), processing-instruction-nodes (&lt;?...?>) etc. will be ignored and not be mapped.

The textual value of all text-nodes of the xml document will be concatenated to one single primary text. This primary text is stored as a STextualDS object. Each element-node containing a primary text and no further element-nodes (called terminal-node) is mapped to a SToken object overlapping the part of the primary data given by the contained text-node. To realize the offset to the start and end position of the overlapping text, a STextualRelation is created connecting the STextualDS and the SToken object.

The following xml fragment

    <a>This is</a><b> a sample text</b>

is mapped to a STexualDS object having the sText "This is a sample text" and two SToken objects, one overlapping the text "This is" and one overlapping the text " a sample text".

> **Note**
>
> The importer does not take care of the given tokenization and does not retokenize it.

An element-node containing further element-nodes (called complex-node) is mapped to a hierarchical structure in the Salt model. Therefore Salt provides the node type SStructure and the relation type SDominanceRelation, which connects a SStructure object with another SStructure, SToken or SSpan object.

The following xml fragment

    <a><b>a sample</b></a>

is mapped to a SToken (corresponding to the element-node &lt;a\>) object overlapping the primary text "a sample" and a SStructure (corresponding to the element-node &lt;b\>) object dominating the SToken object. Both nodes are connected with a SDominanceRelation having the SStructure object as source and the SToken object as target. Since SStructure objects are used for hierarchies, the same goes for an element-node containing another element node.

The following xml fragment

    <a><b>...</b></a>

is mapped to a SStructure object representing the element-node \<a\> and dominating a further SStructure object representing the element-node \<b\>.

In many xml formats element-nodes can have further element-nodes and text-nodes on the first level of their subtree as well. These kind of nodes are often called mixed nodes (for nodes having a mixed content). These kind of nodes are mapped to a SStructure object in Salt. The element-nodes contained in their subtree are mapped to either SToken objects or SStructure objects, depending on their content. For a text-node, an artificial SToken object is created and added to the subtree of the SStructure object in Salt.

The following xml fragment

    <a>This is <!-- t1 --><b>a sample<!-- t2 --></b> text<!-- t3 --></a>

is mapped to three SToken objects _t1_ overlapping the text "This is ", _t2_ overlapping the text "a sample" and _t3_ overlapping the text " text". Because the SToken object _t2_ is the only one having an existing correspondance to a terminal-node, two artificial SToken objects _t1_ and _t3_ are created. The complex-node \<a\> is mapped to a SStructure object dominating the three SToken objects in the order _t1_, _t2_ and _t3_. This mechanism is recursive and will also work for the following xml fragment,

    <a>This is <b><c>a sample</c></b> text</a>

for which a further SStructure object corresponding with the complex-node &lt;b\> is created and is dominating the SToken object corresponding with the terminal-node &lt;c\>.

Attribute-nodes are mapped to SAnnotation objects, having the attribute-name as `SName` and the attribute-value as `SValue`. Such a SAnnotation object then is added to the containing SNode object.

The following xml fragment

    <a att="value">a sample</a>

is mapped to a SToken overlapping the text "a sample" and containing a SAnnotation object having the `SName` "att" and the `SValue` "value". The same goes for complex-nodes and mixed-nodes.

### Properties

Properties allow the customization of a mapping in Pepper. In this case to customize the mapping of an xml file to a Salt model. The here described properties impact the mapping to the Salt model and are dependent of the format you want to use to export the data. For instance the mapping of xml data to hierarchies in Salt does not really make sense, if the export format does not support hierarchies. In that case for instance the property genericXml.importer.asSSpan might help.

The following section contains a brief description to each single property and describes the resulting differences in the mapping to the salt model.

Some of the here described properties use a small subset of the XPath language for addressing nodes in the xml document to import. This subset contains possibilities to address element-nodes, text-nodes and attribute-nodes in just a simple way via following the descendant axis. The descendant axis can only be used by the shortcut syntax represented by a '/' for a direct descendant and '//' for any descendants. The other axes and predicates as well are not yet supported. The following tables show the use of the supported XPath subset.

| XPath           | description                                                                                                                                   |
|-----------------|-----------------------------------------------------------------------------------------------------------------------------------------------|
| /element        | addresses the xml element-node having the name element and which is the root node                                                             |
| //element       | addresses the xml element-node having the name element anywhere in the document                                                               |
| //element//     | addresses all xml element-nodes in the subtree of each element-node having that name                                                    |
| /father/element | addresses the xml element-node having the name element and its subtree, which is a direct descendant of an element-node having the name father |

| XPath                | description                                                                                                                                                                        |
|----------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| //@attribute         | addresses the xml attribute-node having the name attribute anywhere in the document                                                                                                |
| //element/@attribute | addresses the xml attribute-node having the name attribute and belongs to the xml element-node having the name element which is a direct descendant of an element-nodenamed father |

| XPath            | description                                                                                                                                          |
|------------------|------------------------------------------------------------------------------------------------------------------------------------------------------|
| //text()         | addresses every xml text-node anywhere in the document                                                                                               |
| //element/text() | addresses every xml text-node belonging to the xml element-node having the name element which is a direct descendant of an element-node named father |

For some properties it is possible, to not only address  element-node, text-node or attribute-node, but to address a set of nodes. For such cases, you can separate XPath expressions by using the ',' character. For instance:

    //element1/text(), //element2/text()

and so on. The size of such a set is unbound.

The following table contains an overview of all available properties that customize the behaviour of this Pepper module.

| Name of property                              | Type of property | optional/mandatory | default value      |
|-----------------------------------------------|------------------|--------------------|--------------------|
| genericXml.importer.ignoreList                | XPath            | optional           | --                 |
| genericXml.importer.asSSpan                   | XPath            | optional           | --                 |
| genericXml.importer.prefixSAnnotationName     | XPath            | optional           | --                 |
| genericXml.importer.artificialSStruct         | Boolean          | optional           | false              |
| genericXml.importer.sMetaAnnotation           | XPath            | optional           | --                 |
| genericXml.importer.sMetaAnnotation.sDocument | XPath            | optional           | --                 |
| genericXml.importer.textOnly                  | Boolean          | optional           | false              |
| genericXml.importer.sLayer                    | XPath            | optional           | --                 |
| genericXml.importer.ignoreWhitepsaces         | String           | optional           | '\n','\r','\t',' ' |
| genericXml.importer.ignoreNamespaces          | Boolean          | optional           | true               |
| genericXml.importer.elementNameAsSAnno        | XPath            | optional           | --                 |
| genericXml.importer.file.endings              | String           | optional           | xml                |
| genericXml.importer.separateTokens            | String           | optional           | ' '(blank)         |

### genericXml.importer.ignoreList

The ignore list is a list of nodes (element-nodes, attribute-nodes and text-nodes) which are ignored for the mapping to a Salt model. Imagine for instance the follwing xml fragment

    <a><b></b></a>

and the property value of ignore-list //b. This means that the element-node \<b\> will completely be ignored.

Here we give a sample of the usage of the ignore list:

    genericXml.importer.ignoreList= /a/b, //c

To ignore an entire subtree, use the subtree address mechanism e.g.

    genericXml.importer.ignoreList= /a//

To ignore all nodes in the subtree of 'a'.

> **Note**
>
> In this example, only the subtree of element-node 'a' is ignored, not 'a' itself.

### genericXml.importer.asSSpan

In case you do not want to map an element node to a SStructure object, you can also map defined element-nodes to an SSpan object. SSpan in contrast to SStructure objects are not hierarchical and are used to create span-like structures, similar to cells in a table.

Here we give a sample of the usage of this property:

    genericXml.importer.asSSpans= //element1, //father/element2

### genericXml.importer.prefixSAnnotationName

In general the `SName` of a SAnnotation is given by the name of the attribute-node given in the xml document. Sometimes it might be necessary, to remember the name of the element-node containing the attribute-node. In such cases, you can set this property to prefix the SName of the SAnnotation with the name of the surrounding element-node. The following xml fragment

    <a att="value"/>

is mapped to a SAnnotation object having the `SName` 'att'. When setting the property as shown in the following sample,

    genericXml.importer.prefixSAnnotation=//a/@att

the `SName` would be 'a_att'.

### genericXml.importer.artificialSStruct

In general a terminal-node is mapped to a SToken object. This is necessary, because in Salt only SToken objects can overlap parts of the primary data (given by STextualDS objects). In some cases, you may want to map the terminal-node also to a SSpan or SStructure node. Using this property will result in an artificial node dominating or spanning the SToken object.

The xml fragment

    <a>a sample</a>

when using the property

    genericXml.importer.artificialSStruct= true

is mapped to an SToken object overlapping the primary data "a sample" and a SStructure object dominating the SToken object.

> **Note**
>
> When using this flag, an artificial SSpan can be created instead of an SStructure object, when adding an XPath expression addressing the terminal-node to the set of expressions of the property genericXml.importer.asSSpan.

### genericXml.importer.sMetaAnnotation

Usually, an attribute-node is mapped to an SAnnotation object. But sometimes you may want to map it to a SMetaAnnotation object instead. A SMetaAnnotation in Salt marks an attribute-value pair to be not directly a linguistic annotation, which will therefore not be processed or exported as one. Often such annotations are used to mark the annotator of an annotation, or a probability of an annotation (in case of it was done by an automatic tagger etc.) and so on.

The xml fragment

    <a att="value"/>

using the property

    genericXml.importer.sMetaAnnotation=//a/@att

will be mapped to an SNode object representing the element-node \<a\> having a SMetaAnnotation object with the `SName` 'att' and the `SValue` "value".

### genericXml.importer.sMetaAnnotation.sDocument

Xml documents often also contain sections for metadata of the entire document, for instance the name of the author of the document, the year of creation, or the mother tongue of the author etc.. For dealing with such a case, you can use this flag to mark an element or an entire subtree as a metadata section. Each attribute-node of such an element or subtree is mapped to a SMetaAnnotation object having its name as `SName` and its value as `SValue` and is added to the list of metadata of the SDocument.

The following xml fragment

    <a att_2="value"><b att_1="value"/></a>

when using the property

    genericXml.importer.sMetaAnnotation.sDocument=//a

results in a SDocument object containing two SMetaAnnotation objects having the SName 'att\_1' and the `SValue` 'value' or 'att\_2' and 'value'.

> **Note**
>
> If two or more attribute-nodes have the same name, the mapper will add an '\_' and a number to their name, because in Salt an SDocument cannot have two SMetaAnnotation objects having the same `SName`. To avoid this behaviour, you can use the property genericXml.importer.prefixSAnnotationName to concatenate the name of the element-node with the name of the attribute-node.

To interprete an entire subtree of an xml element as containing meta data, use the wildcard notation at the end of your XPath expression.

The following xml fragment

    <a>
        <b att_1="value">
            <c att2="value"/>
        </b>
        <d>a sample text</d>
    </a>

when using the property

    genericXml.importer.sMetaAnnotation.sDocument=//b, //b//

results in one SToken object overlapping the primary text 'a sample text' being dominated by a SStructure object corresponing to element \<a\>. The xml-element \<b\> and its entire subtree is interpreted as metadata for the SDocument object.

You can also just address attribute-nodes. In that case only the attribute nodes are mapped to a SMetaAnnotation object. If an attribute-node is mapped to a SMetaAnnotation object, it is not mapped to a usual SAnnotation object. The following xml fragment

    <a>
        <b att_1="value">
            <c>a sample text</c>
        </b>
    </a>

when using the property

    genericXml.importer.sMetaAnnotation.sDocument=//b/@att_1

results in a SMetaAnnotation object added to the SDocument object.

#### genericXml.importer.textOnly

When using this flag, only the text is imported as primary data. That means all text-nodes will be concatenated to a single text. A STextualDS object is created and its `SText` will be set to the concatenated text.

#### genericXml.importer.sLayer

In Salt, SLayers are important to separate several kinds of linguistic annotations. For instance to separate a syntax analysis from a dialogue analysis etc.. To identify such a layer in a xml file you can use this property. An addressed element-node is mapped to a SLayer object, its attribute-nodes are mapped to SAnnotation objects. In a lot of cases an XPath expression addresses a set of element-nodes. All these nodes having the same name, are mapped to the same SLayer object. All element-nodes of the the subtree of an addressed element-node are added to the corresponding SLayer object. Layers are recursive objects, therefore layers can contain other layers.

The following xml fragment

    <a att="value"><b>a sample <c>text</c></b></a>

with the use of the property

    genericXml.importer.sLayer=//a, //c

results in two SToken t1 and t2 objects, overlapping the text 'a sample' (t1) and 'text' (2). The SToken object t2 is part of a created SLayer object l1 corresponding to the element-node \<c\>. A SStructure object is created containing the SToken objects t1 and t2. This SStructure object and the SToken t1 are not part of the created layer. A second layer l2 is created for the element-node \<a\>. An SAnnotation object having the `SName` 'att' and the `SValue` 'value' is added to l2. All created SNode objects and the SLayer object l1 are contained in the layer l2.

#### genericXml.importer.ignoreWhitespaces

Determines a list of whitespace characters, that will be ignored, if a text-node only contains characters of this list. This is a necessary property, to avoid for instance the tokenization of blanks.

The following xml fragment

    <document><a>a</a> <b>sample</b> <c>text</c></document>

with the use of the property

    genericXml.importer.ignoreWhitepsaces='\n','\r','\t',' '

is mapped tothree SToken objects overlapping the text 'a', 'sample' and 'text' instead of 5 SToken objects additionally overlapping the blanks ' '. A sequence of characters will only be ignored, if it only contains characters being contained in that list. Therefore the following xml fragment

    <a>a sample</a>

with the use of the property

    genericXml.importer.ignoreWhitepsaces='\n','\r','\t'

is mapped to one SToken object overlapping the text 'a sample' including the blank.

#### genericXml.importer.ignoreNamespaces

Determines that all xml namespace declarations and xml namespace prefixes are ignored.

The following xml fragment

    <document xmlns="some namespace" xmlns:ns="some namespace">
        <ns:a ns:att="val"/>
    </document>

with the use of the property

    genericXml.importer.ignoreNamespaces=true

results in a SStructure object representing the element-node 'document' having no SAnnotation objects. A further SStructure object representing the element-node 'a' is created having the `sName` 'a' and a SAnnotation object having the `sName` 'att'.

The same fragment with the use of the property

    genericXml.importer.ignoreNamespaces=false

results in a SStructure object representing the element-node 'document' having two SAnnotation objects. First with `sName` 'xmlns' and `sValue` 'some namespace' and second with `sName` 'xmlns:ns' and `sValue` 'some namespace'. Same goes for the SStructure representing the element-node 'a'.

#### genericXml.importer.elementNameAsSAnno

Determines a list of element-nodes whose names are mapped to artificial SAnnotation objects added to the corresponding SNode object. The following xml fragment

    <a>
        <b/>
    </a>

with the use of the property

    genericXml.importer.elementNameAsSAnno=//a, //a//

results in one SNode object representing the element-node 'a' which is annotated with a SAnnotation whose `sName` and `sValue` is 'a'. The same goes for the second SNode object which gets an annotation b=b.

#### genericXml.importer.file.endings

Determines a list containing the file endings whose files are imported. If you want to import all contained files no matter their ending, add the string 'ALL' to the list. If you want a file having a specific ending not to be imported, use the prefix '-'.

#### genericXml.importer.separateTokens

Sets a separator between tokens, the default separator is a blank (' '), but it can be set to any other character or even ot the empty String. The following xml fragment:

    <w>
        <w>Tout</w>
        <w>au</w>
        <w>plus</w>
    </w>

with the default separator, which is equivalent to setting the property to blank results in the primary text "Tout au plus" instead of "Toutauplus". 


