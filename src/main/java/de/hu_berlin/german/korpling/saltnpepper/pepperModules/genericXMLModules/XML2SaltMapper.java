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
package de.hu_berlin.german.korpling.saltnpepper.pepperModules.genericXMLModules;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

import de.hu_berlin.german.korpling.saltnpepper.pepper.common.DOCUMENT_STATUS;
import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.impl.PepperMapperImpl;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.genericXMLModules.xpath.XPathExpression;
import de.hu_berlin.german.korpling.saltnpepper.salt.SaltFactory;
import de.hu_berlin.german.korpling.saltnpepper.salt.graph.Edge;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDocumentGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpan;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpanningRelation;
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
 * 
 * @author Florian Zipser
 * 
 */
public class XML2SaltMapper extends PepperMapperImpl {

	/**
	 * {@inheritDoc PepperMapper#setSDocument(SDocument)}
	 * 
	 * OVERRIDE THIS METHOD FOR CUSTOMIZED MAPPING.
	 */
	@Override
	public DOCUMENT_STATUS mapSCorpus() {
		return (DOCUMENT_STATUS.COMPLETED);
	}

	/**
	 * {@inheritDoc PepperMapper#setSDocument(SDocument)}
	 * 
	 * OVERRIDE THIS METHOD FOR CUSTOMIZED MAPPING.
	 */
	@Override
	public DOCUMENT_STATUS mapSDocument() {
		if (this.getSDocument().getSDocumentGraph() == null)
			this.getSDocument().setSDocumentGraph(SaltFactory.eINSTANCE.createSDocumentGraph());
		try {
			XMLReader reader = new XMLReader();
			this.readXMLResource(reader, this.getResourceURI());
		} catch (Exception e) {
			e.printStackTrace();
			return (DOCUMENT_STATUS.FAILED);
		}
		return (DOCUMENT_STATUS.COMPLETED);
	}

	/**
	 * Returns whether a given text only contains ignorable whitespace
	 * characters or not.
	 * 
	 * @param text
	 * @return
	 */
	public synchronized boolean onlyContainsIgnorableCharacters(String text) {
		Set<String> ignorableCharacters = ((GenericXMLImporterProperties) getProperties()).getIgnorableWhitespaces();
		for (Character ch : text.toCharArray()) {
			if (!ignorableCharacters.contains(ch.toString())) {
				return (false);
			}
		}
		return (true);
	}

	private class XMLReader extends DefaultHandler2 {
		/**
		 * Checks if the given {@link XPathExpression} xpr matches the given
		 * list of {@link XPathExpression} xprList
		 * 
		 * @return
		 */
		private boolean matches(Collection<XPathExpression> xprList, XPathExpression xpr) {
			if ((xprList != null) && (xprList.size() > 0)) {
				for (XPathExpression xpr2 : xprList) {
					if (XPathExpression.matches(xpr2, xpr))
						return (true);
				}
			}
			return (false);
		}

		/**
		 * Determines if this object is initialized.
		 */
		private boolean isInitialized = false;

		/**
		 * Initializes this object
		 */
		private void init() {
			this.currentXPath = new XPathExpression();
			this.currentSDS = SaltFactory.eINSTANCE.createSTextualDS();
			this.getsDocumentGraph().addSNode(currentSDS);
			this.elementNodeStack = new Stack<ElementNodeEntry>();
			this.isInitialized = true;
			this.sLayerStack = new Stack<SLayer>();
		}

		/**
		 * returns current {@link SDocumentGraph} to store all linguistic data
		 * in
		 * 
		 * @return
		 */
		public SDocumentGraph getsDocumentGraph() {
			return getSDocument().getSDocumentGraph();
		}

		/**
		 * stores the {@link XPathExpression} representing the path from root to
		 * current element.
		 */
		private XPathExpression currentXPath = null;
		/**
		 * current {@link STextualDS} object to store textual primary data.
		 */
		private STextualDS currentSDS = null;

		/**
		 * stack to store all information about the element node path from root
		 * to current element node
		 */
		private Stack<ElementNodeEntry> elementNodeStack = null;

