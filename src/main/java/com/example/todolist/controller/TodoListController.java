package com.example.todolist.controller;

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

import com.example.todolist.dao.TodoDaoImpl;
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
	private final TodoService todoService;//Todolist2で追加

	//Todo 一覧表示
	@GetMapping("/todo")
	public String showTodoList(Model model, @PageableDefault(page = 0, size = 5, sort = "id") Pageable pageable) {
		Page<Todo> todoPage = todoRepository.findAll(pageable);
		model.addAttribute("todoQuery", new TodoQuery());
		model.addAttribute("todoPage", todoPage);
		model.addAttribute("todoList", todoPage.getContent());
		session.setAttribute("todoQuery", new TodoQuery());

		//		model.addAttribute("todoList", todoList);
		//		model.addAttribute("todoQuery", new TodoQuery());

		return "todoList";
	}

	//Todo入力フォーム表示（TodoList2で追加）
	//処理１　Todo 一覧画面（todoList.html）で新規追加リンクがクリックされたとき
	@PostMapping("/todo/create/form")
	public String createTodo(Model model) {

		model.addAttribute("todoData", new TodoData());

		return "todoForm";
	}

	//Todo追加処理（TodoList2で追加）
	//処理２　Todo 入力画面（todoForm.html）で登録ボタンがクリックされたとき
	@PostMapping("/todo/create/do")
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

	@PostMapping("/todo/cancel")
	public String cancel() {

		return "redirect:/todo";
	}

	private final HttpSession session;

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
	public String deleteTodo(@ModelAttribute TodoData todoData) {
		todoRepository.deleteById(todoData.getId());

		return "redirect:/todo";
	}

	//フォームに入力された条件で、Todoを検索（Todolist4で追加、TodoList5で変更）
	@GetMapping("/todo/query") //次へを押すとゲットで送信されるから、それをキャッチ
	public String queryTodo1(@PageableDefault(page = 0, size = 5) Pageable pageable, Model model) {

		TodoQuery todoQuery = (TodoQuery) session.getAttribute("todoQuery");
		Page<Todo> todoPage = todoDaoImpl.findByCriteria(todoQuery, pageable);

		model.addAttribute("todoQuery", todoQuery);
		model.addAttribute("todoPage", todoPage);
		model.addAttribute("todoList", todoPage.getContent());

		return "todoList";
	}

	//	@PostMapping("/todo/query")
	//	public String queryTodo(@PageableDefault(page = 0, size = 5) Pageable pageable, Model model) {
	//
	//		TodoQuery todoQuery = (TodoQuery) session.getAttribute("todoQuery");
	//		Page<Todo> todoPage = todoDaoImpl.findByCriteria(todoQuery, pageable);
	//
	//		model.addAttribute("todoQuery", todoQuery);
	//		model.addAttribute("todoPage", todoPage);
	//		model.addAttribute("todoList", todoPage.getContent());
	//
	//		return "todoList";
	//	}

	@PostMapping("/todo/query")
	public String queryTodo(@ModelAttribute TodoQuery todoQuery, //画面の入力内容をここで受け取る！
			@PageableDefault(page = 0, size = 5) Pageable pageable, Model model) {

		// 次のページ（2ページ目など）に切り替えた時（GET通信）のためにセッションに保存する
		session.setAttribute("todoQuery", todoQuery);

		Page<Todo> todoPage = todoDaoImpl.findByCriteria(todoQuery, pageable);

		model.addAttribute("todoQuery", todoQuery);
		model.addAttribute("todoPage", todoPage);
		model.addAttribute("todoList", todoPage.getContent());

		return "todoList";
	}

	@GetMapping("/todo/{id}")
	public String todoById(@PathVariable(name = "id") int id, Model model) {
		Todo todo = todoRepository.findById(id).get();
		model.addAttribute("todoData", todo);
		session.setAttribute("mode", "update");

		return "todoForm";
	}

	@PersistenceContext
	private EntityManager entityManager;
	TodoDaoImpl todoDaoImpl;

	@PostConstruct
	public void init() {
		todoDaoImpl = new TodoDaoImpl(entityManager);
	}

}
