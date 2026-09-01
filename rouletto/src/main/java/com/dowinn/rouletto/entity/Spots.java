package com.dowinn.rouletto.entity;


import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "spots")
public class Spots {

    @Id
    @Column(name ="id" )
    private Integer id;

    @Column(name = "value")
    private String value;

    @Column(name="index")
    private Integer index;

    @Column(name = "spot")
    private Boolean spot;

    @Column(name = "active")
    private Boolean active;

    @Column(name = "desc")
    private String desc;

    @Column(name="variantid")
    private Integer variant;

    @OneToMany(targetEntity = PayOff.class,fetch = FetchType.EAGER,mappedBy ="spots" )
    private List<PayOff> payOff;
}
