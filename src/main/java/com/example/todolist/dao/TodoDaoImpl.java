package com.example.todolist.dao;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

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
	public List<Todo> findByJPQL(TodoQuery todoQuery) {
		//ここをtodoにすると実行時にエラーになる
		StringBuilder sb = new StringBuilder("select t from Todo t Where 1 = 1");
		//常に真となる 1 = 1 を最初に書いておくことで、後ろに続く条件の and の追加を単純化

		List<Object> params = new ArrayList<>();
		int pos = 0;

		//実行するJPQLの組み立て
		//件名
		if (todoQuery.getTitle().length() > 0) {
			sb.append(" and t.title like ?" + (++pos));//条件に合致するたびに ?1, ?2, ?3 と動的にプレースホルダーの数字が増える
			//			JPAという仕組みでは、SQLのなかに ?1 や ?2 と書いておくと、
			//			後から「?1 にはこの値を入れます」と指定できるルール
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

		//期限：開始～
		if (!todoQuery.getDeadlineForm().equals("")) {
			sb.append(" and t.deadline >= ?" + (++pos));
			params.add(Utils.str2date(todoQuery.getDeadlineForm()));
		}

		//～期限：終了で検索
		if (!todoQuery.getDeadlineTo().equals("")) {
			sb.append(" and t.deadline <= ?" + (++pos));
			params.add(Utils.str2date(todoQuery.getDeadlineTo()));
		}

		//完了
		if (todoQuery.getDone() != null && todoQuery.getDone().equals("Y")) {
			sb.append(" and t.done = ?" + (++pos));
			params.add(todoQuery.getDone());
		}

		//order
		sb.append(" order by id");

		Query query = entityManager.createQuery(sb.toString());//toStringでひとつの文章にする
		for (int i = 0; i < params.size(); ++i) {
			//			params.get(1) で、リストの1番目にある値（3）を取り出す
			//			query.setParameter(i + 1, ...) なので、1 + 1 で 2
			//			SQLの ?2 の穴に、3 を入れてね！」という命令になる
			query = query.setParameter(i + 1, params.get(i));
		}

		@SuppressWarnings("unchecked")
		List<Todo> list = query.getResultList();
		return list;

	}

	//Criteria APIによる検索
	//	@Override
	//	public List<Todo> findByCriteria(TodoQuery todoQuery) {
	//		//内容は次節で解説
	//		return null;
	//	}
	//	private CriteriaBuilder builder;
	//	private CriteriaQuery<Todo> query;
	//	private Root<Todo> root;
	//	private List<Predicate> predicates;

	//	「ユーザーがどんなに複雑な条件で検索しても、データベースから
	//	『いま画面に見えている5件分だけ』をピンポイントで切り抜いて、キレイに画面に返すこと」
	//	@Override
	//	public Page<Todo> findByCriteria(TodoQuery todoQuery, Pageable pageable) {
	//
	//		//Select 作成
	//		Predicate[] predArray = new Predicate[predicates.size()];
	//		predicates.toArray(predArray);
	//		query = query.select(root).where(predArray).orderBy(builder.asc(root.get("id")));
	//
	//		//クエリ作成
	//		TypedQuery<Todo> typedQuery = entityManager.createQuery(query);
	//		//該当レコード数取得
	//		int totalRows = typedQuery.getResultList().size();
	//		//先頭レコードの位置設定
	//		typedQuery.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
	//		//1ページ当たりの件数
	//		typedQuery.setMaxResults(pageable.getPageSize());
	//
	//		Page<Todo> page = new PageImpl<Todo>(typedQuery.getResultList(), pageable, totalRows);
	//
	//		return page;
	//	}

	@Override
	public Page<Todo> findByCriteria(TodoQuery todoQuery, Pageable pageable) {
		//JPAの検索パーツ（道具）をその場で用意する
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Todo> query = builder.createQuery(Todo.class);
		Root<Todo> root = query.from(Todo.class);

		//条件を入れるリストを作り、画面の入力（件名）をチェックして入れる
		List<Predicate> predicatesList = new ArrayList<>();

		//title
		if (todoQuery.getTitle() != null && !todoQuery.getTitle().isEmpty()) {
			Predicate titlePredicate = builder.like(root.get("title"), "%" + todoQuery.getTitle() + "%");
			predicatesList.add(titlePredicate);
		}

		//重要度
		if (todoQuery.getImportance() != -1) {
			Predicate importancePredicate = builder.equal(root.get("importance"), todoQuery.getImportance());
			predicatesList.add(importancePredicate);
		}

		//緊急度
		if (todoQuery.getUrgency() != -1) {
			Predicate urgencyPredicate = builder.equal(root.get("urgency"), todoQuery.getUrgency());
			predicatesList.add(urgencyPredicate);
		}

		//期限：開始～ のチェック
		if (todoQuery.getDeadlineForm() != null && !todoQuery.getDeadlineForm().equals("")) {
			Predicate fromPredicate = builder.greaterThanOrEqualTo(root.get("deadline"),
					Utils.str2date(todoQuery.getDeadlineForm()));
			predicatesList.add(fromPredicate);
		}

		//～期限：終了 のチェック
		if (todoQuery.getDeadlineTo() != null && !todoQuery.getDeadlineTo().equals("")) {
			Predicate toPredicate = builder.lessThanOrEqualTo(root.get("deadline"),
					Utils.str2date(todoQuery.getDeadlineTo()));
			predicatesList.add(toPredicate);
		}

		//完了フラグのチェック
		if (todoQuery.getDone() != null && todoQuery.getDone().equals("Y")) {
			Predicate donePredicate = builder.equal(root.get("done"), todoQuery.getDone());
			predicatesList.add(donePredicate);
		}

		// 配列に変換
		Predicate[] predArray = predicatesList.toArray(new Predicate[0]);

		//queryに条件と並び順をセットする
		query.select(root)
				.where(predArray)
				.orderBy(builder.asc(root.get("id")));

		//データベースから「いま必要な5件」だけを切り出す設定をする
		TypedQuery<Todo> typedQuery = entityManager.createQuery(query);
		typedQuery.setFirstResult((int) pageable.getOffset()); // pageable.getOffset() を使うとページ位置がより正確になります
		typedQuery.setMaxResults(pageable.getPageSize());

		//データベースから「条件に合う全件数」を正しく数えて取得する
		CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
		Root<Todo> countRoot = countQuery.from(Todo.class);

		// データ検索と同じ条件（predArray）を使って、COUNT(*) を実行する
		countQuery.select(builder.count(countRoot)).where(predArray);
		Long totalRows = entityManager.createQuery(countQuery).getSingleResult();

		// 画面に返すパック（Page）を作って返却
		return new PageImpl<Todo>(typedQuery.getResultList(), pageable, totalRows);
	}

}
