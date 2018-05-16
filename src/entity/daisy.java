package entity;

public class daisy {
	
	private static String black= "BLACK";
	private static String white= "WHITE";
	
	private String color;
	private int age;
	private double albedo;
	
	public daisy(String color, int age, double albedo) {
		// TODO Auto-generated constructor stub
		this.albedo = albedo;
		this.age = age;
		this.color = color;
	}
	
	public boolean check_live(int max_age){
		age++;
		return (age<=max_age);
	}
}
