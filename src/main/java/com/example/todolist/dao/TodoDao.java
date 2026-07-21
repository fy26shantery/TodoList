package com.example.todolist.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.todolist.entity.Todo;
import com.example.todolist.form.TodoQuery;

public interface TodoDao {
	// JPQLでDB検索する
	Page<Todo> findByJPQL(TodoQuery todoQuery, Pageable pageable); //検索条件と何ページ目のデータを何件取得するのかの情報
	//ページネーションするために件数と、ページ数を持ってるPageオブジェクトを返すようにする
}