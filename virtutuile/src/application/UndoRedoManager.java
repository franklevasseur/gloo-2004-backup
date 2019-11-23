package application;

import utils.CircularStack;

public class UndoRedoManager {

    private static int allowedUndo = 20;
    private CircularStack<ProjectDto> undoStack = new CircularStack<>(allowedUndo);
    private CircularStack<ProjectDto> redoStack = new CircularStack<>(allowedUndo);

    public void justDoIt(ProjectDto project) {
        undoStack.add(project);
        redoStack.clear();
    }

    public ProjectDto undo() {
        ProjectDto project = undoStack.pop();
        redoStack.add(project);
        return undoStack.next();
    }

    public ProjectDto redo() {
        ProjectDto project = redoStack.pop();
        undoStack.add(project);
        return undoStack.next();
    }

    public boolean undoAvailable() {
        return !this.undoStack.isEmpty();
    }

    public boolean redoAvailable() {
        return !this.redoStack.isEmpty();
    }
}
