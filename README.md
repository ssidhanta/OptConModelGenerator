# TrainingModelGenerator

Please refer to the technical paper Consistency.pdf in the folder.

This is a java client for generating a training model based on Weka API. It generates a J48 decision tree from the training data
given as a training_data.arff file. The parameters are both categorical (operation type: INSERT/READ/SCAN,etc.) and numeric (
latency, staleness, throughput, read-write proportion, packet retransmission rate, thread count, packet count. It classifies 
the data into optimum consitency classes (ONE, QUORUM, ALL, etc.). 
The client is called as java -jar TrainingModelGenerator.jar. The output is the model stored to the file system in form of a 
j48.model file. The j48.model file is later called by predictors to predict the optimum consistency level, gievn the test data
in form of a testing_data.arff
