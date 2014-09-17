package me.dec7.user.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

public class MockMailSender implements MailSender {
	
	private List<String> requests = new ArrayList<String>();
	
	public List<String> getRequests() {
		return requests;
	}

	@Override
	public void send(SimpleMailMessage mailMessage) throws MailException {
		// 전송 요청받은 이메일 주소를 저장
		// 첫번재 수신자 메일 주소만 저장
		requests.add(mailMessage.getTo()[0]);

	}

	@Override
	public void send(SimpleMailMessage[] mailMessage) throws MailException {

	}

}