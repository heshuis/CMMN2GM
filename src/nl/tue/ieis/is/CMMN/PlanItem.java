package nl.tue.ieis.is.CMMN;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.Matcher;


import org.jdom.Element;
import org.jdom.Namespace;

import nl.tue.ieis.is.GoalModel.Goal;


public class PlanItem extends CMMNElement{
	private String name;
	private String definitionRef;
	private Set<Criterion> entryRef;
	private Set<Criterion> exitRef;
	private Stage context;
	private Goal goal;
	private Set<PlanItem> conflicts;

	public PlanItem(Stage s){
		context=s;
		entryRef=new HashSet<Criterion>();
		exitRef=new HashSet<Criterion>();
		goal=null;
		conflicts = new HashSet<PlanItem>();
	}
	
	public void setName(String n){
		name=n; //.replace(" ","_");
	}

	public String getName(){
		return this.getPlanItemDefinition().getName();
	}
	
	public void setDefinitionRef(String n){
		definitionRef=n;
	}
	
	public String getDefinitionRef(){
		return definitionRef;
	}
	
	public Stage getContext(){
		return context;
	}
	
	public void setContext(Stage s){
		context=s;
	}
	
	public void addGoal(Goal g) {
		goal=g;
	}
	
	public Goal getGoal(){
		return goal;
	}
	
	public void resetConflicts() {
		conflicts.clear();
	}
	
	public void addConflict(PlanItem pi) {
		conflicts.add(pi);
	}
	
	public boolean hasConflict(PlanItem pi) {
		return conflicts.contains(pi);
	}
	
	public PlanItemDefinition getPlanItemDefinition(){
		return context.findPlanItemDefinition(definitionRef);
	}
	
	public boolean isSimilar(PlanItem piOther) {
		return this.getPlanItemDefinition().getName().equals(piOther.getPlanItemDefinition().getName());
	}
		
	public void addEntryRef(Criterion n){
		entryRef.add(n);
	}
	
	public void removeEntryRef(Criterion n) {
		entryRef.remove(n);
	}

	public void setEntryRefs(Set<Criterion> ers){
		entryRef=ers;
	}
	
	public Set<Criterion> getEntryRefs(){
		return entryRef;
	}
	
	public void addExitRef(Criterion n){
		exitRef.add(n);
	}
	
	public void removeExityRef(Criterion n) {
		exitRef.remove(n);
	}


	public Set<Criterion> getExitRefs(){
		return exitRef;
	}
	
	public void setExitRefs(Set<Criterion> ers){
		exitRef=ers;
	}
	
	public void print(int indent){
		for (int i=0;i<indent;i++) System.out.print(" ");
		System.out.println("Name:\t"+this.getPlanItemDefinition().getName());
		for (int i=0;i<indent;i++) System.out.print(" ");
		System.out.println("Id:\t"+getId());
		for (int i=0;i<indent;i++) System.out.print(" ");
		System.out.println("DefinitionRef: "+definitionRef);
		for (int i=0;i<indent;i++) System.out.print(" ");
		System.out.println("Context:\t"+context.getId());
		if (!entryRef.isEmpty()){
			for (int i=0;i<indent;i++) System.out.print(" ");
			System.out.println("begin of entryref criteria\t");
			for (Criterion se:entryRef){
				for (int i=0;i<indent;i++) System.out.print(" ");
				System.out.print(se.getSentryRef()+"\n");
			}
			for (int i=0;i<indent;i++) System.out.print(" ");
			System.out.println("end of entryref criteria\t");
		}
		if (!exitRef.isEmpty()){
			for (int i=0;i<indent;i++) System.out.print(" ");
			System.out.println("begin of exitref criteria\t");
			for (Criterion se:exitRef){
				for (int i=0;i<indent;i++) System.out.print(" ");
				System.out.print(se.getSentryRef()+"\n");
			}
			for (int i=0;i<indent;i++) System.out.print(" ");
			System.out.println("end of exitref criteria\t");
		}
	}
	
	public Element printElement(Namespace ns){
		Element planItem=new Element("planItem",ns);
		planItem.setAttribute("id", getId());
		planItem.setAttribute("definitionRef",getDefinitionRef());
		for (Criterion c:entryRef){
			Element el2=new Element("entryCriterion");
			el2.setNamespace(ns);
			el2.setAttribute("id",c.getId());
			el2.setAttribute("sentryRef",c.getSentryRef());
			planItem.addContent(el2);
		}
		for (Criterion c:exitRef){
			Element el2=new Element("exitCriterion");
			el2.setNamespace(ns);
			el2.setAttribute("id",c.getId());
			el2.setAttribute("sentryRef",c.getSentryRef());
			planItem.addContent(el2);
		}		
		return planItem;
	}
	
	
	public Set<PlanItem> getSourceReferences(){
		Set<PlanItem> pis=new HashSet();
		for (Criterion c:entryRef){
			Sentry se=this.getContext().getSentry(c.getSentryRef());
			pis.addAll(se.computeSourceReferences());
		}
		return pis;
	}
	
}
