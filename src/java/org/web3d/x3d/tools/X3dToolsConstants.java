/* Program:      Extensible 3D (X3D) Tools
 *
 * Author(s):    Terry Norbraten, NPS MOVES
 *               http://www.nps.edu and http://www.MovesInstitute.org
 *
 * Created:      April 11, 2006, 10:52 AM
 *
 * File:         X3dToolsConstants.java
 *
 * Compiler:     netBeans IDE 8.2 (External), JDK 1.8
 *
 * Description:  Interface class for defining constants for X3D Tools
 *
 * References:
 *
 * Requirements: None
 *
 * URL:          https://www.web3d.org/x3d/content/tools/canonical/src/org/web3d/x3d/tools/X3dToolsConstants.java
 *
 * Assumptions:  None
 *
 * TODO:         None
 *
 * Copyright (c) 1995-2021 held by the author(s).  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer
 *    in the documentation and/or other materials provided with the
 *    distribution.
 *  * Neither the names of the Naval Postgraduate School (NPS)
 *    Modeling Virtual Environments and Simulation (MOVES) Institute
 *    (http://www.nps.edu and http://www.movesinstitute.org)
 *    nor the names of its contributors may be used to endorse or
 *    promote products derived from this software without specific
 *    prior written permission.
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

/**
 * Interface class for defining constants for X3D Tools
 *
 * @author <a href="mailto:tndorbra@nps.edu?subject=org.web3d.x3d.tools.X3dToolsConstants">Terry Norbraten, NPS MOVES</a>
 * @version $Id$
 * <p>
 *   <b>History:</b>
 * </p>
 *   <pre><b>
 *     Date:     15 April 2006
 *     Time:     1538
 *     Author:   <a href="mailto:tdnorbra@nps.edu?subject=X3dToolsConstants">Terry Norbraten, NPS MOVES</a>
 *     Comments: 1) Initial
 *               2) Added argument switches for DTD checker
 *
 *     Date:     19 July 2006
 *     Time:     2241
 *     Author:   <a href="mailto:tdnorbra@nps.edu?subject=X3dToolsConstants">Terry Norbraten, NPS MOVES</a>
 *     Comments: 1) Added final Strings from X3dDtdChecker
 *               2) Added a DEBUG flag to turn on/off specific c14n
 *                  functionality for diff checking
 *               3) Added a constant to represent a comma separator in MF-type
 *                  array values
 *
 *     Date:     25 December 2006
 *     Time:     2237
 *     Author:   <a href="mailto:tdnorbra@nps.edu?subject=X3dToolsConstants">Terry Norbraten, NPS MOVES</a>
 *     Comments: 1) Added final Strings for checking of empty attribute-value
 *                  pairs
 *               2) Added final String to denote numeric character reference
 *                  "line feed" &amp;#10;
 *
 *     Date:     28 JAN 07
 *     Time:     0021
 *     Author:   <a href="mailto:tdnorbra@nps.edu?subject=X3dToolsConstants">Terry Norbraten, NPS MOVES</a>
 *     Comments: 1) Added pointer to the v3.1 X3D Schema
 *
 *     Date:     14 MAR 2007
 *     Time:     1637
 *     Author:   <a href="mailto:tdnorbra@nps.edu?subject=X3dToolsConstants">Terry Norbraten, NPS MOVES</a>
 *     Comments: 1) Added String pointers to the local and online versions of
 *                  the 3.0 and 3.1 DTDs
 *               2) Removed String for .canonical/ directory extension
 *                 (deprecated)
 *               3) Now includes usage messages
 *
 *     Date:     01 JUN 2007
 *     Time:     2138Z
 *     Author:   <a href="mailto:tdnorbra@nps.edu?subject=org.web3d.x3d.tools.X3dToolsConstants">Terry Norbraten, NPS MOVES</a>
 *     Comments: 1) Changed local DTD URLs to proper paths
 *
 *     Date:     02 DEC 2007
 *     Time:     0214Z
 *     Author:   <a href="mailto:tdnorbra@nps.edu?subject=org.web3d.x3d.tools.X3dToolsConstants">Terry Norbraten, NPS MOVES</a>
 *     Comments: 1) Added support for X3D 3.2
 *
 *     Date:     26 MAY 2008
 *     Time:     0616Z
 *     Author:   <a href="mailto:tdnorbra@nps.edu?subject=org.web3d.x3d.tools.X3dToolsConstants">Terry Norbraten, NPS MOVES</a>
 *     Comments: 1) All data members in an interface are by default, public,
 *                  static and final, duh...
 *
 *     Date:     10 JUL 2008
 *     Time:     0101Z
 *     Author:   <a href="mailto:tdnorbra@nps.edu?subject=org.web3d.x3d.tools.X3dToolsConstants">Terry Norbraten, NPS MOVES</a>
 *     Comments: 1) Add output messages about C14N compliance
 *     Comments: 2) Changed local DTD URLs to proper paths

 *
 *     Date:     28 AUG 2011
 *     Author:   Don Brutzman
 *     Comments: 1) Added support for X3D 3.3
 *   </b></pre>
 */
