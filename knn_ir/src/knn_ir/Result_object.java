package knn_ir;

import java.util.List; 

public class Result_object {
	public String doc_id;
	public String truth;
//	public List<Integer> knn_list; // these are the top 8 nearest neighbors
	public Boolean res_15_true = false;
	public Boolean res_10_true = false;
	public Boolean res_5_true = false;
	public Boolean res_3_true = false;
	public Boolean res_k_true = false;
	public String predicted_class_num;
}
