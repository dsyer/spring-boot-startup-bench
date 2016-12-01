package com.example;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;

public class DevToolsLauncherState extends ProcessLauncherState {

	private Path restart;
	private int count = 0;

	public DevToolsLauncherState(String dir, String restart, String jar, String... args) {
		super(dir, args);
		this.restart = new File(dir, restart).toPath();
		unpack(dir, jar);
	}
	
	public void setup() throws Exception {
		super.run();
	}

	@Override
	public void run() throws Exception {
		update();
		monitor();
	}

	protected void update() throws IOException {
		Files.write(restart, (new Date().toString() + (count++ % 2 == 1 ? " odd " + count : " even " + count)).getBytes());
	}

}
