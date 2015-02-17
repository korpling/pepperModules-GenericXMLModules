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
package de.hu_berlin.german.korpling.saltnpepper.pepperModules.genericXMLModules;

import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.osgi.service.component.annotations.Component;

import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.PepperImporter;
import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.PepperMapper;
import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.exceptions.PepperModuleException;
import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.impl.PepperImporterImpl;
import de.hu_berlin.german.korpling.saltnpepper.salt.SaltFactory;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SElementId;

/**
 * Imports data coming from any XML file. The textual content of an element will
 * be interpreted as a sequence of primary data. When processing the file, the
 * importer will concatenate all these texts to an entire primary text.
 * 
 * @author Florian Zipser
 * @version 1.0
 * 
 */
@Component(name = "GenericXMLImporterComponent", factory = "PepperImporterComponentFactory")
public class GenericXMLImporter extends PepperImporterImpl implements PepperImporter {
	public GenericXMLImporter() {
		super();
		this.setName("GenericXMLImporter");
		this.addSupportedFormat(ENDING_XML, "1.0", null);
		this.setProperties(new GenericXMLImporterProperties());
	}

	/**
	 * Called by Pepper framework and initializes supported file endings.
	 */
	@Override
	public boolean isReadyToStart() {
		Boolean retVal = super.isReadyToStart();
		List<String> fileEndings = ((GenericXMLImporterProperties) this.getProperties()).getFileEndings();

		if ((fileEndings == null) || (fileEndings.size() == 0)) {
			this.getSDocumentEndings().add(ENDING_XML);
		} else if (fileEndings.contains(GenericXMLImporterProperties.KW_ALL)) {
			this.getSDocumentEndings().add(ENDING_ALL_FILES);
		} else if ((fileEndings != null) && (!fileEndings.contains(GenericXMLImporterProperties.KW_ALL))) {
			this.getSDocumentEndings().addAll(fileEndings);
		}
		return (retVal);
	}

	/**
	 * Creates a mapper of type {@link PAULA2SaltMapper}. {@inheritDoc
	 * PepperModule#createPepperMapper(SElementId)}
	 */
	@Override
	public PepperMapper createPepperMapper(SElementId sElementId) {
		XML2SaltMapper mapper = new XML2SaltMapper();
		URI resource = getSElementId2ResourceTable().get(sElementId);
		mapper.setResourceURI(resource);
		return (mapper);
	}
}
