package nl.tue.ieis.is.GoalModel;

public class ComplementsRelation extends GoalRelation {

	private boolean isStageTaskMilestone;
	
	public ComplementsRelation(Goal g1, Goal g2, String s, boolean isStageTaskMilestone) {
		super(g1, g2, s);
		// TODO Auto-generated constructor stub
		this.is_stage_task_milestone=isStageTaskMilestone;
	}
	
	public boolean isComplements() {
		return true;
	}

	
	public void print() {
		System.out.println(this.getFirstGoal().getName()+"   complements   " + this.getSecondGoal().getName()+ " ("+ this.consistency_rule +")");
	}
	
}
