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

package glade.main;

import glade.util.OracleUtils.DiscriminativeOracle;
import glade.util.OracleUtils.Oracle;
import glade.util.OracleUtils.WrappedOracle;
import glade.util.OracleUtils.Wrapper;
import glade.util.ShellUtils.CommandFactory;
import glade.util.ShellUtils.ExecuteDiscriminativeOracle;
import glade.util.ShellUtils.ShellOracle;
import glade.util.ShellUtils.SimpleCommandFactory;
import glade.util.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProgramDataUtils {
	public static class FileParameters {
		public final String queryProg;
		public final String filename;
		public final String auxFilename;
		public final long timeout;
		public final String exampleTrainPath;
		public FileParameters(String queryProg, String filename, String auxFilename, long timeout, String exampleTrainPath) {
			this.queryProg = queryProg;
			this.filename = filename;
			this.auxFilename = auxFilename;
			this.timeout = timeout;
			this.exampleTrainPath = exampleTrainPath;
		}
	}
	
	public static interface ProgramData {
		public abstract Oracle getOracle();
	}
	
	public static DiscriminativeOracle getQueryOracle(ProgramData data) {
		return new ExecuteDiscriminativeOracle(data.getOracle());
	}
	
	public static interface ProgramExamples {
		public abstract List<String> getTrainExamples();
		public abstract List<String> getEmptyExamples();
	}
	
	public static class ShellProgramData implements ProgramData {
		private final FileParameters file;
		private final CommandFactory factory;
		private final String exePath;
		private final boolean isError;
		
		public ShellProgramData(FileParameters file, CommandFactory factory, String exePath, boolean isError) {
			this.file = file;
			this.factory = factory;
			this.exePath = exePath;
			this.isError = isError;
		}
		
		public ShellProgramData(FileParameters file, String exePath, boolean isError) {
			this(file, new SimpleCommandFactory(), exePath, isError);
		}
		
		@Override
		public Oracle getOracle() {
			return new ShellOracle(this.file.filename, this.file.auxFilename, this.factory.getCommand(this.file.filename, this.file.auxFilename, this.file.queryProg + File.separator + this.exePath), this.isError, this.file.timeout);
		}
	}
	
	public static class WrappedProgramData implements ProgramData {
		private final ProgramData data;
		private final Wrapper wrapper;
		
		public WrappedProgramData(ProgramData data, Wrapper wrapper) {
			this.data = data;
			this.wrapper = wrapper;
		}
		
		@Override
		public Oracle getOracle() {
			return new WrappedOracle(this.data.getOracle(), this.wrapper);
		}
	}
	
	public static class SingleFileProgramExamples implements ProgramExamples {
		private final FileParameters file;
		private final String name;
		private final String filename;
		private final String emptyExample;
		private final Wrapper exampleProcessor;
		
		public SingleFileProgramExamples(FileParameters file, String name, String filename, String emptyExample, Wrapper exampleProcessor) {
			this.file = file;
			this.name = name;
			this.filename = filename;
			this.emptyExample = emptyExample;
			this.exampleProcessor = exampleProcessor;
		}
		
		private List<String> getExamples(String path) {
			try {
				List<String> examples = new ArrayList<String>();
				BufferedReader br = new BufferedReader(new FileReader(path + File.separator + this.name + File.separator + this.filename));
				String line;
				while((line = br.readLine()) != null) {
					examples.add(this.exampleProcessor.wrap(line));
				}
				br.close();
				return examples;
			} catch(IOException e) {
				throw new RuntimeException("Error reading examples!", e);
			}
		}
		
		@Override
		public List<String> getTrainExamples() {
			return this.getExamples(this.file.exampleTrainPath);
		}
		
		@Override
		public List<String> getEmptyExamples() {
			return Utils.getList(this.emptyExample);
		}
	}
	
	public static class MultiFileProgramExamples implements ProgramExamples {
		private final FileParameters file;
		private final String name;
		private final String extension;
		private final String emptyExample;
		private final Wrapper exampleProcessor;
		
		public MultiFileProgramExamples(FileParameters file, String name, String extension, String emptyExample, Wrapper exampleProcessor) {
			this.name = name;
			this.extension = extension;
			this.emptyExample = emptyExample;
			this.exampleProcessor = exampleProcessor;
			this.file = file;
		}
		
		private List<String> getExamples(String path) {
			List<String> examples = new ArrayList<String>();
			for(File file : new File(path, this.name).listFiles()) {
				if(!file.getName().endsWith(this.extension)) {
					continue;
				}
				try {
					StringBuilder sb = new StringBuilder();
					BufferedReader br = new BufferedReader(new FileReader(file));
					String line;
					while((line = br.readLine()) != null) {
						sb.append(line).append("\n");
					}
					br.close();
					examples.add(this.exampleProcessor.wrap(sb.toString()));
				} catch(IOException e) {
					throw new RuntimeException("Error reading examples!", e);
				}
			}
			return examples;
		}
		
		@Override
		public List<String> getTrainExamples() {
			return this.getExamples(this.file.exampleTrainPath);
		}

		@Override
		public List<String> getEmptyExamples() {
			return Utils.getList(this.emptyExample);
		}
	}
}
