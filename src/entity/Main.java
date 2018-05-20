package entity;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import settings.*;

public class Main {

	public static void main(String[] args) {

		// 第一步，创建一个workbook对应一个excel文件
		HSSFWorkbook workbook = new HSSFWorkbook();
		// 第二部，在workbook中创建一个sheet对应excel中的sheet
		HSSFSheet sheet = workbook.createSheet("ExcelTable1");
		// 第三部，在sheet表中添加表头第0行，老版本的poi对sheet的行列有限制
		HSSFRow row = sheet.createRow(0);
		// 第四步，创建单元格，设置表头
		HSSFCell cell = row.createCell(0);
		cell.setCellValue("Tick");
		cell = row.createCell(1);
		cell.setCellValue("GlobalTemperature");
		cell = row.createCell(2);
		cell.setCellValue("BlackPopulation");
		cell = row.createCell(3);
		cell.setCellValue("WhitePopulation");

		Patch[][] daisyWorld = new Patch[Params.PATCH_SIZE][Params.PATCH_SIZE];
		double global_temperature = 0;
		int whiteAmount;
		int blackAmount;

		// 初始化daisyWorld
		for (int i = 0; i < Params.PATCH_SIZE; i++) {
			for (int j = 0; j < Params.PATCH_SIZE; j++) {
				daisyWorld[i][j] = new Patch(0);
			}
		}

		// 随机播撒种子
		seed_randomly(daisyWorld, DaisyColor.BLACK);
		seed_randomly(daisyWorld, DaisyColor.WHITE);

		// 计算每个patch的温度
		for (int i = 0; i < Params.PATCH_SIZE; i++) {
			for (int j = 0; j < Params.PATCH_SIZE; j++) {
				daisyWorld[i][j].calc_temperature();
			}
		}
		// 计算平均温度
		global_temperature = average_Temperature(daisyWorld);

		HSSFRow row1 = sheet.createRow(1);
		row1.createCell(0).setCellValue(0);
		row1.createCell(1).setCellValue(global_temperature);
		row1.createCell(2).setCellValue(daisyAmount(daisyWorld, DaisyColor.BLACK));
		row1.createCell(3).setCellValue(daisyAmount(daisyWorld, DaisyColor.WHITE));

		// 开始进行更新，并绘制折线图
		for (int tick = 0; tick < 300; tick++) {
			//Params.setSOLAR_LUMINOSITY(tick);
			// 计算温度
			for (int i = 0; i < Params.PATCH_SIZE; i++) {
				for (int j = 0; j < Params.PATCH_SIZE; j++) {
					daisyWorld[i][j].calc_temperature();
				}
			}
			// 温度消散
			diffusion(daisyWorld);
			// 检查存活率并繁殖
			checkSurvivability(daisyWorld);
			// 计算平均温度
			global_temperature = average_Temperature(daisyWorld);
			blackAmount = daisyAmount(daisyWorld, DaisyColor.BLACK);
			whiteAmount = daisyAmount(daisyWorld, DaisyColor.WHITE);

			// System.out.println(tick);
			// 写入Excel表中
			HSSFRow row2 = sheet.createRow(tick + 2);
			row2.createCell(0).setCellValue(tick + 1);
			row2.createCell(1).setCellValue(global_temperature);
			row2.createCell(2).setCellValue(blackAmount);
			row2.createCell(3).setCellValue(whiteAmount);
		}
		System.out.println("更新结束");
		// 生成文件
		try {
			FileOutputStream fos = new FileOutputStream("E:\\global.xls");
			workbook.write(fos);
			System.out.println("写入成功");
			fos.close();
			workbook.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	// 统计各色的daisy
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

	// 随机播撒种子
	private static void seed_randomly(Patch[][] daisyWorld, DaisyColor color) {
		Random random = new Random();
		int i = 0;
		if (color == DaisyColor.BLACK) {
			while (i < Params.PATCH_SIZE * Params.PATCH_SIZE * Params.NUM_BLACKS) {
				int x = random.nextInt(Params.PATCH_SIZE);
				int y = random.nextInt(Params.PATCH_SIZE);
				if (!daisyWorld[x][y].hasDaisy()) {
					daisyWorld[x][y]
							.setDaisy_here(new Daisy(color, random.nextInt(Params.MAX_AGE), Params.ALBEDO_OF_BLACK));
					i++;
				} else {
					continue;
				}
			}
		} else {
			while (i < Params.PATCH_SIZE * Params.PATCH_SIZE * Params.NUM_BLACKS) {
				int x = random.nextInt(Params.PATCH_SIZE);
				int y = random.nextInt(Params.PATCH_SIZE);
				if (!daisyWorld[x][y].hasDaisy()) {
					daisyWorld[x][y]
							.setDaisy_here(new Daisy(color, random.nextInt(Params.MAX_AGE), Params.ALBEDO_OF_WHITES));
					i++;
				} else {
					continue;
				}
			}
		}
	}

	// 计算全局平均温度
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

	// 温度消散
	private static void diffusion(Patch[][] daisyWorld) {

		// 消散矩阵
		double[][] diffusion = new double[Params.PATCH_SIZE][Params.PATCH_SIZE];
		for (int i = 0; i < Params.PATCH_SIZE; i++) {
			for (int j = 0; j < Params.PATCH_SIZE; j++) {
				diffusion[i][j] = (daisyWorld[i][j].getTemperature()) / 8;
			}
		}

		// 吸收温度
		for (int i = 0; i < Params.PATCH_SIZE; i++) {
			for (int j = 0; j < Params.PATCH_SIZE; j++) {
				double current_temp = daisyWorld[i][j].getTemperature();
				double final_temp;
				double absorbed_temp;

				// 左上角
				if (i == 0 && j == 0) {
					absorbed_temp = diffusion[0][1] + diffusion[1][0] + diffusion[1][1];
					final_temp = absorbed_temp + current_temp / 2;
					daisyWorld[i][j].setTemperature(final_temp);
					continue;
				}
				// 右上角
				if (i == 0 && j == Params.PATCH_SIZE - 1) {
					absorbed_temp = diffusion[i][j - 1] + diffusion[i + 1][j - 1] + diffusion[i + 1][j];
					final_temp = absorbed_temp + current_temp / 2;
					daisyWorld[i][j].setTemperature(final_temp);
					continue;
				}
				// 左下角
				if (i == Params.PATCH_SIZE - 1 && j == 0) {
					absorbed_temp = diffusion[i - 1][j] + diffusion[i - 1][j + 1] + diffusion[i][j + 1];
					final_temp = absorbed_temp + current_temp / 2;
					daisyWorld[i][j].setTemperature(final_temp);
					continue;
				}
				// 右下角
				if (i == Params.PATCH_SIZE - 1 && j == Params.PATCH_SIZE - 1) {
					absorbed_temp = diffusion[i][j - 1] + diffusion[i - 1][j] + diffusion[i - 1][j - 1];
					final_temp = absorbed_temp + current_temp / 2;
					daisyWorld[i][j].setTemperature(final_temp);
					continue;
				}
				// 上边
				if (i == 0 && 0 < j && j < Params.PATCH_SIZE - 1) {
					absorbed_temp = diffusion[i][j - 1] + diffusion[i][j + 1] + diffusion[i + 1][j - 1]
							+ diffusion[i + 1][j] + diffusion[i + 1][j + 1];
					final_temp = absorbed_temp + current_temp / 2;
					daisyWorld[i][j].setTemperature(final_temp);
					continue;
				}
				// 下边
				if (i == Params.PATCH_SIZE - 1 && 0 < j && j < Params.PATCH_SIZE - 1) {
					absorbed_temp = diffusion[i][j - 1] + diffusion[i][j + 1] + diffusion[i - 1][j - 1]
							+ diffusion[i - 1][j] + diffusion[i - 1][j + 1];
					final_temp = absorbed_temp + current_temp / 2;
					daisyWorld[i][j].setTemperature(final_temp);
					continue;
				}
				// 左边
				if (j == 0 && 0 < i && i < Params.PATCH_SIZE - 1) {
					absorbed_temp = diffusion[i - 1][j] + diffusion[i + 1][j] + diffusion[i - 1][j + 1]
							+ diffusion[i][j + 1] + diffusion[i + 1][j + 1];
					final_temp = absorbed_temp + current_temp / 2;
					daisyWorld[i][j].setTemperature(final_temp);
					continue;
				}
				// 右边
				if (j == Params.PATCH_SIZE - 1 && 0 < i && i < Params.PATCH_SIZE - 1) {
					absorbed_temp = diffusion[i][j - 1] + diffusion[i - 1][j - 1] + diffusion[i + 1][j - 1]
							+ diffusion[i - 1][j] + diffusion[i + 1][j];
					final_temp = absorbed_temp + current_temp / 2;
					daisyWorld[i][j].setTemperature(final_temp);
					continue;
				}
				// 中间的点
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

	// 选择空地并繁殖
	private static void reproduce(Patch[][] daisyWorld, int i, int j) {
		// 用来保存neighbour中空点的集合
		ArrayList<Integer> temp = new ArrayList<Integer>();
		Random random = new Random();
		Patch here = daisyWorld[i][j];
		// double temperature = here.getTemperature();
		// 当前patch出的daisy颜色和反光率
		DaisyColor color = here.getDaisy_here().getColor();
		double albedo;
		if (color == DaisyColor.WHITE) {
			albedo = Params.ALBEDO_OF_WHITES;
		} else {
			albedo = Params.ALBEDO_OF_BLACK;
		}

		// 左上角
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
			// 种植reproduce
			if(temp.size()>0) {
				int selected = random.nextInt(temp.size());
				switch (temp.get(selected)) {
				case 5:
					daisyWorld[i][j+1].setDaisy_here(new Daisy(color, 0, albedo));
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
		// 右上角
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
			// 种植reproduce
			if(temp.size()>0) {
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
		// 左下角
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
			// 种植reproduce
			if(temp.size()>0) {
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
		// 右下角
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
			// 种植reproduce
			if(temp.size()>0) {
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
		// 上边(5个邻居)
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
			// 种植reproduce
			if(temp.size()>0) {
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
		// 左边(5个邻居)
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
			// 种植reproduce
			if(temp.size()>0) {
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
		// 下边
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
			// 种植reproduce
			if(temp.size()>0) {
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
		// 右边
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
			// 种植reproduce
			if(temp.size()>0) {
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
		// 中间点(8个邻居)
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
			// 种植reproduce
			if(temp.size()>0) {
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

	// 检查存活，并繁殖
	private static void checkSurvivability(Patch[][] daisyWorld) {
		double seed_threshold;
		for (int i = 0; i < Params.PATCH_SIZE; i++) {
			for (int j = 0; j < Params.PATCH_SIZE; j++) {
				// 判断本地是否有daisy
				Patch here = daisyWorld[i][j];
				double temperature = here.getTemperature();
				if (here.hasDaisy()) {
					// 增长一岁
					here.getDaisy_here().addAge();
					// 开始判断
					if (here.getDaisy_here().getAge() < Params.MAX_AGE) {
						// 在周边找一个种植daisy
						seed_threshold = ((0.1457 * temperature) - (0.0032 * (temperature * temperature)) - 0.6443);
						if (Math.random() < seed_threshold) {
							// 选择空地
							// 种植种子
							reproduce(daisyWorld, i, j);
						}
					} else {
						// daisy dies, temperature remain unchanged死亡，将该地设置为空地
						daisyWorld[i][j].setDaisy_here(null);
					}
				}
			}
		}
	}

}
