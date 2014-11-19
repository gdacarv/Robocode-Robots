package gdacarv;

import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.util.TransferFunctionType;

public class CreateNeuralFiles {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		MultiLayerPerceptron firePerceptron = new MultiLayerPerceptron(TransferFunctionType.TANH, 3, 6, 1);
		// learn the training set
		System.out.println("MultiLayerPerceptron training...");
		firePerceptron.learn(NeuralNetworkDataset.firePowerTraining);
		
		firePerceptron.save("firePerceptron.nnet");
		System.out.println("File saved.");
	}

}
