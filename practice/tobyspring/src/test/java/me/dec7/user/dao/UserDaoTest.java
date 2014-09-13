package me.dec7.user.dao;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import me.dec7.user.domain.User;

import org.junit.Before;
import org.junit.Test;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;


public class UserDaoTest {
	private UserDao dao;
	
	private User user1;
	private User user2;
	private User user3;
	
	@Before
	public void setUp() {
		user1 = new User("dec1", "동규1", "111");
		user2 = new User("dec2", "동규2", "222");
		user3 = new User("dec0", "동규3", "333");
		/*
		 * 3번째 방법
		 */
		dao = new UserDao();
		
		DataSource dataSource = new SingleConnectionDataSource(
				"jdbc:mysql://localhost/springbooktest",
				"spring",
				"book",
				true);
		dao.setDataSource(dataSource);
		
	}
	
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
		assertThat(userget1.getName(), is(user1.getName()));
		assertThat(userget1.getPassword(), is(user1.getPassword()));
		
		User userget2 = dao.get(user2.getId());
		assertThat(userget2.getName(), is(user2.getName()));
		assertThat(userget2.getPassword(), is(user2.getPassword()));
		
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
	 * 모든 사용자 정보를 다 가져오는 getAll()에 대한 테스트
	 * 
	 * 반환: List<User>
	 * 순서: 기본키인 id 순 정렬
	 * 
	 * 이것을 코드화하면 --> 테스트 코드가 됨
	 * 
	 * 테스트 검증방법
	 *  - User타입 오브젝트 user1, user2, user3 DB등록
	 *  - List<User> 타입 반환
	 *  - 크기 3
	 *  - id 순서대로 담겨야함.
	 *  - 동등성 비교
	 *  
	 * 최소 두 가지 이상 테스트 조건에 대해 기대한 결과를 확인해야함
	 */
	@Test
	public void getAll() throws SQLException {
		dao.deleteAll();
		
		/*
		 * getAll의 네거티브 테스트
		 * 결과가 하나도 없는 상황
		 * 
		 * 네거티브 테스트부터 만들기!@!
		 * 
		 * 
		 * JdbcTemplate의 query() 메소드는 예외시 0을 반환하는게 정해져 있는데 이것을 왜 테스트해야하는가?
		 *  - UserDao를 사용하는 입장에서 getAll()이 어떻게 구현되어있는지 모름, 알필요도 없음
		 *  - getAll()이 어떻게 동작하는지만 관심.
		 *  - UserDaoTest 클래스의 UserDao의 getAll()이라는 메소드의 기대 동작방식에 대한 검증이 먼저
		 *  - 그러므로 예상값을 모두 검증하는게 옳다.
		 */
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
		
	}
	

}



