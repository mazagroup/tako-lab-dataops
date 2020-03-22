package org.tako.etl.pdi.carte.plugins.repository;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.annotations.CarteServlet;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.logging.SimpleLoggingObject;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.RepositoryPluginType;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.RepositoriesMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.repository.filerep.KettleFileRepository;
import org.pentaho.di.repository.filerep.KettleFileRepositoryMeta;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransAdapter;
import org.pentaho.di.trans.TransConfiguration;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.www.BaseHttpServlet;
import org.pentaho.di.www.CartePluginInterface;
import org.pentaho.di.www.TransformationMap;
import org.pentaho.di.www.WebResult;
import org.w3c.dom.Document;

@CarteServlet( id = "EnsureRepositoryServlet", name = "EnsureRepositoryServlet" )
public class EnsureRepositoryServlet extends BaseHttpServlet implements CartePluginInterface {

 private static Class<?> PKG = EnsureRepositoryServlet.class; // i18n

 private static final long serialVersionUID = -5879219287669847357L;

 public static final String CONTEXT_PATH = "/kettle/ensureRepo";
 
 protected Repository repository;
 protected RepositoryDirectoryInterface tree;
 protected String virtualFolder;

 public EnsureRepositoryServlet() {
 }

 public EnsureRepositoryServlet( TransformationMap transformationMap ) {
   super( transformationMap );
 }

 public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException,
   IOException {
   if ( isJettyMode() && !request.getContextPath().startsWith( CONTEXT_PATH ) ) {
     return;
   }

   if ( log.isDebug() ) {
     logDebug( BaseMessages.getString( PKG, "EnsureRepositoryServlet.Log.ExecuteTransRequested" ) );
   }

   final String xml = IOUtils.toString( request.getInputStream() );

   
   // Options taken from PAN
   //
   String[] knownOptions = new String[] { "rep", "user", "pass", "trans", "level", };

   String repOption = request.getParameter( "rep" );
   String userOption = request.getParameter( "user" );
   String passOption = Encr.decryptPasswordOptionallyEncrypted( request.getParameter( "pass" ) );
   String transOption = request.getParameter( "trans" );
   String levelOption = request.getParameter( "level" );

   response.setStatus( HttpServletResponse.SC_OK );

   String encoding = System.getProperty( "KETTLE_DEFAULT_SERVLET_ENCODING", null );
   if ( encoding != null && !Utils.isEmpty( encoding.trim() ) ) {
     response.setCharacterEncoding( encoding );
     response.setContentType( "text/html; charset=" + encoding );
   }

   PrintWriter out = response.getWriter();

   try {
	 final Document rootDir = XMLHandler.loadXMLString(xml);
     final Repository repository = ensureRepository( rootDir );
     final TransMeta transMeta = loadTransformation( repository, transOption );

     // Set the servlet parameters as variables in the transformation
     //
     String[] parameters = transMeta.listParameters();
     Enumeration<?> parameterNames = request.getParameterNames();
     while ( parameterNames.hasMoreElements() ) {
       String parameter = (String) parameterNames.nextElement();
       String[] values = request.getParameterValues( parameter );

       // Ignore the known options. set the rest as variables
       //
       if ( Const.indexOfString( parameter, knownOptions ) < 0 ) {
         // If it's a trans parameter, set it, otherwise simply set the variable
         //
         if ( Const.indexOfString( parameter, parameters ) < 0 ) {
           transMeta.setVariable( parameter, values[0] );
         } else {
           transMeta.setParameterValue( parameter, values[0] );
         }
       }
     }

     TransExecutionConfiguration transExecutionConfiguration = new TransExecutionConfiguration();
     LogLevel logLevel = LogLevel.getLogLevelForCode( levelOption );
     transExecutionConfiguration.setLogLevel( logLevel );
     TransConfiguration transConfiguration = new TransConfiguration( transMeta, transExecutionConfiguration );

     String carteObjectId = UUID.randomUUID().toString();
     SimpleLoggingObject servletLoggingObject =
       new SimpleLoggingObject( CONTEXT_PATH, LoggingObjectType.CARTE, null );
     servletLoggingObject.setContainerObjectId( carteObjectId );
     servletLoggingObject.setLogLevel( logLevel );

     // Create the transformation and store in the list...
     //
     final Trans trans = new Trans( transMeta, servletLoggingObject );

     trans.setRepository( repository );
     trans.setSocketRepository( getSocketRepository() );

     getTransformationMap().addTransformation( transMeta.getName(), carteObjectId, trans, transConfiguration );
     trans.setContainerObjectId( carteObjectId );

     if ( repository != null ) {
       // The repository connection is open: make sure we disconnect from the repository once we
       // are done with this transformation.
       //
       trans.addTransListener( new TransAdapter() {
         @Override public void transFinished( Trans trans ) {
           repository.disconnect();
         }
       } );
     }

     // Pass the servlet print writer to the transformation...
     //
     trans.setServletPrintWriter( out );
     trans.setServletReponse( response );
     trans.setServletRequest( request );

     try {
       // Execute the transformation...
       //
       executeTrans( trans );
       out.flush();

     } catch ( Exception executionException ) {
       String logging = KettleLogStore.getAppender().getBuffer( trans.getLogChannelId(), false ).toString();
       throw new KettleException( "Error executing transformation: " + logging, executionException );
     }
   } catch ( Exception ex ) {

     out.println( new WebResult( WebResult.STRING_ERROR, BaseMessages.getString(
       PKG, "ExecuteTransServlet.Error.UnexpectedError", Const.CR + Const.getStackTracker( ex ) ) ) );
   }
 }

