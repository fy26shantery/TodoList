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
	private final TodoService todoService;
	private final HttpSession session;

	@PersistenceContext
	private EntityManager entityManager;
	TodoDaoImpl todoDaoImpl;

	@PostConstruct
	public void init() {
		todoDaoImpl = new TodoDaoImpl(entityManager);
	}

	//Form画面のidリンクがクリックされたとき
	@GetMapping("/todo/{id}")
	public String todoById(@PathVariable(name = "id") int id, Model model) {
		Todo todo = todoRepository.findById(id).get();
		model.addAttribute("todoData", todo);
		session.setAttribute("mode", "update");
		return "todoForm";
	}

	//ToDo一覧表示
	@GetMapping("/todo")
	public String showTodoList(Model model, @PageableDefault(page = 0, size = 5, sort = "id") Pageable pageable) {

		Page<Todo> todoPage = todoRepository.findAll(pageable);

		model.addAttribute("todoQuery", new TodoQuery());
		model.addAttribute("todoPage", todoPage);
		model.addAttribute("todoList", todoPage.getContent());
		session.setAttribute("todoQuery", new TodoQuery());

		return "todoList";
	}

	//ToDo入力フォーム表示
	//【処理１】ToDo一覧画面（todoList.html）で［新規追加］リンクがクリックされたとき
	@PostMapping("/todo/create/form")
	public String createTodo(Model model) {
		model.addAttribute("todoData", new TodoData());
		session.setAttribute("mode", "create");
		return "todoForm";
	}

	//ToDo追加処理
	//【処理２】ToDo入力画面（todoForm.html)で［登録］ボタンがクリックされたとき
	@PostMapping("/todo/create/do")
	public String createTodo(@ModelAttribute @Validated TodoData todoData, BindingResult result,
			Model model) {
		//エラーチェック
		boolean isValid = todoService.isValid(todoData, result);
		if (!result.hasErrors() && isValid) {
			//エラーなし
			Todo todo = todoData.toEntity();
			todoRepository.saveAndFlush(todo);
			return "redirect:/todo";
		} else {
			//エラーあり
			//model.addAttribute("todoData",todoData);
			return "todoForm";
		}
	}

	//入力画面で[更新]ボタンがクリックされたとき
	@PostMapping("/todo/update")
	public String updateTodo(@ModelAttribute @Validated TodoData todoData,
			BindingResult result,
			Model model) {

		//エラーチェック
		boolean isValid = todoService.isValid(todoData, result);
		if (!result.hasErrors() && isValid) {
			//エラーなし
			Todo todo = todoData.toEntity();
			todoRepository.saveAndFlush(todo);
			return "redirect:/todo";
		} else {
			//エラーあり
			//model.addAttribute("todoData", todoData);
			return "todoForm";
		}
	}

	//［削除］ボタンがクリックされたとき
	@PostMapping("/todo/delete")
	public String deleteTodo(@ModelAttribute TodoData todoData) {
		todoRepository.deleteById(todoData.getId());
		return "redirect:/todo";
	}

	//ToDo一覧へ戻る
	//【処理３】ToDo入力画面で［キャンセル登録］ボタンがクリックされたとき
	@PostMapping("/todo/cancel")
	public String cancel() {
		return "redirect:/todo";

	}

	//フォームに入力された時点でToDoを検索
	@PostMapping("/todo/query")
	public String queryTodo(@ModelAttribute TodoQuery todoQuery,
			BindingResult result,
			@PageableDefault(page = 0, size = 5) Pageable pageable,
			Model model) {
		Page<Todo> todoPage = null;
		if (todoService.isValid(todoQuery, result)) {
			//エラーがなければ検索

			todoPage = todoDaoImpl.findByJPQL(todoQuery, pageable);

			//入力された検索条件をsessionに保存
			session.setAttribute("todoQuery", todoQuery);
			model.addAttribute("todoPage", todoPage);
			model.addAttribute("todoList", todoPage.getContent());
		} else {
			//エラーがあった場合
			model.addAttribute("todoPage", null);
			model.addAttribute("todoList", null);

		}

		return "todoList";
	}

	@GetMapping("/todo/query")
	public String queryTodo(@PageableDefault(page = 0, size = 5) Pageable pageable, Model model) {

		//sessionに保存されている条件で検索
		TodoQuery todoQuery = (TodoQuery) session.getAttribute("todoQuery");
		Page<Todo> todoPage = todoDaoImpl.findByJPQL(todoQuery, pageable);

		model.addAttribute("todoQuery", todoQuery);//検索条件表示用
		model.addAttribute("todoPage", todoPage); //page情報
		model.addAttribute("todoList", todoPage.getContent());//検索結果

		return "todoList";
	}
}
