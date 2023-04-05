package nl.tue.ieis.is.CMMN;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;


public class CaseSchema {

	private String name;
	private String id;
	private Stage planModel;
	private Set<Sentry> sentries; // copy of sentries stored in each stage
	private Set<PlanItem> planitems;
	private Set<PlanItemDefinition> planitemdefs;

	
	public CaseSchema(String n){
		name=n;
		sentries=new HashSet<Sentry>();
		planitems=new HashSet<PlanItem>();
		planitemdefs=new HashSet<PlanItemDefinition>();
	}
	
	public void setName(String n){
		name=n;
	}
	
	public String getName(){
		return name;
	}
	
	public void SetID(String ids){
		id=ids;
	}
	
	public void generateID() {
		id="_"+UUID.randomUUID().toString().replace("-","");

	}
	
	public void setPlanModel(Stage s){
		planModel=s;
	}
	
	public Stage getPlanModel(){
		return planModel;
	}
	
	public Set<Sentry> getSentries(){
		return sentries;
	}
	
	public Set<PlanItem> getPlanItems(){
		return planitems;
	}
	
	
	public Vector<PlanItem> getMilestones(){
		Vector<PlanItem> milestones=new Vector<PlanItem>();
		for (PlanItem pi:planitems) {
			if (pi.getPlanItemDefinition().isMilestone()) 
				milestones.add(pi);
		}
		return milestones;
	}

	public Set<PlanItem> getPlanItems(PlanItemDefinition pid){
		Set <PlanItem> pis=new HashSet<PlanItem>();
		for (PlanItem pi:planitems){
			if (pi.getPlanItemDefinition().equals(pid)){
				pis.add(pi);
			}
		}
		return pis;
	}

	public void addPlanItem(PlanItem pi){
		if (!planitems.contains(pi)) planitems.add(pi);
	}
	

	public void addPlanItemDefinition(PlanItemDefinition pi){
		if (!planitemdefs.contains(pi)) planitemdefs.add(pi);
	}

	public Set<PlanItemDefinition> getPlanItemDefinitions(){
		return planitemdefs;
	}
	
	public void printPlanItems(){
		System.out.println("FINAL PLANITEMS"+planitems.size());
	}
	
	public Task findTask(String name){
		for (PlanItemDefinition pi:planitemdefs){
			if ((pi instanceof Task) && pi.getName()!=null &&pi.getName().equals(name)){
				return (Task)pi;
			}
		}		
		return null;
	}
	
	
	public void addSentry(Sentry s){
		sentries.add(s);
	}
	
	public void addSentries(Set<Sentry> ss){
		sentries.addAll(ss);
	}
	
	public void removeSentries(Set<Sentry> ss){
		sentries.removeAll(ss);
	}
	
	public void removeSentry(Sentry s) {
		sentries.remove(s);
	}
	
	
	public Sentry getSentry(String id){
		for (Sentry s:sentries){
			if (s.getId().equals(id)){
				return s;
			}
		}
		return null;
	}
	
	
	public void resolveSourceRefs(){
		for (Sentry s:sentries){
			Set<onPart> es=s.getOnParts();
			for (onPart e:es){
				String sr=e.getSourceRef();
				if (sr!=null){
					PlanItem pi=findPlanItemByID(sr);
					if (pi!=null){
						PlanItemDefinition pid=pi.getPlanItemDefinition();
						e.setSource(pi,pid.getName());
						if (e.isMilestone()){
							e.setMilestoneSource();
						}
						if (e.isStage()){
							e.setStageSource();
						}
					}
					else{
						System.out.println("Source event with ID " + sr+ " not defined");
					}
				}
			}
		}
	}
	
	public boolean findPlanItemDefinition(PlanItemDefinition pid){
		return this.planitemdefs.contains(pid);
	}

	public PlanItemDefinition findPlanItemDefinitionByID(String id){
		for (PlanItemDefinition pi:planitemdefs){
			if (pi.getId()!=null &&pi.getId().equals(id)){
				return pi;
			}
		}
		return null;
	}
	
	public PlanItemDefinition findPlanItemDefinitionByName(String n){
		for (PlanItemDefinition pi:planitemdefs){
			if (pi.getName()!=null &&pi.getName().equals(n)){
				return pi;
			}
		}
		return null;
	}
	
	public boolean findPlanItem(PlanItem pi){
		return this.planitems.contains(pi);
	}
	
	public PlanItem findPlanItemByPlanItemDefName(String name){
		for (PlanItem pi:planitems){
			PlanItemDefinition pid=this.findPlanItemDefinitionByID(pi.getDefinitionRef());
			String pidname=pid.getName();
			if (pidname==null) pidname="null";

			if (pid.getName()!=null &&pid.getName().equals(name)){
				return pi;
			}
		}
		return null;
	}
	
	

	public PlanItem findPlanItemByID(String id){
		for (PlanItem pi:planitems){
			if (pi.getId()!=null &&pi.getId().equals(id)){
				return pi;
			}
		}
		return null;
	}
	
