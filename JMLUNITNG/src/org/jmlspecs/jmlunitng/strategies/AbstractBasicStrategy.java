package org.jmlspecs.jmlunitng.strategies;
/**
 * This class creates a basic strategy for generating an iterator.
 * @author Rinkesh Nagmoti.
 * @version 1.0
 */
public abstract class AbstractBasicStrategy
{
  /**
   * This is the iterator generated by the strategy.
   */
  protected final transient ParameterIterator my_itr;
  /**
   * This is hte array of objects to create iterator.
   */
  protected final transient Object[] my_objects;
  /**
   * This is the constructor.
   */
  public AbstractBasicStrategy()
  {
    final int size = defaultData().length + addData().length;
  
    final Object[] def = defaultData();
    final Object[] added = addData();
    my_objects = new Object[size];
    for (int i = 0; i < size; i++)
    {
      if (i < defaultData().length)
      {
        my_objects[i] = def[i];
      }
      else
      {
       
        my_objects[i] = added[i - (defaultData().length)];
      }
    }
    my_itr = new ParameterIterator(my_objects);
  }
  /**
   * This method returns the created ParameterIterator.
   * @return ParameterIterator
   */
  public ParameterIterator iterator()
  {
    return my_itr;
  }
  /**
   * This method return the default data for iterator.
   * @return Object[]
   */
  public abstract Object[] defaultData();
 
  /**
   * This method return the user provided data for iterator.
   * @return Object[]
   */
  public abstract Object[] addData();
  

 

}
