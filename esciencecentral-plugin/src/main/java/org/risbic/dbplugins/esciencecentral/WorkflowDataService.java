/*
 * Copyright (c) 2014: Arjuna Technologies Limited, Newcastle-upon-Tyne, England;
 *                     Newcastle University, Newcastle-upon-Tyne, England;
 *                     Red Hat Middleware LLC, Newcastle-upon-Tyne, England. All rights reserved.
 */

package org.risbic.dbplugins.esciencecentral;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.arjuna.databroker.data.DataConsumer;
import com.arjuna.databroker.data.DataFlowNode;
import com.arjuna.databroker.data.DataProvider;
import com.arjuna.databroker.data.DataService;
import com.connexience.api.StorageClient;
import com.connexience.api.model.EscDocument;
import com.connexience.api.model.EscDocumentVersion;
import com.connexience.api.model.EscFolder;

public class WorkflowDataService implements DataService, DataProvider<String>, DataConsumer<String>
{
    private static final Logger logger = Logger.getLogger(WorkflowDataService.class.getName());

    public static final String SERVERHOST_PROPERTYNAME   = "Server Host";
    public static final String SERVERPORT_PROPERTYNAME   = "Server Port";
    public static final String USERNAME_PROPERTYNAME     = "User Name";
    public static final String USERPASSWORD_PROPERTYNAME = "User Password";
    public static final String DATAFILENAME_PROPERTYNAME = "Data File Name";

    public WorkflowDataService(String name, Map<String, String> properties)
    {
        _name          = name;
        _properties    = properties;
        _dataConsumers = new LinkedList<DataConsumer<String>>();
    }

    @Override
    public String getName()
    {
        return _name;
    }

    @Override
    public Map<String, String> getProperties()
    {
        return Collections.unmodifiableMap(_properties);
    }

    @Override
    public void consume(DataProvider<String> dataProvider, String data)
    {
        try
        {
            String  serverHost   = _properties.get(SERVERHOST_PROPERTYNAME);
            Integer serverPost   = Integer.parseInt(_properties.get(SERVERPORT_PROPERTYNAME));
            String  userName     = _properties.get(USERNAME_PROPERTYNAME);
            String  userPassword = _properties.get(USERPASSWORD_PROPERTYNAME);
            String  dataFileName = _properties.get(DATAFILENAME_PROPERTYNAME);

            StorageClient  storageClient  = new StorageClient(serverHost, serverPost, false, userName, userPassword);

            // Upload the data to document in home folder
            EscFolder          homeFolder      = storageClient.homeFolder();
            EscDocument        document        = storageClient.createDocumentInFolder(homeFolder.getId(), dataFileName);
            InputStream        dataStream      = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
            EscDocumentVersion documentVersion = storageClient.upload(document, dataStream, data.length());
            dataStream.close();
        }
        catch (Exception exception)
        {
            logger.log(Level.WARNING, "Unexpected problem while store workflow data file", exception);
        }
    }

    @Override
    public DataFlowNode getDataFlowNode()
    {
        return this;
    }

    @Override
    public Collection<Class<?>> getDataProviderDataClasses()
    {
        Set<Class<?>> dataProviderDataClasses = new HashSet<Class<?>>();

        dataProviderDataClasses.add(String.class);
        
        return dataProviderDataClasses;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> DataProvider<T> getDataProvider(Class<T> dataClass)
    {
        if (dataClass == String.class)
            return (DataProvider<T>) this;
        else
            return null;
    }

    @Override
    public Collection<Class<?>> getDataConsumerDataClasses()
    {
        Set<Class<?>> dataConsumerDataClasses = new HashSet<Class<?>>();

        dataConsumerDataClasses.add(String.class);
        
        return dataConsumerDataClasses;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> DataConsumer<T> getDataConsumer(Class<T> dataClass)
    {
        if (dataClass == String.class)
            return (DataConsumer<T>) this;
        else
            return null;
    }

    @Override
    public Collection<DataConsumer<String>> getDataConsumers()
    {
        return _dataConsumers;
    }

    @Override
    public void addDataConsumer(DataConsumer<String> dataConsumer)
    {
        _dataConsumers.add(dataConsumer);
    }

    @Override
    public void removeDataConsumer(DataConsumer<String> dataConsumer)
    {
        _dataConsumers.remove(dataConsumer);
    }

    @Override
    public void produce(String data)
    {
        for (DataConsumer<String> dataConsumer: _dataConsumers)
            dataConsumer.consume(this, data);
    }

    private String                     _name;
    private Map<String, String>        _properties;
    private List<DataConsumer<String>> _dataConsumers;
}
