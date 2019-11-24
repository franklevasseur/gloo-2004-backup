package application;

import utils.CircularStack;

public class UndoRedoManager {

    private static int allowedUndo = 20;
    private CircularStack<ProjectDto> undoStack = new CircularStack<>(allowedUndo);
    private CircularStack<ProjectDto> redoStack = new CircularStack<>(allowedUndo);

    private CircularStack<Integer> debugUndoStack = new CircularStack<Integer>(allowedUndo);
    private CircularStack<Integer> debugRedoStack = new CircularStack<Integer>(allowedUndo);

    private static int num = 0;
    public void justDoIt(ProjectDto project) {
//        System.out.println("debug #" + num);
        debugUndoStack.add(num++);

        undoStack.add(project);
        redoStack.clear();
    }

    public ProjectDto undo() {
        ProjectDto project = undoStack.pop();

        Integer fuck = debugUndoStack.pop();
//        System.out.println("undo #" + fuck);
        debugRedoStack.add(fuck);

        redoStack.add(project);
        return undoStack.next();
    }

    public ProjectDto redo() {
        ProjectDto project = redoStack.pop();

        Integer fuck = debugRedoStack.pop();
//        System.out.println("undo #" + fuck);
        debugUndoStack.add(fuck);

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
