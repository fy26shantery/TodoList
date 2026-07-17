package com.example.todolist.dao;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import com.example.todolist.common.Utils;
import com.example.todolist.entity.Todo;
import com.example.todolist.form.TodoQuery;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TodoDaoImpl implements TodoDao {
	private final EntityManager entityManager;

	//JPQLによる検索
	@Override
	public List<Todo> findByJPQL(TodoQuery todoQuery) {
		//ここをtodoにすると実行時エラーになる
		StringBuilder sb = new StringBuilder("select t from Todo t where 1 = 1");
		List<Object> params = new ArrayList<Object>();
		int pos = 0;

		//実行するJPQLの組み立て
		//件名
		if (todoQuery.getTitle().length() > 0) {
			sb.append("and t.title like?" + (++pos));
			params.add("%" + todoQuery.getTitle() + "%");
		}

		//重要度
		if (todoQuery.getImportance() != -1) {
			sb.append("and t.importance = ?" + (++pos));
			params.add(todoQuery.getImportance());
		}

		//緊急度
		if (todoQuery.getUrgency() != -1) {
			sb.append("and t.urgency = ?" + (++pos));
			params.add(todoQuery.getUrgency());
		}

		//期限 開始～
		if (!todoQuery.getDeadlineFrom().equals("")) {
			sb.append("and t.deadline >= ?" + (++pos));
			params.add(Utils.str2date(todoQuery.getDeadlineFrom()));
		}

		//～期限 終了で検索
		if (!todoQuery.getDeadlineTo().equals("")) {
			sb.append("and t.deadline <= ?" + (++pos));
			params.add(Utils.str2date(todoQuery.getDeadlineTo()));
		}

		//完了
		if (todoQuery.getDone() != null && todoQuery.getDone().equals("Y")) {
			sb.append("and t.done =?" + (++pos));
			params.add(todoQuery.getDone());
		}

		//order
		sb.append(" order by id");

		Query query = entityManager.createQuery(sb.toString());
		for (int i = 0; i < params.size(); ++i) {
			query = query.setParameter(i + 1, params.get(i));
		}

		@SuppressWarnings("unchecked")
		List<Todo> list = query.getResultList();
		return list;

	}
}
