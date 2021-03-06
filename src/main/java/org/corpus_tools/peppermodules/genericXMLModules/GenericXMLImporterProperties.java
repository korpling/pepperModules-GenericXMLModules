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
package org.corpus_tools.peppermodules.genericXMLModules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.corpus_tools.pepper.modules.PepperModuleProperties;
import org.corpus_tools.pepper.modules.PepperModuleProperty;
import org.corpus_tools.peppermodules.genericXMLModules.xpath.XPathExpression;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SSpan;
import org.corpus_tools.salt.common.SStructure;
import org.corpus_tools.salt.common.SStructuredNode;
import org.corpus_tools.salt.common.STextualDS;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.core.SAnnotation;
import org.corpus_tools.salt.core.SLayer;
import org.corpus_tools.salt.core.SMetaAnnotation;

/**
 * Defines the properties to be used for the {@link GenericXMLImporter}.
 * 
 * @author Florian Zipser
 * 
 */
public class GenericXMLImporterProperties extends PepperModuleProperties {
	public static final String PREFIX = "genericXml.importer.";

	/**
	 * Name of property of the nodes to be ignored while import.
	 */
	public static final String PROP_IGNORE_LIST = PREFIX + "ignoreList";
	/**
	 * Name of property of element-nodes to map to an {@link SSpan} instead of
	 * an {@link SStructure}
	 */
	public static final String PROP_AS_SPANS = PREFIX + "asSSpan";
	/**
	 * Name of property containing a list of all element-nodes whos attributes
	 * shall be prefixed by the name of the element-node
	 */
	public static final String PROP_PREFIXED_ANNOS = PREFIX + "prefixSAnnotationName";
	/**
	 * Name of property to determine if an artificial {@link SStructuredNode}
	 * shall be created for all text-nodes.
	 */
	public static final String PROP_ARTIFICIAL_SSTRUCT = PREFIX + "artificialSStruct";
	/**
	 * Name of property containing a list of element-nodes or attribute nodes to
	 * be mapped to {@link SMetaAnnotation} objects instead of
	 * {@link SAnnotation} objects.
	 */
	public static final String PROP_SMETA_ANNOTATION = PREFIX + "sMetaAnnotation";
	/**
	 * Name of property to determine subtrees to be mapped to
	 * {@link SMetaAnnotation} to the entire {@link SDocument}
	 */
	public static final String PROP_SMETA_ANNOTATION_SDOCUMENT = PREFIX + "sMetaAnnotation.sDocument";
	/**
	 * Name of property determining if only text is imported.
	 */
	public static final String PROP_TEXT_ONLY = PREFIX + "textOnly";
	/**
	 * Name of property containing a list of element-nodes being mapped to
	 * {@link SLayer} objects.
	 */
	public static final String PROP_SLAYER = PREFIX + "sLayer";
	/**
	 * Name of property to determine which whitespace characters are ignored and
	 * add to {@link STextualDS} object, but do not get an own {@link SToken}
	 * object. A list separated by ',' for instance /n,/r, ,... for linefeed,
	 * carriage return and blank. Default is {@value #DEFAULT_WHITESPACES}.
	 */
	public static final String PROP_IGNORABLE_WHITESPACES = PREFIX + "ignoreWhitepsaces";
	/**
	 * Name of property to determine if namespaces are ignored. Default is true.
	 */
	public static final String PROP_IGNORABLE_NAMESPACES = PREFIX + "ignoreNamespaces";
	/**
	 * Name of property to determine a list of element-nodes whichs names are
	 * mapped to {@link SAnnotation} objects having that name as sName and
	 * sValue
	 */
	public static final String PROP_ELEMENTNAME_AS_SANNO = PREFIX + "elementNameAsSAnno";
	/**
	 * Name of property to determine the file ending of documents to be
	 * imported.
	 */
	public static final String PROP_FILE_ENDINGS = PREFIX + "file.endings";
	/**
	 * Name of property to set a separator between tokens, the default is a
	 * blank, but it can be set to any other character or even ot the empty
	 * String.
	 **/
	public static final String PROP_SEPARATE_TOKENS = PREFIX + "separateTokens";

