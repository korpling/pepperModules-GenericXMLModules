package de.hu_berlin.german.korpling.saltnpepper.pepperModules.genericXMLModules.xpath.tests;
import java.util.List;
import java.util.Vector;

import de.hu_berlin.german.korpling.saltnpepper.pepperModules.genericXMLModules.xpath.XPathExpression;

import junit.framework.TestCase;


public class XPathExpressionTest extends TestCase {

	private XPathExpression fixture= null;
	public void setFixture(XPathExpression fixture) {
		this.fixture = fixture;
	}

	public XPathExpression getFixture() {
		return fixture;
	}
	public void setUp()
	{
		this.setFixture(new XPathExpression());
	}

	/**
	 * test element-node
	 */
	public void testElementNode()
	{
		assertTrue(XPathExpression.matches(new XPathExpression("/element1/element2/element3"), new XPathExpression("/element1/element2/element3")));
		assertFalse(XPathExpression.matches(new XPathExpression("/element1/element2/element4"), new XPathExpression("/element1/element2/element3")));
		assertFalse(XPathExpression.matches(new XPathExpression("/element1/element4/element3"), new XPathExpression("/element1/element2/element3")));
	}
	
	/**
	 * test attribute-node 
	 */
	public void testAttributeNode()
	{
		assertTrue(XPathExpression.matches(new XPathExpression("/element1/element2/@attribute1"), new XPathExpression("/element1/element2/@attribute1")));
		assertFalse(XPathExpression.matches(new XPathExpression("/element1/element2/@attribute2"), new XPathExpression("/element1/element2/@attribute1")));
	}
	
	/**
	 * test text-node 
	 */
	public void testTextNode()
	{
		assertTrue(XPathExpression.matches(new XPathExpression("/element1/element2/test()"), new XPathExpression("/element1/element2/test()")));
		assertFalse(XPathExpression.matches(new XPathExpression("/element1/element3/test()"), new XPathExpression("/element1/element2/@attribute1")));
	}
	
	/**
	 * test element-node wildcard
	 */
	public void testElementNode_Wildcards()
	{
		assertTrue(XPathExpression.matches(new XPathExpression("//"), new XPathExpression("/element1/element2/element3")));
		assertTrue(XPathExpression.matches(new XPathExpression("//element3"), new XPathExpression("/element1/element2/element3")));
		assertTrue(XPathExpression.matches(new XPathExpression("element1//"), new XPathExpression("/element1/element2/element3")));
		assertFalse(XPathExpression.matches(new XPathExpression("//element1"), new XPathExpression("/element1/element2/element3")));
		assertFalse(XPathExpression.matches(new XPathExpression("//element2"), new XPathExpression("/element1/element2/element3")));
		assertTrue(XPathExpression.matches(new XPathExpression("//element2//"), new XPathExpression("/element1/element2/element3")));
		assertTrue(XPathExpression.matches(new XPathExpression("//element2//element4"), new XPathExpression("/element1/element2/element3/element4")));
	}
	
	/**
	 * test attribute-node wildcard
	 */
	public void testAttributeNode_Wildcard()
	{
		assertTrue(XPathExpression.matches(new XPathExpression("//@attribute1"), new XPathExpression("/element1/element2/@attribute1")));
	}
	
	/**
	 * test text-node wildcard
	 */
	public void testTextNode_Wildcard()
	{
		assertTrue(XPathExpression.matches(new XPathExpression("//text()"), new XPathExpression("/element1/element2/text()")));
	}
}
