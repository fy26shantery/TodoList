package com.example.todolist.service;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import com.example.todolist.common.Utils;
import com.example.todolist.entity.Todo;
import com.example.todolist.form.TodoData;
import com.example.todolist.form.TodoQuery;
import com.example.todolist.repository.TodoRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class TodoService {

	private final TodoRepository todoRepository;

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
				FieldError fieldError = new FieldError(result.getObjectName(),
						"title",
						null,
						true,
						new String[] { "service.titleError" },
						null,
						null);
				result.addError(fieldError);
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
					FieldError fieldError = new FieldError(result.getObjectName(),
							"deadline",
							null,
							true,
							new String[] { "service.deadlineError" },
							null,
							null);
					result.addError(fieldError);
					ans = false;
				}
			} catch (DateTimeException e) {
				FieldError fieldError = new FieldError(result.getObjectName(),
						"deadline",
						null,
						true,
						new String[] { "service.deadlineFormError" },
						null,
						null);
				result.addError(fieldError);
				ans = false;
			}
		}
		return ans;

	}

	public boolean isValid(TodoQuery todoQuery, BindingResult result) {
		boolean ans = true;
		boolean isFromValid = false;
		boolean isToValid = false;
		//リスト9-5期限、開始の形式をチェック
		String dateFrom = todoQuery.getDeadlineFrom();
		if (!dateFrom.equals("")) {
			try {
				LocalDate.parse(dateFrom);
				isFromValid = true;
			} catch (DateTimeException e) {
				// parseできない場合
				FieldError fieldError = new FieldError(result.getObjectName(),
						"deadlineFrom",
						null,
						true,
						new String[] { "service.deadlineFromError" },
						null,
						null);
				result.addError(fieldError);
				ans = false;
			}

		}

		//期限：終了の形式チェック
		String dateTo = todoQuery.getDeadlineTo();
		if (!dateTo.equals("")) {
			try {
				LocalDate.parse(dateTo);
				isToValid = true;
			} catch (DateTimeException e) {
				// parseできない場合
				FieldError fieldError = new FieldError(result.getObjectName(),
						"deadlineTo",
						null,
						true,
						new String[] { "service.deadlineToError" },
						null,
						null);
				result.addError(fieldError);
				ans = false;
			}

		}

		//開始日より終了日が未来の時のチェック
		if (isFromValid && isToValid) {

			try {
				LocalDate fromDate = LocalDate.parse(dateFrom);
				LocalDate toDate = LocalDate.parse(dateTo);

				if (fromDate.isAfter(toDate)) {
					FieldError fieldError = new FieldError(result.getObjectName(),
							"deadlineFrom",
							null,
							true,
							new String[] { "service.deadline.chronology" },
							null,
							null);
					result.addError(fieldError);
					ans = false;
				}
			} catch (Exception e) {
				//日付の変換に失敗
				FieldError fieldError = new FieldError(result.getObjectName(),
						"deadlineFrom",
						null,
						true,
						new String[] { "service.deadlineError" },
						null,
						null);
				result.addError(fieldError);
				ans = false;
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
		} else if (!todoQuery.getDeadlineFrom().equals("") && todoQuery.getDeadlineTo().equals("")) {
			//期限　開始～
			todoList = todoRepository
					.findByDeadlineGreaterThanEqualOrderByDeadlineAsc(Utils.str2date(todoQuery.getDeadlineFrom()));

		} else if (todoQuery.getDeadlineFrom().equals("") && !todoQuery.getDeadlineTo().equals("")) {
			//期限　～終了
			todoList = todoRepository
					.findByDeadlineLessThanEqualOrderByDeadlineAsc(Utils.str2date(todoQuery.getDeadlineTo()));
		} else if (!todoQuery.getDeadlineFrom().equals("") && !todoQuery.getDeadlineTo().equals("")) {
			//期限　開始～終了
			todoList = todoRepository.findByDeadlineBetweenOrderByDeadlineAsc(
					Utils.str2date(todoQuery.getDeadlineFrom()),
					Utils.str2date(todoQuery.getDeadlineTo()));
		} else if (todoQuery.getDone() != null && todoQuery.getDone().equals("Y")) {
			//完了で検索
			todoList = todoRepository.findByDone("Y");
		} else {
			//入力条件がなければ全件検索
			todoList = todoRepository.findAll();

		}
		return todoList;

	}

	//日付の形式のみのチェック
	public boolean formatCheck(TodoData todoData, BindingResult result) {
		boolean ans = true;

		String deadline = todoData.getDeadline();
		LocalDate deadlineDate = null;
		try {
			deadlineDate = LocalDate.parse(deadline);

		} catch (DateTimeException e) {
			FieldError fieldError = new FieldError(result.getObjectName(),
					"deadline",
					null,
					true,
					new String[] { "service.deadlineFormError" },
					null,
					null);
			result.addError(fieldError);
			ans = false;
		}

		return ans;

	}

}
