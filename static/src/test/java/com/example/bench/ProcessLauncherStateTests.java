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

import com.example.bench.CaptureSystemOutput.OutputCapture;
import com.example.bench.CdsBenchmark.CdsState;
import com.example.bench.StripBenchmark.ApplicationState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnJre;
import org.junit.jupiter.api.condition.JRE;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Dave Syer
 *
 */
public class ProcessLauncherStateTests {

	@Test
	@CaptureSystemOutput
	public void vanilla(OutputCapture output) throws Exception {
		// System.setProperty("bench.args", "-verbose:class");
		ProcessLauncherState state = new ProcessLauncherState("target", "--server.port=0");
		state.setProfiles("jlog");
		state.before();
		state.run();
		state.after();
		assertThat(output.toString()).contains("Benchmark app started");
	}

	@Test
	@CaptureSystemOutput
	public void func(OutputCapture output) throws Exception {
		ApplicationState state = new ApplicationState();
		state.sample = ApplicationState.Sample.func;
		state.addArgs("-Ddebug=true");
		state.start();
		state.run();
		state.after();
		assertThat(output.toString()).contains("FuncApplication");
		assertThat(output.toString()).contains("Benchmark app started");
	}

	@Test
	@CaptureSystemOutput
	@EnabledOnJre({ JRE.JAVA_11, JRE.JAVA_14 })
	public void cds(OutputCapture output) throws Exception {
		CdsState state = new CdsState();
		state.sample = CdsState.Sample.demo;
		state.addArgs("-Ddebug=true");
		state.start();
		state.run();
		state.after();
		assertThat(output.toString()).contains("DemoApplication");
		assertThat(output.toString()).contains("Benchmark app started");
	}

}
