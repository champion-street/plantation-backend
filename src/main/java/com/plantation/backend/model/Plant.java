package com.plantation.backend.model;

import lombok.*;

import javax.persistence.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Plant {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="id", updatable = false)
    long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "url")
    private String imageURL;

    @Column(name = "watering_cycle", nullable = false)
    private int wateringCycleInDays;

    @Column(name = "last_watered")
    private String lastWateringDate;

    private String wateringDeadline;

}
