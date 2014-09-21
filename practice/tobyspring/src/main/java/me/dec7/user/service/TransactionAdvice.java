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










