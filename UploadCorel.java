
import oracle.jdbc.*;
import oracle.sql.*;
import java.sql.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class UploadCorel
{
	public static void main(String[] args) throws Exception {
		uploadAllImages();
		System.out.println("\nUpload complete. Creating index.. this may take a couple of minutes..\n");

		createIndex();
		System.out.println("Index created.");
	}

	public static void uploadAllImages() throws Exception {
		Connection conn = DriverManager.getConnection("jdbc:oracle:thin:hr/oracle@localhost:1521/orcl");

		File folder = new File("corel10k");
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {
			if ((i + 1) % 100 == 0 || i == listOfFiles.length - 1) {
				System.out.print("\rUploaded " + (i + 1) + " of " +
					listOfFiles.length + " images.");
			}
			if (listOfFiles[i].isFile()) {
				uploadImage(listOfFiles[i].getAbsolutePath(), conn);
			}
		}

		conn.close();
	}

	public static void dropIndex() {
		Connection conn = null;
		try {
			conn = DriverManager.getConnection("jdbc:oracle:thin:hr/oracle@localhost:1521/orcl");
			Statement dropStmt = conn.createStatement();
			dropStmt.executeUpdate("DROP INDEX ImagesIndex FORCE");
			//System.out.println("Index dropped.");
		} catch (Exception e) {
			//e.printStackTrace();
		} finally {
			try {
				conn.close();
			} catch (Exception e) {}
		}
	}

	public static void createIndex() {
		boolean success = false;
		while (!success) {
			success = true;

			// Index gets either dropped here, or it didn't exist anyway.
			dropIndex();

			Connection conn = null;
			try {
				// It's important to set a high timeout for this query, otherwise it will generate
				// "no more data to read from socket" exceptions.
				Properties props = new Properties();
				props.setProperty(OracleConnection.CONNECTION_PROPERTY_THIN_NET_CONNECT_TIMEOUT, "60000");
				conn = DriverManager.getConnection("jdbc:oracle:thin:hr/oracle@localhost:1521/orcl", props);

				// This often fails, because the connection gets a time out.
				// Just retry until it works.
				Statement stmt = conn.createStatement();
				stmt.executeUpdate("CREATE INDEX ImagesIndex ON Images(image) INDEXTYPE IS LuceneIndex");

				// Health check: Use index to query images, this should return 12 images.
				System.out.println("Performing health check, query should return 12 images.");
				PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(*) FROM IMAGES WHERE is_lucene_similar(image, ?) = 1");
				pstmt.setBytes(1, loadImage(new File("corel10k/1.jpg").getAbsolutePath()));
				ResultSet rs = pstmt.executeQuery();
				if (!rs.next()) {
					System.out.println("Image table is missing?");
					success = false;
				} else  {
					System.out.println("Test query retrieved rows: " + rs.getInt(1));
					if (rs.getInt(1) != 12) {
						System.out.println("Retrieved less than 12 rows. Restarting index..");
						success = false;
					} else {
						System.out.println("Health check: OK.");
					}
				}
				
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Creating the index failed due to a connection timeout. Retrying..");
				success = false;
			} finally {
				try {
					conn.close();
				} catch (Exception e) {}
			}
		}
	}

	public static byte[] loadImage(String fileName) throws Exception
	{
		Path path = Paths.get(fileName);
		return Files.readAllBytes(path);
	}


	public static void uploadImage(String fileName, Connection conn) throws Exception
	{
		String sql = "INSERT INTO images VALUES(?)";

		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setBytes(1, loadImage(fileName));
		pstmt.executeUpdate();

		if (pstmt != null)
			pstmt.close();
	}
}