	/**
	 * When the list of file endings contain this string, all file endings will
	 * be accepted
	 */
	public static final String KW_ALL = "ALL";

	/**
	 * Default whitespaces to be ignored.
	 */
	public static final String DEFAULT_WHITESPACES = "'\n','\r','\t',' '";

	public GenericXMLImporterProperties() {
		this.addProperty(new PepperModuleProperty<String>(PROP_IGNORE_LIST, String.class,
				"IgnoreList is a list of nodes (element nodes , attribute -nodes and text-nodes, which shall be ignored while processing. Note that if an element node is part of the ignore list, its subtree will also be ignored)",
				false));
		this.addProperty(new PepperModuleProperty<String>(PROP_AS_SPANS, String.class,
				"In case of you don't want to map an element node to an SStructure, you can map it to an SSpan. Note that this is only possible, if the element node directly contains a text node.",
				false));
		this.addProperty(new PepperModuleProperty<String>(PROP_PREFIXED_ANNOS, String.class,
				"You can set this flag to prefix the SName of the SAnnotation with the name of the surrounding element node.",
				false));
		this.addProperty(new PepperModuleProperty<String>(PROP_SMETA_ANNOTATION, String.class,
				"Determines a list a list of attribute names mapped to SMetaAnnotation objects instead of SAnnotationobjects.",
				false));
		this.addProperty(new PepperModuleProperty<String>(PROP_SMETA_ANNOTATION_SDOCUMENT, String.class,
				"Determines a list of element-nodes which subtrees are mapped to meta-data on entire SDocument.",
				false));
		this.addProperty(new PepperModuleProperty<String>(PROP_SLAYER, String.class,
				"Determines a list of element-nodes which are mapped to a SLayer object.", false));
		this.addProperty(new PepperModuleProperty<Boolean>(PROP_ARTIFICIAL_SSTRUCT, Boolean.class,
				"If set to true, for each text node an artificial SStructuredNode will be created and overlap the also created (always even if this property is set to false) SToken node.",
				false, false));
		this.addProperty(new PepperModuleProperty<Boolean>(PROP_TEXT_ONLY, Boolean.class,
				"Determines if only text-nodes are mapped.", false, false));
		this.addProperty(new PepperModuleProperty<String>(PROP_IGNORABLE_WHITESPACES, String.class,
				"Determine which whitespace characters are ignored and add to {@link STextualDS} object,     * but do not get an own {@link SToken} object. A list separated by ',' for instance \\n,\\r, ,... for linefeed, carriage return and blank.",
				DEFAULT_WHITESPACES, false));
		this.addProperty(new PepperModuleProperty<Boolean>(PROP_IGNORABLE_NAMESPACES, Boolean.class,
				"Determines if xml-namespaces and namespace declarations are ignored.", true, false));
		this.addProperty(new PepperModuleProperty<String>(PROP_ELEMENTNAME_AS_SANNO, String.class,
				" Determines a list of element-nodes whichs names are mapped to SAnnotation objects having that name as sName and sValue",
				false));
		this.addProperty(new PepperModuleProperty<String>(PROP_FILE_ENDINGS, String.class,
				"Determines a list, containing the file endings, which files shall be imported. If you want to import all contained files no matter to their ending, add the string 'ALL' to the list. ",
				"ALL", false));
		this.addProperty(new PepperModuleProperty<String>(PROP_SEPARATE_TOKENS, String.class,
				"Sets a separator between tokens, the default is a blank, but it can be set to any other character or even ot the empty String. ",
				" ", false));
	}

	public synchronized List<String> getFileEndings() {
		List<String> retVal = new ArrayList<String>();
		String endingList = (String) this.getProperty(PROP_FILE_ENDINGS).getValue();
		String[] endings = endingList.split(",");
		if (endings != null) {
			for (String ending : endings) {
				retVal.add(ending.trim());
			}
		}
		return (retVal);
	}

	/**
	 * Set of {@link XPathExpression}, which nodes shall be ignored including
	 * their subtrees in case of they are element-nodes
	 */
	private Collection<XPathExpression> ignoreList = null;

