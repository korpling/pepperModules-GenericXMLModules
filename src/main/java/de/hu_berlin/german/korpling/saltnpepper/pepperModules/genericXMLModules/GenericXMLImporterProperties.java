package de.hu_berlin.german.korpling.saltnpepper.pepperModules.genericXMLModules;

import java.util.Collection;
import java.util.Vector;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;

import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.PepperModuleProperties;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.PepperModuleProperty;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.genericXMLModules.xpath.XPathExpression;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpan;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SStructure;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SStructuredNode;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SToken;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SMetaAnnotation;

/**
 * Defines the properties to be used for the {@link GenericXMLImporter}. 
 * @author Florian Zipser
 *
 */
public class GenericXMLImporterProperties extends PepperModuleProperties 
{
	public static final String PREFIX="genericXml.importer.";
	
	/**
	 * Name of property of the nodes to be ignored while import.
	 */
	public static final String PROP_IGNORE_LIST=PREFIX+"ignoreList";
	/**
	 * Name of property of element-nodes to map to an {@link SSpan} instead of an {@link SStructure}
	 */
	public static final String PROP_SPANS=PREFIX+"asSSpan";
	/**
	 * Name of property containing a list of all element-nodes whos attributes shall be prefixed by the name of the element-node 
	 */
	public static final String PROP_PREFIXED_ANNOS=PREFIX+"prefixAnnotationName";
	/**
	 * Name of property to determine if an artificial {@link SStructuredNode} shall be created for all text-nodes.
	 */
	public static final String PROP_ARTIFICIAL_SSTRUCT=PREFIX+"artificialSStruct";
	/**
	 * Name of property to determine attribute-nodes to be mapped to {@link SMetaAnnotation}
	 */
	public static final String PROP_SMETA_ANNOTATION=PREFIX+"sMetaAnnotation";
	/**
	 * Name of property to determine subtrees to be mapped to {@link SMetaAnnotation} to the entire {@link SDocument}
	 */
	public static final String PROP_SMETA_ANNOTATION_SDOCUMENT=PREFIX+"sMetaAnnotation.sDocument";
	
	/**
	 * Name of property to determine the file ending of documents to be imported.
	 */
	public static final String PROP_FILE_ENDINGS=PREFIX+"file.endings";
	
	public GenericXMLImporterProperties()
	{
		this.addProperty(new PepperModuleProperty<String>(PROP_IGNORE_LIST, String.class, "IgnoreList is a list of nodes (element nodes , attribute -nodes and text-nodes, which shall be ignored while processing. Note that if an element node is part of the ignore list, its subtree will also be ignored)", false));
		this.addProperty(new PepperModuleProperty<String>(PROP_SPANS, String.class, "In case of you don't want to map an element node to an SStructure, you can map it to an SSpan. Note that this is only possible, if the element node directly contains a text node.", false));
		this.addProperty(new PepperModuleProperty<String>(PROP_PREFIXED_ANNOS, String.class, "You can set this flag to prefix the SName of the SAnnotation with the name of the surrounding element node.", false));
		this.addProperty(new PepperModuleProperty<Boolean>(PROP_ARTIFICIAL_SSTRUCT, Boolean.class, "If set to true, for each text node an artificial SStructuredNode will be created and overlap the also created (always even if this property is set to false) SToken node.", false, false));
		this.addProperty(new PepperModuleProperty<String>(PROP_FILE_ENDINGS, String.class, "Determines a list, containing the file endings, which files shall be imported. If you want to import all contained files no matter to their ending, add the string 'ALL' to the list. ", "ALL", false));
	}
	
	public EList<String> getFileEndings()
	{
		EList<String> retVal= new BasicEList<String>();
		String endingList= (String)this.getProperty(PROP_FILE_ENDINGS).getValue();
		String[] endings= endingList.split(",");
		if (endings!= null)
		{
			for (String ending: endings)
				retVal.add(ending.trim());
		}
		return(retVal);
	}
	
	/**
	 * Set of {@link XPathExpression}, which nodes shall be ignored including their subtrees in case of they are element-nodes
	 */
	private Collection<XPathExpression> ignoreList= null;
	/**
	 * Returns set of {@link XPathExpression}, which nodes shall be ignored including their subtrees in case of they are element-nodes
	 * @return
	 */
	public Collection<XPathExpression> getIgnoreList() {
		if (ignoreList== null)
		{
			Collection<XPathExpression> xPathList= extractXPathExpr(PROP_IGNORE_LIST); 
			if (ignoreList== null)
				ignoreList= xPathList;
		}
		return ignoreList;
	}
	
	/**
	 * set of {@link XPathExpression}, which nodes shall be mapped to {@link SSpan} objects instead of {@link SStructure} objects.
	 */
	private Collection<XPathExpression> asSpans= null;
	/**
	 * Returns set of {@link XPathExpression}, which nodes shall be mapped to {@link SSpan} objects instead of {@link SStructure} objects.
	 * @return
	 */
	public Collection<XPathExpression> getAsSpans() {
		if (asSpans== null)
		{
			Collection<XPathExpression> xPathList= extractXPathExpr(PROP_SPANS); 
			if (asSpans== null)
				asSpans= xPathList;
		}
		return asSpans;
	}
	
	/**
	 * set of {@link XPathExpression}, which attribute nodes shall be prefixed with the element-node name.
	 */
	private Collection<XPathExpression> prefixedAnnoList= null;
	/**
	 * Returns set of {@link XPathExpression}, which attribute nodes shall be prefixed with the element-node name.
	 * @return
	 */
	public Collection<XPathExpression> getPrefixedAnnoList() {
		if (prefixedAnnoList== null)
		{
			Collection<XPathExpression> xPathList= extractXPathExpr(PROP_PREFIXED_ANNOS); 
			if (prefixedAnnoList== null)
				prefixedAnnoList= xPathList;
		}
		return prefixedAnnoList;
	}
	
	private synchronized Collection<XPathExpression> extractXPathExpr(String propName)
	{
		Collection<XPathExpression> xPathList= new Vector<XPathExpression>();
		PepperModuleProperty<?> prop= this.getProperty(propName); 
		if (prop!= null)
		{	
			if (prop.getValue()!= null)
			{
				String[] xPathListStr= ((String)prop.getValue()).split(",");
				if (xPathList!= null)
				{
					for (String xPathString : xPathListStr)
						xPathList.add(new XPathExpression(xPathString.trim())); 
				}
			}
		}
		return(xPathList);
	}
	
	/**
	 * Returns if artificial {@link SStructuredNode} objects shall be created.
	 * @return
	 */
	public boolean isCreateSStructure()
	{
		return((Boolean)this.getProperty(PROP_ARTIFICIAL_SSTRUCT).getValue());
	}
}
