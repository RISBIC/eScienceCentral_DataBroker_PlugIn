/*
 * Copyright (c) 2014: Arjuna Technologies Limited, Newcastle-upon-Tyne, England;
 *                     Newcastle University, Newcastle-upon-Tyne, England;
 *                     Red Hat Middleware LLC, Newcastle-upon-Tyne, England. All rights reserved.
 */
package org.risbic.plugins.esc.processor;

import com.arjuna.databroker.data.DataFlowNode;
import com.arjuna.databroker.data.DataFlowNodeFactory;
import com.arjuna.databroker.data.DataProcessor;
import com.arjuna.databroker.data.InvalidClassException;
import com.arjuna.databroker.data.InvalidMetaPropertyException;
import com.arjuna.databroker.data.InvalidNameException;
import com.arjuna.databroker.data.InvalidPropertyException;
import com.arjuna.databroker.data.MissingMetaPropertyException;
import com.arjuna.databroker.data.MissingPropertyException;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class EscParameterizedDataProcessorFactory implements DataFlowNodeFactory {
	private String _name;

	private Map<String, String> _properties;

	public EscParameterizedDataProcessorFactory(String name, Map<String, String> properties) {
		_name = name;
		_properties = properties;
	}

	@Override
	public String getName() {
		return _name;
	}

	@Override
	public Map<String, String> getProperties() {
		return _properties;
	}

	@Override
	public List<Class<? extends DataFlowNode>> getClasses() {
		List<Class<? extends DataFlowNode>> classes = new LinkedList<>();

		classes.add(DataProcessor.class);

		return classes;
	}

	@Override
	public <T extends DataFlowNode> List<String> getMetaPropertyNames(Class<T> dataFlowNodeClass) {
		return Collections.emptyList();
	}

	@Override
	public <T extends DataFlowNode> List<String> getPropertyNames(Class<T> dataFlowNodeClass, Map<String, String> metaProperties) throws InvalidClassException, InvalidMetaPropertyException, MissingMetaPropertyException {
		List<String> propertyNames = new LinkedList<>();

		propertyNames.add(EscParameterizedDataProcessor.SERVERHOST_PROPERTYNAME);
		propertyNames.add(EscParameterizedDataProcessor.SERVERPORT_PROPERTYNAME);
		propertyNames.add(EscParameterizedDataProcessor.USERNAME_PROPERTYNAME);
		propertyNames.add(EscParameterizedDataProcessor.USERPASSWORD_PROPERTYNAME);
		propertyNames.add(EscParameterizedDataProcessor.WORKFLOWNAME_PROPERTYNAME);

		return propertyNames;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends DataFlowNode> T createDataFlowNode(String name, Class<T> dataFlowNodeClass, Map<String, String> metaProperties, Map<String, String> properties) throws InvalidNameException, InvalidPropertyException, MissingPropertyException {
		if (dataFlowNodeClass.isAssignableFrom(EscDocumentDataProcessor.class)) {
			return (T) new EscParameterizedDataProcessor(name, properties);
		} else {
			return null;
		}
	}
}
