package ui;

import java.io.IOException;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;


class Args4J {
	@Option(name = "-tieba", usage = "The name of tieba.")
	public String tieba = null;
	
	@Option(name = "-n", usage = "How many users to be crawled. "
			+ "This number should not be greater than 2000.")
	public int n = 1000;
}

public class Main {
	public static void main(String[] args) throws CmdLineException, IOException {
		Args4J args4j = new Args4J();
		CmdLineParser parser = new CmdLineParser(args4j);
		parser.parseArgument(args);
		if ( args.length == 0 ) {
			parser.printUsage(System.out);
			return;
		}
		
		if ( args4j.tieba != null ) {
			if ( args4j.n > 2000 ) {
				System.err.println("n should not be greater than 2000.");
				return;
			}
			// TODO
			return;
		}
	}
}
