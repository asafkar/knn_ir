package knn_ir;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;


public class Config {
	public static boolean test;
	public static boolean improved_algo ;
	public static String queryFile;
	public static String docsFile;
	public static String outputFile;
	public static String k_size;
	public static String testFile;
	public static String trainFile;

	public static void read_params_file(String param_file_name) throws IOException {
		File dir = new File(".");
		File params_file = new File(dir.getCanonicalPath() + File.separator + param_file_name);
		BufferedReader br = new BufferedReader(new FileReader(params_file));
		Config.trainFile = "Files/"+ br.readLine().split("=")[1];
		Config.testFile ="Files/"+  br.readLine().split("=")[1];
		Config.outputFile = br.readLine().split("=")[1];
		Config.k_size = br.readLine().split("=")[1].trim();

		br.close();

		// delete old output file
		File output_file = new File(dir.getCanonicalPath() + File.separator + Config.outputFile);
		output_file.delete();
	}
}
