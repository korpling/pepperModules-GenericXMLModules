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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.PepperModuleProperty;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.genericXMLModules.GenericXMLImporterProperties;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.genericXMLModules.XML2SaltMapper;
import de.hu_berlin.german.korpling.saltnpepper.salt.SaltFactory;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDataSourceSequence;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpan;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SStructure;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STYPE_NAME;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SToken;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SAnnotation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SLayer;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SMetaAnnotation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SNode;
import de.hu_berlin.german.korpling.saltnpepper.salt.samples.SampleGenerator;

public class XML2SaltMapperTest {

	private XML2SaltMapper fixture = null;

	public void setFixture(XML2SaltMapper fixture) {
		this.fixture = fixture;
	}

	public XML2SaltMapper getFixture() {
		return fixture;
	}

	/**
	 * {@link OutputStream} where the xml nodes are written to.
	 */
	private ByteArrayOutputStream outStream = null;
	/**
	 * XMLWriter to write an xml stream.
	 */
	private XMLStreamWriter xmlWriter = null;

	/**
	 * Properties which are used for this fixture
	 */
	private GenericXMLImporterProperties props = null;

	@Before
	public void setUp() throws XMLStreamException {
		XML2SaltMapper mapper = new XML2SaltMapper();
		mapper.setSDocument(SaltFactory.eINSTANCE.createSDocument());
		mapper.getSDocument().setSDocumentGraph(SaltFactory.eINSTANCE.createSDocumentGraph());
		mapper.getSDocument().getSDocumentGraph().setSName("testGraph");
		props = new GenericXMLImporterProperties();
		mapper.setProperties(props);
		this.setFixture(mapper);

		outStream = new ByteArrayOutputStream();
		XMLOutputFactory o = XMLOutputFactory.newFactory();
		xmlWriter = o.createXMLStreamWriter(outStream);
	}

	@After
	public void tearDown() {
		outStream.reset();
	}

	private void start(XML2SaltMapper mapper, String xmlString) throws ParserConfigurationException, SAXException, IOException {
		File tmpDir = new File(System.getProperty("java.io.tmpdir") + "/xml2saltTest/");
		tmpDir.mkdirs();
		File tmpFile = new File(tmpDir.getAbsolutePath() + System.currentTimeMillis() + ".xml");
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(tmpFile, "UTF-8");
			writer.println(xmlString);
		} finally {
			if (writer != null)
				writer.close();
		}

