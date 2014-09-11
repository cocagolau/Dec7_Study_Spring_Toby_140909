package me.dec7.user.dao;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.sql.SQLException;

import me.dec7.user.domain.User;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.dao.EmptyResultDataAccessException;

/*
 * P183, JUnit 테스트 수행 방식
 * 
 * Framework는 스스로 주도권을 가지고 주도적으로 동작
 * 개발자가 만든 코드는 프레임워크에 의해 수동적으로 실행
 * 그러므로 사용되는 코드만으로 실행흐름이 보이지 않으므로 프레임워크가 어떻게 사용할지 잘 알아야함
 * 
 * 
 * 1. 테스트 클래스에서 @Test가 붙은 public, void, parameter가 없는 테스트 메소드를 모두 찾음 
 * 2. 테스트 클래스의 오브젝트를 하나 만든다,
 * 3. @Before가 붙은 메소드가 있으면 실행
 * 4. @Test가 붙은 메소드를 하나 호출하고 테스트 결과를 저장
 * 5. @After가 붙은 메소드가 있으면 실행
 * 6. 나머지 테스트 메소드에 대해서 2~5번 반속
 * 7. 모드 테스트결과를 종합해서 전달
 * 
 * 중요
 *  - 테스트 메소드를 실행할 때마다 테스트 클래스의 오브젝트를 새로 만듦
 *  - 테스트 클래스가 @Test 테스트 메소드를 두개 가지고 있다면
 *    테스트 실행중 JUnit은 클래스의 오브젝트를 두 번 만듦
 *    
 * 왜? 테스트마다 오브젝트를 만드는가?
 *  - 각 테스트가 서로 영향을 주지 않고 독립적으로 실행됨을 확실히 보장하기 위함
 *  - 만약 테스트 메소드 일부에서 공통적으로 사용되는 코드가 있는 경우
 *  	- @Before를 사용하기 보다
 *  	  일반적인 메소드 추출 방법을 사용하여 메소드 분리 후 테스트 메소드에서 직접 호출하도록 함
 *  
 *  
 * 픽스처
 *  - 테스트를 수행하는 데 필요한 정보나 오브젝트
 *  - 반복적으로 생성되므로 일반적으로 @Before에 생성해두면 편리
 */


/*
 * 2.3.1, JUnit 테스트 실행방법
 * 
 *  1. JUnitCore를 사용해 콘솔에 결과를 확인
 *   - 가장 간단하지만 테스트 수가 많아지면 관리 어려움 
 *   
 *  2. 보통 자바 IDE에 내장된 JUnit 테스트 지원도구 사용
 */


/*
 * 포괄적인 테스트
 * 개발자는 항상 긍정적인 테스트 코드만 작성하는 경향이 있음
 * 
 * "항상 네거티브 테스트를 먼저 만들라" 스프링 창시자 로드 존슨
 *  - ex) get() method에서 id가 존재하지 않는 경우
 */

/*
 * 기능설계를 위한 테스트
 *  - 추가하고 싶은 기능을 먼저 코드로 표현 --> 테스트 코드
 *  - getUserFailure()
 *  	- 조건: 어떤 조건을 가지고 / 가져올 사용자 정보가 존재하지 않는 경우
 *  	- 행위: 무엇을 할 때	/ 존재하지 않는 id로 get()을 실행하면
 *  	- 결과: 어떤 결과가 나옴	/ 특별한 예외가 던져짐
 * 
 * 테스트 주도 개발
 *  - 테스트를 만들어가며 개발하는 방법이 주는 장점을 극대화한 개발 프로세스
 *  - "실패한 테스트를 성공시키기 위한 목적이 아닌 코드는 만들지 않는다" 
 */
public class UserDaoTest {
	
	/*
	 * 2.3.5, 테스트 코드 개선
	 * 
	 * 반복코드 별도의 메소드로 추출
	 */
	private UserDao dao;
	private User user1;
	private User user2;
	private User user3;
	
