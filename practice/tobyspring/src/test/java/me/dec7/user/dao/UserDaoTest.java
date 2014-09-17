package me.dec7.user.dao;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import me.dec7.user.domain.Level;
import me.dec7.user.domain.User;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/test-applicationContext.xml")
public class UserDaoTest {
//	private UserDaoJdbc dao;
	/*
	 * @Autowired는 spring context내에서 정의된 bean중 주입가능한 타입의 bean을 찾아줌
	 * UserDaoJdbc라고 선언할 수 있지만
	 * 중요한 것은 테스트에 대한 관심으로 dao가 작동만하면 된다
	 */
	@Autowired
	private UserDao dao;
	
	@Autowired
	private DataSource dataSource;
	
	private User user1;
	private User user2;
	private User user3;
	
	@Before
	public void setUp() {
		/*
		 * text fixture로 만든 user1~3을 수정
		user1 = new User("dec1", "동규1", "111");
		user2 = new User("dec2", "동규2", "222");
		user3 = new User("dec0", "동규3", "333");
		*/		
		
		user1 = new User("dec1", "동규1", "111", "dec1@gmail.com",Level.BASIC, 1, 0);
		user2 = new User("dec2", "동규2", "222", "dec2@gmail.com",Level.SILVER, 55, 10);
		user3 = new User("dec0", "동규3", "333", "dec0@gmail.com",Level.GOLD, 100, 40);
	}
	
	@Test(expected=DuplicateKeyException.class)
	public void duplicateKey() {
		dao.deleteAll();
		
		dao.add(user1);
		dao.add(user1);
	}
	/* 
	 * SQLException을 DataAccessException으로 여러가지로 전환 가능
	 * 	- 보편적 방법은 DB코드를 이용하는 것
	 * 		- SQLExceptionTranslator interface구현한 SQLErrorCodeSQLExceptionTranslator 사용하면 됨
	 * 			- DB종류를 알기 위해 DataSource을 필요로함
	 */
	// dataSource를 사용해 SQLException에서 DuplicateKeyException으로 전환하는 기능을 확인해보는 학습테스트
	@Test
	public void sqlExceptionTranslate() {
		dao.deleteAll();
		
		try {
			dao.add(user1);
			dao.add(user1);
		} catch (DuplicateKeyException e) {
			SQLException sqle = (SQLException)e.getRootCause();
			SQLExceptionTranslator set = new SQLErrorCodeSQLExceptionTranslator(this.dataSource);
			
//			assertThat(set.translate(null, null, sqle), is(DuplicateKeyException.class));
		}
	}
	
	
	/*
	 * BadSqlGrammerException / DataAccessException의 하위 class
	 * 	- sql문법이 틀린경우 발생하는 예외
	 */
	@Test
	public void addAndGet() throws SQLException, ClassNotFoundException {
		// db 초기화
		dao.deleteAll();
		assertThat(dao.getCount(), is(0));
		
		// add()
		dao.add(user1);
		dao.add(user2);
		assertThat(dao.getCount(), is(2));
		
		// get()
		User userget1 = dao.get(user1.getId());
		// assertThat(userget1.getName(), is(user1.getName()));
		// assertThat(userget1.getPassword(), is(user1.getPassword()));
		checkSameUser(userget1, user1);
		
		User userget2 = dao.get(user2.getId());
		// assertThat(userget2.getName(), is(user2.getName()));
		// assertThat(userget2.getPassword(), is(user2.getPassword()));
		checkSameUser(userget2, user2);
		
	}
	
	@Test(expected=EmptyResultDataAccessException.class)
	public void getUserFailure() throws SQLException, ClassNotFoundException {
		dao.deleteAll();
		assertThat(dao.getCount(), is(0));

		dao.get("unknown_id");
	}
	
	
	@Test
	public void count() throws SQLException, ClassNotFoundException {		
		dao.deleteAll();
		assertThat(dao.getCount(), is(0));
		
		dao.add(user1);
		assertThat(dao.getCount(), is(1));
		
		dao.add(user2);
		assertThat(dao.getCount(), is(2));
		
		dao.add(user3);
		assertThat(dao.getCount(), is(3));
	}
	
	/*
	 * 수정 테스트 보완
	 * 
	 * JDBC개발에서 리소스반환 같은 기본작업을 제외시
	 * 가장 실수가 많이 일어나는 부분은 SQL문장임
	 * 
	 * 하지만 아래의 테스트에서는 수정된 결과값을 확인할 수 없음
	 * 
	 * 해결방법
	 *  1. JdbcTemplate의 update()가 돌려주는 리턴값을 확인하는 방법
	 *  	- jdbcTemplate의 update, delete를 실행시 영향받는 row의 개수를 반환해줌
	 *  2. 테스트를 보강 / 원하는 사용자 외의 정보는 변경되지 않았음을 확인
	 */
	@Test
	public void update() {
		dao.deleteAll();
		
		dao.add(user1);
		dao.add(user2);
		
		user1.setName("newDec7");
		user1.setPassword("password");
		user1.setLevel(Level.GOLD);
		user1.setLogin(1000);
		user1.setRecommend(999);
		
		dao.update(user1);
		
		User user1update = dao.get(user1.getId());
		checkSameUser(user1, user1update);
		
		User user2same = dao.get(user2.getId());
		checkSameUser(user2, user2same);
		
	}
	
	@Test
	public void getAll() throws SQLException {
		dao.deleteAll();

		List<User> users0 = dao.getAll();
		assertThat(users0.size(), is(0));
		
		
		// user1: id: dec1
		dao.add(user1);
		List<User> users1 = dao.getAll();
		assertThat(users1.size(), is(1));
		checkSameUser(user1, users1.get(0));
		
		// user1: id: dec1
		dao.add(user2);
		List<User> users2 = dao.getAll();
		assertThat(users1.size(), is(1));
		checkSameUser(user1, users2.get(0));
		checkSameUser(user2, users2.get(1));
		
		// user1: id: dec1
		dao.add(user3);
		List<User> users3 = dao.getAll();
		assertThat(users1.size(), is(1));
		checkSameUser(user3, users3.get(0));
		checkSameUser(user1, users3.get(1));
		checkSameUser(user2, users3.get(2));
		
		
	}
	

	private void checkSameUser(User user1, User user2) {
		assertThat(user1.getId(), is(user2.getId()));
		assertThat(user1.getName(), is(user2.getName()));
		assertThat(user1.getPassword(), is(user2.getPassword()));
		assertThat(user1.getEmail(), is(user2.getEmail()));
		assertThat(user1.getLevel(), is(user2.getLevel()));
		assertThat(user1.getLogin(), is(user2.getLogin()));
		assertThat(user1.getRecommend(), is(user2.getRecommend()));
		
	}
	

}



