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
import static java.lang.Math.*;//J2SE 5.0 req

class Adapt08{
  public static void main(String[] args){
    //Default parameter values
    double feedbackGain = 0.0001;
    int numberIterations = 2001;
    int filterLength = 26;//Must be >= 26 for plotting.
    double noiseScale = 10;
    double signalScale = 10;
    
    //Define the path impulse response as a simulation of
    // an acoustic signal with time-delayed echoes.
    double[] pathOperator = {0.0,
                             0.0,
                             0.0,
                             1.0,
                             0.0,
                             0.64,
                             0.0,
                             0.0,
                             0.32768,
                             0.0,
                             0.0,
                             0.0,
                             0.1342176,
                             0.0,
                             0.0,
                             0.0,
                             0.0,
                             0.0439803,
                             0.0};

    //Instantiate a new object of the Adapt08 class
    // and invoke the method named process on that object.
    new Adapt08().process(feedbackGain,
                          numberIterations,
                          filterLength,
                          noiseScale,
                          signalScale,
                          pathOperator);
  }//end main
  //-----------------------------------------------------//
  
  //This is the primary adaptive processing and plotting
  // method for the program.
  void process(double feedbackGain,
               int numberIterations,
               int filterLength,
               double noiseScale,
               double signalScale,
               double[] pathOperator){

    //The following array will be populated with the
    // adaptive filter for display purposes.
    double[] filter = null;

    //Display the pathOperator
    //First instantiate a plotting object.
    PlotALot01 pathOperatorObj = new PlotALot01("Path",
               (pathOperator.length * 4) + 8,148,70,4,0,0);

    //Feed the data to the plotting object.
    for(int cnt = 0;cnt < pathOperator.length;cnt++){
      pathOperatorObj.feedData(40*pathOperator[cnt]);
    }//end for loop
    
    //Cause the graph to be displayed on the computer
    // screen in the upper left corner.
    pathOperatorObj.plotData(0,0);
    
    //Now compute and plot the frequency response of the
    // path
    
    //Instantiate a plotting object for two channels of
    // frequency response data.  One channel is for
    // the amplitude and the other channel is the phase.
    PlotALot03 pathFreqPlotObj = 
                   new PlotALot03("Path",264,148,35,2,0,0);
                   
    //Compute the frequency response and feed the results
    // to the plotting object.
    displayFreqResponse(pathOperator,pathFreqPlotObj,
                                                    128,0);
                       
    //Cause the frequency response data stored in the
    // plotting object to be displayed on the screen in
    // the top row of images.
    pathFreqPlotObj.plotData(112,0);
    
    //Instantiate an object to handle the adaptive behavior
    // of the program.
    AdaptEngine02 adapter = new AdaptEngine02(
                                filterLength,feedbackGain);

    //Instantiate an array object that will be used as a
    // delay line for the white noise data.
    double[] rawData = new double[pathOperator.length];
    
    //Instantiate a plotting object for six channels of
    // time-series data.
    PlotALot07 timePlotObj = 
                   new PlotALot07("Time",468,198,25,2,0,0);

    //Instantiate a plotting object for two channels of
    // filter frequency response data.  One channel is for
    // the amplitude and the other channel is for the
    // phase.
    PlotALot03 freqPlotObj = 
                   new PlotALot03("Freq",264,487,35,2,0,0);

    //Instantiate a plotting object to display the filter
    // impulse response at specific time intervals during
    // the adaptive process.
    PlotALot01 filterPlotObj = new PlotALot01("Filter",
                     (filterLength * 4) + 8,487,70,4,0,0);

    //Declare and initialize working variables.
    double output = 0;
    double err = 0;
    double target = 0;
    double input = 0;
    double whiteNoise = 0;
    double whiteSignal = 0;
    boolean adaptOn;

    //Perform the specified number of iterations.
    for(int cnt = 0;cnt < numberIterations;cnt++){
      
      //The following variable is used to control whether
      // or not the adapt method of the adaptive engine
      // updates the filter coefficients when it is called.
      // The filters are updated when this variable is
      // true and are not updated when this variable is
      // false.
      adaptOn = true;
      
      //Get and scale the next sample of white noise and
      // white signal data from a random number generator.
      // Before scaling by noiseScale and signalScale, the
      // values are uniformly distributed from -1.0 to 1.0.
      whiteNoise = noiseScale*(2*(Math.random() - 0.5));
      whiteSignal = signalScale*(2*(Math.random() - 0.5));
      
      //Set white noise and white signal values near the
      // end of the run to zero and turn adaptation off.
      // Insert an impulse near the end of the whiteNoise
      // data that will produce impulse responses for the
      // path and for the adaptive filter.
      //Set values to zero.
      if(cnt > (numberIterations - 5*filterLength)){
        whiteNoise = 0;
        whiteSignal = 0;
        adaptOn = false;
      }//end if
      //Now insert an impulse at one specific location in
      // the whiteNoise data.
      if(cnt == numberIterations - 3*filterLength){
        whiteNoise = 2 * noiseScale;
      }//end if

      //Insert the white noise data into the delay line
      // that will be used to convolve the white noise
      // with the path operator.
      flowLine(rawData,whiteNoise);
    
      //Apply the path operator to the white noise data.
      double pathNoise = 
                   reverseDotProduct(rawData,pathOperator);

      //Declare a variable that will be populated with the
      // results returned by the adapt method of the
      // adaptive engine.
      AdaptiveResult result = null;
      
      //Establish the appropriate input values for an
      // adaptive noise cancellation filter and perform the
      // adaptive update.
      input = whiteNoise;
      target = pathNoise + whiteSignal;
      result = adapter.adapt(input,target,adaptOn);

      //Get and save adaptive results for plotting and
      // spectral analysis
      output = result.output;
      err = result.err;
      filter = result.filterArray;
    
      //Feed the time series data to the plotting object.
      timePlotObj.feedData(input,target,output,-err,
                            whiteSignal,err + whiteSignal);
    
      //Compute and plot summary results at the end of
      // every 400th iteration.
      if(cnt%400 == 0){
        displayFreqResponse(filter,freqPlotObj,
                                    128,filter.length - 1);

        //Display the filter impulse response coefficient
        // values.  The adaptive engine returns the filter
        // with the time axis reversed relative to the
        // conventional display of an impulse response.
        // Therefore, it is necessary to display it in
        // reverse order.
        for(int ctr = 0;ctr < filter.length;ctr++){
          filterPlotObj.feedData(
                       40*filter[filter.length - 1 - ctr]);
        }//end for loop
      }//End display of frequency response and filter
    }//End for loop,
    
    //Cause the data stored in the plotting objects to be
    // plotted.
    timePlotObj.plotData(376,0);//Top of screen
    freqPlotObj.plotData(0,148);//Left side of screen
    filterPlotObj.plotData(265,148);

  }//end process method
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
 
