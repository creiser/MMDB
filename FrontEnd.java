
import java.io.*;
import java.util.Base64;
import java.nio.file.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.servlet.*;

public class FrontEnd
{
	public static byte[] readAllBytes(InputStream is) throws IOException
	{
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		int nRead;
		byte[] data = new byte[16384];
		while ((nRead = is.read(data, 0, data.length)) != -1) {
			buffer.write(data, 0, nRead);
		}
		buffer.flush();
		return buffer.toByteArray();
	}


	public static void main(String[] args) throws Exception
	{
		int port = 12345;
		if (args.length > 0) {
			port = Integer.parseInt(args[0]);
		}
		Server server = new Server(port);

		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/");

		ServletHolder fileUploadServletHolder = new ServletHolder(new FrontEndServlet());
		fileUploadServletHolder.getRegistration().setMultipartConfig(new MultipartConfigElement("data/tmp"));
		context.addServlet(fileUploadServletHolder, "/");

		server.setHandler(context);
		server.start();
		server.join();

	}

	public static class FrontEndServlet extends HttpServlet
	{
		public static final String uploadForm =
			"<html>\n" +
		        "<head>\n" +
		        "    <title>MMDB</title>\n" +
		        "    <link rel=\"stylesheet\" href=\"https://cdnjs.cloudflare.com/ajax/libs/materialize/0.99.0/css/materialize.min.css\">\n" +
			"    <script type=\"text/javascript\" src=\"https://cdnjs.cloudflare.com/ajax/libs/materialize/0.99.0/js/materialize.min.js\"></script>\n" +
		        "    <script type=\"text/javascript\" src=\"https://cdnjs.cloudflare.com/ajax/libs/jquery/3.1.1/jquery.min.js\"></script>\n" +
		        "\n" +
		        "</head>\n" +
		        "<body>\n" +
		        "\n" +
		        "<nav>\n" +
		        "    <div class=\"nav-wrapper\">\n" +
		        "        <a class=\"brand-logo center\">Content Based Image Retrieval</a>\n" +
		        "        <ul id=\"nav-mobile\" class=\"right hide-on-med-and-down\">\n" +
		        "            <li><a href=\"index.jsp\">Home</a></li>\n" +
		        "            <li><a href=\"about.jsp\">About</a></li>\n" +
		        "        </ul>\n" +
		        "    </div>\n" +
		        "</nav>\n" +
		        "\n" +
		        "<div class=\"container\">\n" +
		        "\n" +
		        "    <div class=\"row\">\n" +
		        "\n" +
		        "        <form enctype=\"multipart/form-data\" method=\"post\" name=\"fileinfo\">\n" +
                "\n" +
                "            <div class=\"file-field input-field\">\n" +
                "                <div class=\"btn\">\n" +
                "                    <span> Upload Image</span>\n" +
                "                    <input type=\"file\" name=\"file\" id=\"image1\" onchange=\"readURL(this);\" required>\n" +
                "                </div>\n" +
                "                <div class=\"file-path-wrapper\">\n" +
                "                    <input class=\"file-path validate\" id=\"place\" type=\"text\" placeholder=\"Upload one or more files\">\n" +
                "                </div>\n" +
                "\n" +
                "            </div>\n" +
                "            <div class=\"row\">\n" +
                "\n" +
                "                <div class=\"col s2\">\n" +
                "                    <div>\n" +
                "                        <button class=\"btn waves-effect waves-light\" type=\"submit\">Search\n" +
                "                        </button>\n" +
                "                    </div>\n" +
                "                </div>\n" +
                "                <div class=\"col s10\">\n" +
                "                    <img id=\"blah\" style='border-radius: 10px;' />\n" +
                "                </div>\n" +
                "            </div>\n" +
                "\n" +
                "        </form>\n" +
		        "    </div>\n" +
		        "\n" +
		        "    <div id=\"results\" class=\"row\">\n" +
		        "\n" +
		        "    </div>\n" +
		        "\n" +
		        "\n" +
		        "</div>\n" +
		        "\n" +
		        "\n" +
                    "<script>\n" +
                    "    function readURL(input) {\n" +
                    "        if (input.files && input.files[0]) {\n" +
                    "            var reader = new FileReader();\n" +
                    "\n" +
                    "            reader.onload = function (e) {\n" +
                    "                $('#blah')\n" +
                    "                    .attr('src', e.target.result)\n" +
                    "                    .width(150)\n" +
                    "                    .height(150);\n" +
                    "            };\n" +
                    "            reader.readAsDataURL(input.files[0]);\n" +
                    "			 document.getElementById(\"place\").setAttribute(\"value\", document.getElementById(\"image1\").files[0].name);"+
                    "        }\n" +
                    "    }\n" +
                    "</script>\n" +
                    "\n" +
		        "<script>\n" +
		        "    var form = document.forms.namedItem(\"fileinfo\");\n" +
		        "    form.addEventListener('submit', function (ev) {\n" +
		        "\n" +
		        "        var oOutput = document.getElementById(\"results\"),\n" +
		        "            oData = new FormData(form);\n" +
		        "        oOutput.innerHTML = \"<br/><h3>Getting results, please wait ...</h3>\";\n" +
		        "        // In case we need to send extra data to the request handler which is UploadServlet in our case\n" +
		        "        oData.append(\"CustomField\", \"This is some extra data\");\n" +
		        "\n" +
		        "        var oReq = new XMLHttpRequest();\n" +
		        "        oReq.open(\"POST\", \"\", true);\n" +
		        "        oReq.onload = function (oEvent) {\n" +
		        "            if (oReq.status == 200) {\n" +
		        "                // here we update the UI after getting the response results\n" +
		        "                oOutput.innerHTML = oReq.responseText;\n" +
		        "            } else {\n" +
		        "                oOutput.innerHTML = \"Error \" + oReq.status + \" occurred when trying to upload your file.<br \\/>\";\n" +
		        "            }\n" +
		        "        };\n" +
		        "\n" +
		        "        oReq.send(oData);\n" +
		        "        ev.preventDefault();\n" +
		        "    }, false);\n" +
		        "</script>\n" +
		        "\n" +
		        "\n" +
		        "</body>\n" +
		        "</html>\n";