 private TransMeta loadTransformation( Repository repository, String trans ) throws KettleException {

   if ( repository == null ) {

     // Without a repository it's a filename --> file:///foo/bar/trans.ktr
     //
     TransMeta transMeta = new TransMeta( trans );
     return transMeta;

   } else {

     // With a repository we need to load it from /foo/bar/Transformation
     // We need to extract the folder name from the path in front of the name...
     //
     String directoryPath;
     String name;
     int lastSlash = trans.lastIndexOf( RepositoryDirectory.DIRECTORY_SEPARATOR );
     if ( lastSlash < 0 ) {
       directoryPath = "/";
       name = trans;
     } else {
       directoryPath = trans.substring( 0, lastSlash );
       name = trans.substring( lastSlash + 1 );
     }
     RepositoryDirectoryInterface directory =
       repository.loadRepositoryDirectoryTree().findDirectory( directoryPath );
     if ( directory == null ) {
       throw new KettleException( "Unable to find directory path '" + directoryPath + "' in the repository" );
     }

     ObjectId transformationID = repository.getTransformationID( name, directory );
     if ( transformationID == null ) {
       throw new KettleException( "Unable to find transformation '" + name + "' in directory :" + directory );
     }
     TransMeta transMeta = repository.loadTransformation( transformationID, null );
     return transMeta;
   }
 }
 
 private Repository ensureRepository() {
     virtualFolder = "ram://file-repo/" + UUID.randomUUID();
     KettleVFS.getFileObject( virtualFolder ).createFolder();

     KettleFileRepositoryMeta repositoryMeta =
       new KettleFileRepositoryMeta( "KettleFileRepository", "FileRep", "File repository", virtualFolder );
     repository = new KettleFileRepository();
     repository.init( repositoryMeta );

     // Test connecting... (no security needed)
     //
     repository.connect( null, null );
     assertTrue( repository.isConnected() );

     // Test loading the directory tree
     //
     tree = repository.loadRepositoryDirectoryTree();
     assertNotNull( tree );
 }

 private Repository openRepository( String repositoryName, String user, String pass ) throws KettleException {

   if ( Utils.isEmpty( repositoryName ) ) {
     return null;
   }

   RepositoriesMeta repositoriesMeta = new RepositoriesMeta();
   repositoriesMeta.readData();
   RepositoryMeta repositoryMeta = repositoriesMeta.findRepository( repositoryName );
   if ( repositoryMeta == null ) {
     throw new KettleException( "Unable to find repository: " + repositoryName );
   }
   PluginRegistry registry = PluginRegistry.getInstance();
   Repository repository = registry.loadClass( RepositoryPluginType.class, repositoryMeta, Repository.class );
   repository.init( repositoryMeta );
   repository.connect( user, pass );
   return repository;
 }

 public String toString() {
   return "Start transformation";
 }

 public String getService() {
   return CONTEXT_PATH + " (" + toString() + ")";
 }

 protected void executeTrans( Trans trans ) throws KettleException {
   trans.prepareExecution( null );
   trans.startThreads();
   trans.waitUntilFinished();
 }

 public String getContextPath() {
   return CONTEXT_PATH;
 }

}
