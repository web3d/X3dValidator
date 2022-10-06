/*
 * Filename:     Validator.java
 * Created:      2 APR 2012
 * Author:       Mike Bailey, Don Brutzman
 * Description:  Comprehensive check of X3D file
 * 
 * Copyright (c) 1995-2021 held by the author(s).  All rights reserved.
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
package edu.nps.moves.x3d;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.serialize.MessageWarner;
import org.apache.xerces.jaxp.JAXPConstants;
import org.web3d.x3d.tools.X3dDoctypeChecker;
import org.web3d.x3d.tools.X3dValuesRegexChecker;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Mike Bailey, jmbailey@nps.edu
 * @author Don Brutzman, brutzman@nps.edu
 *
 * @version	$Id$
 * @since $Date$ @copyright	Copyright (C) 2011
 */
public class Validator {

    private static final String GREEN_PASS = "<font color='green'><b>pass</b></font>.\n";

    private static StringBuilder outputLogStringBuilder;

    // TODO debug
    private static final boolean prettyPrintX3dTidyOutputFile = false; // alternatively just use original validationFile

    private static File x3dTidyOutputFile;

    /**
     * Append results to console log
     *
     * @param passName previously given name of this pass
     * @param error whether or not an error occurred
     */
    private static void appendResults(String passName, boolean error) {
        if (error) {
            outputLogStringBuilder.append("<font color='red'>Error(s) detected during this validation test.\n");
            outputLogStringBuilder.append(passName);
            outputLogStringBuilder.append(": <b>fail</b></font>.\n");
        } else {
            outputLogStringBuilder.append(passName);
            outputLogStringBuilder.append(": ");
            outputLogStringBuilder.append(GREEN_PASS);
        }
    }

    private static void appendStart(String passName, String referenceUrl) {
        appendStart(passName, referenceUrl, "", "");
    }
    private static int stepCount = 1;

    /**
     * Append utility method
     *
     * @param passName include " check" in name for regular output
     * @param referenceUrl url to reference tool performing task
     * @param documentationName name of documentation
     * @param documentationUrl url for documentation
     */
    private static void appendStart(String passName, String referenceUrl, String documentationName, String documentationUrl)
    {
        outputLogStringBuilder.append("\n<b>").append(stepCount).append(". Performing ");
        stepCount++;
        if (referenceUrl.isEmpty()) {
            outputLogStringBuilder.append(passName);
        } else {
            outputLogStringBuilder.append("<a href='").append(referenceUrl.trim()).append("' target='X3dValidatorReference' title='test description or source'>");
            if (passName.contains(" check")) {
                outputLogStringBuilder.append(passName.substring(0, passName.indexOf(" check"))); // first part
                outputLogStringBuilder.append("</a>");
                outputLogStringBuilder.append(passName.substring(passName.indexOf(" check")));   // last part
            } else {
                outputLogStringBuilder.append("</a>");
            }
            if (!documentationName.isEmpty()) {
                outputLogStringBuilder.append(" (<a href='").append(documentationUrl.trim()).append("' target='X3dValidatorReference' title='test reference'>");
                outputLogStringBuilder.append(documentationName); // first part
                outputLogStringBuilder.append("</a>) ");
            }
        }
        outputLogStringBuilder.append("...</b>\n");
    }
    /**
     * Append utility method
     *
     * @param statement Preface prose
     * @param documentationName name of documentation
     * @param documentationUrl url for documentation
     */
    private static void appendStatement(String statement, String documentationName, String documentationUrl)
    {
        if (!statement.isEmpty()) 
        {
            outputLogStringBuilder.append(statement);
        }
        if (!documentationName.isEmpty() && !documentationUrl.isEmpty()) 
        {
                outputLogStringBuilder.append("<a href='").append(documentationUrl.trim()).append("' target='_blank'>");
                outputLogStringBuilder.append(documentationName.trim()); // first part
                outputLogStringBuilder.append("</a>");
        }
        else if (!documentationName.isEmpty()) 
        {
            outputLogStringBuilder.append(documentationName);
        }
        else if (!documentationUrl.isEmpty()) 
        {
                outputLogStringBuilder.append("<a href='").append(documentationUrl.trim()).append("' target='_blank'>");
                outputLogStringBuilder.append(documentationUrl.trim()); // first part
                outputLogStringBuilder.append("</a>");
        }
        outputLogStringBuilder.append("\n");
    }

    @SuppressWarnings("CallToPrintStackTrace")
    private static void catchResponse(Throwable t, String passName) {
        t.printStackTrace();
        outputLogStringBuilder.append("<font color='red'>Internal error caught:\n");
        outputLogStringBuilder.append(t.getClass().getName());
        outputLogStringBuilder.append(": ");
        outputLogStringBuilder.append(t.getLocalizedMessage());
        // https://stackoverflow.com/questions/1149703/how-can-i-convert-a-stack-trace-to-a-string
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            t.printStackTrace(pw);
            outputLogStringBuilder.append(sw.toString());
        } catch (Exception e) {
            System.out.println(outputLogStringBuilder.toString());
            System.out.println();
            System.out.println("*** Validator.catchResponse() secondary exception while handling prior exception...");
            e.printStackTrace(System.out);
        }
        outputLogStringBuilder.append("</font>\n");
        appendResults(passName, true);
        
