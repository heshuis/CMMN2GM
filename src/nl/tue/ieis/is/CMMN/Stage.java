package nl.tue.ieis.is.CMMN;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.ListIterator;
import java.util.Set;

import org.jdom.Element;
import org.jdom.Namespace;

public class Stage extends PlanItemDefinition {

	private Set<PlanItem> planItems;
	private Set<PlanItemDefinition> planItemDefs;
	private Set<Sentry> sentries;
	boolean isPlanModel;
	private CaseSchema caseContext;

	public Stage(String n,Stage c, CaseSchema cs) {
		super(n,c);
		planItems=new HashSet<PlanItem>();
		planItemDefs=new HashSet<PlanItemDefinition>();
		sentries=new HashSet<Sentry>();
		isPlanModel=false;
		caseContext=cs;
	}

	public Stage clone() {
		Stage s= (Stage)super.clone();
		for (PlanItem pi:s.planItems) {
		}
		return s;
	}
	
	public void setPlanModel(){
		isPlanModel=true;
	}
	
	public boolean isPlanModel(){
		return isPlanModel;
	}
	
	public void addChildPlanItem(PlanItem i){
		planItems.add(i);
		caseContext.addPlanItem(i);
	}
	
	public Set<PlanItem> getChildPlanItems(){
		return planItems;
	}
	
	public void addChildPlanItemDef(PlanItemDefinition i){
		planItemDefs.add(i);
		caseContext.addPlanItemDefinition(i);
	}
		
	public Set<PlanItemDefinition> getChildPlanItemDefs(){
		return planItemDefs;
	}
	
	public void addSentry(Sentry s){
		sentries.add(s);
		caseContext.addSentry(s);
	}
	
	public void removeSentry(Sentry s) {
		sentries.remove(s);
		caseContext.removeSentry(s);
	}
	
	public Set<Sentry> getSentries(){
		return sentries;
	}
	
	public Set<Sentry> getSentries(PlanItem pi){
		Set<Sentry>  ss = new HashSet();
		Set<Criterion> cs = new HashSet(pi.getEntryRefs());
		cs.addAll(pi.getExitRefs());
		for (Criterion c:cs){
			String sid=c.getSentryRef();
			for (Sentry s:sentries){
				if (ss.contains(s)) continue;
				if (s.getId().equals(sid)){
					ss.add(s);
				}
			}
		}
		return ss;
	}
	
	public void addSentries(Set<Sentry> ss){
		sentries.addAll(ss);
		caseContext.addSentries(ss);
	}
	
	public void removeSentries(Set<Sentry> ss){
		sentries.removeAll(ss);
		caseContext.removeSentries(ss);
	}
	
	public void removeAllSentries(){
		sentries.clear();
	}
	
	public Sentry getSentry(String id){
		for (Sentry s:sentries){
			if (s.getId().equals(id)){
				return s;
			}
		}
		return null;
	}
	
	// add this and all descendants to the case context of stage s
	public void setCaseContext(Stage s){
		for (PlanItemDefinition pid:planItemDefs){
			s.getCaseContext().addPlanItemDefinition(pid);
			if (pid.isStage()){
				((Stage)pid).setCaseContext(s);
			}
		}
		//repeat for planitems contained in this
		for (PlanItem pi:planItems){
			s.getCaseContext().addPlanItem(pi);
		}
	}
	
	public CaseSchema getCaseContext(){
		return caseContext;
	}
	
	public boolean isAtomic(){
		return planItems.isEmpty() && planItemDefs.isEmpty();
	}
	
	public boolean isCompound(){
		return !isAtomic();
	}
	
	public boolean isStage(){
		return true;
	}
	
	public boolean hasPlanItem(PlanItem p){
		for (PlanItem pi:planItems){
			if (pi.equals(p)){
				return true;
			}
		}
		for (PlanItem pi:planItems) {
			if (pi.getPlanItemDefinition().isStage()) {
				if (((Stage) pi.getPlanItemDefinition()).hasPlanItem(p)) {
					return true;
				}
			}
		}
		return false;
	}

	
	public PlanItemDefinition findPlanItemDefinition(String id){
		for (PlanItemDefinition pi:planItemDefs){
			if (pi.getId()!=null &&pi.getId().equals(id)){
				return pi;
			}
		}
		System.out.println("No planitemdef for string: " +id );
		System.out.println(" in context " + this.getId());

		if (!this.isPlanModel) return this.getContext().findPlanItemDefinition(id); // not found, so find in parent stage
		return null; // not found, no parent stage
	}
	
	public void print(int indent){
		for (int i=0;i<indent;i++) System.out.print(" ");
		System.out.println("Name:\t"+getName());
		for (int i=0;i<indent;i++) System.out.print(" ");
		System.out.println("Id:\t"+getId());
		if (!planItems.isEmpty()){
			System.out.println("begin of planitem children of stage "+this.getName());
			System.out.println(planItems.size() + " children ");
			for (PlanItem p:planItems){
				p.print(indent+3);
			}
			System.out.println("end of children of stage " + this.getName());
		}
		if (!sentries.isEmpty()){
			for (int i=0;i<indent;i++) System.out.print(" ");
			System.out.println("There are " + sentries.size() + " sentries ");
			System.out.println("begin of sentries\t");
			for (Sentry s:sentries){
				s.print(indent+3);
			}
			System.out.println("end of sentries\t");
		}
		if (!planItemDefs.isEmpty()){
			System.out.println("begin of planitemdef children of stage " + this.getName());
			System.out.println(planItemDefs.size() + " children ");

			for (PlanItemDefinition p:planItemDefs){
				p.print(indent+3);
			}
			System.out.println("end of children of stage " + this.getName());
		}
		else {
			System.out.println("no planitemdefs");
		}
	}
	
	public Element printElement(Namespace ns){
		Element el=null;
		if (isPlanModel()){
			el=new Element("casePlanModel");
		}
		else{
			el=new Element("stage");
		}
		el.setNamespace(ns);
		el.setAttribute("name",getName());
		el.setAttribute("id",getId());

		for (PlanItem pi:planItems){
			Element el2=pi.printElement(ns);
			el.addContent(el2);
		}
		for (PlanItemDefinition pid:planItemDefs){
			Element el2=null;
			if (pid instanceof Stage){
				Stage s=(Stage)pid;
				el2=s.printElement(ns);
			}
			if (pid instanceof Milestone){
				Milestone m=(Milestone)pid;
				el2=m.printElement(ns);
			}
			if (pid instanceof Task){
				Task t=(Task)pid;
				el2=t.printElement(ns);
			}
			if (pid instanceof Event){
				Event e=(Event)pid;
				el2=e.printElement(ns);
			}
			if (el2!=null){
				el.addContent(el2);
			}
			else{
				System.out.println("There is an abstract PlanItemDefinition!");
				pid.print(10);
			}
		}
		for (Sentry se:sentries){
			Element el2=se.printElement(ns);
			el.addContent(el2);
		}
		return el;
	}
	

}
