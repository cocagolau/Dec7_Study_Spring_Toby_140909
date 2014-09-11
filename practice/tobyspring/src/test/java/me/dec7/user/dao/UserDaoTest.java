package me.dec7.user.dao;

import java.sql.SQLException;

import me.dec7.user.domain.User;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;


/*
 * spring이 개발자에게 제공하는 가장 중요한 가치
 *  1. 객체지향
 *  2. 테스트
 *  
 *  Spring의 핵심은 IoC/DI는 오브젝트으 설계, 생성, 관계, 사용에 관한 기술
 *   - 대규모 어플리케이션을 객체지향적 방법으로 효과적으로 구현
 *   - 만들어진 코드를 확신할 수 있게 하고
 *   - 변화에 유연하게 대처할 수 있도록 하는 기술
 */

/*
 * UserDaoTest 덕분에
 *  - add(), get() method의 결과를 눈으로 확인가능
 *  - 다양한 방법으로 수정해도 기능이 동작하는지 확인
 *  - 기능이 돌아갔기 때문에 코드를 더욱 효율적으로 수정할 수 있었음
 *  
 * 테스트는 예상이 맞았는지 확인하는 작업
 * 
 * 특징
 *  - main() method 사용
 *  - 테스트 대상인 userDao object를 가져와 method 호출
 *  - 테스트 입력값인 user를 직접 만들어 사용
 *  - 결과를 콘솔에 출력
 *  - 에러가 없다면 성공메시지가 콘솔에 출력
 *  
 * 작은 단위의 테스트
 *  - 테스트 단위가 커질 경우 수행과정도 복잡해지고 오류발생시 원인을 찾기도 어려움
 *  - 관심사의 분리는 테스트에도 적용
 *  - UserDaoTest는 한가지 관심사에 집중됨
 * 
 * Unit Test, 단위 테스트
 *  - 작은 단위의 코드에 대해 테스트를 수행한 것
 *  - 단위
 *  	- 충분히 하나의 관심에 집중해서 테스트할 만한 범위
 *  - 이유
 *  	- 개발자가 설계하고 만든 코드가 원래 의도대로 동작하는지 스스로 빨리 확인받기 위해서
 *  	- 따라서 대상와 조건이 간단, 명확할 수록 좋음
 *  
 * 자동수행 테스트 코드는 중요
 *  - 수행이 간편하므로 자주 반복할 수 있음
 *  - 어플리케이션을 구성하는 클래스 안에 테스트 코드를 넣기보다 테스트용 클래스를 따로 만드는 것이 좋음
 *  
 * 지속적인 개선, 점진적인 개발을 위한 테스트
 *  - 테스트는 완성도 높은 코드를 위해 중요
 *  - 수정하는 과정에서 기능이 잘 동작한다는 것을 지속적으로 확인할 수 있음
 */

/*
 * UserDaoTest 문제점
 *  - 테스트 진행과정이 모두 수동
 *  	- 결과를 눈으로 확인
 *  
 *  - 실행의 번거로움
 *  	- 테스트당 다른 main()을 보유, dao가 늘어나면 main()도 늘어남
 */
public class UserDaoTest {

	public static void main(String[] args) throws ClassNotFoundException, SQLException {
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
