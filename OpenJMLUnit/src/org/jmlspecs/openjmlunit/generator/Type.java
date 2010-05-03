
package org.jmlspecs.openjmlunit.generator;

public class Type {

  /**
   * The fully qualified name of this class.
   */
  protected final String my_name;
  /**
   * The unqualified name of the class.
   */
  protected final String my_short_name;

  //@ invariant my_short_name.equals(my_name.substring(my_name.lastIndexOf('.') + 1));
  /**
   * Create a new Type with the given fully qualified name.
   * @param the_name The fully qualified name of the type.
   */
  public Type(final String the_name) {
    my_name = the_name;
    my_short_name = the_name.substring(the_name.lastIndexOf('.') + 1);
  }

  /**
   * Returns the unqualified name of the class.
   * 
   * @return The name of the class
   */
  public String getShortName() {
    return my_short_name;
  }

  /**
   * Returns the fully qualified name of the class.
   * 
   * @return The name of the class
   */
  public String getFullyQualifiedName() {
    return my_name;
  }
  
  /**
   * Returns the fully qualified name of the type with '.' characters replaced by '_'.
   * @return Fully qualified name of the type with '.' characters replaced by '_'.
   */
  public String getFormattedName() {
    return my_name.replace('.', '_');
  }

  /**
   * Returns the package name of the class.
   * 
   * @return The package name of the class
   */
  public String getPackageName() {
    if (my_name.length() > my_short_name.length()) {
      return my_name.substring(0, my_name.length() - my_short_name.length() - 1);
    } else {
      return "";
    }
  }

  
  /**
   * Compares with object for equality. To ClassInfo objects are equal if they have the same
   * fully qualified names.
   * @param the_o The object to compare.
   * @return true if qualified names are equal. false otherwise.
   */
  public boolean equals(final Object the_o) {
    if (the_o != null && the_o instanceof Type) {
      return ((Type) the_o).my_name.equals(my_name);
    } else {
      return false;
    }
  }
  /**
   * Returns a hash of this object.
   * @return The hash code of this object.
   */
  public int hashCode() {
    return my_name.hashCode();
  }
}
