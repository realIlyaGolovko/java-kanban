package service;

class TaskSequence {
    private static int id;

    public static int getNextId() {
        id++;
        return id;
    }

    public static int getCurrentId() {
        return id;
    }
}