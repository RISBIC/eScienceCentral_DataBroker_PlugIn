/*
 * Copyright (c) 2014: Arjuna Technologies Limited, Newcastle-upon-Tyne, England;
 *                     Newcastle University, Newcastle-upon-Tyne, England;
 *                     Red Hat Middleware LLC, Newcastle-upon-Tyne, England. All rights reserved.
 */
package org.risbic.plugins.esc.tests.processor;

import com.connexience.api.model.EscWorkflowParameterList;
import org.junit.Test;
import static org.junit.Assert.*;

public class EscWorkflowParameterListTest
{
	@Test
	public void formatTest()
	{
		EscWorkflowParameterList workflowParameterList = new EscWorkflowParameterList();
		workflowParameterList.addParameter("TestBlockName0", "TestPropertyName0", "TestPropertyValue0");
		workflowParameterList.addParameter("TestBlockName1", "TestPropertyName1", "TestPropertyValue1");

		assertEquals("JSON Format Change", "{\"values\":[{\"name\":\"TestPropertyName0\",\"blockName\":\"TestBlockName0\",\"value\":\"TestPropertyValue0\"},{\"name\":\"TestPropertyName1\",\"blockName\":\"TestBlockName1\",\"value\":\"TestPropertyValue1\"}]}", workflowParameterList.toJsonObject().toString());
	}
}
