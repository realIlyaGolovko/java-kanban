import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import service.InMemoryHistoryManager;
import service.InMemoryManager;

@DisplayName("Тесты менеджера задач в памяти.")
public class InMemoryManagerTest extends TaskManagerTest<InMemoryManager> {
    @Override
    @BeforeEach
    public void setUp() {
        //sut -> system under test
        sut = new InMemoryManager(new InMemoryHistoryManager());
    }
}
