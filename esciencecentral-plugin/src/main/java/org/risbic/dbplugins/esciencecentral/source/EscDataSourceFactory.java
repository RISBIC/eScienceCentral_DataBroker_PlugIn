/*
 * Copyright (c) 2014: Arjuna Technologies Limited, Newcastle-upon-Tyne, England;
 *                     Newcastle University, Newcastle-upon-Tyne, England;
 *                     Red Hat Middleware LLC, Newcastle-upon-Tyne, England. All rights reserved.
 */
package org.risbic.dbplugins.esciencecentral.source;

import com.arjuna.databroker.data.DataFlowNode;
import com.arjuna.databroker.data.DataFlowNodeFactory;
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

public class EscDataSourceFactory implements DataFlowNodeFactory {
	private String _name;

	private Map<String, String> _properties;

	public EscDataSourceFactory(String name, Map<String, String> properties) {
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

		classes.add(EscFileDataSource.class);
		classes.add(EscDataSetDataSource.class);

		return classes;
	}

	@Override
	public <T extends DataFlowNode> List<String> getMetaPropertyNames(Class<T> dataFlowNodeClass) {
		return Collections.emptyList();
	}

	@Override
	public <T extends DataFlowNode> List<String> getPropertyNames(Class<T> dataFlowNodeClass, Map<String, String> metaProperties) throws InvalidClassException, InvalidMetaPropertyException, MissingMetaPropertyException {
		List<String> propertyNames = new LinkedList<>();

		propertyNames.add(EscFileDataSource.SERVERHOST_PROPERTYNAME);
		propertyNames.add(EscFileDataSource.SERVERPORT_PROPERTYNAME);
		propertyNames.add(EscFileDataSource.USERNAME_PROPERTYNAME);
		propertyNames.add(EscFileDataSource.USERPASSWORD_PROPERTYNAME);
		propertyNames.add(EscFileDataSource.DATAFILENAME_PROPERTYNAME);

		return propertyNames;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends DataFlowNode> T createDataFlowNode(String name, Class<T> dataFlowNodeClass, Map<String, String> metaProperties, Map<String, String> properties) throws InvalidNameException, InvalidPropertyException, MissingPropertyException {
		if (dataFlowNodeClass.isAssignableFrom(EscFileDataSource.class)) {
			return (T) new EscFileDataSource(name, properties);
		} else if (dataFlowNodeClass.isAssignableFrom(EscDataSetDataSource.class)) {
			return (T) new EscDataSetDataSource(name, properties);
		} else {
			return null;
		}
	}
}
