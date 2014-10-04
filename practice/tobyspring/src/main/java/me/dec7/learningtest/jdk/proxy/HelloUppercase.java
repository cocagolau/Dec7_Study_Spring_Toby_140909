package me.dec7.learningtest.jdk.proxy;


/*
 * Hello interface를 구현 Proxy
 * Proxy에 decorator pattern을 적용해 target인 HelloTarget에 부가기능 추가
 * 
 * 추가할 기능
 *  - 반환하는 문자를 모두 대문자로 바꿔주는 것
 * 
 * SimpleTarget이라는 원본클래스는 그대로 두고
 * 경우에 따라 대문자로 출력이 필요한 경우를 위해 HelloUppercase를 Proxy를 통해 문자를 바꿈
 * 
 * 하는 일
 *  - Hello interface 구현
 *  - hello 타입의 target 오브젝트를 받아서 저장
 *  - Hello interface 구현 메소드는 타깃 오브젝트의 메소드를 호출한 뒤
 *    결과를 문자로 바꿔주는 부가기능을 적용한 뒤 리턴
 * 
 * Proxy가 가지는 일반적인 문제점 2가지
 *  1. interface의 모든 메소드를 구현해 위임하도록 코드를 제작
 *  2. 부가기능은 반환 값을 대문자로 바꾸는 기능이 모든 메소드에 중복돼서 나타남
 */
public class HelloUppercase implements Hello {
	
	/*
	 * 위임할 target 오브젝트
	 * 여기서는 target 클래스의 오브젝트인 것은 알지만 다른 프록시를 추가할 수도 있으므로 interface로 접근
	 */
	Hello hello;
	
	public HelloUppercase(Hello hello) {
		this.hello = hello;
	}

	@Override
	public String sayHello(String name) {
		// 위임과 부가기능 적용
		return hello.sayHello(name).toUpperCase();
	}

	@Override
	public String sayHi(String name) {
		// 위임과 부가기능 적용
		return hello.sayHi(name).toUpperCase();
	}

	@Override
	public String sayThankYou(String name) {
		// 위임과 부가기능 적용
		return hello.sayThankYou(name).toUpperCase();
	}

}
