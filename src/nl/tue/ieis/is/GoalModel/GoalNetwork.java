package nl.tue.ieis.is.GoalModel;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import nl.tue.ieis.is.CMMN.PlanItem;

public class GoalNetwork {
	
	private Vector<Goal> goals;
	private Set<GoalRelation> grs;
	private Set<GoalRelation> intransitive;
	private String name;
	
	public GoalNetwork(String n) {
		goals=new Vector<Goal>();
		grs=new HashSet <GoalRelation>();
		name=n;
	}
	
	public Goal getGoal(String n, boolean isMilestone) {
		for (Goal goal:goals) {
			if (goal.getName().equals(n)) {
				return goal;
			}
		}
		// no goal with name n found, so create a new goal
		Goal g=new Goal(n,isMilestone);
		goals.add(g);
		return g;
	}
	
	public void addGoalRelation(GoalRelation gr) {
		grs.add(gr);
	}
	
	public void inferDependencies(){
		HashSet<ComplementsRelation> complements=new HashSet<ComplementsRelation>();
		HashSet<ExcludesRelation> excludes=new HashSet<ExcludesRelation>();
		HashSet<SupportsRelation> supports=new HashSet<SupportsRelation>();

		// step 1
		for (GoalRelation gr:grs) {
			if (gr.isComplements()) {
				ComplementsRelation cr=new ComplementsRelation(gr.getSecondGoal(),gr.getFirstGoal(), "inferred (symmetry complements)", gr.isStageTaskMilestone());
				complements.add(cr);
				cr.setInferred();
			}
			if (gr.isExcludes()) {
				ExcludesRelation er=new ExcludesRelation(gr.getSecondGoal(),gr.getFirstGoal(), "inferred (symmetry excludes)1");
				excludes.add(er);
				er.setInferred();
			}
		}
		for (ComplementsRelation cr:complements) {
			this.addComplements(cr);
		}
		complements.clear();
		for (ExcludesRelation er:excludes) {
			this.addExcludes(er);
		}		
		excludes.clear();
		
		
		// step 2
		boolean changed=true;
		while (changed) {
			changed=false;
			for (GoalRelation gr1:grs) {
				for (GoalRelation gr2:grs) {
					if (gr1.getSecondGoal().equals(gr2.getFirstGoal())) {
						if (gr1.getFirstGoal().equals(gr2.getSecondGoal())) continue; // relations are irreflexive
						if (gr1.isSupports() && gr2.isSupports() && (!this.isSupports(gr1.getFirstGoal(), gr2.getSecondGoal()))) {
							SupportsRelation sr=new SupportsRelation(gr1.getFirstGoal(), gr2.getSecondGoal(), "inferred (transitivity supports)");
							sr.setInferred();
							supports.add(sr);
							changed=true;
						}
						if ((gr1.isComplements()||gr1.isExcludes())&&gr2.isSupports() && (!this.isRelated(gr1.getFirstGoal(), gr2.getSecondGoal()))) {
							SupportsRelation sr=new SupportsRelation(gr1.getFirstGoal(),gr2.getSecondGoal(),"inferred supports");
							sr.setInferred();
							supports.add(sr);
							changed=true;
						}
					}					
				}
			}
			for (ComplementsRelation cr:complements) {
				this.addComplements(cr);
			}
			complements.clear();
			for (ExcludesRelation er:excludes) {
				this.addExcludes(er);
			}		
			excludes.clear();
			for (SupportsRelation sr:supports) {
				this.addSupports(sr);
			}		
			supports.clear();
			for (ComplementsRelation cr:complements) {
				this.addComplements(cr);
			}
			complements.clear();
			for (ExcludesRelation er:excludes) {
				this.addExcludes(er);
			}		
			excludes.clear();

			// repeat first two steps
			//step 1 repeated
			for (GoalRelation gr:grs) {
				if (gr.isComplements()) {
					if (!this.isComplements(gr.getSecondGoal(), gr.getFirstGoal())) {
						ComplementsRelation cr=new ComplementsRelation(gr.getSecondGoal(),gr.getFirstGoal(), "inferred (symmetry complements)",gr.isStageTaskMilestone());
						complements.add(cr);
						cr.setInferred();
						changed=true;
					}
				}
				if (gr.isExcludes()) {
					if (!this.isExcludes(gr.getSecondGoal(), gr.getFirstGoal())) {
						ExcludesRelation er=new ExcludesRelation(gr.getSecondGoal(),gr.getFirstGoal(), "inferred (symmetry excludes2)");
						excludes.add(er);
						er.setInferred();
						changed=true;
					}
				}
			}
			for (ComplementsRelation cr:complements) {
				this.addComplements(cr);
			}
			complements.clear();
			for (ExcludesRelation er:excludes) {
				this.addExcludes(er);
			}		
			excludes.clear();
		}
	}
	

	
	public Set<GoalRelation> computeTransitiveReduction() {
		HashSet<GoalRelation> intransitive=new HashSet<GoalRelation>();
		for (GoalRelation gr:grs) {
			if (gr.isSupports()) intransitive.add(gr);
		}
		for (Goal g1:goals) {
			for (Goal g2:goals)
			if (isSupports(g1,g2)) {
				Set<Goal> succ_g2=this.supportsSucccessor(g2);
				for (Goal g3:goals) {
					if (succ_g2.contains(g3)&&isSupports(g1, g3)) {
						intransitive.removeIf(gr->gr.getFirstGoal().equals(g1)&&gr.getSecondGoal().equals(g3));
					}
				}
			}
		}
		return intransitive;
	}
	
