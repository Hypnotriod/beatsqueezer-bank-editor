/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hypnotriod.beatsqueezereditor.tools;

/**
 *
 * @author ipikin
 */
public class FileName {
  private String fullPath;
  private char pathSeparator, extensionSeparator;

  public FileName(String str, char sep, char ext) {
    fullPath = str;
    pathSeparator = sep;
    extensionSeparator = ext;
  }

  public String extension() {
    int dot = fullPath.lastIndexOf(extensionSeparator);
    return fullPath.substring(dot + 1);
  }

  public String filename() { // gets filename without extension
    int dot = fullPath.lastIndexOf(extensionSeparator);
    int sep = fullPath.lastIndexOf(pathSeparator);
    return fullPath.substring(sep + 1, dot);
  }

  public String path() {
    int sep = fullPath.lastIndexOf(pathSeparator);
    return fullPath.substring(0, sep);
  }
}
