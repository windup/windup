import javax.ejb.Stateless;


@Stateless(name = "SomeFancyEjb")
public class SomeFancyEjbBean {

    public String sayHello(String name) {
        return "Hello " + name;
    }

}