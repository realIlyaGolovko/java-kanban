import model.Epic;
import model.SubTask;
import model.Task;
import model.TaskStatus;
import service.Managers;
import service.TaskManager;

public class Main {

    public static void main(String[] args) {
        System.out.println("Поехали!");
        TaskManager taskManager = Managers.getDefault();
        System.out.println("Тестирование сохранения данных");
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
        //Создание другого эпика с одной сабтаской
        Epic secondEpic = new Epic("2ndEpic", "2ndEpic", 6);
        taskManager.createEpic(secondEpic);
        SubTask thirdSubTask = new SubTask("3rdSubTask", "3rdSubTaskFor2ndEpic", 7,
                6);
        taskManager.createSubTask(thirdSubTask);
        System.out.println(taskManager.getTasks());
        System.out.println(taskManager.getSubTasks());
        System.out.println(taskManager.getEpics());

        System.out.println("Тестирование изменения данных");
        //Изменение  двух тасок
        Task updatedTask = new Task("1stTaskUpd", "1stTaskUpd", 1);
        updatedTask.setStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateTask(updatedTask);
        Task secondUpdatedTask = new Task("2ndTask", "2ndTask", 2);
        secondUpdatedTask.setStatus(TaskStatus.DONE);
        taskManager.updateTask(secondUpdatedTask);
        //Статус эпика не изменится, так как сабтаски не завершены
        Epic updatedEpic = new Epic("1stEpic", "1stEpic", 3);
        updatedEpic.setStatus(TaskStatus.DONE);
        updatedEpic.setName("1stEpicUpdated");
        taskManager.updateEpic(updatedEpic);
        //Изменение сабтасок
        SubTask updatedSubTask = new SubTask("1stSubTaskUpd", "1stSubTaskFor1stEpicUpd", 4,
                3);
        updatedSubTask.setStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateSubTask(updatedSubTask);
        SubTask updatedSecondSubTask = new SubTask("2ndSubTaskUpd", "2ndSubTaskFor1stEpicUpd", 5,
                3);
        updatedSecondSubTask.setStatus(TaskStatus.DONE);
        taskManager.updateSubTask(updatedSecondSubTask);
        SubTask thirdUpdatedSubTask = new SubTask("3rdSubTaskUpd", "3rdSubTaskFor2ndEpicUpd", 7
                , 6);
        thirdUpdatedSubTask.setStatus(TaskStatus.DONE);
        taskManager.updateSubTask(thirdUpdatedSubTask);
        System.out.println(taskManager.getTasks());
        System.out.println(taskManager.getSubTasks());
        System.out.println(taskManager.getEpics());

        System.out.println("Тестирование удаления данных");
        taskManager.deleteTask(1);
        taskManager.deleteSubTask(7);
        taskManager.deleteEpic(3);
        System.out.println(taskManager.getTasks());
        System.out.println(taskManager.getSubTasks());
        System.out.println(taskManager.getEpics());
    }
}
