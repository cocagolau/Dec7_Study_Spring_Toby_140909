package me.dec7.learningtest.template;

import java.io.BufferedReader;
import java.io.IOException;


public interface BufferedReaderCallback {
	
	Integer doSomethingWithReader(BufferedReader br) throws IOException; 
}
