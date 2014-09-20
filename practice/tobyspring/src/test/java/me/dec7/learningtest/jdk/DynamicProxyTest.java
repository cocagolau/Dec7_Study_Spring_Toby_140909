package me.dec7.learningtest.jdk;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Proxy;

import me.dec7.learningtest.jdk.Hello;
import me.dec7.learningtest.jdk.HelloTarget;
import me.dec7.learningtest.jdk.UppercaseHandler;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.junit.Test;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.NameMatchMethodPointcut;

public class DynamicProxyTest {

	@Test
	public void simpleProxy() {
		// JDK dymanic proxy 생성
		Hello proxiedHello = (Hello) Proxy.newProxyInstance(
				getClass().getClassLoader(),
				new Class[] { Hello.class },
				new UppercaseHandler(new HelloTarget())
			);
		
	}
	
	@Test
	public void proxyFactoryBean() {
		/* 
		 * ProxyFactoryBean
		 *  - 작은 단위의 템플릿/콜백 구조를 응용해 적용하여 템플릿 역할을 하는 MethodInvocation을 싱글톤으로 두고 공유 가능
		 *  
		 * addAdvice()
		 *  - MethodInterceptor를 설정시 일반적으로 DI처럼 수정자 메소드를 사용하지 않음
		 *  - ProxyFactoryBean은 여러개의 <advice> MethodInterceptor를 추가할 수 있음
		 *  - 다양한 부가기능이 추가되더라도 ProxyFactoryBean 하나로 충분
		 *  - MethodInterceptor는 Advice interce를 상속하는 subinterface이므로
		 *  	- spring은 method 실행을 가로채는 방식 외에도 부가기능을 추가하는 여러가지 기능이 있음
		 *  - Advice
		 *  	- target 오브젝트에 적용하는 부가기능을 담은 오브젝트
		 * 
		 * Interface를 적용해 주는 부분이 사라짐
		 *  - 물론 필요하지만 ProxyFactoryBean에 있는 interface 자동검출 기능으로 사용해
		 *    target 오브젝트의 interface 정보를 알아냄 / 직접 제공할 수도 있음
		 */
		ProxyFactoryBean pfBean = new ProxyFactoryBean();
		
		// target 설정
		pfBean.setTarget(new HelloTarget());
		
		/*
		 *  부가기능을 담은 어드바이스 추가
		 *  여러개 추가 가능 
		 */
		pfBean.addAdvice(new UppercaseAdvice());
		
		// FactoryBVean이므로 getObject() 로 생성된 Proxy를 가져옴
		Hello proxiedHello = (Hello) pfBean.getObject();
		
		assertThat(proxiedHello.sayHello("Dec7"), is("HELLO DEC7"));
		assertThat(proxiedHello.sayHi("Dec7"), is("HI DEC7"));
		assertThat(proxiedHello.sayThankYou("Dec7"), is("THANKYOU DEC7"));
		
	}
	
	/*
	 * Advice
	 *  - target이 필요없는 순수한 부가기능
	 *  - MethodInterceptor는 메소드 정보 + target 오브젝트가 담긴 MethodInvocation 오브젝트가 전달
	 *  	- MethodInvocation
	 *  		- target 오브젝트의 메소드를 실행할 수 있는 기능 있음
	 *  		- 일종의 콜백 오브젝트, proceed() 실행시 target 오브젝트의 메소드를 내부적으로 실행해주는 기능 있음
	 *  		- MethodInvocation 구현 클래스는 공유 가능한 템플릿처럼 동작
	 */
	
