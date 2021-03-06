package com.github.cambridgeAlphaTeam;

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;

import java.util.Map;
import java.util.HashMap;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;

import com.sun.net.httpserver.*;

/**
 * Serves files from a given directory.
 * @author Kovacsics Robert &lt;rmk35@cam.ac.uk&gt;
 */

public class FileHandler implements HttpHandler {
  /* Where to serve files from */
  private final String servePath;
  /* The path for 404.html */
  private final File notFoundFile;
  /* Buffer size when reading in files */
  private static final int bufferSize = 10*1024;

  static final Map<String, String> contentTypes;
  static {
    contentTypes = new HashMap<>();
    contentTypes.put(".css", "text/css");
    contentTypes.put(".html", "text/html");
    contentTypes.put(".js", "text/javascript");
  }

  public FileHandler() {
    servePath = ".";
    notFoundFile = null;
  }

  public FileHandler(String servePath) {
    this.servePath = servePath;
    notFoundFile = null;
  }

  public FileHandler(String servePath, String errorPath) {
    this.servePath = servePath;
    this.notFoundFile = new File(errorPath);
  }

  public void handle(HttpExchange t) throws IOException {
    try {
      String cwd = new File(servePath).getCanonicalPath();
      /* Always absolute, that is starts with "/" */
      String reqPath = t.getRequestURI().getPath();
      File requestedFile = new File(cwd + reqPath);


      if (requestedFile.exists()) {
        if (requestedFile.isFile()) {
          try {
            String extension = reqPath.substring(reqPath.lastIndexOf("."));
            if (null != contentTypes.get(extension)) {
              t.getResponseHeaders().add("Content-Type", contentTypes.get(extension));
            } else {
              t.getResponseHeaders().add("Content-Type", "application/octet-stream");
            }
          } catch (IndexOutOfBoundsException e) {
            t.getResponseHeaders().add("Content-Type", "application/octet-stream");
          }

          t.sendResponseHeaders(200, requestedFile.length());
          OutputStream os = t.getResponseBody();
          writeFile(requestedFile, os);
        } else if (requestedFile.isDirectory()) {
          t.getResponseHeaders().add("Content-Type", contentTypes.get(".html"));
          File greeter = new File(requestedFile, "index.html");
          if (greeter.isFile()) {
            t.sendResponseHeaders(200, greeter.length());
            OutputStream os = t.getResponseBody();
            writeFile(greeter, os);
          }
        }
      } else {

        /* If we had no errors, we should not get this far. */
        if (null != notFoundFile && notFoundFile.exists()) {
          try {
            String extension = notFoundFile.getPath().substring(reqPath.lastIndexOf("."));
            if (null != contentTypes.get(extension)) {
              t.getResponseHeaders().add("Content-Type", contentTypes.get(extension));
            } else {
              t.getResponseHeaders().add("Content-Type", "application/octet-stream");
            }
          } catch (IndexOutOfBoundsException e) {
            t.getResponseHeaders().add("Content-Type", "application/octet-stream");
          }

          t.sendResponseHeaders(404, notFoundFile.length());
          OutputStream os = t.getResponseBody();
          writeFile(notFoundFile, os);
        } else {
          t.getResponseHeaders().add("Content-Type", "text/plain");
          byte[] response = "Error 404: File not found".getBytes();
          t.sendResponseHeaders(404, response.length);
          OutputStream os = t.getResponseBody();
          os.write(response);
        }
      }
    } finally {
      t.close();
    }
  }

  /**
   * Writes a file into OutputStream os, then LEAVES os OPEN.
   */
  private void writeFile(File f, OutputStream os) throws IOException {
    InputStream fis = new FileInputStream(f);

    byte[] buffer = new byte[bufferSize];
    int n;
    while ((n = fis.read(buffer)) > 0) {
      os.write(buffer, 0, n);
    }

    fis.close();
  }
}
