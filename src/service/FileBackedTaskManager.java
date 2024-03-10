package service;

import converter.Converter;
import converter.EpicConverter;
import converter.HistoryConverter;
import converter.SubTaskConverter;
import converter.TaskConverter;
import exception.ManagerLoadException;
import exception.ManagerSaveException;
import model.Epic;
import model.SubTask;
import model.Task;
import model.TaskType;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class FileBackedTaskManager extends InMemoryManager {
    private final File file;
    private final Map<TaskType, Converter> converters;
    private final String FILE_HEADER = "id,type,name,status,description,epic" + System.lineSeparator();

    public FileBackedTaskManager(File file) {
        super(Managers.getDefaultHistory());
        this.file = file;
        this.converters = Map.of(TaskType.TASK, new TaskConverter(),
                TaskType.SUBTASK, new SubTaskConverter(), TaskType.EPIC, new EpicConverter());
    }

    public void init() {
        loadFromFile();
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        manager.init();
        return manager;
    }

    @Override
    public int createTask(Task task) {
        int newTask = super.createTask(task);
        save();
        return newTask;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void deleteTasks() {
        super.deleteTasks();
        save();
    }

    @Override
    public void deleteTask(int taskId) {
        super.deleteTask(taskId);
        save();
    }

    @Override
    public Task getTask(int taskId) {
        Task task = super.getTask(taskId);
        save();
        return task;
    }

    @Override
    public int createSubTask(SubTask subTask) {
        int newSubTaskId = super.createSubTask(subTask);
        save();
        return newSubTaskId;
    }

    @Override
    public void updateSubTask(SubTask subTask) {
        super.updateSubTask(subTask);
        save();
    }

    @Override
    public void deleteSubTask(int subTaskId) {
        super.deleteSubTask(subTaskId);
        save();
    }

    @Override
    public SubTask getSubTask(int subTaskId) {
        SubTask subTask = super.getSubTask(subTaskId);
        save();
        return subTask;
    }

    @Override
    public void deleteSubTasks() {
        super.deleteSubTasks();
        save();
    }


    @Override
    public int createEpic(Epic epic) {
        int newEpic = super.createEpic(epic);
        save();
        return newEpic;
    }

    @Override
    public Epic getEpic(int epicId) {
        Epic epic = super.getEpic(epicId);
        save();
        return epic;
    }

    @Override
    public void deleteEpics() {
        super.deleteEpics();
        save();
    }

    @Override
    public void deleteEpic(int epicId) {
        super.deleteEpic(epicId);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    private void save() {
        try (final BufferedWriter writer = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
            writer.write(FILE_HEADER);
            for (Task task : getAllTasks()) {
                Converter converter = converters.get(task.getTaskType());
                writer.write(converter.toString(task));
                writer.newLine();
            }
            writer.newLine();
            writer.write(HistoryConverter.toString(getHistory()));
            writer.newLine();
        } catch (IOException | NullPointerException | IllegalArgumentException exception) {
            throw new ManagerSaveException("Error while saving tasks to file", exception);
        }
    }

    private TaskType parseType(String line) {
        return TaskType.valueOf(line.split(",")[1]);
    }


    private void loadFromFile() {
        int maxTaskId = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            reader.readLine(); // skip header
            while (true) {
                String line = reader.readLine();
                if (line.isEmpty()) {
                    break;
                }
                final Task task = converters.get(parseType(line)).fromString(line);
                final int taskId = task.getId();
                switch (task.getTaskType()) {
                    case TASK:
                        insertTask(task);
                        break;
                    case SUBTASK:
                        insertSubTask((SubTask) task);
                        break;
                    case EPIC:
                        insertEpic((Epic) task);
                        break;
                }
                if (maxTaskId < taskId) {
                    maxTaskId = taskId;
                }
            }
            for (SubTask subTask : getSubTasks()) {
                Epic epic = selectEpic(subTask.getEpicId());
                if (epic != null) {
                    epic.addSubTaskId(subTask.getId());
                }
            }
            String historyLine = reader.readLine();
            for (int id : HistoryConverter.fromString(historyLine)) {
                insertHistory(id);
            }
        } catch (IOException | NullPointerException | IllegalArgumentException exception) {
            throw new ManagerLoadException("Error while loading tasks from file", exception);
        }
        setId(maxTaskId);
    }
}