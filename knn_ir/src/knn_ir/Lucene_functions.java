package knn_ir;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.misc.HighFreqTerms; /// add external jar lucene misc
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.similarities.BasicStats;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.search.similarities.Similarity.SimScorer;
import org.apache.lucene.search.similarities.SimilarityBase;
import org.apache.lucene.search.similarities.TFIDFSimilarity;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser; // import lucene-queries and query_parser jars
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory; // TODO - decide if we want RAMDirectory, or FSDirectory 
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import com.opencsv.CSVReader;

import org.apache.lucene.document.Field;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer; //make sure you import into build_path the jar lucene\analysis\common\ jar file
import org.apache.lucene.analysis.util.CharArraySet;

import java.io.FileReader;
import java.io.IOException;
import com.opencsv.CSVReader;
 



public class Lucene_functions  { 
	static StandardAnalyzer analyzer = new StandardAnalyzer(StopFilter.makeStopSet((main.stop_words_list))); 
	static Directory index_dir = new RAMDirectory();
	

	public static void addDoc(IndexWriter w, String title, String content, String doc_num, String label) throws IOException {
		Document doc = new Document();

		doc.add(new StringField("title", title, Field.Store.YES)); // stringfiled isn't tokenized
		doc.add(new TextField("content", content, Field.Store.YES)); // textfield is tokenized
		doc.add(new StringField("doc_id", doc_num, Field.Store.YES));
		doc.add(new StringField("label", label, Field.Store.YES) );
		w.addDocument(doc);

	}

	public static void analyze_most_frequent_terms() throws Exception {
		Iterator iter = EnglishAnalyzer.getDefaultStopSet().iterator();
		while (iter.hasNext()) {
			char[] stopWord = (char[]) iter.next();
			main.stop_words_list.add(new String(stopWord));
		}
	} // analyze_most_frequent_terms

	public static void parse_training_set() throws IOException {
		File dir = new File(".");
		Integer skip_counter = 0;
		
//		FSDirectory index_dir = FSDirectory.open(Paths.get(dir.getCanonicalPath() ));
		CSVReader reader = new CSVReader(new FileReader(Config.trainFile));
		String[] nextLine;
		IndexWriterConfig config = new IndexWriterConfig(analyzer);
		IndexWriter w = new IndexWriter(index_dir, config);
		while ((nextLine = reader.readNext()) != null) {
			skip_counter+=1;
			if (skip_counter==7) { //for each 8 words, take only 1 - "sample" the data...
				addDoc(w, nextLine[2], nextLine[3], nextLine[0],nextLine[1]);
				skip_counter=0;
			};
		}
		reader.close();
		w.close();
	}
	
	public static void parse_test_set() throws IOException, ParseException {
		File dir = new File(".");
//		FSDirectory index_dir = FSDirectory.open(Paths.get(dir.getCanonicalPath() ));
		CSVReader reader = new CSVReader(new FileReader(Config.testFile));
		String[] nextLine;
		while ((nextLine = reader.readNext()) != null) {
//			addDoc(w, nextLine[2], nextLine[3], nextLine[0],nextLine[1]);
			Test_object temp_test_obj = new Test_object();
			temp_test_obj.content = nextLine[3];
			temp_test_obj.title = nextLine[2];
			temp_test_obj.doc_id = nextLine[0];
			temp_test_obj.label = nextLine[1];
			main.test_object_list.add(temp_test_obj);
		}
		reader.close();
	}	
	

	// this function creates a string of stop words for filtering the queries 
	public static void query_filter() {
		Iterator iter = EnglishAnalyzer.getDefaultStopSet().iterator();
		while(iter.hasNext()) {
		    char[] stopWord = (char[]) iter.next();
//		    Main.query_filter_string= Main.query_filter_string+"|"+(new String (stopWord));
		}		
	}

	
	
