package gdacarv;

import java.util.Arrays;

import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.data.DataSet;
import org.neuroph.nnet.learning.BackPropagation;

public class NeuralNetworkDataItem {

	public NeuralNetwork<BackPropagation> network;
	
	public double[][] dataBuffer;
	
	public int bufferIndex = 0;
	
	public DataSet dataSet;

	public NeuralNetworkDataItem(double[][] dataBuffer, DataSet dataSet) {
		super();
		this.dataBuffer = dataBuffer;
		this.dataSet = dataSet;
	}

	public void clear() {
		bufferIndex = 0;
		for(double[] arr : dataBuffer)
			Arrays.fill(arr, 0);
	}

}
