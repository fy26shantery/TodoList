package com.example.todolist.form;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.example.todolist.entity.Todo;

import lombok.Data;

@Data
//画面から送られてきた入力値を保管する箱があるクラス
//エンティティにこの箱を作らないのは、DBと画面入力は型が一致するとは限らないから
//DBはDate型やInteger型を求めてる
public class TodoData {
	private Integer id;

	//エラー対象の入力値がコントローラーに届く前に弾く
	@NotBlank
	private String title;

	@NotNull
	private Integer importance;

	@Min(0)
	private Integer urgency;

	private String deadline;
	private String done;

	//		 入力データからEntityを作って返す
	//画面の入力値をDB用に変換
	public Todo toEntity() {
		Todo todo = new Todo();
		todo.setId(id);
		todo.setTitle(title);
		todo.setImportance(importance);
		todo.setUrgency(urgency);
		todo.setDone(done);
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