package me.dec7.learningtest.spring.factorybean;

import org.springframework.beans.factory.FactoryBean;

/*
 * Message 클래스 오브젝트를 생성해주는
 * FactoryBean Class
 * 
 */
public class MessageFactoryBean implements FactoryBean<Message> {
	
	String text;
	
	/*
	 * 오브젝트 생성시
	 * 필요한 정보를 FactoryBean의 property로 설정해서
	 * 대신 DI받을 수 있게 함
	 *  - 주입된 정보는 오브젝트 생성중 사용됨
	 */
	public void setText(String text) {
		this.text = text;
	}
	
	/*
	 * 실제 빈으로 사용될 오브젝트를 직접 생성
	 * 코드를 사용하므로 복잡한 방식의 생성, 초기화 작업 가능
	 */
	@Override
	public Message getObject() throws Exception {
		
		return Message.newMessage(text);
	}

	@Override
	public Class<?> getObjectType() {
		
		return Message.class;
	}
	
	/*
	 * getObject()가 돌려주는 오브젝트가 싱글톤인지 알려줌
	 * 
	 * 이 FactoryBean은 매번 요청할 때마다 새로운 오브젝트를 만드므로 false로 설정
	 * 이것은 FactoryBean의 동작방식에 대한 설정이고
	 * 만들어진 bean 오브젝트는 싱글톤으로 spring이 관리해줄수 있음
	 */
	@Override
	public boolean isSingleton() {
		// TODO Auto-generated method stub
		return false;
	}

}