	/**
	 * Returns set of {@link XPathExpression}, which nodes shall be ignored
	 * including their subtrees in case of they are element-nodes
	 * 
	 * @return
	 */
	public Collection<XPathExpression> getIgnoreList() {
		if (ignoreList == null) {
			synchronized (this) {
				if (ignoreList == null) {
					Collection<XPathExpression> xPathList = extractXPathExpr(PROP_IGNORE_LIST);
					if (ignoreList == null)
						ignoreList = xPathList;
				}
			}
		}
		return ignoreList;
	}

	/**
	 * set of {@link XPathExpression}, which nodes shall be mapped to
	 * {@link SSpan} objects instead of {@link SStructure} objects.
	 */
	private Collection<XPathExpression> asSpans = null;

	/**
	 * Returns set of {@link XPathExpression}, which nodes shall be mapped to
	 * {@link SSpan} objects instead of {@link SStructure} objects.
	 * 
	 * @return
	 */
	public Collection<XPathExpression> getAsSpans() {
		if (asSpans == null) {
			synchronized (this) {
				if (asSpans == null) {
					Collection<XPathExpression> xPathList = extractXPathExpr(PROP_AS_SPANS);
					if (asSpans == null)
						asSpans = xPathList;
				}
			}
		}
		return asSpans;
	}

	/**
	 * set of {@link XPathExpression}, which attribute nodes shall be prefixed
	 * with the element-node name.
	 */
	private Collection<XPathExpression> prefixedAnnoList = null;

	/**
	 * Returns set of {@link XPathExpression}, which attribute nodes shall be
	 * prefixed with the element-node name.
	 * 
	 * @return
	 */
	public Collection<XPathExpression> getPrefixedAnnoList() {
		if (prefixedAnnoList == null) {
			synchronized (this) {
				if (prefixedAnnoList == null) {
					Collection<XPathExpression> xPathList = extractXPathExpr(PROP_PREFIXED_ANNOS);
					if (prefixedAnnoList == null)
						prefixedAnnoList = xPathList;
				}
			}
		}
		return prefixedAnnoList;
	}

	/**
	 * set of {@link XPathExpression}, which attribute nodes shall be mapped to
	 * {@link SMetaAnnotation} objects
	 */
	private Collection<XPathExpression> sMetaAnnoList = null;

	/**
	 * Returns set of {@link XPathExpression}, which attribute nodes shall be
	 * mapped to {@link SMetaAnnotation} objects.
	 * 
	 * @return
	 */
	public Collection<XPathExpression> getMetaAnnotationList() {
		if (sMetaAnnoList == null) {
			synchronized (this) {
				if (sMetaAnnoList == null) {
					Collection<XPathExpression> xPathList = extractXPathExpr(PROP_SMETA_ANNOTATION);
					if (sMetaAnnoList == null)
						sMetaAnnoList = xPathList;
				}
			}
		}
		return sMetaAnnoList;
	}

	/**
	 * set of {@link XPathExpression}, which attribute nodes shall be mapped to
	 * {@link SMetaAnnotation} objects on the {@link SDocument}
	 */
	private Collection<XPathExpression> sMetaAnnoSDocumentList = null;

	/**
	 * Returns set of {@link XPathExpression}, which attribute nodes shall be
	 * mapped to {@link SMetaAnnotation} objects on the {@link SDocument}.
	 * 
	 * @return
	 */
	public Collection<XPathExpression> getMetaAnnotationSDocumentList() {
		if (sMetaAnnoSDocumentList == null) {
			synchronized (this) {
				if (sMetaAnnoSDocumentList == null) {
					Collection<XPathExpression> xPathList = extractXPathExpr(PROP_SMETA_ANNOTATION_SDOCUMENT);
					if (sMetaAnnoSDocumentList == null)
						sMetaAnnoSDocumentList = xPathList;
				}
			}
		}
		return sMetaAnnoSDocumentList;
	}

	/**
	 * set of {@link XPathExpression}, which element-nodes shall be mapped to
	 * {@link SLayer} objects.
	 */
	private Collection<XPathExpression> sLayerList = null;

