package me.dec7.user.dao;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.sql.SQLException;

import javax.sql.DataSource;

import me.dec7.user.domain.User;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/*
 * SimpleDriverDataSource와 DataSource로 선언하는 방법 중 어느방법이 좋은가?
 *  - bean을 어느 용도로 사용할지에 따라 다름
 * 
 * DataSource
 *  - 단순히 DataSource에 정의된 메소드를 테스트에서 사용하고 싶은 경우
 *  - bean의 구현 클래스가 변경되더라도 테스트 코드는 변경 없음
 *  
 * SimpleDriverDataSource
 *  - 이 타입 그자체의 오브젝트에 관심이 있는 경우
 *  
 * 하지만 보통 interface를 두고 DI를 적용해야함
 *  1. 모든 것은 항상 바뀐다.
 *  2. interface를 두고 DI를 적용하면 다른 서비스도 적용 가능
 *  	- ex) DB connection 수를 counting하는 코드
 *  	- 무언가 새로운 기능을 위해 코드를 수정할 필요는 없음
 *  	- 추가했던 기능도 설정파일로 간단히 수정 가능
 *  3. 테스트
 *  	- 단지 효율적일 테스트를 위해서라도
 *  
 *  
 * 테스트 코드에 의한 DI
 *  - UserDao 에성 DI container가 의존관계 주입에 사용하도록 setter method로 만듦
 *  	- UserDao가 사용할 DataSource 오브젝트를 테스트 코드에서도 변경 가능
 *  	- 테스트시 applicationContext.xml에 정의된 DataSource를 사용해도 되는가?
 *  		- 만약 테스트시 deleteAll()로 모든 정보가 삭제된다면 ...
 * - 테스트시 Dao가 사용할 DataSource 오브젝트를 바꿔주는 방법을 사용   
 */

// 3번째 방법 적용으로 해제
//@RunWith(SpringJUnit4ClassRunner.class)

//@ContextConfiguration(locations="classpath:/applicationContext.xml")
/*
 * 설정 파일을 이원화하여 test용 전용 설정파일을 사용
 * 이 경우 수동 DI나 @dirtiesContext필요 없음
 */
//3번째 방법 적용으로 해제
//@ContextConfiguration(locations="classpath:/test-applicationContext.xml")
/*
 *  test method에서 ApplicationContext의 구성이나 상태를 변경하는 것을 TestContextFramework에 알려줌
 *  Spring TestContextFramework 사용시 다음 테스트에 ApplicationContext를 새로 생성해 공유
 *  class, method level에서 사용 가능
 */
//@DirtiesContext
public class UserDaoTest {

//	@Autowired
//	private ApplicationContext context;
	
//	@Autowired
	private UserDao dao;
	
	private User user1;
	private User user2;
	private User user3;
	
	@Before
	public void setUp() {
		user1 = new User("dec1", "동규1", "111");
		user2 = new User("dec2", "동규2", "222");
		user3 = new User("dec3", "동규3", "333");
		/*
		 * 3번째 방법
		 */
		dao = new UserDao();
		DataSource dataSource = new SingleConnectionDataSource(
				"jdbc:mysql://localthost/springbooktest",
				"spring",
				"book",
				true);
		dao.setDataSource(dataSource);
		
		
		
		// 테스트에서 UserDao가 사용할 DataSource 오브젝트를 직접 생성
		/*
		 * 1번째 방법
		DataSource dataSource = new SingleConnectionDataSource(
				"jdbc:mysql://localthost/springbooktest",
				"spring",
				"book",
				true);
		dao.setDataSource(dataSource);
		*/
		
		/*
		 * 장점
		 *  - XML 설정파일을 수정하지 않고 테스트코드를 통해 오브젝트 관계를 재구성 가능
		 *  	- 특별한 상황을 구성할 수 있음
		 *  
		 * 하지만 applicationContext.xml 파일의 설정정보를 강제로 변경했으므로 조심해야함
		 * Spring TestContextFramework를 적용시 ApplicationContext는 test 중 한 개만 만들어지고 공유됨
		 * 따라서 ApplicationContext의 구성/상태를 테스트내에서 변경하지 않는 것이 원칙
		 * 
		 * 1. @DirtiesContext 어노테이션 사용 방법
		 * 		- Spring TestContextFramework에 ApplicationContext의 변경을 알리고 이 테스트 클래스에는
		 *  	- ApplicationContext를 공유하지 않음 / Test method를 수행 후 새로운 ApplicationContext를 만들어서 다음 테스트가 사용하도록 함
		 * 
		 * 2. TEST를 위한 별도의 DI 설정 / 전용 설정파일 준비
		 *  	- test시 사용될 DataSource를 미리 설정
		 *  
		 * 3. spring container 없는 DI테스트
		 * 		- UserDaoTest는 UserDao가 동작함을 확인하려기보다 Dao 자체의 기능을 test하려는 목적
		 * 		- 매번 UserDao를 생성해야하는 불편함 존재
		 * 
		 * 
		 * 어떤 테스트를 해야하는가?
		 *  - 모두 장단이 존재하지만
		 *    항상 Spring Container 없이 사용할 수 있는 방법을 우선 고려
		 *    가장 빠르고 테스트 자체가 간결
		 *    
		 *  - 복잡한 의존관계를 가진 오브젝트인 경우 Spring을 이용하면 간편
		 *  - 테스트용 설정파일을 따로 관리하는 것이 좋음
		 *  
		 *  - 예외적 의존관계를 설정시 @DirtiesContext사용
		 */
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

}



