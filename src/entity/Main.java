//package entity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class Main {

	/**
	 * main function
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			// IO Setup
			File csv = new File("Data.csv"); // create a data.csv file to store the data
			BufferedWriter bw = new BufferedWriter(new FileWriter(csv));
			// Set up the table header
			bw.write("GlobalTemperature" + "," + "BlackPopulation" + "," + "WhitePopulation" + "," + "GrayPopulation");
			bw.newLine();
			// Declare the daisyWorld which is a two-dimensional array
			Patch[][] daisyWorld = new Patch[Params.PATCH_SIZE][Params.PATCH_SIZE];
			double global_temperature = 0;
			int whiteAmount;
			int blackAmount;
			int grayAmount;

			// Initialize the daisyWorld
			for (int i = 0; i < Params.PATCH_SIZE; i++) {
				for (int j = 0; j < Params.PATCH_SIZE; j++) {
					daisyWorld[i][j] = new Patch(0);
				}
			}

			// Sow the seeds in a random way
			seed_randomly(daisyWorld, DaisyColor.BLACK);
			seed_randomly(daisyWorld, DaisyColor.WHITE);
			seed_randomly(daisyWorld, DaisyColor.GRAY);

			// Update the temperature in every patch
			for (int i = 0; i < Params.PATCH_SIZE; i++) {
				for (int j = 0; j < Params.PATCH_SIZE; j++) {
					daisyWorld[i][j].calc_temperature();
				}
			}

			// Calculate the initial global temperature
			global_temperature = average_Temperature(daisyWorld);

			// Write the first set of data into the data.csv file
			bw.write(0 + "," + global_temperature + "," + daisyAmount(daisyWorld, DaisyColor.BLACK) + ","
					+ daisyAmount(daisyWorld, DaisyColor.WHITE) + "," + daisyAmount(daisyWorld, DaisyColor.GRAY));
			bw.newLine();

			// Update rules, in alignment with the go procedure in the NetLogo code
			for (int tick = 0; tick < 100; tick++) {
				// Params.setSOLAR_LUMINOSITY(tick);
				for (int i = 0; i < Params.PATCH_SIZE; i++) {
					for (int j = 0; j < Params.PATCH_SIZE; j++) {
						daisyWorld[i][j].calc_temperature();
					}
				}
				// Diffuse the temperature to its neighbours
				diffusion(daisyWorld);
				// Check the survivability of daisies and reproduce
				checkSurvivability(daisyWorld);
				// Calculate the global temperature
				global_temperature = average_Temperature(daisyWorld);
				blackAmount = daisyAmount(daisyWorld, DaisyColor.BLACK);
				whiteAmount = daisyAmount(daisyWorld, DaisyColor.WHITE);
				grayAmount = daisyAmount(daisyWorld, DaisyColor.GRAY);
				// Write the new data in that tick into the data file
				bw.write(
						tick + 1 + "," + global_temperature + "," + blackAmount + "," + whiteAmount + "," + grayAmount);
				bw.newLine();
			}
			System.out.println("更新结束");
			bw.close();
		} catch (FileNotFoundException e) {
			// Capture the exceptions during creating the files
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Calculate the amount of the daisy in given color
	 * 
	 * @param daisyWorld
	 * @param color
	 * @return count
	 */
	private static int daisyAmount(Patch[][] daisyWorld, DaisyColor color) {
		int count = 0;
		for (int i = 0; i < Params.PATCH_SIZE; i++) {
			for (int j = 0; j < Params.PATCH_SIZE; j++) {
				Patch here = daisyWorld[i][j];
				if (here.hasDaisy()) {
					if (here.getDaisy_here().getColor() == color) {
						count++;
					}
				}
			}
		}
		return count;
	}

	/**
	 * Randomly choose predefined number of the whole patches to sow the daisies
	 * with specific color
	 * 
	 * @param daisyWorld
	 * @param color
	 */
	private static void seed_randomly(Patch[][] daisyWorld, DaisyColor color) {
		Random random = new Random();
		int i = 0;
		if (color == DaisyColor.BLACK) {
			while (i < Params.PATCH_SIZE * Params.PATCH_SIZE * Params.NUM_BLACKS) {
				int x = random.nextInt(Params.PATCH_SIZE);
				int y = random.nextInt(Params.PATCH_SIZE);
				if (!daisyWorld[x][y].hasDaisy()) {
					daisyWorld[x][y].setDaisy_here(
							new Daisy(color, random.nextInt(Params.MAX_AGE + 1), Params.ALBEDO_OF_BLACK));
					i++;
				} else {
					continue;
				}
			}
		} else if (color == DaisyColor.WHITE) {
			while (i < Params.PATCH_SIZE * Params.PATCH_SIZE * Params.NUM_WHITES) {
				int x = random.nextInt(Params.PATCH_SIZE);
				int y = random.nextInt(Params.PATCH_SIZE);
				if (!daisyWorld[x][y].hasDaisy()) {
					daisyWorld[x][y].setDaisy_here(
							new Daisy(color, random.nextInt(Params.MAX_AGE + 1), Params.ALBEDO_OF_WHITES));
					i++;
				} else {
					continue;
				}
			}
		} else if (color == DaisyColor.GRAY) {
			while (i < Params.PATCH_SIZE * Params.PATCH_SIZE * Params.NUM_GRAY) {
				int x = random.nextInt(Params.PATCH_SIZE);
				int y = random.nextInt(Params.PATCH_SIZE);
				if (!daisyWorld[x][y].hasDaisy()) {
					daisyWorld[x][y].setDaisy_here(
							new Daisy(color, random.nextInt(Params.MAX_AGE + 1), Params.ALBEDO_OF_GRAY));
					i++;
				} else {
					continue;
				}
			}
		}
	}

	/**
	 * Calculate the global temperature
	 * @param daisyWorld
	 * @return global_temperature
	 */
	private static double average_Temperature(Patch[][] daisyWorld) {
		double global_temperature;
		double amount = 0;
		for (int i = 0; i < Params.PATCH_SIZE; i++) {
			for (int j = 0; j < Params.PATCH_SIZE; j++) {
				amount = amount + daisyWorld[i][j].getTemperature();
			}
		}
		global_temperature = amount / (Params.PATCH_SIZE * Params.PATCH_SIZE);
		return global_temperature;

	}

	/**
	 * Patch procedure: Diffuse 50% of local temperature to its neighbours
	 * @param daisyWorld
	 */
	private static void diffusion(Patch[][] daisyWorld) {

		// Declare a two-dimensional matrix to store the diffused temperature of a patch
		double[][] diffusion = new double[Params.PATCH_SIZE][Params.PATCH_SIZE];
		for (int i = 0; i < Params.PATCH_SIZE; i++) {
			for (int j = 0; j < Params.PATCH_SIZE; j++) {
				// Diffuse half of the temperature to 8 neighbours, each neighbour get 1/16
				diffusion[i][j] = (daisyWorld[i][j].getTemperature()) / 16;
			}
		}

		// Patch absorbs the temperature from its neighbours
		for (int i = 0; i < Params.PATCH_SIZE; i++) {
			for (int j = 0; j < Params.PATCH_SIZE; j++) {
				double current_temp = daisyWorld[i][j].getTemperature();
				double final_temp;
				double absorbed_temp;

				// top left corner
				if (i == 0 && j == 0) {
					absorbed_temp = diffusion[0][1] + diffusion[1][0] + diffusion[1][1];
					final_temp = absorbed_temp + current_temp * (13 / 16);
					daisyWorld[i][j].setTemperature(final_temp);
					continue;
				}
				// top right corner
				if (i == 0 && j == Params.PATCH_SIZE - 1) {
					absorbed_temp = diffusion[i][j - 1] + diffusion[i + 1][j - 1] + diffusion[i + 1][j];
					final_temp = absorbed_temp + current_temp * (13 / 16);
					daisyWorld[i][j].setTemperature(final_temp);
					continue;
				}
				// bottom left corner
				if (i == Params.PATCH_SIZE - 1 && j == 0) {
					absorbed_temp = diffusion[i - 1][j] + diffusion[i - 1][j + 1] + diffusion[i][j + 1];
					final_temp = absorbed_temp + current_temp * (13 / 16);
					daisyWorld[i][j].setTemperature(final_temp);
					continue;
				}
				// bottom right corner
				if (i == Params.PATCH_SIZE - 1 && j == Params.PATCH_SIZE - 1) {
					absorbed_temp = diffusion[i][j - 1] + diffusion[i - 1][j] + diffusion[i - 1][j - 1];
					final_temp = absorbed_temp + current_temp * (13 / 16);
					daisyWorld[i][j].setTemperature(final_temp);
					continue;
				}
				// top line 
				if (i == 0 && 0 < j && j < Params.PATCH_SIZE - 1) {
					absorbed_temp = diffusion[i][j - 1] + diffusion[i][j + 1] + diffusion[i + 1][j - 1]
							+ diffusion[i + 1][j] + diffusion[i + 1][j + 1];
					final_temp = absorbed_temp + current_temp * (11 / 16);
					daisyWorld[i][j].setTemperature(final_temp);
					continue;
				}
				// bottom line
				if (i == Params.PATCH_SIZE - 1 && 0 < j && j < Params.PATCH_SIZE - 1) {
					absorbed_temp = diffusion[i][j - 1] + diffusion[i][j + 1] + diffusion[i - 1][j - 1]
							+ diffusion[i - 1][j] + diffusion[i - 1][j + 1];
					final_temp = absorbed_temp + current_temp * (11 / 16);
					daisyWorld[i][j].setTemperature(final_temp);
					continue;
				}
				// left column
				if (j == 0 && 0 < i && i < Params.PATCH_SIZE - 1) {
					absorbed_temp = diffusion[i - 1][j] + diffusion[i + 1][j] + diffusion[i - 1][j + 1]
							+ diffusion[i][j + 1] + diffusion[i + 1][j + 1];
					final_temp = absorbed_temp + current_temp * (11 / 16);
					daisyWorld[i][j].setTemperature(final_temp);
					continue;
				}
				// right column
				if (j == Params.PATCH_SIZE - 1 && 0 < i && i < Params.PATCH_SIZE - 1) {
					absorbed_temp = diffusion[i][j - 1] + diffusion[i - 1][j - 1] + diffusion[i + 1][j - 1]
							+ diffusion[i - 1][j] + diffusion[i + 1][j];
					final_temp = absorbed_temp + current_temp * (11 / 16);
					daisyWorld[i][j].setTemperature(final_temp);
					continue;
				}
				// Medium points
				if (0 < i && i < Params.PATCH_SIZE - 1 && 0 < j && j < Params.PATCH_SIZE - 1) {
					absorbed_temp = diffusion[i - 1][j - 1] + diffusion[i - 1][j] + diffusion[i - 1][j + 1]
							+ diffusion[i][j - 1] + diffusion[i][j + 1] + diffusion[i + 1][j - 1] + diffusion[i + 1][j]
							+ diffusion[i + 1][j + 1];
					final_temp = absorbed_temp + current_temp / 2;
					daisyWorld[i][j].setTemperature(final_temp);
					continue;
				}
			}
		}
	}

	/**
	 * Daisy procedure: according to the quadratic function, daisy has different possibility to 
	 * reproduce in one of the empty patch around it. When randomly picking up the empty patch,
	 * use specific integer to represent the neighbours' position.
	 * @param daisyWorld
	 * @param i
	 * @param j
	 */
	private static void reproduce(Patch[][] daisyWorld, int i, int j) {
		// Declare an arrayList to store the empty patches in the neighbourhood
		ArrayList<Integer> temp = new ArrayList<Integer>();
		Random random = new Random();
		Patch here = daisyWorld[i][j];
		// Save the color and albedo of the current patch
		DaisyColor color = here.getDaisy_here().getColor();
		double albedo = 0;
		if (color == DaisyColor.WHITE) {
			albedo = Params.ALBEDO_OF_WHITES;
		} else if (color == DaisyColor.BLACK) {
			albedo = Params.ALBEDO_OF_BLACK;
		} else if (color == DaisyColor.GRAY) {
			albedo = Params.ALBEDO_OF_GRAY;
		}
		
		// top left corner
		if (i == 0 && j == 0) {
			if (!daisyWorld[i][j + 1].hasDaisy()) {
				temp.add(5);
			}
			if (!daisyWorld[i + 1][j].hasDaisy()) {
				temp.add(7);
			}
			if (!daisyWorld[i + 1][j + 1].hasDaisy()) {
				temp.add(8);
			}
			// Randomly choosing an empty patch and reproduce
			if (temp.size() > 0) {
				int selected = random.nextInt(temp.size());
				switch (temp.get(selected)) {
				case 5:
					daisyWorld[i][j + 1].setDaisy_here(new Daisy(color, 0, albedo));
					break;
				case 7:
					daisyWorld[i + 1][j].setDaisy_here(new Daisy(color, 0, albedo));
					break;
				case 8:
					daisyWorld[i + 1][j + 1].setDaisy_here(new Daisy(color, 0, albedo));
					break;
				}
			}
		}
		// top right corner
		if (i == 0 && j == Params.PATCH_SIZE - 1) {
			if (!daisyWorld[i][j - 1].hasDaisy()) {
				temp.add(4);
			}
			if (!daisyWorld[i + 1][j - 1].hasDaisy()) {
				temp.add(6);
			}
			if (!daisyWorld[i + 1][j].hasDaisy()) {
				temp.add(7);
			}
			// Randomly choosing an empty patch and reproduce
			if (temp.size() > 0) {
				int selected = random.nextInt(temp.size());
				switch (temp.get(selected)) {
				case 4:
					daisyWorld[i][j - 1].setDaisy_here(new Daisy(color, 0, albedo));
					break;
				case 6:
					daisyWorld[i + 1][j - 1].setDaisy_here(new Daisy(color, 0, albedo));
					break;
				case 7:
					daisyWorld[i + 1][j].setDaisy_here(new Daisy(color, 0, albedo));
					break;
				}
			}
		}
		// bottom left corner
		if (i == Params.PATCH_SIZE - 1 && j == 0) {
			if (!daisyWorld[i - 1][j].hasDaisy()) {
				temp.add(2);
			}
			if (!daisyWorld[i - 1][j + 1].hasDaisy()) {
				temp.add(3);
			}
			if (!daisyWorld[i][j + 1].hasDaisy()) {
				temp.add(5);
			}
			// Randomly choosing an empty patch and reproduce
			if (temp.size() > 0) {
				int selected = random.nextInt(temp.size());
				switch (temp.get(selected)) {
				case 2:
					daisyWorld[i - 1][j].setDaisy_here(new Daisy(color, 0, albedo));
					break;
				case 3:
					daisyWorld[i - 1][j + 1].setDaisy_here(new Daisy(color, 0, albedo));
					break;
				case 5:
					daisyWorld[i][j + 1].setDaisy_here(new Daisy(color, 0, albedo));
					break;
				}
			}
		}
		// bottom right corner
		if (i == Params.PATCH_SIZE - 1 && j == Params.PATCH_SIZE - 1) {
			if (!daisyWorld[i - 1][j - 1].hasDaisy()) {
				temp.add(1);
			}
			if (!daisyWorld[i - 1][j].hasDaisy()) {
				temp.add(2);
			}
			if (!daisyWorld[i][j - 1].hasDaisy()) {
				temp.add(4);
			}
			// Randomly choosing an empty patch and reproduce
			if (temp.size() > 0) {
				int selected = random.nextInt(temp.size());
				switch (temp.get(selected)) {
				case 1:
					daisyWorld[i - 1][j - 1].setDaisy_here(new Daisy(color, 0, albedo));
					break;
				case 2:
					daisyWorld[i - 1][j].setDaisy_here(new Daisy(color, 0, albedo));
					break;
				case 4:
					daisyWorld[i][j - 1].setDaisy_here(new Daisy(color, 0, albedo));
					break;
				}
			}
		}
		// top row (5 neighbours)
		if (i == 0 && j > 0 && j < Params.PATCH_SIZE - 1) {
			if (!daisyWorld[i][j - 1].hasDaisy()) {
				temp.add(4);
			}
			if (!daisyWorld[i][j + 1].hasDaisy()) {
				temp.add(5);
			}
			if (!daisyWorld[i + 1][j - 1].hasDaisy()) {
				temp.add(6);
			}
			if (!daisyWorld[i + 1][j].hasDaisy()) {
				temp.add(7);
			}
			if (!daisyWorld[i + 1][j + 1].hasDaisy()) {
				temp.add(8);
			}
			// Randomly choosing an empty patch and reproduce
			if (temp.size() > 0) {
				int selected = random.nextInt(temp.size());
				switch (temp.get(selected)) {
				case 4:
					daisyWorld[i][j - 1].setDaisy_here(new Daisy(color, 0, albedo));
					break;
				case 5:
					daisyWorld[i][j + 1].setDaisy_here(new Daisy(color, 0, albedo));
					break;
				case 6:
					daisyWorld[i + 1][j - 1].setDaisy_here(new Daisy(color, 0, albedo));
					break;
				case 7:
					daisyWorld[i + 1][j].setDaisy_here(new Daisy(color, 0, albedo));
					break;
				case 8:
					daisyWorld[i + 1][j + 1].setDaisy_here(new Daisy(color, 0, albedo));
					break;
				}
			}
		}
		// left column
		if (j == 0 && i > 0 && i < Params.PATCH_SIZE - 1) {
			if (!daisyWorld[i - 1][j].hasDaisy()) {
				temp.add(2);
			}
			if (!daisyWorld[i - 1][j + 1].hasDaisy()) {
				temp.add(3);
			}
			if (!daisyWorld[i][j + 1].hasDaisy()) {
				temp.add(5);
			}
			if (!daisyWorld[i + 1][j].hasDaisy()) {
				temp.add(7);
			}
			if (!daisyWorld[i + 1][j + 1].hasDaisy()) {
				temp.add(8);
			}
			// Randomly choosing an empty patch and reproduce
			if (temp.size() > 0) {
				int selected = random.nextInt(temp.size());
				switch (temp.get(selected)) {
				case 2:
					daisyWorld[i - 1][j].setDaisy_here(new Daisy(color, 0, albedo));
					break;
				case 3:
					daisyWorld[i - 1][j + 1].setDaisy_here(new Daisy(color, 0, albedo));
					break;
				case 5:
					daisyWorld[i][j + 1].setDaisy_here(new Daisy(color, 0, albedo));
					break;
				case 7:
					daisyWorld[i + 1][j].setDaisy_here(new Daisy(color, 0, albedo));
					break;
				case 8:
					daisyWorld[i + 1][j + 1].setDaisy_here(new Daisy(color, 0, albedo));
					break;
				}
			}
		}
		// bottom row
		if (i == Params.PATCH_SIZE - 1 && j > 0 && j < Params.PATCH_SIZE - 1) {
			if (!daisyWorld[i - 1][j - 1].hasDaisy()) {
				temp.add(1);
			}
			if (!daisyWorld[i - 1][j].hasDaisy()) {
				temp.add(2);
			}
			if (!daisyWorld[i - 1][j + 1].hasDaisy()) {
				temp.add(3);
			}
			if (!daisyWorld[i][j - 1].hasDaisy()) {
				temp.add(4);
			}
			if (!daisyWorld[i][j + 1].hasDaisy()) {
				temp.add(5);
			}
			// Randomly choosing an empty patch and reproduce
			if (temp.size() > 0) {
				int selected = random.nextInt(temp.size());
				switch (temp.get(selected)) {
				case 1:
					daisyWorld[i - 1][j - 1].setDaisy_here(new Daisy(color, 0, albedo));
					break;
				case 2:
					daisyWorld[i - 1][j].setDaisy_here(new Daisy(color, 0, albedo));
					break;
				case 3:
					daisyWorld[i - 1][j + 1].setDaisy_here(new Daisy(color, 0, albedo));
					break;
				case 4:
					daisyWorld[i][j - 1].setDaisy_here(new Daisy(color, 0, albedo));
					break;
				case 5:
					daisyWorld[i][j + 1].setDaisy_here(new Daisy(color, 0, albedo));
					break;
				}
			}
		}
		// right column
		if (j == Params.PATCH_SIZE - 1 && i > 0 && i < Params.PATCH_SIZE - 1) {
			if (!daisyWorld[i - 1][j - 1].hasDaisy()) {
				temp.add(1);
			}
			if (!daisyWorld[i - 1][j].hasDaisy()) {
				temp.add(2);
			}
			if (!daisyWorld[i][j - 1].hasDaisy()) {
				temp.add(4);
			}
			if (!daisyWorld[i + 1][j - 1].hasDaisy()) {
				temp.add(6);
			}
			if (!daisyWorld[i + 1][j].hasDaisy()) {
				temp.add(7);
			}
			// Randomly choosing an empty patch and reproduce
			if (temp.size() > 0) {
				int selected = random.nextInt(temp.size());
				switch (temp.get(selected)) {
				case 1:
					daisyWorld[i - 1][j - 1].setDaisy_here(new Daisy(color, 0, albedo));
					break;
				case 2:
					daisyWorld[i - 1][j].setDaisy_here(new Daisy(color, 0, albedo));
					break;
				case 4:
					daisyWorld[i][j - 1].setDaisy_here(new Daisy(color, 0, albedo));
					break;
				case 6:
					daisyWorld[i + 1][j - 1].setDaisy_here(new Daisy(color, 0, albedo));
					break;
				case 7:
					daisyWorld[i + 1][j].setDaisy_here(new Daisy(color, 0, albedo));
					break;
				}
			}
		}
		// mdedium points
		if (i > 0 && i < Params.PATCH_SIZE - 1 && j > 0 && j < Params.PATCH_SIZE - 1) {
			if (!daisyWorld[i - 1][j - 1].hasDaisy()) {
				temp.add(1);
			}
			if (!daisyWorld[i - 1][j].hasDaisy()) {
				temp.add(2);
			}
			if (!daisyWorld[i - 1][j + 1].hasDaisy()) {
				temp.add(3);
			}
			if (!daisyWorld[i][j - 1].hasDaisy()) {
				temp.add(4);
			}
			if (!daisyWorld[i][j + 1].hasDaisy()) {
				temp.add(5);
			}
			if (!daisyWorld[i + 1][j - 1].hasDaisy()) {
				temp.add(6);
			}
			if (!daisyWorld[i + 1][j].hasDaisy()) {
				temp.add(7);
			}
			if (!daisyWorld[i + 1][j + 1].hasDaisy()) {
				temp.add(8);
			}
			// Randomly choosing an empty patch and reproduce
			if (temp.size() > 0) {
				int selected = random.nextInt(temp.size());
				switch (temp.get(selected)) {
				case 1:
					daisyWorld[i - 1][j - 1].setDaisy_here(new Daisy(color, 0, albedo));
					break;
				case 2:
					daisyWorld[i - 1][j].setDaisy_here(new Daisy(color, 0, albedo));
					break;
				case 3:
					daisyWorld[i - 1][j + 1].setDaisy_here(new Daisy(color, 0, albedo));
					break;
				case 4:
					daisyWorld[i][j - 1].setDaisy_here(new Daisy(color, 0, albedo));
					break;
				case 5:
					daisyWorld[i][j + 1].setDaisy_here(new Daisy(color, 0, albedo));
					break;
				case 6:
					daisyWorld[i + 1][j - 1].setDaisy_here(new Daisy(color, 0, albedo));
					break;
				case 7:
					daisyWorld[i + 1][j].setDaisy_here(new Daisy(color, 0, albedo));
					break;
				case 8:
					daisyWorld[i + 1][j + 1].setDaisy_here(new Daisy(color, 0, albedo));
					break;
				}
			}
		}
	}

	/**
	 * Daisy procedure: let the daisy age one and check if the daisy's age beyond its life span,
	 * if it's not, according to the reproductive law to reproduce the daisy.
	 * @param daisyWorld
	 */
	private static void checkSurvivability(Patch[][] daisyWorld) {
		double seed_threshold;
		for (int i = 0; i < Params.PATCH_SIZE; i++) {
			for (int j = 0; j < Params.PATCH_SIZE; j++) {
				Patch here = daisyWorld[i][j];
				double temperature = here.getTemperature();
				// Check if the current patch has daisy
				if (here.hasDaisy()) {
					// daisy age one
					here.getDaisy_here().addAge();
					// Extension: daisy in gray color has longer life span
					if (here.getDaisy_here().getColor() == DaisyColor.GRAY) {
						if (here.getDaisy_here().getAge() <= Params.GRAY_AGE) {
							// quadratic function: the possibility of reproducing
							seed_threshold = ((0.1457 * temperature) - (0.0032 * (temperature * temperature)) - 0.6443);
							if (Math.random() < seed_threshold) {
								// Call the reproduce function
								reproduce(daisyWorld, i, j);
							}
						} else {
							// daisy dies, temperature remain unchanged
							daisyWorld[i][j].setDaisy_here(null);
						}
					} else {
						// Daisy in black or white color has shorter life span
						if (here.getDaisy_here().getAge() <= Params.MAX_AGE) {
							seed_threshold = ((0.1457 * temperature) - (0.0032 * (temperature * temperature)) - 0.6443);
							if (Math.random() < seed_threshold) {
								// Call the reproduce function
								reproduce(daisyWorld, i, j);
							}
						} else {
							// daisy dies, temperature remain unchanged
							daisyWorld[i][j].setDaisy_here(null);
						}
					}
				}
			}
		}
	}

}
