package com.example.todolist.repository;

import java.sql.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.todolist.entity.Todo;

@Repository
public interface TodoRepository extends JpaRepository<Todo, Integer> {
	//ページングが必要ではないとき
	List<Todo> findByTitleLike(String title);

	List<Todo> findByImportance(Integer importance);

	List<Todo> findByUrgency(Integer urgency);

	List<Todo> findByDeadlineBetweenOrderByDeadlineAsc(Date from, Date to);

	List<Todo> findByDeadlineGreaterThanEqualOrderByDeadlineAsc(Date from);

	List<Todo> findByDeadlineLessThanEqualOrderByDeadlineAsc(Date to);

	List<Todo> findByDone(String done);

	//ページングが必要な時

	Page<Todo> findByTitleLike(String title, Pageable pageable);

	Page<Todo> findByImportance(Integer importance, Pageable pageable);

	Page<Todo> findByUrgency(Integer urgency, Pageable pageable);

	Page<Todo> findByDeadlineBetweenOrderByDeadlineAsc(Date from, Date to, Pageable pageable);

	Page<Todo> findByDeadlineGreaterThanEqualOrderByDeadlineAsc(Date from, Pageable pageable);

	Page<Todo> findByDeadlineLessThanEqualOrderByDeadlineAsc(Date to, Pageable pageable);

	Page<Todo> findByDone(String done, Pageable pageable);

}