		this.getFixture().setResourceURI(URI.createFileURI(tmpFile.getAbsolutePath()));
		this.getFixture().mapSDocument();
	}

	@Test
	public void testOnlyContainsIgnorableCharacters() {
		assertTrue(this.getFixture().onlyContainsIgnorableCharacters(" "));
		assertTrue(this.getFixture().onlyContainsIgnorableCharacters("\n"));
		assertTrue(this.getFixture().onlyContainsIgnorableCharacters("\r"));
		assertTrue(this.getFixture().onlyContainsIgnorableCharacters("\n\r \n"));
		assertFalse(this.getFixture().onlyContainsIgnorableCharacters("f\n\r \n"));
		assertFalse(this.getFixture().onlyContainsIgnorableCharacters("\nsdf\r \n"));
		assertFalse(this.getFixture().onlyContainsIgnorableCharacters("\nsdf\r \nafe"));
	}

	/**
	 * Checks if a simple text is mapped. <br/>
	 * sample snippet:<br/>
	 * &lt;text&gt;Is this example more complicated than it appears
	 * to?&lt;/text&gt;
	 * 
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws XMLStreamException
	 */
	@Test
	public void testSimpleText() throws ParserConfigurationException, SAXException, IOException, XMLStreamException {
		String text = "Is this example more complicated than it appears to?";
		xmlWriter.writeStartDocument();
		xmlWriter.writeStartElement("text");
		xmlWriter.writeCharacters(text);
		xmlWriter.writeEndElement();
		xmlWriter.writeEndDocument();
		xmlWriter.flush();

		String xml = outStream.toString();
		start(this.getFixture(), xml);

		assertNotNull(this.getFixture().getSDocument().getSDocumentGraph().getSTextualDSs());
		assertNotNull(this.getFixture().getSDocument().getSDocumentGraph().getSTextualDSs().get(0));
		assertNotNull(this.getFixture().getSDocument().getSDocumentGraph().getSTextualDSs().get(0).getSText());
		assertEquals(text, this.getFixture().getSDocument().getSDocumentGraph().getSTextualDSs().get(0).getSText());
	}
	
	/**
	 * Checks if a pretty printed xml is imported ignoring whitespace characters.
	 * <pre>
	 * <w>
	 *     <w>Tout</w>
	 *	   <w>au</w>
	 *     <w>plus</w>
	 * </w>
	 * </pre>
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws XMLStreamException
	 */
	@Test
	public void testPrettyPrintedXML() throws ParserConfigurationException, SAXException, IOException, XMLStreamException {
		String text = "Tout au plus";
		xmlWriter.writeStartDocument();
		xmlWriter.writeStartElement("w");
			xmlWriter.writeCharacters("\n\t");
			xmlWriter.writeStartElement("w");
				xmlWriter.writeCharacters("Tout");
			xmlWriter.writeEndElement();
			xmlWriter.writeCharacters("\n\t\t");
			xmlWriter.writeStartElement("w");
				xmlWriter.writeCharacters("au");
			xmlWriter.writeEndElement();
			xmlWriter.writeCharacters("\n\t\t");
			xmlWriter.writeStartElement("w");
				xmlWriter.writeCharacters("plus");
			xmlWriter.writeEndElement();
			xmlWriter.writeCharacters("\n\t\t");
		xmlWriter.writeEndElement();
		
		xmlWriter.writeEndDocument();
		xmlWriter.flush();

		String xml = outStream.toString();
		start(this.getFixture(), xml);

		assertNotNull(this.getFixture().getSDocument().getSDocumentGraph().getSTextualDSs());
		assertNotNull(this.getFixture().getSDocument().getSDocumentGraph().getSTextualDSs().get(0));
		assertNotNull(this.getFixture().getSDocument().getSDocumentGraph().getSTextualDSs().get(0).getSText());
		assertEquals(text, this.getFixture().getSDocument().getSDocumentGraph().getSTextualDSs().get(0).getSText());
	}

	/**
	 * Checks if a simple text is mapped, even it is interrupted. See: <br/>
	 * &lt;text&gt;Is this example more&lt;text&gt; complicated than it appears
	 * to be?&lt;/text&gt;&lt;/text&gt; <br/>
	 * Shall be mapped to:
	 * <table border="1">
	 * <tr>
	 * <td colspan="2">span1</td>
	 * <tr>
	 * <tr>
	 * <td>tok1</td>
	 * <td>tok2</td>
	 * <tr>
	 * <tr>
	 * <td>Is this example more</td>
	 * <td>complicated than it appears to be?</td>
	 * <tr>
	 * </table>
	 * 
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws XMLStreamException
	 */
	@Test
	public void testSimpleText_Interrupt() throws ParserConfigurationException, SAXException, IOException, XMLStreamException {
		String text1 = "Is this example more";
		String text2 = "complicated than it appears to be?";
		String text = text1 +" "+ text2;
		xmlWriter.writeStartDocument();
		xmlWriter.writeStartElement("text");
		xmlWriter.writeAttribute("no", "text1");
		xmlWriter.writeCharacters(text1);
		xmlWriter.writeStartElement("text");
		xmlWriter.writeAttribute("no", "text2");
		xmlWriter.writeCharacters(text2);
		xmlWriter.writeEndElement();
		xmlWriter.writeEndElement();
		xmlWriter.writeEndDocument();
		xmlWriter.flush();

		String xml = outStream.toString();
		start(this.getFixture(), xml);

		assertNotNull(this.getFixture().getSDocument().getSDocumentGraph().getSTextualDSs());
		assertNotNull(this.getFixture().getSDocument().getSDocumentGraph().getSTextualDSs().get(0));
		assertNotNull(this.getFixture().getSDocument().getSDocumentGraph().getSTextualDSs().get(0).getSText());
		assertEquals(text, this.getFixture().getSDocument().getSDocumentGraph().getSTextualDSs().get(0).getSText());
	}

	/**
	 * Checks if a simple text is mapped and if a token is created. <br/>
	 * See:<br/>
	 * &lt;text&gt;Is this example more complicated than it appears
	 * to?&lt;/text&gt; <br/>
	 * shall be mapped to:
	 * <table border="1">
	 * <tr>
	 * <td>Is this example more complicated than it appears to?</td>
	 * <tr>
	 * </table>
	 * 
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws XMLStreamException
	 */
	@Test
	public void testSimpleToken() throws ParserConfigurationException, SAXException, IOException, XMLStreamException {
		String text = "Is this example more complicated than it appears to?";

		xmlWriter.writeStartDocument();
		xmlWriter.writeStartElement("text");
		xmlWriter.writeCharacters(text);
		xmlWriter.writeEndElement();
		xmlWriter.writeEndDocument();
		xmlWriter.flush();

		String xml = outStream.toString();
		start(this.getFixture(), xml);

		assertEquals(1, this.getFixture().getSDocument().getSDocumentGraph().getSTokens().size());
		assertNotNull(this.getFixture().getSDocument().getSDocumentGraph().getSTokens().get(0));
		EList<STYPE_NAME> sRelationTypes = new BasicEList<STYPE_NAME>();
		sRelationTypes.add(STYPE_NAME.STEXT_OVERLAPPING_RELATION);
		EList<SDataSourceSequence> sequences = this.getFixture().getSDocument().getSDocumentGraph().getOverlappedDSSequences(this.getFixture().getSDocument().getSDocumentGraph().getSTokens().get(0), sRelationTypes);
		assertEquals(Integer.valueOf(0), sequences.get(0).getSStart());
		assertEquals(Integer.valueOf(text.length()), sequences.get(0).getSEnd());
	}

	/**
	 * Checks if a simple text is mapped and a token is created. Further checks
	 * if an {@link SAnnotation} was created. <br/>
	 * sample snippet: <br/>
	 * &lt;text attName1=&quot;attValue1&quot;&gt;Is this example more
	 * complicated than it appears to?&lt;/text&gt; <br/>
	 * 
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws XMLStreamException
	 */
	@Test
	public void testSimpleToken_WithAttribute() throws ParserConfigurationException, SAXException, IOException, XMLStreamException {
		String text = "Is this example more complicated than it appears to?";
		String attName1 = "attName1";
		String attValue1 = "attValue1";

		xmlWriter.writeStartDocument();
		xmlWriter.writeStartElement("text");
		xmlWriter.writeAttribute(attName1, attValue1);
		xmlWriter.writeCharacters(text);
		xmlWriter.writeEndElement();
		xmlWriter.writeEndDocument();
		xmlWriter.flush();

		String xml = outStream.toString();
		start(this.getFixture(), xml);

		assertEquals(1, this.getFixture().getSDocument().getSDocumentGraph().getSTokens().size());
		assertNotNull(this.getFixture().getSDocument().getSDocumentGraph().getSTokens().get(0));
		EList<STYPE_NAME> sRelationTypes = new BasicEList<STYPE_NAME>();
		sRelationTypes.add(STYPE_NAME.STEXT_OVERLAPPING_RELATION);
		EList<SDataSourceSequence> sequences = this.getFixture().getSDocument().getSDocumentGraph().getOverlappedDSSequences(this.getFixture().getSDocument().getSDocumentGraph().getSTokens().get(0), sRelationTypes);
		assertEquals(Integer.valueOf(0), sequences.get(0).getSStart());
		assertEquals(Integer.valueOf(text.length()), sequences.get(0).getSEnd());
		assertNotNull("an SAnnotation with name '" + attName1 + "' does not belong to annotation list '" + this.getFixture().getSDocument().getSDocumentGraph().getSTokens().get(0).getSAnnotations() + "'", this.getFixture().getSDocument().getSDocumentGraph().getSTokens().get(0).getSAnnotation(attName1));
		assertEquals(attValue1, this.getFixture().getSDocument().getSDocumentGraph().getSTokens().get(0).getSAnnotation(attName1).getSValue());
	}

	/**
	 * Checks if a simple text is mapped and a token is created. Further checks
	 * if an SAnnotation was created. And if one was not created when property
	 * was set.
	 * 
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws XMLStreamException
	 */
	@Test
	public void testSimpleToken_WithAttributes() throws ParserConfigurationException, SAXException, IOException, XMLStreamException {
		String attName1 = "attName1";
		String attValue1 = "attValue1";
		String attName2 = "attName2";
		String attValue2 = "attValue2";

		PepperModuleProperty<String> prop = (PepperModuleProperty<String>) this.getFixture().getProperties().getProperty(GenericXMLImporterProperties.PROP_IGNORE_LIST);
		prop.setValue("//" + attName1);
		String text = "Is this example more complicated than it appears to?";

		xmlWriter.writeStartDocument();
		xmlWriter.writeStartElement("text");
		xmlWriter.writeAttribute(attName1, attValue1);
		xmlWriter.writeAttribute(attName2, attValue2);
		xmlWriter.writeCharacters(text);
		xmlWriter.writeEndElement();
		xmlWriter.writeEndDocument();
		xmlWriter.flush();

		String xml = outStream.toString();
		start(this.getFixture(), xml);

		assertEquals(1, this.getFixture().getSDocument().getSDocumentGraph().getSTokens().size());
		assertNotNull(this.getFixture().getSDocument().getSDocumentGraph().getSTokens().get(0));
		EList<STYPE_NAME> sRelationTypes = new BasicEList<STYPE_NAME>();
		sRelationTypes.add(STYPE_NAME.STEXT_OVERLAPPING_RELATION);
		EList<SDataSourceSequence> sequences = this.getFixture().getSDocument().getSDocumentGraph().getOverlappedDSSequences(this.getFixture().getSDocument().getSDocumentGraph().getSTokens().get(0), sRelationTypes);
		assertEquals(Integer.valueOf(0), sequences.get(0).getSStart());
		assertEquals(Integer.valueOf(text.length()), sequences.get(0).getSEnd());
		assertNotNull(this.getFixture().getSDocument().getSDocumentGraph().getSTokens().get(0).getSAnnotation(attName1));
		assertNotNull(this.getFixture().getSDocument().getSDocumentGraph().getSTokens().get(0).getSAnnotation(attName2));
		assertEquals(attValue2, this.getFixture().getSDocument().getSDocumentGraph().getSTokens().get(0).getSAnnotation(attName2).getSValue());
	}

	/**
	 * Checks if an element-node containing a text-node and further element
	 * nodes will be mapped correctly. No properties used. The xml snippet: <br/>
	 * &lt;a&gt;here&lt;b&gt;comes&lt;/b&gt;text&lt;/a&gt; <br/>
	 * shall be mapped to:
	 * 
	 * <table border="1">
	 * <tr>
	 * <td colspan="3">struct</td>
	 * <tr>
	 * <tr>
	 * <td>tok1</td>
	 * <td>tok2</td>
	 * <td>tok3</td>
	 * <tr>
	 * <tr>
	 * <td>here</td>
	 * <td>comes</td>
	 * <td>text</td>
	 * <tr>
	 * </table>
	 * 
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws XMLStreamException
	 */
	@Test
	public void testElementNodeWithComplexContent() throws ParserConfigurationException, SAXException, IOException, XMLStreamException {
		String text1 = "here";
		String text2 = "comes";
		String text3 = "text";

		xmlWriter.writeStartDocument();
		xmlWriter.writeStartElement("a");
		xmlWriter.writeCharacters(text1);
		xmlWriter.writeStartElement("b");
		xmlWriter.writeCharacters(text2);
		xmlWriter.writeEndElement();
		xmlWriter.writeCharacters(text3);
		xmlWriter.writeEndElement();
		xmlWriter.writeEndDocument();
		xmlWriter.flush();

		String xml = outStream.toString();
		start(this.getFixture(), xml);

		assertEquals(3, this.getFixture().getSDocument().getSDocumentGraph().getSTokens().size());
		assertEquals(1, this.getFixture().getSDocument().getSDocumentGraph().getSStructures().size());
	}

	/**
	 * Checks if a fragment with a deeper hierarchie is mapped correctly. The
	 * xml snippet: <br/>
	 * &lt;a&gt;&lt;b&gt;&lt;c&gt;a&lt;/c&gt;&lt;c&gt;sample&lt;/c&gt;&lt;/b&gt;
	 * &lt;c&gt;tex&lt;/c&gt;&lt;/a&gt; <br/>
	 * shall be mapped to:
	 * 
	 * <table border="1">
	 * <tr>
	 * <td align="center" colspan="3">struct a</td>
	 * <tr>
	 * <tr>
	 * <td colspan="2">struct b</td>
	 * <td/>
	 * <tr>
	 * <tr>
	 * <td>tok1</td>
	 * <td>tok2</td>
	 * <td>tok3</td>
	 * <tr>
	 * <tr>
	 * <td>a</td>
	 * <td>sample</td>
	 * <td>text</td>
	 * <tr>
	 * </table>
	 * 
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws XMLStreamException
	 */
	@Test
	public void testDeepHierarchie() throws ParserConfigurationException, SAXException, IOException, XMLStreamException {
		String text1 = "a";
		String text2 = "sample";
		String text3 = "text";
		String elementA = "a";
		String elementB = "b";
		String elementC = "c";

		xmlWriter.writeStartDocument();
		xmlWriter.writeStartElement(elementA);
		xmlWriter.writeStartElement(elementB);
		xmlWriter.writeStartElement(elementC);
		xmlWriter.writeCharacters(text1);
		xmlWriter.writeEndElement();
		xmlWriter.writeStartElement(elementC);
		xmlWriter.writeCharacters(text2);
		xmlWriter.writeEndElement();
		xmlWriter.writeEndElement();
		xmlWriter.writeStartElement(elementC);
		xmlWriter.writeCharacters(text3);
		xmlWriter.writeEndElement();
		xmlWriter.writeEndElement();
		xmlWriter.writeEndDocument();
		xmlWriter.flush();

		String xml = outStream.toString();
		start(this.getFixture(), xml);

		assertEquals(3, this.getFixture().getSDocument().getSDocumentGraph().getSTokens().size());
		assertEquals(2, this.getFixture().getSDocument().getSDocumentGraph().getSStructures().size());
		assertEquals(4, this.getFixture().getSDocument().getSDocumentGraph().getSDominanceRelations().size());
	}

	/**
	 * Checks if all created {@link SToken} objects got the correct
	 * {@link SAnnotation} objects. <br/>
	 * The xml snippet: <br/>
	 * &lt;a&gt;&lt;b attB1="valB1"&gt;here&lt;b
	 * attB2="valB2"&gt;comes&lt;/b&gt;&lt;/a&gt; <br/>
	 * 
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws XMLStreamException
	 */
	@Test
	public void testSTokensAndSAnnotations() throws ParserConfigurationException, SAXException, IOException, XMLStreamException {
		String text1 = "here";
		String text2 = "text";
		String attB1 = "attB1";
		String valB1 = "valB1";
		String attB2 = "attB2";
		String valB2 = "valB2";

		xmlWriter.writeStartDocument();
		xmlWriter.writeStartElement("a");
		xmlWriter.writeStartElement("b");
		xmlWriter.writeAttribute(attB1, valB1);
		xmlWriter.writeCharacters(text1);
		xmlWriter.writeEndElement();
		xmlWriter.writeStartElement("b");
		xmlWriter.writeAttribute(attB2, valB2);
		xmlWriter.writeCharacters(text2);
		xmlWriter.writeEndElement();
		xmlWriter.writeEndElement();
		xmlWriter.writeEndDocument();
		xmlWriter.flush();

		String xml = outStream.toString();
		start(this.getFixture(), xml);

		assertEquals(2, this.getFixture().getSDocument().getSDocumentGraph().getSTokens().size());
		assertNotNull(this.getFixture().getSDocument().getSDocumentGraph().getSTokens().get(0).getSAnnotations());
		assertEquals(1, this.getFixture().getSDocument().getSDocumentGraph().getSTokens().get(0).getSAnnotations().size());
		assertEquals(valB1, this.getFixture().getSDocument().getSDocumentGraph().getSTokens().get(0).getSAnnotation(attB1).getSValue());

		assertNotNull(this.getFixture().getSDocument().getSDocumentGraph().getSTokens().get(1).getSAnnotations());
		assertEquals(1, this.getFixture().getSDocument().getSDocumentGraph().getSTokens().get(1).getSAnnotations().size());
		assertEquals(valB2, this.getFixture().getSDocument().getSDocumentGraph().getSTokens().get(1).getSAnnotation(attB2).getSValue());
	}

	/**
	 * Checks that the following snippet is mapped to just one {@link SToken}
	 * object. The xml snippet: <br/>
	 * &lt;b&gt;text&lt;/b&gt; <br/>
	 * 
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws XMLStreamException
	 */
	@Test
	public void testJustSToken() throws ParserConfigurationException, SAXException, IOException, XMLStreamException {
		String text1 = "text";

		xmlWriter.writeStartDocument();
		xmlWriter.writeStartElement("b");
		xmlWriter.writeCharacters(text1);
		xmlWriter.writeEndElement();
		xmlWriter.writeEndDocument();
		xmlWriter.flush();

		String xml = outStream.toString();
		start(this.getFixture(), xml);

		assertEquals(1, this.getFixture().getSDocument().getSDocumentGraph().getSTokens().size());
		assertEquals("all nodes are: '" + this.getFixture().getSDocument().getSDocumentGraph().getSNodes() + "'", 2, this.getFixture().getSDocument().getSDocumentGraph().getSNodes().size());
	}

	/**
	 * Checks that the following snippet is mapped to one root
	 * {@link SStructure} overlapping another {@link SStructure} object
	 * overlapping three {@link SToken} objects. The xml snippet:
	 * 
	 * <pre>
	 * &lt;a&gt;&lt;b att="val"&gt;a&lt;/b&gt; sample&lt;c/&gt;text &lt;/a&gt;
	 * </pre>
	 * 
	 * @throws XMLStreamException
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * 
	 */
	@Test
	public void testElementNodeMixedContent() throws XMLStreamException, ParserConfigurationException, SAXException, IOException {
		xmlWriter.writeStartDocument();
		xmlWriter.writeStartElement("root");
		xmlWriter.writeStartElement("a");
		xmlWriter.writeStartElement("b");
		xmlWriter.writeAttribute("att", "val");
		xmlWriter.writeCharacters("a");
		xmlWriter.writeEndElement();
		xmlWriter.writeCharacters("sample");
		xmlWriter.writeStartElement("c");
		xmlWriter.writeEndElement();
		xmlWriter.writeCharacters("text");
		xmlWriter.writeEndElement();
		xmlWriter.writeEndElement();
		xmlWriter.writeEndDocument();
		xmlWriter.flush();

		String xml = outStream.toString();
		start(this.getFixture(), xml);

		assertEquals(3, this.getFixture().getSDocument().getSDocumentGraph().getSTokens().size());
		assertEquals(2, this.getFixture().getSDocument().getSDocumentGraph().getSStructures().size());
		SStructure sStruct = null;
		if ("a".equals(this.getFixture().getSDocument().getSDocumentGraph().getSStructures().get(0).getSName()))
			sStruct = this.getFixture().getSDocument().getSDocumentGraph().getSStructures().get(0);
		else
			this.getFixture().getSDocument().getSDocumentGraph().getSStructures().get(1);
		assertEquals(3, this.getFixture().getSDocument().getSDocumentGraph().getOutEdges(sStruct.getSId()).size());
		assertEquals("all nodes are: '" + this.getFixture().getSDocument().getSDocumentGraph().getSNodes() + "'", 6, this.getFixture().getSDocument().getSDocumentGraph().getSNodes().size());
	}

	/**
	 * This test is similar to {@link #testElementNodeMixedContent()}, but has a
	 * slightly different xml structure Checks that the following snippet is
	 * mapped to one root {@link SStructure} overlapping another
	 * {@link SStructure} object overlapping three {@link SToken} objects. The
	 * xml snippet:
	 * 
	 * <pre>
	 * &lt;a&gt;a &lt;b att="val"&gt;sample&lt;/b&gt;&lt;c/&gt;text &lt;/a&gt;
	 * </pre>
	 * 
	 * @throws XMLStreamException
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * 
	 */
	@Test
	public void testElementNodeMixedContent2() throws XMLStreamException, ParserConfigurationException, SAXException, IOException {
		xmlWriter.writeStartDocument();
		xmlWriter.writeStartElement("root");
		xmlWriter.writeStartElement("a");
		xmlWriter.writeCharacters("a");
		xmlWriter.writeStartElement("b");
		xmlWriter.writeAttribute("att", "val");
		xmlWriter.writeCharacters("sample");
		xmlWriter.writeEndElement();
		xmlWriter.writeStartElement("c");
		xmlWriter.writeEndElement();
		xmlWriter.writeCharacters("text");
		xmlWriter.writeEndElement();
		xmlWriter.writeEndElement();
		xmlWriter.writeEndDocument();
		xmlWriter.flush();

		String xml = outStream.toString();
		start(this.getFixture(), xml);

		assertEquals(3, this.getFixture().getSDocument().getSDocumentGraph().getSTokens().size());
		assertEquals(2, this.getFixture().getSDocument().getSDocumentGraph().getSStructures().size());
		SStructure sStruct = null;
		if ("a".equals(this.getFixture().getSDocument().getSDocumentGraph().getSStructures().get(0).getSName()))
			sStruct = this.getFixture().getSDocument().getSDocumentGraph().getSStructures().get(0);
		else
			this.getFixture().getSDocument().getSDocumentGraph().getSStructures().get(1);
		assertEquals(3, this.getFixture().getSDocument().getSDocumentGraph().getOutEdges(sStruct.getSId()).size());
		assertEquals("all nodes are: '" + this.getFixture().getSDocument().getSDocumentGraph().getSNodes() + "'", 6, this.getFixture().getSDocument().getSDocumentGraph().getSNodes().size());
	}

	/**
	 * Checks that xml-namespaces and use of namespace prefixes are ignored.
	 * 
	 * <pre>
	 *  &lt;document xmlns="some namespace" xmlns:ns="some namespace"&gt;
	 *     &lt;ns:a ns:att="val"/&gt;
	 *  &lt;/document&gt;
	 * </pre>
	 * 
	 * @throws XMLStreamException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	@Test
	public void testXMLNamespaces() throws XMLStreamException, ParserConfigurationException, SAXException, IOException {
		xmlWriter.writeStartDocument();
		xmlWriter.writeStartElement("document");
		xmlWriter.writeDefaultNamespace("someNamespace");
		xmlWriter.writeNamespace("ns", "someNamespace");
		xmlWriter.writeStartElement("ns", "a", "someNamespace");
		xmlWriter.writeAttribute("ns", "someNamespace", "att", "val");
		xmlWriter.writeCharacters("a sample text");
		xmlWriter.writeEndElement();
		xmlWriter.writeEndElement();
		xmlWriter.writeEndDocument();
		xmlWriter.flush();

		String xml = outStream.toString();
		start(this.getFixture(), xml);

		SNode a = null;
		SNode document = null;
		for (SNode sNode : this.getFixture().getSDocument().getSDocumentGraph().getSNodes()) {
			if ("ns:a".equals(sNode.getSName()))
				a = sNode;
			else if ("document".equals(sNode.getSName()))
				document = sNode;
		}

		assertNotNull(a);
		assertNotNull(document);
		assertEquals(0, document.getSAnnotations().size());
		assertEquals("all annos: " + a.getSAnnotations(), 1, a.getSAnnotations().size());
		assertEquals("att", a.getSAnnotations().get(0).getSName());
		assertEquals("val", a.getSAnnotations().get(0).getSValue());
	}

	/**
	 * Tests if when the property
	 * {@link GenericXMLImporterProperties#PROP_PREFIXED_ANNOS} is set, the
	 * {@link SAnnotation} sName is correct. The xml snippet: <br/>
	 * &lt;a attA1="valA1" attA2="valA2"&gt;a sample&lt;/a&gt; <br/>
	 * Shall be mapped to: {@link SToken} object having one {@link SAnnotation}
	 * having the name a_attB1 and one {@link SAnnotation} having the name
	 * attA2.
	 * 
	 * @throws XMLStreamException
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * 
	 */
	@Test
	public void testProp_prefixSAnnotation() throws XMLStreamException, ParserConfigurationException, SAXException, IOException {
		String text1 = "here";
		String element = "a";
		String attA1 = "attA1";
		String valA1 = "valA1";
		String attA2 = "attA2";
		String valA2 = "valA2";

		PepperModuleProperty<String> prop = (PepperModuleProperty<String>) this.getFixture().getProperties().getProperty(GenericXMLImporterProperties.PROP_PREFIXED_ANNOS);
		assertNotNull(prop);
		prop.setValue("//@" + attA1);

		xmlWriter.writeStartDocument();
		xmlWriter.writeStartElement(element);
		xmlWriter.writeAttribute(attA1, valA1);
		xmlWriter.writeAttribute(attA2, valA2);
		xmlWriter.writeCharacters(text1);
		xmlWriter.writeEndElement();
		xmlWriter.writeEndDocument();
		xmlWriter.flush();

		String xml = outStream.toString();
		start(this.getFixture(), xml);

		assertEquals(1, this.getFixture().getSDocument().getSDocumentGraph().getSTokens().size());
		assertNotNull(this.getFixture().getSDocument().getSDocumentGraph().getSTokens().get(0).getSAnnotations());
		assertEquals(2, this.getFixture().getSDocument().getSDocumentGraph().getSTokens().get(0).getSAnnotations().size());
		assertEquals(element + "_" + attA1, this.getFixture().getSDocument().getSDocumentGraph().getSTokens().get(0).getSAnnotations().get(0).getSName());
		assertEquals(attA2, this.getFixture().getSDocument().getSDocumentGraph().getSTokens().get(0).getSAnnotations().get(1).getSName());
	}

	/**
	 * Tests if when the property
	 * {@link GenericXMLImporterProperties#PROP_PREFIXED_ANNOS} is set, the
	 * {@link SAnnotation} sName is correct. The xml snippet: <br/>
	 * &lt;a attA1="valA1" attA2="valA2"&gt;a sample&lt;/a&gt; <br/>
	 * Shall be mapped to: {@link SToken} object having one {@link SAnnotation}
	 * having the name a_attB1 and one {@link SAnnotation} having the name
	 * attA2.
	 * 
	 * @throws XMLStreamException
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * 
	 */
	@Test
	public void testProp_sMetaAnnotation() throws XMLStreamException, ParserConfigurationException, SAXException, IOException {
		String text1 = "here";
		String element = "a";
		String attA1 = "attA1";
		String valA1 = "valA1";
		String attA2 = "attA2";
		String valA2 = "valA2";

		PepperModuleProperty<String> prop = (PepperModuleProperty<String>) this.getFixture().getProperties().getProperty(GenericXMLImporterProperties.PROP_SMETA_ANNOTATION);
		assertNotNull(prop);
		prop.setValue("//@" + attA1);

		xmlWriter.writeStartDocument();
		xmlWriter.writeStartElement(element);
		xmlWriter.writeAttribute(attA1, valA1);
		xmlWriter.writeAttribute(attA2, valA2);
		xmlWriter.writeCharacters(text1);
		xmlWriter.writeEndElement();
		xmlWriter.writeEndDocument();
		xmlWriter.flush();

		String xml = outStream.toString();
		start(this.getFixture(), xml);

		assertEquals(1, this.getFixture().getSDocument().getSDocumentGraph().getSTokens().size());
		assertNotNull(this.getFixture().getSDocument().getSDocumentGraph().getSTokens().get(0).getSAnnotations());
		assertNotNull(this.getFixture().getSDocument().getSDocumentGraph().getSTokens().get(0).getSMetaAnnotations());
		assertEquals(1, this.getFixture().getSDocument().getSDocumentGraph().getSTokens().get(0).getSAnnotations().size());
		assertEquals(1, this.getFixture().getSDocument().getSDocumentGraph().getSTokens().get(0).getSMetaAnnotations().size());
		assertEquals(attA1, this.getFixture().getSDocument().getSDocumentGraph().getSTokens().get(0).getSMetaAnnotations().get(0).getSName());
		assertEquals(attA2, this.getFixture().getSDocument().getSDocumentGraph().getSTokens().get(0).getSAnnotations().get(0).getSName());
	}

	/**
	 * Checks if spans are correctly created in hierarchies when using the prop
	 * genericXml.importer.asSSpan Create a test for snippet: <br/>
	 * 
	 * <pre>
	 * 	&lt;document author="John Doe"&gt;
	 *    &lt;struct const="S"&gt;
	 *        &lt;tok&gt;a sample text&lt;/tok&gt;
	 *    &lt;/struct&gt;
	 * &lt;/document&gt;
	 * </pre>
	 * 
	 * <br/>
	 * {@value GenericXMLImporterProperties#PROP_SMETA_ANNOTATION_SDOCUMENT}=
	 * //document shall result in a {@link SStructure} for &lt;struct&gt; and a
	 * {@link SToken} for &lt;tok&gt. &lt;document&gt; shall be ignored, but its
	 * attributes shall be mapped to {@link SMetaAnnotation} of
	 * {@link SDocument}.
	 * 
	 * @throws XMLStreamException
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * 
	 */
	@Test
	public void testProp_sMetaAnnotation2() throws XMLStreamException, ParserConfigurationException, SAXException, IOException {
		String text = "a sample text";
		String elementDocument = "document";
		String elementStuct = "struct";
		String elementTok = "tok";

		PepperModuleProperty<String> prop = (PepperModuleProperty<String>) this.getFixture().getProperties().getProperty(GenericXMLImporterProperties.PROP_SMETA_ANNOTATION_SDOCUMENT);
		assertNotNull(prop);
		prop.setValue("//" + elementDocument);

		xmlWriter.writeStartDocument();
		xmlWriter.writeStartElement(elementDocument);
		xmlWriter.writeAttribute("author", "John Doe");
		xmlWriter.writeStartElement(elementStuct);
		xmlWriter.writeAttribute("const", "S");
		xmlWriter.writeStartElement(elementTok);
		xmlWriter.writeCharacters(text);
		xmlWriter.writeEndElement();
		xmlWriter.writeEndElement();
		xmlWriter.writeEndElement();
		xmlWriter.writeEndDocument();
		xmlWriter.flush();

		String xml = outStream.toString();
		start(this.getFixture(), xml);

		assertNotNull(this.getFixture().getSDocument().getSDocumentGraph().getSDocument());
		assertNotNull(this.getFixture().getSDocument().getSDocumentGraph().getSDocument().getSMetaAnnotations());
		assertEquals(1, this.getFixture().getSDocument().getSDocumentGraph().getSDocument().getSMetaAnnotations().size());
		assertEquals("John Doe", this.getFixture().getSDocument().getSDocumentGraph().getSDocument().getSMetaAnnotation("author").getSValue());

		assertEquals(1, this.getFixture().getSDocument().getSDocumentGraph().getSStructures().size());
		assertEquals(1, this.getFixture().getSDocument().getSDocumentGraph().getSStructures().get(0).getSAnnotations().size());
		assertEquals("S", this.getFixture().getSDocument().getSDocumentGraph().getSStructures().get(0).getSAnnotation("const").getSValue());
	}

	/**
	 * Checks if spans are correctly created in hierarchies when using the prop
	 * genericXml.importer.asSSpan Create a test for snippet: <br/>
	 * &lt;a&gt;&lt;b&gt;&lt;c&gt;a&lt;/c&gt;&lt;c&gt;sample&lt;/c&gt;&lt;/b&gt;
	 * &lt;/a&gt; <br/>
	 * {@value GenericXMLImporterProperties#PROP_AS_SPANS}= //b shall result in
	 * a {@link SSpan} for &lt;b&gt; and a {@link SStructure} for &lt;a&gt;,
	 * both containing the two {@link SToken} objects overlapping "a" and
	 * "sample"
	 * 
	 * @throws XMLStreamException
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * 
	 */
	@Test
	public void testProp_asSpan() throws XMLStreamException, ParserConfigurationException, SAXException, IOException {
		String text1 = "a";
		String text2 = "sample";
		String elementA = "a";
		String elementB = "b";
		String elementC = "c";

		PepperModuleProperty<String> prop = (PepperModuleProperty<String>) this.getFixture().getProperties().getProperty(GenericXMLImporterProperties.PROP_AS_SPANS);
		assertNotNull(prop);
		prop.setValue("//" + elementB);

		xmlWriter.writeStartDocument();
		xmlWriter.writeStartElement(elementA);
		xmlWriter.writeStartElement(elementB);
		xmlWriter.writeStartElement(elementC);
		xmlWriter.writeCharacters(text1);
		xmlWriter.writeEndElement();
		xmlWriter.writeStartElement(elementC);
		xmlWriter.writeCharacters(text2);
		xmlWriter.writeEndElement();
		xmlWriter.writeEndElement();
		xmlWriter.writeEndElement();
		xmlWriter.writeEndDocument();
		xmlWriter.flush();

		String xml = outStream.toString();
		start(this.getFixture(), xml);

		assertEquals(2, this.getFixture().getSDocument().getSDocumentGraph().getSTokens().size());
		assertEquals(1, this.getFixture().getSDocument().getSDocumentGraph().getSSpans().size());
		assertEquals(1, this.getFixture().getSDocument().getSDocumentGraph().getSStructures().size());
	}

	/**
	 * Tests the same as {@link #testElementNodeWithComplexContent()} but with
	 * setted flag {@link GenericXMLImporterProperties#PROP_ARTIFICIAL_SSTRUCT}.
	 * The xml snippet: <br/>
	 * &lt;a&gt;here&lt;b&gt;comes&lt;/b&gt;text&lt;/a&gt; <br/>
	 * shall be mapped to:
	 * 
	 * <table border="1">
	 * <tr>
	 * <td colspan="3">struct1</td>
	 * <tr>
	 * <tr>
	 * <td/>
	 * <td>struct2</td>
	 * <td/>
	 * <tr>
	 * <tr>
	 * <td>struct3</td>
	 * <td>struct4</td>
	 * <td>struct5</td>
	 * <tr>
	 * <tr>
	 * <td>tok1</td>
	 * <td>tok2</td>
	 * <td>tok3</td>
	 * <tr>
	 * <tr>
	 * <td>here</td>
	 * <td>comes</td>
	 * <td>text</td>
	 * <tr>
	 * </table>
	 * 
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws XMLStreamException
	 */
	@Test
	public void testProp_ArtificialStruct() throws ParserConfigurationException, SAXException, IOException, XMLStreamException {
		String text1 = "here";
		String text2 = "comes";
		String text3 = "text";

		PepperModuleProperty<Boolean> prop = (PepperModuleProperty<Boolean>) this.getFixture().getProperties().getProperty(GenericXMLImporterProperties.PROP_ARTIFICIAL_SSTRUCT);
		assertNotNull(prop);
		prop.setValue(true);

		xmlWriter.writeStartDocument();
		xmlWriter.writeStartElement("a");
		xmlWriter.writeCharacters(text1);
		xmlWriter.writeStartElement("b");
		xmlWriter.writeCharacters(text2);
		xmlWriter.writeEndElement();
		xmlWriter.writeCharacters(text3);
		xmlWriter.writeEndElement();
		xmlWriter.writeEndDocument();
		xmlWriter.flush();

		String xml = outStream.toString();
		start(this.getFixture(), xml);

		assertEquals(3, this.getFixture().getSDocument().getSDocumentGraph().getSTokens().size());
		assertEquals(4, this.getFixture().getSDocument().getSDocumentGraph().getSStructures().size());
	}

	/**
	 * Tests the same as {@link #testElementNodeWithComplexContent()} but with
	 * setted flag {@link GenericXMLImporterProperties#PROP_ARTIFICIAL_SSTRUCT}.
	 * The xml snippet: <br/>
	 * &lt;a&gt;here&lt;b&gt;&lt;c&gt;comes&lt;/c&gt;&lt;/b&gt;text&lt;/a&gt; <br/>
	 * shall be mapped to:
	 * 
	 * <table border="1">
	 * <tr>
	 * <td colspan="3">struct (a)</td>
	 * <tr>
	 * <tr>
	 * <td/>
	 * <td>b</td>
	 * <td/>
	 * <tr>
	 * <tr>
	 * <td/>
	 * <td>c</td>
	 * <td/>
	 * <tr>
	 * <tr>
	 * <td>tok1</td>
	 * <td>tok2</td>
	 * <td>tok3</td>
	 * <tr>
	 * <tr>
	 * <td>here</td>
	 * <td>comes</td>
	 * <td>text</td>
	 * <tr>
	 * </table>
	 * 
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws XMLStreamException
	 */
	@Test
	public void testProp_ArtificialStruct2() throws ParserConfigurationException, SAXException, IOException, XMLStreamException {
		String text1 = "here";
		String text2 = "comes";
		String text3 = "text";

		PepperModuleProperty<Boolean> prop = (PepperModuleProperty<Boolean>) this.getFixture().getProperties().getProperty(GenericXMLImporterProperties.PROP_ARTIFICIAL_SSTRUCT);
		assertNotNull(prop);
		prop.setValue(true);

		xmlWriter.writeStartDocument();
		xmlWriter.writeStartElement("a");
		xmlWriter.writeCharacters(text1);
		xmlWriter.writeStartElement("b");
		xmlWriter.writeStartElement("c");
		xmlWriter.writeCharacters(text2);
		xmlWriter.writeEndElement();
		xmlWriter.writeEndElement();
		xmlWriter.writeCharacters(text3);
		xmlWriter.writeEndElement();
		xmlWriter.writeEndDocument();
		xmlWriter.flush();

		String xml = outStream.toString();
		start(this.getFixture(), xml);

		assertEquals(3, this.getFixture().getSDocument().getSDocumentGraph().getSTokens().size());
		assertEquals(5, this.getFixture().getSDocument().getSDocumentGraph().getSStructures().size());
	}

	/**
	 * Checks if spans are correctly created in hierarchies when using the prop
	 * genericXml.importer.asSSpan Create a test for snippet: <br/>
	 * &lt;a&gt;&lt;b&gt;&lt;c&gt;a&lt;/c&gt;&lt;c&gt;sample&lt;/c&gt;&lt;/b&gt;
	 * &lt;/a&gt; <br/>
	 * {@value GenericXMLImporterProperties#PROP_AS_SPANS}= //b shall result in
	 * a {@link SSpan} for &lt;b&gt; and a {@link SStructure} for &lt;a&gt;,
	 * both containing the two {@link SToken} objects overlapping "a" and
	 * "sample"
	 * 
	 * @throws XMLStreamException
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * 
	 */
	@Test
	public void testProp_textOnly() throws XMLStreamException, ParserConfigurationException, SAXException, IOException {
		String text1 = "a";
		String text2 = "sample";
		String elementA = "a";
		String elementB = "b";
		String elementC = "c";

		PepperModuleProperty<Boolean> prop = (PepperModuleProperty<Boolean>) this.getFixture().getProperties().getProperty(GenericXMLImporterProperties.PROP_TEXT_ONLY);
		assertNotNull(prop);
		prop.setValue(true);

		xmlWriter.writeStartDocument();
		xmlWriter.writeStartElement(elementA);
		xmlWriter.writeStartElement(elementB);
		xmlWriter.writeStartElement(elementC);
		xmlWriter.writeCharacters(text1);
		xmlWriter.writeEndElement();
		xmlWriter.writeStartElement(elementC);
		xmlWriter.writeCharacters(text2);
		xmlWriter.writeEndElement();
		xmlWriter.writeEndElement();
		xmlWriter.writeEndElement();
		xmlWriter.writeEndDocument();
		xmlWriter.flush();

		String xml = outStream.toString();
		start(this.getFixture(), xml);
		assertEquals(1, this.getFixture().getSDocument().getSDocumentGraph().getSTextualDSs().size());
		assertNotNull(this.getFixture().getSDocument().getSDocumentGraph().getSTextualDSs().get(0));
		assertEquals(text1 +" "+ text2, this.getFixture().getSDocument().getSDocumentGraph().getSTextualDSs().get(0).getSText());
	}

	/**
	 * Tests the use of property
	 * {@link GenericXMLImporterProperties#PROP_SMETA_ANNOTATION_SDOCUMENT}. <br/>
	 * &lt;a&gt;&lt;meta att1="val1"&gt;&lt;anothermeta
	 * att2="val1"/&gt;&lt;/meta
	 * &gt;&lt;b&gt;&lt;c&gt;a&lt;/c&gt;&lt;c&gt;sample&
	 * lt;/c&gt;&lt;/b&gt;&lt;/a&gt; <br/>
	 * {@value GenericXMLImporterProperties#PROP_SMETA_ANNOTATION_SDOCUMENT}=
	 * //meta// shall result in a {@link SSpan} for &lt;b&gt; and a
	 * {@link SStructure} for &lt;a&gt;, both containing the two {@link SToken}
	 * objects overlapping "a" and "sample"
	 * 
	 * @throws XMLStreamException
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * 
	 */
	@Test
	public void testProp_sMetaAnnotationSDocument() throws XMLStreamException, ParserConfigurationException, SAXException, IOException {
		String text1 = "a";
		String text2 = "sample";
		String elementA = "a";
		String elementB = "b";
		String elementC = "c";
		String elementMeta1 = "meta";
		String elementMeta2 = "anotherMeta";
		String att1 = "att1";
		String att2 = "att2";

		PepperModuleProperty<String> prop = (PepperModuleProperty<String>) this.getFixture().getProperties().getProperty(GenericXMLImporterProperties.PROP_SMETA_ANNOTATION_SDOCUMENT);
		assertNotNull(prop);
		prop.setValue("//" + elementMeta1 + "//, //" + elementMeta1);

		xmlWriter.writeStartDocument();
		xmlWriter.writeStartElement(elementA);
		xmlWriter.writeStartElement(elementMeta1);
		xmlWriter.writeAttribute(att1, "val1");
		xmlWriter.writeStartElement(elementMeta2);
		xmlWriter.writeAttribute(att2, "val2");
		xmlWriter.writeEndElement();
		xmlWriter.writeEndElement();
		xmlWriter.writeStartElement(elementB);
		xmlWriter.writeStartElement(elementC);
		xmlWriter.writeCharacters(text1);
		xmlWriter.writeEndElement();
		xmlWriter.writeStartElement(elementC);
		xmlWriter.writeCharacters(text2);
		xmlWriter.writeEndElement();
		xmlWriter.writeEndElement();
		xmlWriter.writeEndElement();
		xmlWriter.writeEndDocument();
		xmlWriter.flush();

		SDocument sDocument = SaltFactory.eINSTANCE.createSDocument();
		this.getFixture().setSDocument(sDocument);
		String xml = outStream.toString();
		start(this.getFixture(), xml);

		assertEquals(2, sDocument.getSMetaAnnotations().size());
		assertNotNull(sDocument.getSMetaAnnotation(att1));
		assertEquals("val1", sDocument.getSMetaAnnotation(att1).getSValue());
		assertNotNull(sDocument.getSMetaAnnotation(att2));
		assertEquals("val2", sDocument.getSMetaAnnotation(att2).getSValue());
		assertEquals(1, this.getFixture().getSDocument().getSDocumentGraph().getSTextualDSs().size());
		assertNotNull(this.getFixture().getSDocument().getSDocumentGraph().getSTextualDSs().get(0));
		assertEquals(text1 +" "+ text2, this.getFixture().getSDocument().getSDocumentGraph().getSTextualDSs().get(0).getSText());
	}

	/**
	 * Tests map an attribute node to {@link SMetaAnnotation} of
	 * {@link SDocument}. <br/>
	 * &lt;a&gt;&lt;b att1="val1"
	 * att2="val2"&gt;&lt;c&gt;text&lt;/c&gt;&lt;/b&gt;&lt;a&gt; <br/>
	 * {@value GenericXMLImporterProperties#PROP_SMETA_ANNOTATION_SDOCUMENT}=
	 * //meta// shall result in a {@link SSpan} for &lt;b&gt; and a
	 * {@link SStructure} for &lt;a&gt;, both containing the two {@link SToken}
	 * objects overlapping "a" and "sample"
	 * 
	 * @throws XMLStreamException
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * 
	 */
	@Test
	public void testProp_sMetaAnnotationSDocument_Attribute() throws XMLStreamException, ParserConfigurationException, SAXException, IOException {
		PepperModuleProperty<String> prop = (PepperModuleProperty<String>) this.getFixture().getProperties().getProperty(GenericXMLImporterProperties.PROP_SMETA_ANNOTATION_SDOCUMENT);
		assertNotNull(prop);
		prop.setValue("//b/@att1");

		xmlWriter.writeStartDocument();
		xmlWriter.writeStartElement("a");
		xmlWriter.writeStartElement("b");
		xmlWriter.writeAttribute("att1", "val1");
		xmlWriter.writeAttribute("att2", "val2");
		xmlWriter.writeStartElement("c");
		xmlWriter.writeCharacters("text");
		xmlWriter.writeEndElement();
		xmlWriter.writeEndElement();
		xmlWriter.writeEndElement();
		xmlWriter.writeEndDocument();
		xmlWriter.flush();

		SDocument sDocument = SaltFactory.eINSTANCE.createSDocument();
		this.getFixture().setSDocument(sDocument);
		String xml = outStream.toString();
		start(this.getFixture(), xml);

		SNode a = null;
		SNode b = null;
		SNode c = null;
		for (SNode sNode : this.getFixture().getSDocument().getSDocumentGraph().getSNodes()) {
			if ("a".equals(sNode.getSName()))
				a = sNode;
			else if ("b".equals(sNode.getSName()))
				b = sNode;
			else if ("c".equals(sNode.getSName()))
				c = sNode;
		}
		assertNotNull(a);
		assertNotNull(b);
		assertNotNull(c);
		assertEquals(0, a.getSAnnotations().size());
		assertEquals(1, b.getSAnnotations().size());
		assertEquals(0, c.getSAnnotations().size());
	}

	/**
	 * Checks an element-node is read as {@link SLayer} object.
	 * 
	 * <pre>
	 * <code>
	 * &lt;a&gt;
	 *  &lt;layer&gt;
	 *    &lt;b&gt;
	 *      &lt;c&gt;a&lt;/c&gt;
	 *      &lt;c&gt;sample&lt;/c&gt;
	 *    &lt;/b&gt;
	 *  &lt;/layer&gt;
	 * &lt;/a&gt;
	 * </pre>
	 * 
	 * </pre> {@value GenericXMLImporterProperties#PROP_slay}= //b shall result
	 * in a {@link SSpan} for &lt;b&gt; and a {@link SStructure} for &lt;a&gt;,
	 * both containing the two {@link SToken} objects overlapping "a" and
	 * "sample"
	 * 
	 * @throws XMLStreamException
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * 
	 */
	@Test
	public void testProp_sLayer() throws XMLStreamException, ParserConfigurationException, SAXException, IOException {
		String text1 = "a";
		String text2 = "sample";
		String elementA = "a";
		String elementB = "b";
		String elementC = "c";
		String elementLayer = "layer";
		String layerAtt1 = "layerAtt1";

		PepperModuleProperty<String> prop = (PepperModuleProperty<String>) this.getFixture().getProperties().getProperty(GenericXMLImporterProperties.PROP_SLAYER);
		assertNotNull(prop);
		prop.setValue("//" + elementLayer);

		xmlWriter.writeStartDocument();
		xmlWriter.writeStartElement(elementA);
		xmlWriter.writeStartElement(elementLayer);
		xmlWriter.writeAttribute(layerAtt1, "val1");
		xmlWriter.writeStartElement(elementB);
		xmlWriter.writeStartElement(elementC);
		xmlWriter.writeCharacters(text1);
		xmlWriter.writeEndElement();
		xmlWriter.writeStartElement(elementC);
		xmlWriter.writeCharacters(text2);
		xmlWriter.writeEndElement();
		xmlWriter.writeEndElement();
		xmlWriter.writeEndElement();
		xmlWriter.writeEndElement();
		xmlWriter.writeEndDocument();
		xmlWriter.flush();

		String xml = outStream.toString();
		start(this.getFixture(), xml);

		assertEquals(1, this.getFixture().getSDocument().getSDocumentGraph().getSLayers().size());
		assertNotNull(this.getFixture().getSDocument().getSDocumentGraph().getSLayers().get(0));
		assertEquals(1, this.getFixture().getSDocument().getSDocumentGraph().getSLayers().get(0).getSMetaAnnotations().size());
		assertNotNull(this.getFixture().getSDocument().getSDocumentGraph().getSLayers().get(0).getSMetaAnnotation(layerAtt1));
		assertEquals(2, this.getFixture().getSDocument().getSDocumentGraph().getSTokens().size());
		assertEquals(2, this.getFixture().getSDocument().getSDocumentGraph().getSStructures().size());
		assertEquals(5, this.getFixture().getSDocument().getSDocumentGraph().getSNodes().size());

		assertEquals(3, this.getFixture().getSDocument().getSDocumentGraph().getSLayers().get(0).getSNodes().size());
		assertEquals(2, this.getFixture().getSDocument().getSDocumentGraph().getSLayers().get(0).getSRelations().size());
	}

	/**
	 * Checks two element-nodes are read as {@link SLayer} object. Both
	 * element-nodes shall be mapped to the same {@link SLayer} object.
	 * 
	 * <pre>
	 * <code>
	 * &lt;root&gt;
	 *   &lt;a&gt;
	 *    &lt;layer&gt;
	 *      &lt;b&gt;
	 *        &lt;c&gt;a&lt;/c&gt;
	 *        &lt;c&gt;sample&lt;/c&gt;
	 *      &lt;/b&gt;
	 *    &lt;/layer&gt;
	 *   &lt;/a&gt;
	 *   &lt;a&gt;
	 *     &lt;layer&gt;
	 *       &lt;c&gt;text&lt;/c&gt;
	 *     &lt;/layer&gt;   
	 *   &lt;/a&gt;
	 * &lt;/root&gt;
	 * </pre>
	 * 
	 * </pre>
	 * 
	 * {@value GenericXMLImporterProperties#PROP_slay}= //b shall result in a
	 * {@link SSpan} for &lt;b&gt; and a {@link SStructure} for &lt;a&gt;, both
	 * containing the two {@link SToken} objects overlapping "a" and "sample"
	 * 
	 * @throws XMLStreamException
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * 
	 */
	@Test
	public void testProp_sLayer2() throws XMLStreamException, ParserConfigurationException, SAXException, IOException {
		String text1 = "a";
		String text2 = "sample";
		String text3 = "text";
		String elementRoot = "root";
		String elementA = "a";
		String elementB = "b";
		String elementC = "c";
		String elementLayer = "layer";
		String layerAtt1 = "layerAtt1";

		PepperModuleProperty<String> prop = (PepperModuleProperty<String>) this.getFixture().getProperties().getProperty(GenericXMLImporterProperties.PROP_SLAYER);
		assertNotNull(prop);
		prop.setValue("//" + elementLayer);

		xmlWriter.writeStartDocument();
		xmlWriter.writeStartElement(elementRoot);
		xmlWriter.writeStartElement(elementA);
		xmlWriter.writeStartElement(elementLayer);
		xmlWriter.writeAttribute(layerAtt1, "val1");
		xmlWriter.writeStartElement(elementB);
		xmlWriter.writeStartElement(elementC);
		xmlWriter.writeCharacters(text1);
		xmlWriter.writeEndElement();
		xmlWriter.writeStartElement(elementC);
		xmlWriter.writeCharacters(text2);
		xmlWriter.writeEndElement();
		xmlWriter.writeEndElement();
		xmlWriter.writeEndElement();
		xmlWriter.writeEndElement();

		xmlWriter.writeStartElement(elementA);
		xmlWriter.writeStartElement(elementLayer);
		xmlWriter.writeStartElement(elementC);
		xmlWriter.writeCharacters(text3);
		xmlWriter.writeEndElement();
		xmlWriter.writeEndElement();
		xmlWriter.writeEndElement();

		xmlWriter.writeEndElement();
		xmlWriter.writeEndDocument();
		xmlWriter.flush();

		String xml = outStream.toString();
		start(this.getFixture(), xml);

		assertEquals(1, this.getFixture().getSDocument().getSDocumentGraph().getSLayers().size());
		assertNotNull(this.getFixture().getSDocument().getSDocumentGraph().getSLayers().get(0));
		assertEquals(1, this.getFixture().getSDocument().getSDocumentGraph().getSLayers().get(0).getSMetaAnnotations().size());
		assertNotNull(this.getFixture().getSDocument().getSDocumentGraph().getSLayers().get(0).getSMetaAnnotation(layerAtt1));
		assertEquals(3, this.getFixture().getSDocument().getSDocumentGraph().getSTokens().size());
		assertEquals(4, this.getFixture().getSDocument().getSDocumentGraph().getSStructures().size());
		assertEquals("nodes: " + this.getFixture().getSDocument().getSDocumentGraph().getSNodes(), 8, this.getFixture().getSDocument().getSDocumentGraph().getSNodes().size());

		assertEquals("nodes: " + this.getFixture().getSDocument().getSDocumentGraph().getSLayers().get(0).getSNodes(), 4, this.getFixture().getSDocument().getSDocumentGraph().getSLayers().get(0).getSNodes().size());
		assertEquals("relations: " + this.getFixture().getSDocument().getSDocumentGraph().getSLayers().get(0).getSRelations(), 2, this.getFixture().getSDocument().getSDocumentGraph().getSLayers().get(0).getSRelations().size());
	}

	/**
	 * Tests if for the given xml fragment artificial {@link SAnnotation}
	 * objects containing the name of the element-node as sname and svalue are
	 * created. <br/>
	 * 
	 * <pre>
	 * &lt;a&gt;here&lt;b&gt;&lt;c att="val"&gt;&lt;d&gt;comes&lt;/d&gt;&lt;/c&gt;&lt;/b&gt;text&lt;/a&gt;
	 * </pre>
	 * 
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws XMLStreamException
	 */
	@Test
	public void testProp_PrefixElementNameAsSAnno() throws ParserConfigurationException, SAXException, IOException, XMLStreamException {
		String text1 = "here";
		String text2 = "comes";
		String text3 = "text";

		PepperModuleProperty<String> prop = (PepperModuleProperty<String>) this.getFixture().getProperties().getProperty(GenericXMLImporterProperties.PROP_ELEMENTNAME_AS_SANNO);
		assertNotNull(prop);
		prop.setValue("//b, //b//");

		xmlWriter.writeStartDocument();
		xmlWriter.writeStartElement("a");
		xmlWriter.writeCharacters(text1);
		xmlWriter.writeStartElement("b");
		xmlWriter.writeStartElement("c");
		xmlWriter.writeAttribute("att", "val");
		xmlWriter.writeStartElement("d");
		xmlWriter.writeCharacters(text2);
		xmlWriter.writeEndElement();
		xmlWriter.writeEndElement();
		xmlWriter.writeEndElement();
		xmlWriter.writeCharacters(text3);
		xmlWriter.writeEndElement();
		xmlWriter.writeEndDocument();
		xmlWriter.flush();

		String xml = outStream.toString();
		start(this.getFixture(), xml);

		assertEquals(3, this.getFixture().getSDocument().getSDocumentGraph().getSTokens().size());
		assertEquals(3, this.getFixture().getSDocument().getSDocumentGraph().getSStructures().size());
		SNode b = null;
		SNode c = null;
		SNode d = null;
		for (SNode sNode : this.getFixture().getSDocument().getSDocumentGraph().getSNodes()) {
			if ("b".equals(sNode.getSName()))
				b = sNode;
			else if ("c".equals(sNode.getSName()))
				c = sNode;
			else if ("d".equals(sNode.getSName()))
				d = sNode;
		}
		assertNotNull(b);
		assertNotNull(c);
		assertEquals(1, b.getSAnnotations().size());
		assertEquals("b", b.getSAnnotation("b").getSValue());
		assertEquals(2, c.getSAnnotations().size());
		assertEquals("c", c.getSAnnotation("c").getSValue());
		assertEquals(1, d.getSAnnotations().size());
		assertEquals("d", d.getSAnnotation("d").getSValue());
	}

	/**
	 * Tests the hierarchie structure created in {@link #createHierarchy()} and
	 * compares it to {@link SampleGenerator#createSyntaxStructure(SDocument)} and
	 * {@link SampleGenerator#createSyntaxAnnotations(SDocument)}.
	 * 
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws XMLStreamException
	 */
	@Test
	public void testHierarchies() throws XMLStreamException, ParserConfigurationException, SAXException, IOException {
		PepperModuleProperty<String> prop2 = (PepperModuleProperty<String>) this.getFixture().getProperties().getProperty(GenericXMLImporterProperties.PROP_SMETA_ANNOTATION_SDOCUMENT);
		assertNotNull(prop2);
		prop2.setValue("//document");

		createHierarchy(xmlWriter);
		String xmlDocument = outStream.toString();

		this.start(this.getFixture(), xmlDocument);
		SDocument template = SaltFactory.eINSTANCE.createSDocument();
		SampleGenerator.createSyntaxStructure(template);
		SampleGenerator.createSyntaxAnnotations(template);

		assertNotNull(template);
		// TODO: just some tests to check if numbers of elements are equal, this
		// test is a simplification until an isomorphy tests exists for graphs
		assertEquals(template.getSDocumentGraph().getSNodes().size(), this.getFixture().getSDocument().getSDocumentGraph().getSNodes().size());
		assertEquals(template.getSDocumentGraph().getSRelations().size(), this.getFixture().getSDocument().getSDocumentGraph().getSRelations().size());
		assertEquals(template.getSDocumentGraph().getSTextualDSs().size(), this.getFixture().getSDocument().getSDocumentGraph().getSTextualDSs().size());
		assertEquals(template.getSDocumentGraph().getSTokens().size(), this.getFixture().getSDocument().getSDocumentGraph().getSTokens().size());
		assertEquals(template.getSDocumentGraph().getSStructures().size(), this.getFixture().getSDocument().getSDocumentGraph().getSStructures().size());
		assertEquals(template.getSDocumentGraph().getSSpans().size(), this.getFixture().getSDocument().getSDocumentGraph().getSSpans().size());
		assertEquals(template.getSDocumentGraph().getSTextualRelations().size(), this.getFixture().getSDocument().getSDocumentGraph().getSTextualRelations().size());
		assertEquals(template.getSDocumentGraph().getSDominanceRelations().size(), this.getFixture().getSDocument().getSDocumentGraph().getSDominanceRelations().size());
		assertEquals(template.getSDocumentGraph().getSSpanningRelations().size(), this.getFixture().getSDocument().getSDocumentGraph().getSSpanningRelations().size());
	}

	/**
	 * Tests the span structure created in {@link #createSpan()} and compares it
	 * to {@link SampleGenerator#createInformationStructureSpan(SDocument)} and
	 * {@link SampleGenerator#createInformationStructureAnnotations(SDocument)}
	 * 
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws XMLStreamException
	 */
	@Test
	public void testSpans() throws XMLStreamException, ParserConfigurationException, SAXException, IOException {
		PepperModuleProperty<String> prop1 = (PepperModuleProperty<String>) this.getFixture().getProperties().getProperty(GenericXMLImporterProperties.PROP_AS_SPANS);
		assertNotNull(prop1);
		prop1.setValue("//sSpan1, //sSpan2");
		PepperModuleProperty<String> prop2 = (PepperModuleProperty<String>) this.getFixture().getProperties().getProperty(GenericXMLImporterProperties.PROP_SMETA_ANNOTATION_SDOCUMENT);
		assertNotNull(prop2);
		prop2.setValue("//document");
		PepperModuleProperty<String> prop3 = (PepperModuleProperty<String>) this.getFixture().getProperties().getProperty(GenericXMLImporterProperties.PROP_SLAYER);
		assertNotNull(prop3);
		prop3.setValue("//" + SampleGenerator.MORPHOLOGY_LAYER);

		createSpan(xmlWriter);
		String xmlDocument = outStream.toString();

		this.start(this.getFixture(), xmlDocument);
		// this.getFixture().setSDocument(SaltFactory.eINSTANCE.createSDocument());
		// this.getFixture().getSDocument().createSMetaAnnotation(null,
		// "author", "John Doe");

		SDocument template = SaltFactory.eINSTANCE.createSDocument();
		SampleGenerator.createInformationStructureSpan(template);
		SampleGenerator.createInformationStructureAnnotations(template);

		assertNotNull(template);

		// TODO: just some tests to check if numbers of elements are equal, this
		// test is a simplifictaion until an isomorphy tests exists for graphs
		assertEquals(template.getSDocumentGraph().getSNodes().size(), this.getFixture().getSDocument().getSDocumentGraph().getSNodes().size());
		assertEquals(template.getSDocumentGraph().getSRelations().size(), this.getFixture().getSDocument().getSDocumentGraph().getSRelations().size());
		assertEquals(template.getSDocumentGraph().getSTextualDSs().size(), this.getFixture().getSDocument().getSDocumentGraph().getSTextualDSs().size());
		assertEquals(template.getSDocumentGraph().getSTokens().size(), this.getFixture().getSDocument().getSDocumentGraph().getSTokens().size());
		assertEquals(template.getSDocumentGraph().getSStructures().size(), this.getFixture().getSDocument().getSDocumentGraph().getSStructures().size());
		assertEquals(template.getSDocumentGraph().getSSpans().size(), this.getFixture().getSDocument().getSDocumentGraph().getSSpans().size());
		assertEquals(template.getSDocumentGraph().getSTextualRelations().size(), this.getFixture().getSDocument().getSDocumentGraph().getSTextualRelations().size());
		assertEquals(template.getSDocumentGraph().getSDominanceRelations().size(), this.getFixture().getSDocument().getSDocumentGraph().getSDominanceRelations().size());
		assertEquals(template.getSDocumentGraph().getSSpanningRelations().size(), this.getFixture().getSDocument().getSDocumentGraph().getSSpanningRelations().size());
	}

	/**
	 * Reads the following xml-document, which represents the structure of
	 * {@link SampleGenerator#createSyntaxStructure(SDocument)} and
	 * {@link SampleGenerator#createSyntaxAnnotations(SDocument)}.
	 * 
	 * <pre>
	 *  <code>
	 * &lt;document author="John Doe"&gt;
	 *     &lt;struct const="SQ"&gt;
	 *         &lt;tok&gt;Is&lt;/tok&gt;
	 *         &lt;struct const="NP"&gt;
	 *             &lt;tok&gt;this&lt;/tok&gt;
	 *             &lt;tok&gt;example&lt;/tok&gt;
	 *         &lt;/struct&gt;
	 *         &lt;struct const="ADJP"&gt;
	 *             &lt;struct const="ADJP"&gt;
	 *                 &lt;tok&gt;more&lt;/tok&gt;
	 *                 &lt;tok&gt;complicated&lt;/tok&gt;
	 *             &lt;/struct&gt;
	 *             &lt;struct const="SBAR"&gt;
	 *                 &lt;tok&gt;than&lt;/tok&gt;
	 *                 &lt;struct const="S"&gt;
	 *                     &lt;struct const="NP"&gt;
	 *                         &lt;tok&gt;it&lt;/tok&gt;
	 *                     &lt;/struct&gt;
	 *                     &lt;struct const="VP"&gt;
	 *                         &lt;tok&gt;appears&lt;/tok&gt;
	 *                         &lt;struct const="S"&gt;
	 *                             &lt;struct const="VP"&gt;
	 *                                 &lt;tok&gt;to&lt;/tok&gt;
	 *                                 &lt;struct const="VP"&gt;
	 *                                     &lt;tok&gt;be&lt;/tok&gt;
	 *                                 &lt;/struct&gt;
	 *                             &lt;/struct&gt;
	 *                         &lt;/struct&gt;
	 *                     &lt;/struct&gt;
	 *                 &lt;/struct&gt;
	 *             &lt;/struct&gt;
	 *         &lt;/struct&gt;
	 *     &lt;/struct&gt;
	 *     &lt;tok&gt;?&lt;/tok&gt;
	 * &lt;/document&gt;
	 *  </code>
	 * </pre>
	 * 
	 * @throws XMLStreamException
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	public static void createHierarchy(XMLStreamWriter xmlWriter) throws XMLStreamException, ParserConfigurationException, SAXException, IOException {
		String elemRoot = "root";
		String elemStruct = "struct";
		String attConst = "const";
		String elemTok = "tok";
		
		xmlWriter.writeStartDocument();
		xmlWriter.writeStartElement(elemRoot);//start of root
		xmlWriter.writeAttribute("author", "John Doe");
		xmlWriter.writeStartElement(elemStruct);//SQ_start
		xmlWriter.writeAttribute(attConst, "SQ");
		xmlWriter.writeStartElement(elemTok);//tok_start
		xmlWriter.writeCharacters("Is");
		xmlWriter.writeEndElement();//tok_end

		xmlWriter.writeStartElement(elemStruct);//SQ.NP_start
		xmlWriter.writeAttribute(attConst, "NP");
		xmlWriter.writeStartElement(elemTok);//tok_start
		xmlWriter.writeCharacters("this");
		xmlWriter.writeEndElement();//tok_end
		xmlWriter.writeStartElement(elemTok);//tok_start
		xmlWriter.writeCharacters("example");
		xmlWriter.writeEndElement();//tok_end
		xmlWriter.writeEndElement();//SQ.NP_end

		xmlWriter.writeStartElement(elemStruct);//SQ.ADJP_start
		xmlWriter.writeAttribute(attConst, "ADJP");
		xmlWriter.writeStartElement(elemStruct);//SQ.ADJP.ADJP_start
		xmlWriter.writeAttribute(attConst, "ADJP");
		xmlWriter.writeStartElement(elemTok);//tok_start
		xmlWriter.writeCharacters("more");
		xmlWriter.writeEndElement();//tok_end
		xmlWriter.writeStartElement(elemTok);//tok_start
		xmlWriter.writeCharacters("complicated");
		xmlWriter.writeEndElement();//tok_end
		xmlWriter.writeEndElement();//SQ.ADJP.ADJP_end

		xmlWriter.writeStartElement(elemStruct);//SQ.ADJP.SBAR_start
		xmlWriter.writeAttribute(attConst, "SBAR");
		xmlWriter.writeStartElement(elemTok);//tok_start
		xmlWriter.writeCharacters("than");
		xmlWriter.writeEndElement();//tok_end

		xmlWriter.writeStartElement(elemStruct);//SQ.ADJP.SBAR.S_start
		xmlWriter.writeAttribute(attConst, "S");
		xmlWriter.writeStartElement(elemStruct);//SQ.ADJP.SBAR.S.NP_start
		xmlWriter.writeAttribute(attConst, "NP");
		xmlWriter.writeStartElement(elemTok);//tok_start
		xmlWriter.writeCharacters("it");
		xmlWriter.writeEndElement();//tok_end
		xmlWriter.writeEndElement();//SQ.ADJP.SBAR.S.NP_end

		xmlWriter.writeStartElement(elemStruct);//SQ.ADJP.SBAR.S.VP_start
		xmlWriter.writeAttribute(attConst, "VP");
		xmlWriter.writeStartElement(elemTok);//tok_start
		xmlWriter.writeCharacters("appears");
		xmlWriter.writeEndElement();//tok_end

		xmlWriter.writeStartElement(elemStruct);//SQ.ADJP.SBAR.S.VP.S_start
		xmlWriter.writeAttribute(attConst, "S");
		xmlWriter.writeStartElement(elemStruct);//SQ.ADJP.SBAR.S.VP.S.VP_start
		xmlWriter.writeAttribute(attConst, "VP");

		xmlWriter.writeStartElement(elemTok);//tok_start
		xmlWriter.writeCharacters("to");
		xmlWriter.writeEndElement();//tok_end

		xmlWriter.writeStartElement(elemStruct);//SQ.ADJP.SBAR.S.VP.S.VP.VP_start
		xmlWriter.writeAttribute(attConst, "VP");
		xmlWriter.writeStartElement(elemTok);//tok_start
		xmlWriter.writeCharacters("be");
		xmlWriter.writeEndElement();//tok_end
		xmlWriter.writeEndElement();//SQ.ADJP.SBAR.S.VP.S.VP.VP_end
		xmlWriter.writeEndElement();//SQ.ADJP.SBAR.S.VP.S.VP_end
		xmlWriter.writeEndElement();//SQ.ADJP.SBAR.S.VP.S_end
		xmlWriter.writeEndElement();//SQ.ADJP.SBAR.S.VP_end
		xmlWriter.writeEndElement();//SQ.ADJP.SBAR.S_end
		xmlWriter.writeEndElement();//SQ.ADJP.SBAR_end
		xmlWriter.writeEndElement();//SQ.ADJP_end
		xmlWriter.writeEndElement();//SQ_end
		//inserted "?"
		xmlWriter.writeStartElement(elemTok);//tok_start
		xmlWriter.writeCharacters("?");
		xmlWriter.writeEndElement();//tok_end
		//end
		xmlWriter.writeEndElement();//end of root		
		xmlWriter.writeEndDocument();
		xmlWriter.flush();
	}

	/**
	 * Reads the following xml-document, which represents the structure of
	 * {@link SampleGenerator#createInformationStructureSpan(SDocument)} and
	 * {@link SampleGenerator#createInformationStructureAnnotations(SDocument)}.
	 * 
	 * <pre>
	 *  <code>
	 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;
	 * &lt;document author="John Doe"&gt;
	 *     &lt;span inf-struct="contrast-focus"&gt;
	 *         &lt;morphology&gt;
	 *         	&lt;tok&gt;Is&lt;/tok&gt;
	 *         &lt;/morphology&gt;
	 *     &lt;/span&gt;
	 *     &lt;span inf-struct="topic"&gt;
	 *     	&lt;morphology&gt;
	 *         &lt;tok&gt;this&lt;/tok&gt;
	 *         &lt;tok&gt;example&lt;/tok&gt;
	 *         &lt;tok&gt;more&lt;/tok&gt;
	 *         &lt;tok&gt;complicated&lt;/tok&gt;
	 *         &lt;tok&gt;than&lt;/tok&gt;
	 *         &lt;tok&gt;it&lt;/tok&gt;
	 *         &lt;tok&gt;appears&lt;/tok&gt;
	 *         &lt;tok&gt;to&lt;/tok&gt;
	 *         &lt;tok&gt;be&lt;/tok&gt;
	 *         &lt;tok&gt;?&lt;/tok&gt;
	 *       &lt;/morphology&gt;
	 *     &lt;/span&gt;
	 * &lt;/document&gt;
	 *  </code>
	 * </pre>
	 * 
	 * @throws XMLStreamException
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	public static void createSpan(XMLStreamWriter xmlWriter) throws XMLStreamException, ParserConfigurationException, SAXException, IOException {
		String elemDocument = "document";
		String elemMorphologie = SampleGenerator.MORPHOLOGY_LAYER;
		String attInf = "inf-struct";

		xmlWriter.writeStartDocument();
		xmlWriter.writeStartElement(elemDocument);
		xmlWriter.writeAttribute("author", "John Doe");
		xmlWriter.writeStartElement("sSpan1");
		xmlWriter.writeAttribute(attInf, "contrast-focus");
		xmlWriter.writeStartElement(elemMorphologie);
		xmlWriter.writeStartElement("sTok1");
		xmlWriter.writeCharacters("Is");
		xmlWriter.writeEndElement();
		xmlWriter.writeCharacters(" ");
		xmlWriter.writeEndElement();
		xmlWriter.writeEndElement();

		xmlWriter.writeStartElement("sSpan2");
		xmlWriter.writeAttribute(attInf, "topic");
		xmlWriter.writeStartElement(elemMorphologie);
		xmlWriter.writeStartElement("sTok2");
		xmlWriter.writeCharacters("this");
		xmlWriter.writeEndElement();
		xmlWriter.writeCharacters(" ");

		xmlWriter.writeStartElement("sTok3");
		xmlWriter.writeCharacters("example");
		xmlWriter.writeEndElement();
		xmlWriter.writeCharacters(" ");

		xmlWriter.writeStartElement("sTok4");
		xmlWriter.writeCharacters("more");
		xmlWriter.writeEndElement();
		xmlWriter.writeCharacters(" ");

		xmlWriter.writeStartElement("sTok5");
		xmlWriter.writeCharacters("complicated");
		xmlWriter.writeEndElement();
		xmlWriter.writeCharacters(" ");

		xmlWriter.writeStartElement("sTok6");
		xmlWriter.writeCharacters("than");
		xmlWriter.writeEndElement();
		xmlWriter.writeCharacters(" ");

		xmlWriter.writeStartElement("sTok7");
		xmlWriter.writeCharacters("it");
		xmlWriter.writeEndElement();
		xmlWriter.writeCharacters(" ");

		xmlWriter.writeStartElement("sTok8");
		xmlWriter.writeCharacters("appears");
		xmlWriter.writeEndElement();
		xmlWriter.writeCharacters(" ");

		xmlWriter.writeStartElement("sTok9");
		xmlWriter.writeCharacters("to");
		xmlWriter.writeEndElement();
		xmlWriter.writeCharacters(" ");

		xmlWriter.writeStartElement("sTok10");
		xmlWriter.writeCharacters("be");
		xmlWriter.writeEndElement();		
		
		xmlWriter.writeStartElement("sTok11");
		xmlWriter.writeCharacters("?");
		xmlWriter.writeEndElement();		

		xmlWriter.writeEndElement();
		xmlWriter.writeEndElement();
		xmlWriter.writeEndElement();
		xmlWriter.writeEndDocument();
		xmlWriter.flush();
	}

	/**
	 * Tests if when the property
	 * {@link GenericXMLImporterProperties#PROP_ARTIFICIAL_SSTRUCT} is set to
	 * <code>true</code> and when
	 * {@link GenericXMLImporterProperties#PROP_AS_SPANS} is set to '//' (to
	 * every xml-element). The xml snippet: <br/>
	 * &lt;a&gt;&lt;b&gt;Is&lt;/b&gt;&lt;b&gt;this&lt;/b&gt;&lt;b&gt;&lt;c&gt;&
	 * lt;d&gt;example&lt;/d&gt;&lt;/c&gt;&lt;/b&gt;&lt;/a&gt; <br/>
	 * Shall be mapped to: {@link SToken} object having one {@link SAnnotation}
	 * having the name a_attB1 and one {@link SAnnotation} having the name
	 * attA2.
	 * 
	 * @throws XMLStreamException
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * 
	 */
	@Test
	public void testProp_createArtStruct_and_asSpan() throws XMLStreamException, ParserConfigurationException, SAXException, IOException {
		PepperModuleProperty<Boolean> prop1 = (PepperModuleProperty<Boolean>) this.getFixture().getProperties().getProperty(GenericXMLImporterProperties.PROP_ARTIFICIAL_SSTRUCT);
		assertNotNull(prop1);
		prop1.setValue(Boolean.TRUE);
		PepperModuleProperty<String> prop2 = (PepperModuleProperty<String>) this.getFixture().getProperties().getProperty(GenericXMLImporterProperties.PROP_AS_SPANS);
		assertNotNull(prop2);
		prop2.setValue("//");

		xmlWriter.writeStartDocument();
		xmlWriter.writeStartElement("a");
		xmlWriter.writeStartElement("b");
		xmlWriter.writeCharacters("Is");
		xmlWriter.writeEndElement();
		xmlWriter.writeStartElement("b");
		xmlWriter.writeCData("this");
		xmlWriter.writeEndElement();
		xmlWriter.writeStartElement("b");
		xmlWriter.writeStartElement("c");
		xmlWriter.writeStartElement("d");
		xmlWriter.writeCData("example");
		xmlWriter.writeEndElement();
		xmlWriter.writeEndElement();
		xmlWriter.writeEndElement();
		xmlWriter.writeEndElement();
		xmlWriter.writeEndDocument();
		xmlWriter.flush();

		String xml = outStream.toString();
		start(this.getFixture(), xml);

		assertEquals(3, this.getFixture().getSDocument().getSDocumentGraph().getSTokens().size());
		assertEquals(6, this.getFixture().getSDocument().getSDocumentGraph().getSSpans().size());
		assertEquals(10, this.getFixture().getSDocument().getSDocumentGraph().getSNodes().size());

	}
}
