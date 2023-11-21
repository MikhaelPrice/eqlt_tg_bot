package com.realestate.entity;

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
public class EqtUsersChoices {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "username")
    private String username;

    @Column(name = "price")
    private String price;

    @Column(name = "type")
    private String type;

    @Column(name = "willingness")
    private String willingness;

    @Column(name = "registeredAt")
    private String registeredAt;

    @Column(name = "objectsFound")
    private String objectsFound;
}
