package com.plantation.backend.model;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Plant {

    @Id
    @GeneratedValue
    long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "url")
    private String imageURL;

    @Column(name = "watering_cycle")
    private int wateringCycleInDays;

    @Column(name = "last_watered")
    private String lastWateringDate;

    private String wateringDeadline;

}
