package org.tako.etl.pdi.repo.ws.model;

public class BrowseModel {

	private String targetDirPath;
	private String targetDirName;
	private String repo;
	private String user;
	private String pass;

	public String getTargetDir() {
		return targetDirPath;
	}

	public void setTargetDir(String targetDir) {
		this.targetDirPath = targetDir;
	}

	public String getTargetDirName() {
		return targetDirName;
	}

	public void setTargetDirName(String targetDirName) {
		this.targetDirName = targetDirName;
	}

	public String getRepo() {
		return repo;
	}

	public void setRepo(String repo) {
		this.repo = repo;
	}

	public String getTargetDirPath() {
		return targetDirPath;
	}

	public void setTargetDirPath(String targetDirPath) {
		this.targetDirPath = targetDirPath;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPass() {
		return pass;
	}

	public void setPass(String pass) {
		this.pass = pass;
	}
	
}
