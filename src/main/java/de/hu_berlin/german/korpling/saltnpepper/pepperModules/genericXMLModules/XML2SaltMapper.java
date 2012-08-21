package de.hu_berlin.german.korpling.saltnpepper.pepperModules.genericXMLModules;

import java.util.Collection;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

import de.hu_berlin.german.korpling.saltnpepper.pepperModules.genericXMLModules.xpath.XPathExpression;
import de.hu_berlin.german.korpling.saltnpepper.salt.SaltFactory;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDocumentGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualDS;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SToken;

public class XML2SaltMapper extends DefaultHandler2 {

	public XML2SaltMapper()
	{
		this.init();
	}
	/**
	 * Properties with which this mapper is customized.
	 */
	private GenericXMLImporterProperties props= null;
	/**
	 * returns the properties with which this mapper is customized.
	 * @return
	 */
	public GenericXMLImporterProperties getProps() {
		return props;
	}
	/**
	 * Sets the properties with which this mapper is customized.
	 * @param props
	 */
	public void setProps(GenericXMLImporterProperties props) {
		this.props = props;
	}

	/**
	 * Checks if the given {@link XPathExpression} xpr matches the given list of {@link XPathExpression} xprList
	 * @return
	 */
	private boolean matches(Collection<XPathExpression> xprList, XPathExpression xpr)
	{
		if (	(xprList!= null)&&
				(xprList.size()>0))
		{
			for (XPathExpression xpr2: xprList)
			{
				if (XPathExpression.matches(xpr2, xpr))
					return(true);
			}
		}
		return(false);
	}
	
	/**
	 * Initializes this object
	 */
	private void init()
	{
		this.currentXPath= new XPathExpression();
	}
	/**
	 * current {@link SDocumentGraph} to store all linguistic data in
	 */
	private SDocumentGraph sDocumentGraph= null;
	/**
	 * Sets current {@link SDocumentGraph} to store all linguistic data in
	 * @param sDocumentGraph
	 */
	public void setsDocumentGraph(SDocumentGraph sDocumentGraph) {
		this.sDocumentGraph = sDocumentGraph;
	}
	/**
	 * returns current {@link SDocumentGraph} to store all linguistic data in
	 * @return
	 */
	public SDocumentGraph getsDocumentGraph() {
		return sDocumentGraph;
	}
	/**
	 * stores the {@link XPathExpression} representing the path from root to current element. 
	 */
	private XPathExpression currentXPath= null;
	/**
	 * current {@link STextualDS} object to store textual primary data.
	 */
	private STextualDS currentSDS= null;
	/**
	 * current {@link SToken} object to refer to {@link STextualDS}.
	 */
	private SToken currentSToken= null;
	@Override
	public void characters(	char[] ch,
				            int start,
				            int length) throws SAXException
	{
		StringBuffer text= new StringBuffer();
		text.append(currentSDS.getSText());
		for (int i= start; i< start+length; i++)
			text.append(ch[i]);
		
		int sStart= currentSDS.getSText().length();
		int sEnd= text.length();
		currentSDS.setSText(text.toString());
		
		STextualRelation sTextRel= SaltFactory.eINSTANCE.createSTextualRelation();
		sTextRel.setSToken(currentSToken);
		sTextRel.setSTextualDS(currentSDS);
		sTextRel.setSStart(sStart);
		sTextRel.setSEnd(sEnd);
		
		sDocumentGraph.addSRelation(sTextRel);
		
	}
	
	@Override
	public void startElement(	String uri,
            					String localName,
            					String qName,
            					Attributes attributes)throws SAXException
    {
		currentXPath.addStep(qName);
		if (!this.matches(this.getProps().getIgnoreList(), currentXPath))
		{//if element-node shall not be ignored
			
		}//if element-node shall not be ignored
    }
	
	@Override
	public void endElement(	String uri,
            				String localName,
            				String qName)throws SAXException
    {
		currentXPath.removeLastStep();
    }
	
}
