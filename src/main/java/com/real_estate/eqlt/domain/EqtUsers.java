package com.real_estate.eqlt.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class EqtUsers {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "price")
    private String price;

    @Column(name = "region")
    private String region;

    @Column(name = "type")
    private String type;

    @Column(name = "readiness")
    private String readiness;

    @Column(name = "registeredAt", updatable = false, nullable = false)
    private String registeredAt;
}
