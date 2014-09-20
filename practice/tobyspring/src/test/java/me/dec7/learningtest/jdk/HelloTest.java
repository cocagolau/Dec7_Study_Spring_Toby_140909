package me.dec7.learningtest.jdk;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Proxy;

import org.junit.Test;

public class HelloTest {

	
	/*
	 * Hello interface를 통해
	 * HelloTarget 오브젝트를 사용하는 Client 역할하는
	 * 간단한 테스트 구현
	 */
	@Test
	public void simpleProxy() {
		
		// Target은 interface를 통해 접근하는 습관을 길러야함
		Hello hello = new HelloTarget();
		assertThat(hello.sayHello("Dec7"), is("Hello Dec7"));
		assertThat(hello.sayHi("Dec7"), is("Hi Dec7"));
		assertThat(hello.sayThankYou("Dec7"), is("ThankYou Dec7"));
	}
	
	@Test
	public void helloUppercase() {
		/*
		 * 프록시를 통해 타깃 오브젝트에 접근토록 구성
		 */
		// Hello proxiedHello = new HelloUppercase(new HelloTarget());
		
		/*
		 *  생성된 다이나믹 프록시 오브젝트는 Hello interface를 구현하므로
		 *  Hello 타입으로 캐스팅해도 안전
		 */
		Hello proxiedHello = (Hello) Proxy.newProxyInstance(
				// 다이나믹 프록시 클래스로딩에 사용할 클래스로더
				getClass().getClassLoader(),
				/*
				 *  구현할 interface
				 *  다이나믹 프록시는 한번에 하나 이상의 interface를 구현할 수도 있음
				 *   - 따라서 배열을 사용해 전달
				 */
				new Class[] { Hello.class },
				// 부가기능과 위임 코드를 담은 InvocationHandler
				new UppercaseHandler(new HelloTarget())
				);
		
		assertThat(proxiedHello.sayHello("Dec7"), is("HELLO DEC7"));
		assertThat(proxiedHello.sayHi("Dec7"), is("HI DEC7"));
		assertThat(proxiedHello.sayThankYou("Dec7"), is("THANKYOU DEC7"));
	}

}
