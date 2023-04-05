package nl.tue.ieis.is.CMMN;

import java.util.HashSet;
import java.util.Set;

import org.jdom.Element;
import org.jdom.Namespace;

public class PlanItemDefinition extends CMMNElement {
	private String name;
	private Stage context;
	
	public PlanItemDefinition(String n,Stage c){
		context=c; // null if planmodel (root)
		name=n;
	}
	
	public PlanItemDefinition clone() {
		return new PlanItemDefinition(this.getName(),this.getContext().clone());
	}
	
	public void setName(String n){
		name=n;
	}

	public String getName(){
		return name;
	}
	
	public Stage getContext(){
		return context;
	}
	
	public void setContext(Stage s){
		context=s;
	}

	
	public boolean isStage(){
		return false;
	}
	
	public boolean isMilestone(){
		return false;
	}
	
	public boolean isTask(){
		return false;
	}
		
	public boolean isEventListener(){
		return false;
	}
	
	
	public void print(int indent){
		for (int i=0;i<indent;i++) System.out.print(" ");
		System.out.println("Name:\t"+name);
		for (int i=0;i<indent;i++) System.out.print(" ");
		System.out.println("Id:\t"+getId());
		System.out.println(context.getCaseContext().getName());
	}
		
	public Element printElement(Namespace ns){// abstract class
		return null;
	}	
}
