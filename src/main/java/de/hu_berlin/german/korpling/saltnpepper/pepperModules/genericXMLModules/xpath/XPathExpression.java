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
package de.hu_berlin.german.korpling.saltnpepper.pepperModules.genericXMLModules.xpath;

import java.util.List;
import java.util.Vector;

import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.exceptions.PepperModuleException;

/**
 * Splits an XPath expression into steps. The main function of this class is to
 * compare two XPath expressions, if one expression is mathes the other one. For
 * instance the XPath expression //element1 matches the XPath expression
 * element2/element1. Only wildcards are resolved and only shortcut syntax for
 * descendant axis is supported.
 * 
 * @author Florian Zipser
 * 
 */
// TODO replace this class with an existing library to compare XPath expressions
public class XPathExpression {

	/**
	 * An alias for a wildcard. This alias is used to replace a removed // in an
	 * XPath expression.
	 */
	public static final String XML_WILDCARD_ALIAS = "WILDCARD";
	public static final String XML_TEXT = "text()";
	/**
	 * steps of this expression
	 */
	private List<String> steps = null;

	/**
	 * Initializes this object and calls {@link #setXPathExpression(String)}.
	 * 
	 * @param xPathExpression
	 */
	public XPathExpression(String xPathExpression) {
		this.setXPathExpression(xPathExpression);
	}

	/**
	 * Initializes this object.
	 */
	public XPathExpression() {
	}

	/**
	 * Splits an XPath expression into steps.
	 * 
	 * @param xPathExpression
	 */
	public void setXPathExpression(String xPathExpression) {
		if (xPathExpression == null)
			throw new NullPointerException("an empty XPathExpression was given.");

		xPathExpression = xPathExpression.replace("//", "/" + XML_WILDCARD_ALIAS + "/");
		if (xPathExpression.startsWith("/"))
			xPathExpression = xPathExpression.replaceFirst("/", "");
		steps = new Vector<String>();
		String[] parts = xPathExpression.split("/");
		if (parts.length > 0) {
			for (String part : parts) {
				steps.add(part);
			}
		}
	}

	/**
	 * Retuns XPath expression steps.
	 * 
	 * @return
	 */
	public List<String> getSteps() {
		return (steps);
	}

	/**
	 * Checks if the given XPath expression matches the expression hold by this
	 * object.
	 * 
	 * @param xPathExpression
	 * @return
	 */
	public Boolean matches(String xPathExpressionStr) {
		XPathExpression xPathExpression = new XPathExpression(xPathExpressionStr);
		return (this.matches(xPathExpression));
	}

	/**
	 * Checks if the given XPath expression matches the expression hold by this
	 * object.
	 * 
	 * @param xPathExpression
	 * @return
	 */
	public Boolean matches(XPathExpression xPathExpression) {
		if (xPathExpression == null)
			throw new NullPointerException("Cannot math against an empty XPath expression.");
		return (matches(this, xPathExpression));

	}

	/**
	 * Checks if the given XPath expression matches the expression hold by this
	 * object.
	 * 
	 * @param xPathExpression1
	 *            the one containing wildcards
	 * @return
	 */
	public static Boolean matches(XPathExpression xPathExpression1, XPathExpression xPathExpression2) {
		if ((xPathExpression1 == null) || (xPathExpression2 == null))
			throw new PepperModuleException("Cannot match against an empty XPath expression.");

		boolean goOn = true;
		int step1 = 0;
		int step2 = 0;
		boolean equal = true;
		while (goOn) {
			if (step2 == xPathExpression2.getSteps().size()) {
				if (step1 == xPathExpression1.getSteps().size())
					equal = true;
				else
					equal = false;
				break;
			}
			if (step1 == xPathExpression1.getSteps().size()) {
				if (step2 == xPathExpression2.getSteps().size())
					equal = true;
				else
					equal = false;
				break;
			}

			if (xPathExpression1.getSteps().get(step1).equals(xPathExpression2.getSteps().get(step2)))
				step1++;
			else if (XML_WILDCARD_ALIAS.toString().equals(xPathExpression1.getSteps().get(step1))) {// if
																									// current
																									// step
																									// in
																									// xPathExpression1
																									// is
																									// wildcard
				if (step1 == xPathExpression1.getSteps().size() - 1) {// wildcard
																		// is
																		// last
																		// step
																		// of
																		// xPathExpression1
																		// if
																		// last
																		// step
																		// in
																		// xPathExpression2
																		// is
																		// attribute
																		// or
																		// text
																		// node
																		// expressions
																		// do
																		// not
																		// match
					if ((xPathExpression2.getSteps().get(xPathExpression2.getSteps().size() - 1).startsWith("@")) || ("text()".equals(xPathExpression2.getSteps().get(xPathExpression2.getSteps().size() - 1))))
						equal = false;
					else
						equal = true;
					break;
				}// wildcard is last step of xPathExpression1
				if (!xPathExpression1.getSteps().get(step1 + 1).equals(xPathExpression2.getSteps().get(step2)))
					equal = false;
				else {
					equal = true;
					step1 = step1 + 2;
				}
			}// if current step in xPathExpression1 is wildcard
			else if (!xPathExpression1.getSteps().get(step1).equals(xPathExpression2.getSteps().get(step2)))
				equal = false;
			step2++;
		}
		return (equal);
	}

	/**
	 * Adds a step to the current expression. the new step will be set to the
	 * end.
	 * 
	 * @param step
	 */
	public void addStep(String step) {
		if (steps == null)
			steps = new Vector<String>();
		if ((step != null) && (!step.isEmpty())) {
			if ("//".equals(step))
				steps.add(XML_WILDCARD_ALIAS);
			else
				steps.add(step);
		}
	}

	/**
	 * Removes the last step of this expression.
	 */
	public void removeLastStep() {
		if ((steps != null) && (steps.size() > 0))
			this.steps.remove(steps.size() - 1);
	}

	public String toString() {
		if (steps != null)
			return (this.steps.toString());
		else
			return ("[]");
	}
}
