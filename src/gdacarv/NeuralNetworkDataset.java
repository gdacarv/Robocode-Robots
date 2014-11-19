package gdacarv;

import org.neuroph.core.data.DataSet;

public class NeuralNetworkDataset {

	public static final DataSet firePowerTraining;
	
	static{
		firePowerTraining = new DataSet(3, 1);
		// I: Enemy Distance, Enemy Energy, My Energy | O: Shot Fire Power
		firePowerTraining.addRow(new double[]{0.030, 1.00, 1.00}, new double[]{0.99});
		firePowerTraining.addRow(new double[]{0.040, 0.90, 0.85}, new double[]{0.99});
		firePowerTraining.addRow(new double[]{0.050, 0.85, 0.40}, new double[]{0.99});
		firePowerTraining.addRow(new double[]{0.060, 0.70, 0.90}, new double[]{0.99});
		firePowerTraining.addRow(new double[]{0.050, 0.25, 0.88}, new double[]{0.99});
		firePowerTraining.addRow(new double[]{0.045, 0.24, 0.70}, new double[]{0.99});
		firePowerTraining.addRow(new double[]{0.050, 0.22, 0.50}, new double[]{0.99});
		firePowerTraining.addRow(new double[]{0.050, 0.20, 0.25}, new double[]{0.99});
		firePowerTraining.addRow(new double[]{0.050, 0.10, 0.06}, new double[]{0.99});

		firePowerTraining.addRow(new double[]{0.400, 0.07, 1.00}, new double[]{0.99});
		firePowerTraining.addRow(new double[]{0.300, 0.16, 0.70}, new double[]{0.99});

		firePowerTraining.addRow(new double[]{0.300, 1.00, 0.95}, new double[]{0.66});
		firePowerTraining.addRow(new double[]{0.350, 0.73, 0.99}, new double[]{0.66});
		firePowerTraining.addRow(new double[]{0.350, 0.70, 0.70}, new double[]{0.66});
		firePowerTraining.addRow(new double[]{0.400, 0.92, 0.50}, new double[]{0.66});
		firePowerTraining.addRow(new double[]{0.500, 0.50, 0.91}, new double[]{0.66});
		firePowerTraining.addRow(new double[]{0.550, 0.20, 0.89}, new double[]{0.66});

		firePowerTraining.addRow(new double[]{0.600, 0.99, 0.99}, new double[]{0.33});
		firePowerTraining.addRow(new double[]{0.700, 0.55, 0.76}, new double[]{0.33});
		firePowerTraining.addRow(new double[]{0.700, 0.50, 0.30}, new double[]{0.33});
		firePowerTraining.addRow(new double[]{0.700, 0.45, 0.50}, new double[]{0.33});
		firePowerTraining.addRow(new double[]{0.750, 0.25, 0.30}, new double[]{0.33});
	}
}
