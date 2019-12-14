package application;

import Domain.Project;

public class ProjectRepository {

    private Project project;

    public ProjectRepository(Project project) {
        this.project = project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Project getProject() {
        return project;
    }
}
