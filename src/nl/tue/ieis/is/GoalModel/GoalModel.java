package nl.tue.ieis.is.GoalModel;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
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
	
	public void fixCompositeTypes(GoalNetwork nw) {
		root.fixCompositeTypes(nw);
//		for (GoalNode gn: goalnodes) {
//			if (gn.hasChild()) {
//				if (!gn.isOR()&&!gn.isAND()) {
//					int compl=0;
//					int excl=0;
//					Set<Goal> children=gn.getChildGoals();
//					for (Goal g1:children){
//						for (Goal g2:children){
//							if (g1==g2) continue;
//							if (nw.isComplements(g1, g2)){
//								compl++;
//							}
//							if (nw.isExcludes(g1, g2)){
//								excl++;
//							}
//						}
//						if ((compl>0)&&(excl==0)) {
//							gn.setAND();
//						}
//						if ((compl==0)&&(excl>0)) {
//							gn.setAND();
//						}
//					}
//				}
//			}
//		}
	}
	
	// assumption: this.computeDepth has been called before
	public GoalRelation findGoalRelation(Goal g1, Goal g2) {
		GoalNode gn1=getGoalNode(g1);
		GoalNode gn2=getGoalNode(g2);
		GoalRelation gr=null;
		if (isAncestor(gn1,gn2)) {
			return gr; // gr is computed in another iteration
//			gr=new SupportsRelation(g2,g1,""); // g1 is ancestor of g2, so g2 supports g1
		}
		if (isAncestor(gn2,gn1)) {
			gr=new SupportsRelation(g1,g2,""); // g2 is ancestor of g1, so g1 supports g2
		}
		else {
			GoalNode lca=this.findLowestCommonAncestor(gn1, gn2);
			if (lca.isAND()){
				gr=new ComplementsRelation(g1,g2,"",false); 
			}
			if (lca.isOR()){
				gr=new ExcludesRelation(g1,g2,""); 
			}
		}
		if (g2.getName().equals("Extended fix successful")&&(g1.getName().equals("Problem solved"))) {
			if (gr==null) {
				System.out.println("No goal relation found");
				System.exit(1);
			}
			if (gr.isComplements()) {
				System.out.println("EFS and PS are complementary" );
			}
			if (gr.isSupports()) {
				System.out.println("EFS and PS are supports" );
			}
			if (gr.isExcludes()) {
				System.out.println("EFS and PS are exclusive" );
			}
		}
		return gr;
	}
	
	// return true if gn1 is ancestor of gn2
	public boolean isAncestor(GoalNode gn1, GoalNode gn2) {
		if (gn1.equals(gn2)){
			return true;
		}
		else {
			if (gn2.equals(this.root)) // assuming gn2 can never be root
				return false;
			else {
				return isAncestor(gn1,gn2.getParentGoalNode());
			}
		}
	}
	
	// return lca of gn1 and gn2; algorithm based on https://www.baeldung.com/cs/tree-lowest-common-ancestor
	public GoalNode findLowestCommonAncestor(GoalNode gn1, GoalNode gn2) {
		while (gn1.getDepth()!=gn2.getDepth()) {
			if (gn1.getDepth()> gn2.getDepth()) {
				gn1=gn1.getParentGoalNode();
			}
			else {			
				gn2=gn2.getParentGoalNode();
			}
		}
		while (!gn1.equals(gn2)) {
			gn1=gn1.getParentGoalNode();
			gn2=gn2.getParentGoalNode();
		}
		return gn1;
	}
	
	
	// return true if g1 is ancestor of g2
	public boolean isAncestor(Goal g1, Goal g2) {
		GoalNode gn1=getGoalNode(g1);
		GoalNode gn2=getGoalNode(g2);
		return isAncestor(gn1,gn2);
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
		return root.computeDepth(0);
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
	    try (BufferedWriter writer = new BufferedWriter(new FileWriter(name+"-gm-stats.txt", false))) {

	    	writer.write("Number of nodes:\t" + goalnodes.size()+"\n");
	    	writer.write("Number of edges:\t" + countEdges()+"\n");
	    	writer.write("Number of milestones:\t" + countNrMilestones()+"\n");
	    	writer.write("Depth of the tree:\t" + computeDepth()+"\n");
	    	writer.write("Degree of the tree:\t" + computeDegree()+"\n");
	    	writer.write("Number of composite nodes with single child:\t" + countCompositeWithSingleChild()+"\n");
	    	writer.write("Is complete?\t" + isComplete()+"\n");
	    }
	    catch (Exception e) {
	        e.getStackTrace();
	    }
	}
	
	public void printAnalysisGoalRelations(GoalNetwork gn, Vector<Goal> selected_goals) {
		this.computeDepth();
		int gm_supports_match=0;
		int gm_complements_match=0;
		int gm_excludes_match=0;
		int total_gm_supports=0;
		int total_gm_complements=0;
		int total_gm_excludes=0;
		int gn_supports_match=0;
		int gn_complements_match=0;
		int gn_excludes_match=0;
		int total_gn_supports=0;
		int total_gn_complements=0;
		int total_gn_excludes=0;
		for (Goal g1:selected_goals) {
			for (Goal g2:selected_goals) {
				if (g1.equals(g2)) continue;
				GoalRelation gr=this.findGoalRelation(g1, g2);
				if (gn.isSupportsOriginal(g1, g2)){
					total_gn_supports++;
					if ((gr!=null) && (gr.isSupports())) {
						gn_supports_match++;
					}
				}
				if (gn.isComplementsOriginal(g1, g2)){
					total_gn_complements++;
					if ((gr!=null) && (gr.isComplements())) {
						gn_complements_match++;
					}
				}
				if (gn.isExcludesOriginal(g1, g2)){
					total_gn_excludes++;
//					System.out.println("Goal "+ g1.getName() + " is exclusive with " + g2.getName());
//					if (gr==null) {
//						System.out.println("gr is null");
//					}
//					
					
					if ((gr!=null) && (gr.isExcludes())) {
						gn_excludes_match++;
					}
				}

				if (gr==null) continue;
				if (gr.isSupports()) {
					total_gm_supports++;

//					System.out.println("Goal "+ g1.getName() + " supports " + g2.getName());
					if (gn.isSupportsOriginal(g1, g2)){
						gm_supports_match++;
					}
				}
				if (gr.isComplements()) {
//					System.out.println("Goal "+ g1.getName() + " complements " + g2.getName());
					total_gm_complements++;

					if (gn.isComplementsOriginal(g1, g2)){
						gm_complements_match++;
					}
				}
				if (gr.isExcludes()) {
//					System.out.println("Goal "+ g1.getName() + " is exclusive with " + g2.getName());
					total_gm_excludes++;
					if (gn.isExcludesOriginal(g1, g2)){
						gm_excludes_match++;
					}
				}
				
			}
		}
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(name+"-analysis-goalrelations-stats.txt", false))) {

//	    	writer.write("Number of support relations in goal model / present in CMMN model:\t" +total_gm_supports+"/"+gm_supports_match+"\n");
//	    	writer.write("Number of complements relations in goal model / present in CMMN model:\t" +total_gm_complements+"/"+ gm_complements_match+"\n");
//	    	writer.write("Number of excludes relations in goal model / present in CMMN model:\t" + total_gm_excludes+"/"+gm_excludes_match+"\n");
	    	writer.write("Number of support relations in CMMN model / present in goal model:\t" +total_gn_supports+"/"+ gn_supports_match+"\n");
	    	writer.write("Number of complements relations in CMMN model / present in goal model:\t" + total_gn_complements+"/"+gn_complements_match+"\n");
	    	writer.write("Number of excludes relations in CMMN model / present in goal model :\t" + total_gn_excludes+"/"+gn_excludes_match+"\n");

	    }
	    catch (Exception e) {
	        e.getStackTrace();
	    }

	}
}
	

	
