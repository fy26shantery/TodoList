package com.example.todolist.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.todolist.entity.Todo;
import com.example.todolist.repository.TodoRepository;

import lombok.AllArgsConstructor;

@Controller
@AllArgsConstructor
public class TodolistController {
	private final TodoRepository todoRepository;

	@GetMapping("/todo")
	public String showTodoList(Model md) {
		//一覧を検索して表示する
		List<Todo> todoList = todoRepository.findAll();
		md.addAttribute("todoList", todoList);

		return "todoList";
	}

}
