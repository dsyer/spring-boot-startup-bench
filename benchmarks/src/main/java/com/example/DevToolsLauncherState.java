package com.example;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
		Files.write(restart, (new Date().toString() + IntStream.range(0, count++)
				.mapToObj(i -> "" + i).collect(Collectors.joining(","))).getBytes());
	}

}