        System.out.println(outputLogStringBuilder.toString());
        System.out.println();
        System.out.println("*** Validator.catchResponse() handling exception...");
    }

    @SuppressWarnings("CallToPrintStackTrace")
    public static String validate(File validationFile)
    {
        SAXParserFactory saxParserFactory;
        SAXParser saxParser;
        ThisSAXHandler thisSAXHandler;
        StreamSource xmlStreamSource;
        StreamSource xslStreamSource;
        StreamResult streamResult;
        net.sf.saxon.Configuration saxonConfiguration;
        TransformerFactory saxonTransformerFactory;
        Transformer saxonTransformer;
        ThisTransformListener thisTransformListener;
        boolean foundHTML   = false;
        boolean foundX3DOM  = false;
        boolean foundX_ITE  = false;
        boolean foundCobweb = false;
        String htmlFileText = new String();
        String x3dFileText  = new String();
        String htmlFileTextLower = new String();

        outputLogStringBuilder = new StringBuilder(); // clear prior results
        String passName = new String();
        stepCount = 1; // reset index

        // reference urls also maintained at   
        String referenceQualityAssurance    = "http://www.web3d.org/x3d/content/examples/X3dResources.html#QualityAssurance";
        String referenceX3dTidy             = "http://www.web3d.org/x3d/stylesheets/X3dTidy.html";
        String referenceUrlX3dTidy          = "http://www.web3d.org/x3d/stylesheets/X3dTidy.xslt";
        String referenceUrlDoctype          = "http://www.web3d.org/x3d/content/examples/X3dSceneAuthoringHints.html#Validation";
        String referenceUrlWellFormed       = "http://en.wikipedia.org/wiki/XML#Well-formedness_and_error-handling";
        String referenceUrlDtdSchema        = "http://www.web3d.org/specifications";
        String referenceUrlDtdSchematron    = "http://www.web3d.org/x3d/tools/schematron/X3dSchematron.html";
        String referenceUrlX3dToClassicVrml = "http://www.web3d.org/x3d/stylesheets/X3dToVrml97.xslt";
        String referenceUrlRegex            = "http://www.web3d.org/pipermail/x3d-public_web3d.org/2012-March/001950.html";
        String referenceUrlPrettyPrintHtml  = "http://www.web3d.org/x3d/stylesheets/X3dToXhtml.xslt";
                    
        String referenceX3DOM       = "http://www.x3dom.org";
        String referenceX_ITE_site  = "https://github.com/create3000/x_ite/wiki";
        String referenceX_ITE_code  = "https://create3000.github.io/code/x_ite/latest/dist/";
        String referenceCobweb      = "http://create3000.de/x_ite"; // legacy; not https

        // TODO version awareness
        String referenceUrlX3dSchemaDocumentation = "http://www.web3d.org/specifications/X3dSchemaDocumentation3.3/x3d-3.3.html";
        String referenceUrlX3dDtdDocumentation    = "http://www.web3d.org/specifications/X3dDoctypeDocumentation3.3.html";

        final String xsdBaseInClassPath = "/stylesheets/";

        outputLogStringBuilder.append("\n--------- Commence validation checks for <b>");
        outputLogStringBuilder.append(validationFile.getName());
        outputLogStringBuilder.append("</b> ---------\n");

        // =========================================================================
        System.out.println  ("File retrieval..."); // keep track on console in case unexpected exception/error messages appear
        // create validationFileExcerpt to allow processing of HTML files containing X3D content
        String validationFileX3dName;
        File validationFileX3D = validationFile; // must initialize
        
        try {

            byte[] fileByteArray = Files.readAllBytes(Paths.get(validationFile.getAbsolutePath()));
            htmlFileText      = new String(fileByteArray);
            htmlFileTextLower = htmlFileText.toLowerCase();
            foundHTML   = htmlFileText.contains("<html") || htmlFileText.contains("<HTML");
            foundX3DOM  = htmlFileText.contains("x3dom.");
            foundX_ITE  = htmlFileText.contains("x_ite.") || htmlFileText.toLowerCase().contains("<x3dcanvas ");
            foundCobweb = htmlFileText.contains("cobweb.");
            if (foundHTML || foundX3DOM || foundX_ITE || foundCobweb)
               outputLogStringBuilder.append("foundHTML=").append(foundHTML).append(", foundX3DOM=").append(foundX3DOM).append(", foundX_ITE=").append(foundX_ITE).append(", foundCobweb=").append(foundCobweb).append("\n");
            x3dFileText = new String(fileByteArray); // not yet excerpted
            
            if (foundX_ITE && x3dFileText.contains("<X3DCanvas ")) // show X3DCanvas if present
            {
                int startIndex = x3dFileText.indexOf("<X3DCanvas");
                int deltaIndex = x3dFileText.substring(startIndex).indexOf(">");
                outputLogStringBuilder.append("\n").append("Referenced model ").append(
                        x3dFileText.substring(startIndex,startIndex+deltaIndex+1).replaceAll("<","&lt;").replaceAll(">","&gt;"))
                        .append(" can be checked separately").append("\n").append("\n");
            }
            
            if (x3dFileText.contains("http://www.web3d.org/specifications/x3d-"))
            {
                // avoid problem with Sax unable to follow http->https redirect by web3d.org
                int startPositionDTD = x3dFileText.indexOf("http://www.web3d.org/specifications/x3d-");
                String x3dDtdUrlExtract = x3dFileText.substring(startPositionDTD, x3dFileText.indexOf(".dtd") + 4);
                if   (!x3dDtdUrlExtract.isEmpty())
                    outputLogStringBuilder.append("substituting <i>https</i> to avoid redirection when checking XML DOCTYPE at ")
                             .append(x3dDtdUrlExtract).append("\n");
                int startPositionSchema = x3dFileText.indexOf("xsd:noNamespaceSchemaLocation=") + "xsd:noNamespaceSchemaLocation=".length() + 1;
                String x3dSchemaUrlExtract = x3dFileText.substring(startPositionSchema, x3dFileText.indexOf(".xsd") + 4);
                if   (!x3dSchemaUrlExtract.isEmpty())
                    outputLogStringBuilder.append("substituting <i>https</i> to avoid redirection when checking XML Schema url at ")
                             .append(x3dSchemaUrlExtract).append("\n");
                x3dFileText = x3dFileText.replaceAll("http://www.web3d.org/specifications/x3d-","https://www.web3d.org/specifications/x3d-");
            }
            if (foundHTML) {
                // https://stackoverflow.com/questions/876816/open-temp-file-in-java
                validationFileX3dName = validationFile.getName().substring(0, validationFile.getName().lastIndexOf("."));
                validationFileX3dName += "Excerpt";
                validationFileX3D = File.createTempFile(validationFileX3dName, ".x3d"); // TODO permissions on server
                validationFileX3D.deleteOnExit();
            }
            // remove first <X3D> block
            String x3dFileTextLowerCase = x3dFileText.toLowerCase();
            if (foundHTML && (x3dFileTextLowerCase.contains("<x3d "))) // avoid <X3DCanvas 
            {
                if      ((x3dFileTextLowerCase.contains("</x3d")))
                          x3dFileText = x3dFileText.substring(x3dFileTextLowerCase.indexOf("<x3d "), x3dFileTextLowerCase.indexOf("</x3d>") + 6);
                else
                {
                          x3dFileText = x3dFileText.substring(x3dFileTextLowerCase.indexOf("<x3d "));
                          outputLogStringBuilder.append("No closing element </X3D> found").append("\n");
                }
            } 
            if (foundHTML) 
            {
                // prepend XML header and DOCTYPE to extracted X3D scene for testing
                if (x3dFileText.contains("version='3.") || x3dFileText.contains("version=\"3."))
                {
                    x3dFileText = X3dDoctypeCheckerModified.XML_DECLARATION + "\n"
                                + X3dDoctypeCheckerModified.FINAL_33_DOCTYPE + ">\n" + x3dFileText;
                }
                else
                {
                    x3dFileText = X3dDoctypeCheckerModified.XML_DECLARATION + "\n"
                                + X3dDoctypeCheckerModified.FINAL_40_DOCTYPE + ">\n" + x3dFileText;
                }
            }
            // create corresponding file (containing only X3D) for subsequent validation
            // https://stackoverflow.com/questions/1053467/how-do-i-save-a-string-to-a-text-file-using-java
            try (PrintWriter printWriterX3dExcerptFile = new PrintWriter(validationFileX3D))
            {
                 printWriterX3dExcerptFile.println(x3dFileText);
                 printWriterX3dExcerptFile.close();
            }
            outputLogStringBuilder.append("Total file length: ").append(validationFile.length()   ).append(" bytes").append("\n");
            outputLogStringBuilder.append("X3D file length: ").append(validationFileX3D.length()).append(" bytes").append("\n");
        } 
        catch (IOException t) {
            catchResponse(t, passName);
        }

        // =========================================================================
        if (!foundX_ITE)
        {
            try {
                /* Well formed */
                System.out.println  ("XML well-formed check..."); // keep track on console in case unexpected exception/error messages appear
                appendStart(passName = "XML well-formed check", referenceUrlWellFormed);

                saxParserFactory = SAXParserFactory.newInstance();
                saxParserFactory.setValidating(false);      // Turn off validation
                saxParserFactory.setSchema(null);

                saxParser = saxParserFactory.newSAXParser();
                thisSAXHandler = new ThisSAXHandler(outputLogStringBuilder);
                saxParser.parse(validationFileX3D, thisSAXHandler);
                appendResults(passName, thisSAXHandler.error);
            } catch (IOException | ParserConfigurationException | SAXException t) {
                catchResponse(t, passName);
            }
        }

        // =========================================================================
        try {
            /* DOCTYPE */
            System.out.println  ("DOCTYPE reference check..."); // keep track on console in case unexpected exception/error messages appear
            appendStart(passName = "DOCTYPE reference check", referenceUrlDoctype);
            String validationLog = new X3dDoctypeCheckerModified().processScene(validationFile.getAbsolutePath());
//            if (validationLog.contains(X3dDoctypeCheckerModified.foundHTMLMessage)) {
//                foundHTML = true;
//            }
            outputLogStringBuilder.append("found HTML page wrapping X3D model using X3DOM: ").append(Boolean.toString(foundHTML && foundX3DOM));
            outputLogStringBuilder.append("\n");
            outputLogStringBuilder.append("found HTML page referencing X3D model in X3DCanvas using X_ITE: ").append(Boolean.toString(foundHTML && foundX_ITE));
            outputLogStringBuilder.append("\n");
            outputLogStringBuilder.append(escapeHtml(validationLog));
            outputLogStringBuilder.append("\n");
            appendResults(passName, !foundHTML && (validationLog.contains(X3dDoctypeChecker.errorToken) || validationLog.contains(X3dDoctypeChecker.warningToken)));
        } catch (Throwable t) {
            catchResponse(t, passName);
        }

        // =========================================================================
        try {
            /* X3D DTD validation */
            if (foundHTML)
            {
                System.out.println ("X3D DTD validation..."); // keep track on console in case unexpected exception/error messages appear
                boolean foundCSS = false;
                boolean foundJavascript = false;
                if (foundX3DOM) 
                {
                    appendStart(passName = "X3DOM JavaScript and Cascading Style Sheet (CSS) references check", referenceX3DOM);
                    
                    // source <script type="text/javascript" src="http://www.x3dom.org/download/dev/x3dom-full.js"/>
                    // regex  <script\s+type=['|"]text/javascript['|"]\s+src=['|"]http://www.x3dom.org[^\s]*.js['|"]\s*[/|>\s*</script]>
                    String    regexX3domJs = "<script\\s+type=['|\"]text/javascript['|\"]\\s+src=['|\"]" + referenceX3DOM + "[^\\s]*.js['|\"]\\s*/>";
                    Pattern patternX3domJs = Pattern.compile(regexX3domJs);
                    Matcher matcherX3domJs = patternX3domJs.matcher(htmlFileText);

                    foundJavascript = matcherX3domJs.find(); // find one (and only one) match
                    if (foundJavascript)
                    {
                        outputLogStringBuilder.append("Found online x3dom.js  statement:\n");
                        outputLogStringBuilder.append("<blockquote><code>").append(linkUrlHtml(matcherX3domJs.group(0))).append("</code></blockquote>");
                    }
                    else if (htmlFileText.contains("x3dom.js"))
                    {
                        foundJavascript = true;
                        outputLogStringBuilder.append("Found local x3dom.js statement").append("\n");
                    }
                    else if (htmlFileText.contains("x3dom-full.js"))
                    {
                        foundJavascript = true;
                        outputLogStringBuilder.append("Found local x3dom-full.js statement").append("\n");
                    }
                    else 
                    {
                        outputLogStringBuilder.append("No X3DOM .js  statement found").append("\n");
                    }

                    // Java Regular Expression Tester https://www.freeformatter.com/java-regex-tester.html
                    // NetbeansRegexPlugin - plugin detail http://plugins.netbeans.org/plugin/63405
                    // source <link rel="stylesheet" type="text/css" href="http://www.x3dom.org/download/dev/x3dom.css"/>
                    // regex  <link\s+rel=['|"]stylesheet['|"]\s+type=['|"]text/css['|"]\s+href=['|"]http://www.x3dom.org[^\s]*.css['|"]\s*/>
                    String regexX3domCss = "<link\\s+rel=['|\"]stylesheet['|\"]\\s+type=['|\"]text/css['|\"]\\s+href=['|\"]" + referenceX3DOM + "[^\\s]*.css['|\"]\\s*/>";
                    Pattern patternX3domCss = Pattern.compile(regexX3domCss);
                    Matcher matcherX3domCss = patternX3domCss.matcher(htmlFileText);

                    foundCSS = matcherX3domCss.find(); // find one (and only one) match
                    if (foundCSS) 
                    {
                        outputLogStringBuilder.append("Found online x3dom.css statement:\n");
                        outputLogStringBuilder.append("<blockquote><code>").append(linkUrlHtml(matcherX3domCss.group(0))).append("</code></blockquote>");
                    } 
                    else if (htmlFileText.contains("x3dom.css"))
                    {
                        foundCSS = true;
                        outputLogStringBuilder.append("Found local x3dom.css statement").append("\n");
                    }
                    else
                    {
                        outputLogStringBuilder.append("No x3dom.css statement found").append("\n");
                    }
                    appendResults(passName, !(foundCSS && foundJavascript));    
                } 
                else if (foundX_ITE)
                {
                    appendStart(passName = "X_ITE Cascading Style Sheet (CSS) and JavaScript references check", referenceX_ITE_site);

                    // source <link rel="stylesheet" type="text/css" href="http://code.create3000.de/x_ite/latest/dist/x_ite.css"/>
                    // regex  <link\s+rel=['|"]stylesheet['|"]\s+type=['|"]text/css['|"]\s+href=['|"]http://code.create3000.de/x_ite[^\s]*.css['|"]\s*/>
                    String    regexX_ITECss = "<link\\s+rel=['|\"]stylesheet['|\"]\\s+type=['|\"]text/css['|\"]\\s+href=['|\"]" + referenceX_ITE_code + "[^\\s]*x_ite.css['|\"]\\s*/>";
                    Pattern patternX_ITECss = Pattern.compile(regexX_ITECss);
                    Matcher matcherX_ITECss = patternX_ITECss.matcher(htmlFileText);

                    foundCSS = matcherX_ITECss.find(); // find one (and only one) match
                    if (foundCSS) {
                        outputLogStringBuilder.append("Found X_ITE .css statement:\n");
                        outputLogStringBuilder.append("<blockquote><code>").append(linkUrlHtml(matcherX_ITECss.group())).append("</code></blockquote>");
                    } else {
                        outputLogStringBuilder.append("No X_ITE .css statement found").append("\n");
                    }
                    // source  <script type="text/javascript" src="http://code.create3000.de/x_ite/latest/dist/x_ite.min.js"></script>
                    // regex  <cript\s+type=['|"]text/javascript['|"]\s+src=['|"]http://code.create3000.de/x_ite[^\s]*.js['|"]\s*>\s*</script>
                    String    regexX_ITEJs = "<script\\s+type=['|\"]text/javascript['|\"]\\s+src=['|\"]" + referenceX_ITE_code + "[^\\s]*x_ite.min.js['|\"]\\s*>\\s*</script>";
                    Pattern patternX_ITEJs = Pattern.compile(regexX_ITEJs);
                    Matcher matcherX_ITEJs = patternX_ITEJs.matcher(htmlFileText);

                    foundJavascript = matcherX_ITEJs.find(); // find one (and only one) match
                    if (foundJavascript) {
                        outputLogStringBuilder.append("Found X_ITE .js  statement:\n");
                        outputLogStringBuilder.append("<blockquote><code>").append(linkUrlHtml(matcherX_ITEJs.group())).append("</code></blockquote>");
                    } else {
                        outputLogStringBuilder.append("No X_ITE .js  statement found").append("\n");
                    }
                    appendResults(passName, !(foundCSS && foundJavascript));
                } 
                else if (foundCobweb)
                {
                    appendStart(passName = "Cobweb Cascading Style Sheet (CSS) and JavaScript references check", referenceCobweb);
                    outputLogStringBuilder.append("TODO unimplemented, Cobweb has been replaced by X_ITE ").append(referenceX_ITE_site);

                    appendResults(passName, !(foundCSS && foundJavascript));
                }
            }
        } catch (Exception t) {
            catchResponse(t, passName);
        }
        // =========================================================================
        if (!foundX_ITE)
        {
            try {
                System.out.println ("X3D DTD validation check..."); // keep track on console in case unexpected exception/error messages appear
                /* X3D DTD validation */
                appendStart(passName = "X3D DTD validation check", referenceUrlDtdSchema, "X3D DTD documentation", referenceUrlX3dDtdDocumentation);
                if (x3dFileText.contains("showLog=") || x3dFileText.contains("showProgress=") || x3dFileText.contains("showStat=") || x3dFileText.contains("<X3D id='"))
                {
                    outputLogStringBuilder.append("*** Note that X3DOM allows X3D element to include attributes id, showLog, showProgress, showStats").append("\n");
                }
                saxParserFactory = SAXParserFactory.newInstance();
                saxParserFactory.setValidating(true);
                saxParserFactory.setSchema(null);
                saxParser = saxParserFactory.newSAXParser();
                thisSAXHandler = new ThisSAXHandler(outputLogStringBuilder);
                saxParser.parse(validationFileX3D, thisSAXHandler);
                appendResults(passName, thisSAXHandler.error);
            } catch (IOException | ParserConfigurationException | SAXException t) {
                catchResponse(t, passName);
            }
        }

        // =========================================================================
        if (!foundX_ITE)
        {
            try {
                System.out.println ("X3D schema validation check..."); // keep track on console in case unexpected exception/error messages appear
                /* Schema validation */
                appendStart(passName = "X3D schema validation check", referenceUrlDtdSchema, "X3D schema documentation", referenceUrlX3dSchemaDocumentation);
                if (x3dFileText.contains("showLog=") || x3dFileText.contains("showProgress=") || x3dFileText.contains("showStat=") || x3dFileText.contains("<X3D id='"))
                {
                    outputLogStringBuilder.append("*** Note that X3DOM allows X3D element to include attributes id, showLog, showProgress, showStats").append("\n");
                }
                saxParserFactory = SAXParserFactory.newInstance();
                saxParserFactory.setValidating(true);
                saxParserFactory.setNamespaceAware(true);
                saxParser = saxParserFactory.newSAXParser();
                saxParser.setProperty(JAXPConstants.JAXP_SCHEMA_LANGUAGE, JAXPConstants.W3C_XML_SCHEMA); // see xerces example at http://xerces.apache.org/xerces2-j/faq-pcfp.html
                thisSAXHandler = new ThisSAXHandler(outputLogStringBuilder);
                saxParser.parse(validationFileX3D, thisSAXHandler);
                appendResults(passName, thisSAXHandler.error);
            } catch (IOException | ParserConfigurationException | SAXException t) {
                catchResponse(t, passName);
            }
        }

        // =========================================================================
        if (!foundX_ITE)
        {
            try {
                System.out.println  ("X3dToX3dvClassicVrml.xslt X3dToVrml97.xslt conversion check..."); // keep track on console in case unexpected exception/error messages appear
                /* X3d to ClassicVRML encoding */
                appendStart(passName = "X3dToX3dvClassicVrml.xslt X3dToVrml97.xslt conversion check", referenceUrlX3dToClassicVrml, "Quality Assurance (QA) overview", referenceQualityAssurance);
                xmlStreamSource = new StreamSource(new FileInputStream(validationFileX3D));
                xslStreamSource = new StreamSource(Validator.class.getResourceAsStream(xsdBaseInClassPath + "X3dToX3dvClassicVrmlEncoding.xslt"));
                File classicVrmlOutputFile = File.createTempFile(validationFileX3D.getName() + "_", "_classicVrmlOutput.txt");
                classicVrmlOutputFile.deleteOnExit();
                streamResult = new StreamResult(classicVrmlOutputFile);

                saxonConfiguration = new net.sf.saxon.Configuration();
                saxonConfiguration.setURIResolver((String href, String base) -> {
                    if (href.equals("X3dToVrml97.xslt")) {
                        return new StreamSource(Validator.class.getResourceAsStream(xsdBaseInClassPath + href));
                    }
                    System.out.println("href = " + href + " base = " + base);
                    return null;
                });
                saxonTransformerFactory = new net.sf.saxon.TransformerFactoryImpl(saxonConfiguration);
                saxonTransformer = saxonTransformerFactory.newTransformer(xslStreamSource);
                thisTransformListener = new ThisTransformListener(outputLogStringBuilder);
                saxonTransformer.setErrorListener(thisTransformListener);

                // where are stylesheet xslt:message results?? in Saxon MessageWarner
                StringWriter messageWriter = new StringWriter();
                net.sf.saxon.serialize.MessageWarner messageWarner = new MessageWarner();
                messageWarner.setWriter(messageWriter);
                ((net.sf.saxon.Controller) saxonTransformer).setMessageEmitter(messageWarner);
                saxonTransformer.transform(xmlStreamSource, streamResult);
                outputLogStringBuilder.append(escapeHtml(thisTransformListener.getXsltMessages())); // stylesheet messages
                appendResults(passName, thisTransformListener.error);
                classicVrmlOutputFile.delete();
            } catch (IOException | IllegalArgumentException | TransformerException t) {
                catchResponse(t, passName);
            }
        }

        // =========================================================================
        try {
            System.out.println  ("Regular expression (regex) integer/float data-patterns check..."); // keep track on console in case unexpected exception/error messages appear
            /* regular expression checks, regex */
            appendStart(passName = "Regular expression (regex) integer/float data-patterns check", referenceUrlRegex);
            appendStatement("as part of ", "X3D Regular Expressions (regexes)", "https://www.web3d.org/specifications/X3dRegularExpressions.html");
            Log4JListener.sb = outputLogStringBuilder;
            int numMessages = Log4JListener.numMessages;

            X3dValuesRegexChecker regchk = new X3dValuesRegexChecker(validationFileX3D.getPath());
            String reslt = regchk.processScene();
            outputLogStringBuilder.append(escapeHtml(reslt));
            boolean wasError = (numMessages != Log4JListener.numMessages) || (reslt != null && reslt.length() > 0);
            appendResults(passName, wasError);
        } catch (Throwable t) {
            t.printStackTrace();
            outputLogStringBuilder.append("Error caught: ");
            outputLogStringBuilder.append(t.getClass().getName());
            outputLogStringBuilder.append(": ");
            outputLogStringBuilder.append(t.getLocalizedMessage());
            outputLogStringBuilder.append("\n");
        }

        // =========================================================================
        if (!foundX_ITE)
        {
            try {
                System.out.println  ("X3D Schematron check..."); // keep track on console in case unexpected exception/error messages appear
                /* X3D Schematron */
                appendStart(passName = "X3D Schematron check", referenceUrlDtdSchematron);
                xmlStreamSource = new StreamSource(new FileInputStream(validationFileX3D));
                xslStreamSource = new StreamSource(Validator.class.getResourceAsStream("/stylesheets/X3dSchematronValidityChecks.xslt"));
                File schematronOutputFile = File.createTempFile(validationFile.getName() + "_", "_schematronOutput_1.txt");
                schematronOutputFile.deleteOnExit();
                streamResult = new StreamResult(schematronOutputFile);
                thisTransformListener = new ThisTransformListener(outputLogStringBuilder);

                saxonConfiguration = new net.sf.saxon.Configuration();
                // don't want this output   cfg.setErrorListener(transLis);  // get errors on compile, too
                saxonTransformerFactory = new net.sf.saxon.TransformerFactoryImpl(saxonConfiguration);
                saxonTransformer = saxonTransformerFactory.newTransformer(xslStreamSource);
                saxonTransformer.setErrorListener(thisTransformListener);
                saxonTransformer.transform(xmlStreamSource, streamResult);

                xslStreamSource = new StreamSource(Validator.class.getResourceAsStream("/stylesheets/SvrlReportText.xslt"));
                xmlStreamSource = new StreamSource(new FileInputStream(schematronOutputFile));// Use output from last 

                StringWriter resultStringWriter = new StringWriter();
                streamResult = new StreamResult(resultStringWriter);
                saxonTransformer = saxonTransformerFactory.newTransformer(xslStreamSource);
                saxonTransformer.setErrorListener(new ThisTransformListener(outputLogStringBuilder));

                saxonTransformer.transform(xmlStreamSource, streamResult);
                String resultString = resultStringWriter.toString();
                outputLogStringBuilder.append(escapeHtml(resultString));
                boolean error = (resultString.length() > 0) && resultString.contains("error");// only  output if specifically stated error
                appendResults(passName, error);
                if (resultString.length() > 0) {
                    outputLogStringBuilder.append("Good practice is to fix errors and warnings wherever possible, and consider silencing harmless informational messages, so that important indicators remain noticeable.");
                    outputLogStringBuilder.append("\n");
                }
                schematronOutputFile.delete();
            }
            catch (IOException | IllegalArgumentException | TransformerException t) {
                catchResponse(t, passName);
            }
        }

        // =========================================================================
        if (!foundX_ITE)
        {
            try {
                System.out.println  ("X3D Tidy check..."); // keep track on console in case unexpected exception/error messages appear
                /* X3dTidy.xslt */
                appendStart(passName = "X3D Tidy check", referenceUrlX3dTidy, "X3D Tidy Scene Cleanup, Corrections and Modifications", referenceX3dTidy);
                xmlStreamSource = new StreamSource(new FileInputStream(validationFileX3D));
                xslStreamSource = new StreamSource(Validator.class.getResourceAsStream(xsdBaseInClassPath + "X3dTidy.xslt")); // note capitalization
                x3dTidyOutputFile = File.createTempFile(validationFile.getName() + "_", "_X3dTidyOutput.x3d");
                x3dTidyOutputFile.deleteOnExit();

                saxonConfiguration = new net.sf.saxon.Configuration();
                saxonConfiguration.setURIResolver((String href, String base) -> {
                    if (href.equals("X3dTidy.xslt")) {
                        return new StreamSource(Validator.class.getResourceAsStream(xsdBaseInClassPath + href));
                    }
                    System.out.println("href = " + href + " base = " + base);
                    return null;
                });
                // TODO set defaults to not modify file's revision date
    //            saxonConfiguration.setConfigurationProperty("-reviseCurrentDate", "false"); /? INCORRECT
                // TODO add Saxon javadoc

                StringWriter resultStringWriter = new StringWriter();
                streamResult = new StreamResult(resultStringWriter);
                saxonTransformerFactory = new net.sf.saxon.TransformerFactoryImpl(saxonConfiguration);
                saxonTransformer = saxonTransformerFactory.newTransformer(xslStreamSource);
                thisTransformListener = new ThisTransformListener(outputLogStringBuilder);
                saxonTransformer.setErrorListener(thisTransformListener);
                // special handling for Saxon to capture xsl:message output
                // https://stackoverflow.com/questions/4695489/capture-xslmessage-output-in-java
                // https://stackoverflow.com/questions/33349610/xsltmessage-in-saxon-9-4-vs-saxon-9-6
                // http://www.saxonica.com/html/documentation/javadoc/net/sf/saxon/event/Receiver.html
                // http://www.saxonica.com/html/documentation/javadoc/net/sf/saxon/serialize/MessageWarner.html
                // https://sourceforge.net/p/saxon/discussion/94027/thread/adad0e12/
                // https://www.programcreek.com/java-api-examples/index.php?api=net.sf.saxon.Controller

                // where are stylesheet xslt:message results?? in Saxon MessageWarner
                StringWriter messageWriter = new StringWriter();
                net.sf.saxon.serialize.MessageWarner messageWarner = new MessageWarner();
                messageWarner.setWriter(messageWriter);
                ((net.sf.saxon.Controller) saxonTransformer).setMessageEmitter(messageWarner);
                saxonTransformer.transform(xmlStreamSource, streamResult);
                outputLogStringBuilder.append(escapeHtml(thisTransformListener.getXsltMessages())); // stylesheet messages
                appendResults(passName, thisTransformListener.error);
                // do not delete resulting x3dTidyOutputFile since it may be used in pretty print
            } catch (IOException | IllegalArgumentException | TransformerException t) {
                catchResponse(t, passName);
            }
        }

        // =========================================================================
        if (!foundX_ITE)
        {
            try {
                System.out.println  ("X3D to XHTML pretty-print listing check..."); // keep track on console in case unexpected exception/error messages appear
                /* X3dToXhtml.xslt */
                appendStart(passName = "X3D to XHTML pretty-print listing check", referenceUrlPrettyPrintHtml, "Quality Assurance (QA) overview", referenceQualityAssurance);
                if (prettyPrintX3dTidyOutputFile) {
                    xmlStreamSource = new StreamSource(new FileInputStream(x3dTidyOutputFile));
                } else {
                    xmlStreamSource = new StreamSource(new FileInputStream(validationFileX3D));
                }
                xslStreamSource = new StreamSource(Validator.class.getResourceAsStream("/stylesheets/X3dToXhtml.xslt"));
                File x3dToXhtmlOutputFile = File.createTempFile(validationFile.getName() + "_", "_X3dToXhtmlOutput.txt");
                x3dToXhtmlOutputFile.deleteOnExit();

                saxonConfiguration = new net.sf.saxon.Configuration();
                saxonConfiguration.setURIResolver((String href, String base) -> {
                    if (href.equals("X3dToXhtml.xslt") || href.equals("X3dExtrusionToSvgViaXslt1.1.xslt")) {
                        return new StreamSource(Validator.class.getResourceAsStream(xsdBaseInClassPath + href));
                    }
                    System.out.println("href = " + href + " base = " + base);
                    return null;
                });

                StringWriter resultStringWriter = new StringWriter();
                streamResult = new StreamResult(resultStringWriter);
                saxonTransformerFactory = new net.sf.saxon.TransformerFactoryImpl(saxonConfiguration);
                saxonTransformer = saxonTransformerFactory.newTransformer(xslStreamSource);
                thisTransformListener = new ThisTransformListener(outputLogStringBuilder);
                // server returns linked document that is not necessarily in same directory as local document
                saxonTransformer.setParameter("baseUrlAvailable", "false");
                saxonTransformer.setErrorListener(thisTransformListener);
                saxonTransformer.transform(xmlStreamSource, streamResult);

                String resultString = resultStringWriter.toString();
                resultString = resultString.substring(resultString.indexOf("<body>") + 6, resultString.indexOf("</body>")); // trim to excerpt desired results

                outputLogStringBuilder.append("Conversion complete, documentation appears below.");
                outputLogStringBuilder.append("<div><i>Authoring hints:</i>  Select url links to check the availability of online addresses.  Comments are inserted with local links to document ROUTE connections.  Node tooltips are also provided.</div>");
                outputLogStringBuilder.append("<div style='background-color:#ffffff;white-space:normal;'><hr />");
                outputLogStringBuilder.append(resultString).append("\n"); // no need to escapeHtml()
                outputLogStringBuilder.append("<br />\n").append("<hr />\n").append("</div>");
                appendResults(passName, thisTransformListener.error);
                x3dToXhtmlOutputFile.delete();
            } catch (IOException | IllegalArgumentException | TransformerException t) {
                catchResponse(t, passName);
            }
            validationFileX3D.delete();
        }

        // =========================================================================
        System.out.println  ("Validation checks complete for " + validationFile.getName()); // keep track on console in case unexpected exception/error messages appear
        outputLogStringBuilder.append("\n--------- Validation checks complete for <b>");
        outputLogStringBuilder.append(validationFile.getName());
        outputLogStringBuilder.append("</b> ---------\n");
        outputLogStringBuilder.append("\nThe ")
                .append("<a href='http://www.web3d.org/x3d/content/examples/X3dResources.html#AuthoringSupport' target='X3dValidatorReference'>Authoring Support</a> ")
                .append("section of the ")
                .append("<a href='http://www.web3d.org/x3d/content/examples/X3dResources.html' target='X3dValidatorReference'>X3D Resources</a> ")
                .append("page lists numerous additional resources for authoring X3D.");

        return outputLogStringBuilder.toString();
    }

    /**
     * Replace < and > with escape characters
     */
    private static String escapeHtml(String s) {
        return s.replace(">", "&gt;").replace("<", "&lt;");
    }

    private static String linkUrlHtml(String match) {
        String matchPrefix, matchUrl, matchSuffix, matchResult;

        matchPrefix = match.substring(0, match.indexOf("http"));
        matchUrl = match.substring(match.indexOf("http"));
        matchUrl = matchUrl.substring(0, matchUrl.indexOf("\""));     // strip remainder
        matchSuffix = match.substring(match.indexOf("http"));
        matchSuffix = matchSuffix.substring(matchSuffix.indexOf("\"")); // strip url
        matchResult = escapeHtml(matchPrefix)
                + "<a href='" + matchUrl + "' target='_blank'>" + matchUrl + "</a>" + matchSuffix;
        return matchResult;
    }

    static class ThisTransformListener implements ErrorListener {

        public boolean error = false;
        private final StringBuilder sb;
        private StringBuilder xsltMessages = new StringBuilder();

        public ThisTransformListener(StringBuilder sb) {
            this.sb = sb;
        }

        public String getXsltMessages() {
            return xsltMessages.toString();
        }

        public void clearXsltMessages() {
            xsltMessages = new StringBuilder();
        }

        private void handle(String typ, TransformerException ex) {
            if (ex instanceof net.sf.saxon.trans.XPathException) {
                // XSLT stylesheet messages relayed by Saxon
                xsltMessages.append(ex.getMessage());
                xsltMessages.append("\n");
            } else {
                sb.append("Error type: ");
                sb.append(typ);
                sb.append("\n");
                if (ex != null)
                {
                    sb.append(ex.getClass().getSimpleName());
                    sb.append(":\n");
                    sb.append(ex.getLocalizedMessage());
                    sb.append("\n");
                }
            }
        }

        @Override
        public void warning(TransformerException exception) throws TransformerException {
            handle("Warning", exception);
        }

        @Override
        public void error(TransformerException exception) throws TransformerException {
            error = true;
            handle("Error", exception);
        }

        @Override
        public void fatalError(TransformerException exception) throws TransformerException {
            error = true;
            handle("Fatal error", exception);
        }
    }

    static class ThisSAXHandler extends DefaultHandler {

        private final StringBuilder sb;
        public boolean error = false;

        ThisSAXHandler(StringBuilder sb) {
            this.sb = sb;
        }

        private void handle(String typ, SAXParseException e) {
            sb.append("Error type: ");
            sb.append(typ);
            sb.append("\n");
            sb.append(e.getClass().getSimpleName());
            sb.append(":\n");
            sb.append(e.getLocalizedMessage());
            sb.append("\n");
        }

        @Override
        public void error(SAXParseException e) throws SAXException {
            error = true;
            handle("Error", e);
            super.error(e);
        }

        @Override
        public void fatalError(SAXParseException e) throws SAXException {
            error = true;
            handle("Fatal error", e);
            super.fatalError(e);
        }

        @Override
        public void warning(SAXParseException e) throws SAXException {
            handle("Warning", e);
            super.warning(e);
        }
    }
}
