/*
 * Copyright (c) 2014: Arjuna Technologies Limited, Newcastle-upon-Tyne, England;
 *                     Newcastle University, Newcastle-upon-Tyne, England;
 *                     Red Hat Middleware LLC, Newcastle-upon-Tyne, England. All rights reserved.
 */
package org.risbic.plugins.esc.service.dataset;

import com.arjuna.databroker.data.DataConsumer;
import com.arjuna.databroker.data.DataFlow;
import com.arjuna.databroker.data.DataProvider;
import com.arjuna.databroker.data.DataService;
import com.arjuna.databroker.data.IllegalStateException;
import com.arjuna.databroker.data.InvalidDataFlowException;
import com.arjuna.databroker.data.InvalidNameException;
import com.arjuna.databroker.data.InvalidPropertyException;
import com.arjuna.databroker.data.MissingPropertyException;
import com.arjuna.databroker.data.jee.annotation.DataConsumerInjection;
import com.arjuna.databroker.data.jee.annotation.DataProviderInjection;
import com.connexience.api.DatasetClient;
import com.connexience.api.model.EscDataset;
import com.connexience.api.model.EscDatasetItem;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EscDataSetDataService implements DataService {
	private static final Logger logger = Logger.getLogger(EscDataSetDataService.class.getName());

	public static final String SERVER_HOST = "Server Host";

	public static final String SERVER_PORT = "Server Port";

	public static final String ESC_USER = "User Name";

	public static final String ESC_PASSWORD = "User Password";

	public static final String DATA_SET_NAME = "Data Set Name";

	private String _name;

	private Map<String, String> _properties;

	private DataFlow _dataFlow;

	@DataConsumerInjection(methodName="consume")
	private DataConsumer<String> _consumer;

	@DataProviderInjection
	private DataProvider<String> _provider;

	public EscDataSetDataService(String name, Map<String, String> properties) {
		_name = name;
		_properties = properties;
	}


	@Override
	public String getName() {
		return _name;
	}

	@Override
	public void setName(String name) throws IllegalStateException,
			InvalidNameException {
		_name = name;
	}

	@Override
	public Map<String, String> getProperties() {
		return Collections.unmodifiableMap(_properties);
	}

	@Override
	public void setProperties(Map<String, String> properties)
			throws IllegalStateException, InvalidPropertyException,
			MissingPropertyException {
		_properties = properties;		
	}

	@Override
	public DataFlow getDataFlow() {
		return _dataFlow;
	}

	@Override
	public void setDataFlow(DataFlow dataFlow) throws IllegalStateException,
			InvalidDataFlowException {
		_dataFlow = dataFlow;
	}

	public void consume(String data) {
		try {
			String serverHost = _properties.get(SERVER_HOST);
			Integer serverPost = Integer.parseInt(_properties.get(SERVER_PORT));
			String userName = _properties.get(ESC_USER);
			String userPassword = _properties.get(ESC_PASSWORD);
			String dataSetName = _properties.get(DATA_SET_NAME);

			DatasetClient datasetClient = new DatasetClient(serverHost, serverPost, false, userName, userPassword);

			final EscDataset dataset = datasetClient.getNamedDataset(dataSetName);
			final EscDatasetItem[] items = datasetClient.listDatasetContents(dataset.getId());

			if (items.length > 0) {
				final EscDatasetItem item = items[0];

				datasetClient.appendToMultipleValueDatasetItem(dataset.getId(), item.getName(), "{\"data\":\"" + data + "\"}");
			}
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
