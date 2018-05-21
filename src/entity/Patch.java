package entity;

import settings.*;

public class Patch {
	
	private double temperature;
	private Daisy daisy_here;
	
	Patch(double temperature){
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
	
	//计算patch的温度
	public void calc_temperature(){
		
		double local_heating;
		double absorbed_luminosity = 0;
		
		if(daisy_here == null) {
			absorbed_luminosity = (1-Params.ALBEDO_OF_SURFACE)*Params.SOLAR_LUMINOSITY;
		}else if (daisy_here.getColor() == DaisyColor.BLACK){
			absorbed_luminosity = (1-Params.ALBEDO_OF_BLACK)*Params.SOLAR_LUMINOSITY;
		}else if(daisy_here.getColor() == DaisyColor.WHITE){
			absorbed_luminosity = (1-Params.ALBEDO_OF_WHITES)*Params.SOLAR_LUMINOSITY;
		}else if(daisy_here.getColor() == DaisyColor.GRAY) {
			// Calculate the absorbed luminosity of gray daisy
			absorbed_luminosity = (1-Params.ALBEDO_OF_GRAY)*Params.SOLAR_LUMINOSITY;
		}
		
		if(absorbed_luminosity > 0) {
			local_heating = 72 * Math.log(absorbed_luminosity) + 80;
		}else {
			local_heating = 80;
		}
		
		temperature = (temperature + local_heating)/2;
	}
	
	
}
