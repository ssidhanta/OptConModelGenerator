package com.lsu.ml;

import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import com.lsu.hector.HectorClient;
import com.lsu.jmx.JMXClient;

import weka.classifiers.Classifier;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;
import weka.filters.unsupervised.attribute.Remove;
import weka.gui.treevisualizer.PlaceNode2;
import weka.gui.treevisualizer.TreeVisualizer;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

public class Learner {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		File f = new File("C://Users//ssidha1//Dropbox//CLoudAnalyticsLatest//data//testData.arff");
		
		//predict_Final(f);

	}
	
	public static void writeToTestFile(BufferedWriter fw,String str){
		try {
			fw.append("@relation consistency");
			fw.append("\n@attribute "+JMXClient.attr1+ " NUMERIC" );
			/*fw.write("\n@attribute "+JMXClient.attr2+ " NUMERIC" );
			fw.write("\n@attribute "+JMXClient.attr3+ " NUMERIC" );
			fw.write("\n@attribute "+JMXClient.attr4+ " NUMERIC" );
			fw.write("\n@attribute "+JMXClient.attr5+ " NUMERIC" );
			fw.write("\n@attribute "+JMXClient.attr6+ " NUMERIC" );
			fw.write("\n@attribute "+JMXClient.attr7+ " NUMERIC" );
			fw.write("\n@attribute "+JMXClient.attr8+ " NUMERIC" );
			fw.write("\n@attribute "+JMXClient.attr9+ " NUMERIC" );
			fw.write("\n@attribute "+JMXClient.attr10+ " NUMERIC" );
			fw.write("\n@attribute "+JMXClient.attr11+ " NUMERIC" );
			fw.write("\n@attribute "+JMXClient.attr12+ " NUMERIC" );*/
			fw.write("\n@attribute "+JMXClient.attr13+ " NUMERIC" );
			fw.append("\n@attribute "+JMXClient.attr14+ " NUMERIC" );
			fw.append("\n@attribute "+JMXClient.attr15+ " NUMERIC" );
			fw.append("\n@attribute class {ANY,ONE,TWO,THREE,LOCAL_QUORUM,EACH_QUORUM,QUORUM,ALL}" );
			
			fw.append("\n@data\n");
			fw.append(str+"\n");
			
			//String newLine = System.getProperty("line.separator");
			/*for(int i=0;i<10;i++)
			{
			    f0.write("Result "+ i +" : "+ ans + newLine);
			}
			f0.close();*/
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}//appends the string to the file
		 
	}
	
	public static String[] predict_Final_Failure(File f_test){
		String[] cLevel = null;
        Instances test = null;
        
        try {
        
         BufferedReader reader_test = new BufferedReader(
                 new FileReader(f_test.getPath()));	
         test = new Instances(reader_test);
         test.setClassIndex(test.numAttributes() - 1);
         
            //FilteredClassifier fc = (FilteredClassifier) weka.core.SerializationHelper.read("/tmp/j48.model");
         // deserialize model
         ObjectInputStream ois = new ObjectInputStream(
        		 					new FileInputStream("/tmp/j48.model"));
                                   //new FileInputStream("C://Users//ssidha1//Dropbox//CodeBase Log ingestion//j48.model"));
         //J48 j48 = new J48();
         //j48.setUnpruned(true);
         FilteredClassifier fc = (FilteredClassifier) ois.readObject();
         //MultilayerPerceptron mlp = (MultilayerPerceptron) ois.readObject(); 
         //fc.setClassifier(j48);
         
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
         ois.close();
         cLevel = new String[test.numInstances()];
         for (int i = 0; i < test.numInstances(); i++) {
             double pred = fc.classifyInstance(test.instance(i));
             //double pred = mlp.classifyInstance(test.instance(i));
             System.out.print(" predicted: " + test.classAttribute().value((int) pred));
             cLevel[i] = test.classAttribute().value((int) pred);
           }
         HectorClient.drawROC(test,fc);
         //HectorClient.drawROC(test,mlp);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return cLevel;
	}
	
	//public static String predict(double[] params){
	public static String[] predict_Final_cloud(File f_test){
		String[] cLevel = null;
        Instances test = null;
        
        try {
        
         BufferedReader reader_test = new BufferedReader(
                 new FileReader(f_test.getPath()));	
         test = new Instances(reader_test);
         test.setClassIndex(test.numAttributes() - 1);
         
            //FilteredClassifier fc = (FilteredClassifier) weka.core.SerializationHelper.read("/tmp/j48.model");
         // deserialize model
         ObjectInputStream ois = new ObjectInputStream(
                                   new FileInputStream("C://Users//ssidha1//Dropbox//CLoudAnalyticsLatest//data//j48.model"));
         //J48 j48 = new J48();
         //j48.setUnpruned(true);
         //FilteredClassifier fc = (FilteredClassifier) ois.readObject();
         MultilayerPerceptron mlp = (MultilayerPerceptron) ois.readObject(); 
         //fc.setClassifier(j48);
         
      // display classifier
       /*  final javax.swing.JFrame jf = 
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
         tv.fitToScreen();*/
         ois.close();
         cLevel = new String[test.numInstances()];
         for (int i = 0; i < test.numInstances(); i++) {
             //double pred = fc.classifyInstance(test.instance(i));
             double pred = mlp.classifyInstance(test.instance(i));
             System.out.print(" predicted: " + test.classAttribute().value((int) pred));
             cLevel[i] = test.classAttribute().value((int) pred);
           }
         //HectorClient.drawROC(test,fc);
         HectorClient.drawROC(test,mlp);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return cLevel;
	}
	
	public static String predict(File f_test){
		String cLevel = null;
        Instances test = null;
        
        try {
        
         BufferedReader reader_test = new BufferedReader(
                 new FileReader(f_test.getPath()));	
         test = new Instances(reader_test);
         test.setClassIndex(test.numAttributes() - 1);
         
            //FilteredClassifier fc = (FilteredClassifier) weka.core.SerializationHelper.read("/tmp/j48.model");
         // deserialize model
         ObjectInputStream ois = new ObjectInputStream(
                                   new FileInputStream("C://Users//ssidha1//Dropbox//CLoudAnalyticsLatest//data//j48.model"));
         //J48 j48 = new J48();
         //j48.setUnpruned(true);
         FilteredClassifier fc = (FilteredClassifier) ois.readObject();
         //fc.setClassifier(j48);
         ois.close();
         for (int i = 0; i < test.numInstances(); i++) {
             double pred = fc.classifyInstance(test.instance(i));
             System.out.print(" predicted: " + test.classAttribute().value((int) pred));
             cLevel = test.classAttribute().value((int) pred);
           }
      // create instances
         /*Attribute attr1 = new Attribute("recentReadLatencyMicros");
         Attribute attr2 = new Attribute("load");
         Attribute attr3 = new Attribute("streamThroughputMbPerSec");
         Attribute attr4 = new Attribute("avgDelta");
         Attribute attr5 = new Attribute("class {ANY,ONE,TWO,THREE,LOCAL_QUORUM,EACH_QUORUM,QUORUM,ALL}");

         ArrayList<Attribute> attributes = new ArrayList<Attribute>();
         attributes.add(attr1);
         attributes.add(attr2);
         attributes.add(attr3);
         attributes.add(attr4);
         attributes.add(attr5);
        
         Instances testing = new Instances("consistency", attributes, 0);
         // add data
         double[] values = new double[testing.numAttributes()];
         
         values[0] = testing.attribute(0).addStringValue(Double.toString(params[0]));
         values[1] = testing.attribute(1).addStringValue(Double.toString(params[1]));
         values[2] = testing.attribute(2).addStringValue(Double.toString(params[2]));
         values[3] = testing.attribute(3).addStringValue(Double.toString(params[3]));
         values[4] = testing.attribute(4).addStringValue("ANY");

         // add data to instance
         testing.add(new DenseInstance(1.0, values));
         testing.setClassIndex(testing.numAttributes() - 1);
         //testing.setClassIndex(testing.numAttributes() - 1);
         
         System.out.println("***params 0:" + params[0]+" 1: "+params[1]+" 2:"+params[2]+" 3:"+params[3]);
            //for (int i = 0; i < test.numInstances(); i++) {
         		   //testing.instance(0).setValue(testing.numAttributes() - 1, "ANY");
                   double pred = fc.classifyInstance(testing.instance(0));
                   cLevel = testing.classAttribute().value((int) pred);
                   System.out.println("*****pred VAL: " + pred+"\n");
                   System.out.println("*****clevel VAl : " + cLevel+"\n");
                   */
         
                 //}
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return cLevel;
	}

}
