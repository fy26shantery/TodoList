package com.example.todolist.dao;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.example.todolist.common.Utils;
import com.example.todolist.entity.Todo;
import com.example.todolist.form.TodoQuery;

import lombok.RequiredArgsConstructor;

@Repository //このクラスがDBを直接操作するDaoである
@RequiredArgsConstructor
public class TodoDaoImpl implements TodoDao {
	private final EntityManager entityManager;
	//DBとJavaのデータを出し入れするための
	//オブジェクトを作るときにfinalのフィールドを絶対にもらいたいから

	@Override
	public Page<Todo> findByJPQL(TodoQuery todoQuery, Pageable pageable) {
		//SQLのベースの条件
		StringBuilder sb = new StringBuilder(" from Todo t where 1 = 1");
		//追加しようとしてる条件はいくつ目なのかを判定する手間を省くための条件）
		//ユーザーが入力したところだけJPQLのsbにandをつけてSQL文に付け足す

		List<Object> params = new ArrayList<>();
		//検索項目をいくつ入力してくるのか不明だからList用意
		int pos = 0;

		if (todoQuery.getTitle() != null && todoQuery.getTitle().length() > 0) {
			sb.append(" and t.title like ?").append(++pos);
			params.add("%" + todoQuery.getTitle() + "%");
		}
		if (todoQuery.getImportance() != -1) {
			sb.append(" and t.importance = ?").append(++pos);
			params.add(todoQuery.getImportance());
		}
		if (todoQuery.getUrgency() != -1) {
			sb.append(" and t.urgency = ?").append(++pos);
			params.add(todoQuery.getUrgency());
		}
		if (!todoQuery.getDeadlineFrom().equals("")) {
			sb.append(" and t.deadline >= ?").append(++pos);
			params.add(Utils.str2date(todoQuery.getDeadlineFrom()));
		}
		if (!todoQuery.getDeadlineTo().equals("")) {
			sb.append(" and t.deadline <= ?").append(++pos);
			params.add(Utils.str2date(todoQuery.getDeadlineTo()));
		}
		if (todoQuery.getDone() != null && todoQuery.getDone().equals("Y")) {
			sb.append(" and t.done = ?").append(++pos);
			params.add(todoQuery.getDone());
		}

		//検索条件に一致するデータが全部で何件あるかを取得するクエリ
		//ページリンクをいくつ作るかを計算するため
		Query countQuery = entityManager.createQuery("select count(t)" + sb.toString());
		for (int i = 0; i < params.size(); ++i) {
			countQuery.setParameter(i + 1, params.get(i));
		}
		long totalRows = (long) countQuery.getSingleResult();

		//ページごとのデータを取得する
		sb.append(" order by t.id");
		Query dataQuery = entityManager.createQuery("select t" + sb.toString());
		for (int i = 0; i < params.size(); ++i) {
			//ページネーションのリンクを作るためにデータが全部で何件あるかを知りたいので、SQLで件数を教える
			dataQuery.setParameter(i + 1, params.get(i));
		}

		//何件目のデータから取得するかを決める
		dataQuery.setFirstResult((int) pageable.getOffset());
		//１ページに何件表示するかを決める
		dataQuery.setMaxResults(pageable.getPageSize());

		@SuppressWarnings("unchecked")
		List<Todo> list = dataQuery.getResultList();

		//PageImpl にデータ、ページ情報、総件数を詰めたPage<Todo> を返す
		return new PageImpl<>(list, pageable, totalRows);
	}
}