public interface X3dToolsConstants {

    /* DATA FIELD(s) */

    /** Debugging flag */
    boolean NO_DEBUG = true;

    /** Comma character */
    char COMMA_CHAR = ',';

    /**
     * candidate values: "'" or "\"" (apostrophe numeric character reference
     * &#39; or quote numeric character reference &#34;)
     */
    char DEFAULT_ATTRIBUTE_DELIMITER = '\'';

    /** DOCTYPE internal subset closing */
    char DOCTYPE_INTERNAL_CLOSING = ']';

    /** DOCTYPE internal subset opening */
    char DOCTYPE_INTERNAL_OPENING = '[';

    /** Equals sign character */
    char EQUALS_SIGN = '=';

    /** Normal whitespace */
    char NORMAL_SPACING = ' ';

    /** Quote character */
    char QUOTE_CHAR = '"';

    /** Element closing tag */
    char TAG_CLOSING = '>';

    /** Element opening tag */
    char TAG_OPENING = '<';

    /** JVM exit code specifying abnormal termination */
    int EXIT_ABNORMAL = -1;

    /** Character entity for ' in MFString arrays */
    String CHARACTER_ENTITY_APOS = "&apos;";

    /** Character entity for " in SF/MFString values */
    String CHARACTER_ENTITY_QUOTE = "&quot;";

    /** CDATA closing tag */
    String CDATA_CLOSING = "]]>";

    /** CDATA opening tag */
    String CDATA_OPENING = "<![CDATA[";

    /** Comment closing tag */
    String COMMENT_CLOSING = "-->";

    /** Comment opening tag */
    String COMMENT_OPENING = "<!--";

    /** Message thrown when proper command line usage is not observed */
    String C14N_USAGE_MESSAGE = "[Usage] java -jar dist/lib/X3dC14n.jar [optional -v] <path>/SceneName.x3d or,\n" +
            "        java -jar dist/lib/X3dC14n.jar [optional -v] <path>/SceneName.x3d <path>/SceneNameCanonical.xml";

    /**
     * Code currently depends on computing length for proper lineup of element
     * closing tags.  Indentation 2 spaces.
     */
    String DEFAULT_INDENTATION = "  ";

    /** X3dDtdChecker usage message */
    String DTD_USAGE_MESSAGE = "[Usage] java -cp dist/lib/X3dC14n.jar org.web3d.x3d.tools.X3dDtdChecker [-f | -t] [optional -v] <path>/SceneName.x3d";

    /** System ID for online 3.0 DTD URL */
    String DTD_URL_3_0 = "https://www.web3d.org/specifications/x3d-3.0.dtd";

    /** System ID for online 3.1 DTD URL */
    String DTD_URL_3_1 = "https://www.web3d.org/specifications/x3d-3.1.dtd";

    /** System ID for online 3.2 DTD URL */
    String DTD_URL_3_2 = "https://www.web3d.org/specifications/x3d-3.2.dtd";

    /** System ID for online 3.3 DTD URL */
    String DTD_URL_3_3 = "https://www.web3d.org/specifications/x3d-3.3.dtd";

    /** System ID for online 4.0 DTD URL */
    String DTD_URL_4_0 = "https://www.web3d.org/specifications/x3d-4.0.dtd";

    /** Element singleton closing tag */
    String ELEMENT_SINGLETON_CLOSING = "/>";

    /** Element with children closing tag */
    String ELEMENT_WITH_CHILD_CLOSING = "</";

