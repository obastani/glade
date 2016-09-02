// Copyright 2015-2016 Stanford University
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package glade.util;

import glade.util.OracleUtils.DiscriminativeOracle;
import glade.util.OracleUtils.Oracle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ShellUtils {
	public static void delete(String filename) {
		new File(filename).delete();
	}
	
	public static void write(String query, File file) {
		file.delete();
		try {
			FileWriter fw = new FileWriter(file);
			fw.write(query);
			fw.close();
		} catch(IOException e) {
			throw new RuntimeException("Error writing seed file!", e);
		}
	}
	
	public static void write(String query, String filename) {
		write(query, new File(filename));
	}
	
	public static String read(InputStream input) {
		try {
			StringBuilder result = new StringBuilder();
			BufferedReader br = new BufferedReader(new InputStreamReader(input));
			String line;
			while((line = br.readLine()) != null) {
				result.append(line).append("\n");
			}
			br.close();
			return result.toString();
		} catch (IOException e) {
			throw new RuntimeException("Error reading program output stream!", e);
		}
	}
	
	private static Process executeNoWait(String command) {
		try {
			String[] shellCommand = {"/bin/sh", "-c", command};
			return Runtime.getRuntime().exec(shellCommand);
		} catch(Exception e) {
			throw new RuntimeException("Error executing command: " + command, e);
		}
	}
	
	public static String executeForStream(final String command, final boolean isError, long timeoutMillis) {
		final Process process = executeNoWait(command);
		Callable<String> exec = new Callable<String>() {
			public String call() {
				String result = read(isError ? process.getErrorStream() : process.getInputStream());
				try {
					process.waitFor();
				} catch (InterruptedException e) {
					throw new RuntimeException("Error executing command: " + command, e);
				}
				return result;
			}
		};
		if(timeoutMillis == -1) {
			try {
				return exec.call();
			} catch (Exception e) {
				throw new RuntimeException("Error executing command: " + command, e);
			}
		} else {
			final ExecutorService executor = Executors.newSingleThreadExecutor();
			final Future<String> future = executor.submit(exec);
			executor.shutdown();
			String result;
			try {
				result = future.get(timeoutMillis, TimeUnit.MILLISECONDS);
			} catch(Exception e) {
				process.destroy();
				result = "Timeout!";
			}
			if(!executor.isTerminated()) {
			    executor.shutdownNow();
			}
			return result;
		}
	}
	
	public static interface CommandFactory {
		public abstract String getCommand(String filename, String auxFilename, String exePath);
	}
	
	public static class SimpleCommandFactory implements CommandFactory {
		@Override
		public String getCommand(String filename, String auxFilename, String exePath) {
			return exePath + " " + filename;
		}
	}
	
	public static class ShellOracle implements Oracle {
		private final String command;
		private final String filename;
		private final String auxFilename;
		private final boolean isError;
		private final long timeoutMillis;
		
		public ShellOracle(String filename, String auxFilename, String command, boolean isError, long timeoutMillis) {
			this.filename = filename;
			this.auxFilename = auxFilename;
			this.command = command;
			this.isError = isError;
			this.timeoutMillis = timeoutMillis;
		}
		
		@Override
		public String execute(String query) {
			write("", this.auxFilename);
			write(query, this.filename);
			String result = ShellUtils.executeForStream(this.command, this.isError, this.timeoutMillis);
			delete(this.auxFilename);
			delete(this.filename);
			return result;
		}
	}
	
	public static class ExecuteDiscriminativeOracle implements DiscriminativeOracle {
		private final Oracle oracle;
		
		public ExecuteDiscriminativeOracle(Oracle oracle) {
			this.oracle = oracle;
		}

		@Override
		public boolean query(String query) {
			return this.oracle.execute(query).matches("\\s*");
		}
	}
}
