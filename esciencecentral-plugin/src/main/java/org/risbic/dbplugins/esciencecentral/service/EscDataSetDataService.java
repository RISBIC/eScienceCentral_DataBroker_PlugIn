/*
 * Copyright (c) 2014: Arjuna Technologies Limited, Newcastle-upon-Tyne, England;
 *                     Newcastle University, Newcastle-upon-Tyne, England;
 *                     Red Hat Middleware LLC, Newcastle-upon-Tyne, England. All rights reserved.
 */
package org.risbic.dbplugins.esciencecentral.service;

import com.arjuna.databroker.data.DataConsumer;
import com.arjuna.databroker.data.DataProvider;
import com.arjuna.databroker.data.DataService;
import org.risbic.dbplugins.esciencecentral.intraconnect.SimpleConsumer;
import org.risbic.dbplugins.esciencecentral.intraconnect.SimpleProvider;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EscDataSetDataService implements DataService {
	private static final Logger logger = Logger.getLogger(EscDataSetDataService.class.getName());

	public static final String SERVERHOST_PROPERTYNAME = "Server Host";

	public static final String SERVERPORT_PROPERTYNAME = "Server Port";

	public static final String USERNAME_PROPERTYNAME = "User Name";

	public static final String USERPASSWORD_PROPERTYNAME = "User Password";

	public static final String DATAFILENAME_PROPERTYNAME = "Data File Name";

	private String _name;

	private Map<String, String> _properties;

	private DataConsumer<String> _consumer;

	private DataProvider<String> _provider;

	public EscDataSetDataService(String name, Map<String, String> properties) {
		_name = name;
		_properties = properties;
		_consumer = new SimpleConsumer<>(this, "consume", String.class);
		_provider = new SimpleProvider<>(this);
	}

	@Override
	public String getName() {
		return _name;
	}

	@Override
	public Map<String, String> getProperties() {
		return Collections.unmodifiableMap(_properties);
	}

	public void consume(String data) {
		try {
			String serverHost = _properties.get(SERVERHOST_PROPERTYNAME);
			Integer serverPost = Integer.parseInt(_properties.get(SERVERPORT_PROPERTYNAME));
			String userName = _properties.get(USERNAME_PROPERTYNAME);
			String userPassword = _properties.get(USERPASSWORD_PROPERTYNAME);
			String dataFileName = _properties.get(DATAFILENAME_PROPERTYNAME);

			// TODO: Connect to eSC and store something in a data set
		} catch (Exception exception) {
			logger.log(Level.WARNING, "Unexpected problem while store workflow data file", exception);
		}
	}

	@Override
	public Collection<Class<?>> getDataProviderDataClasses() {
		Set<Class<?>> dataProviderDataClasses = new HashSet<>();

		dataProviderDataClasses.add(String.class);

		return dataProviderDataClasses;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> DataProvider<T> getDataProvider(Class<T> dataClass) {
		if (dataClass == String.class) {
			return (DataProvider<T>) _provider;
		} else {
			return null;
		}
	}

	@Override
	public Collection<Class<?>> getDataConsumerDataClasses() {
		Set<Class<?>> dataConsumerDataClasses = new HashSet<>();

		dataConsumerDataClasses.add(String.class);

		return dataConsumerDataClasses;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> DataConsumer<T> getDataConsumer(Class<T> dataClass) {
		if (dataClass == String.class) {
			return (DataConsumer<T>) _consumer;
		} else {
			return null;
		}
	}
}
