package com.lsu.hector;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.lsu.jmx.JMXClient;
import com.lsu.ml.Learner;

import me.prettyprint.cassandra.model.ConfigurableConsistencyLevel;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.service.CassandraHost;
import me.prettyprint.cassandra.service.CassandraHostConfigurator;
import me.prettyprint.cassandra.service.template.ColumnFamilyTemplate;
import me.prettyprint.cassandra.service.template.ThriftColumnFamilyTemplate;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.HConsistencyLevel;
import me.prettyprint.hector.api.ddl.KeyspaceDefinition;
import me.prettyprint.hector.api.factory.HFactory;

import java.awt.*;
import java.io.*;
import java.util.*;

import javax.swing.*;

import weka.core.*;
import weka.classifiers.*;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.evaluation.*;
import weka.gui.visualize.*;



public class HectorClient {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//callClient();
		//File f = new File("/home/ssidha1/Dropbox/training_data_f.arff");
		String[] cLevel = null;
		File f_test = new File("C://Users//ssidha1//Dropbox//CodeBase Log ingestion//test_data.arff");
		
		File f = new File("C://Users//ssidha1//Dropbox//CodeBase Log ingestion//training_data_final.arff");
		//File f = new File("C://Users//ssidha1//Dropbox//CLoudAnalyticsLatest//data//trainData_final.arff");
		//File f_test = new File("C://Users//ssidha1//Dropbox//CLoudAnalyticsLatest//data//testData.arff");
		
