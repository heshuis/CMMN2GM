package nl.tue.ieis.is.CMMN;

public class Criterion extends CMMNElement {
	private String sentryRef;
	private PlanItem owner;
	
	public Criterion(PlanItem pi){
		owner=pi;
	}
	
	public PlanItem getOwningPlanItem(){
		return owner;
	}
	
	public void setSentryRef(String s){
		sentryRef=s;
	}
	
	public String getSentryRef(){
		return sentryRef;
	}
	
	
	public String generateSMV(){
		String s="test";
		return s;
	}
}
