package nl.tue.ieis.is.CMMN;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import nl.tue.ieis.is.GoalModel.Goal;
import nl.tue.ieis.is.GoalModel.GoalModel;
import nl.tue.ieis.is.GoalModel.GoalNetwork;

public class CMMN2GM {


	
	public static void analyzeMilestoneDependencies(CaseSchema cs, ArrayList<String> milestoneList) {
		Vector<PlanItem> milestones=cs.getMilestones();

		Vector<PlanItem> selected_milestones=new Vector<PlanItem>();
		Vector<Goal> selected_goals=new Vector<Goal>();
		GoalNetwork gn=new GoalNetwork(cs.getName());
		
		for (PlanItem pi:milestones) {
			String s=pi.getName();
			Goal g=gn.getGoal(s,true);
			pi.addGoal(g);
		}
		for (String s:milestoneList) {
			PlanItem pi=cs.findPlanItemByPlanItemDefName(s);
			if (pi==null) {
				System.out.println("Error: milestone " +s + " is not defined." );
				System.exit(0);
			}
			else {
				if (!pi.getPlanItemDefinition().isMilestone()) {
					System.out.println("Error: " +s + "    is not a milestone." );
					System.exit(0);
				}
				selected_milestones.add(pi);
				selected_goals.add(pi.getGoal());
			}
		}
		for (PlanItem pm1:milestones) {
			for (PlanItem pm2:milestones) {
				if (pm1.equals(pm2)) continue;
				Goal g1=pm1.getGoal();
				Goal g2=pm2.getGoal();
				if (cs.isMilestoneSentryConsistent(pm1,pm2)){
					// for each goal corresponding to s1, for each goal corresponding to s2, 
					if (!g1.equals(g2)) gn.setSupports(g1, g2, "milestone-sentry rule");
				}
				if (cs.isSentryConsistent(pm1,pm2)){
					if (!g1.equals(g2)) gn.setComplements(g1, g2, "sentry rule",false);
				}
				if (cs.isExclusiveSentryConsistent(pm1,pm2)){
					if (!g1.equals(g2)) gn.setExcludes(g1, g2, "exclusive sentry rule");
				}
				if (cs.isHierarchyConsistent(pm1,pm2)){
					if (!g1.equals(g2)) gn.setSupports(g1, g2, "hierarchy rule");
				}
				if (cs.isStageMilestoneConsistent(pm1,pm2)){
					if (!g1.equals(g2)) {
						gn.setComplements(g1, g2, "stage/task-milestone rule",true);
					}
				}
				if (cs.isStageOrTaskOutputConsistent(pm1,pm2)){
					if (!g1.equals(g2)) gn.setExcludes(g1, g2, "stage/task-output rule");
				}
				if (cs.hasConflict(pm1,pm2)){
					if (!g1.equals(g2)) gn.setExcludes(g1, g2, "conflicts rule");
				}
			}
		}
		gn.inferDependencies();
		gn.restrict(selected_goals);
		gn.analyzeInconsistencies();
		gn.preserveOriginalGoalRelations();
		gn.printGraph();
		GoalModel gm=gn.computeGoalMode(selected_milestones);
		gm.analyzeInconsistencies();
		gm.printGraph();
		cs.printStatistics();
		gm.printMetrics();
		gm.printAnalysisGoalRelations(gn,selected_goals);
	}
	
	public static void main(String args[]){
		if (args.length>0){
			try{
				
				String name=args[0].substring(0,args[0].indexOf(".cmmn"));
				CaseSchema cs=CMMNreader.doc2cmmn(CMMNreader.getDocument(args[0]),name);
				
				FileReader fr= new FileReader(args[1]);
				BufferedReader br = new BufferedReader(fr);

				ArrayList<String> milestoneList=new ArrayList<String>();

				String sCurrentLine;

				while ((sCurrentLine = br.readLine()) != null) {
			  		if (!sCurrentLine.equals("")){
			  			milestoneList.add(sCurrentLine);
			  		}
				}
				cs.analyzeConflicts();
	  			analyzeMilestoneDependencies(cs,milestoneList);
	  			br.close();
			}
			catch (IOException e) {
			// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		else {
			System.out.println("Usage: CMMN2GM <file.cmmn> <goal-milestone-correspondence.txt> ");
		}
	}
}
