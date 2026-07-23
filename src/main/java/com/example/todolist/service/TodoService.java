package com.example.todolist.service;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import com.example.todolist.common.Utils;
import com.example.todolist.entity.Todo;
import com.example.todolist.form.TodoData;
import com.example.todolist.form.TodoQuery;
import com.example.todolist.repository.TodoRepository;

import lombok.RequiredArgsConstructor;

@Service
//アプリの様々な、複雑な処理をするクラス
@RequiredArgsConstructor
public class TodoService {
	private final TodoRepository todoRepository;

	public boolean isValid(TodoData todoData, BindingResult result, String mode) {
		boolean ans = true;

		// 件名が全角スペースのとき
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
				// messages.properties のキー
				result.rejectValue("title", "todo.title.allDoubleSpace");
				ans = false;
			}
		}

		// 期限が過去日付かチェック
		String deadline = todoData.getDeadline();
		if (!deadline.equals("")) {
			LocalDate today = LocalDate.now();
			LocalDate deadlineDate = null;
			try {
				deadlineDate = LocalDate.parse(deadline);
				if ("create".equals(mode) && deadlineDate.isBefore(today)) {
					//createモードの時は今日より前の日付であるかチェック

					result.rejectValue("deadline", "todo.deadline.past");
					ans = false;
				}
			} catch (DateTimeException e) {
				result.rejectValue("deadline", "todo.deadline.format");
				ans = false;
			}
		}
		return ans;
	}

	public boolean isValid(TodoQuery todoQuery, BindingResult result) {
		boolean ans = true;
		LocalDate fromDate = null;
		LocalDate toDate = null;

		// 期限入力の形式チェック（From）
		String dateFrom = todoQuery.getDeadlineFrom();
		if (!dateFrom.equals("")) {
			try {
				fromDate = LocalDate.parse(dateFrom);
			} catch (DateTimeException e) {
				result.rejectValue("deadlineFrom", "todo.deadlineFrom.format");
				ans = false;
			}
		}

		// 期限入力の形式チェック（To）
		String dateTo = todoQuery.getDeadlineTo();
		if (!dateTo.equals("")) {
			try {
				toDate = LocalDate.parse(dateTo);
			} catch (DateTimeException e) {
				result.rejectValue("deadlineTo", "todo.deadlineTo.format");
				ans = false;
			}
		}

		//両方とも形式エラーがない場合、大小関係をチェックする
		if (fromDate != null && toDate != null) {
			// 開始日が終了日より後の場合
			if (fromDate.isAfter(toDate)) {
				result.rejectValue("deadlineFrom", "todo.deadline.range");
				ans = false;
			}
		}

		return ans;
	}

	public boolean isValidDelete(List<Integer> deleteIds, BindingResult result) {
		if (deleteIds == null || deleteIds.isEmpty()) {
			result.reject("todo.delete.none");
			return false;
		}
		return true;
	}

	public List<Todo> doQuery(TodoQuery todoQuery) {
		List<Todo> todoList = null;
		if (todoQuery.getTitle().length() > 0) {
			todoList = todoRepository.findByTitleLike("%" + todoQuery.getTitle() + "%");
		} else if (todoQuery.getImportance() != null && todoQuery.getImportance() != -1) {
			todoList = todoRepository.findByImportance(todoQuery.getImportance());
		} else if (todoQuery.getUrgency() != null && todoQuery.getUrgency() != -1) {
			todoList = todoRepository.findByUrgency(todoQuery.getUrgency());
		} else if (!todoQuery.getDeadlineFrom().equals("") && todoQuery.getDeadlineTo().equals("")) {
			todoList = todoRepository
					.findByDeadlineGreaterThanEqualOrderByDeadlineAsc(Utils.str2date(todoQuery.getDeadlineFrom()));
		} else if (todoQuery.getDeadlineFrom().equals("") && !todoQuery.getDeadlineTo().equals("")) {
			todoList = todoRepository
					.findByDeadlineLessThanEqualOrderByDeadlineAsc(Utils.str2date(todoQuery.getDeadlineTo()));
		} else if (!todoQuery.getDeadlineFrom().equals("") && !todoQuery.getDeadlineTo().equals("")) {
			todoList = todoRepository.findByDeadlineBetweenOrderByDeadlineAsc(
					Utils.str2date(todoQuery.getDeadlineFrom()), Utils.str2date(todoQuery.getDeadlineTo()));
		} else if (todoQuery.getDone() != null && todoQuery.getDone().equals("Y")) {
			todoList = todoRepository.findByDone("Y");
		} else {
			todoList = todoRepository.findAll();
		}
		return todoList;
	}
}