package com.example.todolist.controller;

import java.util.List;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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
import org.springframework.web.bind.annotation.RequestParam;

import com.example.todolist.dao.TodoDaoImpl;
import com.example.todolist.entity.Todo;
import com.example.todolist.form.TodoData;
import com.example.todolist.form.TodoQuery;
import com.example.todolist.repository.TodoRepository;
import com.example.todolist.service.TodoService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class TodolistController {
	private final TodoRepository todoRepository;
	private final TodoService todoService;
	private final HttpSession session;

	@PersistenceContext
	private EntityManager entityManager;
	TodoDaoImpl todoDaoImpl;

	@PostConstruct
	public void init() {
		todoDaoImpl = new TodoDaoImpl(entityManager);
	}

	@GetMapping("/todo")
	public String showTodoList(Model md,
			@PageableDefault(page = 0, size = 5, sort = "id") org.springframework.data.domain.Pageable pageable) {
		//一覧を検索して表示する
		Integer currentPage = (Integer) session.getAttribute("currentPage");
		Page<Todo> todoPage = null;
		//更新からの遷移の時、受け取った遷移前ページから始める
		if (currentPage != null) {
			Pageable updatePageable = pageable.withPage(currentPage);
			todoPage = todoRepository.findAll(updatePageable);
		} else {
			//更新からの遷移でないとき
			todoPage = todoRepository.findAll(pageable);
		}

		todoService.constitutePage(todoPage, md); //ページング数の指定
		md.addAttribute("todoQuery", new TodoQuery());
		md.addAttribute("todoPage", todoPage);
		md.addAttribute("todoList", todoPage.getContent());
		session.setAttribute("todoQuery", new TodoQuery());

		return "todoList";
	}

	//ToDo入力フォーム表示
	//[処理1]ToDo一覧画面(todoList)で新規追加がクリックされたとき
	@GetMapping("/todo/create/{currentPage}")
	public String createTodo(@PathVariable int currentPage, Model md) {
		session.setAttribute("currentPage", currentPage);
		md.addAttribute("todoData", new TodoData());
		session.setAttribute("mode", "create");
		return "todoForm";
	}

	//ToDo追加処理
	//[処理2]ToDo入力画面(todoForm)で登録ボタンがクリックされたとき
	@PostMapping("/todo/create")
	public String createTodo(@ModelAttribute @Validated TodoData todoData,
			BindingResult result, Model md) {
		//エラーチェック
		boolean isValid = todoService.isValid(todoData, result);
		if (!result.hasErrors() && isValid) {
			//エラーなし
			//session.setAttribute("currentPage", currentPage); //create遷移前のtodoListのページを渡す
			Todo todo = todoData.toEntity();
			todoRepository.saveAndFlush(todo);
			return "redirect:/todo";
		} else {
			//エラーあり
			return "todoForm";
		}
	}

	//ToDo処理一覧に戻る
	//[処理3]Todo入力画面でキャンセル登録がクリックされたとき
	@PostMapping("/todo/cancel")
	public String cancel() {
		//session.setAttribute("currentPage", currentPage); //create遷移前のtodoListのページを渡す
		return "redirect:/todo";
	}

	@GetMapping("/todo/{id}/{currentPage}")
	public String todoById(@PathVariable int id, @PathVariable int currentPage, Model md) {
		session.setAttribute("currentPage", currentPage); //create遷移前のtodoListのページを渡す
		Todo todo = todoRepository.findById(id).get();
		md.addAttribute("todoData", todo);
		session.setAttribute("mode", "update");
		return "todoForm";
	}

	@PostMapping("/todo/update")
	public String updateTodo(TodoData todoData,
			BindingResult result, Model md) {
		//エラーチェック
		boolean isValid = todoService.isValidForUpdate(todoData, result);
		if (!result.hasErrors() && isValid) {
			//エラーなし
			session.setAttribute("mode", "update");
			Todo todo = todoData.toEntity();
			todoRepository.saveAndFlush(todo);
			return "redirect:/todo";
		} else {
			//エラーあり
			return "todoForm";
		}

	}

	//チェックボックスを用いての一括削除
	@PostMapping("/todo/delete/check")
	//複数送られてくる可能性があるのでList
	public String deleteTodo(@RequestParam(required = false) List<Integer> idList) {//required=falseでnull落ちを防ぐ
		if (idList != null) {
			for (int id : idList) {

				todoRepository.deleteById(id);
			}
		}

		return "redirect:/todo";
	}

	@PostMapping("/todo/delete")
	public String deleteTodo(TodoData todoData) {
		todoRepository.deleteById(todoData.getId());
		return "redirect:/todo";
	}

	@PostMapping("/todo/query")
	public String queryTodo(@ModelAttribute TodoQuery todoQuery,
			BindingResult result,
			@PageableDefault(page = 0, size = 5) org.springframework.data.domain.Pageable pageable,
			Model md) {

		Page<Todo> todoPage = null;
		if (todoService.isValid(todoQuery, result)) {
			//エラーがなければ検索
			todoPage = todoDaoImpl.findByJPQL(todoQuery, pageable);

			session.setAttribute("todoQuery", todoQuery);

			todoService.constitutePage(todoPage, md); //ページング数の指定
			md.addAttribute("todoPage", todoPage);
			md.addAttribute("todoList", todoPage.getContent());
		} else {

			md.addAttribute("todoPage", null);
			md.addAttribute("todoList", null);

		}
		return "todoList";
	}

	@GetMapping("/todo/query")
	public String queryTodo(@PageableDefault(page = 0, size = 5) Pageable pageable,
			Model md) {

		//sessionに保存されている情報で検索
		TodoQuery todoQuery = (TodoQuery) session.getAttribute("todoQuery");
		Page<Todo> todoPage = todoDaoImpl.findByJPQL(todoQuery, pageable);

		todoService.constitutePage(todoPage, md); //ページング数の指定
		md.addAttribute("todoQuery", todoQuery);
		md.addAttribute("todoPage", todoPage);
		md.addAttribute("todoList", todoPage.getContent());

		return "todoList";

	}

}