    /** used to test for empty attribute-value pairs */
    String EMPTY_APOSTROPHES = "''";

    /** used to set initial indentation index */
    String EMPTY_STRING = "";

    /** used to test for empty attribute-value pairs */
    String EMPTY_QUOTES = "\"\"";

    /** Pointer to the X3D examples directory */
    String EXAMPLES_DIR_BASE = "c:/x3d-code/www.web3d.org/x3d/content/examples";

    /** Switch argument for final DTD */
    String FINAL_DTD_SELECTION = "-f";

    /** Final 3.0 DOCTYPE declaration */
    String FINAL_30_DOCTYPE = "<!DOCTYPE X3D PUBLIC \"ISO//Web3D//DTD X3D 3.0//EN\" \"https://www.web3d.org/specifications/x3d-3.0.dtd\"";

    /** Final 3.1 DOCTYPE declaration */
    String FINAL_31_DOCTYPE = "<!DOCTYPE X3D PUBLIC \"ISO//Web3D//DTD X3D 3.1//EN\" \"https://www.web3d.org/specifications/x3d-3.1.dtd\"";

    /** Final 3.2 DOCTYPE declaration */
    String FINAL_32_DOCTYPE = "<!DOCTYPE X3D PUBLIC \"ISO//Web3D//DTD X3D 3.2//EN\" \"https://www.web3d.org/specifications/x3d-3.2.dtd\"";

    /** Final 3.3 DOCTYPE declaration */
    String FINAL_33_DOCTYPE = "<!DOCTYPE X3D PUBLIC \"ISO//Web3D//DTD X3D 3.3//EN\" \"https://www.web3d.org/specifications/x3d-3.3.dtd\"";

    /** Final 4.0 DOCTYPE declaration */
    String FINAL_40_DOCTYPE = "<!DOCTYPE X3D PUBLIC \"ISO//Web3D//DTD X3D 4.0//EN\" \"https://www.web3d.org/specifications/x3d-4.0.dtd\"";

    /** System ID for local 3.0 DTD URL */
    String LOCAL_DTD_URL_3_0 = "c:/x3d-code/www.web3d.org/specifications/x3d-3.0.dtd";

    /** System ID for local 3.1 DTD URL */
    String LOCAL_DTD_URL_3_1 = "c:/x3d-code/www.web3d.org/specifications/x3d-3.1.dtd";

    /** System ID for local 3.2 DTD URL */
    String LOCAL_DTD_URL_3_2 = "c:/x3d-code/www.web3d.org/specifications/x3d-3.2.dtd";

    /** System ID for local 3.3 DTD URL */
    String LOCAL_DTD_URL_3_3 = "c:/x3d-code/www.web3d.org/specifications/x3d-3.3.dtd";

    /** System ID for local 4.0 DTD URL */
    String LOCAL_DTD_URL_4_0 = "c:/x3d-code/www.web3d.org/specifications/x3d-4.0.dtd";

    /** Newline character */
    String NEWLINE = "\n";

    /** Numeric character reference for ' in SF/MFString values */
    String NUMERIC_CHARACTER_REFERENCE_APOS = "&#39;";

    /** Numeric character reference for LF in SF/MFString values */
    String NUMERIC_CHARACTER_REFERENCE_LINE_FEED = "&#10;";

    /** Numeric character reference for " in SF/MFString values */
    String NUMERIC_CHARACTER_REFERENCE_QUOTE = "&#34;";

    /** Any other DOCTYPE Regex */
    String REGEX_ANY_DOCTYPE = "<!DOCTYPE X3D PUBLIC";

    /**
     * Final DOCTYPE Regex for 3.0 DTD
     * Removed final closing angle bracket &gt; so that files with embedded
     * entity-reference declarations (such as
     * examples/development/QuadTreeExamples) will work
     * &lt;!DOCTYPE X3D PUBLIC "ISO//Web3D//DTD X3D 3.0//EN" "https://www.web3d.org/specifications/x3d-3.0.dtd"&gt;
     */
    String REGEX_FINAL_30_DOCTYPE = "<!DOCTYPE X3D PUBLIC(\\s)+\"ISO//Web3D//DTD X3D 3.0//EN\"(\\s)+\"http(s)?://www.web3d.org/specifications/x3d-3.0.dtd\"(\\s)*";

