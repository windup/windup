package org.windup.examples.ejb.entitybean;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.NamedQuery;

@Entity
@DiscriminatorValue("DV") 
public class SubEntity extends SimpleEntity {
    private Long id;
}