		/**
		 * A class for storing information about an element node.
		 * 
		 * @author Florian Zipser
		 */
		private class ElementNodeEntry {
			/**
			 * Contains all {@link SNode} objects, which have been created, but
			 * not already added to the tree.
			 */
			public List<SNode> openSNodes = null;
			/**
			 * Temporary stores the name of an element-node. First it has to be
			 * checked if the current element-node is a {@link SToken} or not.
			 */
			public String nodeName = null;
			public SToken representedSToken = null;
			/**
			 * Stores all annotations corresponding to the current {@link SNode}
			 * object. This list can contain {@link SAnnotation} objects and
			 * {@link SMetaAnnotation} objects as well.
			 */
			public EList<SAbstractAnnotation> annotations = null;
			/**
			 * Determines if element node is a complex node (contains further
			 * element nodes)
			 */
			private Boolean isComplex = false;

			/**
			 * Returns if element node is a complex node (contains further
			 * element nodes)
			 * 
			 * @return
			 */
			public Boolean isComplex() {
				return isComplex;
			}

			/**
			 * Determines if element node is a complex node (contains further
			 * element nodes)
			 * 
			 * @param isComplex
			 */
			public void setIsComplex(Boolean isComplex) {
				this.isComplex = isComplex;
				if ((isComplex) && (representedSToken != null)) {
					if (!this.openSNodes.contains(representedSToken))
						this.openSNodes.add(representedSToken);

					if (elementNodeStack.size() > 1)
						elementNodeStack.get(elementNodeStack.size() - 2).openSNodes.remove(representedSToken);
					representedSToken = null;
				}
			}

			public ElementNodeEntry(final String nodeName, final EList<SAbstractAnnotation> annotations) {
				this.nodeName = nodeName;
				this.annotations = annotations;
				this.openSNodes = new Vector<SNode>();
			}

			public String toString() {
				return ("[" + nodeName + ", annotations: " + annotations + ", isComplex: " + this.isComplex() + ", openSNodes: " + this.openSNodes + "]");
			}
		}

		/**
		 * Adds the read text node to the current {@link STextualDS} node. If a
		 * text was found also a {@link SToken} node and a
		 * {@link STextualRelation} will be add to {@link SDocumentGraph}.
		 */
		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			if (!isInitialized){
				init();
			}

			currentXPath.addStep(XPathExpression.XML_TEXT);
			if (!this.matches(((GenericXMLImporterProperties) getProperties()).getIgnoreList(), currentXPath)) {
				// if text-node is not to ignore
				StringBuffer textBuf = new StringBuffer();
				for (int i = start; i < start + length; i++){
					textBuf.append(ch[i]);
				}
				String text = textBuf.toString();

				if (	(textBuf.length() > 0)&&
						(!onlyContainsIgnorableCharacters(text))) {
					textBuf = new StringBuffer();
					String containedText = currentSDS.getSText();
					if (containedText != null){
						textBuf.append(containedText);
						textBuf.append(((GenericXMLImporterProperties) getProperties()).getSeparateToken());
					}
					
					int sStart = textBuf.length();
					textBuf.append(text);
					
					currentSDS.setSText(textBuf.toString());
					int sEnd = textBuf.length();
					
					if (!((GenericXMLImporterProperties) getProperties()).isTextOnly()) {
						// create a new SToken object overlapping the current
						// text-node
						SToken sToken = SaltFactory.eINSTANCE.createSToken();
						sToken.setSName(this.elementNodeStack.peek().nodeName);
						getsDocumentGraph().addSNode(sToken);

						this.copySAbstractAnnotations(sToken);
						if (!this.sLayerStack.isEmpty()) {
							// add to sLayer if exist
							sToken.getSLayers().add(this.sLayerStack.peek());
						}// add to sLayer if exist
							// create a new SToken object overlapping the
							// current text-node
						// create a new STextualRelation object connecting the
						// SToken and the current STextualDS object
						STextualRelation sTextRel = SaltFactory.eINSTANCE.createSTextualRelation();
						sTextRel.setSToken(sToken);
						sTextRel.setSTextualDS(currentSDS);
						sTextRel.setSStart(sStart);
						sTextRel.setSEnd(sEnd);
						getsDocumentGraph().addSRelation(sTextRel);
						// create a new STextualRelation object connecting the
						// SToken and the current STextualDS object

						if (((GenericXMLImporterProperties) getProperties()).isCreateSStructure()) {
							// if prop for creating artificial structure is set
							SNode sNode = null;
							// must be removed and added later on for matches()
							currentXPath.removeLastStep();
							if (this.matches(((GenericXMLImporterProperties) getProperties()).getAsSpans(), currentXPath)) {
								sNode = SaltFactory.eINSTANCE.createSSpan();
							} else {
								sNode = SaltFactory.eINSTANCE.createSStructure();
							}
							currentXPath.addStep(XPathExpression.XML_TEXT);
							sNode.setSName("art");
							this.getsDocumentGraph().addSNode(sNode);

							SRelation sRel = null;
							if (sNode instanceof SSpan)
								sRel = SaltFactory.eINSTANCE.createSSpanningRelation();
							else if (sNode instanceof SStructure)
								sRel = SaltFactory.eINSTANCE.createSDominanceRelation();
							if (sRel != null) {
								sRel.setSSource(sNode);
								sRel.setSTarget(sToken);
								this.getsDocumentGraph().addSRelation(sRel);
								if (!this.sLayerStack.isEmpty()) {
									// add to sLayer if exist
									this.sLayerStack.peek().getSRelations().add(sRel);
								}// add to sLayer if exist
							}
							// copy all SAnnotations from SToken to artificial
							// SNode
							for (SAnnotation sAnno : sToken.getSAnnotations()) {
								sNode.addSAnnotation(sAnno);
							}
							// copy all SMetaAnnotations from SToken to
							// artificial SNode
							for (SMetaAnnotation sMetaAnno : sToken.getSMetaAnnotations()) {
								sNode.addSMetaAnnotation(sMetaAnno);
							}

							// put current node to open nodes of father node
							if (this.elementNodeStack.size() > 1)
								this.elementNodeStack.get(this.elementNodeStack.size() - 2).openSNodes.add(sNode);

							if (!this.sLayerStack.isEmpty()) {// add to sLayer
																// if exist
								this.sLayerStack.peek().getSNodes().add(sNode);
							}// add to sLayer if exist
						}// if prop for creating artificial structure is set
						else {// add token to list of open nodes of father
							this.elementNodeStack.peek().representedSToken = sToken;

							if (this.elementNodeStack.peek().isComplex())
								this.elementNodeStack.peek().openSNodes.add(sToken);
							else if (this.elementNodeStack.size() > 1)
								this.elementNodeStack.get(elementNodeStack.size() - 2).openSNodes.add(sToken);
						}// add token to list of open nodes of father
					}
				}
			}// if text-node is not to ignore
			currentXPath.removeLastStep();
		}

