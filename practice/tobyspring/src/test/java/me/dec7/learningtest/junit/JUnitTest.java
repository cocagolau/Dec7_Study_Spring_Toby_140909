package me.dec7.learningtest.junit;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/*
 * 학습 테스트
 *  - 다른 라이브러리에 대한 테스트를 작성
 *  - 테스트로 라이브러리, 프레임워크의 사용방법 이해
 *  - 자신의 기술, 지식 검증 가능
 *  
 * 장점
 *  - 다양한 조건에 따른 기능을 쉽게 확인 가능
 *  - 학습 테스트 코드를 개발 중 참고 가능
 *  - 프레임워크, 제품 업그레이시 호환성 검증 도와줌
 *  - 테스트 작성에 좋은 훈련 
 */

/*
 * 목적: 테스트시마다 새로운 오브젝트가 생성됨을 확인
 */

/*
 * 동등분할
 *  - 같은 결과를 내는 값의 범위를 구분해서 각 대표값으로 테스트
 *  - true, false, exception
 * 
 * 경계값 분석
 *  - 에러는 경계에서 주로 많이 발생
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="classpath:/junit.xml")
public class JUnitTest {
	
	@Autowired
	// TestContext가 매번 주입하는 ApplicationContext가 항상 같은지 확인
	ApplicationContext context;
	
//	static JUnitTest testObject;
	static Set<JUnitTest> testObjects = new HashSet<JUnitTest>();
	static ApplicationContext contextObject = null;
	
	/*
	 * matcher 설명
	 * 
	 * not()
	 *  - 두에 나오는 결과를 부정
	 *  
	 * is()
	 *  - equals() 비교해서 같으면 성공
	 *  - is(not())은 같지 않아야 성공
	 *  
	 * sameInstance()
	 *  - 동일성 비교 매처
	 *  
	 * hasItem()
	 *  - 컬렉션의 원소인지 검사
	 *  
	 * either()
	 *  - 뒤에 이어 나오는 or()와 함께 두 개 마처의 결과를 OR 조건으로 비교
	 *  
	 * nullValue()
	 *  - 오브젝트가 null인지 확인
	 */
	
	@Test
	public void test1() {
		assertThat(testObjects, not(hasItem(this)));
		testObjects.add(this);
		
		assertThat(contextObject == null || contextObject == this.context, is(true));
		contextObject = this.context;
	}

	@Test
	public void test2() {
		assertThat(testObjects, not(hasItem(this)));
		testObjects.add(this);
		
		assertTrue(contextObject == null || contextObject == this.context);
		contextObject = this.context;
	}
	
	@Test
	public void test3() {
		assertThat(testObjects, not(hasItem(this)));
		testObjects.add(this);
		
		assertThat(contextObject, either(is(nullValue())).or(is(this.contextObject)));
		contextObject = this.context;
	}
}