    /**
     * Final DOCTYPE Regex for 3.1 DTD
     * &lt;!DOCTYPE X3D PUBLIC "ISO//Web3D//DTD X3D 3.1//EN" "https://www.web3d.org/specifications/x3d-3.1.dtd"&gt;
     */
    String REGEX_FINAL_31_DOCTYPE = "<!DOCTYPE X3D PUBLIC(\\s)+\"ISO//Web3D//DTD X3D 3.1//EN\"(\\s)+\"http(s)?://www.web3d.org/specifications/x3d-3.1.dtd\"(\\s)*";

    /**
     * Final DOCTYPE Regex for 3.2 DTD
     * &lt;!DOCTYPE X3D PUBLIC "ISO//Web3D//DTD X3D 3.2//EN" "https://www.web3d.org/specifications/x3d-3.2.dtd"&gt;
     */
    String REGEX_FINAL_32_DOCTYPE = "<!DOCTYPE X3D PUBLIC(\\s)+\"ISO//Web3D//DTD X3D 3.2//EN\"(\\s)+\"http(s)?://www.web3d.org/specifications/x3d-3.2.dtd\"(\\s)*";

    /**
     * Final DOCTYPE Regex for 3.3 DTD
     * &lt;!DOCTYPE X3D PUBLIC "ISO//Web3D//DTD X3D 3.3//EN" "https://www.web3d.org/specifications/x3d-3.3.dtd"&gt;
     */
    String REGEX_FINAL_33_DOCTYPE = "<!DOCTYPE X3D PUBLIC(\\s)+\"ISO//Web3D//DTD X3D 3.3//EN\"(\\s)+\"http(s)?://www.web3d.org/specifications/x3d-3.3.dtd\"(\\s)*";

    /**
     * Final DOCTYPE Regex for 4.0 DTD
     * &lt;!DOCTYPE X3D PUBLIC "ISO//Web3D//DTD X3D 4.0//EN" "https://www.web3d.org/specifications/x3d-4.0.dtd"&gt;
     */
    String REGEX_FINAL_40_DOCTYPE = "<!DOCTYPE X3D PUBLIC(\\s)+\"ISO//Web3D//DTD X3D 4.0//EN\"(\\s)+\"http(s)?://www.web3d.org/specifications/x3d-4.0.dtd\"(\\s)*";

    /**
     * Transitional 3.0 DOCTYPE Regex
     * &lt;!DOCTYPE X3D PUBLIC "https://www.web3d.org/specifications/x3d-3.0.dtd" "file:///www.web3d.org/TaskGroups/x3d/translation/x3d-3.0.dtd"&gt;
     */
    String REGEX_TRANSITIONAL_30_DOCTYPE = "<!DOCTYPE X3D PUBLIC(\\s)+\"https://www.web3d.org/specifications/x3d-3.0.dtd\"(\\s)+\"file:///www.web3d.org/TaskGroups/x3d/translation/x3d-3.0.dtd\"(\\s)*";

    /**
     * Transitional 3.1 DOCTYPE Regex
     * &lt;!DOCTYPE X3D PUBLIC "https://www.web3d.org/specifications/x3d-3.1.dtd" "file:///www.web3d.org/TaskGroups/x3d/translation/x3d-3.1.dtd"&gt;
     */
    String REGEX_TRANSITIONAL_31_DOCTYPE = "<!DOCTYPE X3D PUBLIC(\\s)+\"https://www.web3d.org/specifications/x3d-3.1.dtd\"(\\s)+\"file:///www.web3d.org/TaskGroups/x3d/translation/x3d-3.1.dtd\"(\\s)*";

    /**
     * Regex expression for both XML versions 1.0 and 1.1
     * had trouble with back-referencing group \1 and \2 to ensure matching
     * single/double-quote characters :(
     */
    String REGEX_XML_HEADER = "<\\?xml version=(\"|')1.(0|1)(\"|') encoding=(\"|')UTF-8(\"|')\\?>";
    
    /**
     * Regular XML header, must match exactly or tool results are unpredictable.
     */
    String REGULAR_XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

