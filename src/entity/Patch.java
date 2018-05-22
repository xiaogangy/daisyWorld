// Xiaogang Yang: 828331; Peiwen Du: 800366
package entity;

/**
 * This class simulates the patch in the NegLogo. Same as the implementation in
 * NetLogo, each patch has a temperature property and probably has a daisy on it
 * according to the update rules.
 * 
 * @author Xiaogang Yang. Peiwen Du
 *
 */
public class Patch {

	private double temperature;
	// A daisy could grow on the patch
	private Daisy daisy_here;

	Patch(double temperature) {
		this.temperature = temperature;
		this.daisy_here = null;
	}

	public Patch(double temperature, Daisy daisy) {
		this.temperature = temperature;
		this.daisy_here = daisy;
	}

	public double getTemperature() {
		return temperature;
	}

	public void setTemperature(double temperature) {
		this.temperature = temperature;
	}

	public Daisy getDaisy_here() {
		return daisy_here;
	}

	public void setDaisy_here(Daisy daisy_here) {
		this.daisy_here = daisy_here;
	}

	public boolean hasDaisy() {
		return !(daisy_here == null);
	}

	/**
	 * Calculate the absorbed temperature of the local patch. A patch could have
	 * four different states: an empty patch, a white daisy over it, a black daisy
	 * over it, and a gray daisy over it. Its absorbed temperature depends on the
	 * different luminosity.
	 */
	public void calc_temperature() {
		// Represent the final temperature in terms of the albedo
		double local_heating;
		double absorbed_luminosity = 0;

		if (daisy_here == null) {
			// when the patch is empty
			absorbed_luminosity = (1 - Params.ALBEDO_OF_SURFACE) * Params.SOLAR_LUMINOSITY;
		} else if (daisy_here.getColor() == DaisyColor.BLACK) {
			// when the patch is occupied by the black daisy
			absorbed_luminosity = (1 - Params.ALBEDO_OF_BLACK) * Params.SOLAR_LUMINOSITY;
		} else if (daisy_here.getColor() == DaisyColor.WHITE) {
			// when the patch is occupied by the white daisy
			absorbed_luminosity = (1 - Params.ALBEDO_OF_WHITES) * Params.SOLAR_LUMINOSITY;
		} else if (daisy_here.getColor() == DaisyColor.GRAY) {
			// when the patch is occupied by the gray daisy
			absorbed_luminosity = (1 - Params.ALBEDO_OF_GRAY) * Params.SOLAR_LUMINOSITY;
		}

		if (absorbed_luminosity > 0) {
			local_heating = 72 * Math.log(absorbed_luminosity) + 80;
		} else {
			local_heating = 80;
		}
		// Update the patch's temperature
		temperature = (temperature + local_heating) / 2;
	}

}
