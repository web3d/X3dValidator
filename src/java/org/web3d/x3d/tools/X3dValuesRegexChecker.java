/*
 * Filename:     X3dValuesRegexChecker.java
 * 
 * Created:      30 March 2012
 *
 * Author(s):    Don Brutzman and Terry Norbraten
 *               http://www.nps.edu and http://www.movesinstitute.org
 *
 * Description:  Verify correct X3D final or transitional DTDs, convert one to
 *               the other if desired, verify well-formed and valid X3D.
 *
 * Reference:    Mehran Habibi, Java Regular Expressions:  Taming the
 *               java.util.regex engine, Apress, Springer Verlag, NY, 2004.
 *
 * Reference:    https://www.web3d.org/x3d/content/tools/canonical/src/org/web3d/x3d/tools/X3dHeaderChecker.java
 *
 * URL:          https://www.web3d.org/x3d/content/tools/canonical/src/org/web3d/x3d/tools/X3dValuesRegexChecker.java
 *
 * TODO:         listed throughout code body
 *
 * Copyright (c) 1995-2020 held by the author(s).  All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer
 *       in the documentation and/or other materials provided with the
 *       distribution.
 *     * Neither the names of the Naval Postgraduate School (NPS)
 *       Modeling Virtual Environments and Simulation (MOVES) Institute
 *       (http://www.nps.edu and http://www.MovesInstitute.org)
 *       nor the names of its contributors may be used to endorse or
 *       promote products derived from this software without specific
 *       prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.web3d.x3d.tools;

// Standard library imports
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Check for anomalous integer/float values in X3D scenes, including missing whitespace or leading zeroes
 * <p>[Usage] <code> java -cp dist/lib/X3dC14n.jar org.web3d.x3d.tools.X3dValuesRegexChecker [-f | -t] [optional -v] &lt;path&gt;/SceneName.x3d</code></p>
 *
 * @author <a href="mailto:brutzman@nps.edu">Don Brutzman</a>
 * @version $Id: X3dValuesRegexChecker.java 10140 2012-01-04 01:42:00Z brutzman $
 * <p>
 *   <b>Latest Modifications:</b>
 * </p>
 *   <pre><b>
 *     Date:     29 March 2012
 *     Time:     1302
 *     Author:   <a href="mailto:brutzman@nps.edu?subject=X3dValuesRegexChecker">Don Brutzman, NPS MOVES</a>
 *     Comments: 1) Copied from X3dHeaderChecker.
 *   </b></pre>
 */
public class X3dValuesRegexChecker implements X3dToolsConstants {
    
    /** log4j logger instance */
    static Logger log = LogManager.getLogger(X3dValuesRegexChecker.class);

    private ByteBuffer       bb;
    private FileInputStream  fis;
    private FileChannel      fc;
    private RandomAccessFile raf;
    private String           x3dFileName;
    private String           scene          = null;
    
    /**
     * Creates a new instance of X3dValuesRegexChecker
     * @param args command line arguments
     */
    public X3dValuesRegexChecker(String[] args)
    {
        if (args.length == 0) {
            log.warn("X3dValuesRegexChecker invocation error:  filename not specified");
            exit(new NullPointerException("arguments are null, no scene file specified"));
        }
        else if (args.length > 1) {
            log.warn("X3dValuesRegexChecker invocation should only specify filename");
            exit(new NullPointerException("X3dValuesRegexChecker invocation should only specify filename"));
        }
        setSceneContent(args);
        computeLineCounts();
    
        // application complete
//        System.exit(0); // NOT exit(null);
    }
    
    /**
     * Creates a new instance of X3dValuesRegexChecker
     * @param path path to file
     */
    public X3dValuesRegexChecker(String path)
    {
        if (path.length() == 0) {
            log.warn("X3dValuesRegexChecker invocation error:  filename not specified");
            exit(new NullPointerException("arguments are null, no scene file specified"));
        }
        x3dFileName = path;
        setScene(retrieveFileContent(x3dFileName)); 
        computeLineCounts();
    
        // application complete
//        System.exit(0); // NOT exit(null);
    }
   
    /* GETTER(s)/SETTER(s) */
    
    /* PROTECTED METHOD(s) */
    
    /** perform diagnostics
     * @return diagnostic results */
    public String processScene ()
    {
        return detectMalformedFloats() + detectLeadingZeroes();
    }
    
    protected String getScene() {return scene;}
    private void   setScene(String s) {scene = s;}
    
    /* PRIVATE METHOD(s) */
    
    /**
     * Checks for argument compliance and sets the scene as a String
     *
     * @param args a String[] containing command line arguments
     */
    private void setSceneContent(String[] args) {
        
        // Check for proper arguments
        for (String arg : args) {
              if (arg.contains(".x3d")) {x3dFileName = arg;} 
              else {
                log.warn("X3dValuesRegexChecker invocation error:  filename not specified");
                exit(new IllegalArgumentException("arguments not properly set, fileName.x3d not found"));
              }
	}
        setScene(retrieveFileContent(x3dFileName));        
    }
    
