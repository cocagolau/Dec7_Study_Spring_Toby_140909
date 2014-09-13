package me.dec7.learningtest.template;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

public class CalcSumTest {
	
	private String numFilePath;
	private Calculator calculator;

	@Before
	public void setUp() {
		this.calculator = new Calculator();
		this.numFilePath = getClass().getResource("numbers.txt").getPath();
	}

	@Test
	public void sumOfNumbers() throws IOException {		
		assertThat(calculator.calcSum5(this.numFilePath), is(10));
	}
	
	@Test
	public void multiplyOfNumbers() throws IOException {		
		assertThat(calculator.calcMultiply5(this.numFilePath), is(24));
	}

	@Test
	public void concatenateStrings() throws IOException {		
		assertThat(calculator.concatenate5(this.numFilePath), is("1234"));
	}
}
