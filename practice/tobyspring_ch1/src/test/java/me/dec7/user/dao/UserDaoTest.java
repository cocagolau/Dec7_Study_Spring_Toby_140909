package me.dec7.user.dao;

import java.sql.SQLException;

import me.dec7.user.domain.User;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;



/*
 * 1.5.1 / 스프링의 IoC
 *  - 스프링을 많은 기능을 제공하지만 그 중 핵심은
 *    ApplicationContext 혹은 BeanFactory 이다. / DaoFactory의 일반화 버전
 */

/*
 * Bean
 *  - Spring이 제어권을 가지고 직접 만들고 관계를 부여하는 Object
 *  - managed object (관리되는 오브젝트)라 불리기도 함
 *  - Object 단위의 Application Component
 *  - Spring Bean은 Spring Container가 생성, 관계설정, 사용 등을 제어해주는 IoC가 적용된 Object
 *  - application에서 만들어지는 모든 object가 bean이 아니고 spring이 직접 생성, 제어를 담당하는 object만 bean임
 */

/*
 * BeanFactory
 *  - Spring의 IoC를 담당하는 핵심 container
 *  - Spring에서 Bean의 등록, 생성, 조회, 반환, 관계설정 등 부가적인 bean을 관리하는 기능 담당
 *  - 이를 확장한 ApplicationContext를 주로 사용
 *  - 따라서 BeanFactory와 ApplicationContext는 동일하다고 봐도 됨
 *  
 *  - Bean의 생성, 관계를 설정하는 IoC의 기본기능에 초점을 맞춘 의미
 */

/*
 * ApplicationContext
 *  - BeanFactory를 확장한 IoC Container
 *  - 별도의 정보를 참고하여 Bean 생성, 관계설정 등 제어작업 총괄
 *  - 설정정보를 만드는 방법은 여러가지 존재함
 *  - Application 전반에 걸쳐 모든 구성요소의 제어 작업을 담당하는 IoC 엔진의 의미가 부각됨
 */

/*
 * 설정정보, 설정 메타정보
 *  - ApplicationContext, BeanFactory가 IoC를 적용하기 위해 사용하는 메타 정보 
 *  - IoC container에 의해 관리되는 Application Object를 생성하고 구성할 때 사용됨 
 */

/*
 * ApplicationContext, BeanFactory가 사용할 설정정보라는 표시
 *  - Spring이 BeanFactory를 위한 Object 설정을 담당하는 클래스임을 인식할 수 있도록 함
 */

/*
 * Container, IoC Container
 *  - IoC방식으로 Bean을 관리한다는 의미
 *  - Container라는 말 자체가 IoC의 개념을 담고 있음
 *  - IoC container는 BeanFactory 관점에서
 *  - Container/Spring Container는 ApplicationContextr 관점에서 봄
 */
public class UserDaoTest {

	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		
		/*
		 * 이제 DaoFactory를 설정정보로 사용하는 ApplicationContext를 생성
		 * 
		 * ApplicationContext는 ApplicationContext 타입의 Object
		 * 
		 * AnnotationConfigApplicationContext
		 *  - @Configuration이 붙은 자바 코드를 설정정보로 이용하기 위한 Class
		 */
		
		/*
		 * ApplicationContext 동작방식
		 *  - IoC Container 혹은 Spring Container라 부름
		 *  - ApplicationContext interface 구현
		 *  - ApplicationContext는 BeanFactory interface를 상속했으므로 BeanFactory의 일종
		 *  - IoC를 적용해서 관리할 모든 Object의 생성, 관계설정을 담당함 (제한적이이 않음)
		 *  - 대신 생성, 관계정보를 별도의 설정정보를 통해 얻음
		 *  
		 *  1. ApplicationContext는 DaoFactory를 설정정보로 등록
		 *  2. @Bean이 붙은 method 이름을 가져와 Bean 목록을 만듦
		 *  3. Client가 ApplicationContext의 getBean() 호출시 bean 목록에서 존재여부 찾고 있을 경우 Bean을 생성 후 반환
		 *  
		 * DaoFactory가 아닌 ApplicationContext를 사용시 장점 
		 *  - Client는 구체적인 factory class를 알 필요 없다.
		 *  	- Application이 발전하여 DaoFactory같은 IoC를 적용한 Object도 추가됨
		 *  	- ApplicationContext 사용시 ObjectFactory가 많아져도 신경쓸 필요 없음
		 *  - 종합 IoC 서비스를 제공해줌
		 *  	- Object가 만들어지는 방식, 시점, 전략을 다르게 설정할 수 있음
		 *  	- 부가적으로 자동생성, 오브젝트 후처리, 정보 조합, 설정방식 다변화, 인터셉팅 등
		 *  	  오브젝트를 효과적으로 활용할 수 있는 다양한 기능을 제공
		 *  	- Bean이 사용할 수 있는 기반기술 서비스, 외부서비스 연동 등.. container 차원에서 제공
		 *  - 다양한 방식으로 Bean 검색 가능
		 *  	- getBean()을 이용해 검색
		 *  	- 이름, 타입, 특별한 어노테이션으로 되어있는 bean을 찾을 수 있음  
		 */
		ApplicationContext context = new AnnotationConfigApplicationContext(DaoFactory.class);
		
		/*
		 *  getBean() method
		 *   - 준비된 Object를 가져올 수 있음
		 *   - 기본적으로 Object 타입을 반환
		 *   - 제네릭 메소드방식을 이용해 두번째 인자로 반환타입을 전달하면 Casting하지 않아도 됨
		 */
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
