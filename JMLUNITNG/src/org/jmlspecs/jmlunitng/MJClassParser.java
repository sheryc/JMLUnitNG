
package org.jmlspecs.jmlunitng;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import org.multijava.mjc.JCompilationUnitType;
import org.multijava.mjc.JavadocLexer;
import org.multijava.mjc.MjcCommonOptions;
import org.multijava.mjc.MjcLexer;
import org.multijava.mjc.MjcParser;
import org.multijava.mjc.ParsingController;
import org.multijava.mjc.ParsingController.ConfigurationException;
import antlr.RecognitionException;
import antlr.TokenStreamException;

/**
 * This class parses the given class in the AST format using MultiJava compiler.
 * 
 * @author Rinkesh Nagmoti
 * @version 1.0
 */
public class MJClassParser extends org.multijava.mjc.Main
{
  /**
   * MjcCommonOptions for getting the required class parsed.
   */
  protected final transient MjcCommonOptions my_opt;

  /**
   * File to be parsed.
   */
  protected final transient File my_file;

  /**
   * Constructs the MJClassParser object.
   * 
   * @param the_file the file to be parsed.
   * @param the_options the MjcCommonOptions object to parse the file.
   */
  public MJClassParser(final File the_file, final MjcCommonOptions the_options)
  {
    super();
    my_opt = the_options;
    my_file = the_file;
  }

  /**
   * This method parses the given class file in AST format.
   * 
   * @return JCompilationUnitType
   * @throws FileNotFoundException  Exception if unable to find file.
   * @throws ConfigurationException Exception for issues related to configuration.
   * @throws RecognitionException Exception for issues related to recognition.
   * @throws TokenStreamException Exception for issues related to TokenStream.
   */
  public JCompilationUnitType parse() throws FileNotFoundException, ConfigurationException,
      RecognitionException, TokenStreamException
  {
    final BufferedReader buffer = new BufferedReader(new FileReader(my_file));
    MjcLexer mjclexer;
    JavadocLexer jdoclexer;
    MjcParser my_parser;
    JCompilationUnitType j_cunit;
    ParsingController controller;

    controller = new ParsingController(buffer, my_file);
    mjclexer =
        new MjcLexer(controller, my_opt.source().equals("1.5"), my_opt.multijava(),
                     allowUniverseKeywords, this);
    jdoclexer = new JavadocLexer(controller);

    controller.addInputStream(mjclexer, "multijava");
    controller.addInputStream(jdoclexer, "javadoc");
    controller.selectInitial("multijava");

    setAllowUniverses();

    my_parser =
        new MjcParser(this, controller.initialOutputStream(), controller, my_opt.generic(),
                      my_opt.multijava(), my_opt.relaxed(), allowUniverseKeywords,
                      parseJavadoc);

    j_cunit = my_parser.jCompilationUnit();
    return j_cunit;
  }

  /**
   * This method sets the parameters for parsing the class.
   */
  private void setAllowUniverses()
  {
    this.allowUniverseKeywords = true;
    this.allowUniverseChecks = true;
    this.allowUniversePurity = true;
    this.allowUniverseDynChecks = true;
    this.allowUniverseBytecode = true;
    this.allowUniverseAnnotations = false;
  }

}
