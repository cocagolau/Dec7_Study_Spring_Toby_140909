package me.dec7.learningtest.jdk;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Proxy;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.junit.Test;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.Pointcut;
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
		ProxyFactoryBean pfBean = new ProxyFactoryBean();
		
		// target 설정
		pfBean.setTarget(new HelloTarget());
		pfBean.addAdvice(new UppercaseAdvice());
		
		// FactoryBVean이므로 getObject() 로 생성된 Proxy를 가져옴
		Hello proxiedHello = (Hello) pfBean.getObject();
		
		assertThat(proxiedHello.sayHello("Dec7"), is("HELLO DEC7"));
		assertThat(proxiedHello.sayHi("Dec7"), is("HI DEC7"));
		assertThat(proxiedHello.sayThankYou("Dec7"), is("THANKYOU DEC7"));
		
	}
	
	static class UppercaseAdvice implements MethodInterceptor {

		@Override
		public Object invoke(MethodInvocation invocation) throws Throwable {

			String ret = (String)invocation.proceed();
			
			return ret.toUpperCase();
		}
	
	}
	
	@Test
	public void pointcutAdvisor() {
		ProxyFactoryBean pfBean = new ProxyFactoryBean();
		pfBean.setTarget(new HelloTarget());
		
		// 메소드 이름을 비교해서 대상을 선정하는 알고리즘을 제공하는 포인트컷 생성
		NameMatchMethodPointcut pointcut = new NameMatchMethodPointcut();
		
		// 이름 비교조건설정
		pointcut.setMappedName("sayH*");
		
		pfBean.addAdvisor(new DefaultPointcutAdvisor(pointcut, new UppercaseAdvice()));
		
		Hello proxiedHello = (Hello) pfBean.getObject();
		
		assertThat(proxiedHello.sayHello("Dec7"), is("HELLO DEC7"));
		assertThat(proxiedHello.sayHi("Dec7"), is("HI DEC7"));
		
		// 메소드이름 pointcut의 선정조건에 맞지 않으므로 부가기능 적용 안됨
		assertThat(proxiedHello.sayThankYou("Dec7"), is(not("THANKYOU DEC7")));
	}
	
	
	/*
	 * Bean 후 처리기를 사용한 자동 프록시 생성기
	 *  - BeanPostProcessor
	 *  	- spring은 OCP 개념을 다양하게 적용
	 *  	- spring은 핵심적인 부분을 제외한 나머지는 대부분 확장 포인트 제공
	 *  	- spring bean 오브젝트를 만들어진 후, bean 오브젝트를 재가공
	 *  - DefaultAdvisorAutoProxyCreator
	 *  	- 자동 프록시 생성기
	 *  	- bean 후 처리기 자체를 bean으로 등록
	 *  	- spring은 bean 오브젝트를 만들 때마다 후처리기에 bean을 보냄
	 *  	- DefaultAdvisorAutoProxyCreator는 bean으로 등록된 모든 어드바이저 내의 pointcut을 이용해
	 *        전달받은 bean이 proxy 적용 대상인 확인
	 *        적용 대상시, 내장된 proxy 생성기에 현재 bean에 대한 proxy를 만듦
	 *        만들어진 proxy에 advisor를 연결
	 *        
	 * 확장된 pointcut
	 *  - 두가지 기능
	 *  	1. target 오브젝트의 메소드 중 어떤 메소드에 부가기증을 적용할지 선정하는 역할
	 *  		- NameMatchMethodPointcut는 메소드 선별기능만 가진 특별한 pointcut
	 *  		- 메소드만 선별한 다는 것은 모든 클래스를 다 받아주는 것 
	 *  	2. 포인트 컷이 등록된 빈 중 어떤 빈에 프록시를 적용할지 선택
	 *  		- 두 가지 기능을 모두 적용시 class와 method가 모두 맞아야
	 *  		  target의 method에 advisor가 적용됨
		
		public interface Pointcut {
			// proxy를 적용할 클래스인지 확인
			ClassFilter getClassFilter();
			
			// advisor를 적용할 메소드인지 확인
			MethodMatcher getMethodMatcher();
		}
		
		- ProxyFactoryBean에서 Pointcut을 사용할 때는 이미 target이 정해져 있으므로
		  class를 선별한 필요가 없었음
		- DefaultAdvisorAutoProxyCreator
			- class, method 선정 알고르즘을 모두 가지는 pointcut이 필요함
	
	 *  	
	 */
	@Test
	public void classNamePointcutAdvisor() {
		// pointcut 준비
		NameMatchMethodPointcut classMethodPointcut = new NameMatchMethodPointcut() {
			
			// 내부 익명 class방식으로 class 정ㄴ의
			public ClassFilter getClassFilter() {
				return new ClassFilter() {

					@Override
					public boolean matches(Class<?> clazz) {
						
						// 클래스 이름이 HelloT로 시작하는 것만 선정
						return clazz.getSimpleName().startsWith("HelloT");
						
					}
					
				};
			}
		};
		
		classMethodPointcut.setMappedName("sayH*");
		
		// test
		// 적용 class
		checkAdviced(new HelloTarget(), classMethodPointcut, true);
		
		class HelloWorld extends HelloTarget {};
		// 적용 class 아님
		checkAdviced(new HelloWorld(), classMethodPointcut, false);
		
		class HelloTDec extends HelloTarget {};
		checkAdviced(new HelloTDec(), classMethodPointcut, true);
		
	}

	private void checkAdviced(Object target, Pointcut pointcut, boolean adviced) {
		
		ProxyFactoryBean pfBean = new ProxyFactoryBean();
		pfBean.setTarget(target);
		pfBean.addAdvisor(new DefaultPointcutAdvisor(pointcut, new UppercaseAdvice()));
		
		Hello proxiedHello = (Hello) pfBean.getObject();
		
		if (adviced) {
			assertThat(proxiedHello.sayHello("Dec7"), is("HELLO DEC7"));
			assertThat(proxiedHello.sayHi("Dec7"), is("HI DEC7"));
			assertThat(proxiedHello.sayThankYou("Dec7"), is("ThankYou Dec7"));
			
		} else {
			assertThat(proxiedHello.sayHello("Dec7"), is("Hello Dec7"));
			assertThat(proxiedHello.sayHi("Dec7"), is("Hi Dec7"));
			assertThat(proxiedHello.sayThankYou("Dec7"), is("ThankYou Dec7"));
			
		}
		
	}
	
}


