	/**
	 * Returns set of {@link XPathExpression}, which element-nodes shall be
	 * mapped to {@link SLayer} objects.
	 * 
	 * @return
	 */
	public Collection<XPathExpression> getSLayerList() {
		if (sLayerList == null) {
			Collection<XPathExpression> xPathList = extractXPathExpr(PROP_SLAYER);
			if (sLayerList == null)
				sLayerList = xPathList;
		}
		return sLayerList;
	}

	private synchronized Collection<XPathExpression> extractXPathExpr(String propName) {
		Collection<XPathExpression> xPathList = new Vector<XPathExpression>();
		PepperModuleProperty<?> prop = this.getProperty(propName);
		if (prop != null) {
			if (prop.getValue() != null) {
				String[] xPathListStr = ((String) prop.getValue()).split(",");
				if (xPathList != null) {
					for (String xPathString : xPathListStr)
						xPathList.add(new XPathExpression(xPathString.trim()));
				}
			}
		}
		return (xPathList);
	}

	/**
	 * Returns true if artificial {@link SStructuredNode} object shall be
	 * created.
	 * 
	 * @return
	 */
	public boolean isCreateSStructure() {
		return ((Boolean) this.getProperty(PROP_ARTIFICIAL_SSTRUCT).getValue());
	}

	/**
	 * Returns if only text shall be mapped.
	 * 
	 * @return
	 */
	public boolean isTextOnly() {
		return ((Boolean) this.getProperty(PROP_TEXT_ONLY).getValue());
	}

	/**
	 * Returns if xml-namespaces and namespace declarations are ignored..
	 * 
	 * @return
	 */
	public boolean isIgnoreNamespaces() {
		return ((Boolean) this.getProperty(PROP_IGNORABLE_NAMESPACES).getValue());
	}

	/**
	 * Determine which whitespace characters are ignored and add to
	 * {@link STextualDS} object, but do not get an own {@link SToken} object. A
	 * list separated by ',' for instance /n,/r, ,... for linefeed, carriage
	 * return and blank.
	 */
	private Set<String> ignorableWhitespaces = null;

	/**
	 * Determine which whitespace characters are ignored and add to
	 * {@link STextualDS} object, but do not get an own {@link SToken} object. A
	 * list separated by ',' for instance /n,/r, ,... for linefeed, carriage
	 * return and blank.
	 */
	public Set<String> getIgnorableWhitespaces() {
		if (ignorableWhitespaces == null) {
			synchronized (this) {
				if (ignorableWhitespaces == null) {
					PepperModuleProperty<String> prop = (PepperModuleProperty<String>) this
							.getProperty(PROP_IGNORABLE_WHITESPACES);
					if (prop.getValue() != null) {
						ignorableWhitespaces = new HashSet<String>();
						String[] parts = prop.getValue().split(",");
						for (String part : parts) {
							ignorableWhitespaces.add(part.replace("'", ""));
						}
					}
				}
			}
		}
		return ignorableWhitespaces;
	}

	/**
	 * see {@link #PROP_ELEMENTNAME_AS_SANNO}
	 */
	private Collection<XPathExpression> elementNameAsSAnnoList = null;

	/**
	 * see {@link #PROP_ELEMENTNAME_AS_SANNO}
	 * 
	 * @return
	 */
	public Collection<XPathExpression> getElementNameAsSAnnoList() {
		if (elementNameAsSAnnoList == null) {
			synchronized (this) {
				if (elementNameAsSAnnoList == null) {
					Collection<XPathExpression> xPathList = extractXPathExpr(PROP_ELEMENTNAME_AS_SANNO);
					if (elementNameAsSAnnoList == null)
						elementNameAsSAnnoList = xPathList;
				}
			}
		}
		return elementNameAsSAnnoList;
	}

	private String separateToken = "";

	/**
	 * Property to set a separator between tokens, the default is a blank, but
	 * it can be set to any other character or even ot the empty String.
	 */
	public String getSeparateToken() {
		PepperModuleProperty<String> prop = (PepperModuleProperty<String>) this.getProperty(PROP_SEPARATE_TOKENS);
		if (prop.getValue() != null) {
			separateToken = prop.getValue();
		}
		return (separateToken);
	}
}