		/**
		 * Creates an artificial {@link SAnnotation} for the given
		 * {@link ElementNodeEntry} having the sName and sValue of
		 * {@link ElementNodeEntry#nodeName}.
		 * 
		 * @param entry
		 */
		private void createArtificialSAnno(ElementNodeEntry entry) {
			SAnnotation sAnno = SaltFactory.eINSTANCE.createSAnnotation();
			sAnno.setSName(entry.nodeName);
			sAnno.setSValue(entry.nodeName);
			if (entry.annotations == null)
				entry.annotations = new BasicEList<SAbstractAnnotation>();
			entry.annotations.add(sAnno);
		}

		/**
		 * a stack containing all active layers.
		 */
		private Stack<SLayer> sLayerStack = null;

		/**
		 * Creates a list of {@link SAbstractAnnotation} objects for the given
		 * {@link Attributes} object and the name of the element-node given by
		 * <em>nodeName</em> and returns them.
		 * 
		 * @param nodeName
		 * @param attributes
		 * @return
		 */
		private EList<SAbstractAnnotation> createSAbstractAnnotations(Class<? extends SAbstractAnnotation> clazz, String nodeName, Attributes attributes) {
			EList<SAbstractAnnotation> annoList = null;
			for (int i = 0; i < attributes.getLength(); i++) {// create
																// annotation
																// list
				String attName = attributes.getQName(i);
				if (annoList == null)
					annoList = new BasicEList<SAbstractAnnotation>();
				currentXPath.addStep("@" + attName);

				if (this.matches(((GenericXMLImporterProperties) getProperties()).getIgnoreList(), currentXPath))
					;// do nothing
				else if (this.matches(((GenericXMLImporterProperties) getProperties()).getSMetaAnnotationSDocumentList(), currentXPath)) {
					if (this.getsDocumentGraph().getSDocument() != null)
						this.getsDocumentGraph().getSDocument().createSAnnotation(null, attName, attributes.getValue(i));
				} else {// if element-node shall not be ignored
					SAbstractAnnotation sAnno = null;
					if (this.matches(((GenericXMLImporterProperties) getProperties()).getSMetaAnnotationList(), currentXPath))
						sAnno = SaltFactory.eINSTANCE.createSMetaAnnotation();
					else {
						if (SMetaAnnotation.class.equals(clazz))
							sAnno = SaltFactory.eINSTANCE.createSMetaAnnotation();
						else if (SAnnotation.class.equals(clazz))
							sAnno = SaltFactory.eINSTANCE.createSAnnotation();
					}
					if (sAnno != null) {
						// if xml-namespaces and prefixes shall not be mapped
						if ((((GenericXMLImporterProperties) getProperties()).isIgnoreNamespaces()) && (("xmlns".equals(attName))) || (attName.contains("xmlns")))
							;// do nothing
						else {
							String[] parts = attName.split(":");
							String sName;
							if (parts.length > 1)
								sName = parts[parts.length - 1];
							else
								sName = attName;

							if (this.matches(((GenericXMLImporterProperties) getProperties()).getPrefixedAnnoList(), currentXPath))
								sAnno.setSName(nodeName + "_" + sName);
							else
								sAnno.setSName(sName);
							sAnno.setSValue(attributes.getValue(i));
							annoList.add(sAnno);
						}
					}
				}// if element-node shall not be ignored
				currentXPath.removeLastStep();
			}// create annotation list
			return (annoList);
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

			if (!isInitialized)
				init();
			currentXPath.addStep(qName);

			if (this.matches(((GenericXMLImporterProperties) getProperties()).getSMetaAnnotationSDocumentList(), currentXPath)) {
				EList<SAbstractAnnotation> annoList = this.createSAbstractAnnotations(SMetaAnnotation.class, localName, attributes);
				if ((this.getsDocumentGraph().getSDocument() != null) && (annoList != null)) {
					for (SAbstractAnnotation sAnno : annoList)
						this.getsDocumentGraph().getSDocument().addSMetaAnnotation((SMetaAnnotation) sAnno);
				}
			} else if (this.matches(((GenericXMLImporterProperties) getProperties()).getSLayerList(), currentXPath)) {
				SLayer currSLayer = null;
				EList<SLayer> sLayers = this.getsDocumentGraph().getSLayerByName(qName);
				if ((sLayers != null) && (sLayers.size() > 0))
					currSLayer = sLayers.get(0);
				if (currSLayer == null) {
					currSLayer = SaltFactory.eINSTANCE.createSLayer();
					currSLayer.setSName(qName);
				}

				EList<SAbstractAnnotation> annoList = this.createSAbstractAnnotations(SMetaAnnotation.class, qName, attributes);
				if ((currSLayer != null) && (annoList != null) && (annoList.size() > 0)) {
					for (SAbstractAnnotation sAnno : annoList)
						currSLayer.addSMetaAnnotation((SMetaAnnotation) sAnno);
				}
				if (!this.sLayerStack.isEmpty())
					this.sLayerStack.peek().getSSubLayers().add(currSLayer);
				this.getsDocumentGraph().addSLayer(currSLayer);
				this.sLayerStack.push(currSLayer);
			} else if ((!this.matches(((GenericXMLImporterProperties) getProperties()).getIgnoreList(), currentXPath)) && (!((GenericXMLImporterProperties) getProperties()).isTextOnly())) {// if
																																																// element-node
																																																// shall
																																																// not
																																																// be
																																																// ignored
				if (this.elementNodeStack.size() > 0) {// notify parent element,
														// that it is complex
					this.elementNodeStack.peek().setIsComplex(true);
				}// notify parent element, that it is complex

				BasicEList<SAbstractAnnotation> annoList = null;
				if (attributes.getLength() > 0) {// if attribute nodes are
													// given, map them to
													// SAnnotation objects
					annoList = new BasicEList<SAbstractAnnotation>();
					annoList.addAll(this.createSAbstractAnnotations(SAnnotation.class, qName, attributes));
				}// if attribute nodes are given, map them to SAnnotation
					// objects
					// create ElementNodeEntry for current element node and add
					// to stack
				if (this.elementNodeStack.size() > 0)
					this.elementNodeStack.peek().setIsComplex(true);
				ElementNodeEntry elementNode = new ElementNodeEntry(qName, annoList);

				// creates an artificial SAnnotation out of the element name is
				// prop is set
				if (this.matches(((GenericXMLImporterProperties) getProperties()).getElementNameAsSAnnoList(), currentXPath))
					this.createArtificialSAnno(elementNode);

				this.elementNodeStack.push(elementNode);
			}// if element-node shall not be ignored
		}

