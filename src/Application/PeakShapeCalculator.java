package Application;
import java.io.*;
import java.lang.System;
import java.util.Scanner;

public class PeakShapeCalculator {

	public static void main(String[] args) {
		// Start by creating CalculatorSystem object
		
		CalculatorSystem cs = new CalculatorSystem();
		cs.readFile("settings.txt");
		Scanner in = new Scanner(System.in);
		
		System.out.println("Please enter the Protein injection volume in uL: ");
		cs.setInjectionParameter(0, in.nextDouble());
		System.out.println("Please enter the Injection delay min: ");
		cs.setInjectionParameter(2, in.nextDouble());
		System.out.println("Please enter the Injection repeat gap min: ");
		cs.setInjectionParameter(3, in.nextDouble());
		System.out.println("Please enter the D2O injection volume in uL: ");
		cs.setInjectionParameter(4, in.nextDouble());
		System.out.println("Please enter the Number of peaks: ");
		cs.setNumberOfPeaks(in.nextInt());
		
		cs.calculateInjectionParameters();
		
		cs.createTable();
		in.close();
	}

}


class CalculatorSystem {
	/*
	 * Key:
	 * 
	 * systemFlowSettings[0]: Protein Flow Rate uL/min
	 * systemFlowSettings[1]: D2O Flow Rate uL/min
	 * systemFlowSettings[2]: Acid Quench Flow Rate uL/min
	 * 
	 * proteinLineParameters[0]: Protein Transfer Line ID mm
	 * proteinLineParameters[1]: Transfer Line mm
	 * proteinLineParameters[2]: Transfer Line Volume uL
	 * proteinLineParameters[3]: Inner Capillary ID mm
	 * proteinLineParameters[4]: Inner Capillary mm
	 * proteinLineParameters[5]: Inner Capillary Volume uL
	 * proteinLineParameters[6]: Mixer Volume uL
	 * proteinLineParameters[7]: Post Quench Volume uL
	 * proteinLineParameters[8]: Delay Time min
	 * 
	 * d2OLineParameters[0]: D2O Transfer Line ID mm
	 * d2OLineParameters[1]: Transfer Line mm
	 * d2OLineParameters[2]: Transfer Line Volume uL
	 * d2OLineParameters[3]: Outer Capillary ID mm
	 * d2OLineParameters[4]: Outer Capillary Length mm
	 * d2OLineParameters[5]: Inner Capillary OD mm
	 * d2OLineParameters[6]: Outer Capillary VOlume uL
	 * d2OLineParameters[7]: Delay Time min
	 * 
	 * 
	 * injectionParameters[0]: Protein Injection Volume uL
	 * injectionParameters[1]: Protein MS Time min
	 * injectionParameters[2]: Injection Delay min
	 * injectionParameters[3]: Injection Repeat Gap min
	 * injectionParameters[4]: D2O Injection Volume uL
	 * injectionParameters[5]: D2O MS Time min
	 * 
	 */
	
	private double[] systemFlowSettings, proteinLineParameters, d2OLineParameters, injectionParameters;
	private int numberOfPeaks;
	
	public CalculatorSystem() {
		this.systemFlowSettings = new double[3];
		this.proteinLineParameters = new double[9];
		this.d2OLineParameters = new double[8];
		this.injectionParameters = new double[6];
	}
	
	
	public void readFile(String fileName) {
		Scanner sc;
		try {
			if(fileName == null) throw new FileNotFoundException();
			else {
				sc = new Scanner(new File(fileName));
				
				for(int i = 0; i < 3; i++) {
					this.systemFlowSettings[i] = sc.nextDouble();
				}
				
				for(int i = 0; i < 8; i++) {
					if(i != 2 && i != 5)
						this.proteinLineParameters[i] = sc.nextDouble();
				}
				
				this.proteinLineParameters[2] = Math.PI * ((this.proteinLineParameters[0] / 2) * (this.proteinLineParameters[0] / 2)) * this.proteinLineParameters[1];
				this.proteinLineParameters[5] = Math.PI * ((this.proteinLineParameters[3] / 2) * (this.proteinLineParameters[3] / 2)) * this.proteinLineParameters[4];

				
				for(int i = 0; i < 6; i++) {
					if(i != 2)
						this.d2OLineParameters[i] = sc.nextDouble();
				}
				
				this.d2OLineParameters[2] = Math.PI * ((this.d2OLineParameters[0] / 2) * (this.d2OLineParameters[0] / 2)) * this.d2OLineParameters[1];
				this.d2OLineParameters[6] = Math.PI * (((this.d2OLineParameters[3] / 2) * (this.d2OLineParameters[3] / 2)) - ((this.d2OLineParameters[5] / 2) * (this.d2OLineParameters[5] / 2)));
				this.d2OLineParameters[7] = ((this.d2OLineParameters[2] + this.d2OLineParameters[6]) / this.systemFlowSettings[1]) + (this.proteinLineParameters[6] + this.proteinLineParameters[7]) / (this.systemFlowSettings[0] + this.systemFlowSettings[1] + this.systemFlowSettings[2]);
				
				
				
				sc.close();
			}
			
		} catch (FileNotFoundException e) {
			System.out.println(e.getMessage());;
		}
	}
	
	public void createTable() {
		double nextTime;
		double proteinPeak = (this.systemFlowSettings[0] / (this.systemFlowSettings[0] + this.systemFlowSettings[1])) * 100.0;
		
		
		System.out.println("\nPeak Shape Table:");
		
		// Print D2O Table first
		System.out.printf("%10s %10s \n", "Time min", "D2O");
		System.out.printf("%10.2f %10d \n", 0.00, 0);
		nextTime = this.d2OLineParameters[7];
		System.out.printf("%10.2f %10d \n", nextTime - 0.2, 0);
		System.out.printf("%10.2f %10d \n", nextTime, 100);
		nextTime += (this.injectionParameters[4] / this.systemFlowSettings[1]);
		System.out.printf("%10.2f %10d \n", nextTime + 0.2, 100);
		System.out.printf("%10.2f %10d \n\n\n", nextTime + 0.5, 0);
		
		// Print Protein Table second
		System.out.printf("%10s %10s \n", "Time min", "Protein");
		System.out.printf("%10.2f %10d \n", 0.00, 0);
		
		nextTime = this.d2OLineParameters[7] + this.injectionParameters[2];
		for(int i = 0; i < this.numberOfPeaks; i++) {
			System.out.printf("%10.2f %10d \n", nextTime - 0.2, 0);
			System.out.printf("%10.2f %10.2f \n", nextTime, proteinPeak);
			nextTime += this.injectionParameters[0] / this.systemFlowSettings[0];
			System.out.printf("%10.2f %10.2f \n", nextTime, proteinPeak);
			System.out.printf("%10.2f %10d \n", nextTime + 0.2, 0);
			nextTime += this.injectionParameters[3];
		}
		System.out.printf("%10.2f %10d \n", nextTime + 0.5, 0);
		
	}
	
	public void setInjectionParameter(int index, double value) {
		this.injectionParameters[index] = value;
	}
	
	public void calculateInjectionParameters() {
		this.injectionParameters[1] = this.injectionParameters[0] / this.systemFlowSettings[0];
		this.injectionParameters[5] = this.injectionParameters[4] / this.systemFlowSettings[1];
	}
	
	public void setNumberOfPeaks(int value) {
		this.numberOfPeaks = value;
	}
	
}