	/*
	 * 포인트컷
	 *  - 부가기능 적용 대상 메소드 선정법
	 *  - TxProxyFactoryBean은 pattern으로 적용대상 메소드를 선정
	 *  - MethodInterceptor 오브젝트는 여러 proxy가 공유해서 사용가능하므로
	 *    target 정보를 가지지 않고, 따라서 적용대상 method이름 pattern을 넣는것은 곤란
	 *    
	 *  - MethodInterceptor는 InvocationHandler와 다르게 Proxy가 client로부터 받는 요청을 일일이 전달할 필요 없음
	 *    MethodInterceptor에는 재사용 가능한 순수한 부가기능 코드만 남겨주는 것
	 *    대신 Proxy에는 부가기능 적용 메소드를 선택하는 기능을 넣음
	 *  - Proxy의 핵심 가치는 target을 대신해서 client의 요청을 받아 처리한는 오브젝트로서의 존재자체임
	 *    따라서, 메소드를 선별하는 기능은 Proxy와 분리하는 것이 좋음
	 *    메소드를 선정하는 것은 전략패턴을 사용할 수 있음
	 *    
	 * JDK dynamic Proxy 방식
	 *  - ProxyFactoryBean이 Dynamic Proxy 생성
	 *  - Dynamic Proxy는 InvocationHandler를 통해 모든 메소드에 요청
	 *  - InvocationHandler는 자체적으로 부가기능을 추가하고 메소드 선정 알고리즘을 사용
	 *  - 그리고 target에 위임
	 *  - 문제점
	 *  	- 부가기능을 가진 InvocationHandler가 target / method 선정 알고리즘을 의존
	 *  	- 따라서 InvocationHandler는 특정 target을 위한 proxy에 제한
	 *  	- 그래서 따로 bean으로 등록하지 않고, TxProxyFactoryBean에서 매번 생성토록 함
	 *  
	 * ProxyFactoryBean 방식 
	 *  - advice: 부가기능을 제공하는 오브젝트
	 *  - pointcut
	 *  	- 메소드 선정 알고리즘
	 *  	- Pointcut interface를 구현해서 만ㄷ름
	 *  - advice, pointcut 모두 proxy에 DI되어 사용, bean으로 등록되어 여러 proxy에 공유 가능
	 *  
	 *  1) proxy는 client에게 요청 받으면 pointcut에게 부가기능을 부여할 메소드인지 확인 요청
	 *  2) MethodInterceptor 타입의 advice를 호출
	 *  	- advice는 JDK의 dynamic proxy의 InvocationHandler와 다르게 직접 타깃을 호출하지 않음
	 *  	- 자신은 target정보를 가지지 않음
	 *  	- target을 직접 의존하지 않도록 템플릿 구조로 됨
	 *  	- advice가 부가기능을 부여하는 중에 target메소드의 호출이 필요할 경우
	 *        proxy로부터 전달받은 MethodInvocation 타입 콜백오브젝트의 proceed() 메소드를 호출하면 됨
	 *  
	 * 
	 * 
	 * 
	 */
	static class UppercaseAdvice implements MethodInterceptor {

		@Override
		public Object invoke(MethodInvocation invocation) throws Throwable {
			/*
			 * reflection의 Method와 다르게 메소드 실행시 target 오브젝트를 전달할 필요가 없음
			 * MethodInvocation은 메소드 정보와 함께 Target 오브젝트를 알고 있기 때문
			 */
			String ret = (String)invocation.proceed();
			
			return ret.toUpperCase();
		}
	
	}
	
	// target과 proxy가 구현할 interface
	/*
	static interface Hello {
		String sayHello(String name);
		String sayHi(String name);
		String sayThankYou(String name);
	}
	
	static class HelloTarget implements Hello {

		@Override
		public String sayHello(String name) {
			
			return "Hello " + name;
		}

		@Override
		public String sayHi(String name) {
			
			return "Hi " + name;
		}

		@Override
		public String sayThankYou(String name) {
			
			return "ThankYou " + name;
		}
		
	}
	*/
	
	
	/*
	 * poincut이 적용된 ProxyFactoryBean
	 */
	@Test
	public void pointcutAdvisor() {
		ProxyFactoryBean pfBean = new ProxyFactoryBean();
		pfBean.setTarget(new HelloTarget());
		
		// 메소드 이름을 비교해서 대상을 선정하는 알고리즘을 제공하는 포인트컷 생성
		NameMatchMethodPointcut pointcut = new NameMatchMethodPointcut();
		
		// 이름 비교조건설정
		pointcut.setMappedName("sayH*");
		
		// ProxyFactoryBean에 pointcut, advice를 모두 추가
		/*
		 * pointcut이 필요없을 때는 ProxyFactoryBean의 addAddvice()에 advice만 등록
		 * 하지만, pointcut + advice를 등록하기 위해서 서로 묶은 Advisor 타입으로 호출해야함
		 * 	- ProxyFactoryBean에는 여러개의 advice와 pointcut이 추가될 수 있고,
		 *    따로 등록하게 되면 서로의 관계가 애매해지기 때문
		 *    
		Advisor = Pointcut <메소드 선정 알고리즘> + Advice <부가기능>
		 * 
		 */
		pfBean.addAdvisor(new DefaultPointcutAdvisor(pointcut, new UppercaseAdvice()));
		
		Hello proxiedHello = (Hello) pfBean.getObject();
		
		assertThat(proxiedHello.sayHello("Dec7"), is("HELLO DEC7"));
		assertThat(proxiedHello.sayHi("Dec7"), is("HI DEC7"));
		
		// 메소드이름 pointcut의 선정조건에 맞지 않으므로 부가기능 적용 안됨
		assertThat(proxiedHello.sayThankYou("Dec7"), is(not("THANKYOU DEC7")));
	}
	
}


















