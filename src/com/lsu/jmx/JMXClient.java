package com.lsu.jmx;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Set;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.cassandra.concurrent.JMXEnabledThreadPoolExecutorMBean;
import org.apache.cassandra.db.compaction.CompactionManagerMBean;
import org.apache.cassandra.service.StorageProxyMBean;
import org.apache.cassandra.service.StorageServiceMBean;

import com.lsu.ml.Learner;

//import com.lsu.hector.HectorClient;
//import com.lsu.ml.Learner;
public class JMXClient {

	public static String attr1 = "recentReadLatencyMicros";
	public static String attr13 = "load";
	public static String attr14 = "streamThroughputMbPerSec";
	public static String attr15 = "avgDelta";
	/**
	 * @param args
	 */
	public static String getParameters(String[] nodeList){
				JMXServiceURL url;
				JMXConnector jmxc = null;
				String arffStr = "";
				double[] params = new double[4];
				double delta = 0;
				try {
					url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://173.253.227.93:8081/jmxrmi");
					jmxc = JMXConnectorFactory.connect(url, null);

					MBeanServerConnection mbsc = 
							jmxc.getMBeanServerConnection();
					
					
					ObjectName mbeanName = new ObjectName("org.apache.cassandra.db:type=StorageProxy");
					StorageProxyMBean storageProxyMBean = JMX.newMBeanProxy(mbsc, mbeanName, 
							StorageProxyMBean.class);
					arffStr = arffStr + storageProxyMBean.getTotalRangeLatencyMicros(); 
					//arffStr = arffStr + 0;
					params[0] = 0;
					mbeanName = new ObjectName("org.apache.cassandra.db:type=StorageService");
					StorageServiceMBean mbeanStorageService = JMX.newMBeanProxy(mbsc, mbeanName, 
							StorageServiceMBean.class);
					arffStr = arffStr + ","+ mbeanStorageService.getStreamThroughputMbPerSec();
					params[1] = mbeanStorageService.getStreamThroughputMbPerSec();
					final OperatingSystemMXBean myOsBean=  
				            ManagementFactory.getOperatingSystemMXBean();
					double load = myOsBean.getSystemLoadAverage();
					//System.out.println("mxbean cpu load:"+load);
					arffStr = arffStr + "," + load +",0";
					params[2] = load;
					params[3] = 0;
					arffStr = arffStr +",TWO \n";
					//TO DO ADD the delta score input ?
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (MalformedObjectNameException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NullPointerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					try {
						jmxc.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						jmxc=null;
					}
				}
				return arffStr;
				//return params;
	}
	

	public static void main(String[] args) {
		
				///getParameters();

	}

}
