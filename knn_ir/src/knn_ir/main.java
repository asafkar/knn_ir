package knn_ir;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
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
		
		int cnt=0;
		
		for (Test_object t_o : test_object_list) {
			Lucene_functions.KnnClassification(t_o , Integer.parseInt(Config.k_size));	
//			cnt+=1;
//			if (cnt==10) {break;};
		}
		
//		System.out.println("Number of correct classifications = " + CorrectClassificationCount);
//		System.out.println("Number of wrong classifications = " + WrongClassificationCount);
		int sum_k3=0;
		int sum_k5=0;
		int sum_k10=0;
		int sum_k15=0;
		int sum_k=0; // k provided in param file
		
		
		
		//delete old output file
		File dir = new File(".");
		File output_file = new File(dir.getCanonicalPath() + File.separator + Config.outputFile);
		output_file.delete();
		
	    
		File result_file = new File(dir.getCanonicalPath() + File.separator + Config.outputFile);
		result_file.getParentFile().mkdirs(); //creates the dir if doesn't exist

    	FileWriter  fw = new FileWriter(result_file.getAbsolutePath(), true);
    	BufferedWriter   bw = new BufferedWriter(fw);
    	PrintWriter    out = new PrintWriter(bw);
	    
	    
	    String file_line;
	    
	    //vars for calculating recall, precision
	    int[] tp_class = new int[14];
	    int[] fp_class = new int[14];
	    int[] fn_class = new int[14];
	    int[] precision_vec = new int[14];
	    int[] recall_vec = new int[14];
	    int [] f1_vec = new int[14];
	    
		for (Result_object res : result_object_list) {
			sum_k3+= (res.res_3_true) ? 1 : 0;
			sum_k5+= (res.res_5_true) ? 1 : 0;
			sum_k10+= (res.res_10_true) ? 1 : 0;
			sum_k15+= (res.res_15_true) ? 1 : 0;
			sum_k+= (res.res_k_true) ? 1:0;
			file_line = res.doc_id+","+res.predicted_class_num+","+res.truth;
			out.println(file_line);
			
			//calculating recall and precision:
			if (res.predicted_class_num==res.truth) {
				tp_class[Integer.parseInt(res.truth)-1]+=1;
			} else {
				fp_class[Integer.parseInt(res.predicted_class_num)-1]+=1;
				fn_class[Integer.parseInt(res.truth)-1]+=1;
			}
		}
		
		for (int ii = 0; ii < 13; ii=ii+1) {
			precision_vec[ii]=tp_class[ii]/(tp_class[ii]+fp_class[ii]);
			recall_vec[ii]=tp_class[ii]/(tp_class[ii]+fn_class[ii]);
			f1_vec[ii]=(2*precision_vec[ii]*recall_vec[ii])/(precision_vec[ii]+recall_vec[ii]);
		}
		//calculating macro avg
		int macro_avg=  Arrays.stream(f1_vec).sum()/14;
		
		//calculating micro avg (needs some pre calculations)
		int total_tp =  Arrays.stream(tp_class).sum();
		int total_fp = Arrays.stream(fp_class).sum();
		int total_fn = Arrays.stream(fn_class).sum();
		int total_precision = total_tp/(total_tp+total_fp);
		int total_recall = total_tp/(total_tp+total_fn);
		int micro_avg=  (2*total_precision*total_recall)/(total_precision+total_recall);
		System.out.println("Macro average is "+ macro_avg);
		System.out.println("Micro average is "+ micro_avg);
		
		System.out.println("Total number of correct hits with k=3 is "+sum_k3);
		System.out.println("Total number of correct hits with k=5 is "+sum_k5);
		System.out.println("Total number of correct hits with k=10 is "+sum_k10);
		System.out.println("Total number of correct hits with k=15 is "+sum_k15);
		System.out.println("Total number of correct hits with provided k is "+sum_k);
		bw.close();
		fw.close();
		out.close();
		
		
	}

}
