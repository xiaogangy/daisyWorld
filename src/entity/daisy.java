// Xiaogang Yang: 828331; Peiwen Du: 800366
package entity;

/**
 * Simulates the daisy in alignment with NetLogo model. Each daisy has different
 * color, daisy with gray color has longer life span and its albedo is situated 
 * between the white daisy and black daisy.
 * @author Xiaogang Yang. Peiwen Du
 *
 */
public class Daisy {
	
	private DaisyColor color;
	private int age;
	private double albedo;
	
	public Daisy(DaisyColor color, int age, double albedo) {
		this.albedo = albedo;
		this.age = age;
		this.color = color;
	}

	public DaisyColor getColor() {
		return color;
	}

	public void setColor(DaisyColor color) {
		this.color = color;
	}

	public int getAge() {
		return age;
	}
	
	/**
	 * The daisy ages one
	 */
	public void addAge() {
		age++;
	}

	public double getAlbedo() {
		return albedo;
	}

	public void setAlbedo(double albedo) {
		this.albedo = albedo;
	}
}
