/*
 * Filename:     Index.java
 * Created:      2 APR 2012
 * Author:       Mike Bailey, Don Brutzman
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
package edu.nps.moves.x3d;

import com.oreilly.servlet.MultipartRequest;
import java.io.*;
import java.net.MalformedURLException;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpStatus;

/**
 * @author Mike Bailey, jmbailey@nps.edu
 *
 * @version	$Id$
 * @since $Date$ @copyright	Copyright (C) 2011
 */
@WebServlet(name = "Index",
        description = "Validator for X3D file formats",
        displayName = "X3D Validator",
        /** Array of URL patterns to which this Filter applies
         * https://stackoverflow.com/questions/16578694/servlet-webservlet-urlpatterns
         * https://www.javaguides.net/2019/02/webservlet-annotation-example.html
         */
        urlPatterns = {"/"}, // {"/validate"},
        asyncSupported = true)
@MultipartConfig()
@SuppressWarnings("serial")
public class Index extends BaseServlet {

    private static final String TEMP_PREFIX = "X3DValidator";

    private String originalUrl;
    private String trimmedUrl = new String();
    private String urlFilename;

    public Index() {
        super();
    }

    /**
     * Handles the HTTP <code>GET</code> method. This is called on first
     * invocation, from bookmark or link
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        PrintWriter outPrintWriter2 = null;
        
        try (PrintWriter outPrintWriter = response.getWriter()) {
            initHtmlPieces(request);
            outPrintWriter2 = outPrintWriter;
            outPrintWriter.print(indexFileFirst+indexFileSecond);
            outPrintWriter.flush();
        } catch (Exception e) {
            if (outPrintWriter2 != null) {
                outPrintWriter2.print("<html><head><title><Servlet Error</title></head><body><h1>Servlet error: </h1><br/>");
                outPrintWriter2.print(e.getClass().getSimpleName() + "<br/>");
                outPrintWriter2.print(e.getLocalizedMessage());
                outPrintWriter2.print("</body></html>");
            }
        } finally {
            if (outPrintWriter2 != null)
                outPrintWriter2.close();
        }
    }

    /**
     * Handles the HTTP <code>POST</code> method. This is called as a result of
     * pressing the "Upload and begin" button from the rendered page.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");

        File tempFile1 = null, tempDirectory = null, tempFile2 = null;
        CloseableHttpClient httpclient = null;

        try (PrintWriter outPrintWriter3 = response.getWriter()) {
            StringBuilder sb = new StringBuilder();
            try {
                tempDirectory = makeTempDir();
                MultipartRequest mpr = new MultipartRequest(request, tempDirectory.getAbsolutePath(), 5 * 1024 * 1024);
                initHtmlPieces(request);  // breaks in two for "working" label
                outPrintWriter3.print(indexFileFirst);

                if (mpr.getParameter("method").equalsIgnoreCase("file")) 
                {
                    // A file (or files) has been uploaded with post method
                    String hiddenPath = mpr.getParameter("hiddenPath");

                    if ((hiddenPath != null) && !hiddenPath.isEmpty()) // here if a file was passed through the url parameters
                    {
                        doValidate(sb, hiddenPath, new File(hiddenPath));
                    } else { // here if the user chose one through the html file-input button
                        Enumeration<?> enumr = mpr.getFileNames();
                        while (enumr.hasMoreElements()) {
                            String fsName = mpr.getFilesystemName((String) enumr.nextElement());
                            if (fsName == null) {
                                sb.append("Must supply file\n");
                            } else {
                                tempFile1 = new File(tempDirectory, fsName);
                                sb.append("Local file ");
                                // note that local path on client machine is not available due to browser security restrictions
                                doValidate(sb, fsName, tempFile1);
                            }
                        }
                    }
                } 
                else 
                {
                    // A url has been specified via the submit button
                    originalUrl = mpr.getParameter("url");
                    trimmedUrl = originalUrl.trim();
                    if (!trimmedUrl.equals(originalUrl))
                    {
                        sb.append ("(trimmed whitespace from original url)");
                        sb.append("\n").append("\n");
                    }
                    sb.append("originalUrl=").append(originalUrl).append(" trimmedUrl=").append(trimmedUrl);
                    sb.append("\n").append("\n");
        
                    // https://jira.nps.edu/browse/IA-9713
                    String regexAllowedUrlExtensions    = "(x3d|X3D|xml|XML)";
                    String regexValidUrl                = "^https?:\\/\\/[\\w\\.\\-]+[\\w\\/\\-]*\\/[\\w\\-]+\\." + regexAllowedUrlExtensions + "$";
                    // https://stackoverflow.com/questions/24924072/website-url-validation-regex-in-java
            //      String regexValidUrl                = "^(http:\\/\\/|https:\\/\\/)?(www.)?([a-zA-Z0-9]+).[a-zA-Z0-9]*.[a-z]{3}.?([a-z]+)\\\\." + regexAllowedUrlExtensions + "$?$";
                    Pattern patternValidUrl = Pattern.compile(regexValidUrl);
                    Matcher matcherValidUrl = patternValidUrl.matcher(trimmedUrl);
                    boolean foundValidUrl = matcherValidUrl.find(); // find one (and only one) match
                    
                    if (!foundValidUrl) // clickbait check
                    {
                        String errorMessage = " url regular expression (regex) check found illegal url " + trimmedUrl;
                        // diagnostic if not matching regexAllowedUrlExtensions
                        if (!trimmedUrl.toLowerCase().endsWith(".x3d")  &&
                            !trimmedUrl.toLowerCase().endsWith(".xml")  &&
                            !trimmedUrl.toLowerCase().endsWith(".html") &&
                            !trimmedUrl.toLowerCase().endsWith(".xhtml"))
                            errorMessage += " (illegal file extension)";
                        throw  new MalformedURLException(errorMessage); // exit gracefully
                    }
// Tomcat allows resubmission of an updated model via that url, so do not block it
//                    else if (trimmedUrl.contains("validate/")) // warning, this is not very portable!!
//                    {
//                        String errorMessage = " url input not allowed via /validate subdirectory: " + trimmedUrl;
//                        throw  new MalformedURLException(errorMessage); // exit gracefully
//                    }
                    else
                    {
                        sb.append ("X3D model address regular expression (regex) check found a safe well-formed url ").append(trimmedUrl);
                        sb.append("\n").append("\n");
                    }
                    
                    int lastSlash;
                    if ((lastSlash = trimmedUrl.lastIndexOf("/")) != -1)
                    {
                        urlFilename = trimmedUrl.substring(lastSlash + 1, trimmedUrl.length());
                    //  sb.append("urlFilename=").append(urlFilename).append("\n").append("\n");
                    }
        
                    if (trimmedUrl.startsWith("http") && trimmedUrl.endsWith(".x3d"))
                    {
                        sb.append("X3D model address: ");
                    }
                    else if (trimmedUrl.endsWith(".x3d"))
                    {
                        sb.append("X3D model: ");
                    }
                    else if (trimmedUrl.startsWith("file"))
                    {
                        sb.append("File address: ");
                    }
                    else
                    {
                        sb.append("File: ");
                    }

                    httpclient = HttpClients.createDefault();
                    HttpGet httpget = new HttpGet(originalUrl); // originalUrl and not trimmedUrl due to Tomcat prefixing??
                    CloseableHttpResponse httpResponse = httpclient.execute(httpget);
                    int statusCode = httpResponse.getCode();
                    if (statusCode == HttpStatus.SC_OK)
                    {
                        HttpEntity entity = httpResponse.getEntity();
                        if (entity != null)
                        {
                            tempFile2 = copyFile(entity.getContent(), urlFilename);
                            doValidate(sb, trimmedUrl, tempFile2);
                        } else {
                            sb.append("empty retrieval from http request");
                        }
                    } else {
                        sb.append("retrieval attempt of ");
                    //  sb.append(simpleUrl);
                        sb.append("<a href='").append(originalUrl).append("'>").append(trimmedUrl).append("</a>");
                        sb.append(" returned http status code ");
                        sb.append(statusCode);
                        sb.append(" ");
                        sb.append(httpResponse.getReasonPhrase());
                    }
                }
                outPrintWriter3.println(sb.toString());
            } 
            catch (Exception ex) 
            {
                if (sb.length() > 0)
                {
                    outPrintWriter3.println(sb.append("\n").toString());
                }
                outPrintWriter3.println("*** Exception on server: " + ex.getClass().getSimpleName() + ": " + ex.getLocalizedMessage());
            }
            finally 
            {
                if (tempFile1 != null) {
                    cleanUpTempFile(tempFile1);
                }
                if (tempFile2 != null) {
                    cleanUpTempFile(tempFile2);
                }
                if (tempDirectory != null && tempDirectory.exists()) {
                    tempDirectory.delete();
                }
                if (httpclient != null) {
                    httpclient.close();
                }
                outPrintWriter3.println(indexFileSecond);
                outPrintWriter3.flush();
            }
//            outPrintWriter3.println(indexFileSecond);
//            outPrintWriter3.flush();
        }
    }

    private void doValidate(StringBuilder sb, String simpleFileName, File f) throws Exception
    {
        if (simpleFileName.toLowerCase().endsWith(".x3d")) {
            sb.append("X3D model file name: ");
        } 
        else if (simpleFileName.toLowerCase().endsWith(".html")) {
            sb.append("HTML file name: ");
        }
        else if (simpleFileName.toLowerCase().endsWith(".xhtml")) {
            sb.append("XHTML file name: ");
        }
        else {
            sb.append("File name: ");
        }
        sb.append("<b>");
        sb.append(simpleFileName);
        sb.append("</b>  ");

        sb.append("(length: ");
        sb.append(f.length());
        sb.append(" bytes)");
        // confirm url local or online
        if (trimmedUrl.contains(simpleFileName))
            sb.append(" (<a href='").append("https://savage.nps.edu/X3dValidator").append("?url=").append(trimmedUrl).append("'>").append("revalidation address").append("</a>)").append("\n");
        sb.append(Validator.validate(f));
    }

    private File copyFile(InputStream inStr, String filename) throws IOException {
        File tempDir = File.createTempFile(TEMP_PREFIX, null);
        tempDir.delete();
        tempDir = new File(tempDir.getAbsolutePath());
        tempDir.mkdir();
        tempDir.deleteOnExit();
        File tempF = new File(tempDir, filename);  // do this so the file name is right
        tempF.deleteOnExit();

        try (OutputStream fos = new FileOutputStream(tempF)) {
            byte[] buf = new byte[4096];
            int count;
            while ((count = inStr.read(buf)) != -1) {
                fos.write(buf, 0, count);
            }
            
            inStr.close();
        }
        return tempF;
    }

    private void cleanUpTempFile(File f) {
        if (f.isFile()) {
            if (f.getParentFile().getName().startsWith(TEMP_PREFIX)) {
                File parent = f.getParentFile();
                if (!f.delete()) {
                    System.out.println("bp");
                }
                if (!parent.delete()) {
                    System.out.println("bp1");
                }
            }
        }
    }

    private File makeTempDir() throws IOException {
        final File temp;

        temp = File.createTempFile(TEMP_PREFIX, Long.toString(System.nanoTime()));

        if (!(temp.delete())) {
            throw new IOException("Could not delete temp file: " + temp.getAbsolutePath());
        }

        if (!(temp.mkdir())) {
            throw new IOException("Could not create temp directory: " + temp.getAbsolutePath());
        }
        return temp;
    }

    @Override
    public String getServletInfo() {
        return "A servlet to accept an X3dFile by post or url, and apply a suite of tests on it.";
    }
}
