/**
 * Copyright 2009 Humboldt-Universität zu Berlin, INRIA.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */
package de.hu_berlin.german.korpling.saltnpepper.pepperModules.genericXMLModules.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.corpus_tools.pepper.common.CorpusDesc;
import org.corpus_tools.pepper.common.FormatDesc;
import org.corpus_tools.pepper.testFramework.PepperImporterTest;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SCorpusGraph;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.samples.SampleGenerator;
import org.eclipse.emf.common.util.URI;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import de.hu_berlin.german.korpling.saltnpepper.pepperModules.genericXMLModules.GenericXMLImporter;

public class GenericXMLImporterTest extends PepperImporterTest {
	URI resourceURI = URI.createFileURI(new File(".").getAbsolutePath());

	@Before
	public void setUp() throws Exception {
		super.setFixture(new GenericXMLImporter());

		super.getFixture().setSaltProject(SaltFactory.createSaltProject());

		super.setResourcesURI(resourceURI);

		// setting temproraries and resources
		getFixture().setResources(resourceURI);

		// set formats to support
		FormatDesc formatDesc = new FormatDesc();
		formatDesc.setFormatName("xml");
		formatDesc.setFormatVersion("1.0");
		this.supportedFormatsCheck.add(formatDesc);
	}

	/**
	 * Creates a corpus having the following simple corpus structure:
	 * 
	 * <pre>
	 * 			corp1
	 * 		/			\
	 * 	corp2			corp3
	 * |	|			|		\
	 * doc1	doc2		doc3	doc4
	 * </pre>
	 * 
	 * doc1 and doc3 are created with
	 * {@link XML2SaltMapperTest#createHierarchy()} and doc2 and doc4 are
	 * created with {@link XML2SaltMapperTest#createSpan()} and
	 * 
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws XMLStreamException
	 */
	@Test
	public void testSet1() throws XMLStreamException, ParserConfigurationException, SAXException, IOException {
		// start: create xml-structure
		File tmpFolder = new File(System.getProperty("java.io.tmpdir"));
		File rootCorpus = new File(tmpFolder.getAbsolutePath() + "/corp1");
		rootCorpus.mkdirs();
		File corp2 = new File(rootCorpus.getAbsolutePath() + "/corp2");
		corp2.mkdirs();
		File corp3 = new File(rootCorpus.getAbsolutePath() + "/corp3");
		corp3.mkdirs();

		File doc1 = new File(corp2.getAbsolutePath() + "/doc1.xml");
		File doc2 = new File(corp2.getAbsolutePath() + "/doc2.xml");
		File doc3 = new File(corp3.getAbsolutePath() + "/doc3.xml");
		File doc4 = new File(corp3.getAbsolutePath() + "/doc4.xml");

		XMLOutputFactory xof = null;
		XMLStreamWriter xmlWriter = null;

		// write doc1
		xof = XMLOutputFactory.newInstance();
		xmlWriter = null;
		xmlWriter = xof.createXMLStreamWriter(new FileWriter(doc1.getAbsolutePath()));
		XML2SaltMapperTest.createHierarchy(xmlWriter);
		xmlWriter.flush();
		xmlWriter.close();

		// write doc2
		xof = XMLOutputFactory.newInstance();
		xmlWriter = null;
		xmlWriter = xof.createXMLStreamWriter(new FileWriter(doc2.getAbsolutePath()));
		XML2SaltMapperTest.createSpan(xmlWriter);
		xmlWriter.flush();
		xmlWriter.close();

		// write doc3
		xof = XMLOutputFactory.newInstance();
		xmlWriter = null;
		xmlWriter = xof.createXMLStreamWriter(new FileWriter(doc3.getAbsolutePath()));
		XML2SaltMapperTest.createHierarchy(xmlWriter);
		xmlWriter.flush();
		xmlWriter.close();

		// write doc4
		xof = XMLOutputFactory.newInstance();
		xmlWriter = null;
		xmlWriter = xof.createXMLStreamWriter(new FileWriter(doc4.getAbsolutePath()));
		XML2SaltMapperTest.createSpan(xmlWriter);
		xmlWriter.flush();
		xmlWriter.close();

		// end: create xml-structure

		// start: creating and setting corpus definition
		CorpusDesc corpDesc = new CorpusDesc();
		FormatDesc formatDesc = new FormatDesc();
		formatDesc.setFormatName("xml");
		formatDesc.setFormatVersion("1.0");
		corpDesc.setFormatDesc(formatDesc);
		corpDesc.setCorpusPath(URI.createFileURI(rootCorpus.getAbsolutePath()));
		getFixture().setCorpusDesc(corpDesc);
		// end: creating and setting corpus definition

		// SCorpusGraph importedSCorpusGraph=
		// SaltFactory.createSCorpusGraph();
		// getFixture().getSaltProject().getCorpusGraphs().add(importedSCorpusGraph);

		// runs the PepperModule
		this.start();

		SCorpusGraph importedSCorpusGraph = getFixture().getSaltProject().getCorpusGraphs().get(0);
		// check importCorpusStructure
		assertNotNull(importedSCorpusGraph.getCorpora());
		assertEquals(3, importedSCorpusGraph.getCorpora().size());

		assertNotNull(importedSCorpusGraph.getDocuments());
		assertEquals(4, importedSCorpusGraph.getDocuments().size());

		SDocument sDoc13 = SaltFactory.createSDocument();
		SampleGenerator.createSyntaxStructure(sDoc13);
		SampleGenerator.createSyntaxAnnotations(sDoc13);

		SDocument sDoc24 = SaltFactory.createSDocument();
		SampleGenerator.createInformationStructureSpan(sDoc24);
		SampleGenerator.createInformationStructureAnnotations(sDoc24);

		assertNotNull(importedSCorpusGraph.getDocuments().get(0));
		assertNotNull(importedSCorpusGraph.getDocuments().get(0).getDocumentGraph());
		assertNotNull(importedSCorpusGraph.getDocuments().get(0).getDocumentGraph().getTextualDSs());
		assertNotNull(importedSCorpusGraph.getDocuments().get(0).getDocumentGraph().getTokens());
		assertTrue(importedSCorpusGraph.getDocuments().get(0).getDocumentGraph().getTokens().size() > 0);

		assertNotNull(importedSCorpusGraph.getDocuments().get(1));
		assertNotNull(importedSCorpusGraph.getDocuments().get(1).getDocumentGraph());
		assertNotNull(importedSCorpusGraph.getDocuments().get(1).getDocumentGraph().getTextualDSs());
		assertNotNull(importedSCorpusGraph.getDocuments().get(1).getDocumentGraph().getTokens());
		assertTrue(importedSCorpusGraph.getDocuments().get(1).getDocumentGraph().getTokens().size() > 0);

		assertNotNull(importedSCorpusGraph.getDocuments().get(2));
		assertNotNull(importedSCorpusGraph.getDocuments().get(2).getDocumentGraph());
		assertNotNull(importedSCorpusGraph.getDocuments().get(2).getDocumentGraph().getTextualDSs());
		assertNotNull(importedSCorpusGraph.getDocuments().get(2).getDocumentGraph().getTokens());
		assertTrue(importedSCorpusGraph.getDocuments().get(2).getDocumentGraph().getTokens().size() > 0);

		assertNotNull(importedSCorpusGraph.getDocuments().get(3));
		assertNotNull(importedSCorpusGraph.getDocuments().get(3).getDocumentGraph());
		assertNotNull(importedSCorpusGraph.getDocuments().get(3).getDocumentGraph().getTextualDSs());
		assertNotNull(importedSCorpusGraph.getDocuments().get(3).getDocumentGraph().getTokens());
		assertTrue(importedSCorpusGraph.getDocuments().get(3).getDocumentGraph().getTokens().size() > 0);
	}
}
