/*
 * Filename:     X3dDoctypeChecker.java
 * Created:      22 July 2004
 * Author:       Don Brutzman
 * Reference:    Mehran Habibi, Java Regular Expressions:  Taming the java.util.regex engine, Apress, Springer Verlag, NY, 2004.
 * Description:  Verify correct use of X3D final and transitional DTDs, convert .x3d file header if desired.
 *
 * Copyright (c) 1995-2022 held by the author(s).  All rights reserved.
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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author  brutzman
 */
public class X3dDoctypeChecker {
    /** diagnostic prefix */
    public static String warningToken= "[Warning]";
    /** diagnostic prefix */
    public static String errorToken  = "[Error]";

    /** diagnostic message */
    public static String foundHTMLMessage  = "found HTML";

    static String           UsageMessage   = "usage: java X3dDoctypeChecker sceneName.x3d [-verbose | -setFinalDTD | -setTransitionalDTD]";

    static boolean          setTransitionalDTD       = false;
    static boolean          setFinalDTD              = false;
    static boolean          foundHTML                = false;
    static boolean          foundNo_DTD              = false;
    static boolean          foundTransitional_30_DTD = false;
    static boolean          foundTransitional_31_DTD = false;
    static boolean          foundFinal_30_DTD        = false;
    static boolean          foundFinal_31_DTD        = false;
    static boolean          foundFinal_32_DTD        = false;
    static boolean          foundFinal_33_DTD        = false;
    static boolean          foundFinal_40_DTD        = false;
    static boolean          foundFinal_41_DTD        = false;
    static boolean          readOnlyFile             = false;
    static boolean          saveFile                 = true;
    static boolean          verbose                  = false;

    String           sceneText    = new String();
    String           headerText   = new String();
    String           revisedScene = new String();
    String           outputLog    = new String();
	
    /** sharable string constant */
    public static final String     XML_DECLARATION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    // removed final close bracket > so that files with embedded entity-reference declarations (such as examples/development/QuadTreeExamples) will work -->
    /** sharable string constant */
   public static final String     TRANSITIONAL_30_DOCTYPE = "<!DOCTYPE X3D PUBLIC \"https://www.web3d.org/specifications/x3d-3.0.dtd\" \"file:///www.web3d.org/TaskGroups/x3d/translation/x3d-3.0.dtd\"";
    /** sharable string constant */
   public static final String     TRANSITIONAL_31_DOCTYPE = "<!DOCTYPE X3D PUBLIC \"https://www.web3d.org/specifications/x3d-3.1.dtd\" \"file:///www.web3d.org/TaskGroups/x3d/translation/x3d-3.1.dtd\"";
    /** sharable string constant */
   public static final String     FINAL_30_DOCTYPE        = "<!DOCTYPE X3D PUBLIC \"ISO//Web3D//DTD X3D 3.0//EN\" \"https://www.web3d.org/specifications/x3d-3.0.dtd\"";
    /** sharable string constant */
   public static final String     FINAL_31_DOCTYPE        = "<!DOCTYPE X3D PUBLIC \"ISO//Web3D//DTD X3D 3.1//EN\" \"https://www.web3d.org/specifications/x3d-3.1.dtd\"";
    /** sharable string constant */
   public static final String     FINAL_32_DOCTYPE        = "<!DOCTYPE X3D PUBLIC \"ISO//Web3D//DTD X3D 3.2//EN\" \"https://www.web3d.org/specifications/x3d-3.2.dtd\"";
    /** sharable string constant */
   public static final String     FINAL_33_DOCTYPE        = "<!DOCTYPE X3D PUBLIC \"ISO//Web3D//DTD X3D 3.3//EN\" \"https://www.web3d.org/specifications/x3d-3.3.dtd\"";
    /** sharable string constant */
   public static final String     FINAL_40_DOCTYPE        = "<!DOCTYPE X3D PUBLIC \"ISO//Web3D//DTD X3D 4.0//EN\" \"https://www.web3d.org/specifications/x3d-4.0.dtd\"";
    /** sharable string constant */
   public static final String     FINAL_41_DOCTYPE        = "<!DOCTYPE X3D PUBLIC \"ISO//Web3D//DTD X3D 4.1//EN\" \"https://www.web3d.org/specifications/x3d-4.1.dtd\"";
    final String     WarningComment = "<!--Warning:  transitional DOCTYPE in source .x3d file-->\n";
    final String     WarningRegex   = "<!--Warning:  transitional DOCTYPE in source \\.x3d file-->(\\s)*";

