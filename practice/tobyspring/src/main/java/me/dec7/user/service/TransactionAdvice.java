package me.dec7.user.service;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/*
 * Spring의 Advice interface 구현
 */
/*
 * AOP, 관점 지향 프로그래밍
 * 전통적인 객체지향 기술 설계방법
 *  - 부가기능 (독립적인 모듈화가 불가능한 트랜잭션 경계설정과 같은)을 어떻게 모듈화할지 고민
 *  - 부가기능 모듈화 작업은 기존 객체지향 설계 패러다임과 구분되는 특성을 지님
 *  	- 애스팩트 (그래서 새로운 이름을 부여)
 *  		- 어플리케이션의 부가적 기능이지만, 어플리케이션을 구성하는 중요한 한 요소
 *  		- 핵심기능에 부가되어 의미를 갖는 특별한 모듈을 의미함
 *  			- advice, pointcut, advisor
 *  		- 어플리케이션을 구성하는 한가지 측면 
 *  
 * aspect
 *  - 핵심기능 코드 사이에 침투 가능한 부가기능을 독립적인 모듈 --> aspect로 구분
 *  - 2차원적 코드를 3차원적 코드로 확장하여 
 *    어플리케이션은 순수한 기능을 담은 핵심기능과 하나 이상의 부가기능이 런타임시 서로 어우러져
 *    자신이 필요한 위치에 다이나믹하게 참여하게 될 것
 *    
 * AOP (Aspect Oriented Programming)
 *  - 어플리케이션의 핵심적인 기능에서 부가적인 기능을 분리해 aspect라는 모듈로 만들어 설계하고 개발하는 방법
 *  - AOP는 OOP를 돕는 보조적 기술
 *  - AOP는 aspect를 분리함으로써 핵심기능을 설계, 구현시 객체지향적 가치를 지킬 수 있도록 도와주는 것
 *  - AOP는 어플리케이션을 다양한 관점에서 바라보며 개발할 수 있도록 도와줌
 *  	- transaction
 *  		- 사용자 관리라는 핵심 관점에서 transaction 경계설정이라는 관점으로 변경 --> 집중 개발
 * 
 */

