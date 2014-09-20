package me.dec7.user.service;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/*
 * Spring의 Advice interface 구현
 */
public class TransactionAdvice implements MethodInterceptor {
	
	PlatformTransactionManager transactionManager;

	public void setTransactionManager(PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	/*
	 * (non-Javadoc)
	 * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
	 * 
	 * target을 호출하는 기능을 가진 콜백 오브젝트를 proxy로부터 받음
	 * advice는 특정 target에 의존하지 않고 재사용 가능
	 */
	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		TransactionStatus status = this.transactionManager.getTransaction(new DefaultTransactionDefinition());
		
		try {
			/*
			 * callback을 호출해서 target의 메소드를 실행,
			 * target 메소드 호출 전후로 필요한 부가기능을 넣을 수 있음
			 * 
			 * 경우에 따라, target이 아예 호출되지 않게하거나 재시도를 위한 반복적 호출도 가능
			 */
			Object ret = invocation.proceed();
			
			this.transactionManager.commit(status);
			return ret;
			
		/*
		 * JDK dynamic proxy가 제공하는 method와 다르게 
		 * spring의 MethodInvocation을 통한 target 호출은 예외가 포장되지 않고 target에서 보낸 그대로 전달
		 */
		} catch (RuntimeException e) {
			this.transactionManager.rollback(status);
			throw e;
		}
		
	}

}
