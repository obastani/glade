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
import glade.util.ShellUtils.CommandFactory;


public class SedData {
	public static final String SED_EXE = "sed/sed-4.2.2/sed/sed";
	public static final boolean SED_IS_ERROR = true;
	
	public static final String SED_EXTENSION = ".sed";
	public static final String SED_EMPTY = "";
	
	public static final String SED_NAME = "sed";
	public static final ProgramData SED_DATA = new ShellProgramData(Files.FILE_PARAMETERS, new SedCommandFactory(), SED_EXE, SED_IS_ERROR);
	public static final ProgramExamples SED_EXAMPLES = new MultiFileProgramExamples(Files.FILE_PARAMETERS, SED_NAME, SED_EXTENSION, SED_EMPTY, new IdentityWrapper());
	
	public static class SedCommandFactory implements CommandFactory {
		@Override
		public String getCommand(String filename, String auxFilename, String exePath) {
			return exePath + " -f " + filename + " " + auxFilename;
		}
	}
}
