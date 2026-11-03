package com.tutorhub.catalog.entity;
import jakarta.persistence.*;
@Entity @Table(name="districts") public class District { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Integer id; private String city; private String name; public Integer getId(){return id;} public String getCity(){return city;} public String getName(){return name;} }
