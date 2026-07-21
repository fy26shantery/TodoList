package com.example.todolist.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

import com.example.todolist.common.Utils;
import com.example.todolist.entity.Todo;
import com.example.todolist.form.TodoQuery;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TodoDaoImpl implements TodoDao {
	private final EntityManager entityManager;

	//JPQLによる検索
	public Page<Todo> findByJPQL(TodoQuery todoQuery, @PageableDefault Pageable pageable) {
		//ここを"todo"にすると実行時エラーになる
		StringBuilder sb = new StringBuilder("where 1 = 1");
		List<Object> params = new ArrayList<>();
		int pos = 0;

		//実行するJPSQLの組み立て
		//件名
		if (todoQuery.getTitle().length() > 0) {
			sb.append(" and t.title like ?" + (++pos));
			params.add("%" + todoQuery.getTitle() + "%");
		}
		//重要度
		if (todoQuery.getImportance() != -1) {
			sb.append(" and t.importance = ?" + (++pos));
			params.add(todoQuery.getImportance());
		}
		//緊急度
		if (todoQuery.getUrgency() != -1) {
			sb.append(" and t.urgency = ?" + (++pos));
			params.add(todoQuery.getUrgency());
		}
		//期限　：開始～
		if (!todoQuery.getDeadlineFrom().equals("")) {
			sb.append(" and t.deadline >= ?" + (++pos));
			params.add(Utils.str2date(todoQuery.getDeadlineFrom()));
		}

		//～期限　：終了
		if (!todoQuery.getDeadlineTo().equals("")) {
			sb.append(" and t.deadline <= ?" + (++pos));
			params.add(Utils.str2date(todoQuery.getDeadlineTo()));
		}

		//	完了
		if (todoQuery.getDone() != null && todoQuery.getDone().equals("Y")) {
			sb.append(" and t.done = ?" + (++pos));
			params.add(todoQuery.getDone());
		}

		//データの総件数取得クエリ
		String countJpql = "select count(t) from Todo t " + sb.toString();
		Query query = entityManager.createQuery(countJpql);
		for (int i = 0; i < params.size(); ++i) {
			query = query.setParameter(i + 1, params.get(i));
		}
		long total = (Long) query.getSingleResult();

		//該当データが０件なら空のPageImplを返す
		if (total == 0) {
			return new PageImpl<>(Collections.emptyList(), pageable, 0);//中身が空のpageオブジェクトを作る
		}

		//データ一覧を取得するクエリの実行
		String selectJpql = "select t from Todo t " + sb.toString() + " order by t.id";
		TypedQuery<Todo> selectQuery = entityManager.createQuery(selectJpql, Todo.class);
		for (int i = 0; i < params.size(); ++i) {
			selectQuery.setParameter(i + 1, params.get(i));
		}

		// Pageableオブジェクトからオフセットとページサイズを設定
		selectQuery.setFirstResult((int) pageable.getOffset());
		selectQuery.setMaxResults(pageable.getPageSize());

		List<Todo> list = selectQuery.getResultList();

		// List, Pageable, 総件数 から PageImpl オブジェクトを作成して返却
		return new PageImpl<>(list, pageable, total);

	}

}
