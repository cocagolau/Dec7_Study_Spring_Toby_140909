package me.dec7.user.service;

import org.springframework.aop.ClassFilter;
import org.springframework.aop.support.NameMatchMethodPointcut;
import org.springframework.util.PatternMatchUtils;

public class NameMatchClassMedthodPointcut extends NameMatchMethodPointcut {

	public void setMappedClassName(String mappedClassName) {
		/*
		 *  모든 클래스를 다 허용하던 DefaultClassFilter를 property로 받은
		 *  클래스 이름을 이용해 필터를 만들어 덮어씌움 
		 */
		this.setClassFilter(new SimpleClassFilter(mappedClassName));
	}
	
	
	static class SimpleClassFilter implements ClassFilter {
		String mappedName;
		
		private SimpleClassFilter(String mappedName) {
			this.mappedName = mappedName;
		}

		@Override
		public boolean matches(Class<?> clazz) {
			
			/*
			 *  와일드 카드가 들어간 문자열 비교를 지원하는 spring의 utility method
			 *  *name, name*, *name* 세가지 방식 지원
			 */
			return PatternMatchUtils.simpleMatch(mappedName, clazz.getSimpleName());
		}

	}
	
}
