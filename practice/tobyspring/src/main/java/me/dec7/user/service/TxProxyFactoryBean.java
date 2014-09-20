package me.dec7.user.service;

import java.lang.reflect.Proxy;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.transaction.PlatformTransactionManager;


/*
 * 다이나믹 프록시를 만들어주는 팩토리 빈
 * 
 * Proxy의 newProxyInstance() 메소드를 통해서만 다이나믹 프록시를 생성 가능
 * 위 방법은 스프링빈으로 등록 불가 
 */

/*
 * Proxy Factory Bena
 * 
 *  - 재사용
 *  	- TransactionHandler를 이용하는 TxProxyFactoryBean은 코드 수정 없이 다양한 클래스 적용 가능

	<bean id="coreService" class="complex.module.CoreServiceImpl">
		<property name="coreDao" ref="coreDao" />
	</bean>
	
	---------------------------------------------------------------
	
	<bean id="coreServiceTarget" class="complex.module.CoreServiceImpl">
		<property name="coreDao" ref="coreDao" />
	</bean>
	
	<bean id="coreService" class="me.dec7.service.TxProxyFactoryBean">
		<property name="target" rewf="coreServiceTarget" />
		<property name="transactionManager" ref="transactionManager" />
		<property name="pattern" value="" />
		<property name="serviceInterface" value="complex.module.CoreService" />
	</bean>


 * 장점
 *  1. 다이나믹 프록시 사용시 타깃 interface를 일일이 구현하지 않아도 됨
 *  2. handlerMethod 구현만으로 다양한 메소드에 부가기능 부여 가능 --> 코드 중복 사라짐
 *  3. Dynamic Proxy에 FactoryBean을 사용한 DI 추가시, DP 생성코드도 제거 가능
 *  
 * 한계
 *  1. Proxy를 통해 Target에 부가기능을 제공하는 것은 Method 단위로 일어남
 *     하나의 클래스에 존재하는 여러개의 메소드에 부가기능을 한번에 제공하는 것은 쉽게 함
 *     하지만, 한번에 여러 개의 클래스에 공통적인 부가기능을 부여하는 것은 지금 방법으로 불가능
 *     하나의 타깃 오브젝트에만 부여되는 부가기능이라면 상관없지만
 *     트랜잭션과 같이 비지니스 로직을 담은 많은 클래스의 메소드에 적용할 필요가 있다면 Proxy Factory Bean의 설정이 중복되는 것은 못막음
 *  2. 하나의 타깃에 여러개의 부가기능 적용시 설정파일의 양이 비대해짐
 *	   설정파일로 쉽게 기능을 추가할 수 있는 점은 긍정적이나, 복잡해지는 것은 안 좋음
 *  3. TransactionHandler 오브젝트가 ProxyFactoryBean 개수만큼 만들어짐
 *     동일한 기능임에도 target 오브젝트가 달라지면 새로운 TransactionHandler가 필요 
 *      
 */

public class TxProxyFactoryBean implements FactoryBean<Object> {
	
	Object target;
	// TransactionHandler를 생성시 필요
	PlatformTransactionManager transactionManager;
	String pattern;
	/*
	 * 다이나믹 프록시를 생성시 필요
	 * UserService외 interface를 가진 타겟도 적용할 수 있음
	 */
	Class<?> serviceInterface;
	
	
	
	public void setTarget(Object target) {
		this.target = target;
	}

	public void setTransactionManager(PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public void setServiceInterface(Class<?> serviceInterface) {
		this.serviceInterface = serviceInterface;
	}

	
	// FactoryBean interface 구현 메소드
	@Override
	public Object getObject() throws Exception {
		/*
		 *  DI받은 정보를 바탕으로
		 *  TransactionHandler를 사용하는 다이나믹 프록시를 생성
		 */
		TransactionHandler txHandler = new TransactionHandler();
		txHandler.setTarget(target);
		 txHandler.setTransactionManager(transactionManager);
		 txHandler.setPattern(pattern);
		 
		 return Proxy.newProxyInstance(
				 	getClass().getClassLoader(),
				 	new Class[] { serviceInterface },
				 	txHandler
				 );
	}

	@Override
	public Class<?> getObjectType() {
		
		return serviceInterface;
	}

	@Override
	public boolean isSingleton() {
		
		return false;
	}

}
