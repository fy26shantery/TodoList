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

	//list5で追加
	@PersistenceContext
	private EntityManager entityManager;
	TodoDaoImpl todoDaoImpl;

	@PostConstruct
	public void init() {
		todoDaoImpl = new TodoDaoImpl(entityManager);
	}

	//Todo一覧表示(Todolistで追加)
	@GetMapping("/todo")
	public String showTodoList(Model m, @PageableDefault(page = 0, size = 5, sort = "id") Pageable pageable) {
		//		Page<Todo> todoList = todoRepository.findAll(pageable);
		Page<Todo> todoPage = todoRepository.findAll(pageable);
		m.addAttribute("todoQuery", new TodoQuery());
		m.addAttribute("todoPage", todoPage);
		m.addAttribute("todoList", todoPage.getContent());
		session.setAttribute("todoQuery", new TodoQuery());

		return "todoList";
	}

	//Todo入力フォーム表示(Todolist2で追加)
	//【処理1】Todo一覧画面(todoList.html)で新規追加リンクがクリックされたとき
	@PostMapping("/todo/create/form")
	public String createTodo(Model m) {
		m.addAttribute("todoData", new TodoData());
		session.setAttribute("mode", "create");
		return "todoForm";
	}

	//Todo追加処理(Todolist2で追加)
	//【処理2】Todo入力画面(todoList.html)で登録ボタンがクリックされたとき
	@PostMapping("/todo/create/do")
	public String createTodo(@ModelAttribute @Validated TodoData todoData, BindingResult result, Model m) {

		//エラーチェック
		boolean isValid = todoService.isValid(todoData, result);
		if (!result.hasErrors() && isValid) {
			//エラーなし
			Todo todo = todoData.toEntity();
			todoRepository.saveAndFlush(todo);
			return "redirect:/todo";

		} else {
			//エラーあり
			return "todoForm";
		}
		//		m.addAttribute("todoList", new TodoDate());
		//		return "todoForm";
	}

	//Todo一覧へ戻る(Todolist2で追加)
	//【処理3】Todo入力画面(todoList.html)でキャンセル登録ボタンがクリックされたとき
	@PostMapping("/todo/cancel")
	public String cancel() {
		return "redirect:/todo";
	}

	//主キーで検索する
	@GetMapping("/todo/{id}")
	public String toById(@PathVariable(name = "id") int id, Model m) {
		Todo todo = todoRepository.findById(id).get();
		m.addAttribute("todoData", todo);
		session.setAttribute("mode", "update");
		return "todoForm";

	}

	//更新ボタン押したとき
	@PostMapping("/todo/update")
	public String updateTodo(@ModelAttribute @Validated TodoData todoData, BindingResult result, Model m) {

		//エラーチェック
		boolean isValid = todoService.isValid(todoData, result);
		if (!result.hasErrors() && isValid) {
			//エラーなし
			Todo todo = todoData.toEntity();
			todoRepository.saveAndFlush(todo);
			return "redirect:/todo";

		} else {
			//エラーあり
			return "todoForm";
		}

	}

	//キャンセルボタン
	@PostMapping("/todo/delete")
	public String deleteTodo(@ModelAttribute TodoData todoData) {
		todoRepository.deleteById(todoData.getId());
		return "redirect:/todo";
	}

	@PostMapping("/todo/query")
	public String queryTodo(@ModelAttribute TodoQuery todoQuery, BindingResult result,
			@PageableDefault(page = 0, size = 5) Pageable pageable, Model m) {

		Page<Todo> todoPage = null;
		if (todoService.isValid(todoQuery, result)) {
			//エラーがなければ検索
			todoPage = todoDaoImpl.findByJPQL(todoQuery, pageable);

			//入力された検索条件をsessionに保存
			session.setAttribute("tidiQuery", todoQuery);

			m.addAttribute("todoPage", todoPage);
			m.addAttribute("todoList", todoPage.getContent());

		} else {
			//エラーがあった場合検索
			m.addAttribute("todoPage", null);
			m.addAttribute("todoList", null);
		}

		return "todoList";

	}

	@GetMapping("/todo/query")
	public String queryTodo(@PageableDefault(page = 0, size = 5) Pageable pageable, Model m) {

		//sessionnに保存されている条件で検索
		TodoQuery todoQuery = (TodoQuery) session.getAttribute("todoQuery");
		Page<Todo> todoPage = todoDaoImpl.findByJPQL(todoQuery, pageable);

		m.addAttribute("todoQuery", todoQuery);
		m.addAttribute("todoPage", todoPage);
		m.addAttribute("todoList", todoPage.getContent());
		return "todoList";
	}
}
