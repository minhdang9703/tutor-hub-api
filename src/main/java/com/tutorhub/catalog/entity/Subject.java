package com.tutorhub.catalog.entity;
import jakarta.persistence.*;
@Entity @Table(name="subjects") public class Subject { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Integer id; private String name; private String slug; private boolean active; public Integer getId(){return id;} public String getName(){return name;} public String getSlug(){return slug;} public boolean isActive(){return active;} }
