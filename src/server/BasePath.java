package server;

public enum BasePath {
    TASK("/TASKS", "TASKS"), SUBTASK("/SUBTASKS", "SUBTASKS"),
    EPIC("/EPICS", "EPICS"), HISTORY("/HISTORY", "HISTORY"),
    PRIORITY("/PRIORITIZED", "PRIORITIZED");
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
