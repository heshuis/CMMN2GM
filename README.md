# CMMN2GM
Synthesizing CMMN models into goal models

CMMN2GM computes a goal model from a CMMN schema, given a set of milestones from the CMMN schema that correspond to goals. The goal models are visualized as PNG file using dot (https://graphviz.org/)

CMMN2GM requires
- JDOM library: jdom-1.1.3.jar
- Graphviz.Java (https://github.com/jabbalaci/graphviz-java-api/blob/master/src/main/java/com/github/jabbalaci/graphviz/GraphViz.java)

Usage: java -jar nl.tue.ieis.is.CMMN.CMMN2GM <file.cmmn> <goal-milestone-correspondence.txt>, where <file.CMMN> is a CMMN file and <goal-milestone-correspondence.txt> is a textfile in which each line contains a milestone that corresponds to a goal.

Folder 'examples' contains sample CMMN and correspondence files.
