package nl.tue.ieis.is.GoalModel;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import com.github.jabbalaci.graphviz.GraphViz;

public class GoalModel {

	private Set<GoalNode> goalnodes;
	private String name;
	private GoalNode root;
	
	public GoalModel(String n) {
		goalnodes=new HashSet<GoalNode>();
		name=n;
		root=null;
	}
	
	public void addGoal(Goal g) {
		if (this.hasGoal(g)) return;
		GoalNode gn=new GoalNode(g,goalnodes.size());
		goalnodes.add(gn);
	}
	
	public boolean hasGoal(Goal g) {
		return getGoalNode(g)!=null;
	}
	
	public GoalNode getGoalNode(Goal g) {
		for (GoalNode gn:goalnodes) {
			if (gn.hasGoal(g)) return gn;
		}
		return null;
	}
	
	public Set<GoalNode> getGoalNodes(Set<Goal> gs) {
		Set<GoalNode> gns=new HashSet<GoalNode>();
		for (Goal g:gs) {
			gns.add(this.getGoalNode(g));
		}
		return gns;
	}
	
	public boolean hasParent(Goal g) {
		GoalNode gn=getGoalNode(g);
		return gn.hasParent();
	}
	
	public boolean hasChild(Goal g) {
		GoalNode gn=getGoalNode(g);
		return gn.hasChild();
	}
	
	public void addChildParent(Goal child, Goal parent) {
		GoalNode cn=this.getGoalNode(child);
		GoalNode pn=this.getGoalNode(parent);
		cn.addParentNode(pn);
		pn.addChildNode(cn);
	}
	
	public void changeParent(GoalNode child, GoalNode oldparent, GoalNode newparent) {
		child.removeParentNode(oldparent);
		child.addParentNode(newparent);
		oldparent.removeChildNode(child);
		newparent.addChildNode(child);
	}
	
	public void changeParent(Goal child, Goal oldparent, Goal newparent) {
		GoalNode gn1=this.getGoalNode(child);
		GoalNode gn2=this.getGoalNode(oldparent);
		GoalNode gn3=this.getGoalNode(newparent);
		gn1.removeParentNode(gn2);
		gn1.addParentNode(gn3);
		gn2.removeChildNode(gn1);
		gn3.addChildNode(gn1);
	}
	
	
	public void computeRoot() {
		int i=0;
		GoalNode rn=null;
		for (GoalNode gn:goalnodes) {
			if (!gn.hasParent()) {
				i++;
				rn=gn;
			}
		}	
		if (i==1) {
			root=rn;
		}
		else {
			Goal gr=new Goal("root",false);
			this.addGoal(gr);
			for (GoalNode gn:goalnodes) {
				if (gn.getGoal().equals(gr)) continue;
				if (!gn.hasParent()) {
					this.addChildParent(gn.getGoal(), gr);
				}
			}
			root=this.getGoalNode(gr);	
		}
		
	}
	
	public Vector<GoalNode> reverseTopoSort(){
		return reverseTopoSort(root);
	}
	

	public Vector<GoalNode> reverseTopoSort(GoalNode gn){
		Vector<GoalNode> vg=new Vector<GoalNode>();
		Set <GoalNode> hg=gn.getChildren();
		for (GoalNode g:hg) {
			vg.addAll(reverseTopoSort(g));
		}
		vg.add(gn);
		return vg;
	}
	
	public Goal findParent(Set<Goal> goals) {
		HashSet<Goal> gns=new HashSet<Goal>();
		for (Goal g:goals) {
			GoalNode gn=this.getGoalNode(g);
			gns.add(gn.getParentGoal());
		}
		if (gns.size()>1) System.out.println("Error: more than one parent!");
		return gns.iterator().next();
	}
		

	public void analyzeInconsistencies() {
		for (GoalNode gn:goalnodes) {
			if (gn.isAND()&&gn.isOR()) System.out.println("Goal "+ gn.getName() + " is inconsistent!");
		}
	}
	

	
	public void printGraph() {
		GraphViz gv = new GraphViz();
		gv.addln(gv.start_graph());
		gv.addln("edge[arrowhead=none]");
		for (GoalNode gn:goalnodes) {
			if (gn.isAND()||gn.isOR()) {
				gv.addln(gn.getId() + "[label=\""+ gn.getName()+ ":" +gn.getType()  +"\"]");
			}
			else {
				gv.addln(gn.getId() + "[label=\""+ gn.getName() +gn.getType()  +"\"]");				
			}
		}
		for (GoalNode gn:goalnodes) {
			gn.printGraph(gv);
		}
		gv.addln(gv.end_graph());
		gv.increaseDpi();   // 106 dpi
		String type = "png";	
		String repesentationType= "dot";
		File out = new File(name+"."+ type);   // Linux
		gv.writeGraphToFile( gv.getGraph(gv.getDotSource(), type, repesentationType), out );
	}
	
	public void print() {
		for (GoalNode gn:goalnodes) {
			if (gn.isRoot()) {
				gn.print(0);
			}
		}
	}
	
	public int computeDepth() {
		return root.computeDepth();
	}
	
	public int computeDegree() {
		return root.computeDegree();
		
	}
	
	public boolean isComplete() {
		return root.isComplete();
	}
	
	public int countCompositeWithSingleChild() {
		return root.countCompositeWithSingleChild();
	}
	
	
	public int countNrMilestones() {
		int c=0;
		for (GoalNode gn:goalnodes) {
			if (gn.getGoal().isMilestone()) c++;
		}
		return c;
	}
	
	public int countEdges() {
		return root.countEdges();

	}
	
	public void printMetrics() {
		System.out.println("Number of nodes:\t" + goalnodes.size());
		System.out.println("Number of edges:\t" + countEdges());
		System.out.println("Number of milestones:\t" + countNrMilestones());
		System.out.println("Depth of the tree:\t" + computeDepth());
		System.out.println("Degree of the tree:\t" + computeDegree());
		System.out.println("Number of composite nodes with single child:\t" + countCompositeWithSingleChild());
		System.out.println("Is complete?\t" + isComplete());
		
	}
}
