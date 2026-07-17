package com.example.todolist.service;

import java.time.DateTimeException;
import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import com.example.todolist.form.TodoData;
import com.example.todolist.form.TodoQuery;

@Service
public class TodoService {

	public boolean isValid(TodoData todoData, BindingResult result) {
		boolean ans = true;

		//件名が全角スペースだけだったらエラー
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
				FieldError fieldError = new FieldError(
						result.getObjectName(), "title", "件名が全角スペースです");//getobjectしちゃってる
				result.addError(fieldError);
				ans = false;
			}
		}
		//期限が過去日付けならエラー
		String deadline = todoData.getDeadline();
		if (!deadline.equals("")) {
			LocalDate tody = LocalDate.now();
			LocalDate deadlineDate = null;
			try {
				deadlineDate = LocalDate.parse(deadline);
				if (deadlineDate.isBefore(tody)) {
					FieldError fieldError = new FieldError(result.getObjectName(), "deadline", "期限を設定するときは今日以降にしてください");
					result.addError(fieldError);
					ans = false;
				}
			} catch (DateTimeException e) {
				FieldError fieldError = new FieldError(result.getObjectName(), "deadline",
						"期限を設定するときはyyyy-MM-dd形式で入力してください");
				result.addError(fieldError);
				ans = false;
			}
		}

		return ans;
	}

	public boolean isValid(TodoQuery todoQuery, BindingResult result) {
		boolean ans = true;

		//期限開始の形式をチェック
		String date = todoQuery.getDeadlineForm();
		if (!date.equals("")) {
			try {
				LocalDate.parse(date);
			} catch (DateTimeException e) {
				//parseでないとき
				FieldError fieldError = new FieldError(result.getObjectName(), "deedLineForm",
						"期限：開始を入力するときは、yyy-mm-dd形式で入力してください");
				result.addError(fieldError);
				ans = false;

			}
		}

		//期限：終了の形式をチェック
		date = todoQuery.getDeadlineTo();
		if (!date.equals("")) {
			try {
				LocalDate.parse(date);

			} catch (DateTimeException e) {
				//parseできない場合
				FieldError fieldError = new FieldError(result.getObjectName(), "deedLineTo",
						"期限：終了を入力するときは、yyy-mm-dd形式で入力してください");
				result.addError(fieldError);
				ans = false;
			}
		}
		return ans;
	}

}
