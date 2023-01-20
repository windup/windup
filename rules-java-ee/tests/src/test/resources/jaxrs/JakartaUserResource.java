import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
        
@Path("/users/{username}")
public class JakartaUserResource {

    @GET
    @Produces("text/xml")
    public String getUser(@PathParam("username") String userName) {
        return "foo";
    }
}