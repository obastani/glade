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

public class XmlData {
	public static final String XML_EXE = "xml/libxml2-2.9.2/xmllint";
	public static final boolean XML_IS_ERROR = true;
	public static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>";
	public static final String XML_EXTENSION = ".xml";
	public static final String XML_EMPTY = "<a/>";
	
	public static final String XML_NAME = "xml";
	public static final ProgramData XML_DATA = new ShellProgramData(Files.FILE_PARAMETERS, XML_EXE, XML_IS_ERROR);
	public static final ProgramExamples XML_EXAMPLES = new MultiFileProgramExamples(Files.FILE_PARAMETERS, XML_NAME, XML_EXTENSION, XML_EMPTY, new IdentityWrapper());
	
	public static final String XML_EXAMPLE = "seed.xml";
}
