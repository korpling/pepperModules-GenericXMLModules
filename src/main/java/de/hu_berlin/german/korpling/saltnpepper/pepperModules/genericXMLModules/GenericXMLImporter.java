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

import java.io.IOException;
import java.util.Map;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.osgi.service.component.annotations.Component;

import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperExceptions.PepperModuleException;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.PepperImporter;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.impl.PepperImporterImpl;
import de.hu_berlin.german.korpling.saltnpepper.salt.SaltFactory;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpusGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SElementId;

/**
 * Imports data coming from any XML file. The textual content of an element will be interpreted as a sequence of primary data. When processing the file, the importer will concatenate all
 * these texts to an entire primary text.
 * 
 * @author Florian Zipser
 * @version 1.0
 *
 */
@Component(name="GenericXMLImporterComponent", factory="PepperImporterComponentFactory")
public class GenericXMLImporter extends PepperImporterImpl implements PepperImporter
{
	public GenericXMLImporter()
	{
		super();
		this.name= "GenericXMLImporter";
		this.addSupportedFormat("xml", "1.0", null);
		this.setProperties(new GenericXMLImporterProperties());
	}
	
	/**
	 * A table to map physical resources on disk to logical Salt objects (see {@link SDocument}).
	 */
	private Map<SElementId, URI> documentResourceTable= null;
	
	/**
	 * This method is called by Pepper at the start of conversion process. 
	 * It shall create the structure the corpus to import. That means creating all necessary SCorpus, 
	 * SDocument and all Relation-objects between them. The path tp the corpus to import is given by
	 * this.getCorpusDefinition().getCorpusPath().
	 * @param an empty graph given by Pepper, which shall contains the corpus structure
	 */
	@Override
	public void importCorpusStructure(SCorpusGraph sCorpusGraph)
			throws PepperModuleException
	{
		EList<String> fileEndings= ((GenericXMLImporterProperties)this.getProperties()).getFileEndings();
		if (fileEndings.contains(GenericXMLImporterProperties.KW_ALL))
			fileEndings= new BasicEList<String>();
		
		this.setSCorpusGraph(sCorpusGraph);
		
		try {
			this.documentResourceTable= this.createCorpusStructure(this.getCorpusDefinition().getCorpusPath(), null, fileEndings);
		} catch (IOException e) {
			throw new PepperModuleException("Cannot import corpus-structure. ", e);
		}
	}
	
	/**
	 * This method is called by method start() of superclass PepperImporter, if the method was not overwritten
	 * by the current class. If this is not the case, this method will be called for every document which has
	 * to be processed.
	 * @param sElementId the id value for the current document or corpus to process  
	 */
	@Override
	public void start(SElementId sElementId) throws PepperModuleException 
	{
		if (	(sElementId!= null) &&
				(sElementId.getSIdentifiableElement()!= null) &&
				((sElementId.getSIdentifiableElement() instanceof SDocument)))
		{//only if given sElementId belongs to an object of type SDocument or SCorpus	
			SDocument sDocument= (SDocument)sElementId.getIdentifiableElement();
			XML2SaltMapper mapper= new XML2SaltMapper();
			mapper.setProps((GenericXMLImporterProperties)this.getProperties());
			if (sDocument.getSDocumentGraph()==  null)
				sDocument.setSDocumentGraph(SaltFactory.eINSTANCE.createSDocumentGraph());
			mapper.setsDocumentGraph(sDocument.getSDocumentGraph());
			URI sDocumentUri= documentResourceTable.get(sElementId);
			if (sDocumentUri== null)
				throw new GenericXMLModuleException("No uri was found for SElementId '"+sElementId+"' in mapping table '"+documentResourceTable+"'. No entry has been generated during importCorpusStructure-phase. This might be a bug.");
			this.readXMLResource(mapper, sDocumentUri);
		}//only if given sElementId belongs to an object of type SDocument or SCorpus
	}
}
