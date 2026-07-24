package com.example.todolist.service;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.List;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpSession;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import com.example.todolist.dao.TodoDaoImpl;
import com.example.todolist.entity.Todo;
import com.example.todolist.form.TodoData;
import com.example.todolist.form.TodoQuery;
import com.example.todolist.repository.TodoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TodoService {
	private final TodoRepository todoRepository;

	private final HttpSession session;

	@PersistenceContext
	private EntityManager entityManager;
	TodoDaoImpl todoDaoImpl;

	@PostConstruct
	public void init() {
		todoDaoImpl = new TodoDaoImpl(entityManager);
	}

	public boolean isValid(TodoData todoData, BindingResult result) {
		boolean ans = true;

		//件名が全角スペースだけで構成されていたらエラー
		String title = todoData.getTitle();
		if (title != null && !title.equals("")) {
			boolean isAllDoubleSpace = true;
			for (int i = 0; i < title.length(); i++) {
				if (title.charAt(i) != ' ') {
					isAllDoubleSpace = false;
					break;
				}
			}
			if (isAllDoubleSpace) {
				result.rejectValue("title", "tilte.input.error"); //エラーを足す

				ans = false;
			}
		}

		//期限が過去日付ならエラー
		String deadline = todoData.getDeadline();
		if (!deadline.equals("")) {
			LocalDate tody = LocalDate.now();
			LocalDate deadlineDate = null;

			try {
				deadlineDate = LocalDate.parse(deadline);
				if (deadlineDate.isBefore(tody)) {
					result.rejectValue("deadline", "deadline.beforeset.error"); //エラーを足す
					ans = false;

				}
			} catch (DateTimeException e) {
				result.rejectValue("deadline", "deadline.format.error"); //エラーを足す
				ans = false;
			}
		}
		return ans;
	}

	//update用のエラーチェック（期限過去は考慮しない）
	public boolean isValidForUpdate(TodoData todoData, BindingResult result) {
		boolean ans = true;
		//期限のフォーマットが誤っていたらエラー
		String deadline = todoData.getDeadline();
		if (!deadline.equals("")) {
			try {
				LocalDate.parse(deadline);

			} catch (DateTimeException e) {
				result.rejectValue("deadline", "deadline.format.error"); //エラーを足す

				ans = false;
			}
		}
		return ans;
	}

	public boolean isValid(TodoQuery todoQuery, BindingResult result) {
		boolean ans = true;

		//期限:開始の形式をチェック
		String dateFrom = todoQuery.getDeadlineFrom();
		if (!dateFrom.equals("")) {
			try {
				LocalDate.parse(dateFrom);
			} catch (DateTimeException e) {
				//parseできない場合
				result.rejectValue("deadlineFrom", "deadline.format.error"); //エラーを足す
				ans = false;

			}
		}

		//期限:終了の形式をチェック
		String dateTo = todoQuery.getDeadlineTo();
		if (!dateTo.equals("")) {
			try {
				LocalDate.parse(dateTo);
			} catch (DateTimeException e) {
				//parseできない場合
				result.rejectValue("deadlineTo", "deadline.format.error"); //エラーを足す
				ans = false;

			}

			//開始が終了よりも以前であることをチェック
			if (!dateFrom.equals("") && !dateTo.equals("") && ans == true) {

				LocalDate dateFromDate = LocalDate.parse(dateFrom);
				LocalDate dateToDate = LocalDate.parse(dateTo);

				//開始が終了よりも後の場合
				if (dateFromDate.isAfter(dateToDate)) {
					result.reject("dateReverse", "deadline.reverse.error");
					ans = false;

				}

			}

		}
		return ans;
	}

	public List<Todo> doQuery(TodoQuery todoQuery) {
		List<Todo> todoList = null;
		if (todoQuery.getTitle().length() > 0) {
			//タイトルで検索
			todoList = todoRepository.findByTitleLike("%" + todoQuery.getTitle() + "%");

		} else if (todoQuery.getImportance() != null && todoQuery.getImportance() != -1) {
			//重要度で検索
			todoList = todoRepository.findByImportance(todoQuery.getImportance());

		} else if (todoQuery.getUrgency() != null && todoQuery.getUrgency() != -1) {
			//緊急度で検索
			todoList = todoRepository.findByUrgency(todoQuery.getUrgency());

		} else if (!todoQuery.getDeadlineFrom().equals("") &&
				todoQuery.getDeadlineTo().equals("")) {
			//期限 開始～
			todoList = todoRepository.findByDeadlineGreaterThanEqualOrderByDeadlineAsc(
					com.example.todolist.common.Utils.str2date(todoQuery.getDeadlineFrom()));

		} else if (todoQuery.getDeadlineFrom().equals("") && !todoQuery.getDeadlineTo().equals("")) {
			//期限 ～終了
			todoList = todoRepository.findByDeadlineLessThanEqualOrderByDeadlineAsc(
					com.example.todolist.common.Utils.str2date(todoQuery.getDeadlineTo()));

		} else if (!todoQuery.getDeadlineFrom().equals("") && !todoQuery.getDeadlineTo().equals("")) {
			//期限 開始～終了
			todoList = todoRepository.findByDeadlineBetweenOrderByDeadlineAsc(
					com.example.todolist.common.Utils.str2date(todoQuery.getDeadlineFrom()),
					com.example.todolist.common.Utils.str2date(todoQuery.getDeadlineTo()));

		} else if (todoQuery.getDone() != null && todoQuery.getDone().equals("Y")) {
			//完了で検索
			todoList = todoRepository.findByDone("Y");

		} else {
			//入力条件がなければ全件検索
			todoList = todoRepository.findAll();
		}
		return todoList;
	}

	//適切なページリンク表示用メソッド（前二つ後ろ二つ以内）
	public void constitutePage(Page<Todo> todoPage, Model md) {
		int currentPage = todoPage.getNumber();
		int totalPage = todoPage.getTotalPages();

		//現在ページより前のページについて
		if (currentPage > 1) { //現在ページより前のページが十分にある場合
			md.addAttribute("startPage", currentPage - 2);
		} else if (todoPage.isFirst()) {//現在ページが最初の場合
			md.addAttribute("startPage", currentPage);
		} else {
			//現在ページより前に１ページしかない場合
			md.addAttribute("startPage", currentPage - 1);
		}

		//現在ページより後のページについて
		if (currentPage + 2 < totalPage) { //現在ページより後のページが十分にある場合
			md.addAttribute("endPage", currentPage + 2);
		} else if (todoPage.isLast()) { //現在ページが最後の場合
			md.addAttribute("endPage", currentPage);
		} else {
			//現在ページより後ろに１ページしかない場合
			md.addAttribute("endPage", currentPage + 1);
		}
	}

	//searchからの検索メソッド
	@SuppressWarnings("unchecked")
	public void dealQuery(TodoQuery todoQuery, BindingResult result,
			Pageable pageable, Model md) {
		Page<Todo> todoPage = null;
		if (isValid(todoQuery, result)) {
			//エラーがなければ検索
			todoPage = todoDaoImpl.findByJPQL(todoQuery, pageable);

			session.setAttribute("todoQuery", todoQuery);

			constitutePage(todoPage, md); //ページング数の指定
			md.addAttribute("todoPage", todoPage);
			md.addAttribute("todoList", todoPage.getContent());

		} else {

			//検索不能のため、前画面の情報をもらう
			todoPage = (Page<Todo>) session.getAttribute("todoPage");
			md.addAttribute("todoPage", todoPage);
			md.addAttribute("todoList", session.getAttribute("todoList"));
			constitutePage(todoPage, md);

		}
		session.setAttribute("useSearch", 1); //検索フラグを立てる

	}

	//ページリンク先の表示リスト構築
	public void dealQuery(Pageable pageable, Model md) {
		//sessionに保存されている情報で検索
		TodoQuery todoQuery = (TodoQuery) session.getAttribute("todoQuery");
		Page<Todo> todoPage = todoDaoImpl.findByJPQL(todoQuery, pageable);

		constitutePage(todoPage, md); //ページング数の指定
		md.addAttribute("todoQuery", todoQuery);
		md.addAttribute("todoPage", todoPage);
		md.addAttribute("todoList", todoPage.getContent());

	}

}
