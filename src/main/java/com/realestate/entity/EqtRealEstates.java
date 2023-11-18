package com.realestate.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class EqtRealEstates {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "neighbourhood")
    private String neighbourhood;

    @Column(name = "project")
    private String project;

    @Column(name = "type")
    private String type;

    @Column(name = "willingness")
    private String willingness;

    @Column(name = "price")
    private String price;

    @Column(name = "size")
    private String size;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "picture1")
    private String picture1;

    @Column(name = "picture2")
    private String picture2;

    @Column(name = "picture3")
    private String picture3;
}
