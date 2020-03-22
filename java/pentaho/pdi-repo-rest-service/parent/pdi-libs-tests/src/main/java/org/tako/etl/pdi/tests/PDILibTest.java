package org.tako.etl.pdi.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.nio.file.Paths;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.filerep.KettleFileRepository;
import org.pentaho.di.repository.filerep.KettleFileRepositoryMeta;

/*@Capability(namespace=SERVICE_NAMESPACE, 
    attribute="objectClass=org.osgi.enroute.examples.microservice.dao.PersonDao")*/
public class PDILibTest {

    private final Bundle bundle = FrameworkUtil.getBundle(this.getClass());
    
    protected KettleFileRepository repository;
    protected RepositoryDirectoryInterface tree;

    protected String virtualFolder;
    
    @Before
    public void setUp() throws Exception {
        KettleEnvironment.init();

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
    
    @After
    public void tearDown() throws Exception {
        try {
            KettleVFS.getFileObject( virtualFolder ).deleteAll();
            // remove residual files
            FileUtils.deleteDirectory( Paths.get( virtualFolder ).getParent().getParent().toFile() );
          } catch ( Exception ignored ) {
            //
          }
    }
    
    @Test
    public void testCurrentDirJob() throws Exception {
      final String dirName = "dirName";
      final String jobName = "job";
      JobMeta setupJobMeta = new JobMeta();
      setupJobMeta.setName( jobName );
      RepositoryDirectoryInterface repoDir = repository.createRepositoryDirectory( new RepositoryDirectory(), dirName );
      setupJobMeta.setRepositoryDirectory( repoDir );
      repository.save( setupJobMeta, "" );

      JobMeta jobMeta = repository.loadJob( jobName, repoDir, null, "" );
      assertEquals( repository, jobMeta.getRepository() );
      assertEquals( repoDir.getPath(), jobMeta.getRepositoryDirectory().getPath() );

      jobMeta.setInternalKettleVariables();
      String currentDir = jobMeta.getVariable( Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY );
      assertEquals( repoDir.getPath(), currentDir );
    }
}
