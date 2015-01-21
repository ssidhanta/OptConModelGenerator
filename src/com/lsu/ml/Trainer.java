package com.lsu.ml;

import java.awt.BorderLayout;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.io.RandomAccessFile;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.db.Row;
import org.apache.cassandra.db.RowMutation;
import org.apache.cassandra.db.SystemTable;
import org.apache.cassandra.db.Table;
import org.apache.cassandra.gms.ApplicationState;
import org.apache.cassandra.gms.EndpointState;
import org.apache.cassandra.gms.Gossiper;
import org.apache.cassandra.gms.VersionedValue;
import org.apache.cassandra.service.StorageService;
import org.apache.cassandra.utils.FBUtilities;
import org.apache.commons.math3.stat.regression.SimpleRegression;

import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.functions.MultilayerPerceptron;
//import weka.classifiers.functions.SMOreg;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NominalToBinary;
import weka.filters.unsupervised.attribute.Remove;
import weka.gui.treevisualizer.PlaceNode2;
import weka.gui.treevisualizer.TreeVisualizer;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import me.prettyprint.cassandra.model.BasicColumnDefinition;
import me.prettyprint.cassandra.model.BasicColumnFamilyDefinition;
import me.prettyprint.cassandra.model.ConfigurableConsistencyLevel;
import me.prettyprint.cassandra.model.CqlQuery;
import me.prettyprint.cassandra.model.CqlRows;
import me.prettyprint.cassandra.serializers.LongSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.service.CassandraHost;
import me.prettyprint.cassandra.service.ThriftCfDef;
import me.prettyprint.cassandra.service.ThriftKsDef;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.HConsistencyLevel;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.ddl.ColumnFamilyDefinition;
import me.prettyprint.hector.api.ddl.ColumnIndexType;
import me.prettyprint.hector.api.ddl.ComparatorType;
import me.prettyprint.hector.api.ddl.KeyspaceDefinition;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.Mutator;
import me.prettyprint.hector.api.query.QueryResult;

public class Trainer {