  void displayFreqResponse(
     double[] filter,PlotALot03 plot,int len,int zeroTime){

    //Create the arrays required by the Fourier Transform.
    double[] timeDataIn = new double[len];
    double[] realSpect = new double[len];
    double[] imagSpect = new double[len];
    double[] angle = new double[len];
    double[] magnitude = new double[len];
    
    //Copy the filter into the timeDataIn array
    System.arraycopy(filter,0,timeDataIn,0,filter.length);

    //Compute DFT of the filter from zero to the folding
    // frequency and save it in the output arrays.
    ForwardRealToComplex01.transform(timeDataIn,
                                     realSpect,
                                     imagSpect,
                                     angle,
                                     magnitude,
                                     zeroTime,
                                     0.0,
                                     0.5);

    //Note that the conversion to decibels has been
    // disabled.  You can re-enable the conversion by
    // removing the comment indicators.
/*    
    //Display the magnitude data. Convert to normalized
    // decibels first.
    //Eliminate or change any values that are incompatible
    // with log10 method.
    for(int cnt = 0;cnt < magnitude.length;cnt++){
      if((magnitude[cnt] == Double.NaN) || 
                                    (magnitude[cnt] <= 0)){
        //Replace the magnitude by a very small positive
        // value.
        magnitude[cnt] = 0.0000001;
      }else if(magnitude[cnt] == Double.POSITIVE_INFINITY){
        //Replace the magnitude by a very large positive
        // value.
        magnitude[cnt] = 9999999999.0;
      }//end else if
    }//end for loop
    
    //Now convert magnitude data to log base 10
    for(int cnt = 0;cnt < magnitude.length;cnt++){
      magnitude[cnt] = log10(magnitude[cnt]);
    }//end for loop
*/
    //Find the absolute peak value.  Begin with a negative
    // peak value with a large magnitude and replace it
    // with the largest magnitude value.
    double peak = -9999999999.0;
    for(int cnt = 0;cnt < magnitude.length;cnt++){
      if(peak < abs(magnitude[cnt])){
        peak = abs(magnitude[cnt]);
      }//end if
    }//end for loop

    //Normalize to 20 times the peak value
    for(int cnt = 0;cnt < magnitude.length;cnt++){
      magnitude[cnt] = 20*magnitude[cnt]/peak;
    }//end for loop

    //Now feed the normalized data to the plotting
    // object.
    for(int cnt = 0;cnt < magnitude.length;cnt++){
      plot.feedData(magnitude[cnt],angle[cnt]/20);
    }//end for loop
    
  }//end displayFreqResponse
  //-----------------------------------------------------//
  
  //This method receives two arrays and treats each array
  // as a vector. The two arrays must have the same length.
  // The program reverses the order of one of the vectors
  // and returns the vector dot product of the two vectors.
  double reverseDotProduct(double[] v1,double[] v2){
    if(v1.length != v2.length){
      System.out.println("reverseDotProduct");
      System.out.println("Vectors must be same length.");
      System.out.println("Terminating program");
      System.exit(0);
    }//end if
    
    double result = 0;
    
    for(int cnt = 0;cnt < v1.length;cnt++){
      result += v1[cnt] * v2[v1.length - cnt - 1];
    }//end for loop

    return result;
  }//end reverseDotProduct
  //-----------------------------------------------------//
}//end class Adapt08
