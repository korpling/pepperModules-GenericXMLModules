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
package de.hu_berlin.german.korpling.saltnpepper.pepperModules.genericXMLModules.xpath.tests;

import junit.framework.TestCase;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.genericXMLModules.xpath.XPathExpression;

public class XPathExpressionTest extends TestCase {

	private XPathExpression fixture = null;

	public void setFixture(XPathExpression fixture) {
		this.fixture = fixture;
	}

	public XPathExpression getFixture() {
		return fixture;
	}

	public void setUp() {
		this.setFixture(new XPathExpression());
	}

	/**
	 * test element-node
	 */
	public void testElementNode() {
		assertTrue(XPathExpression.matches(new XPathExpression("/element1/element2/element3"), new XPathExpression("/element1/element2/element3")));
		assertFalse(XPathExpression.matches(new XPathExpression("/element1/element2/element4"), new XPathExpression("/element1/element2/element3")));
		assertFalse(XPathExpression.matches(new XPathExpression("/element1/element4/element3"), new XPathExpression("/element1/element2/element3")));
	}

	/**
	 * test attribute-node
	 */
	public void testAttributeNode() {
		assertTrue(XPathExpression.matches(new XPathExpression("/element1/element2/@attribute1"), new XPathExpression("/element1/element2/@attribute1")));
		assertFalse(XPathExpression.matches(new XPathExpression("/element1/element2/@attribute2"), new XPathExpression("/element1/element2/@attribute1")));
	}

	/**
	 * test text-node
	 */
	public void testTextNode() {
		assertTrue(XPathExpression.matches(new XPathExpression("/element1/element2/test()"), new XPathExpression("/element1/element2/test()")));
		assertFalse(XPathExpression.matches(new XPathExpression("/element1/element3/test()"), new XPathExpression("/element1/element2/@attribute1")));
	}

	/**
	 * test element-node wildcard
	 */
	public void testElementNode_Wildcards() {
		assertTrue(XPathExpression.matches(new XPathExpression("//"), new XPathExpression("/element1/element2/element3")));
		assertTrue(XPathExpression.matches(new XPathExpression("//element3"), new XPathExpression("/element1/element2/element3")));
		assertTrue(XPathExpression.matches(new XPathExpression("element1//"), new XPathExpression("/element1/element2/element3")));
		assertFalse(XPathExpression.matches(new XPathExpression("//element1"), new XPathExpression("/element1/element2/element3")));
		assertFalse(XPathExpression.matches(new XPathExpression("//element2"), new XPathExpression("/element1/element2/element3")));
		assertTrue(XPathExpression.matches(new XPathExpression("//element2//"), new XPathExpression("/element1/element2/element3")));
		assertTrue(XPathExpression.matches(new XPathExpression("//element2//element4"), new XPathExpression("/element1/element2/element3/element4")));
	}

	/**
	 * test element-node wildcard and checks if it also works with using
	 * {@link XPathExpression#addStep(String)}
	 */
	public void testElementNode_Wildcards2() {
		XPathExpression xpr1 = new XPathExpression();
		xpr1.addStep("//");
		xpr1.addStep("head");
		xpr1.addStep("//");

		XPathExpression xpr2 = new XPathExpression();
		xpr2.addStep("text");
		xpr2.addStep("body");
		xpr2.addStep("div");
		xpr2.addStep("head");
		xpr2.addStep("foreign");

		assertTrue("xpr '" + xpr1 + "' shall match '" + xpr2 + "'", XPathExpression.matches(xpr1, xpr2));

		xpr2.removeLastStep();
		xpr2.removeLastStep();
		xpr2.addStep("p");

		assertFalse("xpr '" + xpr1 + "' shall not match '" + xpr2 + "'", XPathExpression.matches(xpr1, new XPathExpression("/text/body/div/p")));
	}

	/**
	 * test attribute-node wildcard
	 */
	public void testAttributeNode_Wildcard() {
		assertTrue(XPathExpression.matches(new XPathExpression("//@attribute1"), new XPathExpression("/element1/element2/@attribute1")));
	}

	/**
	 * test text-node wildcard
	 */
	public void testTextNode_Wildcard() {
		assertTrue(XPathExpression.matches(new XPathExpression("//text()"), new XPathExpression("/element1/element2/text()")));
	}
}
