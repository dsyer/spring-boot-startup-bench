/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */package com.example;

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
