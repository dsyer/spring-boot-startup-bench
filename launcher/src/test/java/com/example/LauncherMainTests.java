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

import com.example.LauncherMain.Args;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Dave Syer
 *
 */
public class LauncherMainTests {

	@Test
	public void justJar() {
		Args args = new Args(new String[] { "my.jar" });
		assertThat(args.getProgArgs()[0]).isEqualTo("my.jar");
		assertThat(args.getJmhArgs()).isEmpty();
	}

	@Test
	public void jarPlusJmh() {
		Args args = new Args(new String[] { "my.jar", "--", "-i", "1" });
		assertThat(args.getProgArgs()[0]).isEqualTo("my.jar");
		assertThat(args.getJmhArgs()).contains("-i");
		assertThat(args.getJmhArgs()).hasSize(2);
	}

	@Test
	public void jarPlusArgsPlusJmh() {
		Args args = new Args(
				new String[] { "my.jar", "--server.port=0", "--", "-i", "1" });
		assertThat(args.getProgArgs()[0]).isEqualTo("my.jar");
		assertThat(args.getProgArgs()).hasSize(2);
		assertThat(args.getJmhArgs()).contains("-i");
		assertThat(args.getJmhArgs()).hasSize(2);
	}

}
