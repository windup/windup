import javax.jws.WebService;

@WebService()
public class JavaxHello implements Hello {
    private String message = new String("Hello, ");
    public void JavaxHello() {}

    public String sayHello(String name) {
        return message + name + ".";
    }
}
