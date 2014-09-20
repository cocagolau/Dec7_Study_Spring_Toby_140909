package me.dec7.user.service;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class TransactionHandler implements InvocationHandler {
	/*
	 *  부가기능을 제공할 타깃 오브젝트
	 *  Object 타입이므로 어떤 타입이근 적용 가능
	 */
	private Object target;
	
	// 트랜젝션 기능을 제공하는데 필요한 트랜잭션 매니저
	private PlatformTransactionManager transactionManager;
	
	// 트랜잭션을 적용할 메소드 이름 패턴
	private String pattern;
	
	public void setTarget(Object target) {
		this.target = target;
	}
	
	// 트랜잭션 추상화 interface를 DI
	public void setTransactionManager(PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}
	
	// 타깃 오브젝트의 모든 메소드에 무조건 트랜잭션이 적용되지 않도록 적용할 메소드 이름의 패턴을 DI받음
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}


	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		// 트랜잭션 적용 대상 메소드 선별, 트랜잭션 경계설정 기능 부여
		
		// 특정 패턴을 지닌 메소드만 드랜잭션을 적용하는 메소드 호출
		if (method.getName().startsWith(pattern)) {
			return invokeTransaction(method, args);
		// 그렇지 않은 경우 부가기능 없이 메소드 호출
		} else {
			return method.invoke(target, args);
		}
	}

	private Object invokeTransaction(Method method, Object[] args) throws Throwable {
		// 부가기능 수행
		TransactionStatus status = this.transactionManager.getTransaction(new DefaultTransactionDefinition());
		
		try {
			// 위임
			/*
			 *  트랜잭션을 시작하고 타깃 오브젝트의 메소드 호출
			 *  예외 발생하지 않앗다면 커밋
			 */
			Object ret = method.invoke(target, args);
			
			
			// 부가기능 수행
			this.transactionManager.commit(status);
			return ret;
		
		/*
		 * Rallback을 적용하기 위해
		 * RuntimeException이 아닌 InvocationTargetException을 잡아야함
		 *  - Method.invoke() 사용해 target 오브젝트 메소드 호출시
		 *    타깃 오브젝트에서 발생하는 예외가 InvocationTargetException으로 한번 포장되기 때문에
		 * 
		 * 
		 */
		} catch (InvocationTargetException e) {
			
			// 예외 발생시 트랜잭션 롤백
			this.transactionManager.rollback(status);
			throw e.getTargetException();
		}
	}

}