	public Set<Goal> supportsSucccessor(Goal sg){
		HashSet<Goal> succs=new HashSet<Goal>();	
		for (Goal g:goals) {
			if (isSupports(sg,g)) {
				succs.add(g);
				succs.addAll(supportsSucccessor(g));
			}
		}
		return succs;
	}
	

	
	public void analyzeInconsistencies() {
		for (Goal g1:goals) {
			for (Goal g2:goals) {
				if (this.isComplements(g1, g2) && this.isExcludes(g1, g2)) {
					System.out.println("Inconsistency: goals " +g1.getName() + " and " + g2.getName() + " are both complementary and exclusive!");
				}
				if (this.isSupports(g1, g2) && this.isExcludes(g1, g2)) {
					System.out.println("Inconsistency: goals "+ g1.getName() + " and " + g2.getName() + " are exclusive, yet " +g1.getName() + " supports " + g2.getName()+ "!");
				}
				if (this.isSupports(g2, g1) && this.isExcludes(g1, g2)) {
					System.out.println("Inconsistency: goals "+ g1.getName() + " and " + g2.getName() + " are exclusive, yet " +g2.getName() + " supports " + g1.getName()+ "!");
				}
				if (this.isSupports(g1, g2) && this.isComplements(g1, g2)) {	
					System.out.println("Inconsistency: goals " + g1.getName() + " and " + g2.getName() + " are complementary, yet " +g1.getName() + " supports " + g2.getName()+ "!");
				}
				if (this.isSupports(g2, g1) && this.isComplements(g1, g2)) {	
					System.out.println("Inconsistency: goals " + g1.getName() + " and " + g2.getName() + " are complementary, yet " +g2.getName() + " supports " + g1.getName()+ "!");
				}
			}
		}
	}
	
	
	
	
	public boolean isRelated(Goal g1, Goal g2) {
		return isSupports(g1,g2)||isComplements(g1,g2)||isExcludes(g1,g2);
	}
	
