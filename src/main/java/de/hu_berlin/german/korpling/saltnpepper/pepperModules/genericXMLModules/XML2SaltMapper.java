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
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SLayer;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SMetaAnnotation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SNode;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SRelation;

/**
 * Maps an XML structure to a Salt {@link SDocumentGraph}.
 * @author Florian Zipser
 *
 */
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
		this.elementNodeStack= new Stack<XML2SaltMapper.ElementNodeEntry>();
		this.isInited= true;
		this.sLayerStack= new Stack<SLayer>();
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
				(text.toString().length()>0)&&
				(!isMetaSection))
		{
			textBuf= new StringBuffer();
			String containedText= currentSDS.getSText();
			if (containedText!= null)
				textBuf.append(containedText);
			int sStart= textBuf.length();
			textBuf.append(text);
			currentSDS.setSText(textBuf.toString());
			int sEnd= text.length();
			
			if (!this.getProps().isTextOnly())
			{
				//create a new SToken object overlapping the current text-node
					SToken sToken= SaltFactory.eINSTANCE.createSToken();
					sToken.setSName(this.elementNodeStack.peek().qName);
					sDocumentGraph.addSNode(sToken);
					if (openSNodes== null)
						openSNodes= new BasicEList<SNode>();
					openSNodes.add(sToken);
					this.copySAbstractAnnotations(sToken);
					if (!this.sLayerStack.isEmpty())
					{//add to sLayer if exist
						this.sLayerStack.peek().getSNodes().add(sToken);
					}//add to sLayer if exist
				//create a new SToken object overlapping the current text-node
				
				if (this.getProps().isCreateSStructure())
					this.elementNodeStack.peek().createSStruct= true;
				else this.elementNodeStack.peek().createSStruct= false;
				
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
	}	
//	/**
//	 * Contains all nodes, which have been created, but not already added to the tree.
//	 */
//	private EList<SToken> openSToken= null;
	/**
	 * Contains all {@link SNode} objects, which have been created, but not already added to the tree.
	 */
	private EList<SNode> openSNodes= null;
	/**
	 * stack to store all information about the element node path from root to current element node 
	 */
	private Stack<ElementNodeEntry> elementNodeStack= null;
	/**
	 * A class for storing information about an element node.
	 * @author Florian Zipser
	 */
	private class ElementNodeEntry
	{
		/**
		 * Temporary stores the name of an element-node. First it has to be checked if the 
		 * current element-node is a {@link SToken} or not. 
		 */
		public String qName= null;
		/**
		 * Stores all annotations corresponding to the current {@link SNode} object. This list can contain {@link SAnnotation} objects
		 * and {@link SMetaAnnotation} objects as well.
		 */
		public EList<SAbstractAnnotation> annotations= null;
		public Boolean createSStruct= null;
		/**
		 * Determines if element node is a complex node (contains further element nodes) 
		 */
		public Boolean isComplex= false;
		
		public ElementNodeEntry(	final String qName, 
									final EList<SAbstractAnnotation> annotations, 
									final Boolean createSStruct)
		{
			this.qName= qName;
			this.annotations= annotations;
			this.createSStruct= createSStruct;
		}
		
		public String toString()
		{
			return("["+qName+", "+ createSStruct+", "+ annotations+", "+isComplex+"]");
		}
	}
	/**
	 * Determines if currently a section for meta data is active.
	 */
	private boolean isMetaSection=false;
	/**
	 * a stack containing all active layers.
	 */
	private Stack<SLayer> sLayerStack= null;
	
	/**
	 * Creates a list of {@link SAbstractAnnotation} objects for the given {@link Attributes} object and the 
	 * name of the element-node given by <em>qName</em> and returns them.
	 * @param qName
	 * @param attributes
	 * @return
	 */
	private EList<SAbstractAnnotation> createSAbstractAnnotations(	Class<? extends SAbstractAnnotation> clazz, 
																	String qName, 
																	Attributes attributes)
	{
		EList<SAbstractAnnotation> annoList= null;
		for (int i= 0; i< attributes.getLength(); i++)
		{//create annotation list
			if (annoList== null)
				annoList= new BasicEList<SAbstractAnnotation>();
			currentXPath.addStep("@"+attributes.getQName(i));
			if (!this.matches(this.getProps().getIgnoreList(), currentXPath))
			{//if element-node shall not be ignored
				SAbstractAnnotation  sAnno= null;
				if (this.matches(this.getProps().getSMetaAnnotationList(), currentXPath))
					sAnno= SaltFactory.eINSTANCE.createSMetaAnnotation();
				else
				{
					if (SMetaAnnotation.class.equals(clazz))
						sAnno= SaltFactory.eINSTANCE.createSMetaAnnotation();
					else if (SAnnotation.class.equals(clazz))
						sAnno= SaltFactory.eINSTANCE.createSAnnotation();
				}
				if (this.matches(this.getProps().getPrefixedAnnoList(), currentXPath))
					sAnno.setSName(qName+"_"+attributes.getQName(i));
				else
					sAnno.setSName(attributes.getQName(i));
				sAnno.setSValue(attributes.getValue(i));
				annoList.add(sAnno);
			}//if element-node shall not be ignored
			currentXPath.removeLastStep();
		}//create annotation list
		return(annoList);
	}
	
	@Override
	public void startElement(	String uri,
            					String localName,
            					String qName,
            					Attributes attributes)throws SAXException
    {
		if (!isInited)
			init();
		currentXPath.addStep(qName);
		if (this.matches(this.getProps().getSMetaAnnotationSDocumentList(), currentXPath))
			this.isMetaSection= true;
		
		if (this.isMetaSection)
		{
			EList<SAbstractAnnotation>annoList= new BasicEList<SAbstractAnnotation>();
			annoList.addAll(this.createSAbstractAnnotations(SMetaAnnotation.class, qName, attributes));
			if (this.getsDocumentGraph().getSDocument()!= null)
			{
				for (SAbstractAnnotation sAnno: annoList)
					this.getsDocumentGraph().getSDocument().addSMetaAnnotation((SMetaAnnotation)sAnno);
			}
		}
		else if (this.matches(this.getProps().getSLayerList(), currentXPath))
		{
			SLayer currSLayer= null;
			EList<SLayer> sLayers= this.getsDocumentGraph().getSLayerByName(qName);
			if (	(sLayers!= null)&&
					(sLayers.size()>0))
				currSLayer= sLayers.get(0);
			if (currSLayer== null)
			{
				currSLayer= SaltFactory.eINSTANCE.createSLayer();
				currSLayer.setSName(qName);
			}
			
			EList<SAbstractAnnotation>annoList= this.createSAbstractAnnotations(SMetaAnnotation.class, qName, attributes);
			if (	(currSLayer!= null)&&
					(annoList!= null)&&
					(annoList.size()> 0))
			{
				for (SAbstractAnnotation sAnno: annoList)
					currSLayer.addSMetaAnnotation((SMetaAnnotation)sAnno);
			}
			if (!this.sLayerStack.isEmpty())
				this.sLayerStack.peek().getSSubLayers().add(currSLayer);
			this.getsDocumentGraph().addSLayer(currSLayer);
			this.sLayerStack.push(currSLayer);
		}
		else if (	(!this.matches(this.getProps().getIgnoreList(), currentXPath))&&
					(!this.getProps().isTextOnly()))
		{//if element-node shall not be ignored
			//notify parent element, that it is complex
			if (this.elementNodeStack.size()> 0)
				this.elementNodeStack.peek().isComplex= true;
			
			BasicEList<SAbstractAnnotation> annoList= null;
			if (attributes.getLength()> 0)
			{//if attribute nodes are given, map them to SAnnotation objects
				annoList= new BasicEList<SAbstractAnnotation>();
				annoList.addAll(this.createSAbstractAnnotations(SAnnotation.class, qName, attributes));
			}//if attribute nodes are given, map them to SAnnotation objects
			//create ElementNodeEntry for current element node and add to stack
			ElementNodeEntry elementNode= new ElementNodeEntry(qName, annoList, true);
			this.elementNodeStack.push(elementNode);
		}//if element-node shall not be ignored
    }
	
	/**
	 * Cleans up the stack and removes first element. 
	 * Cleans up current XPath and removes last element.
	 * Creates a new SNode of type {@link SSpan} or {@link SStructure}, with respect to flag {@link GenericXMLImporterProperties#PROP_AS_SPANS}.
	 */
	@Override
	public void endElement(	String uri,
            				String localName,
            				String qName)throws SAXException
    {
		
		if (this.isMetaSection)
		{
			if (this.matches(this.getProps().getSMetaAnnotationSDocumentList(), currentXPath))
				this.isMetaSection= false;
			currentXPath.removeLastStep();
		}
		else if (this.matches(this.getProps().getSLayerList(), currentXPath))
		{	
			if (!this.sLayerStack.isEmpty())
				this.sLayerStack.pop();
			currentXPath.removeLastStep();
		}
		else if (	(!this.matches(this.getProps().getIgnoreList(), currentXPath))&&
					(!this.getProps().isTextOnly()))
		{//if element-node shall not be ignored
			if (	(this.elementNodeStack.peek().createSStruct)||
					(this.elementNodeStack.peek().isComplex))
			{
				SNode sNode= null;
				if (this.matches(this.getProps().getAsSpans(), currentXPath))
					sNode= SaltFactory.eINSTANCE.createSSpan();
				else sNode= SaltFactory.eINSTANCE.createSStructure();
				sNode.setSName(qName);
				this.getsDocumentGraph().addSNode(sNode);
				//copy all annotations to sNode
				this.copySAbstractAnnotations(sNode);
				
				if (openSNodes!= null)
				{//put all open SToken objects into subtree of current tree
					for (SNode childSNode: openSNodes)
					{
						SRelation sRel= null;
						if (	(sNode instanceof SSpan)&&
								(childSNode instanceof SToken))
						{
						  sRel= SaltFactory.eINSTANCE.createSSpanningRelation();
						}
						else if (sNode instanceof SStructure)
							sRel= SaltFactory.eINSTANCE.createSDominanceRelation();
						sRel.setSSource(sNode);
						sRel.setSTarget(childSNode);
						this.getsDocumentGraph().addSRelation(sRel);
						if (!this.sLayerStack.isEmpty())
						{//add to sLayer if exist
							this.sLayerStack.peek().getSRelations().add(sRel);
						}//add to sLayer if exist
					}
					openSNodes= null;
				}//put all open SToken objects into subtree of current tree
				if (openSNodes== null)
					openSNodes= new BasicEList<SNode>();
				openSNodes.add(sNode);
				if (!this.sLayerStack.isEmpty())
				{//add to sLayer if exist
					this.sLayerStack.peek().getSNodes().add(sNode);
				}//add to sLayer if exist
			}
			
			currentXPath.removeLastStep();
			this.elementNodeStack.pop();
		}//if element-node shall not be ignored
    }
	
	/**
	 * Copies all annotations being contained in {@link #currSAbstractAnnotations} to the given {@link SNode} object
	 * @param sNode
	 */
	private void copySAbstractAnnotations(SNode sNode)
	{
		if (elementNodeStack.peek().annotations!= null)
		{
			for (SAbstractAnnotation sAnno: elementNodeStack.peek().annotations)
			{
				if (sAnno instanceof SAnnotation)
					sNode.addSAnnotation((SAnnotation)sAnno);
				else if (sAnno instanceof SMetaAnnotation)
					sNode.addSMetaAnnotation((SMetaAnnotation)sAnno);
			}
		}
	}
	
}
