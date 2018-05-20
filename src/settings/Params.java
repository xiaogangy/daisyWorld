package settings;

public class Params {
	
	public static final int MAX_AGE = 25;
	public static final int PATCH_SIZE = 29;
	
	public static final double NUM_WHITES = 0.2;
	public static final double NUM_BLACKS = 0.2;
	public static final double ALBEDO_OF_WHITES = 0.75;
	public static final double ALBEDO_OF_BLACK = 0.25;
	public static final double ALBEDO_OF_SURFACE =0.4;
	
	public static double SOLAR_LUMINOSITY = 1;
	
	public double getSOLAR_LUMINOSITY() {
		return SOLAR_LUMINOSITY;
	}

	public double setSOLAR_LUMINOSITY(String scenario) {
		if (scenario.equals("ramp_up_ramp_down")){
			SOLAR_LUMINOSITY = 0.8;
		}else if(scenario.equals("low_solar_luminosity")){
			SOLAR_LUMINOSITY = 0.6;
		}else if(scenario.equals("our_solar_luminosity")){
			SOLAR_LUMINOSITY = 1.0;
		}else if(scenario.equals("high_solar_luminosity")){
			SOLAR_LUMINOSITY = 1.4;
		}
		return SOLAR_LUMINOSITY;
	}

}
