package me.dec7.learningtest.jdk;

/*
 *  target 클래스
 */
public class HelloTarget implements Hello {

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
