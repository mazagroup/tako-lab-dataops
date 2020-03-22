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
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.RepositoryPluginType;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.RepositoriesMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryDirectories;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryDirectory;
import org.pentaho.di.www.BaseHttpServlet;
import org.pentaho.di.www.CartePluginInterface;
import org.pentaho.di.www.JobMap;
import org.pentaho.di.www.WebResult;
import org.tako.etl.pdi.carte.plugins.adapters.UIRepositoryDirectoryAdapter;

@CarteServlet(id = "RepositoryExplorerServlet", name = "RepositoryExplorerServlet")
public class RepositoryExplorerServlet extends BaseHttpServlet implements CartePluginInterface {

	private static Class<?> PKG = RepositoryExplorerServlet.class; // i18n

	private static final long serialVersionUID = -5879219287669847356L;

	public static final String CONTEXT_PATH = "/kettle/repoexplorer";

	private static final byte[] XML_HEADER = XMLHandler.getXMLHeader(Const.XML_ENCODING)
			.getBytes(Charset.forName(Const.XML_ENCODING));

	public RepositoryExplorerServlet() {
	}

	public RepositoryExplorerServlet(JobMap jobMap) {
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

		String repOption = request.getParameter("rep");
		String userOption = request.getParameter("user");
		String passOption = Encr.decryptPasswordOptionallyEncrypted(request.getParameter("pass"));
		String levelOption = request.getParameter("level");

		response.setStatus(HttpServletResponse.SC_OK);

		response.setContentType("text/xml");
		response.setCharacterEncoding(Const.XML_ENCODING);

		OutputStream out = null;
		byte[] data = null;

		// -- Process CMD
		// - for now, hardcoded commands
		String resultXml = null;

		String cmd = getExplorerCmd(request);
		
		try {
			switch (cmd) {
			case "tree":
				resultXml = loadRepositoryDirectoryTree(repOption, userOption, passOption);
				break;
			case "add":
				// code block
				break;
			default:
				// code block
			}

			data = resultXml.getBytes(Charset.forName(Const.XML_ENCODING));
			out = response.getOutputStream();
            response.setContentLength( XML_HEADER.length + data.length );
            out.write( XML_HEADER );
            out.write( data );
			out.flush();

			response.flushBuffer();
		} catch (Exception ex) {
			throw new ServletException( "Error processing explorer command: " + cmd, ex );

		}
	}

	private String loadRepositoryDirectoryTree(String repOption, String userOption, String passOption)
			throws KettleException {
		final Repository repo = openRepository(repOption, userOption, passOption);

		final RepositoryDirectoryInterface tree = repo.loadRepositoryDirectoryTree();
		final UIRepositoryDirectoryAdapter repoDirModel = new UIRepositoryDirectoryAdapter(tree, null, repo);
		return repoDirModel.toXML();
	}

	private Repository openRepository(String repositoryName, String user, String pass) throws KettleException {

		if (Utils.isEmpty(repositoryName)) {
			return null;
		}

		RepositoriesMeta repositoriesMeta = new RepositoriesMeta();
		repositoriesMeta.readData();
		RepositoryMeta repositoryMeta = repositoriesMeta.findRepository(repositoryName);
		if (repositoryMeta == null) {
			throw new KettleException("Unable to find repository: " + repositoryName);
		}
		PluginRegistry registry = PluginRegistry.getInstance();
		Repository repository = registry.loadClass(RepositoryPluginType.class, repositoryMeta, Repository.class);
		repository.init(repositoryMeta);
		repository.connect(user, pass);
		return repository;
	}

	private String getExplorerCmd(HttpServletRequest request) {
		final String requestPath = request.getRequestURI();
		return requestPath.substring(CONTEXT_PATH.length()+1);
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
