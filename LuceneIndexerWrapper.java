

import net.semanticmetadata.lire.builders.GlobalDocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.features.global.AutoColorCorrelogram;
import net.semanticmetadata.lire.imageanalysis.features.global.CEDD;
import net.semanticmetadata.lire.imageanalysis.features.global.FCTH;
import net.semanticmetadata.lire.indexers.parallel.ParallelIndexer;

import net.semanticmetadata.lire.builders.DocumentBuilder;
import net.semanticmetadata.lire.searchers.*;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.Directory;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.document.Document;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Paths;
import java.net.URI;
import java.util.List;
import java.io.*;

public class LuceneIndexerWrapper {

	static String BASE_PATH; // Pseudo constant, load from LuceneIndexer
	static final int NUM_THREADS = 8;
	static final int NUM_RESULTS = 12;

	public static void init(String basePath) throws Exception {
		BASE_PATH = basePath;
		System.setOut(new PrintStream(new BufferedOutputStream(new FileOutputStream(BASE_PATH + "logs/wrapper_stdout", true)), true));
		System.setErr(new PrintStream(new BufferedOutputStream(new FileOutputStream(BASE_PATH + "logs/wrapper_stderr", true)), true));
	}
	
	public static void updateIndex(String basePath, String indexName) throws Exception {
		init(basePath);
		System.out.println("updateIndex");
		System.out.println("BASE_PATH: " + BASE_PATH);

		final String indexPath = BASE_PATH + indexName + "/index";
		final String imagePath = BASE_PATH + indexName + "/images";
		final int numOfThreads = 8;
		
		// Run indexer only if there are actually some files. Otherwise
		// an exception will be thrown.
		if (new File(imagePath).list().length > 0) {
			ParallelIndexer indexer = new ParallelIndexer(NUM_THREADS,
				indexPath, imagePath);
			indexer.addExtractor(CEDD.class);
			//indexer.addExtractor(FCTH.class);
			//indexer.addExtractor(AutoColorCorrelogram.class);
			indexer.run();
			printDebugInfo(indexName);
		}

		System.out.println("Finished indexing.");

		
	}

	public static void printDebugInfo(String indexName) throws Exception {
		IndexReader ir = DirectoryReader.open(FSDirectory.open(Paths.get(new URI("file://" + BASE_PATH + indexName + "/index"))));
		System.out.println("dbg numDocs: " + ir.numDocs());
		for (int i = 0; i < ir.maxDoc(); i++) {
			System.out.println("doc: " + i);
			Document doc = ir.document(i);

			/*List<IndexableField> allFields = doc.getFields();
			for (IndexableField field : allFields)
			{
				System.out.println(field.name() + " " + field.stringValue());
			}*/
				
			String docFileName = doc.getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
			System.out.println("docFileName: " + docFileName);
		}
	}

	public static String[] search(InputStream compareImageStream, String basePath, String indexName) throws Exception {
		init(basePath);
		System.out.println("search");

		BufferedImage img = ImageIO.read(compareImageStream);

		IndexReader ir = DirectoryReader.open(FSDirectory.open(Paths.get(new URI("file://" + BASE_PATH + indexName + "/index"))));
		ImageSearcher searcher = new GenericFastImageSearcher(NUM_RESULTS, CEDD.class);

		ImageSearchHits hits = searcher.search(img, ir);

		String[] results = new String[hits.length()];
		for (int i = 0; i < hits.length(); i++) {
			String path = ir.document(hits.documentID(i)).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
			String fileName = path.substring(path.lastIndexOf('/') + 1, path.length() );
			System.out.println(hits.score(i) + ": \t" + fileName);
			results[i] = fileName;
		}

		return results;
	}
}
