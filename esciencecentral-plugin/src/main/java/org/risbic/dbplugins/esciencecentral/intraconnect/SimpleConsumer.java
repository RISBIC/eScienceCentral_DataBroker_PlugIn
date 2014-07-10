package org.risbic.dbplugins.esciencecentral.intraconnect;

import com.arjuna.databroker.data.DataConsumer;
import com.arjuna.databroker.data.DataFlowNode;
import com.arjuna.databroker.data.DataProvider;

import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SimpleConsumer<T> implements DataConsumer<T> {
	private static final Logger logger = Logger.getLogger(SimpleConsumer.class.getName());

	private final Method _method;

	private final DataFlowNode _dataFlowNode;

	private final Class<T> _dataClass;

	public SimpleConsumer(DataFlowNode dataFlowNode, String methodName, Class<T> dataClass) {
		logger.log(Level.FINE, "SimpleConsumer: " + dataFlowNode + ", " + methodName + ", " + dataClass);

		_dataFlowNode = dataFlowNode;
		_dataClass = dataClass;
		_method = getMethod(_dataFlowNode.getClass(), methodName);
	}

	@Override
	public DataFlowNode getDataFlowNode() {
		return _dataFlowNode;
	}

	@Override
	public void consume(DataProvider<T> dataProvider, T data) {
		try {
			logger.log(Level.INFO, "BasicDataConsumer.consume: [" + data.toString() + "]");
			_method.invoke(_dataFlowNode, data);
		} catch (Throwable throwable) {
			logger.log(Level.WARNING, "Problem invoking consumer", throwable);
		}
	}

	private Method getMethod(Class<?> nodeClass, String nodeMethodName) {
		try {
			return nodeClass.getMethod(nodeMethodName, new Class[] { _dataClass });
		} catch (Throwable throwable) {
			logger.log(Level.WARNING, "Unable to find method \"" + nodeMethodName + "\"", throwable);

			return null;
		}
	}
}
