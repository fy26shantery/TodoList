package com.example.todolist.dao;

import java.util.List;

import com.example.todolist.entity.Todo;
import com.example.todolist.form.TodoQuery;

public interface TodoDao {

	//JPQLによる検索
	List<Todo> findByJPQL(TodoQuery todoQuery);

}
