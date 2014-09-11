package me.dec7.user.dao;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.sql.SQLException;

import me.dec7.user.domain.User;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;

/*
 * UserDaoTest 문제점
 *  - 테스트 진행과정이 모두 수동
 *  	- 결과를 눈으로 확인
 *  
 *  - 실행의 번거로움
 *  	- 테스트당 다른 main()을 보유, dao가 늘어나면 main()도 늘어남
 */
public class UserDaoTest {
	
	/*
	 * 개선2, 실행의 번거로움
	 * 
	 * JUnit 사용
	 * 
	 * 프레임워크의 기본원리가 IoC임.
	 * 따라서 test를 main()으로 한다는 것은 제어권을 직접 갖는다는 의미로 프레임워크에 적합하지 않음
	 * 
	 * main() method --> 일반 method
	 */
	
	@Test
	public void addAndGet() throws SQLException, ClassNotFoundException {
		ApplicationContext context = new GenericXmlApplicationContext("classpath:/applicationContext.xml");
		UserDao dao = context.getBean("userDao", UserDao.class);
		
		User user = new User();
		user.setId("dec7");
		user.setName("동규");
		user.setPassword("127");
		
		// add()
		dao.add(user);
		
		// get()
		User user2 = dao.get(user.getId());
		
		// test결과
		/*
		if (!user.getName().equals(user2.getName())) {
			System.out.println("테스트 실패 (name)");
			
		} else if (!user.getPassword().equals(user2.getPassword())) {
			System.out.println("테스트 실패 (password)");
			
		} else {
			System.out.println("조회 테스트 성공");
			
		}
		*/
		assertThat(user2.getName(), is(user.getName()));
		assertThat(user2.getPassword(), is(user.getPassword()));
	}
	
	
	/*
	 * JUnit 테스트 실행
	 * 
	 * JUnit도 spring container처럼 어디선가 한번은 JUnit framework를 실행해야됨
	 */
	public static void main(String[] args) {
		JUnitCore.main("me.dec7.user.dao.UserDaoTest");
	}
}



