package nl.tue.ieis.is.GoalModel;

import java.util.Collection;
import java.util.Vector;

public abstract class GoalRelation {
	private Goal g1;
	private Goal g2;
	boolean inferred;
	boolean is_stage_task_milestone;
	String consistency_rule;
	
	public GoalRelation(Goal g1, Goal g2, String s) {
		this.g1=g1;
		this.g2=g2;
		inferred=false;
		consistency_rule=s;
		is_stage_task_milestone=false;
	}
	
	public Goal getFirstGoal() {
		return g1;
	}
	
	public Goal getSecondGoal() {
		return g2;
	}
	
	public void replace(Goal oldgoal, Goal newgoal) {
		if (g1.equals(oldgoal)) g1=newgoal;
		if (g2.equals(oldgoal)) g2=newgoal;
	}
	
	public void setInferred() {
		inferred=true;
	}
	
	public boolean isInferred() {
		return inferred;
	}
	
	public boolean isComplements() {
		return false;
	}
	
	public boolean isSupports() {
		return false;
	}
	
	public boolean isExcludes() {
		return false;
	}
	
	public boolean isReflexive() {
		return g1.equals(g2);
	}
	public boolean isStageTaskMilestone() {
		return is_stage_task_milestone;
	}
	
	public boolean covers(Goal g1, Goal g2) {
		return this.g1.equals(g1) && this.g2.equals(g2);
	}
	
	public boolean touches(Goal g) {
		return this.g1.equals(g)||this.g2.equals(g);
	}
	
	public boolean touches(Collection<Goal> gs) {
		for (Goal g:gs) {
			if (touches(g)) return true;
		}
		return false;
	}
	
	public boolean isCoveredBy(Collection<Goal> gs) {
		return gs.contains(this.g1)&&gs.contains(this.g2);
	}
	public String getConsistencyRule() {
		return consistency_rule;
	}
	
}
