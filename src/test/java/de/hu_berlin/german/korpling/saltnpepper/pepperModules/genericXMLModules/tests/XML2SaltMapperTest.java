package de.hu_berlin.german.korpling.saltnpepper.pepperModules.genericXMLModules.tests;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import junit.framework.TestCase;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.PepperModuleProperty;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.genericXMLModules.GenericXMLImporterProperties;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.genericXMLModules.XML2SaltMapper;
import de.hu_berlin.german.korpling.saltnpepper.salt.SaltFactory;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.resources.dot.Salt2DOT;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDataSourceSequence;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpan;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SStructure;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STYPE_NAME;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SToken;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SAnnotation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SLayer;

public class XML2SaltMapperTest extends TestCase {

	private XML2SaltMapper fixture= null;

	public void setFixture(XML2SaltMapper fixture) {
		this.fixture = fixture;
	}

	public XML2SaltMapper getFixture() {
		return fixture;
	}
	
	/**
	 * {@link OutputStream} where the xml nodes are written to.
	 */
	private ByteArrayOutputStream outStream= null;
	/**
	 * XMLWriter to write an xml stream.
	 */
	private XMLStreamWriter xmlWriter= null;
	
	/**
	 * Properties which are used for this fixture
	 */
	private GenericXMLImporterProperties props= null;
	
	@Override
	public void setUp() throws XMLStreamException
	{
		XML2SaltMapper mapper= new XML2SaltMapper();
		mapper.setsDocumentGraph(SaltFactory.eINSTANCE.createSDocumentGraph());
		props= new GenericXMLImporterProperties();
		mapper.setProps(props);
		this.setFixture(mapper);
		
		outStream = new ByteArrayOutputStream();
		XMLOutputFactory o= XMLOutputFactory.newFactory();
		xmlWriter= o.createXMLStreamWriter(outStream);
	}
	
	
	private void start(XML2SaltMapper mapper, String xmlString) throws ParserConfigurationException, SAXException, IOException
	{
		SAXParser parser;
        XMLReader xmlReader;
        
        SAXParserFactory factory= SAXParserFactory.newInstance();
        
    	parser= factory.newSAXParser();
        xmlReader= parser.getXMLReader();
        xmlReader.setContentHandler(mapper);
        InputStream inputStream = new ByteArrayInputStream(xmlString.getBytes());
        
        Reader reader = new InputStreamReader(inputStream, "UTF-8");
		 
		InputSource is = new InputSource(reader);
		is.setEncoding("UTF-8");
		 
		xmlReader.parse(is);
	}	
	/**
	 * Checks if a simple text is mapped.
	 * <br/>sample snippet:<br/>
	 * &lt;text&gt;Is this example more complicated than it appears to?&lt;/text&gt;
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws XMLStreamException 
	 */
	public void testSimpleText() throws ParserConfigurationException, SAXException, IOException, XMLStreamException
	{
		String text= "Is this example more complicated than it appears to?";
		xmlWriter.writeStartElement("text");
		xmlWriter.writeCharacters(text);
		xmlWriter.writeEndElement();
		
		String xml= outStream.toString();
		start(this.getFixture(), xml);
		
		assertNotNull(this.getFixture().getsDocumentGraph().getSTextualDSs());
		assertNotNull(this.getFixture().getsDocumentGraph().getSTextualDSs().get(0));
		assertNotNull(this.getFixture().getsDocumentGraph().getSTextualDSs().get(0).getSText());
		assertEquals(text, this.getFixture().getsDocumentGraph().getSTextualDSs().get(0).getSText());
	}	
	/**
	 * Checks if a simple text is mapped, even it is interrupted.
	 * See:
	 * <br/>
	 * &lt;text&gt;Is this example more&lt;text&gt; complicated than it appears to be?&lt;/text&gt;&lt;/text&gt;
	 * <br/>
	 * Shall be mapped to:
	 * <table border="1">
	 * <tr><td colspan="2">span1</td><tr>
	 * <tr><td>tok1</td><td>tok2</td><tr>
	 * <tr><td>Is this example more</td><td> complicated than it appears to be?</td><tr>
	 * </table>
	 * 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws XMLStreamException 
	 */
	public void testSimpleText_Interrupt() throws ParserConfigurationException, SAXException, IOException, XMLStreamException
	{
		String text1= "Is this example more";
		String text2= " complicated than it appears to be?";
		String text= text1 + text2;
		xmlWriter.writeStartElement("text");
		xmlWriter.writeAttribute("no", "text1");
		xmlWriter.writeCharacters(text1);
		xmlWriter.writeStartElement("text");
		xmlWriter.writeAttribute("no", "text2");
		xmlWriter.writeCharacters(text2);
		xmlWriter.writeEndElement();
		xmlWriter.writeEndElement();
		
		String xml= outStream.toString();
		start(this.getFixture(), xml);
		
		assertNotNull(this.getFixture().getsDocumentGraph().getSTextualDSs());
		assertNotNull(this.getFixture().getsDocumentGraph().getSTextualDSs().get(0));
		assertNotNull(this.getFixture().getsDocumentGraph().getSTextualDSs().get(0).getSText());
		assertEquals(text, this.getFixture().getsDocumentGraph().getSTextualDSs().get(0).getSText());
	}
	/**
	 * Checks if a simple text is mapped and if a token is created.
	 * <br/>See:<br/>
	 * &lt;text&gt;Is this example more complicated than it appears to?&lt;/text&gt;
	 * <br/>
	 * shall be mapped to:
	 * <table border="1">
	 * <tr><td>Is this example more complicated than it appears to?</td><tr>
	 * </table>
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws XMLStreamException 
	 */
	public void testSimpleToken() throws ParserConfigurationException, SAXException, IOException, XMLStreamException
	{
		String text= "Is this example more complicated than it appears to?";
		xmlWriter.writeStartElement("text");
		xmlWriter.writeCharacters(text);
		xmlWriter.writeEndElement();
		
		String xml= outStream.toString();
		start(this.getFixture(), xml);
		
		assertEquals(1, this.getFixture().getsDocumentGraph().getSTokens().size());
		assertNotNull(this.getFixture().getsDocumentGraph().getSTokens().get(0));
		EList<STYPE_NAME> sRelationTypes= new BasicEList<STYPE_NAME>();
		sRelationTypes.add(STYPE_NAME.STEXT_OVERLAPPING_RELATION);
		EList<SDataSourceSequence> sequences= this.getFixture().getsDocumentGraph().getOverlappedDSSequences(
				this.getFixture().getsDocumentGraph().getSTokens().get(0), sRelationTypes);
		assertEquals(new Integer(0), sequences.get(0).getSStart());
		assertEquals(new Integer(text.length()), sequences.get(0).getSEnd());
	}
	
