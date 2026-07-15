package com.example.todolist.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.todolist.entity.Todo;
import com.example.todolist.repository.TodoRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class TodoListController {
	private final TodoRepository todoRepository;

	@GetMapping("/todo")
	public String showTodoList(Model m) {
		List<Todo> all = todoRepository.findAll();
		m.addAttribute("todoList", all);
		return "todoList";
	}
}
