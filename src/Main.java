import model.Epic;
import model.SubTask;
import model.Task;
import service.Managers;
import service.TaskManager;

public class Main {

    public static void main(String[] args) {
        System.out.println("Поехали!");
        TaskManager taskManager = Managers.getDefault();
        System.out.println("Тестирование сохранения истории просмотров");
        //Создание двух новых тасок
        Task task = new Task("1stTask", "1stTask", 1);
        taskManager.createTask(task);
        Task secondTask = new Task("2ndTask", "2ndTask", 2);
        taskManager.createTask(secondTask);
        //Создание нового эпика с двумя сабтасками
        Epic epic = new Epic("1stEpic", "1stEpic", 3);
        taskManager.createEpic(epic);
        SubTask subTask = new SubTask("1stSubTask", "1stSubTaskFor1stEpic", 4, 3);
        taskManager.createSubTask(subTask);
        SubTask secondSubTask = new SubTask("2ndSubTask", "2ndSubTaskFor1stEpic", 5, 3);
        taskManager.createSubTask(secondSubTask);
        taskManager.createSubTask(secondSubTask);
        //Создание другого эпика
        Epic secondEpic = new Epic("2ndEpic", "2ndEpic", 7);
        taskManager.createEpic(secondEpic);


        taskManager.getTask(2);
        taskManager.getTask(2);
        taskManager.getTask(1);
        taskManager.getEpic(3);
        taskManager.getEpic(3);
        taskManager.getTask(1);
        taskManager.getEpic(7);
        taskManager.getSubTask(4);
        taskManager.getSubTask(4);
        taskManager.getSubTask(5);
        taskManager.getSubTask(6);
        System.out.println(taskManager.getHistory());
        taskManager.deleteTask(2);
        taskManager.deleteEpic(3);
        taskManager.deleteEpic(7);
        System.out.println(taskManager.getHistory());

    }
}