	/**
	 * Checks if a simple text is mapped and a token is created. Further checks if an {@link SAnnotation} was created.
	 * <br/>
	 * sample snippet:
	 * <br/>
	 * &lt;text attName1=&quot;attValue1&quot;&gt;Is this example more complicated than it appears to?&lt;/text&gt;
	 * <br/>
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws XMLStreamException 
	 */
	public void testSimpleToken_WithAttribute() throws ParserConfigurationException, SAXException, IOException, XMLStreamException
	{
		String text= "Is this example more complicated than it appears to?";
		String attName1= "attName1";
		String attValue1= "attValue1";
		xmlWriter.writeStartElement("text");
		xmlWriter.writeAttribute(attName1, attValue1);
		xmlWriter.writeCharacters(text);
		xmlWriter.writeEndElement();
		
		String xml= outStream.toString();
		start(this.getFixture(), xml);
		
		assertEquals(1, this.getFixture().getsDocumentGraph().getSTokens().size());
		assertNotNull(this.getFixture().getsDocumentGraph().getSTokens().get(0));
		EList<STYPE_NAME> sRelationTypes= new BasicEList<STYPE_NAME>();
		sRelationTypes.add(STYPE_NAME.STEXT_OVERLAPPING_RELATION);
		EList<SDataSourceSequence> sequences= this.getFixture().getsDocumentGraph().getOverlappedDSSequences(
				this.getFixture().getsDocumentGraph().getSTokens().get(0), sRelationTypes);
		assertEquals(new Integer(0), sequences.get(0).getSStart());
		assertEquals(new Integer(text.length()), sequences.get(0).getSEnd());
		assertNotNull("an SAnnotation with name '"+attName1+"' does not belong to annotation list '"+this.getFixture().getsDocumentGraph().getSTokens().get(0).getSAnnotations()+"'", this.getFixture().getsDocumentGraph().getSTokens().get(0).getSAnnotation(attName1));
		assertEquals(attValue1, this.getFixture().getsDocumentGraph().getSTokens().get(0).getSAnnotation(attName1).getSValue());
	}
	
