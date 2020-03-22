/*! ******************************************************************************
*
* Pentaho Data Integration
*
* Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
*
*******************************************************************************
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with
* the License. You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
******************************************************************************/

package org.pentaho.di.sdk.samples.carte;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.jetty.util.log.Slf4jLog;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.xml.XMLHandler;
import org.w3c.dom.Node;

public class RepoExplorerSampleTest extends BaseCarteServletTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		System.setProperty("KETTLE_PLUGIN_CLASSES", "org.tako.etl.pdi.carte.plugins.RepositoryExplorerServlet");

		BaseCarteServletTest.setUpBeforeClass();
	}

	@Test
	public void testGetAuthString() {
		assertEquals("Basic YWRtaW46cGFzc3dvcmQ=", RepoExplorerSample.getAuthString("admin", "password"));
	}

	@Test
	public void testGetTreeCmdUrlString() {
		assertEquals("http://sample:1000/kettle/repoexplorer/tree?xml=Y",
				RepoExplorerSample.getUrlString("sample", "1000", "tree", ""));
	}

	//@Test
	public void testTreeXMLResponse() throws Exception {
		String url = RepoExplorerSample.getUrlString(hostname, port, "tree", "&rep="+"q7l repo");
		String auth = RepoExplorerSample.getAuthString(CARTE_USERNAME, CARTE_PASSWORD);
		
		System.out.println(url);

		String response = RepoExplorerSample.sendGetRepoRootDirRequest(url, auth);
		assertNotNull(response);
		Node repDir = XMLHandler.getSubNode(XMLHandler.loadXMLString(response), "repdir");
		int repositoryNr = XMLHandler.countNodes(XMLHandler.getSubNode( repDir, "subdirs" ), "repdir");
		assertTrue(repositoryNr > 0);
	}

	/*
	 * @Test public void testMain() throws Exception { RepoExplorerSample.main( new
	 * String[]{ hostname, port, CARTE_USERNAME, CARTE_PASSWORD } ); }
	 */
}
