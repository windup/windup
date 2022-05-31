package org.windup.examples.ejb.entitybean;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.NamedQuery;
import javax.persistence.NamedQueries;


@Entity
@Table(name = "SecondEntityTable")
@NamedQueries({@NamedQuery(name = "secondEntity.byName", query = "from SecondEntity where name=?"), @NamedQuery(name = "secondEntity.byState", query = "from SecondEntity where state=?")})
public class SecondEntity {
    private Long id;
    private String name;
    private String state;
}
