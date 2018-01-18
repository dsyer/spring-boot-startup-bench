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

package com.example.bench;

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
	public void vanilla() throws Exception {
		ProcessLauncherState state = new ProcessLauncherState("target",
				"--server.port=0");
		state.setProfiles("old");
		state.before();
		state.run();
		state.after();
		output.flush();
		assertThat(output.toString()).contains("Benchmark app started");
	}

}
