// Dear Dylan: Your dad totally figured out the following magical commands:
//   To compile: javac -classpath .:classes:commons-math3-3.6.1/'*' testyboi.java
//   To run: java -classpath .:classes:commons-math3-3.6.1/'*' testyboi
// So there!

import java.util.*;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;

public class testyboi {
    public static void main(String[] args) {
	double[][] xdata = {{0,0}, {0,1}, {1,0}};
	double[] ydata = {1,2,2};
	
	OLSMultipleLinearRegression reg = new OLSMultipleLinearRegression();
	reg.newSampleData(ydata, xdata);
	System.out.println(reg.calculateResidualSumOfSquares());
	
    }
}


