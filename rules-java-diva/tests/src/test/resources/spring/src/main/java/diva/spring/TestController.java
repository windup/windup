package diva.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
public class TestController {

    private TestService testService;

    @Autowired
    public TestController(TestService testService) {
        this.testService = testService;
    }

    @GetMapping(path = "/modify")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public int modify(@PathVariable final int id) {
        try {
            return testService.modify(id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