	/**
	 * Checks if a simple text is mapped and a token is created. Further checks if an SAnnotation was created.
	 * And if one was not created when property was set.
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws XMLStreamException 
	 */
	public void testSimpleToken_WithAttributes() throws ParserConfigurationException, SAXException, IOException, XMLStreamException
	{
		String attName1= "attName1";
		String attValue1= "attValue1";
		String attName2= "attName2";
		String attValue2= "attValue2";
		
		PepperModuleProperty<String> prop= (PepperModuleProperty<String>)this.getFixture().getProps().getProperty(GenericXMLImporterProperties.PROP_IGNORE_LIST);
		prop.setValue("//"+ attName1);
		String text= "Is this example more complicated than it appears to?";
		
		xmlWriter.writeStartElement("text");
		xmlWriter.writeAttribute(attName1, attValue1);
		xmlWriter.writeAttribute(attName2, attValue2);
		xmlWriter.writeCharacters(text);
		xmlWriter.writeEndElement();
		
		String xml= outStream.toString();
		start(this.getFixture(), xml);
		
		assertEquals(1, this.getFixture().getsDocumentGraph().getSTokens().size());
		assertNotNull(this.getFixture().getsDocumentGraph().getSTokens().get(0));
		EList<STYPE_NAME> sRelationTypes= new BasicEList<STYPE_NAME>();
		sRelationTypes.add(STYPE_NAME.STEXT_OVERLAPPING_RELATION);
		EList<SDataSourceSequence> sequences= this.getFixture().getsDocumentGraph().getOverlappedDSSequences(
				this.getFixture().getsDocumentGraph().getSTokens().get(0), sRelationTypes);
		assertEquals(new Integer(0), sequences.get(0).getSStart());
		assertEquals(new Integer(text.length()), sequences.get(0).getSEnd());
		assertNotNull(this.getFixture().getsDocumentGraph().getSTokens().get(0).getSAnnotation(attName1));
		assertNotNull(this.getFixture().getsDocumentGraph().getSTokens().get(0).getSAnnotation(attName2));
		assertEquals(attValue2, this.getFixture().getsDocumentGraph().getSTokens().get(0).getSAnnotation(attName2).getSValue());
	}
	
