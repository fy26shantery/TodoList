package com.example.todolist.dao;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.example.todolist.common.Utils;
import com.example.todolist.entity.Todo;
import com.example.todolist.form.TodoQuery;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TodoDaoImpl implements TodoDao {
	private final EntityManager entityManager;

	//JPQLによる検索
	@Override
	public Page<Todo> findByJPQL(TodoQuery todoQuery, Pageable pageable) {
		//ここを"todo"にすると実行時エラーになる
		StringBuilder sb = new StringBuilder("select t from Todo t where 1 = 1");
		List<Object> params = new ArrayList<>();
		int pos = 0;

		//実行時にpos足してプレースホルダーに順番を教えてる

		//実行するJOQLの組み立て
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
			sb.append(" and t.urgency =?" + (++pos));
			params.add(todoQuery.getUrgency());
		}

		//期限：開始ー
		if (!todoQuery.getDeadlineFrom().equals("")) {
			sb.append(" and t.deadline >= ?" + (++pos));
			params.add(Utils.str2date(todoQuery.getDeadlineFrom()));
		}

		//期限：終了で検索
		if (!todoQuery.getDeadlineTo().equals("")) {
			sb.append(" and t.deadline <= ?" + (++pos));
			params.add(Utils.str2date(todoQuery.getDeadlineTo()));

		}

		//完了
		if (todoQuery.getDone() != null && todoQuery.getDone().equals("Y")) {
			sb.append(" and t.done = ?" + (++pos));
			params.add(todoQuery.getDone());
		}

		//select t の部分が select count(t)になってリストの作ったデータの数を数える
		String count = sb.toString().replace("select t", "select count(t)");
		Query countQuery = entityManager.createQuery(count);
		for (int i = 0; i < params.size(); ++i) {
			countQuery = countQuery.setParameter(i + 1, params.get(i));
		}

		//long型にキャスト(intじゃだめらしい）
		long allCount = (long) countQuery.getSingleResult();

		//order id順に並べ替え
		sb.append("order by id");

		Query query = entityManager.createQuery(sb.toString());
		for (int i = 0; i < params.size(); ++i) {
			query = query.setParameter(i + 1, params.get(i));
		}

		//ページのはじまりのデータをおしえてくれる、ここからかいてね　longで返ってくるからキャストする
		query.setFirstResult((int) pageable.getOffset());
		//１ページいくつかくかおしえてくれる
		query.setMaxResults(pageable.getPageSize());

		@SuppressWarnings("unchecked")
		List<Todo> list = query.getResultList();

		return new PageImpl<>(list, pageable, allCount);
	}

	public List<Todo> findByJPQL(TodoQuery todoQuery) {
		//ここを"todo"にすると実行時エラーになる
		StringBuilder sb = new StringBuilder("select t from Todo t where 1 = 1");
		List<Object> params = new ArrayList<>();
		int pos = 0;

		//実行するJOQLの組み立て
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
			sb.append(" and t.urgency =?" + (++pos));
			params.add(todoQuery.getUrgency());
		}

		//期限：開始ー
		if (!todoQuery.getDeadlineFrom().equals("")) {
			sb.append(" and t.deadline >= ?" + (++pos));
			params.add(Utils.str2date(todoQuery.getDeadlineFrom()));
		}

		//期限：終了で検索
		if (!todoQuery.getDeadlineTo().equals("")) {
			sb.append(" and t.deadline <= ?" + (++pos));
			params.add(Utils.str2date(todoQuery.getDeadlineTo()));

		}

		//完了
		if (todoQuery.getDone() != null && todoQuery.getDone().equals("Y")) {
			sb.append(" and t.done = ?" + (++pos));
			params.add(todoQuery.getDone());
		}

		//order id順に並べ替え
		sb.append("order by id");

		Query query = entityManager.createQuery(sb.toString());
		for (int i = 0; i < params.size(); ++i) {
			query = query.setParameter(i + 1, params.get(i));
		}

		@SuppressWarnings("unchecked")
		List<Todo> list = query.getResultList();

		return list;
	}

}

//	教科書のいらんとこ
//	
//	@Override
//	public Page<Todo> findByCriteria(TodoQuery todoQuery, Pageable pageable) {
//
//		//SELECT作成
//		Predicate[] predArray = new Predicate[predicates.size()];
//		predicates.toArray(predArray);
//		query = query.select(root).where(predArray).orderBy(builder.asc(root.get(Todo_.id)));
//
//		//クエリ生成
//		TypedQuery<Todo> typedQuery = entityManager.createQuery(query);
//		//該当レコード数取得
//		int totalRows = typedQuery.getResultList().size();
//		//先頭レコードの位置設定
//		typedQuery.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
//		//1ページあたりの件数
//		typedQuery.setMaxResults(pageable.getPageSize());
//
//		Page<Todo> page = new PageImpl<Todo>(typedQuery.getResultList(), pageable, totalRows);
//		return page;
//	}
