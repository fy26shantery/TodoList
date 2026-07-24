package com.example.todolist.controller;

import java.util.ArrayList;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
		session.setAttribute("todoPage", todoPage);
		session.setAttribute("todoList", todoPage.getContent());

		session.setAttribute("useSearch", 0); //検索フラグの初期化

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

	//全選択
	@GetMapping("/todo/delete/select/{currentPage}")
	public String selectDeleteData(@RequestParam(required = false) List<Integer> currentIdList,
			@PageableDefault(page = 0, size = 5, sort = "id") org.springframework.data.domain.Pageable pageable,
			Model md,
			@PathVariable int currentPage, RedirectAttributes redirectAttributes) {
		List<Integer> deleteIdList = new ArrayList<>();
		if (currentIdList != null) {
			for (int id : currentIdList) { //表示されているid全てを回収

				deleteIdList.add(id);
			}

		} else { //nullの場合
			deleteIdList = List.of();

		}

		Integer searchFlag = (Integer) session.getAttribute("useSearch");

		//検索機能をつかっているかどうか
		if (searchFlag != null && (Integer) searchFlag != 0) {
			if ((Integer) currentPage != null) {
				pageable = pageable.withPage(currentPage);

			}
			md.addAttribute("selectList", deleteIdList);
			todoService.dealQuery(pageable, md);

			return "todoList";
		} else { //検索を使っていない場合
			session.setAttribute("currentPage", currentPage); //今のtodoListのページを渡す
			redirectAttributes.addFlashAttribute("selectList", deleteIdList); //htmlに渡してチェックをつける
			return "redirect:/todo";
		}

	}

	//全解除
	@GetMapping("/todo/delete/noselect/{currentPage}")
	public String selectDeleteNoData(@RequestParam(required = false) List<Integer> currentIdList,
			@PageableDefault(page = 0, size = 5, sort = "id") org.springframework.data.domain.Pageable pageable,
			Model md,
			@PathVariable int currentPage, RedirectAttributes redirectAttributes) {
		List<Integer> deleteIdList = new ArrayList<>();
		if (currentIdList != null) {
			int size = currentIdList.size();
			for (int i = 0; i < size; i++) { //表示されているid分を回収

				deleteIdList.add(0); //どのidとも一致しない0をいれる
			}

		} else { //nullの場合
			deleteIdList = List.of();

		}

		Integer searchFlag = (Integer) session.getAttribute("useSearch");
		//検索機能をつかっているかどうか
		if (searchFlag != null && (Integer) searchFlag != 0) {
			if ((Integer) currentPage != null) {
				pageable = pageable.withPage(currentPage);

			}
			md.addAttribute("selectList", deleteIdList);
			todoService.dealQuery(pageable, md);

			return "todoList";
		} else { //検索を使っていない場合
			redirectAttributes.addFlashAttribute("selectList", deleteIdList); //htmlに渡してチェックをつける
			session.setAttribute("currentPage", currentPage); //今のtodoListのページを渡す
			return "redirect:/todo";
		}
	}

	//チェックボックスを用いての一括削除
	@PostMapping("/todo/delete/check")
	//複数送られてくる可能性があるのでList
	public String deleteTodo(@RequestParam(required = false) List<Integer> idList,
			RedirectAttributes redirectAttributes) {//required=falseでnull落ちを防ぐ
		if (idList != null) {
			for (int id : idList) {

				todoRepository.deleteById(id);
			}
		} else {
			//データベースに関係のない値なのでBindingResultは使わない
			redirectAttributes.addFlashAttribute("deleteCheckNull", "deleteCheckNull");
		}

		return "redirect:/todo";
	}

	@PostMapping("/todo/delete")
	public String deleteTodo(TodoData todoData) {
		todoRepository.deleteById(todoData.getId());
		return "redirect:/todo";
	}

	//todoList上部"検索"実行時
	@PostMapping("/todo/query")
	public String queryTodo(@ModelAttribute TodoQuery todoQuery,
			BindingResult result,
			@PageableDefault(page = 0, size = 5) org.springframework.data.domain.Pageable pageable,
			Model md) {

		todoService.dealQuery(todoQuery, result, pageable, md);

		return "todoList";
	}

	//ページリンクでの遷移時
	@GetMapping("/todo/query")
	public String queryTodo(@PageableDefault(page = 0, size = 5) Pageable pageable,
			Model md) {

		todoService.dealQuery(pageable, md);

		return "todoList";

	}

}
