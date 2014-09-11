package me.dec7.user.dao;

import java.sql.SQLException;

import me.dec7.user.domain.User;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;

/*
 * Spring IoC Container
 *  - Dependency Injection / DI container
 *  
 * DI
 *  - Object에 다른 Object를 주입할 수 있는건 아님 / Object의 reference가 전달될 뿐
 *  - 오브젝트 레퍼런스를 외부로부터 주입(제공)받고 이를 통해 오브젝트와 다이나믹하게 의존관계가 만들어지는 것이 핵심
 */

/*
 * DI 받는다
 *  - 외부에서 파라미터로 오브젝트를 전달했다고 해서 모두 DI가 아님을 주의 
 *  - 주입받은 메소드 파라미터가 특정 클래스 타입으로 고정시 DI가 일어날 수 없음
 *  - DI에서 말하는 주입
 *  	- 다이나믹하게 구현 클래스를 결정
 *  	- 제공받을 수 있도록 interface 타입의 파라미터를 통해 이뤄져야 함
 *  	- 따라서 주입받는다는 표현 --> DI 받는다.
 */
public class UserDaoTest {

	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		
		/*
		 * ApplicationContext 설정 방식을 변경
		 * 
		ApplicationContext context = new AnnotationConfigApplicationContext(DaoFactory.class);
		
		GenericXmlApplicationContext 외에도
		ClassPathXmlApplicationContext를 이용해 XML로 설정정보를 가져오는 ApplicationContext를 만들 수 있음
		
		GenericXmlApplicationContext
		 - classpath뿐 아니라 다양한 소스로부터 설정파일을 읽어올 수 있음
		 
		ClassPathXmlApplicationContext
		 - Xml파일을 클래스패스에서 가져올 때 사용할 수 있는 편리한 기능 추가됨
		 - ex) 기능 중 클래스패스의 경로정보가 클래스에서 가져오도록 하는 기능
		 	- springbook.user.dao 패키지 내부의 daoContext.xml 설정파일시
		 		- GenericXmlApplicationContext
		 			- root로부터 파일의 위치를 지정해야함.
		 			- new GenericXmlApplicationContext("springbook/user/dao/daoContext.xml");
		 		- ClassPathXmlApplicationContext
		 			- daoContext.xml과 같은 클래스패스위에 있는 UserDao를 함께 제공
		 			- new ClassPahtXmlApplicationContext("daoContext.xml", UserDao.class);
		*/
		ApplicationContext context = new GenericXmlApplicationContext("classpath:/applicationContext.xml");
		UserDao dao = context.getBean("userDao", UserDao.class);
		
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

	}
	
}
