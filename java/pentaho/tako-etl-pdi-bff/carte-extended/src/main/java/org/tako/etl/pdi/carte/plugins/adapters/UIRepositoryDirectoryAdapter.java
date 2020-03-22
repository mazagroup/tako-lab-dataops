package org.tako.etl.pdi.carte.plugins.adapters;

import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryDirectory;

public class UIRepositoryDirectoryAdapter extends UIRepositoryDirectory {

	public static final String XML_TAG = "repdir";

	public UIRepositoryDirectoryAdapter() {
		super();
	}

	public UIRepositoryDirectoryAdapter(RepositoryDirectoryInterface rd, UIRepositoryDirectory uiParent,
			Repository rep) {
		super(rd, uiParent, rep);
	}

	public String toXML() {
		return getXML( getDirectory(), 0 );
	}

	private static String getXML( RepositoryDirectory dir,int level) {
		String spaces = Const.rightPad(" ", level);
		StringBuilder retval = new StringBuilder(200);

		retval.append(spaces).append("<repdir>").append(Const.CR);
		retval.append(spaces).append("  ").append(XMLHandler.addTagValue("name", dir.getName()));
		retval.append(spaces).append("  ").append(XMLHandler.addTagValue("path", dir.getPath()));

		if (dir.getNrSubdirectories() > 0) {
			retval.append(spaces).append("    <subdirs>").append(Const.CR);
			for (int i = 0; i < dir.getNrSubdirectories(); i++) {
				RepositoryDirectory subdir = dir.getSubdirectory(i);
				retval.append(getXML(subdir,level + 1));
			}
			retval.append(spaces).append("    </subdirs>").append(Const.CR);
		}

		retval.append(spaces).append("</repdir>").append(Const.CR);

		return retval.toString();
	}

}
