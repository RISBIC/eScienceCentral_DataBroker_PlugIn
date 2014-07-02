/*
 * Copyright (c) 2014: Arjuna Technologies Limited, Newcastle-upon-Tyne, England;
 *                     Newcastle University, Newcastle-upon-Tyne, England;
 *                     Red Hat Middleware LLC, Newcastle-upon-Tyne, England. All rights reserved.
 */

package org.risbic.dbplugins.esciencecentral;

import java.util.Collections;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import com.arjuna.databroker.data.DataFlowNodeFactoryInventory;

@Startup
@Singleton
public class WorkflowFactoriesSetup
{
    @PostConstruct
    public void setup()
    {
        WorkflowDataProcessorFactory workflowDataProcessorFactory = new WorkflowDataProcessorFactory("e-Science Central Data Processor Factory", Collections.<String, String>emptyMap());
        WorkflowDataServiceFactory   workflowDataServiceFactory   = new WorkflowDataServiceFactory("e-Science Central Data Service Factory", Collections.<String, String>emptyMap());

        _dataFlowNodeFactoryInventory.addDataFlowNodeFactory(workflowDataProcessorFactory);
        _dataFlowNodeFactoryInventory.addDataFlowNodeFactory(workflowDataServiceFactory);
    }

    @PreDestroy
    public void cleanup()
    {
        _dataFlowNodeFactoryInventory.removeDataFlowNodeFactory("e-Science Central Data Processor Factory");
        _dataFlowNodeFactoryInventory.removeDataFlowNodeFactory("e-Science Central Data Service Factory");
    }

    @EJB(lookup="java:global/databroker/control-core/DataFlowNodeFactoryInventory")
    private DataFlowNodeFactoryInventory _dataFlowNodeFactoryInventory;
}