/*
 * Copyright (c) 2014: Arjuna Technologies Limited, Newcastle-upon-Tyne, England;
 *                     Newcastle University, Newcastle-upon-Tyne, England;
 *                     Red Hat Middleware LLC, Newcastle-upon-Tyne, England. All rights reserved.
 */
package org.risbic.dbplugins.esciencecentral.processor;

import com.arjuna.databroker.data.DataConsumer;
import com.arjuna.databroker.data.DataProcessor;
import com.arjuna.databroker.data.DataProvider;
import com.connexience.api.StorageClient;
import com.connexience.api.WorkflowClient;
import com.connexience.api.model.EscDocument;
import com.connexience.api.model.EscDocumentVersion;
import com.connexience.api.model.EscFolder;
import com.connexience.api.model.EscWorkflow;
import com.connexience.api.model.EscWorkflowInvocation;
import org.risbic.dbplugins.esciencecentral.intraconnect.SimpleConsumer;
import org.risbic.dbplugins.esciencecentral.intraconnect.SimpleProvider;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EscDataProcessor implements DataProcessor {
	private static final Logger logger = Logger.getLogger(EscDataProcessor.class.getName());

	public static final String SERVERHOST_PROPERTYNAME = "Server Host";

	public static final String SERVERPORT_PROPERTYNAME = "Server Port";

	public static final String USERNAME_PROPERTYNAME = "User Name";

	public static final String USERPASSWORD_PROPERTYNAME = "User Password";

	public static final String WORKFLOWNAME_PROPERTYNAME = "Workflow Name";

	public static final String RESULTSFILENAME_PROPERTYNAME = "Results File Name";

	private final String _name;

	private final Map<String, String> _properties;

	private final DataConsumer<String> _consumer;

	private final DataProvider<String> _provider;

	public EscDataProcessor(String name, Map<String, String> properties) {
		_name = name;
		_properties = properties;
		_consumer = new SimpleConsumer<>(this, "consume", String.class);
		_provider = new SimpleProvider<>(this);
	}

	public String getName() {
		return _name;
	}

	public Map<String, String> getProperties() {
		return Collections.unmodifiableMap(_properties);
	}

	public void consume(String data) {
		String results = null;

		try {
			String serverHost = _properties.get(SERVERHOST_PROPERTYNAME);
			Integer serverPost = Integer.parseInt(_properties.get(SERVERPORT_PROPERTYNAME));
			String userName = _properties.get(USERNAME_PROPERTYNAME);
			String userPassword = _properties.get(USERPASSWORD_PROPERTYNAME);
			String workflowName = _properties.get(WORKFLOWNAME_PROPERTYNAME);
			String resultsFileName = _properties.get(RESULTSFILENAME_PROPERTYNAME);

			WorkflowClient workflowClient = new WorkflowClient(serverHost, serverPost, false, userName, userPassword);
			StorageClient storageClient = new StorageClient(serverHost, serverPost, false, userName, userPassword);

			// Get available workflows
			EscWorkflow[] workflows = workflowClient.listWorkflows();

			// Find the workflow with specified 'workflow name'
			EscWorkflow workflow = null;
			for (EscWorkflow currentWorkflow : workflows) {
				if (currentWorkflow.getName().equals(workflowName)) {
					workflow = currentWorkflow;
				}
			}

			if (workflow != null) {
				// Upload the data to document in home folder
				EscFolder homeFolder = storageClient.homeFolder();
				EscDocument document = storageClient.createDocumentInFolder(homeFolder.getId(), UUID.randomUUID().toString());
				InputStream dataStream = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
				EscDocumentVersion documentVersion = storageClient.upload(document, dataStream, data.length());
				dataStream.close();

				// Run the workflow on data
				EscWorkflowInvocation workflowInvocation = workflowClient.executeWorkflowOnDocument(workflow.getId(), documentVersion.getDocumentRecordId());

				// Poll until this workflow finishes (no timeout at the moment)
				while (workflowInvocation.getStatus().equals("Running") || workflowInvocation.getStatus().equals("Queued") || workflowInvocation.getStatus().equals("Debugging")) {
					logger.fine("Status = " + workflowInvocation.getStatus());
					workflowInvocation = workflowClient.getInvocation(workflowInvocation.getId());

					Thread.sleep(5000); // NOOOOOOOOOOO
				}

				logger.fine("Status : " + workflowInvocation.getStatus());

				// Find the results file
				EscDocument[] resultsDocuments = storageClient.folderDocuments(workflowInvocation.getId());
				for (EscDocument resultsDocument : resultsDocuments) {
					if (resultsDocument.getName().equals(resultsFileName)) {
						ByteArrayOutputStream resultsStream = new ByteArrayOutputStream();
						storageClient.download(resultsDocument, resultsStream);
						results = new String(resultsStream.toByteArray(), StandardCharsets.UTF_8);
					}
				}

				// Delete the uploaded data document
				storageClient.deleteDocument(documentVersion.getDocumentRecordId());
			} else {
				logger.warning("Could not find the workflow called \"" + workflowName + "\"");
			}
		} catch (Exception exception) {
			logger.log(Level.WARNING, "Unexpected problem while invoking workflow", exception);
		}

		logger.finer("Results : " + results);

		if (results != null) {
			_provider.produce(results);
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
