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
	    double[]  tp_class      = new double[14];
	    double[]  fp_class      = new double[14];
	    double[]  fn_class      = new double[14];
	    double[]  precision_vec = new double[14];
	    double[]  recall_vec    = new double[14];
	    double[] f1_vec         = new double[14];
	    
		for (Result_object res : result_object_list) {
			sum_k3+= (res.res_3_true) ? 1 : 0;
			sum_k5+= (res.res_5_true) ? 1 : 0;
			sum_k10+= (res.res_10_true) ? 1 : 0;
			sum_k15+= (res.res_15_true) ? 1 : 0;
			sum_k+= (res.res_k_true) ? 1:0;
			file_line = res.doc_id+","+res.predicted_class_num+","+res.truth;
			out.println(file_line);
			
			//calculating recall and precision:
			if (Integer.parseInt(res.predicted_class_num)==Integer.parseInt(res.truth)) {
				tp_class[Integer.parseInt(res.truth)-1]+=1;
			} else {
				fp_class[Integer.parseInt(res.predicted_class_num)-1]+=1;
				fn_class[Integer.parseInt(res.truth)-1]+=1;
			}
		}
		for (int ii = 0; ii < 14; ii=ii+1) {
			precision_vec[ii]=tp_class[ii]/(tp_class[ii]+fp_class[ii]);
			recall_vec[ii]=tp_class[ii]/(tp_class[ii]+fn_class[ii]);
//			System.out.println("Debug: precision="+precision_vec[ii]+" recall="+recall_vec[ii]+" ii="+ii);
			f1_vec[ii]=(2*precision_vec[ii]*recall_vec[ii])/ (precision_vec[ii]+recall_vec[ii]); //max 1 incase ans is 0
		}
		//calculating macro avg
		double macro_avg=  Arrays.stream(f1_vec).sum()/14;
		
		//calculating micro avg (needs some pre calculations)
		double total_tp =  Arrays.stream(tp_class).sum();
		double total_fp = Arrays.stream(fp_class).sum();
		double total_fn = Arrays.stream(fn_class).sum();
		double total_precision = total_tp/(total_tp+total_fp);
		double total_recall = total_tp/(total_tp+total_fn);
		double micro_avg=  (2*total_precision*total_recall)/(total_precision+total_recall);
		System.out.println("Macro average is "+ macro_avg);
		System.out.println("Micro average is "+ micro_avg);
		
//		 System.out.println("tp_class       is "+  Arrays.toString(tp_class      ));
//		 System.out.println("fp_class       is "+  Arrays.toString(fp_class      ));
//		 System.out.println("fn_class       is "+  Arrays.toString(fn_class      ));
//		 System.out.println("precision_vec  is "+  Arrays.toString(precision_vec ));
//		 System.out.println("recall_vec     is "+  Arrays.toString(recall_vec    ));
//		 System.out.println("f1_vec         is "+  Arrays.toString(f1_vec        ));

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
