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
	private final TodoService todoService;
	private final HttpSession session;

	//Todo一覧表示(Todolistで追加)
	@GetMapping("/todo")
	public String showTodoList(Model m) {
		List<Todo> todoList = todoRepository.findAll();
		m.addAttribute("todoList", todoList);

		m.addAttribute("todoQuery", new TodoQuery());

		return "todoList";
	}

	//Todo入力フォーム表示(Todolist2で追加)
	//【処理1】Todo一覧画面(todoList.html)で新規追加リンクがクリックされたとき
	@GetMapping("/todo/create")
	public String createTodo(Model m) {
		m.addAttribute("todoData", new TodoData());
		session.setAttribute("mode", "create");
		return "todoForm";
	}

	//Todo追加処理(Todolist2で追加)
	//【処理2】Todo入力画面(todoList.html)で登録ボタンがクリックされたとき
	@PostMapping("/todo/create")
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
	public String queryTodo(@ModelAttribute TodoQuery todoQuery, BindingResult result, Model m) {

		List<Todo> todoList = null;
		if (todoService.isValid(todoQuery, result)) {
			//エラーがなければ検索
			todoList = todoService.doQuery(todoQuery);
		}
		m.addAttribute("todoList", todoList);
		return "todoList";

	}
}
