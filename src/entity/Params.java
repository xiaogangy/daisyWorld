// Xiaogang Yang: 828331; Peiwen Du: 800366
package entity;
/**
 * This files stores all the parameters of this system
 * @author Xiaogang Yang. Peiwen Du
 *
 */
public class Params {
	
	// The life span of the daisy in white or black color
	public static final int MAX_AGE = 25;
	// The life span of the daisy in gray
	public static final int GRAY_AGE = 30;
	// The size of the world
	public static final int PATCH_SIZE = 29;
	
	// The percentage of the daisy in different color 
	public static final double NUM_WHITES = 0.2;
	public static final double NUM_BLACKS = 0.2;
	public static final double NUM_GRAY = 0.2;
	// The albedo of different turtles
	public static final double ALBEDO_OF_WHITES = 0.75;
	public static final double ALBEDO_OF_BLACK = 0.25;
	public static final double ALBEDO_OF_SURFACE =0.4;
	// The albedo of gray daisy is situated between the black and white
	public static final double ALBEDO_OF_GRAY = 0.5;
	
	public static double SOLAR_LUMINOSITY = 1;
	
	/**
	 * When the scenario is ramp-up-ramp-down, this function would modify the solar
	 * luminosity based on the tick
	 * @param tick
	 */
	public static void setSOLAR_LUMINOSITY(int tick) {
		if(tick>200 && tick <= 400) {
			// Make sure the precision is 4 digits
			SOLAR_LUMINOSITY = (double)Math.round((SOLAR_LUMINOSITY+0.005)*10000)/10000;
		}
		if(tick>600 && tick <= 850) {
			SOLAR_LUMINOSITY = (double)Math.round((SOLAR_LUMINOSITY-0.0025)*10000)/10000;
		}
	}

}
