package nl.tue.ieis.is.GoalModel;

public class Goal {
	String name;
	boolean isMilestone;
	static int counter=0;
	
	public Goal() {
		name="G"+counter++;
		isMilestone=false;
	}
	public Goal(String n, boolean isMil) {
		name=n;
		isMilestone=isMil;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean isMilestone() {
		return isMilestone;
	}
	
	public void print() {
		System.out.println("Goal: " + name);
	}
}
