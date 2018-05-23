// Xiaogang Yang: 828331; Peiwen Du: 800366
package entity;

/**
 * Simulates the daisy in alignment with NetLogo model. Each daisy has different
 * color, the albedo of daisy in gray is situated between the white daisy and
 * black daisy.
 * 
 * @author Xiaogang Yang. Peiwen Du
 *
 */
public class Daisy {

	private DaisyColor color;
	private int age;
	private double albedo;
	
	/**
	 * Constructor: create a daisy with designated color, age and albedo
	 * @param color
	 * @param age
	 * @param albedo
	 */
	public Daisy(DaisyColor color, int age, double albedo) {
		this.albedo = albedo;
		this.age = age;
		this.color = color;
	}

	/**
	 * Getter: get the color of daisy
	 * @return color
	 */
	public DaisyColor getColor() {
		return color;
	}

	/**
	 * Setter: set the color of daisy
	 * @param color
	 */
	public void setColor(DaisyColor color) {
		this.color = color;
	}

	/**
	 * Getter: get current age of daisy
	 * @return age
	 */
	public int getAge() {
		return age;
	}

	/**
	 * The daisy ages one
	 */
	public void addAge() {
		age++;
	}

	/**
	 * Getter: get the albedo of daisy
	 * @return albedo
	 */
	public double getAlbedo() {
		return albedo;
	}

	/**
	 * Setter: set the albedo of daisy
	 * @param albedo
	 */
	public void setAlbedo(double albedo) {
		this.albedo = albedo;
	}
}
