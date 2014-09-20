package me.dec7.learningtest.factorybean;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import me.dec7.learningtest.spring.factorybean.Message;
import me.dec7.learningtest.spring.factorybean.MessageFactoryBean;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
//설정파일 이름을 지정하지 않으면.. "클래스이름-context.xml"이 default
@ContextConfiguration("classpath:/FactoryBeanTest-context.xml")
public class FactoryBeanTest {

	@Autowired
	ApplicationContext context;
	
	@Test
	public void getMessageFromFactoryBean() {
		/*
		 *  학습테스트의 message 빈의 타입이 명확하지 않으므로
		 *  context.getBean()으로 bean을 가져옴
		 */
		
		Object message = context.getBean("message");
		
		// 드물지만 factory bean 자체를 가져오고 싶은 경우
		Object factory = context.getBean("&message");
		
		// 타입확인		
		assertThat(factory, is(instanceOf(MessageFactoryBean.class)));
		assertThat(message, is(instanceOf(Message.class)));
		
		// 설정과 기능 확인
		assertThat(((Message)message).getText(), is("Factory Bean"));
	}

}