	/**
	 * Checks if an element-node containing a text-node and further element nodes will be mapped correctly.
	 * No properties used.
	 * The xml snippet:
	 * <br/>
	 * &lt;a&gt;here&lt;b&gt;comes&lt;/b&gt;text&lt;/a&gt;
	 * <br/>
	 * shall be mapped to:
	 * 
	 * <table border="1">
	 * <tr><td colspan="3">struct</td><tr>
	 * <tr><td>tok1</td><td>tok2</td><td>tok3</td><tr>
	 * <tr><td>here</td><td>comes</td><td>text</td><tr>
	 * </table>
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws XMLStreamException 
	 */
	public void testElementNodeWithComplexContent() throws ParserConfigurationException, SAXException, IOException, XMLStreamException
	{
		String text1= "here";
		String text2= "comes";
		String text3= "text";
		
		xmlWriter.writeStartElement("a");
		xmlWriter.writeCharacters(text1);
		xmlWriter.writeStartElement("b");
		xmlWriter.writeCharacters(text2);
		xmlWriter.writeEndElement();
		xmlWriter.writeCharacters(text3);
		xmlWriter.writeEndElement();
		
		String xml= outStream.toString();
		start(this.getFixture(), xml);
		
		assertEquals(3, this.getFixture().getsDocumentGraph().getSTokens().size());
		assertEquals(1, this.getFixture().getsDocumentGraph().getSStructures().size());
	}	
	/**
	 * Checks if a fragment with a deeper hierarchie is mapped correctly.
	 * The xml snippet:
	 * <br/>
	 * &lt;a&gt;&lt;b&gt;&lt;c&gt;a&lt;/c&gt;&lt;c&gt;sample&lt;/c&gt;&lt;/b&gt;&lt;c&gt;tex&lt;/c&gt;&lt;/a&gt;
	 * <br/>
	 * shall be mapped to:
	 * 
	 * <table border="1">
	 * <tr><td align="center" colspan="3">struct a</td><tr>
	 * <tr><td colspan="2">struct b</td><td/><tr>
	 * <tr><td>tok1</td><td>tok2</td><td>tok3</td><tr>
	 * <tr><td>a</td><td>sample</td><td>text</td><tr>
	 * </table>
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws XMLStreamException 
	 */
	public void testDeepHierarchie() throws ParserConfigurationException, SAXException, IOException, XMLStreamException
	{
		String text1= "a";
		String text2= "sample";
		String text3= "text";
		String elementA= "a";
		String elementB= "b";
		String elementC= "c";
		
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
		
		String xml= outStream.toString();
		start(this.getFixture(), xml);
		
		assertEquals(3, this.getFixture().getsDocumentGraph().getSTokens().size());
		assertEquals(2, this.getFixture().getsDocumentGraph().getSStructures().size());
		assertEquals(4, this.getFixture().getsDocumentGraph().getSDominanceRelations().size());
	}	
	/**
	 * Checks if all created {@link SToken} objects got the correct {@link SAnnotation} objects.
	 * <br/>
	 * The xml snippet:
	 * <br/>
	 * &lt;a&gt;&lt;b attB1="valB1"&gt;here&lt;b attB2="valB2"&gt;comes&lt;/b&gt;&lt;/a&gt;
	 * <br/>
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws XMLStreamException 
	 */
	public void testSTokensAndSAnnotations() throws ParserConfigurationException, SAXException, IOException, XMLStreamException
	{
		String text1= "here";
		String text2= "text";
		String attB1="attB1";
		String valB1="valB1";
		String attB2="attB2";
		String valB2="valB2";
		
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
		
		String xml= outStream.toString();
		start(this.getFixture(), xml);
		
		assertEquals(2, this.getFixture().getsDocumentGraph().getSTokens().size());
		assertNotNull(this.getFixture().getsDocumentGraph().getSTokens().get(0).getSAnnotations());
		assertEquals(1, this.getFixture().getsDocumentGraph().getSTokens().get(0).getSAnnotations().size());
		assertEquals(valB1, this.getFixture().getsDocumentGraph().getSTokens().get(0).getSAnnotation(attB1).getSValue());
		
		assertNotNull(this.getFixture().getsDocumentGraph().getSTokens().get(1).getSAnnotations());
		assertEquals(1, this.getFixture().getsDocumentGraph().getSTokens().get(1).getSAnnotations().size());
		assertEquals(valB2, this.getFixture().getsDocumentGraph().getSTokens().get(1).getSAnnotation(attB2).getSValue());
	}
	/**
	 * Checks that the following snippet is mapped to just one {@link SToken} object.
	 * The xml snippet:
	 * <br/>
	 * &lt;b&gt;text&lt;/b&gt;
	 * <br/>
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws XMLStreamException 
	 */
	public void testJustSToken() throws ParserConfigurationException, SAXException, IOException, XMLStreamException
	{
		String text1= "text";
		
		xmlWriter.writeStartElement("b");
		xmlWriter.writeCharacters(text1);
		xmlWriter.writeEndElement();
		
		String xml= outStream.toString();
		start(this.getFixture(), xml);
		
		assertEquals(1, this.getFixture().getsDocumentGraph().getSTokens().size());
		assertEquals("all nodes are: '"+this.getFixture().getsDocumentGraph().getSNodes()+"'", 2, this.getFixture().getsDocumentGraph().getSNodes().size());
	}
	
