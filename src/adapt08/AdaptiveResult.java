/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package adapt08;

/**
 *
 * @author CPU11516-local
 */
//This class is used to encapsulate the adaptive results
// into an object for return to the calling method.
class AdaptiveResult{
  public double[] filterArray;
  public double output;
  public double err;
  
  //Constructor
  public AdaptiveResult(double[] filterArray,
                        double output,
                        double err){
    this.filterArray = filterArray;
    this.output = output;
    this.err = err;
  }//end constructor
}//end class AdaptiveResult
