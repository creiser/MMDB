

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.net.URLClassLoader;
import java.net.URL;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import oracle.ODCI.ODCIIndexInfo;
import oracle.ODCI.ODCIEnv;
import oracle.ODCI.ODCIPredInfo;
import oracle.ODCI.ODCIQueryInfo;
import oracle.ODCI.ODCIRidList;
import java.sql.Blob;
import java.io.InputStream;
import java.io.OutputStream;
import oracle.ODCI.ODCIColInfo;

import java.io.*;
import oracle.sql.*;
import java.sql.*;
import oracle.CartridgeServices.*;
import java.math.*;

public class LuceneIndexer implements SQLData  {
	final static String BASE_PATH = "/home/oracle/MMDB/";
	final static BigDecimal SUCCESS = new BigDecimal("0");
  	final static BigDecimal ERROR = new BigDecimal("1");

	public BigDecimal key;

	public static String indexName;

	String sql_type;
	public String getSQLTypeName() throws SQLException 
	{
		return sql_type;
	}

	public void readSQL(SQLInput stream, String typeName) throws SQLException 
	{
		sql_type = typeName;
		key = stream.readBigDecimal();
	}

	public void writeSQL(SQLOutput stream) throws SQLException 
	{
		stream.writeBigDecimal(key);
	}

	public static void init(ODCIIndexInfo indexInfo) throws Exception
	{
		// Disable all security. Follwing SQL has to be executed first:
		// 	call dbms_java.grant_permission( 'PUBLIC', 'SYS:java.lang.RuntimePermission', 'setSecurityManager', '' );
		System.setSecurityManager(null);

		// Create directory for logs
		(new File(BASE_PATH + "/logs")).mkdirs();

		System.setOut(new PrintStream(new BufferedOutputStream(new FileOutputStream(BASE_PATH + "logs/stdout", true)), true));
		System.setErr(new PrintStream(new BufferedOutputStream(new FileOutputStream(BASE_PATH + "logs/stderr", true)), true));

		// Add dependencies dynamically
		URLClassLoader systemClassLoader = (URLClassLoader)ClassLoader.getSystemClassLoader();
		Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
		method.setAccessible(true);

		String[] jars = {"commons-io-2.5", "commons-math3-3.6.1", "jopensurf-1.0.0",  "lire", 
			"lucene-analyzers-common-6.3.0", "lucene-core-6.3.0", "lucene-queries-6.3.0", 
			"lucene-queryparser-6.3.0", "lucene-sandbox-6.3.0"};
		for (String jar : jars)
			method.invoke(systemClassLoader, new URL("file://" + BASE_PATH + "lib/lire/" + jar + ".jar"));

		indexName = indexInfo.getIndexName();
	}

	private static Class<?> getLuceneIndexerWrapper() throws Exception {
		URLClassLoader urlClassLoader = new URLClassLoader(new URL[] {new URL("file://" + BASE_PATH)});
		return urlClassLoader.loadClass("LuceneIndexerWrapper");
	}


	public static void writeImageToFile(String fileName, Blob image) throws Exception
	{
		File tempFile = new File(BASE_PATH + indexName + "/images/" + fileName);
		InputStream in = image.getBinaryStream();
		OutputStream out = new FileOutputStream(tempFile);
		byte[] buff = image.getBytes(1, (int)image.length());
		out.write(buff);
		out.close();
	}

	public static void deleteTempImage(String fileName)
	{
		File tempFile = new File(BASE_PATH + indexName + "/images/" + fileName);
		tempFile.delete();
	}

	public static String fromRowID(String rowid) {
		// TODO: Check if more special characters have to be replaced.
		return rowid.replace('/', '_') + ".jpg";
	}

	public static String toRowID(String fileName) {
		return fileName.split("\\.")[0].replace('_', '/');
	}

	public static void updateIndex() throws Exception
	{
		getLuceneIndexerWrapper().getDeclaredMethod("updateIndex", String.class, String.class).
			invoke(null, BASE_PATH, indexName);
	}

	public static BigDecimal ODCIInsert(ODCIIndexInfo indexInfo, String rowid, Blob newVal, 
		ODCIEnv env) throws Exception
	{
		init(indexInfo);
		System.out.println("ODCIInsert");
		System.out.println("rowid: " + rowid);

		writeImageToFile(fromRowID(rowid), newVal);
		updateIndex();

		return SUCCESS;
	}

