/*
 * Copyright (c) 2014: Arjuna Technologies Limited, Newcastle-upon-Tyne, England;
 *                     Newcastle University, Newcastle-upon-Tyne, England;
 *                     Red Hat Middleware LLC, Newcastle-upon-Tyne, England. All rights reserved.
 */
package org.risbic.plugins.esc.source.file;

import com.arjuna.databroker.data.DataProvider;
import com.arjuna.databroker.data.DataSource;
import com.connexience.api.StorageClient;
import com.connexience.api.model.EscDocument;
import com.connexience.api.model.EscFolder;
import org.risbic.plugins.esc.intraconnect.SimpleProvider;

import java.io.ByteArrayOutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EscFileDataSource implements DataSource {
	private static final Logger logger = Logger.getLogger(EscFileDataSource.class.getName());

	public static final String SERVERHOST_PROPERTYNAME = "Server Host";

	public static final String SERVERPORT_PROPERTYNAME = "Server Port";

	public static final String USERNAME_PROPERTYNAME = "User Name";

	public static final String USERPASSWORD_PROPERTYNAME = "User Password";

	public static final String DATAFILENAME_PROPERTYNAME = "File Name";

	private String _name;

	private Map<String, String> _properties;

	private SimpleProvider<String> _provider = new SimpleProvider<>(this);

	public EscFileDataSource(String name, Map<String, String> properties) {
		_name = name;
		_properties = properties;

		// TODO: Make this periodic / based on latest version
		produce();
	}

	@Override
	public String getName() {
		return _name;
	}

	@Override
	public Map<String, String> getProperties() {
		return Collections.unmodifiableMap(_properties);
	}

	public void produce() {
		try {
			String serverHost = _properties.get(SERVERHOST_PROPERTYNAME);
			Integer serverPost = Integer.parseInt(_properties.get(SERVERPORT_PROPERTYNAME));
			String userName = _properties.get(USERNAME_PROPERTYNAME);
			String userPassword = _properties.get(USERPASSWORD_PROPERTYNAME);
			String dataFileName = _properties.get(DATAFILENAME_PROPERTYNAME);

			StorageClient storageClient = new StorageClient(serverHost, serverPost, false, userName, userPassword);

			// Upload the data to document in home folder
			EscFolder homeFolder = storageClient.homeFolder();
			EscDocument[] documents = storageClient.folderDocuments(homeFolder.getId());

			for (final EscDocument escDocument : documents) {
				if (dataFileName.equals(escDocument.getName())) {

					ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

					storageClient.download(escDocument, outputStream);

					_provider.produce(outputStream.toString());
				}
			}
		} catch (Exception exception) {
			logger.log(Level.WARNING, "Unexpected problem while accessing eSC file", exception);
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
}
