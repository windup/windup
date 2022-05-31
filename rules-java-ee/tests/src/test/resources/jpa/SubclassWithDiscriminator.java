import javax.persistence.Entity;

@Entity
@DiscriminatorValue("DV")
public class SubclassWithDiscriminator extends BaseEntity {

}