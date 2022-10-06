/*
 * Filename:     BaseServlet.java
 * Created:      2 APR 2012
 * Author:       Mike Bailey
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

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Mike Bailey, jmbailey@nps.edu
 *
 * @version	$Id$
 * @since $Date$ @copyright	Copyright (C) 2011
 */
abstract public class BaseServlet extends HttpServlet
{
  // n.b. space sensitive!
  public static String HTML_FILE_DELIMITER   = "<!--DELIMITER-->";
  public static String HTML_FILE             = "xindex.html";
  public static String URL_PARAMETER_START   = "/*URL_PARAMETER_START*/";
  public static String URL_PARAMETER_END     = "/*URL_PARAMETER_END*/";
  public static String FILE_PARAMETER_START  = "/*FILE_PARAMETER_START*/";
  public static String FILE_PARAMETER_END    = "/*FILE_PARAMETER_END*/";
  
  protected static String indexFileFirst;
  protected static String indexFileSecond;
  
  public BaseServlet() {
      super();
  }

  /** Splits the file in two around the scene info, but also handles passed url 
   * (not form-supplied) parameters
   * @param httpServletRequest and HttpServletRequest
   * @throws java.lang.Exception if something mucks up
   */
  @SuppressWarnings("CallToThreadDumpStack")
  protected void initHtmlPieces(HttpServletRequest httpServletRequest) throws Exception
  {
    String s = getServletContext().getRealPath("/");
    s += HTML_FILE;
    s = file2String(s);
    
    s = checkForParameter(s, httpServletRequest.getParameter("url"), httpServletRequest.getParameter("file"));
    
    String[] sa = s.split(HTML_FILE_DELIMITER);
    
    indexFileFirst  = sa[0];
    indexFileSecond = sa[1];
  }

  private String file2String(String path) throws FileNotFoundException, IOException
  {
    Reader fr = new FileReader(path);
    StringBuilder sb = new StringBuilder();
    char[] buf = new char[4096];
    int n;

    while ((n = fr.read(buf)) > 0) {
      sb.append(buf, 0, n);
    }
    return sb.toString();
  }
  
  private String checkForParameter(String s, String url, String file)
  {
    if(url != null) {
      return subForParameter(s,url,URL_PARAMETER_START,URL_PARAMETER_END);     
    }
    else if(file != null) {
      return subForParameter(s,file,FILE_PARAMETER_START,FILE_PARAMETER_END);
    }
    return s;
  }

  private String subForParameter(String s, String parm, String startTag, String endTag)
  {
    int strt = s.indexOf(startTag);
    int end = s.indexOf(endTag)+endTag.length();
    if(strt == -1 || end == -1)
      return s;
    
    StringBuilder sb = new StringBuilder(s);
    sb.replace(strt, end, " =\""+parm+"\";");
    return sb.toString();
  }
}
