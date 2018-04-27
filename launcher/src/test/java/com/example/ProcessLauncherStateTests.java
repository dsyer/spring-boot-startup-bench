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
 */

package com.example;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;

import org.springframework.boot.test.rule.OutputCapture;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Dave Syer
 *
 */
public class ProcessLauncherStateTests {

	@Rule
	public OutputCapture output = new OutputCapture();

	@Test
	public void main() throws Exception {
		// System.setProperty("debug", "true");
		ProcessLauncherState
				.setLauncherArgs("target/it/support/target/demo-1.0.0-exec.jar");
		ProcessLauncherState state = new ProcessLauncherState("target/test",
				"--server.port=0") {
			@Override
			public void run() throws Exception {
				mainClassFromManifest();
				super.run();
			}
		};
		state.run();
		state.after();
		output.flush();
		assertThat(output.toString()).contains("Benchmark app started");
		state.clean();
	}

	@Test
	public void start() throws Exception {
		// System.setProperty("debug", "true");
		ProcessLauncherState
				.setLauncherArgs("target/it/support/target/demo-1.0.0-exec.jar");
		ProcessLauncherState state = new ProcessLauncherState("target/test",
				"--server.port=0") {
			@Override
			public void run() throws Exception {
				startClassFromManifest();
				super.run();
			}
		};
		state.run();
		state.after();
		output.flush();
		assertThat(output.toString()).contains("Benchmark app started");
		state.clean();
	}

	@Test
	public void fatJar() throws Exception {
		// System.setProperty("debug", "true");
		ProcessLauncherState
				.setLauncherArgs("target/it/support/target/demo-1.0.0-exec.jar");
		ProcessLauncherState state = new ProcessLauncherState("target/test",
				"--server.port=0") {
			@Override
			protected void customize(List<String> args) {
				args.add("-jar");
				args.add(jarFile());
			}

		};
		state.setMarker("Tomcat started");
		state.run();
		state.after();
		output.flush();
		assertThat(output.toString()).contains("Tomcat started");
		state.clean();
	}

	@Test
	public void thinMain() throws Exception {
		System.setProperty("debug", "true");
		ProcessLauncherState
				.setLauncherArgs("target/it/support/target/demo-1.0.0-thin.jar");
		ProcessLauncherState state = new ProcessLauncherState("target/test",
				"--server.port=0", "--thin.debug") {
			@Override
			public void run() throws Exception {
				mainClassFromManifest();
				super.run();
			}
		};
		state.run();
		state.after();
		output.flush();
		assertThat(output.toString()).contains("Benchmark app started");
		state.clean();
	}

	@Test
	public void thinStart() throws Exception {
		// System.setProperty("debug", "true");
		ProcessLauncherState
				.setLauncherArgs("target/it/support/target/demo-1.0.0-thin.jar");
		ProcessLauncherState state = new ProcessLauncherState("target/test",
				"--server.port=0", "--thin.debug") {
			@Override
			public void run() throws Exception {
				startClassFromManifest();
				super.run();
			}
		};
		state.run();
		state.after();
		output.flush();
		assertThat(output.toString()).contains("Benchmark app started");
		state.clean();
	}

	@Test
	public void thinJar() throws Exception {
		// System.setProperty("debug", "true");
		ProcessLauncherState
				.setLauncherArgs("target/it/support/target/demo-1.0.0-thin.jar");
		ProcessLauncherState state = new ProcessLauncherState("target/test",
				"--server.port=0") {
			@Override
			protected void customize(List<String> args) {
				args.add("-jar");
				args.add(jarFile());
			}

		};
		state.setMarker("Tomcat started");
		state.run();
		state.after();
		output.flush();
		assertThat(output.toString()).contains("Tomcat started");
		state.clean();
	}

}