    /**
     * Loads the content of a file into a String which returns for parsing
     * @param fileName the name of the file to load
     * @return String representing contents of the scene file
     */
    private String retrieveFileContent(String fileName) {
        
        /* Must open as read-only FileInputStream first to ensure existence
         * before opening as read-write RandomAccessFile
         */
        try 
        {
            fis = new FileInputStream(fileName);
            
            /* This can not be set to just null in hopes that the JVM's gc()
             * will return resources quickly enough, must close
             */
            fis.close();
        } 
        catch (FileNotFoundException fnf) 
        {
            exit(new RuntimeException("scene \"" + fileName + "\" not found", fnf));
        } 
        catch (IOException ioe) 
        {
            log.fatal(ioe);
        }
        try 
        {
            raf = new RandomAccessFile(fileName, "rwd");
	    fc  = raf.getChannel();
            bb  = ByteBuffer.allocate((int) fc.size());
	    fc.read(bb);
        } 
        catch (IOException ioe) 
        { }
        
        // Can only verify valid DTD, no modifications
        if (raf == null) {
            try {
                raf = new RandomAccessFile(fileName, "r");
                log.warn("Scene is read-only");
	    fc  = raf.getChannel();
                bb  = ByteBuffer.allocate((int) fc.size());
	    fc.read(bb);
            } 
            catch (IOException ioe) 
            {
                exit(new RuntimeException("unable to read scene \"" + fileName + "\"", ioe));
            }
	}
        
        bb.flip();
        String returnString = new String(bb.array());
	bb = null;
	return returnString;
    }
    
    private final int   MAX_NUMBER_LINES = 100000;
    protected     int[] totalCharCountByLine;
    
    private void computeLineCounts()
    {
        totalCharCountByLine = new int[MAX_NUMBER_LINES]; // defer creation until needed at tun time
        int lineCount = 0;
        String line;
        BufferedReader bufferedReader;
        
	try 
	{
            bufferedReader = new BufferedReader(new FileReader (x3dFileName));
            
            totalCharCountByLine[lineCount] = 0; // there is no line 0
            while ((line = bufferedReader.readLine()) != null)
            {
                lineCount++;
                totalCharCountByLine[lineCount] = totalCharCountByLine[lineCount-1] + line.length() + 1; // TODO what about crlf?
            }
            bufferedReader.close();
        }
        catch (IOException e)
        {
            System.err.println ("Problem reading " + x3dFileName + " for computing line counts");
            System.err.println (e);
        }
    }
    
    private String reportLineCharacterCounts(int fileCharCount)
    {
        for (int i = 1; i <= MAX_NUMBER_LINES; i++) // there is no line 0
        {
            if (fileCharCount < totalCharCountByLine[i])
            {
               // character count falls inside this line
               return " in line " + i + " column " + (fileCharCount - totalCharCountByLine[i-1]) + ": ";
            }
        }
        return " (not found in first " + MAX_NUMBER_LINES + " lines )";
    }
    
    private String detectMalformedFloats() 
    {
        StringBuilder sb = new StringBuilder();
        Pattern patternMalformedFloats = Pattern.compile(REGEX_GARBLED_FLOATS);
        Matcher matcherMalformedFloats = patternMalformedFloats.matcher(getScene());

        int numberMatchesFound = 0;
        while (matcherMalformedFloats.find()) 
        {
            numberMatchesFound++;
            
            sb.append("-")
               .append(reportLineCharacterCounts(matcherMalformedFloats.start()))
            // .append("<code><b>").append(matcherMalformedFloats.group()).append("</b></code>") // TODO
               .append(matcherMalformedFloats.group())
            // .append(" (file characters ").append(matcherMalformedFloats.start()).append("..").append(matcherMalformedFloats.end()).append(")")
               .append("\n");
        }
        if (numberMatchesFound > 0)
            return "Found " + numberMatchesFound + " malformed float groups:\n" + sb.toString();
        else // 0
            return ""; // valid, no response needed
    }

    private String detectLeadingZeroes()
    {
        StringBuilder sb = new StringBuilder();
        Pattern patternLeadingZeroes = Pattern.compile(REGEX_LEADING_ZEROES);
        Matcher matcherLeadingZeroes = patternLeadingZeroes.matcher(getScene());
        
        int numberMatchesFound = 0;
        while (matcherLeadingZeroes.find())
        {
            numberMatchesFound++;
            
            sb.append("-")
                .append(reportLineCharacterCounts (matcherLeadingZeroes.start()))
            //  .append("<code><b>").append(matcherLeadingZeroes.group()).append("</b></code>") // TODO
                .append(matcherLeadingZeroes.group())
            //  .append(" (file characters ").append(matcherLeadingZeroes.start()).append("..").append(matcherLeadingZeroes.end()).append(")")
                .append("\n");
        }
        if      (numberMatchesFound == 1)
            return "Found " + numberMatchesFound + " leading-zero match:\n"   + sb.toString();
        else if (numberMatchesFound > 1)
            return "Found " + numberMatchesFound + " leading-zero matches:\n" + sb.toString();
        else // 0
            return ""; // valid, no response needed
    }
    
    /**
     * Cleans up resources upon a normal process.  Executes a RumtimeException
     * should one be encountered during an abnormal process.  This will be
     * highlighted in the Ant build output to identify the problem.
     * </p>
     * @param re the message prepared RuntimeException to throw if not null
     */
    private void exit(RuntimeException re) {
        System.out.flush();
        System.err.flush();
        if (re != null) 
            log.error(re);
        try
        {
            if (raf != null) 
            {
                raf.close();
            }
        } catch (IOException ioe) {
            log.error(ioe);
//            System.exit(EXIT_ABNORMAL);
        } finally {
            x3dFileName = null;
            bb = null;
            raf = null;
	}
    }
	
    /* MAIN METHOD */
	
    /**
     * Command line entry point for the program
     * @param args the command line arguments (if any)
     */
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public static void main(String[] args) {
        new X3dValuesRegexChecker(args);
    }
}
