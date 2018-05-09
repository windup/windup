
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.NamedQuery;

@Entity
@DiscriminatorValue("DV")
public class SubclassWithDiscriminator extends BaseEntity {

}