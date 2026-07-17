package com.example.todolist.controller;

import java.util.List;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.todolist.entity.Todo;
import com.example.todolist.form.TodoData;
import com.example.todolist.form.TodoQuery;
import com.example.todolist.repository.TodoRepository;
import com.example.todolist.service.TodoService;

import lombok.AllArgsConstructor;

@Controller
@AllArgsConstructor
public class TodoListController {
	private final TodoRepository todoRepository;
	private final TodoService todoService;//Todolist2で追加

	//Todo 一覧表示
	@GetMapping("/todo")
	public String showTodoList(Model model) {
		List<Todo> todoList = todoRepository.findAll();
		model.addAttribute("todoList", todoList);
		model.addAttribute("todoQuery", new TodoQuery());

		return "todoList";
	}

	//Todo入力フォーム表示（TodoList2で追加）
	//処理１　Todo 一覧画面（todoList.html）で新規追加リンクがクリックされたとき
	@GetMapping("/todo/create")
	public String createTodo(Model model) {

		model.addAttribute("todoData", new TodoData());

		return "todoForm";
	}

	//Todo追加処理（TodoList2で追加）
	//処理２　Todo 入力画面（todoForm.html）で登録ボタンがクリックされたとき
	@PostMapping("todo/create")
	public String createTodo(@ModelAttribute @Validated TodoData todoData, BindingResult result, Model model) {

		//エラーチェック
		boolean isValid = todoService.isValid(todoData, result);
		if (!result.hasErrors() && isValid) {
			//エラーなし
			Todo todo = todoData.toEntity();
			todoRepository.saveAndFlush(todo);
			return "redirect:/todo"; //絶対違うから後でしっかり確認　model

		} else {
			//エラーあり
			return "todoForm";
		}

	}

	@PostMapping("todo/cancel")
	public String cancel() {

		return "redirect:/todo";
	}

	private final HttpSession session;

	@GetMapping("/todo/{id}")
	public String todoById(@PathVariable(name = "id") int id, Model model) {
		Todo todo = todoRepository.findById(id).get();
		model.addAttribute("todoData", todo);
		session.setAttribute("mode", "update");

		return "todoForm";
	}

	@PostMapping("/todo/update")
	public String updateTodo(@ModelAttribute @Validated TodoData todoData, BindingResult result, Model model) {

		//エラーチェック
		boolean isValid = todoService.isValid(todoData, result);
		if (!result.hasErrors() && isValid) {
			//エラーなし
			Todo todo = todoData.toEntity();
			todoRepository.saveAndFlush(todo);

			return "redirect:/todo";

		} else {
			//エラーあり
			//model.addAttribute("todoData",todoData):
			return "todoForm";
		}

	}

	@PostMapping("/todo/delete")
	public String deleatTodo(@ModelAttribute TodoData todoData) {
		todoRepository.deleteById(todoData.getId());

		return "redirect:/todo";
	}

}
