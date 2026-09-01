package com.dowinn.rouletto.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

@Entity
@Table(name = "savedetails", schema = "roulette")
@Data
public class SaveDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "tableid", nullable = false)
    private String tableId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "value", columnDefinition = "jsonb")
    private Map<String, Object> value;

    @Column(name = "userid", nullable = false)
    private Long userId;

    @Column(name ="betorder")
    private Integer order;
}