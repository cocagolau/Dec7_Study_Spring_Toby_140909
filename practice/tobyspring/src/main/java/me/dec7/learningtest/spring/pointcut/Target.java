package me.dec7.learningtest.spring.pointcut;

/*
 * pointcut 표현식
 *  - filter, matcher에서 class와 method의 meta정보를 제공 받음
 *    이름, 정의된 패키지, 파라미터, 리턴값, 부여된 어노테이션, 구현한 인터페이스, 상속한 클래스. 등 알 수 있음
 *  - Pointcut Expression
 *  	- spring은 간단하게 선정 알고리즘을 작성할 수 있음
 *  	- AspectExpressionPointcut
 *  		- class, method의 선정 알고리즘을 한번에 지정할 수 있게함
 *  		AspectJ pointcut 표현식
 */

/*
 * AspectJ 표현식
 * 
 * bean()
 *  - 스프링에서 사용시 bean의 이름으로 비교
 *  - bean(*Service)
 *  	- id가 Service로 끝나는 모든 bean 선택
 *  - @annotation(org.springframework.transaction.annotation.Transactional)
 *  	- 특정 어노테이션 타입, 메소드, 파라미터에 적용되어있는 정보로 메소드 선정
 *  	- @Transactional 어노테이션 사용한 메소드 선정
 * 
 */
public class Target implements TargetInterface {

	@Override
	public void hello() { }

	@Override
	public void hello(String a) { }

	@Override
	public int minus(int a, int b) throws RuntimeException { return 0; }

	@Override
	public int plus(int a, int b) { return 0; }
	
	// Target에서 새롭게 정의
	public void method() { }

}
