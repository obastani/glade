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
import glade.main.ProgramDataUtils.ShellProgramData;
import glade.util.OracleUtils.IdentityWrapper;

public class PythonData {
	public static final String PYTHON_EXE = "python/Python-2.7.10/python";
	public static final boolean PYTHON_IS_ERROR = true;
	
	public static final String PYTHON_EXTENSION = ".py";
	public static final String PYTHON_EMPTY = "";
	
	public static final String PYTHON_NAME = "python";
	public static final ProgramData PYTHON_DATA = new ShellProgramData(Files.FILE_PARAMETERS, PYTHON_EXE, PYTHON_IS_ERROR);
	
	public static final ProgramExamples PYTHON_EXAMPLES = new MultiFileProgramExamples(Files.FILE_PARAMETERS, PYTHON_NAME, PYTHON_EXTENSION, PYTHON_EMPTY, new IdentityWrapper());
}
