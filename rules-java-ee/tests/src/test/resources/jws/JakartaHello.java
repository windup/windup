import jakarta.jws.WebService;

@WebService()
public class JakartaHello implements Hello {
    private String message = new String("Hello, ");
    public void JakartaHello() {}
    public String sayHello(String name) {
        return message + name + ".";
    }
}