	public Set<PlanItem> getSuccessors(PlanItem pi) {
		HashSet<PlanItem> pis=new HashSet<PlanItem>();
		for (PlanItem pi1:planitems) {
			Set<Criterion> ec1=pi1.getEntryRefs();
			for (Criterion c1:ec1) {
				Sentry s1=this.getSentry(c1.getSentryRef());
				if (s1.hasTrigger(pi)) {
					pis.add(pi1);
				}
			}
		}
		return pis;
	}
	
	public Set<PlanItem> getIndirectSuccessors(PlanItem pi) {
		Set<PlanItem> pis=this.getSuccessors(pi);
		HashSet<PlanItem> processed=new HashSet<PlanItem>();
		processed.add(pi);
		boolean change=true;
		while (change) {
			change=false;
			for (PlanItem pi1:pis) {
				if (processed.contains(pi1)) 
					continue; 
				else { 
					change=true;
					pis.addAll(this.getSuccessors(pi1));
					processed.add(pi1);
				}
			}
		}
		return pis;
	}
	
	// return indirect sucessors of pi that are not indirect successor of piOther
	public Set<PlanItem> getIndirectSuccessors(PlanItem pi,PlanItem piOther) {
		Set<PlanItem> piOthers=this.getIndirectSuccessors(piOther);
		Set<PlanItem> pis=this.getIndirectSuccessors(pi);
		pis.removeAll(piOthers);
		return pis;
	} 
	
	public boolean isIndirectSuccessor(PlanItem pi1, PlanItem pi2) {
		Set<PlanItem> succ=this.getSuccessors(pi1);
		if (succ.contains(pi2)) return true;
		for (PlanItem pi:succ) {
			if (isIndirectSuccessor(pi,pi2)) return true;
		}
		return false;
	}
	
	public boolean hasConflict(PlanItem pi1, PlanItem pi2) {
		return pi1.hasConflict(pi2)&&pi2.hasConflict(pi1);
	}
	
	public void analyzeConflicts() {
		for (PlanItem pi1:planitems) {
			pi1.resetConflicts();
		}
		for (PlanItem pi1:planitems) {
			for (PlanItem pi2:planitems) {
				if (pi1.equals(pi2)) continue;
				if (hasSentryConflict(pi1,pi2)) {
					Set<PlanItem> pi1Succ=this.getIndirectSuccessors(pi1, pi2);
					Set<PlanItem> pi2Succ=this.getIndirectSuccessors(pi2, pi1);
					for (PlanItem pi1s:pi1Succ) {
						for (PlanItem pi2s:pi2Succ) {
							if (pi1s.getPlanItemDefinition().isMilestone()&&pi2s.getPlanItemDefinition().isMilestone()){
								pi1s.addConflict(pi2s);
								pi2s.addConflict(pi1s);
							}
						}
					}
				}
			}
		}
	}
	
	public boolean hasSentryConflict(PlanItem pi1, PlanItem pi2) {
		Set<Criterion> ec1=pi1.getEntryRefs();
		for (Criterion c1:ec1) {
			Sentry s1=this.getSentry(c1.getSentryRef());
			Set<Criterion> ec2=pi2.getEntryRefs();
			for (Criterion c2:ec2) {
				Sentry s2=this.getSentry(c2.getSentryRef());
				if (s1.conflicts(s2)) {
					if (ec1.size()==1||ec2.size()==1) return true;
				}
			}
		}
		
		return false;	
	}

	public void printOwns() {
		for (PlanItem pi1:planitems) {
			for (PlanItem pi2:planitems) {
				if (owns(pi1,pi2)) {
					System.out.println(pi1.getName() + " owns " + pi2.getName());
				}
			}
		}
	}
	
	public boolean owns(PlanItem ps, PlanItem pm) {
		Set<Criterion> ec=pm.getEntryRefs();
		for (Criterion c:ec) {
			Sentry s=this.getSentry(c.getSentryRef());
			if (s.hasTrigger(ps)) return true;
		}
		return false;
	}
	
	public boolean isMilestoneSentryConsistent(PlanItem pm1, PlanItem pm2) {
		Set<Criterion> ec=pm2.getEntryRefs();
		for (Criterion c:ec) {
			Sentry s=this.getSentry(c.getSentryRef());
			if (s.hasTrigger(pm1)) return true;
		}
		return false;	
	}
	
	
	
