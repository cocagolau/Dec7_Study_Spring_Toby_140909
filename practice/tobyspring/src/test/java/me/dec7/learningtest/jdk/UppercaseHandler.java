package me.dec7.learningtest.jdk;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;



/*
 * Dynamic Proxy 적용
 * 
 * 동작방식
 *  - Proxy Factory에 의해 runtime시 다이나믹하게 만들어지는 오브젝트
 *  	- Dynamic Proxy 오브젝트는 target 오브젝트와 같은 interface타입으로 만들어짐
 *  	- Client는 Dynamic Proxy 오브젝트를 target intarface를 통해 사용할 수 있음
 *  - Proxy Factory는 interface만 제공시 해당 interface를 구현한 class의 오브젝트를 자동으로 만들어줌
 *  - 부가기능은 프록시 오브젝트와 독립적으로 InvocationHandler를 구현한 오브젝트에 담음
 *  	- Dynamaic Proxy가 Interface 구현 클래스의 오브젝트를 만들어주지만
 *        Proxy로 필요한 부가기근ㅇ 제공 코드는 직접 작성
 *      - InvocationHandler interface는 
 *       public Object invoke(Object proxy, Method method, Object[] args) 만 가짐
 *       	- 다이나믹 프록시 오브젝트는 Client의 모든 요청을 리플렉션 정보로 변화해 InvocationHandler 구현 오브젝트의 invoke() 메소드로 전달
 *       	- target interface의 모든 메소드 요청이 하나의 method로 집중 --> 중복제거
 * 
 *  
 * 장점
 *  - InvocationHandler와 Dynamic Proxy를 생성/사용하는 코드를 손댈 필요 없음
 *  	- 다이나믹 프록시가 만들어질 때 추가된 메소드가 자동으로 포함될 것.
 *  	- 부가기능은 invoke() 메소드에 의해 처리
 *  - target의 종류에 상관없이 적용 가능
 *  	- reflection의 Method interface를 사용해 target의 메소드를 호출하는 것이므로
 *  	- interface에 관계없이 반환값이 String이 경우만 대분자로 결과를 바꾸도록 할 수 있음
 *  
 */

/*
 * 다이나믹 프록시로부터 요청을 전달받기 위해 InvocationHandler를 구현함
 * 다이나믹 프록시가 클라이언트로 받는 모든 요청은 invoke() 메소드로 전달됨
 * 
 * 다이나믹 프록시를 통해 요청이 전달되면 reflection api를 통해 target 오브젝트의 메소드를 호출
 */
public class UppercaseHandler implements InvocationHandler {
	// 확장된 UppercaseHandler
	
	// Hello target;
	Object target;
	
	/*
	 * 다이나믹 프록시로부터 전달받은 요청을 다시 타깃 오브젝트에 위임해야하므로
	 * 타깃 오브젝트를 주입받아 둠 
	 */
	// 어떤 종류의 interface를 구현한 target도 적용가능하도록 Object 타입으로 수정
	public UppercaseHandler(Object target) {
		this.target = target;
	}
	
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		// 타깃으로 위임, 인터페이스 메소드 호출에 모두 적용
		// String ret = (String) method.invoke(target, args);
		Object ret = method.invoke(target, args);
		
		// 리턴타입이 String인 경우만 대문자를 변경 && say로 시작하는 메소드
		if (ret instanceof String && method.getName().startsWith("say")) {
			return ((String) ret).toUpperCase();
		} else {
			return ret;
		}
		
		// 부가기능 제공
		//return ret.toUpperCase();
	}
	
	

}
