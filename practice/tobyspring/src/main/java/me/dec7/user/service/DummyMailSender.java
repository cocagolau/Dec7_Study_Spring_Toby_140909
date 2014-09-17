package me.dec7.user.service;

import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

/*
 * 아무기능이 없는 DummyMailSender
 * 테스트 설정파일에서 mailSender의 bean 클래스로 변경
 */

/*
 * 5.4, 테스트 대역
 * 
 * DummyMailSender는 하는일은 없지만 가진 가치는 매우 큼
 *  - JavaMail로 메일을 직접 발송하는 클래스를 대치하지 않은 경우 테스트는 매우 불편했을 것
 *  - 테스트할 대상이 의존하고 있는 오브젝트를 DI로 바꿔치기 하는 기법이 테스트를 편리하게함\
 *  
 * 의존 오브젝트 변경을 통한 테스트 방법
 *  - UserDao는 DB와 연결돼 작동, 하지만 테스트시 DB연결은 짐
 *  - 테스트 대상이 되는 코드를 수정하지 않고 메일 발송 작업때문에 UserService 자체에 대한 테스트에 지장이 생기지 않도록 한것이 DummyMailSender임
 *  - 테스트 대상 오브젝트가 의존 오브젝트를 가진경우 테스트상 문제점이 많음 --> DI가 큰 힘이 됨
 *  
 * 일반적 테스트
 *  - 보통 어떤 시스템에 입력을 주었을 때 기대하는 출력이 나오는지 검증
 *  - 단위테스트
 *  	- 보통 입력 값을 데스트 대상 오브젝트의 메소드의 파라미터로 전달
 *  	- 메소드의 리턴 값을 출력값으로 보고 검증
 *  
 * 테스트 대역
 *  - 테스트 환경을 만들어주기 위해
 *    테스트 대상이 되는 오브젝트의 기능에만 충실하게 수행하며
 *    빠르게, 자주 테스트를 실행할 수 있도록 사용하는 오브젝트
 *  - 테스트 스텁 (test stub)
 *  	- 대표적인 테스트 대역
 *  	- 간접적인 출력 값을 받게 할 수 있음
 *  	- DummyMailSender는 테스트 오브젝트에 돌려주는 것이 없지만
 *  	  테스트 오브젝트인 UserService로부터 전달 받는 것이 있음
 *  	- 목 오브젝트 (mock object) 필요
 *  		- 테스트 대상 오브젝트의 메소드가 돌려주는 결과 검증시
 *  		- 테스트 오브젝트가 간접적으로 의존 오브젝트에 넘기는 값과 그 행위 검증
 *  
 * Mock Object
 *  - stub 처럼 테스트 오브젝트가 정상적으로 실행되도록 도와줌
 *  - 테스트 오브젝으와 자신의 사이에서 일어나는 커뮤니케이션 내용을 저장한 뒤
 *    테스트 결과를 검증할 수 있도록 함
 *  - 테스트 대상 오브젝으는 테스트뿐 아니라 목 오브젝트(의존 오브젝트)와도 커뮤니케이션 함
 *  - 때로는 테스트 대상 오브젝트가 의존 오브젝트에게 출력한 값에 관심을 갖기도 함
 *  - me.dec7.user.dao.UserServiceTest 참고 
 */
public class DummyMailSender implements MailSender {

	@Override
	public void send(SimpleMailMessage arg0) throws MailException {
		// TODO Auto-generated method stub

	}

	@Override
	public void send(SimpleMailMessage[] arg0) throws MailException {
		// TODO Auto-generated method stub

	}

}
