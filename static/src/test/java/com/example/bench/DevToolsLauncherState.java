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
 */package com.example.bench;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DevToolsLauncherState extends ProcessLauncherState {

	private static String[] defaultArgs = new String[] { "--server.port=0",
			"--spring.devtools.livereload.enabled=false",
			"--spring.devtools.restart.pollInterval=10",
			"--spring.devtools.restart.quietPeriod=1", "--spring.jmx.enabled=false" };

	private Path restart;
	private int count = 0;

	public DevToolsLauncherState(String dir, String restart, String... args) {
		super(dir, enhance(args));
		this.restart = new File(dir, restart).toPath();
	}

	private static String[] enhance(String[] args) {
		String[] result = new String[args.length + defaultArgs.length];
		System.arraycopy(defaultArgs, 0, result, 0, defaultArgs.length);
		if (args.length > 0) {
			System.arraycopy(args, 0, result, defaultArgs.length, args.length);
		}
		return result;
	}

	@Override
	public void before() throws Exception {
		super.before();
		super.run();
	}

	@Override
	public void run() throws Exception {
		monitor();
	}

	public void update() throws Exception {
		drain();
		Files.write(restart, (new Date().toString() + IntStream.range(0, count++)
				.mapToObj(i -> "" + i).collect(Collectors.joining(","))).getBytes());
		finish();
	}

}