	// @Test(매번 테스트) 메소드를 실행하기 전에 먼저 실행돼야 하는 메소드를 정의
	@Before
	public void setUp() {
		ApplicationContext context = new GenericXmlApplicationContext("classpath:/applicationContext.xml");
		
		// 픽스쳐 생성
		dao = context.getBean("userDao", UserDao.class);
		
		user1 = new User("dec1", "동규1", "111");
		user2 = new User("dec2", "동규2", "222");
		user3 = new User("dec3", "동규3", "333");
	}
	
	
	/*
	 * Test결과의 일관성
	 * 
	 * 매번 UserDaoTest 테스트를 실행하기 전 DB의 User 데이터를 모두 삭제해줘야 했음
	 *  - 테스트가 외부 상태에 따라 결과가 달라짐 
	 *  - 코드에 변경사항이 없다면 테스트는 항상 동일해야 함
	 *  
	 * UserDaoTest 문제
	 *  - 이전 테스트 때문에 DB에 등록된 중복 데이터가 있을 수 있다는 점
	 *  - 가장 좋은 해결책은 테스트 후 테스트 정보를 삭제해서 이전 상태를 만들어 주는 것
	 *  	- deleteAll(), getCount() 추가
	 */
	
	@Test
	public void addAndGet() throws SQLException, ClassNotFoundException {
// 중복으로 추출
//		ApplicationContext context = new GenericXmlApplicationContext("classpath:/applicationContext.xml");
//		UserDao dao = context.getBean("userDao", UserDao.class);
		
		// db 초기화
		dao.deleteAll();
		assertThat(dao.getCount(), is(0));
		// 픽스쳐 @Before에 모음
		// User user1 = new User("dec1", "동규1", "111");
		// User user2 = new User("dec2", "동규2", "222");
		// user.setId("dec7");
		// user.setName("동규");
		// user.setPassword("127");
		
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
	
	/*
	 *  get() 예외 조건에 대한 테스트
	 *  
	 *  get()시 전달된 id에 해당하는 값이 없는 경우
	 *   1. null과 같은 특별한 값을 반환
	 *   2. 정보를 찾을 수 없다는 예외를 던짐
	 *   
	 *  보통 테스트 중 예외가 발생하면 테스트 메소드는 중단되고 테스트는 실패
	 *  하지만 이 경우, 특정 예외가 발생하면 테스트가 성공, 그렇지 않으면 실패로 간주 해야함
	 *  
	 *  예외는 비교할 수 있는 성질이 아니므로 assertThat() 메소드로 검증 불가
	 *  JUnit은 예외조건 테스트를 위한 새로운 방법을 제공함 
	 */
	
	/*
	 *  테스트 중 발생할 것으로 기대되는 예외 클래스를 지정함
	 * 
	 *  이 경우 보통의 테스트와 반대로
	 *  정상적으로 테스트가 마치면 테스트가 실패하고
	 *  expected에 지정한 예외가 발생하면 테스트간 성공함
	 */
	@Test(expected=EmptyResultDataAccessException.class)
	public void getUserFailure() throws SQLException, ClassNotFoundException {
//		ApplicationContext context = new GenericXmlApplicationContext("applicationContext.xml");
//		UserDao dao = context.getBean("userDao", UserDao.class);
		
		dao.deleteAll();
		assertThat(dao.getCount(), is(0));
		
		/*
		 * 예외 발생지점
		 * get() method에서 EmptyResultDataAccessException 예외가 발생하지 않아 테스트 실패
		 * 
		 * UserDao.get() method 수정
		 */
		dao.get("unknown_id");
	}
	
	
	/*
	 * addAndGet(), count() test는 어떤 순서로 실행될지 모름
	 *  - JUnit은 특정한 테스트 메소드의 실행순서를 보장하지 않음
	 *  - 테스트 결과가 테스트 실행순서에 영향을 받는다면 테스트를 잘못 만든 것
	 * 
	 */
	@Test
	public void count() throws SQLException, ClassNotFoundException {
//		ApplicationContext context = new GenericXmlApplicationContext("applicationContext.xml");
//		UserDao dao = context.getBean("userDao", UserDao.class);
		
//		User user1 = new User("dec1", "서", "ddeecc11");
//		User user2 = new User("dec2", "동", "ddeecc22");
//		User user3 = new User("dec3", "규", "ddeecc33");
		
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
	 * JUnit 테스트 실행
	 * 
	 * JUnit도 spring container처럼 어디선가 한번은 JUnit framework를 실행해야됨
	 * 
	 *  --> Java IDE가 제공하는 기능을 사용하자 
	 *  	
	public static void main(String[] args) {
		JUnitCore.main("me.dec7.user.dao.UserDaoTest");
	}
	
	*/
}



