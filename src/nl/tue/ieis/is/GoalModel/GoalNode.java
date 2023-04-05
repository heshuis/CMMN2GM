package nl.tue.ieis.is.GoalModel;

import java.util.HashSet;
import java.util.Set;

import com.github.jabbalaci.graphviz.GraphViz;

public class GoalNode {
	private Goal goal;
	private GoalNode parent;
	private Set<GoalNode> children;
	private String color;
	private boolean ANDtype;
	private boolean ORtype;	
	private String id;
	
	public GoalNode(Goal g, int i) {
		goal=g;
		parent=null;
		children=new HashSet<GoalNode>();
		ANDtype=false;
		ORtype=false;
		id="goal"+i;
	}
	
	public Goal getGoal() {
		return goal;
	}
	
	public String getId() {
		return id;
	}
	
	public String getName() {
		if (goal==null) return "(empty)";
		return goal.getName();
	}
	
	public boolean hasGoal(Goal g) {
		if (goal==null) return false;
		return goal.equals(g);
	}
	
	public void addParentNode(GoalNode gn) {
		if (parent!=null) {
			System.out.println("The goal model is not a tree, results are therefore inaccurate");
		}
		parent=gn;
	}
	
	public void removeParentNode(GoalNode gn) {
		parent=null;
	}
	
	public void addChildNode(GoalNode g) {
		children.add(g);
	}
	
	public Set<GoalNode> getChildren(){
		Set<GoalNode> cc=new HashSet<GoalNode>(); // because of modification of children list
		cc.addAll(children);
		return cc;
	}
	
	public Set<Goal> getChildGoals(){
		Set <Goal> childgoals=new HashSet<Goal>();
		for (GoalNode gn:children) {
			childgoals.add(gn.getGoal());
		}
		return childgoals;
	}
	
	public Goal getParentGoal() {
		return parent.getGoal();
	}
	
	public void removeChildNode(GoalNode gn) {
		children.remove(gn);
	}
	
	public boolean hasParent() {
		return parent!=null;
	}
	
	public boolean hasChild() {
		return !children.isEmpty();
	}
	
	public boolean hasChildren(Set<GoalNode> gns) {
		return gns.containsAll(children) && children.containsAll(gns);
	}
	
	public boolean isRoot() {
		return parent==null;
	}
	
	
	public void setAND() {
		if (!this.isAND()) ANDtype=true;	
	}
	
	
	public boolean isAND() {
		return ANDtype;
	}
	
	public void setOR() {
		if (!this.isOR()) ORtype=true;	
	}
	
	public boolean isOR() {
		return ORtype;
	}
	
	public String getType() {
		String type="";
		if (isOR()) type+="OR";
		if (isAND()) type+="AND";
		return type;
	}
	

	public int computeDegree() {
		int d=children.size();
		for (GoalNode c:children) {
			int dc=c.computeDegree();
			if (d<dc) {
				d=dc;
			}
		}
		return d;
	}
	
	public int computeDepth() {
		int d=0;
		for (GoalNode c:children) {
			int dc=c.computeDepth();
			if (d<dc) {
				d=dc;
			}
		}
		return d+1;
	}
	
	public int countEdges() {
		int e=children.size();
		for (GoalNode c:children) {
			e+=c.countEdges();
		}
		return e;
	}
	
	
	public boolean isComplete() {
		if ((!this.isAND()&&!this.isOR())&&children.size()>0) return false;
		for (GoalNode c:children) {
			if (!c.isComplete()) return false;
		}
		return true;
	}
	
	public int countCompositeWithSingleChild() {
		int count=0;
		if (children.size()==1) count++;
		for (GoalNode c:children) {
			count+=c.countCompositeWithSingleChild();
		}
		return count;
	}
	
	public boolean isTree() {
		for (GoalNode c:children) {
			if (!c.isTree()) return false;
		}
		return true;
	}
	
	public void printGraph(GraphViz gv) {
		for (GoalNode g:children) {
			gv.addln(this.getId() + " -> " + g.getId()+";");
		}
	}
	
	public void print(int l) {
		for (int i=0;i<l;i++) System.out.print("  ");
		System.out.print(this.getName()+" : ");
		if (isAND()) System.out.println("AND");
		if (isOR()) System.out.println("OR");
		System.out.println(children.size()+" children");
		for (GoalNode gn:children) {
			gn.print(l+3);
		}
		
	}
}
