package me.dec7.learningtest.spring.factorybean;

/*
 * Spring의 FactoryBean의 동작원리를 확인할 수 있도록 테스트 제작
 */
public class Message {
	String text;

	// private으로 외부에서 생성자를 통해 오브젝트를 만들 수 없음
	private Message(String text) {
		this.text = text;
	}
	
	public String getText() {
		return text;
	}
	
	/*
	 * spring 설정파일에 bean으로 등록할 때
	 * 생성자를 통해 오브젝트를 생성할 수 없으므로 
	 * 
		<bean id="m" class="me.dec7.learningtest.spring.factorybean.Message">
		
	 * 위 처럼 쓸 수 없음
	 * 
	 * $사실 스프링은 private 생성자를 가진 class도 bean으로 등록시 reflection을 사용해 오브젝트 만들어 줌
	 * 	- reflection은 private으로 선언된 접근 규약을 위반할 수 있기 때문
	 * 	- 하지만, 생성자가 private이라는 것은 
	 * 	  static method를 통해 오브젝트가 만들어져야 하는 중요한 이유가 있으므로
	 *    이를 무시하면 위험
	 */

	// 생성자를 대신 사용할 수 있는 Static factory method 제공
	public static Message newMessage(String text) {
		return new Message(text);
	}
	
	
}