	/**
	 * Tests if when the property {@link GenericXMLImporterProperties#PROP_PREFIXED_ANNOS} is set, the {@link SAnnotation} 
	 * sName is correct.
	 * The xml snippet:
	 * <br/>
	 * &lt;a attA1="valA1" attA2="valA2"&gt;a sample&lt;/a&gt;
	 * <br/>
	 * Shall be mapped to:
	 * {@link SToken} object having one {@link SAnnotation} having the name a_attB1 and one {@link SAnnotation} having the name attA2.
	 * @throws XMLStreamException 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * 
	 */
	public void testProp_prefixSAnnotation() throws XMLStreamException, ParserConfigurationException, SAXException, IOException
	{
		String text1= "here";
		String element= "a";
		String attA1="attA1";
		String valA1="valA1";
		String attA2="attA2";
		String valA2="valA2";
		
		PepperModuleProperty<String> prop= (PepperModuleProperty<String>)this.getFixture().getProps().getProperty(GenericXMLImporterProperties.PROP_PREFIXED_ANNOS);
		assertNotNull(prop);
		prop.setValue("//@"+ attA1);
		
		xmlWriter.writeStartElement(element);
		xmlWriter.writeAttribute(attA1, valA1);
		xmlWriter.writeAttribute(attA2, valA2);
		xmlWriter.writeCharacters(text1);
		xmlWriter.writeEndElement();
		
		String xml= outStream.toString();
		start(this.getFixture(), xml);
		
		assertEquals(1, this.getFixture().getsDocumentGraph().getSTokens().size());
		assertNotNull(this.getFixture().getsDocumentGraph().getSTokens().get(0).getSAnnotations());
		assertEquals(2, this.getFixture().getsDocumentGraph().getSTokens().get(0).getSAnnotations().size());
		assertEquals(element+"_"+attA1, this.getFixture().getsDocumentGraph().getSTokens().get(0).getSAnnotations().get(0).getSName());
		assertEquals(attA2, this.getFixture().getsDocumentGraph().getSTokens().get(0).getSAnnotations().get(1).getSName());
	}	
	/**
	 * Tests if when the property {@link GenericXMLImporterProperties#PROP_PREFIXED_ANNOS} is set, the {@link SAnnotation} 
	 * sName is correct.
	 * The xml snippet:
	 * <br/>
	 * &lt;a attA1="valA1" attA2="valA2"&gt;a sample&lt;/a&gt;
	 * <br/>
	 * Shall be mapped to:
	 * {@link SToken} object having one {@link SAnnotation} having the name a_attB1 and one {@link SAnnotation} having the name attA2.
	 * @throws XMLStreamException 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * 
	 */
	public void testProp_sMetaAnnotation() throws XMLStreamException, ParserConfigurationException, SAXException, IOException
	{
		String text1= "here";
		String element= "a";
		String attA1="attA1";
		String valA1="valA1";
		String attA2="attA2";
		String valA2="valA2";
		
		PepperModuleProperty<String> prop= (PepperModuleProperty<String>)this.getFixture().getProps().getProperty(GenericXMLImporterProperties.PROP_SMETA_ANNOTATION);
		assertNotNull(prop);
		prop.setValue("//@"+ attA1);
		
		xmlWriter.writeStartElement(element);
		xmlWriter.writeAttribute(attA1, valA1);
		xmlWriter.writeAttribute(attA2, valA2);
		xmlWriter.writeCharacters(text1);
		xmlWriter.writeEndElement();
		
		String xml= outStream.toString();
		start(this.getFixture(), xml);
		
		assertEquals(1, this.getFixture().getsDocumentGraph().getSTokens().size());
		assertNotNull(this.getFixture().getsDocumentGraph().getSTokens().get(0).getSAnnotations());
		assertNotNull(this.getFixture().getsDocumentGraph().getSTokens().get(0).getSMetaAnnotations());
		assertEquals(1, this.getFixture().getsDocumentGraph().getSTokens().get(0).getSAnnotations().size());
		assertEquals(1, this.getFixture().getsDocumentGraph().getSTokens().get(0).getSMetaAnnotations().size());
		assertEquals(attA1, this.getFixture().getsDocumentGraph().getSTokens().get(0).getSMetaAnnotations().get(0).getSName());
		assertEquals(attA2, this.getFixture().getsDocumentGraph().getSTokens().get(0).getSAnnotations().get(0).getSName());
	}
	/**
	 * Checks if spans are correctly created in hierarchies when using the prop genericXml.importer.asSSpan
	 * Create a test for snippet:
	 * <br/>
	 * &lt;a&gt;&lt;b&gt;&lt;c&gt;a&lt;/c&gt;&lt;c&gt;sample&lt;/c&gt;&lt;/b&gt;&lt;/a&gt;
	 * <br/>
	 * {@value GenericXMLImporterProperties#PROP_AS_SPANS}= //b
	 * shall result in a {@link SSpan} for &lt;b&gt; and a {@link SStructure} for &lt;a&gt;, both containing the two {@link SToken} objects overlapping "a" and "sample"
	 * 
	 * @throws XMLStreamException 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * 
	 */
	public void testProp_asSpan() throws XMLStreamException, ParserConfigurationException, SAXException, IOException
	{
		String text1= "a";
		String text2= "sample";
		String elementA= "a";
		String elementB= "b";
		String elementC= "c";
		
		PepperModuleProperty<String> prop= (PepperModuleProperty<String>)this.getFixture().getProps().getProperty(GenericXMLImporterProperties.PROP_AS_SPANS);
		assertNotNull(prop);
		prop.setValue("//"+elementB);
		
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
		
		String xml= outStream.toString();
		start(this.getFixture(), xml);
		
		assertEquals(2, this.getFixture().getsDocumentGraph().getSTokens().size());
		assertEquals(1, this.getFixture().getsDocumentGraph().getSSpans().size());
		assertEquals(1, this.getFixture().getsDocumentGraph().getSStructures().size());
	}	
	/**
	 * Tests the same as {@link #testElementNodeWithComplexContent()} but with setted flag {@link GenericXMLImporterProperties#PROP_ARTIFICIAL_SSTRUCT}.
	 * The xml snippet:
	 * <br/>
	 * &lt;a&gt;here&lt;b&gt;comes&lt;/b&gt;text&lt;/a&gt;
	 * <br/>
	 * shall be mapped to:
	 * 
	 * <table border="1">
	 * <tr><td colspan="3">struct</td><tr>
	 * <tr><td/><td>struct1</td><td/><tr>
	 * <tr><td>tok1</td><td>tok2</td><td>tok3</td><tr>
	 * <tr><td>here</td><td>comes</td><td>text</td><tr>
	 * </table>
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws XMLStreamException 
	 */
	public void testProp_ArtificialStruct() throws ParserConfigurationException, SAXException, IOException, XMLStreamException
	{
		String text1= "here";
		String text2= "comes";
		String text3= "text";
		
		PepperModuleProperty<Boolean> prop= (PepperModuleProperty<Boolean>)this.getFixture().getProps().getProperty(GenericXMLImporterProperties.PROP_ARTIFICIAL_SSTRUCT);
		assertNotNull(prop);
		prop.setValue(true);
		
		xmlWriter.writeStartElement("a");
		xmlWriter.writeCharacters(text1);
		xmlWriter.writeStartElement("b");
		xmlWriter.writeCharacters(text2);
		xmlWriter.writeEndElement();
		xmlWriter.writeCharacters(text3);
		xmlWriter.writeEndElement();
		
		String xml= outStream.toString();
		start(this.getFixture(), xml);
		
		assertEquals(3, this.getFixture().getsDocumentGraph().getSTokens().size());
		assertEquals(2, this.getFixture().getsDocumentGraph().getSStructures().size());
	}
	/**
	 * Tests the same as {@link #testElementNodeWithComplexContent()} but with setted flag {@link GenericXMLImporterProperties#PROP_ARTIFICIAL_SSTRUCT}.
	 * The xml snippet:
	 * <br/>
	 * &lt;a&gt;here&lt;b&gt;&lt;c&gt;comes&lt;/c&gt;&lt;/b&gt;text&lt;/a&gt;
	 * <br/>
	 * shall be mapped to:
	 * 
	 * <table border="1">
	 * <tr><td colspan="3">struct (a)</td><tr>
	 * <tr><td/><td>b</td><td/><tr>
	 * <tr><td/><td>c</td><td/><tr>
	 * <tr><td>tok1</td><td>tok2</td><td>tok3</td><tr>
	 * <tr><td>here</td><td>comes</td><td>text</td><tr>
	 * </table>
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws XMLStreamException 
	 */
	public void testProp_ArtificialStruct2() throws ParserConfigurationException, SAXException, IOException, XMLStreamException
	{
		String text1= "here";
		String text2= "comes";
		String text3= "text";
		
		PepperModuleProperty<Boolean> prop= (PepperModuleProperty<Boolean>)this.getFixture().getProps().getProperty(GenericXMLImporterProperties.PROP_ARTIFICIAL_SSTRUCT);
		assertNotNull(prop);
		prop.setValue(true);
		
		xmlWriter.writeStartElement("a");
		xmlWriter.writeCharacters(text1);
		xmlWriter.writeStartElement("b");
		xmlWriter.writeStartElement("c");
		xmlWriter.writeCharacters(text2);
		xmlWriter.writeEndElement();
		xmlWriter.writeEndElement();
		xmlWriter.writeCharacters(text3);
		xmlWriter.writeEndElement();
		
		String xml= outStream.toString();
		start(this.getFixture(), xml);
		
		assertEquals(3, this.getFixture().getsDocumentGraph().getSTokens().size());
		assertEquals(3, this.getFixture().getsDocumentGraph().getSStructures().size());
	}
	/**
	 * Checks if spans are correctly created in hierarchies when using the prop genericXml.importer.asSSpan
	 * Create a test for snippet:
	 * <br/>
	 * &lt;a&gt;&lt;b&gt;&lt;c&gt;a&lt;/c&gt;&lt;c&gt;sample&lt;/c&gt;&lt;/b&gt;&lt;/a&gt;
	 * <br/>
	 * {@value GenericXMLImporterProperties#PROP_AS_SPANS}= //b
	 * shall result in a {@link SSpan} for &lt;b&gt; and a {@link SStructure} for &lt;a&gt;, both containing the two {@link SToken} objects overlapping "a" and "sample"
	 * 
	 * @throws XMLStreamException 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * 
	 */
	public void testProp_textOnly() throws XMLStreamException, ParserConfigurationException, SAXException, IOException
	{
		String text1= "a";
		String text2= "sample";
		String elementA= "a";
		String elementB= "b";
		String elementC= "c";
		
		PepperModuleProperty<Boolean> prop= (PepperModuleProperty<Boolean>)this.getFixture().getProps().getProperty(GenericXMLImporterProperties.PROP_TEXT_ONLY);
		assertNotNull(prop);
		prop.setValue(true);
		
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
		
		String xml= outStream.toString();
		start(this.getFixture(), xml);
		assertEquals(1, this.getFixture().getsDocumentGraph().getSTextualDSs().size());
		assertNotNull(this.getFixture().getsDocumentGraph().getSTextualDSs().get(0));
		assertEquals(text1+ text2, this.getFixture().getsDocumentGraph().getSTextualDSs().get(0).getSText());
	}
	/**
	 * Tests the use of property {@link GenericXMLImporterProperties#PROP_SMETA_ANNOTATION_SDOCUMENT}.
	 * <br/>
	 * &lt;a&gt;&lt;meta att1="val1"&gt;&lt;anothermeta att2="val1"/&gt;&lt;/meta&gt;&lt;b&gt;&lt;c&gt;a&lt;/c&gt;&lt;c&gt;sample&lt;/c&gt;&lt;/b&gt;&lt;/a&gt;
	 * <br/>
	 * {@value GenericXMLImporterProperties#PROP_SMETA_ANNOTATION_SDOCUMENT}= //meta
	 * shall result in a {@link SSpan} for &lt;b&gt; and a {@link SStructure} for &lt;a&gt;, both containing the two {@link SToken} objects overlapping "a" and "sample"
	 * 
	 * @throws XMLStreamException 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * 
	 */
	public void testProp_sMetaAnnotationSDocument() throws XMLStreamException, ParserConfigurationException, SAXException, IOException
	{
		String text1= "a";
		String text2= "sample";
		String elementA= "a";
		String elementB= "b";
		String elementC= "c";
		String elementMeta1= "meta";
		String elementMeta2= "anotherMeta";
		String att1= "att1";
		String att2= "att2";
		
		PepperModuleProperty<String> prop= (PepperModuleProperty<String>)this.getFixture().getProps().getProperty(GenericXMLImporterProperties.PROP_SMETA_ANNOTATION_SDOCUMENT);
		assertNotNull(prop);
		prop.setValue("//"+elementMeta1);
		
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
		
		SDocument sDocument= SaltFactory.eINSTANCE.createSDocument();
		this.getFixture().getsDocumentGraph().setSDocument(sDocument);
		String xml= outStream.toString();
		start(this.getFixture(), xml);
		
		assertEquals(2, sDocument.getSMetaAnnotations().size());
		assertNotNull(sDocument.getSMetaAnnotation(att1));
		assertEquals("val1", sDocument.getSMetaAnnotation(att1).getSValue());
		assertNotNull(sDocument.getSMetaAnnotation(att2));
		assertEquals("val2", sDocument.getSMetaAnnotation(att2).getSValue());
		assertEquals(1, this.getFixture().getsDocumentGraph().getSTextualDSs().size());
		assertNotNull(this.getFixture().getsDocumentGraph().getSTextualDSs().get(0));
		assertEquals(text1+ text2, this.getFixture().getsDocumentGraph().getSTextualDSs().get(0).getSText());
	}	
	
