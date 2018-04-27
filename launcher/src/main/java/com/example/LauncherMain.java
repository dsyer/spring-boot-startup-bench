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

import org.openjdk.jmh.Main;

/**
 * @author Dave Syer
 *
 */
public class LauncherMain {

	public static void main(String[] args) throws Exception {
		Args cmd = new Args(args);
		String[] argv = cmd.getJmhArgs();
		ProcessLauncherState.setLauncherArgs(cmd.getProgArgs());
		Main.main(argv);
	}

	static class Args {

		private String[] jmhArgs = new String[0];
		private String[] progArgs = new String[0];

		public Args(String[] args) {
			int index = -1;
			for (int i = 0; i < args.length; i++) {
				String string = args[i];
				if ("--".equals(string)) {
					index = i;
					break;
				}
			}
			if (index >= 0 && index < args.length) {
				jmhArgs = new String[args.length - index - 1];
				System.arraycopy(args, index + 1, jmhArgs, 0, jmhArgs.length);
			}
			if (index < 0) {
				index = args.length;
			}
			if (index > 0) {
				progArgs = new String[index];
				System.arraycopy(args, 0, progArgs, 0, progArgs.length);
			}
		}

		public String[] getJmhArgs() {
			return jmhArgs;
		}

		public String[] getProgArgs() {
			return progArgs;
		}

	}
}
