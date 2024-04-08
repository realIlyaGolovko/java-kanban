package server;

public enum BasePath {
    TASK("/tasks", "tasks"), SUBTASK("/subtasks", "subtasks"),
    EPIC("/epics", "epics"), HISTORY("/history", "history"),
    PRIORITY("/prioritized", "prioritized");
    private final String root;
    private final String value;

    BasePath(String root, String value) {
        this.root = root;
        this.value = value;
    }

    public String getRoot() {
        return root;
    }

    public String getValue() {
        return value;
    }
}
