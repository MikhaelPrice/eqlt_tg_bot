package com.real_estate.eqlt.domain;

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
public class RealEstates {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "price", updatable = false, nullable = false)
    private String price;

    @Column(name = "place", updatable = false)
    private String place;

    @Column(name = "description", updatable = false)
    private String description;

    @Column(name = "rooms", updatable = false)
    private String rooms;

    @Column(name = "square", updatable = false)
    private String square;

}
