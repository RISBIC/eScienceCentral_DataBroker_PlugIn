/*
 * Copyright (c) 2014: Arjuna Technologies Limited, Newcastle-upon-Tyne, England;
 *                     Newcastle University, Newcastle-upon-Tyne, England;
 *                     Red Hat Middleware LLC, Newcastle-upon-Tyne, England. All rights reserved.
 */
package org.risbic.plugins.esc;

import com.arjuna.databroker.data.DataFlowNodeFactoryInventory;
import org.risbic.plugins.esc.processor.EscDataProcessorFactory;
import org.risbic.plugins.esc.service.dataset.EscDataSetDataServiceFactory;
import org.risbic.plugins.esc.service.file.EscFileDataServiceFactory;
import org.risbic.plugins.esc.source.dataset.EscDataSetDataSourceFactory;
import org.risbic.plugins.esc.source.file.EscFileDataSourceFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.util.Collections;

@Startup
@Singleton
public class EscFactoriesSetup {
	private static final String PROCESSOR_FACTORY_NAME = "e-Science Central Data Processor Factory";

	private static final String FILE_SOURCE_FACTORY_NAME = "e-Science Central File Data Source Factory";

	private static final String DATASET_SOURCE_FACTORY_NAME = "e-Science Central Data Set Data Source Factory";

	private static final String FILE_SERVICE_FACTORY_NAME = "e-Science Central File Data Service Factory";

	private static final String DATASET_SERVICE_FACTORY_NAME = "e-Science Central Data Set Data Service Factory";

	@EJB(lookup = "java:global/databroker/control-core/DataFlowNodeFactoryInventory")
	private DataFlowNodeFactoryInventory _dataFlowNodeFactoryInventory;

	@PostConstruct
	public void setup() {
		EscDataProcessorFactory escDataProcessorFactory = new EscDataProcessorFactory(PROCESSOR_FACTORY_NAME, Collections.<String, String>emptyMap());

		EscFileDataSourceFactory escFileDataSourceFactory = new EscFileDataSourceFactory(FILE_SOURCE_FACTORY_NAME, Collections.<String, String>emptyMap());

		EscFileDataServiceFactory escFileDataServiceFactory = new EscFileDataServiceFactory(FILE_SERVICE_FACTORY_NAME, Collections.<String, String>emptyMap());

		EscDataSetDataSourceFactory escDataSetDataSourceFactory = new EscDataSetDataSourceFactory(DATASET_SOURCE_FACTORY_NAME, Collections.<String,String>emptyMap());

		EscDataSetDataServiceFactory escDataSetDataServiceFactory = new EscDataSetDataServiceFactory(DATASET_SERVICE_FACTORY_NAME, Collections.<String,String>emptyMap());

		_dataFlowNodeFactoryInventory.addDataFlowNodeFactory(escDataProcessorFactory);
		_dataFlowNodeFactoryInventory.addDataFlowNodeFactory(escFileDataSourceFactory);
		_dataFlowNodeFactoryInventory.addDataFlowNodeFactory(escFileDataServiceFactory);
		_dataFlowNodeFactoryInventory.addDataFlowNodeFactory(escDataSetDataSourceFactory);
		_dataFlowNodeFactoryInventory.addDataFlowNodeFactory(escDataSetDataServiceFactory);

	}

	@PreDestroy
	public void cleanup() {
		_dataFlowNodeFactoryInventory.removeDataFlowNodeFactory(PROCESSOR_FACTORY_NAME);
		_dataFlowNodeFactoryInventory.removeDataFlowNodeFactory(FILE_SOURCE_FACTORY_NAME);
		_dataFlowNodeFactoryInventory.removeDataFlowNodeFactory(FILE_SERVICE_FACTORY_NAME);
		_dataFlowNodeFactoryInventory.removeDataFlowNodeFactory(DATASET_SOURCE_FACTORY_NAME);
		_dataFlowNodeFactoryInventory.removeDataFlowNodeFactory(DATASET_SERVICE_FACTORY_NAME);
	}
}