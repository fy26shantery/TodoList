package com.example.todolist.controller;

import jakarta.servlet.http.HttpSession;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.todolist.dao.TodoDao;
import com.example.todolist.entity.Todo;
import com.example.todolist.form.TodoData;
import com.example.todolist.form.TodoQuery;
import com.example.todolist.repository.TodoRepository;
import com.example.todolist.service.TodoService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class TodoListController {
	private final TodoRepository todoRepository;
	private final TodoService todoService;
	private final HttpSession session;
	private final TodoDao todoDao;

	@GetMapping("/todo")
	public String showTodoList(Model model,
			@PageableDefault(page = 0, size = 5, sort = "id") Pageable pageable) {
		Page<Todo> todoPage = todoRepository.findAll(pageable);
		model.addAttribute("todoQuery", new TodoQuery());
		model.addAttribute("todoPage", todoPage);
		model.addAttribute("todoList", todoPage.getContent());
		session.setAttribute("todoQuery", new TodoQuery());
		return "todoList";
	}

	@PostMapping("/todo/query")
	public String queryTodo(@ModelAttribute TodoQuery todoQuery, BindingResult result,
			@PageableDefault(page = 0, size = 5) Pageable pageable, Model model) {
		//検索ボタンを押したときはデータが送信されるので＠Post
		Page<Todo> todoPage = null;
		if (todoService.isValid(todoQuery, result)) {
			todoPage = todoDao.findByJPQL(todoQuery, pageable);
			session.setAttribute("todoQuery", todoQuery);
			//検索ボタンを押した後にページリンクを押すと検索条件がきえてしまうので、セッションに保存しておく

			model.addAttribute("todoPage", todoPage);
			model.addAttribute("todoList", todoPage.getContent());
		} else {
			model.addAttribute("todoPage", null);
			model.addAttribute("todoList", null);
		}
		return "todoList";
	}

	@GetMapping("/todo/query")
	//URLでアクセスされたときはページ数の指定がないので、0ページ目は５件ずつ表示しておく
	//検索結果のページリンクを押されたときに＠Get
	public String queryTodo(@PageableDefault(page = 0, size = 5) Pageable pageable, Model model) {
		TodoQuery todoQuery = (TodoQuery) session.getAttribute("todoQuery");
		//ページリンクが押されたとき、page=1というページ番号しか送られてこないので、さっきPostでセッションに保存した検索条件をページ番号と合わせ、DBを再検索する

		Page<Todo> todoPage = todoDao.findByJPQL(todoQuery, pageable);
		model.addAttribute("todoQuery", todoQuery);
		model.addAttribute("todoPage", todoPage);
		model.addAttribute("todoList", todoPage.getContent());
		return "todoList";
	}

	@GetMapping("/todo/create")
	public String createTodo(Model model) {
		model.addAttribute("todoData", new TodoData());
		session.setAttribute("mode", "create");
		return "todoForm";
	}

	@PostMapping("/todo/create")
	public String createTodo(@ModelAttribute @Validated TodoData todoData, BindingResult result, Model model) {
		boolean isValid = todoService.isValid(todoData, result);
		if (!result.hasErrors() && isValid) {
			Todo todo = todoData.toEntity();
			todoRepository.saveAndFlush(todo);
			return "redirect:/todo";
		} else {
			return "todoForm";
		}
	}

	@PostMapping("/todo/cancel")
	public String cancel() {
		return "redirect:/todo";
	}

	@GetMapping("/todo/{id}")
	public String todoById(@PathVariable(name = "id") int id, Model model) {
		Todo todo = todoRepository.findById(id).get();
		model.addAttribute("todoData", todo);
		session.setAttribute("mode", "update");
		return "todoForm";
	}

	@PostMapping("/todo/update")
	public String updateTodo(@ModelAttribute @Validated TodoData todoData, BindingResult result, Model model) {
		boolean isValid = todoService.isValid(todoData, result);
		if (!result.hasErrors() && isValid) {
			Todo todo = todoData.toEntity();
			todoRepository.saveAndFlush(todo);
			return "redirect:/todo";
		} else {
			return "todoForm";
		}
	}

	@PostMapping("/todo/delete")
	public String deleteTodo(@ModelAttribute TodoData todoData) {
		todoRepository.deleteById(todoData.getId());
		return "redirect:/todo";
	}
}