	public static void main(String[] args) {
		File f = new File("C://Users//ssidha1//Dropbox//CLoudAnalyticsLatest//data//trainData.arff");
		exhaustiveSearchOptimalThreshold(f,-1,1,2,1,2,1);
		Trainer.generateModel();
		
	}
	
	
	
	
	public static void exhaustiveSearchOptimalThreshold(File f,double wt_latency,double wt_throughput,double wt_staleness,double wt_retransmisson,double wt_load,double wt_packetloss){
		String cLevel = "ONE", str = null,cLevelOld,input="",hmapKey = null;
		BufferedReader reader = null;
		String line = "";
		ArrayList<String> rows = new ArrayList<String>();
		Map<String,Map<String,ArrayList<String>>> hmap = null;
		Map<String,ArrayList<String>> hmapInner = new HashMap();
		int ave = 0;
		double maxVal = 0, val =0, valThreadCount = 0, valPacketCount = 0, valReadWriteProportions = 0;
		try {
			
			reader = new BufferedReader(
					new FileReader(f.getPath()));
			while ((line = reader.readLine())!= null) {
				if(!"".equalsIgnoreCase(line) && !line.contains("@"))
				{
					str = line.toString();
					valPacketCount = Double.parseDouble(str.split(",")[6]);
					valThreadCount = Double.parseDouble(str.split(",")[7]);
					valReadWriteProportions = Double.parseDouble(str.split(",")[8]);
					if(hmap!=null && hmap.containsKey(valPacketCount+","+valThreadCount+","+valReadWriteProportions))
					{
						hmapKey = valPacketCount+","+valThreadCount+","+valReadWriteProportions;
						hmapInner = hmap.get(hmapKey);
						if(hmapInner!=null)
						{
						Iterator<Map.Entry<String, ArrayList<String>>> entriesInner = hmapInner.entrySet().iterator();
					        while (entriesInner.hasNext()) {
								Entry<String, ArrayList<String>> entryInner = entriesInner.next();
								rows = (ArrayList<String>) entryInner.getValue();
								if(entryInner.getKey()!=null && entryInner.getKey().contains(",") && entryInner.getKey().split(",").length>0)
									maxVal = Double.parseDouble((String)entryInner.getKey().split(",")[1]);
								rows.add(str);
					        }
						}
						if(cLevel == null)
						{
							cLevel = str.split(",")[9].trim();
							
						}
						else 
						{
							
							if(hmapInner!=null && rows!=null && cLevel!=null)
							{
								
								hmapInner.remove(cLevel+","+maxVal);
								hmap.remove(hmapKey);
								if(Double.parseDouble(str.split(",")[1])<=wt_latency && Double.parseDouble(str.split(",")[5]) <=wt_retransmisson && Double.parseDouble(str.split(",")[2])>wt_throughput && Double.parseDouble(str.split(",")[4])>wt_staleness)
									cLevel = str.split(",")[9].trim();
								maxVal = maxVal + val; 
								ave = ave + 1;
								
							}
							
						}
						
					}
					else if(hmap==null)
					{
						hmap = new HashMap();
						hmapKey = valPacketCount+","+valThreadCount+","+valReadWriteProportions;
						maxVal =  val;
						hmapInner = new HashMap();
						rows = new ArrayList();
						rows.add(str);
						
					}
					else if(hmapKey!=null && !hmapKey.equalsIgnoreCase(valPacketCount+","+valThreadCount+","+valReadWriteProportions))
					{
						
						if(hmapInner==null)
								hmapInner = new HashMap();
							else
							{
								hmapInner.put(cLevel+","+maxVal, rows);
								hmap.put(hmapKey, hmapInner);
								hmapInner = new HashMap();
								rows = new ArrayList();
								ave = 0;
							}
							
							maxVal = maxVal + val; 
							rows.add(str);
							maxVal = maxVal / ave;
							
							hmapKey = valPacketCount+","+valThreadCount+","+valReadWriteProportions;
							
					}
					else
					{
						hmapKey = valPacketCount+","+valThreadCount+","+valReadWriteProportions;
						hmapInner = hmap.get(hmapKey);
						if(val>maxVal)
						{
							if(hmapInner!=null)
								hmapInner.remove(cLevel+","+maxVal);
							
							hmap.remove(hmapKey);
							//input = input + str + "\n";
							maxVal = val;
							cLevel = str.split(",")[9].trim();
							
						}
						
						
						rows.add(str);
						if(hmapInner==null)
							hmapInner = new HashMap();
						 
					}
					
					
				}
				else if(!"".equalsIgnoreCase(line))
				{
					input = input + line + "\n";
					
				}
				
			}
			
			
			Iterator<Map.Entry<String, Map<String, ArrayList<String>>>> entries = hmap.entrySet().iterator();
			while (entries.hasNext()) {
				Entry<String, Map<String, ArrayList<String>>> entry = entries.next();
		       
		        hmapInner = (Map<String, ArrayList<String>>) entry.getValue();
		        if(hmapInner!=null)
		        {
			        Iterator<Map.Entry<String, ArrayList<String>>> entriesInner = hmapInner.entrySet().iterator();
			        while (entriesInner.hasNext()) {
						Entry<String, ArrayList<String>> entryInner = entriesInner.next();
						rows = (ArrayList<String>) entryInner.getValue();
				        for(int i=0;i<rows.size();i++){
				        	str = (String)rows.get(i);
				        	cLevel = (String)entryInner.getKey().split(",")[0];
							cLevelOld = str.split(",")[9];
							
							if(str!=null && !"".equalsIgnoreCase(str) && cLevel!=null)
								str = str.replace(cLevelOld, cLevel);
				        	input = input + str + "\n";
				        }
			        }
		        }
		    }
			
			File file = new File("C://Users//ssidha1//Dropbox//CodeBase Log ingestion//training_data_final.arff");
			 
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
 
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(input);
			bw.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void exhaustiveSearchOptimal(File f,double wt_latency,double wt_throughput,double wt_staleness,double wt_retransmisson,double wt_load,double wt_packetloss){
		String cLevel = "ONE", str = null,cLevelOld,input="",hmapKey = null;
		BufferedReader reader = null;
		String line = "";
		ArrayList<String> rows = new ArrayList<String>();
		Map<String,Map<String,ArrayList<String>>> hmap = null;
		Map<String,ArrayList<String>> hmapInner = new HashMap();
		int ave = 0;
		double maxVal = 0, val =0, valThreadCount = 0, valPacketCount = 0, valReadWriteProportions = 0;
		try {
			/*RandomAccessFile file = new RandomAccessFile(filePath, "r");
			file.seek(position);
			byte[] bytes = new byte[size];
			file.read(bytes);
			file.close();*/
			
			reader = new BufferedReader(
					new FileReader(f.getPath()));
			while ((line = reader.readLine())!= null) {
				if(!"".equalsIgnoreCase(line) && !line.contains("@"))
				{
					str = line.toString();
					valPacketCount = Double.parseDouble(str.split(",")[6]);
					valThreadCount = Double.parseDouble(str.split(",")[7]);
					valReadWriteProportions = Double.parseDouble(str.split(",")[8]);
					//rows.add(str);
					val = Double.parseDouble(str.split(",")[1])*wt_latency;
					val = Double.parseDouble(str.split(",")[2])*wt_throughput - val;
					//val = val + Double.parseDouble(line.split(",")[2])*wt_load;
					val = val - Double.parseDouble(str.split(",")[4])*wt_staleness;
					val = val -  Double.parseDouble(str.split(",")[5])*wt_retransmisson;
					//cLevel = str.split(",")[9].trim();
					//System.out.println("666 cLevel hmap line str:---"+str);
					if(hmap!=null && hmap.containsKey(valPacketCount+","+valThreadCount+","+valReadWriteProportions))
					{
						//System.out.println("882222444444444422222222else cLevel hmap line:---"+line);
						hmapKey = valPacketCount+","+valThreadCount+","+valReadWriteProportions;
						hmapInner = hmap.get(hmapKey);
						if(hmapInner!=null)
						{
						Iterator<Map.Entry<String, ArrayList<String>>> entriesInner = hmapInner.entrySet().iterator();
					        while (entriesInner.hasNext()) {
								Entry<String, ArrayList<String>> entryInner = entriesInner.next();
								rows = (ArrayList<String>) entryInner.getValue();
								if(entryInner.getKey()!=null && entryInner.getKey().contains(",") && entryInner.getKey().split(",").length>0)
									maxVal = Double.parseDouble((String)entryInner.getKey().split(",")[1]);
								rows.add(str);
								//System.out.println("1 rows add line:---"+line);
					        }
						}
						if(cLevel == null)
						{
							cLevel = str.split(",")[9].trim();
							//System.out.println("2 line:---"+line);
							
							//output = output + str + "\n";;
						}
						else //if(val>maxVal)
						{
							//hmapInner = hmap.get(cLevel+","+maxVal);
							if(hmapInner!=null && rows!=null && cLevel!=null)
							{
								 //rows =  new ArrayList();
								hmapInner.remove(cLevel+","+maxVal);
								hmap.remove(hmapKey);
								//rows.add(str);
								cLevel = str.split(",")[9].trim();
								//System.out.println("3 line:---"+hmapInner);
								maxVal = maxVal + val; 
								ave = ave + 1;
								//hmapInner.put(cLevel+","+maxVal, rows);
								
							}
							//hmapInner.put(cLevel, rows);
							//hmap.put(packetCount+","+valThreadCount+","+valReadWriteProportions, cLevel);
							
							//output = output + str + "\n";;
						}
						/*else
						{
							//System.out.println("8 line:---"+line);
							
							//output = output + str + "\n";;
						}*/
						
					}
					else if(hmap==null)
					{
						hmap = new HashMap();
						hmapKey = valPacketCount+","+valThreadCount+","+valReadWriteProportions;
						maxVal =  val;
						hmapInner = new HashMap();
						rows = new ArrayList();
						rows.add(str);
						//System.out.println("4 line:---"+hmapInner);
					}
					else if(hmapKey!=null && !hmapKey.equalsIgnoreCase(valPacketCount+","+valThreadCount+","+valReadWriteProportions))
					{
						
						/*if(hmapKey==null)
						{*/
							
							//if(cLevel == null)
								//cLevel = str.split(",")[9].trim();
							if(hmapInner==null)
								hmapInner = new HashMap();
							else
							{
								hmapInner.put(cLevel+","+maxVal, rows);
								hmap.put(hmapKey, hmapInner);
								hmapInner = new HashMap();
								rows = new ArrayList();
								ave = 0;
							}
							//System.out.println("5 line:---"+hmapInner);
							/*if(maxVal < val)
								maxVal = val;*/
							maxVal = maxVal + val; 
							rows.add(str);
							maxVal = maxVal / ave;
							//input = input + str + "\n";
							hmapKey = valPacketCount+","+valThreadCount+","+valReadWriteProportions;
							//hmapInner = new HashMap();
							
							//System.out.println("3 line:---"+line);
							//output = output + str + "\n";;
							
						/*}
						else
						{
							if(cLevel == null)
								cLevel = str.split(",")[9].trim();
							rows.add(str);
							if(hmapInner==null)
								hmapInner = new HashMap();
							if(hmapInner!=null && cLevel!=null)
								hmapInner.put(cLevel+","+maxVal, rows);
							hmap.put(hmapKey, hmapInner);
							hmapKey = valPacketCount+","+valThreadCount+","+valReadWriteProportions;
							cLevel = str.split(",")[9].trim();
							System.out.println("4 line:---"+line);
							//output = output + str + "\n";;
						}
						hmapInner = new HashMap();
					    rows =  new ArrayList();*/
					    
					}
					else
					{
						hmapKey = valPacketCount+","+valThreadCount+","+valReadWriteProportions;
						hmapInner = hmap.get(hmapKey);
						if(val>maxVal)
						{
							if(hmapInner!=null)
								hmapInner.remove(cLevel+","+maxVal);
							
							hmap.remove(hmapKey);
							//input = input + str + "\n";
							maxVal = val;
							cLevel = str.split(",")[9].trim();
							/*cLevel = str.split(",")[9].trim();
							//hmap.put(packetCount+","+valThreadCount+","+valReadWriteProportions, cLevel);
							System.out.println("5 line:---"+line);
							
							 rows.add(str);
							 hmapInner.put(cLevel+","+maxVal, rows);
							 hmapKey = valPacketCount+","+valThreadCount+","+valReadWriteProportions;
							 hmap.put(hmapKey, hmapInner);*/
							//output = output + str + "\n";;
						}
						/*else
						{
							//System.out.println("cLevel hmap inner str:---"+str);
							//input = input + str + "\n";
							System.out.println("6 cLevel:---"+str);
							hmapInner = new HashMap();
							rows =  new ArrayList();
							rows.add(str);
							hmapKey = valPacketCount+","+valThreadCount+","+valReadWriteProportions;
							hmap.put(hmapKey, hmapInner);
							//output = output + str + "\n";;
						}*/
						//System.out.println("6 line:---"+hmapInner);
						
						rows.add(str);
						if(hmapInner==null)
							hmapInner = new HashMap();
						 //hmapInner.put(cLevel+","+maxVal, rows);
						 //hmap.put(hmapKey, hmapInner);
					}
					//System.out.println("cLevel hmap inner:---"+cLevel);
					
				}
				else if(!"".equalsIgnoreCase(line))
				{
					input = input + line + "\n";
					//System.out.println("7 line:---"+line);
					//output = output + str + "\n";;
				}
				
			}
			
			
			//System.out.println("Iterator entries hmap size:---"+input);
			Iterator<Map.Entry<String, Map<String, ArrayList<String>>>> entries = hmap.entrySet().iterator();
			while (entries.hasNext()) {
				Entry<String, Map<String, ArrayList<String>>> entry = entries.next();
		        //System.out.println(entry.getKey() + " = " + entry.getValue());
		        hmapInner = (Map<String, ArrayList<String>>) entry.getValue();
		        if(hmapInner!=null)
		        {
			        Iterator<Map.Entry<String, ArrayList<String>>> entriesInner = hmapInner.entrySet().iterator();
			        while (entriesInner.hasNext()) {
						Entry<String, ArrayList<String>> entryInner = entriesInner.next();
						rows = (ArrayList<String>) entryInner.getValue();
				        for(int i=0;i<rows.size();i++){
				        	str = (String)rows.get(i);
				        	cLevel = (String)entryInner.getKey().split(",")[0];
							cLevelOld = str.split(",")[9];
							//System.out.println("str value:---"+str+" :: cLevel:"+cLevel);
							if(str!=null && !"".equalsIgnoreCase(str) && cLevel!=null)
								str = str.replace(cLevelOld, cLevel);
				        	input = input + str + "\n";
				        }
			        }
		        }
		    }
			
			File file = new File("C://Users//ssidha1//Dropbox//CodeBase Log ingestion//training_data_final.arff");
			 
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
 
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(input);
			bw.close();
			//System.out.println("maxVal:---"+maxVal);
			//System.out.println("cLevel:---"+cLevel);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	//public static void exhaustiveSearch(File f,double wt_latency,double wt_throughput,double wt_staleness,double wt_retransmisson,double wt_load,double wt_packetloss,double packetCount, double threadCount,double readWriteProprtion){
	public static void exhaustiveSearch(File f,double wt_latency,double wt_throughput,double wt_staleness,double wt_retransmisson,double wt_load,double wt_packetloss){
		double packetCount=0, threadCount=0, readWriteProportions=0;
		String cLevel = "ONE", str,cLevelOld,input="";
		BufferedReader reader = null;
		String line = "";
		ArrayList<String> rows = new ArrayList<String>();
		HashMap hmap = new HashMap();
		double maxVal = 0, val =0, valThreadCount = 0, valPacketCount = 0, valReadWriteProportions = 0;
		try {
			/*RandomAccessFile file = new RandomAccessFile(filePath, "r");
			file.seek(position);
			byte[] bytes = new byte[size];
						hmapInner = new HashMap();
					    rows =  new ArrayList();
					    
			file.read(bytes);
			file.close();*/
			
			reader = new BufferedReader(
					new FileReader(f.getPath()));
			while ((line = reader.readLine())!= null) {
				if(!"".equalsIgnoreCase(line) && !line.contains("@"))
				{
					rows.add(line.toString());
				}
				else if(!"".equalsIgnoreCase(line))
					input = input + line.toString() + "\n";
			}
			for(int i=0;i<rows.size();i++){
				    str = (String)rows.get(i);
					valPacketCount = Double.parseDouble(str.split(",")[6]);
					valThreadCount = Double.parseDouble(str.split(",")[7]);
					valReadWriteProportions = Double.parseDouble(str.split(",")[8]);
					for(int j=0;j<rows.size();j++){
						packetCount = Double.parseDouble(str.split(",")[6]);
						threadCount = Double.parseDouble(str.split(",")[7]);
						readWriteProportions = Double.parseDouble(str.split(",")[8]);
						if((valPacketCount==packetCount) && (valThreadCount==threadCount) && (valReadWriteProportions==readWriteProportions))
						{
							val = Double.parseDouble(str.split(",")[1])*wt_latency;
							val = Double.parseDouble(str.split(",")[2])*wt_throughput - val;
							//val = val + Double.parseDouble(line.split(",")[2])*wt_load;
							val = val - Double.parseDouble(str.split(",")[4])*wt_staleness;
							val = val -  Double.parseDouble(str.split(",")[5])*wt_retransmisson;
							//System.out.println("***line val:---"+val);
							if(val>maxVal)
							{
								maxVal = val;
								cLevel = str.split(",")[9].trim();
								hmap.put(packetCount+","+valThreadCount+","+valReadWriteProportions, cLevel);
								
							}
						}
							
					}

			}
			for(int i=0;i<rows.size();i++){
			    str = (String)rows.get(i);
				valPacketCount = Double.parseDouble(str.split(",")[6]);
				valThreadCount = Double.parseDouble(str.split(",")[7]);
				valReadWriteProportions = Double.parseDouble(str.split(",")[8]);
				cLevel = (String)hmap.get(packetCount+","+valThreadCount+","+valReadWriteProportions);
				cLevelOld = str.split(",")[8];
				System.out.println("str value:---"+str);
				if(str!=null && !"".equalsIgnoreCase(str) && cLevel!=null)
				{
					str.replace(cLevelOld, cLevel);
					rows.set(i,str);
				}
				input = input + str + "\n";
			}
			File file = new File("/tmp/training_data_final.arff");
			 
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
 
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(input);
			bw.close();
			//System.out.println("maxVal:---"+maxVal);
			//System.out.println("cLevel:---"+cLevel);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	public static void generateModel()
	{
		Instances train = null;
		ObjectOutputStream oos = null;
        File f_final = new File("C://Users//ssidha1//Dropbox//CLoudAnalyticsLatest//data//trainData_final.arff");
        BufferedReader reader = null;
		new ByteArrayOutputStream();			
		FilteredClassifier fc = null;
		new StringBuffer();
		try {
			
            reader = new BufferedReader(
						new FileReader(f_final.getPath()));
		         //System.out.println("*******in f exists"+f.getPath());
		         train = new Instances(reader);
		  reader.close();
		 // setting class attribute
         train.setClassIndex(train.numAttributes() - 1);
         Remove rm = new Remove();
         rm.setAttributeIndices("1");  // remove 1st attribute
         // classifier
         J48 j48 = new J48();
         j48.setUnpruned(true);        // using an unpruned J48
         // meta-classifier
         fc = new FilteredClassifier();
         fc.setFilter(rm);
         fc.setClassifier(j48); 
         
         // train and make predictions
         
         fc.buildClassifier(train);
            
         // display classifier
            final javax.swing.JFrame jf = 
              new javax.swing.JFrame("Weka Classifier Tree Visualizer: J48");
            jf.setSize(500,400);
            jf.getContentPane().setLayout(new BorderLayout());
            TreeVisualizer tv = new TreeVisualizer(null,
                fc.graph(),
                new PlaceNode2());
            jf.getContentPane().add(tv, BorderLayout.CENTER);
            jf.addWindowListener(new java.awt.event.WindowAdapter() {
              public void windowClosing(java.awt.event.WindowEvent e) {
                jf.dispose();
              }
            });

            jf.setVisible(true);
            tv.fitToScreen();
            
            oos = new ObjectOutputStream(
                    new FileOutputStream("C://Users//ssidha1//Dropbox//CLoudAnalyticsLatest//data//j48.model"));
			oos.writeObject(fc);
			oos.flush();
			oos.close();
            
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
}
