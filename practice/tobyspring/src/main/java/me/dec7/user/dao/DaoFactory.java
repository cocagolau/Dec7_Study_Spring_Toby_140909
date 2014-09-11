package me.dec7.user.dao;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

/*
 * 1.7.3 / Dependency Lookup (의존관계 검색)
 * 
 * dependency lookup
 *  - IoC 방법에는 dependency injection 뿐 아니라
 *    코드에서는 클래스에 의존하지 않고 런타임시 의존관계를 결정한다는 점에서
 *    의존관계를 맺는 방법이 외부로부터 주입하는 것이 아니라 스스로 검색을 이용하는 것
 *    
 *  - 런타임시 의존관계를 맺을 오브젝트를 결정하는 것, 오브젝트 생성 --> IoC container에 맡김
 *  - 이를 가져올 때 setter method나, constructor를 이용하는 대신 스스로 container에게 요청하는 방법 

	public UserDao() {
		DaoFactory daoFactory = new DaoFactory();
		this.connectionMaker = daoFactory.connectionMaker();
	}

 * UserDao는 여전히 어떤 ConnectionMaker 오브젝트를 사용할지 미리 모름 / 의존대상은 ConnectionMaker interface 뿐
 * 적용방법은 외부로부터 주입받는 것이 아닌 스스로 IoC container에게 요청하는 것
 * spring applicationContext의 경우 미리 정해높은 이름을 전달해 그 이름에 해당하는 오브젝트를 찾게됨


	// 의존관계 검색을 이용하는 UserDao constructor
	public UserDao() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(DaoFactory.class);
		this.connectionMaker = context.getBean("connectionMaker", ConnectionMaker.class);
	}

 * 
 * dependency lookup 장점
 *  - 방법만 다를뿐 의존관계 주입의 거의 모든 장점을 다 가짐
 *  	- 하지만, 의존관계 검색시 spring api가 사용되므로 깔끔하지 못함 
 *  	- 그리고, application component가 성격이 다른 object에 의존
 *  
 *  - 의존관계 검색시 검색한 오브젝트 자신이 spring bean일 필요가 없다는 점 
 *  	- UserDaoTest..
 */

/*
 * 1.7.5 / method를 이용한 DI
 * 
 * setter method를 이용한 주입
 *  - 파라미터로 전달된 값을 내부의 인스턴스 변수에 저장함
 *  - set으로 시작하고 한 번에 한 개의 파라미터만 가질 수 있음 
 *  
 * 일반 method를 이용한 주입
 *  - setter method의 제약이 싫은 경우
 *  - 한 번에 여러 개의 파라미터를 받을 수 있음 
 *  - 실수하기 쉬워지지만 한번에 처리하는 경우 사용됨
 * 
 */
@Configuration 
public class DaoFactory {
	
	@Bean
	public UserDao userDao() {
		
//		return new UserDao(connectionMaker());
		
		// setter method DI 방식으로 수정되어 설정도 변경
		UserDao dao = new UserDao();
		dao.setDataSource(dataSource());
		
		return dao;
	}
	
	@Bean
	public AccountDao accountDao() {
		
		return new AccountDao(connectionMaker());
	}
	
	@Bean
	public MessageDao messageDao() {
		
		return new MessageDao(connectionMaker());
	}
	
	@Bean
	public ConnectionMaker realConnectionMaker() {
		
		return new SimpleConnectionMaker();
	}
	
	// 새로운 의존관계를 컨테이너가 사용할 사용정보를 만듦
	// 모든 dao는 여전히 connectionMaker()에서 만들어지는 오브젝트를 DI 받음
	@Bean
	public ConnectionMaker connectionMaker() {
		return new CountingConnectionMaker(realConnectionMaker());
	}
	
	@Bean
	public DataSource dataSource() {
		SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
		
		dataSource.setDriverClass(com.mysql.jdbc.Driver.class);
		dataSource.setUrl("jdbc:mysql://localhost/springbook");
		dataSource.setUsername("spring");
		dataSource.setPassword("book");
		
		return dataSource;
	}
	
}
