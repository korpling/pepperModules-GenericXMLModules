package de.hu_berlin.german.korpling.saltnpepper.pepperModules.genericXMLModules;

import java.util.Collection;
import java.util.Stack;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

import de.hu_berlin.german.korpling.saltnpepper.pepperModules.genericXMLModules.xpath.XPathExpression;
import de.hu_berlin.german.korpling.saltnpepper.salt.SaltFactory;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDocumentGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpan;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SStructure;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualDS;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SToken;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SAbstractAnnotation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SAnnotation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SMetaAnnotation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SNode;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SRelation;

public class XML2SaltMapper extends DefaultHandler2 {

	public XML2SaltMapper()
	{
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
	 * Determines if this object is inited.
	 */
	private boolean isInited= false;
	
	/**
	 * Initializes this object
	 */
	private void init()
	{
		this.currentXPath= new XPathExpression();
		this.currentSDS= SaltFactory.eINSTANCE.createSTextualDS();
		this.getsDocumentGraph().addSNode(currentSDS);
		this.currSNodePath= new Stack<SNode>();
		this.currSAbstractAnnotations= new Stack<EList<SAbstractAnnotation>>();
		this.isInited= true;
	}
	/**
	 * Contains all {@link SNode} object from current path in xml tree to its root.
	 */
	private Stack<SNode> currSNodePath= null;
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
	/**
	 * Determines if the last created sNode was a {@link SToken} object, used for recognizing, that an artificial 
	 * {@link SSpan} or {@link SStructure} will not be created in default. 
	 * 
	 */
	private Boolean sTokenCreated= false;
	/**
	 * Adds the read text node to the current {@link STextualDS} node. If a text was found also a {@link SToken} node
	 * and a {@link STextualRelation} will be add to {@link SDocumentGraph}.
	 */
	@Override
	public void characters(	char[] ch,
				            int start,
				            int length) throws SAXException
	{
		if (!isInited)
			init();
		StringBuffer textBuf= new StringBuffer();
		for (int i= start; i< start+length; i++)
			textBuf.append(ch[i]);
		
		String text= textBuf.toString();
		if (	(text!= null) &&
				(text.toString().length()>0))
		{
			textBuf= new StringBuffer();
			String containedText= currentSDS.getSText();
			if (containedText!= null)
				textBuf.append(containedText);
			int sStart= textBuf.length();
			textBuf.append(text);
			currentSDS.setSText(textBuf.toString());
			int sEnd= text.length();
			
			//create a new SToken object overlapping the current text-node
				SToken sToken= SaltFactory.eINSTANCE.createSToken();
				sToken.setSName(qName);
				sDocumentGraph.addSNode(sToken);
				if (openSToken== null)
					openSToken= new BasicEList<SToken>();
				openSToken.add(sToken);
				this.copySAbstratAnnotations(sToken);
			//create a new SToken object overlapping the current text-node
			
			//create a new STextualRelation object connecting the SToken and the current STextualDS object 
				STextualRelation sTextRel= SaltFactory.eINSTANCE.createSTextualRelation();
				sTextRel.setSToken(sToken);
				sTextRel.setSTextualDS(currentSDS);
				sTextRel.setSStart(sStart);
				sTextRel.setSEnd(sEnd);
			//create a new STextualRelation object connecting the SToken and the current STextualDS object
				
			sDocumentGraph.addSRelation(sTextRel);
		}
		
	}
	
	/**
	 * Temporary stores the attribute nodes of an element-node. First it has to be checked if the 
	 * current element-node is a {@link SToken} or not.  
	 */
	private Attributes attributes= null;
	/**
	 * Contains all nodes, which have been created, but not already added to the tree.
	 */
	private EList<SToken> openSToken= null;
	/**
	 * Temporary stores the name of an element-node. First it has to be checked if the 
	 * current element-node is a {@link SToken} or not.  
	 */
	private String qName= null;
	/**
	 * Stores all annotations corresponding to the current {@link SNode} object. This list can contain {@link SAnnotation} objects
	 * and {@link SMetaAnnotation} objects as well.
	 */
	private Stack<EList<SAbstractAnnotation>> currSAbstractAnnotations= null;
	
	@Override
	public void startElement(	String uri,
            					String localName,
            					String qName,
            					Attributes attributes)throws SAXException
    {
		if (!isInited)
			init();
		System.out.println("qName: "+ qName);
		currentXPath.addStep(qName);
		System.out.println("currentXPath: "+ currentXPath);
		currentSToken= null;
		if (!this.matches(this.getProps().getIgnoreList(), currentXPath))
		{//if element-node shall not be ignored
//			if (currSAbstractAnnotations== null)
			
			
			currSAbstractAnnotations.push(new BasicEList<SAbstractAnnotation>());
			System.out.println("put on stack: "+ this.currSAbstractAnnotations);
			if (attributes!= null)
			{//add attributes as annotations to node
				System.out.println("---> read annos");
//				currSAbstractAnnotations= null;
				for (int i= 0; i< attributes.getLength(); i++)
				{
					currentXPath.addStep("@"+attributes.getQName(i));
					if (!this.matches(this.getProps().getIgnoreList(), currentXPath))
					{//if element-node shall not be ignored
						SAnnotation  sAnno= SaltFactory.eINSTANCE.createSAnnotation();
						sAnno.setSName(attributes.getQName(i));
						sAnno.setSValue(attributes.getValue(i));
						currSAbstractAnnotations.peek().add(sAnno);
						System.out.println("---> create anno: "+ sAnno);
						System.out.println("---> in: "+ currSAbstractAnnotations);
					}//if element-node shall not be ignored
					currentXPath.removeLastStep();
				}
			}//add attributes as annotations to node
			System.out.println("not in ignoreList");
			this.attributes= attributes;
			this.qName= qName;
		}//if element-node shall not be ignored
    }
	
	/**
	 * Cleans up the stack and removes first element. 
	 * Cleans up current XPath and removes last element.
	 * Creates a new SNode of type {@link SSpan} or {@link SStructure}, with respect to flag {@link GenericXMLImporterProperties#PROP_SPANS}.
	 */
	@Override
	public void endElement(	String uri,
            				String localName,
            				String qName)throws SAXException
    {
		System.out.println("end: qName: "+ qName);
		if (!this.matches(this.getProps().getIgnoreList(), currentXPath))
		{//if element-node shall not be ignored
			
			if (!sTokenCreated)
			{
				SNode sNode= null;
				if (this.matches(this.getProps().getAsSpans(), currentXPath))
					sNode= SaltFactory.eINSTANCE.createSSpan();
				else sNode= SaltFactory.eINSTANCE.createSStructure();
				this.getsDocumentGraph().addSNode(sNode);
				System.out.println("--> HERE 1");
		//		if (currSAbstractAnnotations!= null)
				{//copy all annotations to sNode
					System.out.println("--> HERE 2");
					this.copySAbstratAnnotations(sNode);
	//				//set annotations to null, so that all other methods know to create a new list if necessary
	//				currSAbstractAnnotations= null;
				}//copy all annotations to sNode
				if (openSToken!= null)
				{//put all open SToken objects into subtree of current tree
					for (SToken sToken: openSToken)
					{
						SRelation sRel= null;
						if (sNode instanceof SSpan)
							sRel= SaltFactory.eINSTANCE.createSSpanningRelation();
						else if (sNode instanceof SStructure)
							sRel= SaltFactory.eINSTANCE.createSDominanceRelation();
						sRel.setSSource(sNode);
						sRel.setSTarget(sToken);
						this.getsDocumentGraph().addSRelation(sRel);
					}
					openSToken= null;
				}//put all open SToken objects into subtree of current tree
			}
			
	//		currSNodePath.pop();
			currentXPath.removeLastStep();
		}//if element-node shall not be ignored
    }
	
	/**
	 * Copies all annotations being contained in {@link #currSAbstractAnnotations} to the given {@link SNode} object
	 * @param sNode
	 */
	private void copySAbstratAnnotations(SNode sNode)
	{
		System.out.println("--> HERE 2");
		EList<SAbstractAnnotation> annos= currSAbstractAnnotations.pop();
		if (annos!= null)
		{
			for (SAbstractAnnotation sAnno: annos)
			{
				System.out.println("--> adding anno");
				if (sAnno instanceof SAnnotation)
					sNode.addSAnnotation((SAnnotation)sAnno);
				else if (sAnno instanceof SMetaAnnotation)
					sNode.addSMetaAnnotation((SMetaAnnotation)sAnno);
				System.out.println("--> sNode: "+ sNode.getSAnnotations());
			}
		}
	}
	
}