	public static void KnnClassification(Test_object t_o, int k_size) throws IOException, ParseException {
		String current_query_str = t_o.content;
		IndexReader reader = DirectoryReader.open(index_dir);
		IndexSearcher searcher = new IndexSearcher(reader);
		
		SimilarityBase tfidfSimilarity = new SimilarityBase() {
			@Override
			protected float score(BasicStats stats, float freq, float docLen) {
				float tf = (float) (1 + Math.log(freq));
				float idf = (float) (Math.log((stats.getNumberOfDocuments()) / (stats.getDocFreq())));
				return tf*idf;
			}
	
			@Override
			public String toString() {
				return "tf-idf similarity";
			}
		};
		searcher.setSimilarity(tfidfSimilarity);

    	Query currentQuery = new QueryParser("content", analyzer).parse(QueryParser.escape(current_query_str.replaceAll(main.query_filter_string, " ")));
//		String[] fields = { "content", "title" };
//        Query currentQuery = new MultiFieldQueryParser( fields, analyzer).parse(QueryParser.escape(current_query_str));
         
        TopScoreDocCollector inputCollector = TopScoreDocCollector.create(k_size);
        searcher.search(currentQuery, inputCollector);
        ScoreDoc[] hits = inputCollector.topDocs().scoreDocs;
//        System.out.println("current query:" + current_query_str + "  Classification: ");
        
        Integer[] arr = new Integer[14];
        ArrayList<Integer> labelCount = new ArrayList<>(Arrays.asList(arr));
        Collections.fill(labelCount, 0); // an array of 14, each cell represents the num of hits for specific query (test doc)

        
        Result_object temp_res = new Result_object(); //this object holds the temp result for query
        temp_res.doc_id=t_o.doc_id;
        temp_res.truth=t_o.label;
        Integer curr_index=0;
        
        Integer maxCount ;
        int classificationResult ;
        
        // go over all hits (search results) - 
        // for each result, add to labelCount
        // on curr_index values, see what's the dominant number of labels, and decide according to it
        
        for (ScoreDoc hit : hits) { // run on k results 
//        	System.out.println("Document:" + hit.doc);
//        	System.out.println("Label of the document:" + searcher.doc(hit.doc).getField("label").stringValue());
        	Integer hit_label = Integer.parseInt(searcher.doc(hit.doc).getField("label").stringValue());
        	labelCount.set(hit_label-1, labelCount.get(hit_label-1)+1);
//            temp_res.knn_list.add(hit_label);
            if (curr_index==k_size-1) {
            	maxCount = Collections.max(labelCount);
            	classificationResult =  labelCount.indexOf(maxCount) + 1;
            	if (classificationResult == Integer.parseInt(t_o.label)) {temp_res.res_k_true = true;};	            
            	temp_res.predicted_class_num= Integer.toString(classificationResult);
            } else if (curr_index==15-1) { // if provided k is smaller than this, will remain 0...
            	maxCount = Collections.max(labelCount);
            	classificationResult =  labelCount.indexOf(maxCount) + 1;
            	if (classificationResult == Integer.parseInt(t_o.label)) {temp_res.res_15_true = true;};        	
            } else if (curr_index==10-1) {
            	maxCount = Collections.max(labelCount);
            	classificationResult =  labelCount.indexOf(maxCount) + 1;
            	if (classificationResult == Integer.parseInt(t_o.label)) {temp_res.res_10_true = true;};               	
            } else if (curr_index==5-1){
            	maxCount = Collections.max(labelCount);
            	classificationResult =  labelCount.indexOf(maxCount) + 1;
            	if (classificationResult == Integer.parseInt(t_o.label)) {temp_res.res_5_true = true;};   
            } else if (curr_index== 3-1) {
            	maxCount = Collections.max(labelCount);
            	classificationResult =  labelCount.indexOf(maxCount) + 1;
            	if (classificationResult == Integer.parseInt(t_o.label)) {temp_res.res_3_true = true;};   
            }
            	 
            curr_index+=1;
            
        }
        main.result_object_list.add(temp_res);
//        System.out.println(temp_res.res_4_true.toString()+temp_res.res_5_true.toString()+temp_res.res_6_true.toString()+
//	        		temp_res.res_7_true.toString()+temp_res.res_8_true.toString());
        
//        System.out.println("Label as classified using knn - " + classificationResult + " while the document truth is " + t_o.label);


//        if (classificationResult == Integer.parseInt(t_o.label)) {
//        	Main.CorrectClassificationCount++;
//        } else {
//        	Main.WrongClassificationCount++;
//        }

	}
	
	
	
	
}
