/**
 * Copyright 2009 Humboldt University of Berlin, INRIA.
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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.eclipse.emf.common.util.URI;
import org.xml.sax.SAXException;

import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.CorpusDefinition;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.FormatDefinition;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.PepperModulesFactory;
import de.hu_berlin.german.korpling.saltnpepper.pepper.testSuite.moduleTests.PepperImporterTest;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.genericXMLModules.GenericXMLImporter;
import de.hu_berlin.german.korpling.saltnpepper.salt.SaltFactory;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltCommonFactory;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpusGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltSample.SaltSample;

public class GenericXMLImporterTest extends PepperImporterTest
{	
	URI resourceURI= URI.createFileURI(new File(".").getAbsolutePath());
	URI temproraryURI= URI.createFileURI(System.getProperty("java.io.tmpdir"));	
	
	protected void setUp() throws Exception 
	{
		super.setFixture(new GenericXMLImporter());
		
		super.getFixture().setSaltProject(SaltCommonFactory.eINSTANCE.createSaltProject());
		super.setResourcesURI(resourceURI);
		super.setTemprorariesURI(temproraryURI);
		
		//setting temproraries and resources
		this.getFixture().setTemproraries(temproraryURI);
		this.getFixture().setResources(resourceURI);
		
		//set formats to support
		FormatDefinition formatDef= PepperModulesFactory.eINSTANCE.createFormatDefinition();
		formatDef.setFormatName("xml");
		formatDef.setFormatVersion("1.0");
		this.supportedFormatsCheck.add(formatDef);
	}
	
	/**
	 * Creates a corpus having the following simple corpus structure:
	 * <pre>
	 * 			corp1
	 * 		/			\
	 * 	corp2			corp3
	 * |	|			|		\
	 * doc1	doc2		doc3	doc4
	 * </pre>
	 * 
	 * doc1 and doc3 are created with {@link XML2SaltMapperTest#createHierarchy()} and
	 * doc2 and doc4 are created with {@link XML2SaltMapperTest#createSpan()} and
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws XMLStreamException 
	 */
	public void testSet1() throws XMLStreamException, ParserConfigurationException, SAXException, IOException
	{
		//start: create xml-structure
			File tmpFolder= new File(System.getProperty("java.io.tmpdir"));
			File rootCorpus= new File(tmpFolder.getAbsolutePath()+"/corp1");
			rootCorpus.mkdirs();
			File corp2= new File(rootCorpus.getAbsolutePath()+"/corp2");
			corp2.mkdirs();
			File corp3= new File(rootCorpus.getAbsolutePath()+"/corp3");
			corp3.mkdirs();
			
			File doc1= new File(corp2.getAbsolutePath()+"/doc1.xml");
			File doc2= new File(corp2.getAbsolutePath()+"/doc2.xml");
			File doc3= new File(corp3.getAbsolutePath()+"/doc3.xml");
			File doc4= new File(corp3.getAbsolutePath()+"/doc4.xml");
			
			XMLOutputFactory xof= null;
			XMLStreamWriter xmlWriter= null;
			
			//write doc1
			xof = XMLOutputFactory.newInstance();
	        xmlWriter = null;
	        xmlWriter = xof.createXMLStreamWriter(new FileWriter(doc1.getAbsolutePath()));
	        XML2SaltMapperTest.createHierarchy(xmlWriter);
	        xmlWriter.flush();
	        xmlWriter.close();
	        
	      //write doc2
			xof = XMLOutputFactory.newInstance();
	        xmlWriter = null;
	        xmlWriter = xof.createXMLStreamWriter(new FileWriter(doc2.getAbsolutePath()));
	        XML2SaltMapperTest.createSpan(xmlWriter);
	        xmlWriter.flush();
	        xmlWriter.close();
	        
	      //write doc3
			xof = XMLOutputFactory.newInstance();
	        xmlWriter = null;
	        xmlWriter = xof.createXMLStreamWriter(new FileWriter(doc3.getAbsolutePath()));
	        XML2SaltMapperTest.createHierarchy(xmlWriter);
	        xmlWriter.flush();
	        xmlWriter.close();
	        
	      //write doc4
			xof = XMLOutputFactory.newInstance();
	        xmlWriter = null;
	        xmlWriter = xof.createXMLStreamWriter(new FileWriter(doc4.getAbsolutePath()));
	        XML2SaltMapperTest.createSpan(xmlWriter);
	        xmlWriter.flush();
	        xmlWriter.close();
			
		//end: create xml-structure
		
		//start: creating and setting corpus definition
			CorpusDefinition corpDef= PepperModulesFactory.eINSTANCE.createCorpusDefinition();
			FormatDefinition formatDef= PepperModulesFactory.eINSTANCE.createFormatDefinition();
			formatDef.setFormatName("xml");
			formatDef.setFormatVersion("1.0");
			corpDef.setFormatDefinition(formatDef);
			corpDef.setCorpusPath(URI.createFileURI(rootCorpus.getAbsolutePath()));
			this.getFixture().setCorpusDefinition(corpDef);
		//end: creating and setting corpus definition
		
		SCorpusGraph importedSCorpusGraph= SaltCommonFactory.eINSTANCE.createSCorpusGraph();
		this.getFixture().getSaltProject().getSCorpusGraphs().add(importedSCorpusGraph);
		
		//runs the PepperModule
		this.start();
		
		//check importCorpusStructure
		assertNotNull(importedSCorpusGraph.getSCorpora());
		assertEquals(3, importedSCorpusGraph.getSCorpora().size());
		
		assertNotNull(importedSCorpusGraph.getSDocuments());
		assertEquals(4, importedSCorpusGraph.getSDocuments().size());
		
		SDocument sDoc13= SaltFactory.eINSTANCE.createSDocument();
		SaltSample.createSyntaxStructure(sDoc13);
		SaltSample.createSyntaxAnnotations(sDoc13);
		
		SDocument sDoc24= SaltFactory.eINSTANCE.createSDocument();
		SaltSample.createInformationStructureSpan(sDoc24);
		SaltSample.createInformationStructureAnnotations(sDoc24);
		
		assertNotNull(importedSCorpusGraph.getSDocuments().get(0));
		assertNotNull(importedSCorpusGraph.getSDocuments().get(0).getSDocumentGraph());
		assertNotNull(importedSCorpusGraph.getSDocuments().get(0).getSDocumentGraph().getSTextualDSs());
		assertNotNull(importedSCorpusGraph.getSDocuments().get(0).getSDocumentGraph().getSTokens());
		assertTrue(importedSCorpusGraph.getSDocuments().get(0).getSDocumentGraph().getSTokens().size()>0);
        
		assertNotNull(importedSCorpusGraph.getSDocuments().get(1));
        assertNotNull(importedSCorpusGraph.getSDocuments().get(1).getSDocumentGraph());
        assertNotNull(importedSCorpusGraph.getSDocuments().get(1).getSDocumentGraph().getSTextualDSs());
        assertNotNull(importedSCorpusGraph.getSDocuments().get(1).getSDocumentGraph().getSTokens());
        assertTrue(importedSCorpusGraph.getSDocuments().get(1).getSDocumentGraph().getSTokens().size()>0);
        
        assertNotNull(importedSCorpusGraph.getSDocuments().get(2));
        assertNotNull(importedSCorpusGraph.getSDocuments().get(2).getSDocumentGraph());
        assertNotNull(importedSCorpusGraph.getSDocuments().get(2).getSDocumentGraph().getSTextualDSs());
        assertNotNull(importedSCorpusGraph.getSDocuments().get(2).getSDocumentGraph().getSTokens());
        assertTrue(importedSCorpusGraph.getSDocuments().get(2).getSDocumentGraph().getSTokens().size()>0);
        
        assertNotNull(importedSCorpusGraph.getSDocuments().get(3));
        assertNotNull(importedSCorpusGraph.getSDocuments().get(3).getSDocumentGraph());
        assertNotNull(importedSCorpusGraph.getSDocuments().get(3).getSDocumentGraph().getSTextualDSs());
        assertNotNull(importedSCorpusGraph.getSDocuments().get(3).getSDocumentGraph().getSTokens());
        assertTrue(importedSCorpusGraph.getSDocuments().get(3).getSDocumentGraph().getSTokens().size()>0);
		
		
//		assertEquals(sDoc13, importedCorpusGraph.getSDocuments().get(0));
//		assertEquals(sDoc13, importedCorpusGraph.getSDocuments().get(2));
//		assertEquals(sDoc24, importedCorpusGraph.getSDocuments().get(1));
//		assertEquals(sDoc24, importedCorpusGraph.getSDocuments().get(3));
	}
}
