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

public class GenericXMLImporterTest extends PepperImporterTest {
	
	private GenericXMLImporter fixture= null;

	public void setFixture(GenericXMLImporter fixture) {
		this.fixture = fixture;
	}

	public GenericXMLImporter getFixture() {
		return fixture;
	}
	
	public void setUp()
	{
		this.setFixture(new GenericXMLImporter());
		
		this.setName("GenericXMLImporter");
		this.setTemprorariesURI(URI.createFileURI(System.getProperty("java.io.tmpdir")));
		this.setResourcesURI(URI.createFileURI(System.getProperty("java.io.tmpdir")));
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
			File corp2= new File(rootCorpus.getAbsolutePath()+"/corp2");
			File corp3= new File(rootCorpus.getAbsolutePath()+"/corp3");
			
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
		
		SCorpusGraph importedCorpusGraph= SaltCommonFactory.eINSTANCE.createSCorpusGraph();
		this.getFixture().getSaltProject().getSCorpusGraphs().add(importedCorpusGraph);
		this.getFixture().importCorpusStructure(importedCorpusGraph);
		
		//runs the PepperModule
		this.start();
		
		SDocument sDoc13= SaltFactory.eINSTANCE.createSDocument();
		SaltSample.createSyntaxStructure(sDoc13);
		SaltSample.createSyntaxAnnotations(sDoc13);
		
		SDocument sDoc24= SaltFactory.eINSTANCE.createSDocument();
		SaltSample.createInformationStructureSpan(sDoc24);
		SaltSample.createInformationStructureAnnotations(sDoc24);
		
		assertNotNull(importedCorpusGraph.getSCorpora());
		assertEquals(3, importedCorpusGraph.getSCorpora().size());
		
		assertNotNull(importedCorpusGraph.getSDocuments());
		assertEquals(4, importedCorpusGraph.getSDocuments().size());
		
		assertNotNull(importedCorpusGraph.getSDocuments().get(0));
		assertNotNull(importedCorpusGraph.getSDocuments().get(0).getSDocumentGraph());
		assertNotNull(importedCorpusGraph.getSDocuments().get(0).getSDocumentGraph().getSTextualDSs());
		assertNotNull(importedCorpusGraph.getSDocuments().get(0).getSDocumentGraph().getSTokens());
		assertTrue(importedCorpusGraph.getSDocuments().get(0).getSDocumentGraph().getSTokens().size()>0);
        
		assertNotNull(importedCorpusGraph.getSDocuments().get(1));
        assertNotNull(importedCorpusGraph.getSDocuments().get(1).getSDocumentGraph());
        assertNotNull(importedCorpusGraph.getSDocuments().get(1).getSDocumentGraph().getSTextualDSs());
        assertNotNull(importedCorpusGraph.getSDocuments().get(1).getSDocumentGraph().getSTokens());
        assertTrue(importedCorpusGraph.getSDocuments().get(1).getSDocumentGraph().getSTokens().size()>0);
        
        assertNotNull(importedCorpusGraph.getSDocuments().get(2));
        assertNotNull(importedCorpusGraph.getSDocuments().get(2).getSDocumentGraph());
        assertNotNull(importedCorpusGraph.getSDocuments().get(2).getSDocumentGraph().getSTextualDSs());
        assertNotNull(importedCorpusGraph.getSDocuments().get(2).getSDocumentGraph().getSTokens());
        assertTrue(importedCorpusGraph.getSDocuments().get(2).getSDocumentGraph().getSTokens().size()>0);
        
        assertNotNull(importedCorpusGraph.getSDocuments().get(3));
        assertNotNull(importedCorpusGraph.getSDocuments().get(3).getSDocumentGraph());
        assertNotNull(importedCorpusGraph.getSDocuments().get(3).getSDocumentGraph().getSTextualDSs());
        assertNotNull(importedCorpusGraph.getSDocuments().get(3).getSDocumentGraph().getSTokens());
        assertTrue(importedCorpusGraph.getSDocuments().get(3).getSDocumentGraph().getSTokens().size()>0);
		
		
//		assertEquals(sDoc13, importedCorpusGraph.getSDocuments().get(0));
//		assertEquals(sDoc13, importedCorpusGraph.getSDocuments().get(2));
//		assertEquals(sDoc24, importedCorpusGraph.getSDocuments().get(1));
//		assertEquals(sDoc24, importedCorpusGraph.getSDocuments().get(3));
	}
	
}