/*
 * Proxy를 이용한 AOP
 * AOP
 *  - 스프링은 IoC/DI 컨테이너, Dynamic Proxy, Decorator Pattern, Proxy Pattern,
 *    자동 프록시 생성기법, bean 오브젝트 후처리 조작 기법 등 기술을 조합해 AOP지원
 * 
 * Proxy방식의 AOP
 *  - Proxy를 이용했다는 것 / 간접적 방식
 *  - Spring의 AOP 방식
 *  	- proxy로 만들어 DI로 연결된 bean 사이에 적용해 target의 메소드 호출 과정에 참여, 부가기능 제공
 *  	- 따라서, spring AOP는 자바 기본 JDK, spring container 외 기술/환경이 필요없음
 *  	- spring container인 applicationContext도 환경,/JVM 설정을 요구안함
 *  	- 서버환경은 Servlet container로 충분
 *  	- 독립적으로 갭라한 부가기능 모듈을 다양한 target 오브젝트의 메소드에 다이나믹하게 적용하기 위해 가장 중요한 역할
 *  
 * ByteCode 생성과 조작을 통한 AOP
 *  - AspectJ
 *  	- Proxy를 사용하지 않는 대표적 AOP 기술 / AOP기술 원조
 *  	- Target 오브젝트를 뜯어고쳐 부가기능을 넣어주는 직접적 방법
 *  		- 컴파일된 target의 클래스 파일 자체를 수정
 *  		- 클래스가 JVM에 로딩되는 시점을 가로채 bytecode를 조작하는 방식
 *  	- bytecode를 조작하는 이유
 *  		1) spring과 같은 DI container의 도움을 받아 자동 proxy 생성방식을 사용하지 않아도 됨
 *  		   컨테이너가 사용되지 않는 환경에서 쉽게 적용 가능
 *  		2) Proxy 방식보다 유연 
 *  			- proxy방식은 부가기능을 부여할 대상은 client가 호출할 떄 사용하는 메소드로 제한되나
 *  			  bytecode를 직접 조작시,
 *  			  오브젝트의 생성, 필드값 조회, 조작, 스태틱 초기화 등 다양한 작접에 부가기능 부여 가능
 *  				- proxy는 target 오브젝트 생성 불가
 *  				- private 메소드 호출, static 메소드 호출 / 초기화
 *  				- field 입출력 등 부가기능 부여시
 * 
 * AOP 용어
 *  - target
 *  	- 부가기능을 부여할 대상
 *  	- 핵심기능을 담고 있는 클래스 일수도, 다른 부가기능을 제공하는 proxy 오브젝트일 수도 있음
 *  - advice
 *  	- target에 제공할 부가기능을 담은 모듈
 *  	- class, method level에서 적용 가능
 *  	- 메소드 호출과정시 참여할 수도 / 메소드 호출과정의 일부에서만 동작하는 advice도 있음
 *  - join point
 *  	- advice가 적용될 수 있는 위치
 *  	- spring proxy AOP에서 join point는 method 실행 단계 뿐
 *  		- target 오브젝트가 구현한 interface의 모든 method가 join point가 됨
 *  - pointcut
 *  	- advice를 적용할 join point를 선별하는 작업 / 그 기능을 정의한 모듈
 *  	- spring AOP의 join point는 메소드의 실행
 *  		- 따라서, spring의 pointcut은 method를 선정하는 기능을 갖지고 있음
 *  		- 그래서, pointcut expression은 메소드의 실행이라는 의미를 가지는 execution()으로 시작하고
 *  			메소드의 signiture를 비교하는 방식으로 구현
 *  - proxy
 *  	- client와 target 사이에서 투명하게 존재 --> 부가기능 제공 오브젝트
 *  	- DI를 통해 target대신 client에 주입
 *  	- client의 메소드 호출을 대신 받아 target에 위임 --> 그 과정에서 부가기능 부여
 *  - advisor
 *  	- pointcut, advice를 하나씩 가지고 있는 오브젝트
 *  	- 부가기능을 어디에 전달할지를 알고 있는 AOP의 가장 기본이 되는 모듈
 *  	- spring은 자동 프록시 생성기가 advisor를 AOP 작업의 정보로 사용
 *  		- 따라서 advisor는 spring에서만 사용되는 특별한 용어
 *  - aspect
 *  	- OOP:Class = AOP:Aspect
 *  	- AOP의 기본 모듈
 *  	- 한개 혹은 그 이상의 pointcut과 advice의 조합으로 만들어짐
 *  	- 보통 singleton 형태의 오브젝트로 존재 
 */


public class TransactionAdvice implements MethodInterceptor {
	
	PlatformTransactionManager transactionManager;

	public void setTransactionManager(PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	/*
	 * (non-Javadoc)
	 * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
	 * 
	 * target을 호출하는 기능을 가진 콜백 오브젝트를 proxy로부터 받음
	 * advice는 특정 target에 의존하지 않고 재사용 가능
	 */
	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		TransactionStatus status = this.transactionManager.getTransaction(new DefaultTransactionDefinition());
		
		try {
			/*
			 * callback을 호출해서 target의 메소드를 실행,
			 * target 메소드 호출 전후로 필요한 부가기능을 넣을 수 있음
			 * 
			 * 경우에 따라, target이 아예 호출되지 않게하거나 재시도를 위한 반복적 호출도 가능
			 */
			Object ret = invocation.proceed();
			
			this.transactionManager.commit(status);
			return ret;
			
		/*
		 * JDK dynamic proxy가 제공하는 method와 다르게 
		 * spring의 MethodInvocation을 통한 target 호출은 예외가 포장되지 않고 target에서 보낸 그대로 전달
		 */
		} catch (RuntimeException e) {
			this.transactionManager.rollback(status);
			throw e;
		}
		
	}
}










