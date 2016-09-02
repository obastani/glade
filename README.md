GLADE
=====

GLADE is a tool for automatically synthesizing program input grammars, i.e., a context-free grammar encoding the language of valid inputs for a target program. GLADE requires two inputs: (i) an query oracle that responds whether a given input is valid for the target program, and (ii) a set of examples of valid inputs, known as seed inputs. From these inputs, GLADE produces a grammar that can be sampled to produce new program inputs.

For a detailed introduction to GLADE, see:

- [**Synthesizing Program Input Grammars**](http://arxiv.org/abs/1608.01723)

Table of Contents
=====
0. Prerequisites
1. Downloading and Building GLADE
2. Running GLADE
3. Using GLADE as a Library
5. Notes
6. Contact

Prerequisites
=====

GLADE has been tested on Ubuntu 64-bit 16.04.1 and on Mac OS X 10.9.5, but should work on typical Linux installations. GLADE requires Java 1.7 or above, and building GLADE requires Apache Ant. The build process has specifically been tested on OpenJDK version 1.8.0_91 and Apache Ant version 1.9.7. The example programs that come with GLADE should be self contained, except flex requires GNU M4, which on Ubuntu can be installed using

    $ sudo apt-get install m4

GLADE can be built without GNU M4, but running the flex example will fail (the remaining programs should run without issue).

Downloading and Building GLADE
=====

The GLADE source code is available on GitHub under the Apache Software License version 2.0 at https://github.com/obastani/glade. To check out the GLADE source code repository, run:

    $ git clone https://github.com/obastani/glade.git

To build GLADE, run:

    $ cd glade
    $ ant

Doing so should produce a jar file named `glade.jar`.

The GLADE code is currently set up to synthesize grammars for the following programs: GNU sed, GNU grep, flex, xml, and the standard Python interpreter. To set up the example programs that come with GLADE on Ubuntu, run:

    $ ./setup.sh

This command should work for typical Linux distributions. If setting up GLADE on Mac OS X, instead run

    $ ./setup_osx.sh

To test that the build script and the setup script ran as expected, run:

    $ java -jar glade.jar -mode test -verbose

Additionally, to run the tests for a specific program provided with GLADE, run:

    $ java -jar glade.jar -mode test -program <program> -verbose

The available values of `<program>` are `sed`, `grep`, `flex`, `xml`, `python`, and `python-wrapped`. The last option synthesizes a grammar specifically for the Python parser, by wrapping the input (say, `<input>`) in an `if False` block:

    $ if False:
    $     <input>

Logging information is printed to `log.txt`. Additional options are `-log <filename>`, which prints logging information to the file with the given filename, and `-verbose`, which prints logging information to `stdout`. For example, running:

    $ java -jar glade.jar -mode test -log out.txt

runs the tests for all provided programs, and prints logging information both to `out.txt` (but not to `stdout`).

To uninstall GLADE, run:

    $ ant clean
    
To uninstall the example programs that come with GLADE, run:

    $ ./cleanup.sh

Running GLADE
=====

To synthesize a grammar for an example program using GLADE, run:

    $ java -jar glade.jar -mode learn -program <program>

The `-log` and `-verbose` options work as for running tests. For example, running:

    $ java -jar glade.jar -mode learn -program sed -log out_learn.txt -verbose

synthesizes a grammar for GNU sed, and prints logging information to `out_learn.txt` and to `stdout`.

The seed inputs given to GLADE as examples for each of these programs are stored in `data/inputs-train/<program>/`. Learned grammars are stored (in serialized form, not human readable) in the folder `data/grammars/<program>/`. A grammar is generated for each seed input, as well as a grammar `all.gram` learned from all seed inputs.

Once a grammar has been synthesized for a program (in particular, a file `data/grammars/<program>/all.gram` has been generated), it can be used to randomly generate new inputs by running:

    $ java -jar glade.jar -mode fuzz -program <program> -fuzzer <fuzzer>

The two fuzzers `<fuzzer>` available are `grammar` (the grammar-based fuzzer), which only generates inputs in the synthesized grammar, and `combined` (the combined fuzzer), which generates a combination of inputs in the synthesized grammar and purposely invalid inputs. GLADE prints 10 sample inputs to the log as well as the overall pass rate (i.e., the fraction of the 10 sample inputs that are valid). For example, running:

    $ java -jar glade.jar -mode fuzz -program sed -fuzzer grammar -log out_fuzz.txt -verbose

runs the grammar-based fuzzer on the grammar learned for GNU sed, and prints logging information to `out_fuzz.txt` and `stdout`.

Using GLADE as a Library
=====

We have provided a simple Java program that uses GLADE to synthesize a grammar and then generate random samples. The program is located at `test/main/Test.java`. To compile this test program, run:

    $ javac -classpath test:glade.jar test/main/Test.java

To run the compiled test program, run:

    $ java -classpath test:glade.jar main.Test

To clean the test, run:

    $ rm test/main/Test.class

The test program uses GLADE to synthesize a grammar encoding a matching parentheses language, with three kinds of parentheses: `()`, `[]`, and `{}`. Then, it uses the synthesized grammar to generate 10 random samples. Finally, it computes and prints the pass rate, which is the fraction of samples that are contained in the true matching parentheses language. The pass rate should be 1.0.

There are two key inputs to GLADE's grammar synthesis algorithm:

**Query oracle:** The query oracle is an instance of a class implementing the interface `glade.util.OracleUtils.DiscriminativeOracle`, which specifies a single method

    $ boolean query(String query)

This method should return true if the string `query` is contained in the target language, and false otherwise. Our test program uses the following oracle:

    $ DiscriminativeOracle oracle = new TestOracle();

The implementation of `TestOracle` is in `test/main/Test.java` (lines 31-56).

**Examples:** The examples are a list of strings contained in the target language. 

    $ List<String> examples = Arrays.asList(new String[]{"{([][])([][])}{[()()][()()]}"});

Then, the following code calls the grammar synthesis algorithm:

    $ Grammar grammar = GrammarSynthesis.getGrammarMultiple(examples, oracle);

Now, we can use GLADE to generate samples from `grammar`. The following code creates an `Iterable` that returns random samples from `grammar`:

    $ Iterable<String> samples = new GrammarMutationSampler(grammar, sampleParams, maxLen, numMut, new Random());

The parameter `maxLen` (set to 1000 in the test program) bounds the maximum length of a sample. The parameter `numMut` (set to 20 in the test program) is the number of random mutations made to obtain a random sample. The parameter `sampleParams` are the probabilities of expanding different terms in the grammar; the defaults used in the test program should work well for most applications.

Notes
=====

- Flex, sed, grep, and xml inputs are primarily obtained from the respective distributions.
- Python inputs are obtained from https://wiki.python.org/moin/SimplePrograms.

Contact
=====

For questions, feel free to contact `obastani@cs.stanford.edu`.
