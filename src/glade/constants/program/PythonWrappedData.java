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

package glade.constants.program;

import glade.constants.Files;
import glade.main.ProgramDataUtils.MultiFileProgramExamples;
import glade.main.ProgramDataUtils.ProgramData;
import glade.main.ProgramDataUtils.ProgramExamples;
import glade.main.ProgramDataUtils.WrappedProgramData;
import glade.util.OracleUtils.IdentityWrapper;
import glade.util.OracleUtils.Wrapper;

public class PythonWrappedData {
	public static final String PYTHON_WRAPPED_NAME = "python_wrapped";
	public static final String PYTHON_WRAPPED_EMPTY = "pass";
	public static final ProgramData PYTHON_WRAPPED_DATA = new WrappedProgramData(PythonData.PYTHON_DATA, new PythonWrapper());
	
	public static final ProgramExamples PYTHON_EXAMPLES = new MultiFileProgramExamples(Files.FILE_PARAMETERS, PYTHON_WRAPPED_NAME, PythonData.PYTHON_EXTENSION, PYTHON_WRAPPED_EMPTY, new IdentityWrapper());
	
	public static class PythonWrapper implements Wrapper {
		@Override
		public String wrap(String input) {
			StringBuilder sb = new StringBuilder();
			
			// header
			sb.append("if False:\n");
			
			// query
			boolean isEmpty = true;
			for(String line : input.split("\n")) {
				sb.append("    ").append(line).append("\n");
				isEmpty = false;
			}
			
			// pass if needed
			if(isEmpty) {
				sb.append("    pass");
			}
			
			return sb.toString();
		}
	}
}