	/**
	 * Checks an element-node is read as {@link SLayer} object.
	 * <br/>
	 * &lt;a&gt;&lt;layer&gt;&lt;b&gt;&lt;c&gt;a&lt;/c&gt;&lt;c&gt;sample&lt;/c&gt;&lt;/b&gt;&lt;/layer&gt;&lt;/a&gt;
	 * <br/>
	 * {@value GenericXMLImporterProperties#PROP_slay}= //b
	 * shall result in a {@link SSpan} for &lt;b&gt; and a {@link SStructure} for &lt;a&gt;, both containing the two {@link SToken} objects overlapping "a" and "sample"
	 * 
	 * @throws XMLStreamException 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * 
	 */
	public void testProp_sLayer() throws XMLStreamException, ParserConfigurationException, SAXException, IOException
	{
		String text1= "a";
		String text2= "sample";
		String elementA= "a";
		String elementB= "b";
		String elementC= "c";
		String elementLayer= "layer";
		String layerAtt1= "layerAtt1";
		
		PepperModuleProperty<String> prop= (PepperModuleProperty<String>)this.getFixture().getProps().getProperty(GenericXMLImporterProperties.PROP_SLAYER);
		assertNotNull(prop);
		prop.setValue("//"+elementLayer);
		
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
		
		String xml= outStream.toString();
		start(this.getFixture(), xml);
		
		assertEquals(1, this.getFixture().getsDocumentGraph().getSLayers().size());
		assertNotNull(this.getFixture().getsDocumentGraph().getSLayers().get(0));
		assertEquals(1, this.getFixture().getsDocumentGraph().getSLayers().get(0).getSMetaAnnotations().size());
		assertNotNull(this.getFixture().getsDocumentGraph().getSLayers().get(0).getSMetaAnnotation(layerAtt1));
		assertEquals(2, this.getFixture().getsDocumentGraph().getSTokens().size());
		assertEquals(1, this.getFixture().getsDocumentGraph().getSSpans().size());
		assertEquals(1, this.getFixture().getsDocumentGraph().getSStructures().size());
		
		assertEquals(4, this.getFixture().getsDocumentGraph().getSLayers().get(0).getSNodes().size());
		assertEquals(4, this.getFixture().getsDocumentGraph().getSLayers().get(0).getSRelations().size());
	}	
}
