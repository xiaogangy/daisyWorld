package settings;

public class Params {
	
	public static final int MAX_AGE = 25;
	public static final int PATCH_SIZE = 29;
	
	public static final double NUM_WHITES = 0.2;
	public static final double NUM_BLACKS = 0.2;
	public static final double ALBEDO_OF_WHITES = 0.75;
	public static final double ALBEDO_OF_BLACK = 0.25;
	public static final double ALBEDO_OF_SURFACE =0.4;
	
	public static double SOLAR_LUMINOSITY = 0.6;
	
	public static void setSOLAR_LUMINOSITY(int tick) {
		if(tick>200 && tick <= 400) {
			SOLAR_LUMINOSITY = (double)Math.round((SOLAR_LUMINOSITY+0.005)*10000)/10000;
		}
		if(tick>600 && tick <= 850) {
			SOLAR_LUMINOSITY = (double)Math.round((SOLAR_LUMINOSITY-0.0025)*10000)/10000;
		}
	}

}
