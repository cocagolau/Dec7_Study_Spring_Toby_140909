package me.dec7.user.dao;

import java.sql.SQLException;

import me.dec7.user.domain.User;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class UserDaoConnectionCountingTest {
	
	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(DaoFactory.class);
		UserDao dao = context.getBean(UserDao.class);
		
		// DB 사용코드
		// ---------------
		User user = new User();
		user.setId("dec7");
		user.setName("동규");
		user.setPassword("127");
		
		dao.add(user);
		System.out.println(user.getId() + "등록 성공");
		
		User user2 = dao.get(user.getId());
		System.out.println(user2.getName());
		System.out.println(user2.getPassword());
		
		System.out.println(user2.getId() + "조회 성공");
		// -----------------
		
		
		CountingConnectionMaker ccm = context.getBean(CountingConnectionMaker.class);
		System.out.println("Connection counter: " + ccm.getCounter());
		
	}
}
