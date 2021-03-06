/*
 * Copyright (c) 2014: Arjuna Technologies Limited, Newcastle-upon-Tyne, England;
 *                     Newcastle University, Newcastle-upon-Tyne, England;
 *                     Red Hat Middleware LLC, Newcastle-upon-Tyne, England. All rights reserved.
 */
package org.risbic.plugins.esc.service.file;

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
import com.connexience.api.StorageClient;
import com.connexience.api.model.EscDocument;
import com.connexience.api.model.EscFolder;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EscFileDataService implements DataService {
	private static final Logger logger = Logger.getLogger(EscFileDataService.class.getName());

	public static final String SERVERHOST_PROPERTYNAME = "Server Host";

	public static final String SERVERPORT_PROPERTYNAME = "Server Port";

	public static final String USERNAME_PROPERTYNAME = "User Name";

	public static final String USERPASSWORD_PROPERTYNAME = "User Password";

	public static final String DATAFILENAME_PROPERTYNAME = "Data File Name";

	private String _name;

	private Map<String, String> _properties;
	
	private DataFlow _dataFlow;

	@DataConsumerInjection(methodName="consume")
	private DataConsumer<String> _consumer;

	@DataProviderInjection
	private DataProvider<String> _provider;

	public EscFileDataService(String name, Map<String, String> properties) {
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
			String serverHost = _properties.get(SERVERHOST_PROPERTYNAME);
			Integer serverPost = Integer.parseInt(_properties.get(SERVERPORT_PROPERTYNAME));
			String userName = _properties.get(USERNAME_PROPERTYNAME);
			String userPassword = _properties.get(USERPASSWORD_PROPERTYNAME);
			String dataFileName = _properties.get(DATAFILENAME_PROPERTYNAME);

			StorageClient storageClient = new StorageClient(serverHost, serverPost, false, userName, userPassword);

			// Upload the data to document in home folder
			EscFolder homeFolder = storageClient.homeFolder();
			EscDocument document = storageClient.createDocumentInFolder(homeFolder.getId(), dataFileName);

			// Write contents and close
			InputStream dataStream = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
			storageClient.upload(document, dataStream, data.length());
			dataStream.close();
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