	public static BigDecimal ODCICreate(ODCIIndexInfo indexInfo, String params, ODCIEnv env) throws Exception
	{
		init(indexInfo);
		System.out.println("ODCICreate");

		Connection conn = DriverManager.getConnection("jdbc:default:connection:");

		// Assume that only one column will be indexed
		ODCIColInfo indexColumn = indexInfo.getIndexCols().getArray()[0];
		String tableName = indexColumn.getTableName();
		String columnName = indexColumn.getColName();
		System.out.println("tableName: " + tableName);
		System.out.println("columnName: " + columnName);

		// Create folder for images
		(new File(BASE_PATH + indexName + "/images")).mkdirs();

		// Get all the images together with their rowids
		String sql = "SELECT ROWID, " + columnName + " FROM " + tableName;
		PreparedStatement stmt = conn.prepareStatement(sql);
		ResultSet resultSet = stmt.executeQuery();

	    	while (resultSet.next()) {
			String fileName = fromRowID(resultSet.getString(1));
			System.out.println("fileName: " + fileName);
			writeImageToFile(fileName, resultSet.getBlob(2));
		}

		resultSet.close();
		conn.close();

		updateIndex();

		return SUCCESS;
	}

	static void deleteRecursive(File f) throws IOException {
		if (f.isDirectory()) {
			for (File c : f.listFiles())
				deleteRecursive(c);
		}
		f.delete();
	}

	public static BigDecimal ODCIDrop(ODCIIndexInfo indexInfo, ODCIEnv env) throws Exception
	{
		init(indexInfo);
		System.out.println("ODCIDrop");
		System.out.println("Delete directory: " + BASE_PATH + indexName);
		deleteRecursive(new File(BASE_PATH + indexName));
		return SUCCESS;
	}

	public static BigDecimal ODCIDelete(ODCIIndexInfo indexInfo, String rowid, Blob oldVal, ODCIEnv env) throws Exception
	{
		init(indexInfo);
		System.out.println("ODCIDelete");
		System.out.println("rowid: " + rowid);
		System.out.println("oldVal: " + oldVal);

		deleteTempImage(fromRowID(rowid));
		updateIndex();

		return SUCCESS;
	}

	public static BigDecimal ODCIUpdate(ODCIIndexInfo indexInfo, String rowid, 
		Blob oldVal, Blob newVal, ODCIEnv env) throws Exception
	{
		init(indexInfo);
		System.out.println("ODCIUpdate");
		System.out.println("rowid: " + rowid);

		deleteTempImage(fromRowID(rowid));
		writeImageToFile(fromRowID(rowid), newVal);
		updateIndex();

		return SUCCESS;
	}

	public static BigDecimal ODCIStart(STRUCT[] sctx, ODCIIndexInfo indexInfo, ODCIPredInfo predicateInfo, 
		oracle.ODCI.ODCIQueryInfo queryInfo, BigDecimal start, BigDecimal stop, Blob compareVal, ODCIEnv env) throws Exception
	{
		init(indexInfo);
		System.out.println("ODCIStart");
		
		Connection conn = DriverManager.getConnection("jdbc:default:connection:");
		
		// TODO: replace by blob that is passed by operator parameter
		//InputStream testStream = new FileInputStream(BASE_PATH + "corel10k/666.jpg");

		String[] fileNames = (String[])getLuceneIndexerWrapper().
			getDeclaredMethod("search", InputStream.class, String.class, String.class).
			invoke(null, /*testStream*/compareVal.getBinaryStream(), BASE_PATH, indexName);

		StoredContext storedContext = new StoredContext(fileNames);
		int keyToStore = ContextManager.setContext(storedContext);
		System.out.println("keyToStore: " + keyToStore);

		Object[] impAttr = new Object[1];
		impAttr[0] = new BigDecimal(keyToStore); 
		StructDescriptor sd = new StructDescriptor("LUCENEINDEXER", conn);
		sctx[0] = new STRUCT(sd, conn, impAttr);

		return SUCCESS;
	}


	public BigDecimal ODCIFetch(BigDecimal numRows, ODCIRidList[] rowids, ODCIEnv env) throws Exception
	{
		System.out.println("ODCIFetch");
		System.out.println("key after pass: " + key.intValue());

		// retrieve stored context using the key
		StoredContext storedContext;
		storedContext = (StoredContext)ContextManager.getContext(key.intValue());
		
		System.out.println("storedContext.fileNames.length: " + storedContext.fileNames.length);
		String[] rlist = new String[numRows.intValue()];
		for (int i = 0; i < numRows.intValue(); i++)
		{
			if (i < storedContext.fileNames.length)
			{
				rlist[i] = toRowID(storedContext.fileNames[i]);
				System.out.println("rlist[i]: " + rlist[i]);
			}
			else
			{
				rlist[i] = null;
				break;
			}
		}

		rowids[0] = new ODCIRidList(rlist);

		return SUCCESS;
	}

	public BigDecimal ODCIClose(ODCIEnv env) throws Exception
	{
		System.out.println("ODCIClose");
		System.out.println("key on close: " + key.intValue());
		ContextManager.clearContext(key.intValue());
		return SUCCESS;
	}
}
