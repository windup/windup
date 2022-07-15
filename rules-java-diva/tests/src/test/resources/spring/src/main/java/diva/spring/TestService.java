package diva.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TestService extends BaseService<TestEntity> {

    @Autowired
    public TestService(TestRepository repository) {
        super(repository);
    }

    @Transactional
    public int modify(long id) {
        TestEntity test = repository.findById(id).get();
        int counter = test.getCounter();
        test.setCounter(++counter);
        repository.save(test);
        return counter;
    }
}
