import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
        
@Path("/users/{username}")
public class JavaxUserResource {

    @GET
    @Produces("text/xml")
    public String getUser(@PathParam("username") String userName) {
        return "foo";
    }
}