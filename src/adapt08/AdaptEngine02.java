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
class AdaptEngine02{
  
  double[] filterArray;//filter coefficients stored here
  double[] dataArray;//historical data is stored here
  double feedbackGain;//used in LMS adaptive algorithm
  
  //Constructor
  public AdaptEngine02(int filterLength,
                       double feedbackGain){
    //Construct the two arrays and save the feedback gain.
    filterArray = new double[filterLength];
    dataArray = new double[filterLength];
    this.feedbackGain = feedbackGain;
  }//end constructor
  //-----------------------------------------------------//
  
  //This method implements a classical LMS adaptive
  // algorithm to create and to apply a convolution filter.
  // The filter output, the error, and a reference to the
  // array containing the filter coefficients are 
  // encapsulated in an object of type AdaptiveResult and
  // returned to the calling method.  The adaptive update
  // process is disabled when the parameter named adaptOn
  // is false.
  AdaptiveResult adapt(double rawData,
                       double target,
                       boolean adaptOn){
                        
    //Insert the incoming data value into the data delay
    // line.
    flowLine(dataArray,rawData);
  
    //Apply the current filter coefficients to the data.
    double output = dotProduct(filterArray,dataArray);
    //Compute the error.
    double err = output - target;

    //Only update the coefficients when adaptOn is true.
    if(adaptOn){
      //Use the error to update the filter coefficients.
      for(int ctr = 0;ctr < filterArray.length;ctr++){
        filterArray[ctr] -= 
                           err*dataArray[ctr]*feedbackGain;
      }//end for loop.
    }//end if

    //Construct and return an object containing the filter
    // output, the error, and a reference to the array
    // object containing the current filter coefficients.
    return new AdaptiveResult(filterArray,output,err);
  }//end adapt
  //-----------------------------------------------------//
  
  //This method simulates a tapped delay line. It receives
  // a reference to an array and a value.  It discards the
  // value at index 0 of the array, moves all the other
  // values by one element toward 0, and inserts the new
  // value at the top of the array.
  void flowLine(double[] line,double val){
    for(int cnt = 0;cnt < (line.length - 1);cnt++){
      line[cnt] = line[cnt+1];
    }//end for loop
    line[line.length - 1] = val;
  }//end flowLine
  //-----------------------------------------------------//
  
  //This method receives two arrays and treats the first N 
  // elements in each of the two arrays as a pair of
  // vectors.  It computes and returns the vector dot
  // product of the two vectors.  If the length of one
  // array is greater than the length of the other array,
  // it considers the number of dimensions of the vectors
  // to be equal to the length of the smaller array.
  double dotProduct(double[] v1,double[] v2){
    double result = 0;
    if((v1.length) <= (v2.length)){
      for(int cnt = 0;cnt < v1.length;cnt++){
        result += v1[cnt]*v2[cnt];
      }//emd for loop
      return result;
    }else{
      for(int cnt = 0;cnt < v2.length;cnt++){
        result += v1[cnt]*v2[cnt];
      }//emd for loop
      return result;
    }//end else
  }//end dotProduct
  //-----------------------------------------------------//
}//end class AdaptEngine02