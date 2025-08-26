package com.hmt.healix.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long doctorId;

    private String doctorName;
    private String specialization;
    private String qualification;
    private int experience;
    private String gender;
    @Column(unique = true,nullable = false)
    private String registerNumber;
    private String phoneNumber;
    private String address;
    private int consultingFee;

    @OneToOne
    @JoinColumn(name = "userId", referencedColumnName = "userId", nullable = false)
    private Users user;

}
