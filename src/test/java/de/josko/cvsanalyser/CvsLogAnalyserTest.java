package de.josko.cvsanalyser;

import static org.junit.Assert.*;

import org.apache.commons.io.IOUtils;
import org.junit.Test;


public class CvsLogAnalyserTest {



	@Test
	public void getLogFile() throws Exception {
		String xml = IOUtils.toString(
			      this.getClass().getResourceAsStream("svn.log"),
			      "UTF-8"
			    );
	}
}
