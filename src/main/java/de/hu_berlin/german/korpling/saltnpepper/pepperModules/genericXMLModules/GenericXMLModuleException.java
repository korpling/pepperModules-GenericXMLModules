package de.hu_berlin.german.korpling.saltnpepper.pepperModules.genericXMLModules;

import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperExceptions.PepperModuleException;

public class GenericXMLModuleException extends PepperModuleException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1250477034746343085L;

	public GenericXMLModuleException()
	{ super(); }
	
    public GenericXMLModuleException(String s)
    { super(s); }
    
	public GenericXMLModuleException(String s, Throwable ex)
	{super(s, ex); }
}
