package utils;

import Domain.Project;

//classe qui permet de retourner des pair d'element util pour le mode d'inspection
public class pairResult {
    public Project project;
    public String message;

    public pairResult(Project pProject, String pMessage){
        this.project = pProject;
        this.message = pMessage;
    }
}