		//chooseConsistency(f,5366,200,3637475.387755102,10,0,50);
		cLevel = Learner.predict_Final_Failure(f_test);
	}
	
	public static void chooseConsistency(File f,double wt_latency,double wt_throughput,double wt_staleness,double wt_retransmisson,double wt_load,double wt_packetloss){
		String cLevel = "ONE";
		BufferedReader reader = null;
		String line = "";
		double maxVal = 0, val =0;
		try {
			reader = new BufferedReader(
					new FileReader(f.getPath()));
			while ((line = reader.readLine())!= null) {
					if(!"".equalsIgnoreCase(line) && !line.contains("@"))
					{
						//Maximize the product approach
						//System.out.println("lines:---"+Arrays.toString(line.split(",")));
						val = 1/Math.pow(Double.parseDouble(line.split(",")[1]),wt_latency);
						val = val * Math.pow(Double.parseDouble(line.split(",")[2]),wt_throughput);
						//val = val * Math.pow(Double.parseDouble(line.split(",")[2]),wt_load);
						val = val / Math.pow(Double.parseDouble(line.split(",")[4]),wt_staleness);
						val = val / Math.pow(Double.parseDouble(line.split(",")[5]),wt_retransmisson);

						//Maximize the sum approach
						val = Double.parseDouble(line.split(",")[1])*wt_latency;
						val = Double.parseDouble(line.split(",")[2])*wt_throughput - val;
						//val = val + Double.parseDouble(line.split(",")[2])*wt_load;
						val = val - Double.parseDouble(line.split(",")[4])*wt_staleness;
						val = val -  Double.parseDouble(line.split(",")[5])*wt_retransmisson;
						
						if(val>maxVal)
						{
							maxVal = val;
							cLevel = line.split(",")[9].trim();
						}
					}
			}
			System.out.println("maxVal:---"+maxVal);
			System.out.println("cLevel:---"+cLevel);
			/*Cluster cluster = HFactory.getOrCreateCluster("Test Cluster", "172.31.21.207");
			ConfigurableConsistencyLevel configurableConsistencyLevel = new ConfigurableConsistencyLevel();
			Map<String, HConsistencyLevel> clmap = new HashMap<String, HConsistencyLevel>();
	
			// Define CL.ONE for ColumnFamily "MyColumnFamily"
	
			//clmap.put("MyColumnFamily", HConsistencyLevel.ONE);
			clmap.put("MyColumnFamily", HConsistencyLevel.valueOf(cLevel));
	
			// In this we use CL.ONE for read and writes. But you can use different CLs if needed.
			configurableConsistencyLevel.setReadCfConsistencyLevels(clmap);
			configurableConsistencyLevel.setWriteCfConsistencyLevels(clmap);
	
			// Then let the keyspace know
			HFactory.createKeyspace("keyspaceDef", cluster, configurableConsistencyLevel);*/
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void callClient(){
		String str=null,cLevel = "ONE";
		String[] nodeList = null;
		int i = 0;
		double[] params =null;
		File f_test = new File("/tmp/test_data.arff");
        OutputStreamWriter writer_test = null;
        BufferedWriter fbw_test = null;
        Process p;
		String command = null;
		StringBuffer output = new StringBuffer();
		BufferedReader reader = null;
        try {
        	command = "sudo /home/ubuntu/apache-cassandra-2.0.6/bin/nodetool --host=172.31.21/207 --port=9999 status";
			p = Runtime.getRuntime().exec(command);
			p.waitFor();
			reader = 
                    new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line = "";		
            int j = 0;
            String[] listHost = new String[30];
			while ((line = reader.readLine())!= null) {
				if(line.toString().split("  ").length>1 && !"Address".contains(line.toString().split("  ")[1]))
				{
					listHost[j] = line.toString().split("  ")[1];
					System.out.println("***nodetool output host: "+listHost[j]);
					j++;
				}
				
			}
        	/*CassandraHostConfigurator cassandraHostConfigurator = new CassandraHostConfigurator("173.253.225.182");
        	cassandraHostConfigurator.setAutoDiscoverHosts(true);*/
        	Cluster cluster = HFactory.getOrCreateCluster("Test Cluster", "172.31.21.207");
			KeyspaceDefinition keyspaceDef = cluster.describeKeyspace("ycsb");
			//for(CassandraHost host: cluster.getKnownPoolHosts(true))
			j = 0;
			while(listHost[j]!=null && !"".equalsIgnoreCase(listHost[j]))
			{
				System.out.println("**********list of Hosts: " + listHost[j]);
				j++; 
				//nodeList[i] = host.getHost();
			}
        	str = JMXClient.getParameters(nodeList);
        	synchronized (f_test) {
        	if(f_test.exists()) 
        		f_test.delete();
        	f_test.createNewFile(); //the true will append the new data
                
        	writer_test = new OutputStreamWriter(
			new FileOutputStream(f_test, true), "UTF-8");
			
		    fbw_test = new BufferedWriter(writer_test);
		    Learner.writeToTestFile(fbw_test,str);
		    fbw_test.flush();
				
        	}
        	cLevel = Learner.predict(f_test);
        	//cLevel = Learner.predict(params);
			//System.out.println("****clevel value:"+cLevel);
			//Create a customized Consistency Level
			ConfigurableConsistencyLevel configurableConsistencyLevel = new ConfigurableConsistencyLevel();
			Map<String, HConsistencyLevel> clmap = new HashMap<String, HConsistencyLevel>();
	
			// Define CL.ONE for ColumnFamily "MyColumnFamily"
	
			//clmap.put("MyColumnFamily", HConsistencyLevel.ONE);
			clmap.put("MyColumnFamily", HConsistencyLevel.valueOf(cLevel));
	
			// In this we use CL.ONE for read and writes. But you can use different CLs if needed.
			configurableConsistencyLevel.setReadCfConsistencyLevels(clmap);
			configurableConsistencyLevel.setWriteCfConsistencyLevels(clmap);
	
			// Then let the keyspace know
			HFactory.createKeyspace("keyspaceDef", cluster, configurableConsistencyLevel);
        } /*catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/ catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	public static void drawROC(Instances data, Classifier cl)
	{
		
	// train classifier
	Evaluation eval;
	try {
		eval = new Evaluation(data);
	
		eval.crossValidateModel(cl, data, 10, new Random(1));
		
		// generate curve
		ThresholdCurve tc = new ThresholdCurve();
		
		int classIndex = 0;
		Instances result = tc.getCurve(eval.predictions(), classIndex);
		System.out.println(eval.toMatrixString());
		// plot curve
		ThresholdVisualizePanel vmc = new ThresholdVisualizePanel();
		vmc.setROCString("(Area under ROC = " + 
		Utils.doubleToString(tc.getROCArea(result), 4) + ")");
		vmc.setName(result.relationName()); 	
		PlotData2D tempd = new PlotData2D(result);
		tempd.setPlotName(result.relationName());
		tempd.addInstanceNumberAttribute();
		// specify which points are connected
		boolean[] cp = new boolean[result.numInstances()];
		for (int n = 1; n < cp.length; n++)
		cp[n] = true;
		tempd.setConnectPoints(cp);
		// add plot
		vmc.addPlot(tempd);
		
		// display curve
		String plotName = vmc.getName(); 
		final javax.swing.JFrame jf = 
		new javax.swing.JFrame("Weka Classifier Visualize: "+plotName);
		jf.setSize(500,400);
		jf.getContentPane().setLayout(new BorderLayout());
		jf.getContentPane().add(vmc, BorderLayout.CENTER);
		jf.addWindowListener(new java.awt.event.WindowAdapter() {
		public void windowClosing(java.awt.event.WindowEvent e) {
		jf.dispose();
		}
		});
		jf.setVisible(true);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}