    /** Check for malformed floating point numbers.
     * Reference: buildExamplesTemplate.xml
     * &lt;property name="regexGarbledFloats" value="(\s|,|&quot;|')(((\+|-)?((\.\d+)|(\d+\.\d*))((E|e)(\+|-)?\d+)?(\.|\+|-)+(\d*))|(\d+((\+|-)\d+)+))(\s|,|&quot;|')"/&gt;
     */
    /** Check for malformed floating point numbers.
     * Reference: buildExamplesTemplate.xml
     * &lt;property name="regexGarbledFloats" value="(\s|,|&quot;|')(((\+|-)?((\.\d+)|(\d+\.\d*))((E|e)(\+|-)?\d+)?(\.|\+|-)+(\d*))|(\d+((\+|-)\d+)+))(\s|,|&quot;|')"/&gt;
     */
    String             REGEX_GARBLED_FLOATS =      "(\\s|,|\"|')(((\\+|-)?((\\.\\d+)|(\\d+\\.\\d*))((E|e)?(\\+|-)?\\d+)?(\\.|\\+|-)+(\\d*))|(\\d+((\\+|-)\\d+)+))(\\s|,|\"|')";

    /** Check for leading zeroes in integer or floating point numbers.
     * Reference: buildExamplesTemplate.xml
     * &lt;property name="regexLeadingZeroes" value="(\s|,|&quot;|')(\+|-)?0\d+(\.\d*)?((E|e)(\+|-)?\d+)?(\s|,|&quot;|')"/&gt;
     */
    String              REGEX_LEADING_ZEROES =     "(\\s|,|\"|')(\\+|-)?0\\d+(\\.\\d*)?((E|e)(\\+|-)?\\d+)?(\\s|,|\"|')";

    /** Switch argument for transitional DTD */
    String TRANSITIONAL_DTD = "-t";

    /**
     * Transitional 3.0 DOCTYPE declaration.  Removed final closing angle bracket &gt; so
     * that files with embedded entity-reference declarations (such as
     * examples/development/QuadTreeExamples) will work
     */
    String TRANSITIONAL_30_DOCTYPE = "<!DOCTYPE X3D PUBLIC \"https://www.web3d.org/specifications/x3d-3.0.dtd\" \"file:///www.web3d.org/TaskGroups/x3d/translation/x3d-3.0.dtd\"";

    /** Transitional 3.1 DOCTYPE declaration */
    String TRANSITIONAL_31_DOCTYPE = "<!DOCTYPE X3D PUBLIC \"https://www.web3d.org/specifications/x3d-3.1.dtd\" \"file:///www.web3d.org/TaskGroups/x3d/translation/x3d-3.1.dtd\"";

    /** Transitional warning comment */
    String WARNING_COMMENT = "<!-- Warning:  transitional DOCTYPE in source .x3d file -->\n";

    /** Regex expression to check for Transitional warning comment */
    String WARNING_REGEX = "<!--(\\s)*Warning:  transitional DOCTYPE in source .x3d file(\\s)*-->(\\s)*";

    /** Pointer to the 3.0 X3D Schema */
    String X3D_30_SCHEMA = "c:/x3d-code/www.web3d.org/specifications/x3d-3.0.xsd";

    /** Pointer to the 3.1 X3D Schema */
    String X3D_31_SCHEMA = "c:/x3d-code/www.web3d.org/specifications/x3d-3.1.xsd";

    /** Pointer to the 3.2 X3D Schema */
    String X3D_32_SCHEMA = "c:/x3d-code/www.web3d.org/specifications/x3d-3.2.xsd";

    /** Pointer to the 3.3 X3D Schema */
    String X3D_33_SCHEMA = "c:/x3d-code/www.web3d.org/specifications/x3d-3.3.xsd";

    /** Pointer to the 4.0 X3D Schema */
    String X3D_40_SCHEMA = "c:/x3d-code/www.web3d.org/specifications/x3d-4.0.xsd";

    /** Message about X3D c14n compliant */
    String C14N_COMPLIANT = "Source file is already X3D C14N compliant";

    /** Message about X3D c14n non-compliance and rewriting out as such */
    String C14N_NON_COMPLIANT = "X3D Canonicalization (C14N) complete";

    /** Message informing of embedded digital signature in X3D document */
    String DIGITAL_SIGNATURE_FOUND = "Source file has been digitally signed.  Assuming already in X3D C14N form";

    /* PUBLIC METHOD(s) */

} // end interface file X3dToolsConstants.java