    FileInputStream  fis;
    RandomAccessFile raf;
    FileChannel      fc;
    ByteBuffer       bb;

/**
 * extracts the content of a file
 * @param fileName the name of the file to extract
 * @return String representing contents of the file
 */
@SuppressWarnings("CallToThreadDumpStack")
public String getFileContent(String fileName)
{
    // Must open as read-only FileInputStream first to ensure existence
	// before opening as read-write RandomAccessFile
	try
	{
	    fis = new FileInputStream (fileName);
	}
	catch (FileNotFoundException fnf)
	{
	//  fnf.printStackTrace(System.out);
	    addLogEntryLine (errorToken+" [X3dDoctypeChecker] scene \"" + fileName + "\" not found.");
	    addLogEntryLine (UsageMessage);
	    saveFile = false;
	    return "";
	}
	try
	{
	    raf = new RandomAccessFile (fileName, "rwd");
	    fc  = raf.getChannel();
	    bb  = ByteBuffer.allocate((int)fc.size());
	    fc.read(bb);
	    bb.flip();
	}
	catch (IOException ioe)
	{
	    readOnlyFile = true;
	//  ioe.printStackTrace(System.out);
	}
	if (raf == null) try
	{
	    raf = new RandomAccessFile (fileName, "r");
	    fc  = raf.getChannel();
	    bb  = ByteBuffer.allocate((int)fc.size());
	    fc.read(bb);
	    bb.flip();
	    addLogEntryLine (warningToken+" [X3dDoctypeChecker] " + fileName + " file is read-only.");
	}
	catch (IOException ioe)
	{
	    addLogEntryLine (errorToken+" [X3dDoctypeChecker] unable to read scene \"" + fileName + "\".");
            System.err.println (outputLog);
	    ioe.printStackTrace(System.out);
	    return "";
	}
	String returnString = new String (bb.array());
	bb = null;
	return returnString;
}
/**
 * resets the content of a file
 * @param revisedScene content being reset
 */
@SuppressWarnings("CallToThreadDumpStack")
public void setFileContent(String revisedScene)
{
	try
	{
	    bb = ByteBuffer.wrap(revisedScene.getBytes());
	    fc.truncate(revisedScene.length());
	    fc.position(0);
	    fc.write(bb);
	}
	catch (IOException ioe)
	{
	    ioe.printStackTrace(System.out);
	}
}
/**
 * @param args the command line arguments
 */
public static void main(String[] args)
{
        String x3dFile;

	if ((args!= null) && (args.length >= 1) && (args.length <= 2))
	{
	    x3dFile = args[0];
	}
	else
	{
	    System.out.println (UsageMessage);
	    return;
	}
	if (args.length > 1)
	{
	    for (int i=1; i<=args.length-1; i++)
	    {
	        if      ((args[i].compareTo("-v")==0) || (args[i].compareTo("-verbose")==0))
	        {
	            verbose = true;
	        }
            else if      ((args[i].compareTo("-f")==0) || (args[i].compareTo("-setFinalDTD")==0))
	        {
	            setFinalDTD = true;
	        }
	        else if ((args[i].compareTo("-t")==0) || (args[i].compareTo("-setTransitionalDTD")==0))
	        {
	            setTransitionalDTD = true;
	        }
	        else
	        {
	            System.out.println(errorToken+" [X3dDoctypeChecker] unrecognized command-line option \"" + args[i] + "\"");
	            System.out.println(UsageMessage);
	            return;
	        }
	    }
	}
	if (setFinalDTD && setTransitionalDTD)
	{
	    System.out.println (errorToken+" [X3dDoctypeChecker] both -setFinalDTD and -setTransitionalDTD specified,");
	    System.out.println ("        only one operation allowed.");
	    System.out.println (UsageMessage);
	    return;
	}
    X3dDoctypeChecker doctypeChecker = new X3dDoctypeChecker();
    String log = doctypeChecker.processScene (x3dFile);
    System.out.println (log);
}
/**
 * processes the scene to check DOCTYPE
 * @param x3dFileName is path and file name of X3D content to be processed
 * @return outputLog providing processing results
 */

public String processScene (String x3dFileName)
{
    outputLog = "";
    sceneText  = getFileContent (x3dFileName);
    if (sceneText==null)
    {
        addLogEntryLine  ("[X3dDoctypeChecker] failure: file read unsuccessful for " + x3dFileName);
        return outputLog;
    }
    else if (sceneText.length()==0)
    {
        addLogEntryLine  ("[X3dDoctypeChecker] failure: empty file " + x3dFileName);
        return outputLog;
    }
    int indexHTML = 0;
	if      (sceneText.contains("<html"))
		     indexHTML = sceneText.indexOf("<html", 0);
	else if (sceneText.contains("<HTML"))
		     indexHTML = sceneText.indexOf("<HTML", 0);
	
    int indexX3D = sceneText.indexOf("<X3D", 0);
    if (indexX3D > 0)
         headerText = sceneText.substring(0, indexX3D).trim();
    else
    {
         addLogEntryLine  (errorToken+"[X3dDoctypeChecker] failure: no <X3D> element found");
         headerText = sceneText;
    }
	foundHTML = (indexHTML > 0);
	if ((indexHTML > 0) && (indexX3D > 0) && (indexHTML > indexX3D))
		foundHTML = false; // apparently <HTML string within X3D scene

	// had trouble with backreferencing group (\1 and \3), (\4 and \6) to ensure matching single/double-quote characters :(
	String    regexXmlHeader = "<\\?xml version=(\"|')1.(0|1)(\"|') encoding=(\"|')UTF-(8|16)(\"|')\\?>";
	Pattern patternXmlHeader = Pattern.compile(regexXmlHeader);
	Matcher matcherXmlHeader = patternXmlHeader.matcher(sceneText);

	String    regexXmlHeaderUtfIgnoreCase = "<\\?xml version=(\"|')1.(0|1)(\"|') encoding=(\"|')(U|u)(T|t)(F|f)-(8|16)(\"|')\\?>";
	Pattern patternXmlHeaderUtfIgnoreCase = Pattern.compile(regexXmlHeaderUtfIgnoreCase);
	Matcher matcherXmlHeaderUtfIgnoreCase = patternXmlHeaderUtfIgnoreCase.matcher(sceneText);

	if      (foundHTML && matcherXmlHeader.find())
	{
	    addLogEntry ("[X3dDoctypeChecker] " + foundHTMLMessage + ", ignoring original XML declaration.");
	}
    else if (foundHTML && !matcherXmlHeader.find())
	{
	    addLogEntry ("[X3dDoctypeChecker] " + foundHTMLMessage + ", ignoring absence of XML declaration.");
	}
    else if (matcherXmlHeader.find())
	{
	    addLogEntry ("[X3dDoctypeChecker] success: valid XML declaration found.");
	}
        else if (matcherXmlHeaderUtfIgnoreCase.find())
	{
	    addLogEntry ("[X3dDoctypeChecker] failure: invalid XML declaration found (note that encoding='UTF-8' must include hyphen and be upper case).");
	}
	else // no resetting of XML declaration performed
	{
	    addLogEntryLine  (errorToken+" [X3dDoctypeChecker] failure: no valid XML declaration found in scene!");
            addLogEntryLine  (headerText);
	    foundNo_DTD = true;
	    addLogEntryLine (UsageMessage);
	    if (!setFinalDTD && !setTransitionalDTD) // force update if requested
	    {
	    	return outputLog;
	    }
	}
	// <!-- <!DOCTYPE not is matched since embedded as XML comment
	// DOCTYPE line can finish with close bracket > or else open square bracket [ in files with embedded entity-reference declarations (such as examples/development/QuadTreeExamples.x3d) will work

	// <!DOCTYPE X3D PUBLIC "ISO//Web3D//DTD X3D 3.0//EN" "https://www.web3d.org/specifications/x3d-3.0.dtd">
	String  regexFinal30Doctype    = "[^<][^!][^-][^-](\\s)?<!DOCTYPE X3D PUBLIC(\\s)+\"ISO//Web3D//DTD X3D 3.0//EN\"(\\s)+\"http(s)?://www.web3d.org/specifications/x3d-3.0.dtd\"(\\s)*(>|\\[)";
	Pattern patternFinal30Doctype  = Pattern.compile(regexFinal30Doctype);
	Matcher matcherFinal30Doctype  = patternFinal30Doctype.matcher(sceneText);

	String  regexFinal31Doctype    = "[^<][^!][^-][^-](\\s)?<!DOCTYPE X3D PUBLIC(\\s)+\"ISO//Web3D//DTD X3D 3.1//EN\"(\\s)+\"http(s)?://www.web3d.org/specifications/x3d-3.1.dtd\"(\\s)*(>|\\[)";
	Pattern patternFinal31Doctype  = Pattern.compile(regexFinal31Doctype);
	Matcher matcherFinal31Doctype  = patternFinal31Doctype.matcher(sceneText);

	String  regexFinal32Doctype    = "[^<][^!][^-][^-](\\s)?<!DOCTYPE X3D PUBLIC(\\s)+\"ISO//Web3D//DTD X3D 3.2//EN\"(\\s)+\"http(s)?://www.web3d.org/specifications/x3d-3.2.dtd\"(\\s)*(>|\\[)";
	Pattern patternFinal32Doctype  = Pattern.compile(regexFinal32Doctype);
	Matcher matcherFinal32Doctype  = patternFinal32Doctype.matcher(sceneText);

	String  regexFinal33Doctype    = "[^<][^!][^-][^-](\\s)?<!DOCTYPE X3D PUBLIC(\\s)+\"ISO//Web3D//DTD X3D 3.3//EN\"(\\s)+\"http(s)?://www.web3d.org/specifications/x3d-3.3.dtd\"(\\s)*(>|\\[)";
	Pattern patternFinal33Doctype  = Pattern.compile(regexFinal33Doctype);
	Matcher matcherFinal33Doctype  = patternFinal33Doctype.matcher(sceneText);

	String  regexFinal40Doctype    = "[^<][^!][^-][^-](\\s)?<!DOCTYPE X3D PUBLIC(\\s)+\"ISO//Web3D//DTD X3D 4.0//EN\"(\\s)+\"http(s)?://www.web3d.org/specifications/x3d-4.0.dtd\"(\\s)*(>|\\[)";
	Pattern patternFinal40Doctype  = Pattern.compile(regexFinal40Doctype);
	Matcher matcherFinal40Doctype  = patternFinal40Doctype.matcher(sceneText);

	String  regexFinal41Doctype    = "[^<][^!][^-][^-](\\s)?<!DOCTYPE X3D PUBLIC(\\s)+\"ISO//Web3D//DTD X3D 4.1//EN\"(\\s)+\"https://www.web3d.org/specifications/x3d-4.1.dtd\"(\\s)*(>|\\[)";
	Pattern patternFinal41Doctype  = Pattern.compile(regexFinal41Doctype);
	Matcher matcherFinal41Doctype  = patternFinal41Doctype.matcher(sceneText);

	// <!DOCTYPE X3D PUBLIC "https://www.web3d.org/specifications/x3d-3.0.dtd" "file:///www.web3d.org/TaskGroups/x3d/translation/x3d-3.0.dtd">
	String  regexTransitional30Doctype   = "[^<][^!][^-][^-](\\s)?<!DOCTYPE X3D PUBLIC(\\s)+\"https://www.web3d.org/specifications/x3d-3.0.dtd\"(\\s)+\"file:///www.web3d.org/TaskGroups/x3d/translation/x3d-3.0.dtd\"(\\s)*(>|\\[)";
	Pattern patternTransitional30Doctype = Pattern.compile(regexTransitional30Doctype);
	Matcher matcherTransitional30Doctype = patternTransitional30Doctype.matcher(sceneText);

	String  regexTransitional31Doctype   = "[^<][^!][^-][^-](\\s)?<!DOCTYPE X3D PUBLIC(\\s)+\"https://www.web3d.org/specifications/x3d-3.1.dtd\"(\\s)+\"file:///www.web3d.org/TaskGroups/x3d/translation/x3d-3.1.dtd\"(\\s)*(>|\\[)";
	Pattern patternTransitional31Doctype = Pattern.compile(regexTransitional31Doctype);
	Matcher matcherTransitional31Doctype = patternTransitional31Doctype.matcher(sceneText);

	String  regexAnyDoctype   = "[^<][^!][^-][^-](\\s)?<!DOCTYPE X3D PUBLIC";
	Pattern patternAnyDoctype = Pattern.compile(regexAnyDoctype);
	Matcher matcherAnyDoctype = patternAnyDoctype.matcher(sceneText);

	if      (foundHTML && sceneText.contains("<!DOCTYPE"))
	{
	    addLogEntry ("[X3dDoctypeChecker] " + foundHTMLMessage + ", ignoring original DOCTYPE, using X3D DOCTYPE v3.3;");
	}
    else if (foundHTML)
	{
	    addLogEntry ("[X3dDoctypeChecker] " + foundHTMLMessage + ", ignoring absence of DOCTYPE, using X3D DOCTYPE v3.3;");
	}
    else if      (matcherFinal41Doctype.find())
	{
	    foundFinal_41_DTD = true;
	    addLogEntryLine  ("[X3dDoctypeChecker] success: final X3D 4.1 DOCTYPE found.");
	    if (verbose) addLogEntryLine  (headerText);
	}
    else if      (matcherFinal40Doctype.find())
	{
	    foundFinal_40_DTD = true;
	    addLogEntryLine  ("[X3dDoctypeChecker] success: final X3D 4.0 DOCTYPE found.");
	    if (verbose) addLogEntryLine  (headerText);
	}
	else if      (matcherFinal33Doctype.find())
	{
	    foundFinal_33_DTD = true;
	    addLogEntryLine  ("[X3dDoctypeChecker] success: final X3D 3.3 DOCTYPE found.");
	    if (verbose) addLogEntryLine  (headerText);
	}
	else if      (matcherFinal32Doctype.find())
	{
	    foundFinal_32_DTD = true;
	    addLogEntryLine  ("[X3dDoctypeChecker] success: final X3D 3.2 DOCTYPE found.");
	    if (verbose) addLogEntryLine  (headerText);
	}
	else if      (matcherFinal31Doctype.find())
	{
	    foundFinal_31_DTD = true;
	    addLogEntryLine  ("[X3dDoctypeChecker] success: final X3D 3.1 DOCTYPE found.");
	    if (verbose) addLogEntryLine  (headerText);
	}
	else if      (matcherFinal30Doctype.find())
	{
	    foundFinal_30_DTD = true;
	    addLogEntryLine  ("[X3dDoctypeChecker] success: final X3D 3.0 DOCTYPE found.");
	    if (verbose) addLogEntryLine  (headerText);
	}

	else if (matcherTransitional30Doctype.find())
	{
	    foundTransitional_30_DTD = true;
	    addLogEntryLine  ("[X3dDoctypeChecker] warning: transitional X3D 3.0 DOCTYPE found.");
	    if (verbose) addLogEntryLine  (headerText);
	}
	else if (matcherTransitional31Doctype.find())
	{
	    foundTransitional_31_DTD = true;
	    addLogEntryLine  ("[X3dDoctypeChecker] warning: transitional X3D 3.1 DOCTYPE found.");
	    if (verbose) addLogEntryLine  (headerText);
	}
	else if (matcherAnyDoctype.find())
	{
	    addLogEntryLine  ("\n[X3dDoctypeChecker] "+errorToken+" failure: nonstandard X3D DOCTYPE found!");
        addLogEntryLine  (headerText);
	    return outputLog;
	}
	else
	{
	    addLogEntryLine  ("\n[X3dDoctypeChecker] "+errorToken+" failure: no X3D DOCTYPE found!");
        addLogEntryLine  (headerText);
	    foundNo_DTD = true;
	    if (!setFinalDTD && !setTransitionalDTD) // force update if requested
	    {
	    	return outputLog;
	    }
	}
	// check for multiple DOCTYPEs present
	matcherAnyDoctype.reset();
	int matchCount = 0;
	while (matcherAnyDoctype.find())
	{
	    matchCount++;
	}
	if (matchCount > 1)
	{
	    addLogEntryLine (warningToken+" Multiple X3D DOCTYPEs found (" + matchCount + " total).");
	    if ((setFinalDTD || setTransitionalDTD) && (readOnlyFile == false) && !foundHTML)
	    {
	         addLogEntryLine  ("[X3dDoctypeChecker] No DTD conversion attempted.");
	    }
        addLogEntryLine  (headerText);
	    return outputLog;
	}
	if (readOnlyFile || foundHTML)
	{
	    return outputLog; // application complete
	}

	if      (setFinalDTD)         System.out.print ("[X3dDoctypeChecker] set final X3D DTD:  ");
	else if (setTransitionalDTD)  System.out.print ("[X3dDoctypeChecker] set transitional X3D DTD:  ");

	if      (setFinalDTD && foundTransitional_30_DTD)
	{
	    matcherTransitional30Doctype.reset();
	    revisedScene = matcherTransitional30Doctype.replaceFirst (FINAL_30_DOCTYPE);
	    revisedScene = revisedScene.replaceAll (WarningRegex,"");  // also remove warning comment
	    addLogEntryLine ("[X3dDoctypeChecker] scene reset to final X3D 3.0 DTD.");
	    addLogEntryLine (FINAL_30_DOCTYPE+">");
	    saveFile = true;
	}
	else if (setFinalDTD && foundTransitional_31_DTD)
	{
	    matcherTransitional31Doctype.reset();
	    revisedScene = matcherTransitional31Doctype.replaceFirst (FINAL_31_DOCTYPE);
	    revisedScene = revisedScene.replaceAll (WarningRegex,"");  // also remove warning comment
	    addLogEntryLine ("[X3dDoctypeChecker] scene reset to final X3D 3.1 DTD.");
	    addLogEntryLine (FINAL_31_DOCTYPE+">");
	    saveFile = true;
	}
	else if (setTransitionalDTD && foundFinal_30_DTD)
	{
	    matcherFinal30Doctype.reset();
	    revisedScene = matcherFinal30Doctype.replaceFirst (WarningComment + TRANSITIONAL_30_DOCTYPE);
	    addLogEntryLine ("[X3dDoctypeChecker] scene reset to transitional X3D DTD.");
	    addLogEntryLine (TRANSITIONAL_30_DOCTYPE+">");
	    saveFile = true;
	}
	else if (setTransitionalDTD && foundFinal_31_DTD)
	{
	    matcherFinal31Doctype.reset();
	    revisedScene = matcherFinal31Doctype.replaceFirst (WarningComment + TRANSITIONAL_31_DOCTYPE);
	    addLogEntryLine ("[X3dDoctypeChecker] scene reset to transitional X3D DTD.");
	    addLogEntryLine (TRANSITIONAL_31_DOCTYPE+">");
	    saveFile = true;
	}
	else if (foundNo_DTD)
	{
            addLogEntryLine  ("no action taken, functionality not implemented...");
	    saveFile = false;
	}
	else if (setFinalDTD || setTransitionalDTD)
	{
            addLogEntryLine  ("no action necessary.");
	    saveFile = false;
	}
	saveFileIfSet (); // application complete
    return outputLog.trim();
}

/** save file if appropriate */
@SuppressWarnings("CallToThreadDumpStack")
public void saveFileIfSet ()
{
    if (saveFile == false) return;
    if (setFinalDTD || setTransitionalDTD) setFileContent (revisedScene);
    try
    {
        if (fc != null)
        {
            raf.close();
        }
    }
    catch (IOException ioe)
    {
        ioe.printStackTrace(System.out);
    }
}
protected void addLogEntry (String newString)
{
    outputLog += newString;
}
protected void addLogEntryLine (String newString)
{
    outputLog += newString + "\n";
}
}