	public boolean isSupports(Goal g1, Goal g2) {
		for (GoalRelation gr:grs) {
			if (gr.covers(g1, g2) && gr.isSupports()) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isComplements(Goal g1, Goal g2) {
		for (GoalRelation gr:grs) {
			if (gr.covers(g1, g2) && gr.isComplements()) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isExcludes(Goal g1, Goal g2) {
		for (GoalRelation gr:grs) {
			if (gr.covers(g1, g2) && gr.isExcludes()) {
				return true;
			}
		}
		return false;
	}
	
	public void setComplements(Goal g1, Goal g2, String s, boolean isStageTaskMilestone) {	
		if (!isComplements(g1,g2)) {
			ComplementsRelation gr_new=new ComplementsRelation(g1,g2,s,isStageTaskMilestone);
			this.addGoalRelation(gr_new);
		}
	}
	
	public void addComplements(ComplementsRelation cr) {
		if (!isComplements(cr.getFirstGoal(),cr.getSecondGoal())) {
			this.addGoalRelation(cr);
		}
	}
	
	
	public void setExcludes(Goal g1, Goal g2, String s) {	
		if (!isExcludes(g1,g2)) {
			ExcludesRelation gr_new=new ExcludesRelation(g1,g2,s);
			this.addGoalRelation(gr_new);
		}
	}
	
	public void addExcludes(ExcludesRelation er) {
		if (!isExcludes(er.getFirstGoal(),er.getSecondGoal())) {
			this.addGoalRelation(er);
		}
	}
	
	
	public void setSupports(Goal g1, Goal g2, String s) {	
		if (!isSupports(g1,g2)) {
			SupportsRelation gr_new=new SupportsRelation(g1,g2,s);
			this.addGoalRelation(gr_new);
		}
	}
	
	public void addSupports(SupportsRelation sr) {
		if (!isSupports(sr.getFirstGoal(),sr.getSecondGoal())) {
			this.addGoalRelation(sr);
		}
	}
	
	public GoalModel process(GoalModel gm, GoalNode gn,Set<GoalRelation> grs) {
		Goal g=gn.getGoal();
		Set<GoalNode> children=gn.getChildren();
		Set<Goal> childgoals=gn.getChildGoals();
		Set<Goal> toprocess=new HashSet<Goal>(); // newly created auxiliary goals
		boolean childrenchanged=false;
		
		// find AND cliques
		Set<Goal> processed=new HashSet<Goal>(); // all children of g that belong to a clique
		for (GoalNode cn:children) {
			Goal c=cn.getGoal();
			if (processed.contains(c)) continue; // a clique contains c so skip
		
			// process AND clique
			Set<Goal> clique=this.findMaxNeighbourCluster(true, c, childgoals);
//			if (clique.size()>1 || clique.size()==children.size()) {
			if (clique.size()>1 ) {
				processed.addAll(clique);
				if (!clique.containsAll(childgoals)) {
					Goal gc=new Goal();
					gm.addGoal(gc);
					for (Goal g1: clique) {
						gm.changeParent(g1, g, gc);
					}
					gm.addChildParent(gc, g);
					toprocess.add(gc);
					childrenchanged=true;
				}
				
				Goal gp=gm.findParent(clique);
				for (Goal gc:clique) {
					for (GoalRelation gr:grs) {
						if (gr.isComplements()||gr.isExcludes()) {
							gr.replace(gc, gp);
						}
					}
				}
				grs.removeIf(gr -> gr.isReflexive());
				GoalNode gnp=gm.getGoalNode(gp);
				gnp.setAND();
			}
			else {
				// Clique of size 1 is skipped
			}
		}
		if (childrenchanged) {
			goals.addAll(toprocess);
			gm=process(gm,gn,grs); // process gn again since the children of gn have changed
		}		
		
		// find OR cliques
		for (GoalNode cn:children) {
			Goal c=cn.getGoal();
			if (processed.contains(c)) continue; // a clique contains c so skip

			// process OR clique
			Set<Goal> clique=this.findMaxNeighbourCluster(false, c, childgoals);


			if (clique.size()>1) {
				processed.addAll(clique);
				if (!clique.containsAll(childgoals)) {
					Goal gc=new Goal();
					gm.addGoal(gc);
					for (Goal g1: clique) {
						gm.changeParent(g1, g, gc);
					}
					gm.addChildParent(gc, g);
					toprocess.add(gc);
					childrenchanged=true;
				}
				
				Goal gp=gm.findParent(clique);
				for (Goal gc:clique) {
					for (GoalRelation gr:grs) {
						if (gr.isComplements()||gr.isExcludes()) {
							if (gr.touches(clique)) {
								gr.replace(gc, gp);
							}
						}
					}
				}
				grs.removeIf(gr -> gr.isReflexive());
				GoalNode gnp=gm.getGoalNode(gp);
				gnp.setOR();
			}
		}
		if (childrenchanged) {
			goals.addAll(toprocess);
			gm=process(gm,gn,grs); // process gn again since the children of gn have changed
		}
		return gm;
	}
	
	public GoalModel computeGoalMode(Vector<PlanItem> pi_milestones) {
		GoalModel gm=new GoalModel(name);
		for (PlanItem pi:pi_milestones) {
			gm.addGoal(pi.getGoal());
		}
		Set<GoalRelation> intransitive=this.computeTransitiveReduction();
		for (GoalRelation gr:intransitive) {
			gm.addGoal(gr.getFirstGoal());
			gm.addGoal(gr.getSecondGoal());
			gm.addChildParent(gr.getFirstGoal(), gr.getSecondGoal());
		}
		gm.computeRoot();
		Vector <GoalNode> order=gm.reverseTopoSort();		
		for (GoalNode gn:order) {
			gm=this.process(gm, gn, grs);
		}
		return gm;
	}
	
	
	// return true if n1 and n2 are neighbours and n1 and n2 share the same neighbours
	public boolean haveSameNeighbours(Goal n1, Goal n2, boolean complements) {
		if (complements) {
			if (!isComplements(n1,n2)) return false;
		}
		else {
			if (!isExcludes(n1,n2)) return false;
		}
		for (Goal g:goals) {
			if (g.equals(n1)) continue;
			if (g.equals(n2)) continue;
			if (complements) {
				if (isComplements(g,n1)!=isComplements(g,n2)) { 
					return false;
				}
			}
			else {
				if (isExcludes(g,n1)!=isExcludes(g,n2)) {
					return false;
				}
			}		
		}
		return true;
	}
	
	public void restrict(Vector<Goal> gs) {
		Set<GoalRelation> removeGrs=new HashSet<GoalRelation>();
		for (GoalRelation gr:grs) {
			if (!gr.isCoveredBy(gs)) removeGrs.add(gr);
		}
		grs.removeAll(removeGrs);
		goals.retainAll(gs);
	}
	
	public Set<Goal> findMaxNeighbourCluster(boolean complements, Goal gc, Set<Goal> childgoals){
		String n;
		if (complements) {
			n="AND";
		}
		else {
			n="XOR";
		}
		
		Set<Goal> clique=new HashSet<Goal>();
		clique.add(gc);
		for (Goal g:childgoals) {
			if (g.equals(gc)) continue;
			if (this.haveSameNeighbours(g, gc, complements)) {
				clique.add(g);
			}
			else {
			}
		}
		return clique;
	}
	

	
	
	
	public void print() {
		System.out.println("There are " + grs.size() + " goal relations:");
		System.out.println("There are " + goals.size() + " goals:");
	}
}