	public boolean isSentryConsistent(PlanItem pm1, PlanItem pm2) {
		for (Sentry s:sentries) {
			if (s.hasTrigger(pm1)&&s.hasTrigger(pm2)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isExclusiveSentryConsistent(PlanItem pm1, PlanItem pm2) {
		for (PlanItem pi:planitems) {
			if (pi.equals(pm1)||pi.equals(pm2)) continue; 
			
			Set<Criterion> ec=pi.getEntryRefs();
			if (ec.size()>1) {
				boolean foundpm1=false;
				for (Criterion c:ec) {
					Sentry s=this.getSentry(c.getSentryRef());
					if (s.hasTrigger(pm1)&&!s.hasTrigger(pm2)) {
						foundpm1= true;
						
					}		
				}
				if (foundpm1) {
					for (Criterion c:ec) {
						Sentry s=this.getSentry(c.getSentryRef());
						if (s.hasTrigger(pm2)&&!s.hasTrigger(pm1)) {
							return  true;
						}
					}
				}
			}
		}
		return false;
	}
	
	
	
	public boolean isHierarchyConsistent(PlanItem pm1, PlanItem pm2) {
		for (PlanItem pi:planitems) {
			if (pi.getPlanItemDefinition().isStage()&&this.owns(pi, pm2)) {
				Stage s=(Stage)pi.getPlanItemDefinition();
				if (s.hasPlanItem(pm1))	return true;
			}
		}
		return false;
	}
	

	public boolean isStageMilestoneConsistent(PlanItem pm1, PlanItem pm2) {
		for (PlanItem pi:planitems) {
			if ((pi.getPlanItemDefinition().isStage()||pi.getPlanItemDefinition().isTask())&&this.owns(pi, pm2)) {
				Set<Criterion> ec=pi.getEntryRefs();
				for (Criterion c:ec) {
					Sentry se=this.getSentry(c.getSentryRef());
					if (se.hasTrigger(pm1)) {
						return true;
					}
				}		
			}
			if (pi.getPlanItemDefinition().isStage()&&this.owns(pi, pm2)) {
				Stage S=(Stage)pi.getPlanItemDefinition();
				if (S.hasPlanItem(pm1)) continue;
				for (PlanItem pi1:planitems) {
					if (S.hasPlanItem(pi1)) {
						Set<Criterion> ec=pi1.getEntryRefs();
						for (Criterion c:ec) {
							Sentry se=this.getSentry(c.getSentryRef());
							if (se.hasTrigger(pm1)) {
								return true;
							}
						}	
					}
				}
			}
		}
		return false;	
	}
	
	public boolean isStageOrTaskOutputConsistent(PlanItem pm1, PlanItem pm2) {
		for (PlanItem pi:planitems) {
			if ((pi.getPlanItemDefinition().isStage()||pi.getPlanItemDefinition().isTask())&&this.owns(pi,pm1)&&this.owns(pi, pm2)) {
				return true;
			}
		}
		return false;
	}
	
	
	

	

	public int countMilestones(){
		int count=0;
		for (PlanItemDefinition pid:planitemdefs){
			if (pid.isMilestone()){
				count++;
			}
		}
		return count;
	}
	
	public int countStages(){
		int count=0;
		for (PlanItemDefinition pid:planitemdefs){
			if (pid.isStage()){
				count++;
			}
		}
		return count;
	}
	
	public int countTasks(){
		int count=0;
		for (PlanItemDefinition pid:planitemdefs){
			if (pid.isTask()){
				count++;
			}
		}
		return count;
	}
	
	public int countEventListeners(){
		int count=0;
		for (PlanItemDefinition pid:planitemdefs){
			if (pid.isEventListener()){
				count++;
			}
		}
		return count;
	}
	
	
	
	public void printStatistics(){
		System.out.println("Case schema: " + this.getName());
		System.out.println("There are " + this.planitemdefs.size() + "  plan item definitions." );
		System.out.println("                   " + this.countStages() + "  stages." );
		System.out.println("                   " + this.countTasks() + "  tasks." );
		System.out.println("                   " + this.countMilestones() + "  milestones." );
		System.out.println("                   " + this.countEventListeners() + "  event listeners." );


		System.out.println("There are " + this.sentries.size() + "  sentries." );
	}

	
	
	public PlanItem findSimilarPlanItem(PlanItem piOther) {
		String otherName=piOther.getPlanItemDefinition().getName();

		for (PlanItem pi:planitems){ 
			PlanItemDefinition pid=this.findPlanItemDefinitionByID(pi.getDefinitionRef());
			if (pid.getName().equals(otherName)) return pi;
		}
		return null;
	}
	
	

	
	
	public Document printCMMN(){
		Namespace ns = Namespace.getNamespace("url");
        Namespace ns2 = Namespace.getNamespace("dc","http://www.omg.org/spec/CMMN/20151109/DC");
        Namespace ns3 = Namespace.getNamespace("di","http://www.omg.org/spec/CMMN/20151109/DI");
        Namespace ns4 = Namespace.getNamespace("cmmndi","http://www.omg.org/spec/CMMN/20151109/CMMNDI");
        Namespace ns5 = Namespace.getNamespace("cmmn","http://www.omg.org/spec/CMMN/20151109/MODEL");
        Namespace ns6 = Namespace.getNamespace("xsi","http://www.w3.org/2001/XMLSchema-instance");
        
        Document doc = new Document();
        Element def=new Element("definitions",ns5);
        def.setAttribute("id",id+"_definitions");
        def.addNamespaceDeclaration(ns2);
        def.addNamespaceDeclaration(ns3);
        def.addNamespaceDeclaration(ns4);
        def.addNamespaceDeclaration(ns5);
        def.addNamespaceDeclaration(ns6);
        doc.setRootElement(def);
        
        Element caseE=new Element("case",ns5);
        caseE.setAttribute("name",name);
        caseE.setAttribute("id",id);
        def.addContent(caseE);
        
        caseE.addContent(planModel.printElement(ns5));

        return doc;
	}
	
}
