/*
 * Copyright (c) 2014: Arjuna Technologies Limited, Newcastle-upon-Tyne, England;
 *                     Newcastle University, Newcastle-upon-Tyne, England;
 *                     Red Hat Middleware LLC, Newcastle-upon-Tyne, England. All rights reserved.
 */
package org.risbic.plugins.esc.processor;

import com.arjuna.databroker.data.DataConsumer;
import com.arjuna.databroker.data.DataFlow;
import com.arjuna.databroker.data.DataProcessor;
import com.arjuna.databroker.data.DataProvider;
import com.arjuna.databroker.data.IllegalStateException;
import com.arjuna.databroker.data.InvalidDataFlowException;
import com.arjuna.databroker.data.InvalidNameException;
import com.arjuna.databroker.data.InvalidPropertyException;
import com.arjuna.databroker.data.MissingPropertyException;
import com.arjuna.databroker.data.jee.annotation.DataConsumerInjection;
import com.arjuna.databroker.data.jee.annotation.DataProviderInjection;
import com.connexience.api.WorkflowClient;
import com.connexience.api.model.EscWorkflow;
import com.connexience.api.model.EscWorkflowInvocation;
import com.connexience.api.model.EscWorkflowParameterList;
import com.connexience.api.model.json.JSONObject;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EscParameterizedDataProcessor implements DataProcessor {
	private static final Logger logger = Logger.getLogger(EscParameterizedDataProcessor.class.getName());

	public static final String SERVERHOST_PROPERTYNAME = "Server Host";

	public static final String SERVERPORT_PROPERTYNAME = "Server Port";

	public static final String USERNAME_PROPERTYNAME = "User Name";

	public static final String USERPASSWORD_PROPERTYNAME = "User Password";

	public static final String WORKFLOWNAME_PROPERTYNAME = "Workflow Name";

	private String _name;

	private Map<String, String> _properties;
	
	private DataFlow _dataFlow;

	@DataConsumerInjection(methodName="consume")
	private DataConsumer<String> _consumer;

	@DataProviderInjection
	private DataProvider<String> _provider;

	public EscParameterizedDataProcessor(String name, Map<String, String> properties) {
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
			String workflowName = _properties.get(WORKFLOWNAME_PROPERTYNAME);

			WorkflowClient workflowClient = new WorkflowClient(serverHost, serverPost, false, userName, userPassword);

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
				JSONObject               parameterListJSON     = new JSONObject(data);
				EscWorkflowParameterList workflowParameterList = new EscWorkflowParameterList(parameterListJSON);

				// Run the workflow on data
				EscWorkflowInvocation workflowInvocation = workflowClient.executeWorkflowWithParameters(workflow.getId(), workflowParameterList);

				// Poll until this workflow finishes (no timeout at the moment)
				while (workflowInvocation.isInProgress()) {
					logger.fine("Status = " + workflowInvocation.getStatus());
					workflowInvocation = workflowClient.getInvocation(workflowInvocation.getId());

					if (workflowInvocation.isInProgress())
					    Thread.sleep(5000);
				}

				logger.fine("Status : " + workflowInvocation.getStatus());

				_provider.produce(null);
			} else {
				logger.warning("Could not find the workflow called \"" + workflowName + "\"");
			}
		} catch (Exception exception) {
			logger.log(Level.WARNING, "Unexpected problem while invoking workflow", exception);
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
