package nl.tue.ieis.is.GoalModel;

public class ExcludesRelation extends GoalRelation {
 
	public ExcludesRelation(Goal g1, Goal g2, String s) {
		super(g1, g2, s);
		// TODO Auto-generated constructor stub
	}

	public boolean isExcludes() {
		return true;
	}
	
	public void print() {
		System.out.println(this.getFirstGoal().getName()+"   excludes   " + this.getSecondGoal().getName()+ " ("+ this.consistency_rule +")");
	}
	
}
