package com.example.todolist.form;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.example.todolist.entity.Todo;

import lombok.Data;

@Data
public class TodoQuery {

	private String title;
	private Integer importance;
	private Integer urgency;
	private String deadline;
	private String deadlineFrom;
	private String deadlineTo;
	private String done;

	public TodoQuery() {
		title = "";
		importance = -1;
		urgency = -1;
		deadlineFrom = "";
		deadlineTo = "";
		done = "";
	}

	/*
	 * 入力データからEntityを生成して返す
	 */
	public Todo toEntity() {
		Todo todo = new Todo();

		SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd");
		long ms;
		try {
			ms = sdFormat.parse(deadline).getTime();
			todo.setDeadline(new Date(ms));
		} catch (ParseException e) {
			todo.setDeadline(null);
		}

		return todo;
	}
}
