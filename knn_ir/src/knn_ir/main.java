package knn_ir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.queryparser.classic.ParseException;

public class main {
  
	public static List<Test_object> test_object_list = new ArrayList<Test_object>();
	public static List<String> stop_words_list = new ArrayList<String>();
	public static List<Result_object> result_object_list = new ArrayList<Result_object>();
	public static int CorrectClassificationCount = 0;
	public static int WrongClassificationCount = 0;
	public static String query_filter_string="AND";
	public static void main(String[] args) throws IOException, ParseException {
		Config.read_params_file(args[0]);
		Lucene_functions.query_filter();
		Lucene_functions.parse_training_set();
		Lucene_functions.parse_test_set();
		
		for (Test_object t_o : test_object_list) {
			Lucene_functions.KnnClassification(t_o ,800);			
		}
		
//		System.out.println("Number of correct classifications = " + CorrectClassificationCount);
//		System.out.println("Number of wrong classifications = " + WrongClassificationCount);
		int sum_k400=0;
		int sum_k500=0;
		int sum_k600=0;
		int sum_k700=0;
		int sum_k800=0;
		
		File dir = new File(".");
	    FileWriter fileWriter = new FileWriter(dir.getCanonicalPath() + File.separator+Config.outputFile);
	    PrintWriter printWriter = new PrintWriter(fileWriter);
	    
	    
	    String file_line;
	    
		for (Result_object res : result_object_list) {
			sum_k400+= (res.res_4_true) ? 1 : 0;
			sum_k500+= (res.res_5_true) ? 1 : 0;
			sum_k600+= (res.res_6_true) ? 1 : 0;
			sum_k700+= (res.res_7_true) ? 1 : 0;
			sum_k800+= (res.res_8_true) ? 1 : 0;
			file_line = res.doc_id+","+res.predicted_class_num+","+res.truth;
			printWriter.print(file_line);
		}
		System.out.println("Total number of correct hits with k=4 is "+sum_k400);
		System.out.println("Total number of correct hits with k=5 is "+sum_k500);
		System.out.println("Total number of correct hits with k=6 is "+sum_k600);
		System.out.println("Total number of correct hits with k=7 is "+sum_k700);
		System.out.println("Total number of correct hits with k=8 is "+sum_k800);
		printWriter.close();
		fileWriter.close();
		
		
	}

}
