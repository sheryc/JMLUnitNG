
package org.jmlspecs.jmlunitng;

/**
 * An interface for defining constants.
 * 
 * @author Rinkesh Nagmoti
 * @version 1.0 Some of the code is borrowed from open source JMLUNIT interface
 *          org.jmlspecs.jmlunit.Constants
 */
public interface Constants extends org.multijava.mjc.Constants
{

  /** Post fix for the Test Class generated by JMLUnitNG. */
  String TEST_CLASS_NAME_POSTFIX = "_JMLUNITNG_Test";
  /** Post fix for the file name of the Test Class generated by JMLUnitNG. */
  String TEST_CLASS_FILE_NAME_POSTFIX = "_JMLUNITNG_Test.java";
  /** Post fix for the TestData Class generated by JMLUnitNG. */
  String TEST_DATA_NAME_POSTFIX = "_JMLUNITNG_Test_Data";
  /**
   * Post fix for the file name of the TestData Class generated by JMLUnitNG.
   */
  String TEST_DATA_FILE_NAME_POSTFIX = "_JMLUNITNG_Test_Data.java";
  /** Prefix for the name of Test Method generated. */
  String TEST_METHOD_NAME_PREFIX = /* Need to decide on this. */"";
  /** A Dot Java extension for the file names. */
  String DOT_JAVA = ".java";

  /** The name of the JUnit framework package. */
  String PKG_JUNIT = "junit.framework.";
  /** The name of the JML RAC runtime package. */
  String PKG_JMLRAC = "org.jmlspecs.jmlrac.runtime.";
  /** The name of the JML jmlunitng package. */
  String PKG_JMLUNITNG = "org.jmlspecs.jmlunitng.";
  /** The name of the JML jmlunitng strategies package. */
  String PKG_STRATEGIES = "org.jmlspecs.jmlunitng.strategies.";
  /**
   * This is the constants for indentation.
   */
  int ONE = 1;
  /**
   * This is the constants for indentation.
   */
  int TWO = 2;
  /**
   * This is the constants for indentation.
   */
  int FOUR = 4;
  /**
   * This is the constants for indentation.
   */
  int SIX = 6;
  /**
   * This is the constants for indentation.
   */
  int EIGHT = 8;
  /**
   * This is the constants for indentation.
   */
  int TEN = 10;

  /*
   * @ invariant PKG_JUNIT.length() > 0
   * 
   * @ ==> PKG_JUNIT.charAt(PKG_JUNIT.length()) == '.';
   * 
   * @
   */
  /*
   * @ invariant PKG_JMLRAC.length() > 0
   * 
   * @ ==> PKG_JMLRAC.charAt(PKG_JMLRAC.length()) == '.';
   * 
   * @
   */
  /*
   * @ invariant PKG_JMLUNITNG.length() > 0
   * 
   * @ ==> PKG_JMLUNITNG.charAt(PKG_JMLUNITNG.length()) == '.';
   * 
   * @
   */
  /*
   * @ invariant PKG_STRATEGIES.length() > 0
   * 
   * @ ==> PKG_STRATEGIES.charAt(PKG_JMLUNITNG.length()) == '.';
   * 
   * @
   */
}