		@Override
		protected void doGet( HttpServletRequest request, HttpServletResponse response )
			throws ServletException, IOException
		{
			response.setContentType("text/html");
			response.setStatus(HttpServletResponse.SC_OK);
			response.getWriter().println(uploadForm);
		}

		@Override
		protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException
		{
			resp.setContentType("text/html");
			PrintWriter out = resp.getWriter();
			Part part = req.getPart("file");
			byte[] uploadedImage = readAllBytes(part.getInputStream());

			try {
				Connection conn = DriverManager.getConnection("jdbc:oracle:thin:hr/oracle@localhost:1521/orcl");
				PreparedStatement stmt = conn.prepareStatement("SELECT image FROM images WHERE is_lucene_similar(image, ?) = 1");
				stmt.setBytes(1, uploadedImage);
				ResultSet rs = stmt.executeQuery();
				
				int counter = 0;
				final int imagesPerRow = 6;
				out.println("<table>");
				while (rs.next()) {
					if (counter % imagesPerRow == 0) {
						out.println("<tr>");
					}
					String base64image = Base64.getEncoder().encodeToString(rs.getBytes(1));
					out.println("<td><img width='150' height='150' style='border-radius: 10px;' src=\"data:image/jpeg;base64," + base64image + "\"></td>");
					counter++;
					if (counter % imagesPerRow == 0) {
						out.println("</tr>");
					}
						
				}
				out.println("</table>");

				rs.close();
				conn.close();
			} catch (Exception e) {
				e.printStackTrace();
				out.println("<div class='row'><h4>Unable to establish a connection to the database :(</h4></div>");

			}


			/*out.println(uploadForm);

			for (Part part: req.getParts())
			{
				byte[] uploadedImage = readAllBytes(part.getInputStream());
				String base64image = Base64.getEncoder().encodeToString(uploadedImage);
				out.println("Uploaded file:<br>");
				out.println("<img src=\"data:image/jpeg;base64," + base64image + "\"><br>");
				out.println("Similar images:<br>");
				
				try {
					Connection conn = DriverManager.getConnection("jdbc:oracle:thin:hr/oracle@localhost:1521/orcl");
					PreparedStatement stmt = conn.prepareStatement("SELECT image FROM images WHERE is_lucene_similar(image, ?) = 1");
					stmt.setBytes(1, uploadedImage);
					ResultSet rs = stmt.executeQuery();
					
					int counter = 0;
					while (rs.next()) {
						base64image = Base64.getEncoder().encodeToString(rs.getBytes(1));
						out.println("<img src=\"data:image/jpeg;base64," + base64image + "\">");
						counter++;
						if (counter == 4) {
							out.println("<br>");
							counter = 0;
						}	
					}

					rs.close();
					conn.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}*/
		}
	}
}

