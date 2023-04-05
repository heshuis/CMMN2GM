package nl.tue.ieis.is.CMMN;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.jdom.Element;
import org.jdom.Namespace;

public class Sentry extends CMMNElement {
	private Set<onPart> ops;
	private String ifPart;
	String name;
	
	public Sentry(){
		ops=new HashSet<onPart>();
		ifPart="";
	}
	
	public void setName(String s){
		name=s;
	}
	
	public String getName(){
		return name;
	}
	
		
	public void addOnPart(onPart op){
		ops.add(op);
	}
	
	public void addOnParts(Set<onPart> opsa){
		ops.addAll(opsa);
	}


	public void setGuard(String g){
		ifPart=g;
	}
	
	public String getGuard(){
		return ifPart;
	}
	
	public Set<onPart> getOnParts(){
		return ops;
	}
	
	public boolean conflicts(Sentry se) {
		String se_ifPart=se.getGuard();
		if (this.isNegationForm()) {
			if (ifPart.replaceAll("[()]|not", "").equals(se_ifPart.replaceAll("[()]", ""))) {
				return true;
			}
		}
		else if (se.isComparison()&&this.isComparison()) {
			if (this.isStrictComparison()) {
				if (ifPart.replaceAll("<", "").equals(se_ifPart.replaceAll(">=",""))) return true;
				if (ifPart.replaceAll(">", "").equals(se_ifPart.replaceAll("<=",""))) return true;

			}
			else if (this.isNonStrictComparison()) {
				if (ifPart.replaceAll("<=", "").equals(se_ifPart.replaceAll(">",""))) return true;
				if (ifPart.replaceAll(">=", "").equals(se_ifPart.replaceAll("<",""))) return true;
			}
		}
		else if (se.isEquality()&&this.isEquality()) {
			if (ifPart.replaceAll("=", "").equals(se_ifPart.replaceAll("!=",""))) return true;
			if (ifPart.replaceAll("!=", "").equals(se_ifPart.replaceAll("=",""))) return true;
		}
		return false;
	}
	
	public boolean isNegationForm() {
		if (ifPart.startsWith("!")|ifPart.startsWith("not")) return true;
		return false;
	}
	
	public boolean isEquality() {
		if (ifPart.contains("=")) return true;
		return false;
	}

	public boolean isComparison() {
		return isNonStrictComparison()||isStrictComparison();
	}
	public boolean isNonStrictComparison() {
		if (ifPart.contains("=<")) return true;
		if (ifPart.contains(">=")) return true;
		return false;
	}
	
	public boolean isStrictComparison() {
		if (ifPart.contains("<")) return true;
		if (ifPart.contains(">")) return true;
		return false;
	}
	
	public Set<PlanItem> computeSourceReferences(){
		Set<PlanItem> references=new HashSet();
		for (onPart o:ops){
			PlanItem pi=o.getSource();
			references.add(pi);
		}
		return references;
	}
	
	public boolean hasTrigger(PlanItem p) {
		for (onPart o:ops){
			PlanItem pi=o.getSource();
			if (pi.equals(p)) {
				return true;
			}
		}
		return false;
	}
	
	public void print(int indent){	
		System.out.println(this.getId());
		for (int i=0;i<indent;i++) System.out.print(" ");
		if (!ops.isEmpty()){
			System.out.println("OnParts");
			for (onPart e:ops){
				e.print(indent+2);
			}
		}
		else {
			System.out.println("no OnParts");
		}
		if (!ifPart.isEmpty()){
			System.out.println("Guard: "+ifPart);
		}
		else{
			System.out.println("Empty guard");
		}
	}
	
		
	Element printElement(Namespace ns){
		Element el=new Element("sentry");
		el.setNamespace(ns);
		el.setAttribute("id",getId());
		if (name!=null){
			el.setAttribute("name",name);
		}
		for (onPart e:ops){			
			Element el2=new Element("planItemOnPart");
			el2.setNamespace(ns);
			el2.setAttribute("id",e.getId());
			el2.setAttribute("sourceRef",e.getSourceRef());
			
			Element el3=new Element("standardEvent");
			el3.setNamespace(ns);
			if (e.getStandardEventText()!=null){
				el3.addContent(e.getStandardEventText());
			}				
			el.addContent(el2);
			el2.addContent(el3);
		}
		if (getGuard()!=null){
			Element el4=new Element("ifPart");
			el4.setNamespace(ns);
			Element el5=new Element("condition");
			el5.addContent(getGuard());
			el5.setNamespace(ns);
			el4.addContent(el5);
			el.addContent(el4);
		}
		return el;
	}
}
