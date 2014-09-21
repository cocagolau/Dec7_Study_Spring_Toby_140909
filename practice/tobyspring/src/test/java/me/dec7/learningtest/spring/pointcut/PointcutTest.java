package me.dec7.learningtest.spring.pointcut;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;

/*
 * pointcut expression 문법
 * 
 * execution()
 *  - AspectJ pointcut expression의 포인터컷 지시자 중 주로 대표적인 것
 *  - [] 옵션항목	/ 생략가능
 *  - | 		/ or 조건

	execution([접근제어자 패턴] 리턴-타입패턴 [타입패턴.]메소드-이름패턴 (파라미터-타입패턴 | "..", ...) [throws 예외-예외패턴])
		- 접근제어자
			- public, protected, privatre
			- 생략 가능
		- 리턴
			- 반드시 하나를 지정하거나, *
		- 패키지.클래스
			- 생략가능, 생략시 모든 타입 모두 허용
			- 메소드이름과는 .으로 구분
			- 와일드카드 사용 가능
			- .. 사용시 한번에 어러개의 패키지 선택 가능
		- 메소드이름
			- 필수, 와일드카드 사용 가능
		- 파라미터
			- 타입, 이름
			- 순서없을 경우 ()
			- 순서적용시 ,
			- 개수와 상관없이 모두 허용하는 패턴시 ..
		- 예외
			- 생략가능

 * 
 */
public class PointcutTest {

	@Test
	public void methodSignaturePointcut() throws SecurityException, NoSuchMethodException {
		
		AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
		pointcut.setExpression(
				// Target 클래스의 minus(int,int) 메소드 시그니쳐
				/*
				 * execution()
				 *  - 포인트컷 표현식은 execution() 안에 넣어 작성
				 *  - execution() 메소드는 실행에 대한 포인트컷이라는 의미
				 */
				"execution(public int me.dec7.learningtest.spring.pointcut.Target.minus(int,int) " +
				"throws java.lang.RuntimeException)");
		
		// Target.minus();
		assertThat(
				pointcut.getClassFilter().matches(Target.class) &&
				pointcut.getMethodMatcher().matches(
						Target.class.getMethod("minus", int.class, int.class), null),
			is(true));
		
		// Target.plus()
		assertThat(
				pointcut.getClassFilter().matches(Target.class) &&
				pointcut.getMethodMatcher().matches(
						Target.class.getMethod("plus", int.class, int.class), null),
			is(false));
		
		// Bean.method()
		assertThat(
				pointcut.getClassFilter().matches(Bean.class) &&
				pointcut.getMethodMatcher().matches(
						Target.class.getMethod("method"), null),
			is(false));
		
		/*
		 * execution(int minus(int,int))
		 *  - return: int
		 *  - method: minus
		 *  - parameters: int, int
		 * 
		 * execution(* minus(int,int))
		 *  - return: *
		 *  - method: minus
		 *  - parameters: int, int
		 *  
		 * execution(* minus(..))
		 *  - return: *
		 *  - method: minus
		 *  - parameters: 개수와 타입 무시
		 *  
		 * execution(* *(..))
		 *  - return: *
		 *  - method: *
		 *  - parameters: 개수, 타입 무시
		 * 
		 */
		
	}
	
	@Test
	public void pointcut() throws Exception {
		/*
		 * 모든 메소드를 다 허용하는 표현식
		 * 모든 메소드에 대해 true
		 */
		targetClassPointcutMatches("execution(* *(..))", true, true, true, true, true, true, true);
	}
	
	private void targetClassPointcutMatches(String expression, boolean ... expected) throws Exception {
		pointcutMatches(expression, expected[0], Target.class, "hello");
		pointcutMatches(expression, expected[1], Target.class, "hello", String.class);
		pointcutMatches(expression, expected[2], Target.class, "plus", int.class, int.class);
		pointcutMatches(expression, expected[3], Target.class, "minus", int.class, int.class);
		pointcutMatches(expression, expected[4], Target.class, "method");
		pointcutMatches(expression, expected[5], Bean.class, "method");
	}
	
	private void pointcutMatches(String expression, Boolean expected, Class<?> clazz, String methodName, Class<?> ... args) throws Exception {
		AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
		pointcut.setExpression(expression);
		
		assertThat(
				pointcut.getClassFilter().matches(clazz) &&
				pointcut.getMethodMatcher().matches(clazz.getMethod(methodName, args), null), is(expected)
			);
		
	}

}








