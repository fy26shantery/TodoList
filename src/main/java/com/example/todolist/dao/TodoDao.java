package com.example.todolist.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

import com.example.todolist.entity.Todo;
import com.example.todolist.form.TodoQuery;

public interface TodoDao {

	//JPQLによる検索
	Page<Todo> findByJPQL(TodoQuery todoQuery, @PageableDefault Pageable pageable);
}
