package org.tako.etl.pdi.carte.plugins;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.annotations.VisibleForTesting;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.annotations.CarteServlet;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.RepositoriesMeta;
import org.pentaho.di.www.BaseHttpServlet;
import org.pentaho.di.www.CartePluginInterface;
import org.pentaho.di.www.JobMap;

@CarteServlet( id = "ListRepositoriesServlet", name = "ListRepositoriesServlet" )
public class ListRepositoriesServlet extends BaseHttpServlet implements CartePluginInterface {

	private static Class<?> PKG = ListRepositoriesServlet.class; // i18n

	private static final long serialVersionUID = -5879219287669847356L;

	public static final String CONTEXT_PATH = "/kettle/repos/list";

	private static final byte[] XML_HEADER = XMLHandler.getXMLHeader(Const.XML_ENCODING)
			.getBytes(Charset.forName(Const.XML_ENCODING));

	public ListRepositoriesServlet() {
	}

	public ListRepositoriesServlet(JobMap jobMap) {
		super(jobMap);
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		if (isJettyMode() && !request.getContextPath().startsWith(CONTEXT_PATH)) {
			return;
		}

		if (log.isDebug()) {
			logDebug(BaseMessages.getString(PKG, "TransStatusServlet.Log.TransStatusRequested"));
		}


		response.setStatus(HttpServletResponse.SC_OK);


		response.setContentType("text/xml");
		response.setCharacterEncoding(Const.XML_ENCODING);

		OutputStream out = null;
		byte[] data = null;

		RepositoriesMeta repositories;
		try {
			repositories = listRepositories();
		} catch (KettleException ke) {
			throw new ServletException("Unable to load repositories", ke);
		}

		// Send the result back as XML
		//
		String xml = repositories.getXML();
		data = xml.getBytes(Charset.forName(Const.XML_ENCODING));
		out = response.getOutputStream();
		response.setContentLength(data.length);
		out.write(data);
		out.flush();

		response.flushBuffer();
	}


	@VisibleForTesting
	RepositoriesMeta listRepositories() throws KettleException {

		RepositoriesMeta repositoriesMeta = new RepositoriesMeta();
		repositoriesMeta.readData();

		return repositoriesMeta;
	}

	@Override
	public String toString() {
		return "List repos";
	}

	@Override
	public String getService() {
		return CONTEXT_PATH + " (" + toString() + ")";
	}


	@Override
	public String getContextPath() {
		return CONTEXT_PATH;
	}

}
