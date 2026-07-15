package com.example.todolist.entity;

import java.sql.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;

import org.springframework.data.annotation.Id;

import lombok.Data;

@Entity
@Table(name = "todo")
@Data
public class Todo {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private String title;
	private Integer importance;
	private Integer uregency;
	private Date deadline;
	private String done;

}
