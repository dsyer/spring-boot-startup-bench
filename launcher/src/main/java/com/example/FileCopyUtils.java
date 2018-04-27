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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * @author Dave Syer
 *
 */
public class FileCopyUtils {

	public static final int BUFFER_SIZE = 4096;

	public static boolean deleteRecursively(File root) {
		if (root == null) {
			return false;
		}

		try {
			return deleteRecursively(root.toPath());
		}
		catch (IOException ex) {
			return false;
		}
	}

	public static boolean deleteRecursively(Path root) throws IOException {
		if (root == null) {
			return false;
		}
		if (!Files.exists(root)) {
			return false;
		}

		Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
					throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc)
					throws IOException {
				Files.delete(dir);
				return FileVisitResult.CONTINUE;
			}
		});
		return true;
	}

	public static void copyRecursively(File src, File dest) throws IOException {
		copyRecursively(src.toPath(), dest.toPath());
	}

	public static void copyRecursively(Path src, Path dest) throws IOException {
		BasicFileAttributes srcAttr = Files.readAttributes(src,
				BasicFileAttributes.class);

		if (srcAttr.isDirectory()) {
			Files.walkFileTree(src, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult preVisitDirectory(Path dir,
						BasicFileAttributes attrs) throws IOException {
					Files.createDirectories(dest.resolve(src.relativize(dir)));
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
						throws IOException {
					Files.copy(file, dest.resolve(src.relativize(file)),
							StandardCopyOption.REPLACE_EXISTING);
					return FileVisitResult.CONTINUE;
				}
			});
		}
		else if (srcAttr.isRegularFile()) {
			Files.copy(src, dest);
		}
		else {
			throw new IllegalArgumentException(
					"Source File must denote a directory or file");
		}
	}

	public static int copy(File in, File out) throws IOException {
		return copy(Files.newInputStream(in.toPath()),
				Files.newOutputStream(out.toPath()));
	}

	public static int copy(InputStream in, OutputStream out) throws IOException {

		try {
			int byteCount = 0;
			byte[] buffer = new byte[BUFFER_SIZE];
			int bytesRead = -1;
			while ((bytesRead = in.read(buffer)) != -1) {
				out.write(buffer, 0, bytesRead);
				byteCount += bytesRead;
			}
			out.flush();
			return byteCount;
		}
		finally {
			try {
				in.close();
			}
			catch (IOException ex) {
			}
			try {
				out.close();
			}
			catch (IOException ex) {
			}
		}
	}

}
