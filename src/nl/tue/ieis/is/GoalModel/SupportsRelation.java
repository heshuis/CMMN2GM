package nl.tue.ieis.is.GoalModel;

public class SupportsRelation extends GoalRelation {

	public SupportsRelation(Goal g1, Goal g2, String s) {
		super(g1, g2, s);
		// TODO Auto-generated constructor stub
	}
	
	public boolean isSupports() {
		return true;
	}

	public void print() {
		System.out.println(this.getFirstGoal().getName()+"   supports   " + this.getSecondGoal().getName() + " ("+ this.consistency_rule +")");
	}
	
}
