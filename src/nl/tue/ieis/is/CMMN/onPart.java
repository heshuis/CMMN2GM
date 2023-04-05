package nl.tue.ieis.is.CMMN;

public class onPart extends CMMNElement {

	private String sourceref;
	private PlanItem source;
	private boolean direction;
	private boolean isStage;
	private boolean isMilestone;
	private String standardEventText;
	private String name; // of planitemdef of source
	
	public onPart(){
		direction=true;
		isStage=false;
		isMilestone=false;
	}
	

	
	public void setSourceRef(String s){
		sourceref=s;
	}
	
	public String getSourceRef(){
		return sourceref;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String n) {
		name=n;
	}
	
	public void setSource(PlanItem t, String n){ // task , stge or milestone, referred to by sourceref
		source=t;
	    name=n;
	}
	
	public PlanItem getSource(){
		return source;
	}
	
	public void setDirection(boolean plus){
		direction=plus;
	}
	
	public void setMilestoneSource(){
		isMilestone=true;
	}
	
	public void setStageSource(){
		isStage=true;
	}
	
	public boolean isStage(){
		return isStage;
	}
	
	public boolean isMilestone(){
		return isMilestone;
	}
	
	public boolean isStageMilestone(){
		return isStage || isMilestone;
	}
	
	public boolean returnDirection(){
		return direction;
	}
	
	public void setStandardEventText(String s){
		this.standardEventText=s;
	}
	
	public String getStandardEventText(){
		return this.standardEventText;
	}
	
	public void print(int indent){
		for (int i=0;i<indent;i++) System.out.print(" ");
		System.out.println("Event with sourceref\t"+this.sourceref);
		System.out.println("StandardEventText:\t"+this.standardEventText);

		if (source!=null){
			System.out.println("Source:\t"+source.getName());
		}
		else{
			System.out.println("Empty source");
		}
	}
	
}
