package com.realestate.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@RequiredArgsConstructor
public class EqtUsersErrors {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "timeCreated")
    private String timeCreated;

    @Column(name = "errorContent", length = 2000)
    private String errorContent;
}
