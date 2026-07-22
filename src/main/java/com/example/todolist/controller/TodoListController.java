package com.example.todolist.controller;

import java.util.List;

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

		// 戻り先URLを保存
		session.setAttribute("returnUrl", "/todo?page=" + pageable.getPageNumber());
		//ページ番号が入ってるURLをセッションに保存
		// ページリンクの表示範囲を決める
		setPageInfo(model, todoPage);
		return "todoList";
	}

	@PostMapping("/todo/query")
	//検索ボタンを押した時

	public String queryTodo(@ModelAttribute TodoQuery todoQuery, BindingResult result,
			@PageableDefault(page = 0, size = 5) Pageable pageable, Model model) {
		//設定された入力条件をTodoQueryとしてもらい、バリテーションしてからDBから検索し、表示

		Page<Todo> todoPage = null;
		if (todoService.isValid(todoQuery, result)) {
			todoPage = todoDao.findByJPQL(todoQuery, pageable);
			session.setAttribute("todoQuery", todoQuery);

			model.addAttribute("todoPage", todoPage);
			model.addAttribute("todoList", todoPage.getContent());

			// 戻り先URLを保存
			session.setAttribute("returnUrl", "/todo/query?page=" + pageable.getPageNumber());
			// ページリンクの表示範囲を決める
			setPageInfo(model, todoPage);
		} else {
			model.addAttribute("todoPage", null);
			model.addAttribute("todoList", null);
		}
		return "todoList";
	}

	@GetMapping("/todo/query")
	//検索後にページリンクを押したとき
	//ページリンクを押したときにURLと一緒に送られてくるのは何ページ目かという情報だけ
	//だからセッションに保存しておいた検索条件をtodoQueryから取る
	public String queryTodo(@PageableDefault(page = 0, size = 5) Pageable pageable, Model model) {
		TodoQuery todoQuery = (TodoQuery) session.getAttribute("todoQuery");
		Page<Todo> todoPage = todoDao.findByJPQL(todoQuery, pageable);
		model.addAttribute("todoQuery", todoQuery);
		model.addAttribute("todoPage", todoPage);
		model.addAttribute("todoList", todoPage.getContent());

		// 戻り先URLを保存
		session.setAttribute("returnUrl", "/todo/query?page=" + pageable.getPageNumber());
		setPageInfo(model, todoPage);
		return "todoList";
	}

	// ページリンクの前後2ページ分を計算する
	private void setPageInfo(Model model, Page<Todo> todoPage) {
		if (todoPage != null && todoPage.getTotalPages() > 0) {
			int currentPage = todoPage.getNumber();
			int startPage = Math.max(0, currentPage - 2);
			//引数の2つの数字を比較して、大きいほうを返す
			//ページ番号がマイナスにならないため
			int endPage = Math.min(todoPage.getTotalPages() - 1, currentPage + 2);
			//小さいほうを返す
			//最後のページを超えないように総ページ数　VS　現在のページ数＋２
			model.addAttribute("startPage", startPage);
			model.addAttribute("endPage", endPage);
		}
	}

	//セッションから戻り先を取得するメソッド
	private String getReturnUrl() {
		String returnUrl = (String) session.getAttribute("returnUrl");
		if (returnUrl != null) {
			return "redirect:" + returnUrl;
		} else {
			return "redirect:/todo";
		}
	}

	@GetMapping("/todo/create")
	//新規登録ボタンを押したとき、もしくはURLでアクセス
	//空のフォームを見せてもらうだけだから、Getマッピング
	public String createTodo(Model model) {
		model.addAttribute("todoData", new TodoData());
		session.setAttribute("mode", "create");
		return "todoForm";
	}

	@PostMapping("/todo/create")
	//新規登録入力画面で、登録ボタンを押したとき
	//DBを書き換える操作があるので、Postマッピング
	public String createTodo(@ModelAttribute @Validated TodoData todoData, BindingResult result, Model model) {
		//フォームクラスのアノテーションの入力チェックを詰められると同時に実行してる
		String mode = (String) session.getAttribute("mode");
		boolean isValid = todoService.isValid(todoData, result, mode);
		if (!result.hasErrors() && isValid) {
			Todo todo = todoData.toEntity();
			todoRepository.saveAndFlush(todo);
			//エンティティのオブジェクトをDBに実行と保存をする
			//IDが空ならINSERT、存在するならUPDATEのSQLを組み立て、Flush()で一気にDBに送信し、実行
			// 登録後は元のページへ戻る
			return getReturnUrl();
		} else {
			return "todoForm";
			//エラーメッセージを持ったまま、元の入力画面をフォワードで再表示
		}
	}

	@PostMapping("/todo/cancel")
	public String cancel() {
		// キャンセル時も元のページへ戻る
		return getReturnUrl();
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
		String mode = (String) session.getAttribute("mode");
		boolean isValid = todoService.isValid(todoData, result, mode);
		if (!result.hasErrors() && isValid) {
			Todo todo = todoData.toEntity();
			todoRepository.saveAndFlush(todo);
			// 更新後は元のページへ戻る
			return getReturnUrl();
		} else {
			return "todoForm";
		}
	}

	@PostMapping("/todo/delete")
	public String deleteTodo(@ModelAttribute TodoData todoData) {
		todoRepository.deleteById(todoData.getId());
		// 削除後も元のページへ戻る
		return getReturnUrl();
	}

	@PostMapping("/todo/deleteList")
	public String deleteTodoList(@RequestParam(name = "deleteIds", required = false) List<Integer> deleteIds) {
		// チェックボックスが1つも選択されずに送信された場合はfalseで、nullを代入して、処理続行
		if (deleteIds != null && !deleteIds.isEmpty()) {
			// Spring Data JPA の deleteAllById を使うと、リストで渡したIDを全部削除してくれる
			//削除のクエリを組み立て、実行してくれる
			todoRepository.deleteAllById(deleteIds);
		}

		// 削除後は、セッションに保存しておいた元のページ番号、検索条件のURLへ戻る
		return getReturnUrl();
	}
}