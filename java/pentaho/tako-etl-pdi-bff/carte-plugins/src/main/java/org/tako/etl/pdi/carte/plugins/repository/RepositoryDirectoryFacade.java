package org.tako.etl.pdi.carte.plugins.repository;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class RepositoryDirectoryFacade {
	public static final String XML_TAG = "dir";

	private RepositoryDirectoryInterface dir;
	private Node dirNode;
	
	private String name;

	public RepositoryDirectoryFacade() {
	}

	/**
	 * @param statusDescription
	 * @param transStatusList
	 * @param jobStatusList
	 */
	public RepositoryDirectoryFacade(RepositoryDirectoryInterface dir) {
		this.dir = dir;
	}

	public String getXML() throws KettleException {
		StringBuilder xml = new StringBuilder();

		xml.append("<" + XML_TAG + ">").append(Const.CR);
		xml.append(XMLHandler.addTagValue("dirname", dir.getName()));
		xml.append(XMLHandler.addTagValue("objectid", dir.getObjectId().getId()));
		xml.append(XMLHandler.addTagValue("subdircount", dir.getNrSubdirectories()));

		// -- list 1st level children
		xml.append("  <dir>").append(Const.CR);
		for (RepositoryDirectoryInterface subDir : dir.getChildren()) {
			xml.append("    ").append(getSubdirectoryXML(subDir)).append(Const.CR);
		}
		xml.append("  </dir>").append(Const.CR);

		xml.append("</" + XML_TAG + ">").append(Const.CR);

		return xml.toString();
	}

	public String getSubdirectoryXML(RepositoryDirectoryInterface dir) throws KettleException {
		StringBuilder xml = new StringBuilder();

		xml.append("<" + XML_TAG + ">").append(Const.CR);
		xml.append(XMLHandler.addTagValue("dirname", dir.getName()));
		xml.append(XMLHandler.addTagValue("objectid", dir.getObjectId().getId()));
		xml.append(XMLHandler.addTagValue("subdircount", dir.getNrSubdirectories()));
		xml.append("</" + XML_TAG + ">").append(Const.CR);

		return xml.toString();
	}

	public static RepositoryDirectoryFacade fromXML(String xml) throws KettleException {
		Document document = XMLHandler.loadXMLString(xml);
		return new RepositoryDirectoryFacade(XMLHandler.getSubNode(document, XML_TAG));
	}

	public RepositoryDirectoryFacade(Node dirNode) throws KettleException {
		this();
		this.name = XMLHandler.getTagValue(dirNode, "dirname");
		this.dirNode = dirNode;
		/*
		 * 
		 * Node listSubdirectoriesNode = XMLHandler.getSubNode(dirNode, XML_TAG);
		 * 
		 * int nrDirs = XMLHandler.countNodes(dirNode, XML_TAG);
		 * 
		 * for (int i = 0; i < nrDirs; i++) { Node subDirNode =
		 * XMLHandler.getSubNodeByNr(listSubdirectoriesNode, XML_TAG, i);
		 * transStatusList.add(new SlaveServerTransStatus(transStatusNode)); }
		 */
	}
}