		/**
		 * Cleans up the stack and removes first element. Cleans up current
		 * XPath and removes last element. Creates a new SNode of type
		 * {@link SSpan} or {@link SStructure}, with respect to flag
		 * {@link GenericXMLImporterProperties#PROP_AS_SPANS}.
		 */
		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if (this.matches(((GenericXMLImporterProperties) getProperties()).getSMetaAnnotationSDocumentList(), currentXPath))
				;
			else if (this.matches(((GenericXMLImporterProperties) getProperties()).getSLayerList(), currentXPath)) {
				if (!this.sLayerStack.isEmpty())
					this.sLayerStack.pop();
			} else if ((!this.matches(((GenericXMLImporterProperties) getProperties()).getIgnoreList(), currentXPath)) && (!((GenericXMLImporterProperties) getProperties()).isTextOnly())) {// if
																																																// element-node
																																																// shall
																																																// not
																																																// be
																																																// ignored
				if (this.elementNodeStack.peek().isComplex()) {// map a complex
																// node
					SNode sNode = null;
					if (this.matches(((GenericXMLImporterProperties) getProperties()).getAsSpans(), currentXPath))
						sNode = SaltFactory.eINSTANCE.createSSpan();
					else
						sNode = SaltFactory.eINSTANCE.createSStructure();
					sNode.setSName(qName);
					this.getsDocumentGraph().addSNode(sNode);
					// copy all annotations to sNode
					this.copySAbstractAnnotations(sNode);
					if (this.elementNodeStack.peek().openSNodes.size() > 0) {// put
																				// all
																				// open
																				// SToken
																				// objects
																				// into
																				// subtree
																				// of
																				// current
																				// tree
						for (SNode childSNode : this.elementNodeStack.peek().openSNodes) {
							SRelation sRel = null;
							if ((sNode instanceof SSpan) && (childSNode instanceof SToken)) {
								sRel = SaltFactory.eINSTANCE.createSSpanningRelation();
							} else if (sNode instanceof SStructure)
								sRel = SaltFactory.eINSTANCE.createSDominanceRelation();
							else if ((sNode instanceof SSpan) && (childSNode instanceof SSpan)) {

								EList<Edge> outEdges = this.getsDocumentGraph().getOutEdges(childSNode.getSId());
								if (outEdges != null) {
									for (Edge outEdge : outEdges) {
										if (outEdge instanceof SSpanningRelation) {
											SSpanningRelation sSpanRel = SaltFactory.eINSTANCE.createSSpanningRelation();
											sSpanRel.setSSource(sNode);
											sSpanRel.setSTarget(((SSpanningRelation) outEdge).getSToken());
											this.getsDocumentGraph().addSRelation(sSpanRel);
											if (!this.sLayerStack.isEmpty()) {// add
																				// to
																				// sLayer
																				// if
																				// exist
												this.sLayerStack.peek().getSRelations().add(sSpanRel);
											}// add to sLayer if exist
										}
									}
								}
							}
							// else if ( (sNode instanceof SSpan)&&
							// (childSNode instanceof SStructure))
							// {
							// }

							if (sRel != null) {
								sRel.setSSource(sNode);
								sRel.setSTarget(childSNode);
								this.getsDocumentGraph().addSRelation(sRel);
								if (!this.sLayerStack.isEmpty()) {// add to
																	// sLayer if
																	// exist
									this.sLayerStack.peek().getSRelations().add(sRel);
								}// add to sLayer if exist
							}
						}
					}// put all open SToken objects into subtree of current tree
						// put current node to open nodes of father node
					if (this.elementNodeStack.size() > 1)
						this.elementNodeStack.get(this.elementNodeStack.size() - 2).openSNodes.add(sNode);
					if (!this.sLayerStack.isEmpty()) {// add to sLayer if exist
						this.sLayerStack.peek().getSNodes().add(sNode);
					}// add to sLayer if exist
				}// map a complex node
				this.elementNodeStack.pop();
			}// if element-node shall not be ignored
			currentXPath.removeLastStep();
		}

		/**
		 * Copies all annotations being contained in
		 * {@link #currSAbstractAnnotations} to the given {@link SNode} object
		 * 
		 * @param sNode
		 */
		private void copySAbstractAnnotations(SNode sNode) {
			if (elementNodeStack.peek().annotations != null) {
				for (SAbstractAnnotation sAnno : elementNodeStack.peek().annotations) {
					if (sAnno instanceof SAnnotation)
						sNode.addSAnnotation((SAnnotation) sAnno);
					else if (sAnno instanceof SMetaAnnotation)
						sNode.addSMetaAnnotation((SMetaAnnotation) sAnno);
				}
			}
		}
	}
}
