/*
 * Copyright (c) 2014: Arjuna Technologies Limited, Newcastle-upon-Tyne, England;
 *                     Newcastle University, Newcastle-upon-Tyne, England;
 *                     Red Hat Middleware LLC, Newcastle-upon-Tyne, England. All rights reserved.
 */
package org.risbic.dbplugins.esciencecentral;

import com.arjuna.databroker.data.DataFlowNodeFactoryInventory;
import org.risbic.dbplugins.esciencecentral.processor.EscDataProcessorFactory;
import org.risbic.dbplugins.esciencecentral.service.EscDataServiceFactory;
import org.risbic.dbplugins.esciencecentral.source.EscDataSourceFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.util.Collections;

@Startup
@Singleton
public class EscFactoriesSetup {
	private static final String SOURCE_FACTORY_NAME = "e-Science Central Data Source Factory";

	private static final String PROCESSOR_FACTORY_NAME = "e-Science Central Data Processor Factory";

	private static final String SERVICE_FACTORY_NAME = "e-Science Central Data Service Factory";

	@EJB(lookup = "java:global/databroker/control-core/DataFlowNodeFactoryInventory")
	private DataFlowNodeFactoryInventory _dataFlowNodeFactoryInventory;

	@PostConstruct
	public void setup() {
		EscDataSourceFactory escDataSourceFactory = new EscDataSourceFactory(SOURCE_FACTORY_NAME, Collections.<String, String>emptyMap());
		EscDataProcessorFactory escDataProcessorFactory = new EscDataProcessorFactory(PROCESSOR_FACTORY_NAME, Collections.<String, String>emptyMap());
		EscDataServiceFactory escDataServiceFactory = new EscDataServiceFactory(SERVICE_FACTORY_NAME, Collections.<String, String>emptyMap());

		_dataFlowNodeFactoryInventory.addDataFlowNodeFactory(escDataSourceFactory);
		_dataFlowNodeFactoryInventory.addDataFlowNodeFactory(escDataProcessorFactory);
		_dataFlowNodeFactoryInventory.addDataFlowNodeFactory(escDataServiceFactory);
	}

	@PreDestroy
	public void cleanup() {
		_dataFlowNodeFactoryInventory.removeDataFlowNodeFactory(SOURCE_FACTORY_NAME);
		_dataFlowNodeFactoryInventory.removeDataFlowNodeFactory(PROCESSOR_FACTORY_NAME);
		_dataFlowNodeFactoryInventory.removeDataFlowNodeFactory(SERVICE_FACTORY_NAME);
	}
}