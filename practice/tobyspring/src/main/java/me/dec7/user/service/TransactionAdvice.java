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

/*
 *  6.6, 트랜잭션 속성
 *  
 *  트랜잭션 추상화 <PlatformTransactionManager>
 *   - 가져올 때 사용한 DefaultTransactionDefinition 오브젝트
 */

/*
 * 선언적 트랜잭션
 *  - AOP를 이용해 코드외부에서 트랜잭션 기능 부여, 속성 지정
 *  
 * 프로그램에 의한 트랜잭션
 *  - TransactionTemplate이나 개별 데이터 기술의 트랜잭션 api를 사용해 직접 코드 안에서 사용하는 방법
 * 
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
		/*
		 * 트랜잭션 시작?
		 *  - transactionManager.getTransaction();
		 *  - 트랜잭션 시작을 가져온다고 하는 이유?
		 *  	- 트랜잭션 전파 속성이 있기 때문, 따라서 항상 새로운 트랜잭션이 시작하는 것이 아님,
		 * 
		 * 트랜잭션 정의
		 *  - DefaultTransactionDefinition()
		 *  
		 * 트랜잭션 경계
		 * 
		 * 트랜잭션 종료
		 * 	- transactionManager.commit(status);
		 *  - transactionManager.rollback(status);
		 *  
		 *  
		 * 트랜잭션 정의
		 *  - 더 이상 쪼갤 수 없는 최소 단위 작업 / 기본개념 유효
		 *  - 모두 같은 방식으로 동작은 안함
		 *  
		 * TransactionDefinition interface의 4가지 속성
		 *  - DefaultTransactionDefinition이 구현함
		 *  1) transaction propagation (트랜잭션 전파)
		 *  	- 트랜잭션 경계에서 이미 진행중이 트랜잭션이 존재할 때 혹은 없을 때 어떻게 동작할 결정하는 방식
		 *  	- A 트랜잭션 아직 끝나지 않은 시점 / B 호출시 B의 코드는 어떤 트랜잭션 안에서 동작해야 하는가?
		 *  		a) B코드는 A에서 이미 시작한 트랜잭션에 참여
		 *  		b) B의 트랜잭션은 A와 무관하게 독립적으로 생성
		 *  
		 *  	- PROPAGATION_REQUIRED
		 *  		- 진행 중인 트랜잭션이 없으면 새로 생성
		 *  		- 진행 중인 트랜잭션이 존재시 이에 참여
		 *  		- A, B, A->B, B->A
		 *  		- DefaultTransactionDefinition의 트랜잭션 전파 속성
		 *  
		 *  	- PROPAGATION_REQUIRES_NEW
		 *  		- 항상 새로운 트랜잭션 시작
		 *  		- 독립적인 트랜잭션이 보장돼야 하는 코드 적용 가능
		 *  
		 *  	- PROPAGATION_NOT_SUPPORTED
		 *  		- 트랜잭션 없이 동작하도록 할 수 있음
		 *  		- 진행 중인 트랜잭션이 있어도 무시
		 *  		- 이유
		 *  			1) 트랜잭션 경계설정은 보통 AOP를 이용해 한 번에 많은 메소드를 동시 적용
		 *  			   그 중 특별한 메소드만 트랜잭션 적용에서 제외하기 위해
		 *
		 *  2) Isolation Level (격리 수준)
		 *  	- 서버환경에서 여러 개의 트랜잭션이 동시에 진행될 수 있고, 동시에 진행하며 문제 없도록 제어
		 *  	- 가능하다면 모든 트랜잭션이 순차적으로 진행되어 다른 트랜잭션의 작업에 독립적인 것이 좋겠지만
		 *  	  성능상의 불이익이 있음
		 *  
		 *  	- ISOLATION_DEFAULT
		 *  		- datasource에 설정된 default 격리수준을 그대로 유진
		 *  		- DefaultTransactionDefinition의 격리수준
		 *  
		 *  3) timeout (제한시간)
		 *  	- DefaultTransactionDefinition
		 *  		- 제한시간 없음
		 *  	- 트랜잭션을 직접 시작할 수 있는 속성 (PROPAGATION_REQUIRED or PROPAGATION_REQUIRES_NEW)
		 *  	  과 함께해야 의미 있음
		 *  
		 *  4) read only (읽기전용)
		 *  	- 트랜잭션 내에서 데이터를 조작하는 시도를 막음
		 *  	- 떼이터 액세스 기술에 따라 성능 향상
		 */
		
		/*  
		 * - 속성 설정
		 *  	- 트랜잭션 경계설정 코드를 가진 TransactionAdvice에서 TransactionDefinition interface의 구현체를
		 *  	  DI 받아 사용하면 됨, 하지만 이 방법은 모든 트랜잭션의 속성이 바뀐다는 문제가 있음
		 *  	- advice 기능을 확장해 해결
		 *  		- 메소드 이름 패턴에 따라 다른 트랜잭션 정의가 적용되도록 함
		 *  
		 *  
		 * TransactionInterceptor
		 *  - 트랜잭션 경계설정 advice로 사용할 수 있음
		 *  - 동작방식은 TransactionAdvice와 동일, 트랜잭션 정ㅇ늬를 메소드 이름패턴에서 다르게 지정하도록 추가지원
		 *  - properties type
		 *  	- PlatformTransactionManager
		 *  	- Properties
		 *  		- 이름은 transactionAttributes, 트랜잭션 속성을 정의한 property
		 *  		- TransctionAttribute interface
		 *  			- TransactionDefinition (4가지 속성) + rollbackOn()
		 *  			- rollbackOn() 메소드	
		 *  				- 어떤 예외가 발생시 롤백여부를 결정하는 메소드
		 *  			- 트랜잭션 부가기능의 동작을 모두 제어 가능
		 * 
		 * TransactionAdvice
		 *  - RuntimeException이 발생하는 경우만 트랜잭션을 롤백시킴
		 *  - 모든 종류의 예외에 대해 트랜잭션을 롤백시켜서는 안됨
		 *  	- 비지니스 로직상 예외 경우를 나타내기 위해 타깃 오브젝트가 체크 예외를 던지는 경우엔
		 *  	  DB 트랜잭션은 커밋 시켜야 하기 때문
		 *  
		 *  	- TransactionInterceptor는 두가지 종류의 예외 처리방식
		 *  		1) runtime 예외
		 *  			- 트랜잭션 롤백
		 *  		2) 체크 예외
		 *  			- 예외 상황으로 해석하지 않고, 비지니스 로직에 따른 의미가 있는 리턴 방식의 한 가지로 인식
		 *  			- 트랜잭션 커밋
		 *  			- 스프링 예외처리 원칙
		 *  				- 비지니스적인 의미가 있는 예외상황에서 체크예외 사용	
		 *  				- 그 외의 모든 복구 불가능한 순수한 예외의 경우 런타임예외로 포장해 전달하는 방식을 따른다고 가정
		 *  		- 위 기본원칙을 따르지 않을 수 있음
		 *  			- rollbackOn() 속성으로 기본 원칙과 다른 예외 처리 가능
		 *  			- 특정 체크 예외의 경우 트랜잭션 롤백 / 특정 런타임 예외시 트랜잭션 커밋 가능
		 *  
		 * 메소드 이름 패턴 사용
		 *  - Properties type의 transactionAttributes property는 메소드 패턴과 트랜잭션 속성을 키/값으로 갖는 컬랙션
			
				PROPAGATION_NAME, ISOLATION_NAME, readOnly, timeout_NNNN, -Exception1, +Exception2
				
				PROPAGATION_NAME
					- 트랜잭션 전파방식, 필수 항목
				ISOLATION_NAME
					- 격리수준, 생략 가능, 생략시 디폴트 격리
				readOnly
					- 읽기전용항목, 생략 가능, 디폴트는 읽기전용 아님
				timeout_NNNN
					- 제한시간, 초단위 시간을 붙임, 생략가능
				-Exception1
					- 체크예외중, 롤백대상으로 추가할 것, 한개이상 가능
				+Exception2
					- 런타임예외나 롤백시키지 않을 예외들, 한개이상 가능
			
		 * 주의사항
		 *  - Proxy 방식 AOP는 같은 target 오브젝트 내의 메소드를 호출할 때는 적용되지 않음
		 *  	- 타깃 오브젝트 내의 메소드 호출시 전파속성이 REQUIRES_NEW라 했더라도
		 *  	  타깃 오브젝트의 메소드는 이전 트랜잭션에 단순 참여할 뿐
		 *  - 클라이언트로부터 호출이 일어날 때만 가능
		 *  	- 클라이언트를 인터페이스를 통해 타킷 오브젝트를 사용하는 다른 오브젝트
		 */
		TransactionStatus status = this.transactionManager.getTransaction(new DefaultTransactionDefinition());
		
		try {
			Object ret = invocation.proceed();
			
			this.transactionManager.commit(status);
			return ret;
			
		} catch (RuntimeException e) {
			this.transactionManager.rollback(status);
			throw e;
		}
		
	}
}










