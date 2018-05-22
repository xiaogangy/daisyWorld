//package entity;

//import settings.*;

public class Daisy {
	
	private DaisyColor color;
	private int age;
	private double albedo;
	
	public Daisy(DaisyColor color, int age, double albedo) {
		// TODO Auto-generated constructor stub
		this.albedo = albedo;
		this.age = age;
		this.color = color;
	}
	
	public boolean check_live(int max_age){
		age++;
		return (age<=max_age);
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
